package org.fenixedu.ulisboa.integration.sas.tasks.sicabe;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacy;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacyState;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;

//Force task to be read only and process each report on its own transaction to avoid errors in a report affecting other reports
@Task(englishTitle = "Ingest SAS Scholarships from SICABE", readOnly = true)
public class IngestSasScholarshipSicabe extends CronTask {

    @Override
    public void runTask() throws Exception {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.fillAllSasScholarshipCandidacies(ExecutionYear.readCurrentExecutionYear());


        // process only pending (news) and modified (TODO)
        final List<SasScholarshipCandidacy> sas2Process = SasScholarshipCandidacy.findAll().stream()
                .filter(c -> c.getState() == SasScholarshipCandidacyState.PENDING)
                .collect(Collectors.toList());

        
        sicabe.processSasScholarshipCandidacies(sas2Process);
        
        //enviado-processado (excpeto anulado) compara entre o novo processamento e q ja existia
        // - enviados passada a modificado
        // - processado passa a processado! ;)

        // TODO send an email to user
    }

}
