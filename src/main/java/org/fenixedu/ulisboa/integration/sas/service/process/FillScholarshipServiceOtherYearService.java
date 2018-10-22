package org.fenixedu.ulisboa.integration.sas.service.process;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.ulisboa.integration.sas.domain.SocialServicesConfiguration;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.integration.sas.dto.ScholarshipStudentOtherYearBean;
import org.fenixedu.ulisboa.specifications.domain.services.CurriculumModuleServices;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;
import org.joda.time.YearMonthDay;

public class FillScholarshipServiceOtherYearService extends AbstractFillScholarshipService {

    @Override
    protected void fillSpecificInfo(AbstractScholarshipStudentBean inputBean, Registration registration,
            ExecutionYear requestYear) {

        final ScholarshipStudentOtherYearBean bean = (ScholarshipStudentOtherYearBean) inputBean;

        bean.setNumberOfDegreeChanges(getNumberOfDegreeChanges(registration));
        bean.setHasMadeDegreeChangeOnCurrentYear(hasMadeDegreeChange(registration, requestYear));
        bean.setCycleNumberOfEnrolmentsYearsInIntegralRegime(
                getCycleNumberOfEnrolmentYearsInIntegralRegime(registration, requestYear));
        bean.setNumberOfApprovedEcts(RegistrationServices.getCurriculum(registration, requestYear).getSumEctsCredits());

        final ExecutionYear lastEnrolmentYear = getCycleLastEnrolmentYear(registration, requestYear);
        final Registration lastRegistration = getLastEnrolmentYearRegistration(registration, lastEnrolmentYear);

        if (lastEnrolmentYear != null && lastRegistration != null) {

            bean.setNumberOfEnrolledEctsLastYear(getEnroledCredits(lastRegistration, lastEnrolmentYear));
            bean.setNumberOfApprovedEctsLastYear(getApprovedCredits(lastRegistration, lastEnrolmentYear));

            bean.setLastEnrolmentYear(lastEnrolmentYear.getBeginDateYearMonthDay().getYear());
            bean.setLastEnrolmentCurricularYear(
                    RegistrationServices.getCurricularYear(lastRegistration, lastEnrolmentYear).getResult());

            final StudentCurricularPlan lastRegistrationCurricularPlan =
                    RegistrationServices.getStudentCurricularPlan(lastRegistration, lastEnrolmentYear);
            final YearMonthDay lastAcademicActDate = CurriculumModuleServices
                    .calculateLastAcademicActDate(lastRegistrationCurricularPlan.getRoot(), lastEnrolmentYear, false);
            bean.setLastAcademicActDateLastYear(lastAcademicActDate != null ? lastAcademicActDate.toLocalDate() : lastEnrolmentYear.getEndLocalDate());
        }
    }

    private Integer getNumberOfDegreeChanges(Registration currentRegistration) {
        return (int) getCycleRegistrations(currentRegistration).stream().filter(r -> SocialServicesConfiguration.getInstance()
                .getIngressionTypeWhichAreDegreeTransferSet().contains(r.getIngressionType())).count();
    }

    private Boolean hasMadeDegreeChange(Registration registration, ExecutionYear requestYear) {
        return registration.getStartExecutionYear() == requestYear && SocialServicesConfiguration.getInstance()
                .getIngressionTypeWhichAreDegreeTransferSet().contains(registration.getIngressionType());
    }

    private ExecutionYear getCycleLastEnrolmentYear(Registration registration, ExecutionYear requestYear) {
        return getCycleEnrolmentYears(registration, requestYear.getPreviousExecutionYear()).stream()
                .sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed()).findFirst().orElse(null);
    }

    private Registration getLastEnrolmentYearRegistration(Registration registration, ExecutionYear lastEnrolmentYear) {
        return getCycleRegistrations(registration).stream()
                .filter(r -> RegistrationServices.getEnrolmentYears(r).contains(lastEnrolmentYear))
                .sorted(Registration.COMPARATOR_BY_START_DATE.reversed()).findFirst().orElse(null);
    }

    private Integer getCycleNumberOfEnrolmentYearsInIntegralRegime(Registration registration, ExecutionYear requestYear) {
        return (int) getCycleEnrolmentYears(registration, requestYear).stream().filter(ey -> !registration.isPartialRegime(ey))
                .count();
    }

}