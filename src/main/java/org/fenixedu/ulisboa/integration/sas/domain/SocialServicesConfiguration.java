package org.fenixedu.ulisboa.integration.sas.domain;

import java.util.Collection;
import java.util.Set;

import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.bennu.core.domain.Bennu;

public class SocialServicesConfiguration extends SocialServicesConfiguration_Base {

    public SocialServicesConfiguration() {
        super();
        setBennu(Bennu.getInstance());
    }

    public void edit(int numberOfMonthsOfAcademicYear, String email, String institutionCode,
            Collection<IngressionType> ingressionTypesWhichAreDegreeTransfer) {
        SocialServicesConfiguration config = Bennu.getInstance().getSocialServicesConfiguration();
        config.setNumberOfMonthsOfAcademicYear(numberOfMonthsOfAcademicYear);
        config.setEmail(email);
        config.setInstitutionCode(institutionCode);
        Set<IngressionType> ingressionTypeWhichAreDegreeTransferSet =
                config.getIngressionTypeWhichAreDegreeTransferSet();
        ingressionTypeWhichAreDegreeTransferSet.clear();
        ingressionTypeWhichAreDegreeTransferSet.addAll(ingressionTypesWhichAreDegreeTransfer);
    }

    public static SocialServicesConfiguration getInstance() {
        return Bennu.getInstance().getSocialServicesConfiguration();
    }

    //Change visibility of getters
    @Override
    public Set<IngressionType> getIngressionTypeWhichAreDegreeTransferSet() {
        return super.getIngressionTypeWhichAreDegreeTransferSet();
    }

    @Override
    public int getNumberOfMonthsOfAcademicYear() {
        return super.getNumberOfMonthsOfAcademicYear();
    }
    
}
