package org.fenixedu.ulisboa.integration.sas.service.registration.report;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.StatuteType;
import org.fenixedu.academic.domain.student.StudentStatute;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationState;
import org.joda.time.LocalDate;

public class RegistrationHistoryReport {

    private ExecutionInterval executionInterval;

    private Registration registration;

    private StudentCurricularPlan studentCurricularPlan;

    private RegistrationState registrationState;

    private Collection<StatuteType> statuteTypes;

    private Integer firstSemesterEnrolmentsCount;

    private BigDecimal firstSemesterEnroledCredits;

    private BigDecimal firstSemesterApprovedCredits;

    private Integer secondSemesterEnrolmentsCount;

    private BigDecimal secondSemesterEnroledCredits;

    private BigDecimal secondSemesterApprovedCredits;

    private BigDecimal totalEnroledCredits;

    private BigDecimal totalApprovedCredits;

    private Integer totalEnrolmentsCount;

    private RegistrationRegimeType regimeType;

    private BigDecimal equivalenceCredits;

    private BigDecimal approvedCredits;

    private Integer curricularYear;

    private LocalDate enrolmentDate;

    private Integer enrolmentYearsCount;

    private Integer enrolmentYearsInFullRegimeCount;

    private LocalDate lastAcademicActDate;

    public RegistrationHistoryReport(Registration registration, ExecutionInterval executionInterval) {
        this.registration = registration;
        this.executionInterval = executionInterval;
    }

    public ExecutionInterval getExecutionInterval() {
        return executionInterval;
    }

    public void setExecutionInterval(ExecutionInterval executionInterval) {
        this.executionInterval = executionInterval;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public StudentCurricularPlan getStudentCurricularPlan() {
        return studentCurricularPlan;
    }

    public void setStudentCurricularPlan(StudentCurricularPlan studentCurricularPlan) {
        this.studentCurricularPlan = studentCurricularPlan;
    }

    public RegistrationState getRegistrationState() {
        return registrationState;
    }

    public void setRegistrationState(RegistrationState registrationState) {
        this.registrationState = registrationState;
    }

    public Collection<StatuteType> getStatuteTypes() {
        return statuteTypes;
    }

    public void setStatuteTypes(Collection<StatuteType> statuteTypes) {
        this.statuteTypes = statuteTypes;
    }

    public Integer getFirstSemesterEnrolmentsCount() {
        return firstSemesterEnrolmentsCount;
    }

    public void setFirstSemesterEnrolmentsCount(Integer firstSemesterEnrolmentsCount) {
        this.firstSemesterEnrolmentsCount = firstSemesterEnrolmentsCount;
    }

    public BigDecimal getFirstSemesterEnroledCredits() {
        return firstSemesterEnroledCredits;
    }

    public void setFirstSemesterEnroledCredits(BigDecimal firstSemesterEnroledCredits) {
        this.firstSemesterEnroledCredits = firstSemesterEnroledCredits;
    }

    public BigDecimal getFirstSemesterApprovedCredits() {
        return firstSemesterApprovedCredits;
    }

    public void setFirstSemesterApprovedCredits(BigDecimal firstSemesterApprovedCredits) {
        this.firstSemesterApprovedCredits = firstSemesterApprovedCredits;
    }

    public Integer getSecondSemesterEnrolmentsCount() {
        return secondSemesterEnrolmentsCount;
    }

    public void setSecondSemesterEnrolmentsCount(Integer secondSemesterEnrolmentsCount) {
        this.secondSemesterEnrolmentsCount = secondSemesterEnrolmentsCount;
    }

    public BigDecimal getSecondSemesterEnroledCredits() {
        return secondSemesterEnroledCredits;
    }

    public void setSecondSemesterEnroledCredits(BigDecimal secondSemesterEnroledCredits) {
        this.secondSemesterEnroledCredits = secondSemesterEnroledCredits;
    }

    public BigDecimal getSecondSemesterApprovedCredits() {
        return secondSemesterApprovedCredits;
    }

    public void setSecondSemesterApprovedCredits(BigDecimal secondSemesterApprovedCredits) {
        this.secondSemesterApprovedCredits = secondSemesterApprovedCredits;
    }

    public RegistrationRegimeType getRegimeType() {
        return regimeType;
    }

    public void setRegimeType(RegistrationRegimeType regimeType) {
        this.regimeType = regimeType;
    }

    public BigDecimal getApprovedCredits() {
        return approvedCredits;
    }

    public void setApprovedCredits(BigDecimal approvedCredits) {
        this.approvedCredits = approvedCredits;
    }

    public Integer getCurricularYear() {
        return curricularYear;
    }

    public void setCurricularYear(Integer curricularYear) {
        this.curricularYear = curricularYear;
    }

    public LocalDate getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(LocalDate enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
    }

    public BigDecimal getEquivalenceCredits() {
        return equivalenceCredits;
    }

    public void setEquivalenceCredits(BigDecimal equivalenceCredits) {
        this.equivalenceCredits = equivalenceCredits;
    }

    public BigDecimal getTotalEnroledCredits() {
        return totalEnroledCredits;
    }

    public void setTotalEnroledCredits(BigDecimal totalEnroledCredits) {
        this.totalEnroledCredits = totalEnroledCredits;
    }

    public BigDecimal getTotalApprovedCredits() {
        return totalApprovedCredits;
    }

    public void setTotalApprovedCredits(BigDecimal totalApprovedCredits) {
        this.totalApprovedCredits = totalApprovedCredits;
    }

    public Integer getTotalEnrolmentsCount() {
        return totalEnrolmentsCount;
    }

    public void setTotalEnrolmentsCount(Integer totalEnrolmentsCount) {
        this.totalEnrolmentsCount = totalEnrolmentsCount;
    }

    public Integer getEnrolmentYearsCount() {
        return enrolmentYearsCount;
    }

    public void setEnrolmentYearsCount(Integer enrolmentYearsCount) {
        this.enrolmentYearsCount = enrolmentYearsCount;
    }

    public boolean isWorkingStudent() {

        Predicate<? super StudentStatute> isValidWorkingStudent =
                x -> getExecutionInterval().isAfterOrEquals(x.getBeginExecutionPeriod())
                        && x.getEndExecutionPeriod().isAfterOrEquals(getExecutionInterval())
                        && x.getType().isWorkingStudentStatute();

        Optional<StudentStatute> workingStatute =
                getRegistration().getStudent().getStudentStatutesSet().stream().filter(isValidWorkingStudent).findAny();
        workingStatute.isPresent();

        return false;

    }

    public Integer getEnrolmentYearsInFullRegimeCount() {
        return enrolmentYearsInFullRegimeCount;
    }

    public void setEnrolmentYearsInFullRegimeCount(Integer enrolmentYearsInFullRegimeCount) {
        this.enrolmentYearsInFullRegimeCount = enrolmentYearsInFullRegimeCount;
    }

    public LocalDate getLastAcademicActDate() {
        return lastAcademicActDate;
    }

    public void setLastAcademicActDate(LocalDate lastAcademicActDate) {
        this.lastAcademicActDate = lastAcademicActDate;
    }

}