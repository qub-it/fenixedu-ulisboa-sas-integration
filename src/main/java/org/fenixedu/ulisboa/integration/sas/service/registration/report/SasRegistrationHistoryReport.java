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

public class SasRegistrationHistoryReport {

    private ExecutionInterval executionInterval;

    private Registration registration;

    private StudentCurricularPlan studentCurricularPlan;


    private BigDecimal totalEnroledCredits;

    private BigDecimal totalApprovedCredits;

    private RegistrationRegimeType regimeType;

    private BigDecimal equivalenceCredits;

    private BigDecimal approvedCredits;

    private Integer curricularYear;

    private LocalDate enrolmentDate;

    private Integer enrolmentYearsCount;

    private Integer enrolmentYearsInFullRegimeCount;

    private LocalDate lastAcademicActDate;

    public SasRegistrationHistoryReport(Registration registration, ExecutionInterval executionInterval) {
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

    
    public Integer getEnrolmentYearsCount() {
        return enrolmentYearsCount;
    }

    public void setEnrolmentYearsCount(Integer enrolmentYearsCount) {
        this.enrolmentYearsCount = enrolmentYearsCount;
    }

    public boolean isWorkingStudent() {

        Predicate<? super StudentStatute> isValidWorkingStudent =
                x -> getExecutionInterval().isAfterOrEquals(x.getBeginExecutionPeriod().getExecutionYear())
                        && x.getEndExecutionPeriod().getExecutionYear().isAfterOrEquals(getExecutionInterval())
                        && x.getType().isWorkingStudentStatute();

        Optional<StudentStatute> workingStatute =
                getRegistration().getStudent().getStudentStatutesSet().stream().filter(isValidWorkingStudent).findAny();

        return workingStatute.isPresent();
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