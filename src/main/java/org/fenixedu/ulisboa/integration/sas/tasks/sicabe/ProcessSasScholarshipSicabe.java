package org.fenixedu.ulisboa.integration.sas.tasks.sicabe;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;

//Force task to be read only and process each report on its own transaction to avoid errors in a report affecting other reports
@Task(englishTitle = "Process SAS Scholarships", readOnly = true)
public class ProcessSasScholarshipSicabe extends CronTask {

    @Override
    public void runTask() throws Exception {
        
        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.processAllSasScholarshipCandidacies();
        
        // TODO send an email to user
        

    }

}
