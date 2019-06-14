package org.fenixedu.ulisboa.integration.sas.tasks.sicabe;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.GrantOwnerType;
import org.fenixedu.academic.domain.student.PersonalIngressionData;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academic.domain.student.StudentStatute;
import org.fenixedu.academic.domain.student.services.StatuteServices;
import org.fenixedu.academic.domain.util.email.Message;
import org.fenixedu.academic.domain.util.email.Recipient;
import org.fenixedu.academic.domain.util.email.ReplyTo;
import org.fenixedu.bennu.SasSpringConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.domain.CandidacyState;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacy;
import org.fenixedu.ulisboa.integration.sas.domain.SocialServicesConfiguration;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

//Force task to be read only and process each report on its own transaction to avoid errors in a report affecting other reports
@Task(englishTitle = "Ingest SAS Scholarships from SICABE", readOnly = true)
public class IngestSasScholarshipSicabe extends CronTask {

    @Override
    public void runTask() throws Exception {

        try {
            final int beforeSasCandidacies = Bennu.getInstance().getSasScholarshipCandidaciesSet().size();
            final long beforeWithStateModified = getNumberOfCandidaciesWithModifiedState();

            final SicabeExternalService sicabe = new SicabeExternalService();
            final ExecutionYear currentExecutionYear = ExecutionYear.readCurrentExecutionYear();
            sicabe.removeAllCandidaciesWithoutRegistrationAndLoadAllSasCandidacies(currentExecutionYear);

            sicabe.processAllSasScholarshipCandidacies(currentExecutionYear);

            final int afterSasCandidacies = Bennu.getInstance().getSasScholarshipCandidaciesSet().size();
            final long afterWithStateModified = getNumberOfCandidaciesWithModifiedState();

            if (beforeSasCandidacies != afterSasCandidacies || beforeWithStateModified != afterWithStateModified) {
                sendEmailForUser(
                        BundleUtil.getString(SasSpringConfiguration.BUNDLE,
                                "sasScholarship.ingestion.task.message.notification.subject"),
                        BundleUtil.getString(SasSpringConfiguration.BUNDLE,
                                "sasScholarship.ingestion.task.message.notification.body",
                                String.valueOf(afterSasCandidacies - beforeSasCandidacies),
                                String.valueOf(afterWithStateModified - beforeWithStateModified)));
            }

            updatePersonalIngressionDataAndAddStatuteType(currentExecutionYear);

        } catch (Throwable e) {
            sendEmailForUser(
                    BundleUtil.getString(SasSpringConfiguration.BUNDLE,
                            "sasScholarship.ingestion.task.message.notification.subject.error"),
                    BundleUtil.getString(SasSpringConfiguration.BUNDLE,
                            "sasScholarship.ingestion.task.message.notification.body.error", ExceptionUtils.getStackTrace(e)));

            throw e;
        }

    }

    @Atomic
    private void updatePersonalIngressionDataAndAddStatuteType(final ExecutionYear currentExecutionYear) {
        final StatuteType sasStatuteType = SocialServicesConfiguration.getInstance().getStatuteTypeSas();
        currentExecutionYear.getSasScholarshipCandidaciesSet().stream()
                .filter(c -> c.getRegistration() != null && c.getCandidacyState() == CandidacyState.DEFERRED).forEach(c -> {
                    updatePersonalIngressionData(c);
                    if (sasStatuteType != null) {
                        assignGrantOwnerStatute(c, sasStatuteType);
                    }
                });
    }

    private void updatePersonalIngressionData(final SasScholarshipCandidacy candidacy) {

        final PersonalIngressionData pid =
                candidacy.getRegistration().getStudent().getPersonalIngressionDataByExecutionYear(candidacy.getExecutionYear());

        if (pid != null && pid.getGrantOwnerType() != GrantOwnerType.HIGHER_EDUCATION_SAS_GRANT_OWNER) {
            pid.setGrantOwnerType(GrantOwnerType.HIGHER_EDUCATION_SAS_GRANT_OWNER);
        }

    }

    private void assignGrantOwnerStatute(final SasScholarshipCandidacy candidacy, final StatuteType statuteType) {
        if (!studentHasStatuteType(candidacy, statuteType)) {
            new StudentStatute(candidacy.getRegistration().getStudent(), statuteType,
                    candidacy.getExecutionYear().getExecutionSemesterFor(1),
                    candidacy.getExecutionYear().getExecutionSemesterFor(2), null, null, "", candidacy.getRegistration());
        }
    }

    private boolean studentHasStatuteType(final SasScholarshipCandidacy candidacy, final StatuteType statuteType) {
        return StatuteServices.findStatuteTypes(candidacy.getRegistration(), candidacy.getExecutionYear()).stream()
                .anyMatch(st -> st == statuteType);
    }

    private long getNumberOfCandidaciesWithModifiedState() {
        return Bennu.getInstance().getSasScholarshipCandidaciesSet().stream().filter(c -> c.isModified()).count();
    }

    @Atomic
    private void sendEmailForUser(final String subject, final String body) {

        Runnable runnable = () -> {
            FenixFramework.atomic(() -> {
                final String emailAddress = Bennu.getInstance().getSocialServicesConfiguration().getEmail();

                new Message(Bennu.getInstance().getSystemSender(), Collections.<ReplyTo> emptyList(),
                        Collections.<Recipient> emptyList(), subject, body,
                        new HashSet<String>(Arrays.asList(emailAddress.split(","))));
            });
        };

        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
