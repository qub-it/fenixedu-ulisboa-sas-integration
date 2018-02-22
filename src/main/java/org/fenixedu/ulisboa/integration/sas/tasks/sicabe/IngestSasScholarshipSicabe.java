package org.fenixedu.ulisboa.integration.sas.tasks.sicabe;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.util.email.Message;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;

//Force task to be read only and process each report on its own transaction to avoid errors in a report affecting other reports
@Task(englishTitle = "Ingest SAS Scholarships from SICABE", readOnly = true)
public class IngestSasScholarshipSicabe extends CronTask {

    @Override
    public void runTask() throws Exception {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.fillAllSasScholarshipCandidacies(ExecutionYear.readCurrentExecutionYear());
        
        sicabe.processAllSasScholarshipCandidacies();

        // process only pending (news) and modified candidacies
        /*final List<SasScholarshipCandidacy> sas2Process =
                SasScholarshipCandidacy.findAll().stream().filter(c -> (c.getState() == SasScholarshipCandidacyState.PENDING
                        || c.getState() == SasScholarshipCandidacyState.MODIFIED)).collect(Collectors.toList());

        sicabe.processSasScholarshipCandidacies(sas2Process);*/
        
        //enviado/processado (excepto anulado) compara entre o novo processamento e o q ja existia
        // - enviados passada a modificado
        // - processado passa a processado! ;)

        // TODO send an email to user
        sendEmailForUser();

    }

    public void sendEmailForUser() {

        final String emailAddress = Bennu.getInstance().getSocialServicesConfiguration().getEmail();

        final String subject = "Fenix Bolsas SAS";
//                BundleUtil.getString(Bundle.CANDIDATE, "label.application.recomentation.upload.notification.email.subject");
        final String body = "TODO_BODY";
//                BundleUtil.getString(Bundle.CANDIDATE, "label.application.recomentation.upload.notification.email.body",
//                        getRecomentation().getName(), getRecomentation().getInstitution());

        new Message(Bennu.getInstance().getSystemSender(), emailAddress, subject, body);
    }

}
