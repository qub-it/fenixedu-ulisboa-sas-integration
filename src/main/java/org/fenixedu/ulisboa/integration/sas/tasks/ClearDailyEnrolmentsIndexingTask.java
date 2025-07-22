package org.fenixedu.ulisboa.integration.sas.tasks;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.ulisboa.integration.sas.service.DailyEnrolmentsIndexing;

@Task(englishTitle = "Clear Daily Enrolments indexing/cache", readOnly = true)
@Deprecated
public class ClearDailyEnrolmentsIndexingTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        DailyEnrolmentsIndexing.performDailyClean();
    }
}
