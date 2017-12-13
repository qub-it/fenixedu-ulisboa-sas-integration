package org.fenixedu.ulisboa.integration.sas.domain;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;

public class SasScholarshipData extends SasScholarshipData_Base {
    
    public SasScholarshipData() {
        super();
        super.setBennu(Bennu.getInstance());;
    }
    
    @Atomic
    public void delete() {
        setSasScholarshipCandidacy(null);
        setBennu(null);
        deleteDomainObject();
    }
    
}
