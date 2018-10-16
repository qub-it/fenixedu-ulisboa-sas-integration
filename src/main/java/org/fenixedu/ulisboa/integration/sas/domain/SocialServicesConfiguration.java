package org.fenixedu.ulisboa.integration.sas.domain;

import java.util.Collection;
import java.util.Set;

import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.specifications.domain.studentCurriculum.CreditsReasonType;

public class SocialServicesConfiguration extends SocialServicesConfiguration_Base {

    public SocialServicesConfiguration() {
        super();
        setBennu(Bennu.getInstance());
    }

    public void edit(int numberOfMonthsOfAcademicYear, String email, String institutionCode,
            Collection<IngressionType> ingressionTypesWhichAreDegreeTransfer,
            Collection<IngressionType> ingressionTypesWithExternalData, Collection<CreditsReasonType> creditsReasonTypes) {
        SocialServicesConfiguration config = Bennu.getInstance().getSocialServicesConfiguration();
        config.setNumberOfMonthsOfAcademicYear(numberOfMonthsOfAcademicYear);
        config.setEmail(email);
        config.setInstitutionCode(institutionCode);

        Set<IngressionType> ingressionTypeWhichAreDegreeTransferSet = config.getIngressionTypeWhichAreDegreeTransferSet();
        ingressionTypeWhichAreDegreeTransferSet.clear();
        ingressionTypeWhichAreDegreeTransferSet.addAll(ingressionTypesWhichAreDegreeTransfer);

        Set<IngressionType> ingressionTypesWithExternalDataSet = config.getIngressionTypesWithExternalDataSet();
        ingressionTypesWithExternalDataSet.clear();
        ingressionTypesWithExternalDataSet.addAll(ingressionTypesWithExternalData);

        Set<CreditsReasonType> creditsReasonType = config.getCreditsReasonTypesSet();
        creditsReasonType.clear();
        creditsReasonType.addAll(creditsReasonTypes);
    }

    public static SocialServicesConfiguration getInstance() {
        return Bennu.getInstance().getSocialServicesConfiguration();
    }

    @Override
    public int getNumberOfMonthsOfAcademicYear() {
        return super.getNumberOfMonthsOfAcademicYear();
    }

    public boolean ingressionTypeRequiresExternalData(final Registration registration) {
        return registration.getIngressionType() != null
                && getIngressionTypesWithExternalDataSet().contains(registration.getIngressionType());
    }

}
