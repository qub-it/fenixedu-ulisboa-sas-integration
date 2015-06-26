package org.fenixedu.ulisboa.integration.sas.webservices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.integration.sas.dto.ActiveStudentBean;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;

@WebService
public class ActiveStudentsWebService extends BennuWebService {

    @WebMethod
    public Collection<ActiveStudentBean> getActiveStudents() {
        return populateActiveStudents(calculateActiveStudents());
    }

    @WebMethod
    public Collection<ActiveStudentBean> getDailyRegistration() {
        return populateActiveStudents(calculateStudentsRegisteredInCurrentDay());
    }

    //Consider moving this logic to a different place
    private Collection<ActiveStudentBean> populateActiveStudents(Stream<Student> students) {
        return students.map(student -> populateActiveStudent(student)).collect(Collectors.toList());
    }

    private Stream<Student> calculateActiveStudents() {
        return Bennu.getInstance().getStudentsSet().stream().filter(student -> isActive(student));
    }

    private boolean isActive(Student student) {
        //TODO review the requirements since the concept of active student for SAS is not the same as in fenix
//        return !student.getActiveRegistrations().isEmpty();
        return true;
    }

    private Stream<Student> calculateStudentsRegisteredInCurrentDay() {
        return Bennu.getInstance().getDailyEnrolmentsSet().stream().map(enrolment -> enrolment.getStudent()).distinct();
    }

    private ActiveStudentBean populateActiveStudent(Student student) {
        ExecutionSemester actualExecutionSemester = ExecutionSemester.readActualExecutionSemester();
        // TODO review which registration to use
        // Return the first Registration found

        ActiveStudentBean activeStudentBean = new ActiveStudentBean();
//        String name;
        activeStudentBean.setName(student.getName());
        activeStudentBean.setGender(student.getPerson().getGender().toLocalizedString(Locale.getDefault()));
        //information still not available
        //String mifare;
        activeStudentBean.setMifare("123456789");

        activeStudentBean.setIdentificationNumber(student.getPerson().getDocumentIdNumber());
        activeStudentBean.setFiscalIdentificationNumber(student.getPerson().getSocialSecurityNumber());
        YearMonthDay dateOfBirthYearMonthDay = student.getPerson().getDateOfBirthYearMonthDay();
        activeStudentBean.setDateOfBirth(dateOfBirthYearMonthDay != null ? dateOfBirthYearMonthDay.toString() : "");
        activeStudentBean.setStudentCode(student.getNumber().toString());
        activeStudentBean.setOriginCountry(student.getPerson().getCountry().getLocalizedName().getContent(Locale.getDefault()));

        /*if (!student.getActiveRegistrations().isEmpty()) {
            Registration registration = student.getActiveRegistrations().iterator().next();
            SchoolLevelType schoolLevelTypeFor = SchoolLevelTypeMapping.getSchoolLevelTypeFor(registration.getDegreeType());
            if (schoolLevelTypeFor == null) {
                //Consider all courses without school level as the free course 
                activeStudentBean.setDegreeCode(ActiveDegreesWebService.FREE_COURSES_CODE);
            } else {
                activeStudentBean.setDegreeCode(registration.getDegree().getCode());
                activeStudentBean.setOficialDegreeCode(registration.getDegree().getMinistryCode());

                ArrayList<ExecutionYear> sortedExecutionYears = getSortedExecutionYears(registration);
                ExecutionYear currentExecutionYear = sortedExecutionYears.get(sortedExecutionYears.size() - 1);
                activeStudentBean.setCurrentExecutionYear(currentExecutionYear.toString());
                activeStudentBean.setEnroledECTTotal(Double.toString(registration.getEnrolmentsEcts(currentExecutionYear)));

                if (sortedExecutionYears.size() > 1) {
                    ExecutionYear previousExecutionYear = sortedExecutionYears.get(sortedExecutionYears.size() - 1);
                    activeStudentBean.setCurrentExecutionYear(previousExecutionYear.toString());
                    activeStudentBean.setEnroledECTTotal(Double.toString(registration.getEnrolmentsEcts(previousExecutionYear)));
                    activeStudentBean.setApprovedECTTotalInPreviousYear(getApprovedEcts(registration, previousExecutionYear)
                            .toString());
                }

                activeStudentBean.setDateOfRegistration(getEnrolmentDate(registration, currentExecutionYear).toString());
                activeStudentBean.setCurricularYear(Integer.toString(registration.getCurricularYear()));
                activeStudentBean.setRegime(registration.getRegimeType(currentExecutionYear).toString());
            }
        } else {*/
        Degree fakeDegree =
                Bennu.getInstance().getDegreesSet().stream().filter(x -> x.getCode() != null && x.getMinistryCode() != null)
                        .findAny().get();
        activeStudentBean.setDegreeCode(fakeDegree.getCode());
        activeStudentBean.setOficialDegreeCode(fakeDegree.getMinistryCode());
        ExecutionYear currentExecutionYear = ExecutionYear.readCurrentExecutionYear();
        activeStudentBean.setCurrentExecutionYear(currentExecutionYear.getName());
        activeStudentBean.setEnroledECTTotal("10");
        activeStudentBean.setPreviousExecutionYear(currentExecutionYear.getPreviousExecutionYear().getName());
        activeStudentBean.setEnroledECTTotalInPreviousYear("10");
        activeStudentBean.setApprovedECTTotalInPreviousYear("10");
        activeStudentBean.setDateOfRegistration("10");
        activeStudentBean.setCurricularYear("2");
        activeStudentBean.setRegime(RegistrationRegimeType.FULL_TIME.toString());
//        }

        //information will only be available during implementation of academic-treasury
        activeStudentBean.setIsPayingSchool(true);

        return activeStudentBean;
    }

    private LocalDate getEnrolmentDate(Registration firstRegistration, ExecutionYear currentExecutionYear) {
        return firstRegistration.getRegistrationDataByExecutionYearSet().stream()
                .filter(rdby -> rdby.getExecutionYear().equals(currentExecutionYear)).findFirst().get().getEnrolmentDate();
    }

    private Double getApprovedEcts(Registration firstRegistration, ExecutionYear previousExecutionYear) {
        return firstRegistration.getEnrolments(previousExecutionYear).stream().filter(e -> e.isApproved())
                .map(e -> e.getEctsCredits()).reduce((n1, n2) -> n1 + n2).get();
    }

    private ArrayList<ExecutionYear> getSortedExecutionYears(Registration firstRegistration) {
        ArrayList<ExecutionYear> arrayList = new ArrayList<>();
        arrayList.addAll(firstRegistration.getEnrolmentsExecutionYears());
        arrayList.sort((e1, e2) -> e1.compareTo(e2));
        return arrayList;
    }
}
