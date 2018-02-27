package org.fenixedu.ulisboa.integration.sas.service.process;

import org.fenixedu.ulisboa.integration.sas.domain.ScholarshipReportRequest;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.integration.sas.service.registration.report.SasRegistrationHistoryReport;

public class FillScholarshipFirstYearService extends AbstractFillScholarshipService {

    @Override
    protected void fillSpecificInfo(AbstractScholarshipStudentBean bean, SasRegistrationHistoryReport currentYearRegistrationReport,
            ScholarshipReportRequest request) {
        // first time students in cycle should be 1
        bean.setCycleNumberOfEnrolmentsYears(currentYearRegistrationReport.getEnrolmentYearsCount());
    }

}