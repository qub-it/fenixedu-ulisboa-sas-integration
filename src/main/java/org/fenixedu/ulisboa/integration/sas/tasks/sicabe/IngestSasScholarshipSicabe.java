package org.fenixedu.ulisboa.integration.sas.tasks.sicabe;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.util.email.Message;
import org.fenixedu.academic.domain.util.email.Recipient;
import org.fenixedu.academic.domain.util.email.ReplyTo;
import org.fenixedu.bennu.SasSpringConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;

//Force task to be read only and process each report on its own transaction to avoid errors in a report affecting other reports
@Task(englishTitle = "Ingest SAS Scholarships from SICABE", readOnly = true)
public class IngestSasScholarshipSicabe extends CronTask {

    @Override
    public void runTask() throws Exception {

        final int beforeSasCandidacies = Bennu.getInstance().getSasScholarshipCandidaciesSet().size();
        final long beforeWithStateModified = getNumberOfCandidaciesWithModifiedState();

        final SicabeExternalService sicabe = new SicabeExternalService();
        final ExecutionYear currentExecutionYear = ExecutionYear.readCurrentExecutionYear();
        sicabe.removeAllCandidaciesWithoutRegistrationAndLoadAllSasCandidacies(currentExecutionYear);

        sicabe.processAllSasScholarshipCandidacies(currentExecutionYear);

        final int afterSasCandidacies = Bennu.getInstance().getSasScholarshipCandidaciesSet().size();
        final long afterWithStateModified = getNumberOfCandidaciesWithModifiedState();

        if (beforeSasCandidacies != afterSasCandidacies || beforeWithStateModified != afterWithStateModified) {
            sendEmailForUser();
        }

    }

    private long getNumberOfCandidaciesWithModifiedState() {
        return Bennu.getInstance().getSasScholarshipCandidaciesSet().stream().filter(c -> c.isModified()).count();
    }

    public void sendEmailForUser() {

        final String emailAddress = Bennu.getInstance().getSocialServicesConfiguration().getEmail();

        final String subject = BundleUtil.getString(SasSpringConfiguration.BUNDLE, "message.notification.subject");
        final String body = BundleUtil.getString(SasSpringConfiguration.BUNDLE, "message.notification.body");

        new Message(Bennu.getInstance().getSystemSender(), Collections.<ReplyTo> emptyList(), Collections.<Recipient> emptyList(),
                subject, body, new HashSet<String>(Arrays.asList(emailAddress.split(","))));
    }

}
