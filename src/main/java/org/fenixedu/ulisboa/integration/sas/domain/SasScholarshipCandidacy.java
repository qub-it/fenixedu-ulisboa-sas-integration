package org.fenixedu.ulisboa.integration.sas.domain;

import java.util.Collection;
import java.util.Iterator;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.specifications.domain.studentCurriculum.CreditsReasonType;

import pt.ist.fenixframework.Atomic;

public class SasScholarshipCandidacy extends SasScholarshipCandidacy_Base {

    public SasScholarshipCandidacy() {
        super();
        super.setBennu(Bennu.getInstance());
    }

    @Atomic
    public void delete() {
        setRegistration(null);
        setExecutionYear(null);

        if (getSasScholarshipData() != null) {
            getSasScholarshipData().delete();

        }
        
        for (final Iterator<SasScholarshipDataChangeLog> iterator = getSasScholarshipDataChangeLogsSet().iterator(); iterator.hasNext();) {

            final SasScholarshipDataChangeLog log = iterator.next();
            iterator.remove();
            log.delete();
        }

        setBennu(null);
        deleteDomainObject();
    }

    
    static public Collection<SasScholarshipCandidacy> findAll() {
        return Bennu.getInstance().getSasScholarshipCandidaciesSet();
    }
    
}
