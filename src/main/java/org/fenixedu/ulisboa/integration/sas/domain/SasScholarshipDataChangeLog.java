package org.fenixedu.ulisboa.integration.sas.domain;

import org.fenixedu.bennu.core.domain.Bennu;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class SasScholarshipDataChangeLog extends SasScholarshipDataChangeLog_Base {
    
    public SasScholarshipDataChangeLog() {
        super();
        super.setBennu(Bennu.getInstance());
    }
    
    public SasScholarshipDataChangeLog(DateTime date, String studentNumber, String studentName, String description) {
        super();
        super.setBennu(Bennu.getInstance());
        setDate(date);
        setStudentNumber(studentNumber);
        setStudentName(studentName);
        setDescription(description);
    }
    
    
    @Atomic
    public void delete() {
        setSasScholarshipCandidacy(null);
        setBennu(null);
        deleteDomainObject();
    }
    
}
