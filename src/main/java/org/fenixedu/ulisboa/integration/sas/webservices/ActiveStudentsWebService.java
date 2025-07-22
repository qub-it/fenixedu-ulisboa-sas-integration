package org.fenixedu.ulisboa.integration.sas.webservices;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationDataByExecutionYear;
import org.fenixedu.academic.domain.student.RegistrationServices;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academictreasury.services.TuitionServices;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.integration.sas.domain.EducationLevelTypeMapping;
import org.fenixedu.ulisboa.integration.sas.dto.ActiveStudentBean;
import org.fenixedu.ulisboa.specifications.ULisboaConfiguration;
import org.fenixedu.ulisboa.specifications.service.StudentActive;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;
import com.qubit.solution.fenixedu.integration.cgd.domain.idcards.CgdCard;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.FenixFramework;

@WebService
public class ActiveStudentsWebService extends BennuWebService {

    private static Logger logger = LoggerFactory.getLogger(ActiveStudentsWebService.class);
    private static SoftReference<Collection<ActiveStudentBean>> cache = null;
    private static long timestamp = 0;
    private static final String THREAD_NAME = "ActiveStudenBean cache updater";
    private static final String DEGREE = "DEGREE";
    private static final String MASTER_DEGREE = "MASTER_DEGREE";
    private static final String MASTER_DEGREE_INTEGRATED = "MASTER_DEGREE_INTEGRATED";
    private static final String DOCTORATE_DEGREE = "DOCTORATE_DEGREE";

    private static final Atomic atomic = new Atomic() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Atomic.class;
        }

        @Override
        public TxMode mode() {
            return TxMode.READ;
        }

        @Override
        public boolean flattenNested() {
            return false;
        }

    };

    private static final Comparator COMPARATOR = new Comparator<Registration>() {

        @Override
        public int compare(Registration o1, Registration o2) {
            EducationLevelTypeMapping educationLevelTypeMapping = o1.getDegree().getDegreeType().getEducationLevelTypeMapping();
            EducationLevelTypeMapping educationLevelTypeMapping2 = o2.getDegree().getDegreeType().getEducationLevelTypeMapping();
            return getOrderEducationLevelType(educationLevelTypeMapping).compareTo(
                    getOrderEducationLevelType(educationLevelTypeMapping2));
        }

        private Integer getOrderEducationLevelType(EducationLevelTypeMapping educationLevelTypeMapping) {
            if (educationLevelTypeMapping == null || educationLevelTypeMapping.getEducationLevelType() == null) {
                return 5;
            }

            switch (educationLevelTypeMapping.getEducationLevelType().getCode()) {
            case DEGREE:
                return 1;
            case MASTER_DEGREE:
                return 2;
            case MASTER_DEGREE_INTEGRATED:
                return 3;
            case DOCTORATE_DEGREE:
                return 4;
            default:
                return 5;
            }
        }
    };

    private static class ActiveStudentCalculator implements CallableWithoutException<Object> {

        @Override
        public Object call() {
            cache = new SoftReference<Collection<ActiveStudentBean>>(parallelPopulateActiveStudents(calculateActiveStudents()));
            timestamp = System.currentTimeMillis();
            return null;
        }

    }

    static {
        launchCacheUpdaterThread();
    }

    private static void launchCacheUpdaterThread() {
        logger.info("Launching ActiveStudentBean cache updater!");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                boolean run = ULisboaConfiguration.getConfiguration().getActiveStudentsServoceEnable();
                // Let's wait 90 seconds before we start loading the students cache.
                // We're doing this because this thread is started at application startup,
                // and we want to give all the resources to the startup and only afterwards
                // we want to start this thread. Otherwise we can seriously delay the startup.
                // 
                // 25 August 2015 - Paulo Abrantes
                try {
                    Thread.sleep(90 * 1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                while (run) {
                    logger.info("Updating ActiveStudentsBean cache.");
                    if (cache == null || cache.get() == null || ((System.currentTimeMillis() - timestamp) / 1000) > (3600 * 4)) {
                        try {
                            FenixFramework.getTransactionManager().withTransaction(new ActiveStudentCalculator(), atomic);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    logger.info("Updated ActiveStudentsBean cache.");
                    try {
                        Thread.sleep(300 * 1000);
                    } catch (InterruptedException e) {
                        run = false;
                    }

                }
            }
        });
        thread.setName(THREAD_NAME);
        thread.start();
    }

    // It could happen that the thread was kill by the JVM for some reason but
    // the system is still up. So basically what we'll do is if the cache is not
    // present we'll check if the thread exists and if not we'll relaunch it.
    //
    // 28 August 2015 - Paulo Abrantes
    private void launchThreadIfIsDead() {
        boolean needsLaunch = true;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(THREAD_NAME)) {
                needsLaunch = false;
                break;
            }
        }
        if (needsLaunch) {
            launchCacheUpdaterThread();
        }
    }

    @WebMethod
    public Collection<ActiveStudentBean> getActiveStudents() {
        Collection<ActiveStudentBean> collection = cache != null ? cache.get() : null;
        if (collection == null) {
            launchThreadIfIsDead();
            throw new RuntimeException(
                    "Cache was empty...most probably system is recalculating cache. Please invoke the webservice a few minutes later. If this message keeps showing up please contact the support");
        }
        return collection;

    }

    @WebMethod
    public Collection<ActiveStudentBean> getDailyRegistration() {
        return parallelPopulateActiveStudents(calculateStudentsRegisteredInCurrentDay());
    }

    @WebMethod
    public Collection<ActiveStudentBean> getCurrentDayIssuedCards() {
        return parallelPopulateActiveStudents(getStudentsWithCardsIssuedToday());
    }

    private static List<ActiveStudentBean> parallelPopulateActiveStudents(List<Student> collect) {
        List<StudentDataCollector> collectors = new ArrayList<StudentDataCollector>();
        int size = collect.size();
        int numberOfthreads = ULisboaConfiguration.getConfiguration().getActiveStudentsThreadNumber();
        int split = size / numberOfthreads + ((size % numberOfthreads) > 0 ? 1 : 0);
        for (int i = 0; i < numberOfthreads; i++) {
            int start = i * split;
            int end = Math.min(start + split, size);
            if (start >= end) {
                // we don't need that many threads. let's break the cycle
                break;
            }
            collectors.add(new StudentDataCollector(collect.subList(start, end)));
        }

        List<Thread> threadList = new ArrayList<Thread>();
        for (StudentDataCollector collector : collectors) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Collection<ActiveStudentBean> results =
                                FenixFramework.getTransactionManager().withTransaction(collector, atomic);
                        collector.setBeans(results);
                    } catch (Exception e) {
                        logger.warn("Problems in one of the student cache calculation threads", e);
                        collector.setBeans(Collections.EMPTY_LIST);
                    }
                }
            });
            threadList.add(t);
            t.start();
        }
        List<ActiveStudentBean> beans = new ArrayList<ActiveStudentBean>();
        for (Thread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (StudentDataCollector collector : collectors) {
            beans.addAll(collector.getBeans());
        }
        return beans;
    }

    private static class StudentDataCollector implements CallableWithoutException<Collection<ActiveStudentBean>> {

        private final List<Student> students;
        private Collection<ActiveStudentBean> beans;

        public StudentDataCollector(List<Student> students) {
            this.students = students;
        }

        public void setBeans(Collection<ActiveStudentBean> beans) {
            this.beans = beans;
        }

        public Collection<ActiveStudentBean> getBeans() {
            return beans;
        }

        @Override
        public Collection<ActiveStudentBean> call() {
            return populateActiveStudents(students.stream());
        }

    }

    //Consider moving this logic to a different place
    public static Collection<ActiveStudentBean> populateActiveStudents(Stream<Student> students) {
        List<ActiveStudentBean> collect = students.map(student -> populateActiveStudent(student)).collect(Collectors.toList());
        return collect;
    }

    private static List<Student> calculateActiveStudents() {
        return Bennu.getInstance().getStudentsSet().stream().filter(student -> StudentActive.isActiveStudent(student))
                .collect(Collectors.toList());
    }

    private List<Student> calculateStudentsRegisteredInCurrentDay() {
        return Bennu.getInstance().getDailyEnrolmentsSet().stream().map(enrolment -> enrolment.getStudent()).distinct()
                .collect(Collectors.toList());
    }

    private static ActiveStudentBean populateActiveStudent(Student student) {
        // TODO review which registration to use
        // Return the first Registration found

        try {
            ActiveStudentBean activeStudentBean = new ActiveStudentBean();
            activeStudentBean.setName(student.getName());
            activeStudentBean.setGender(student.getPerson().getGender().toLocalizedString(Locale.getDefault()));
            //information still not available

            Optional<CgdCard> card = student.getPerson().getCgdCardsSet().stream().filter(CgdCard::isValid).findAny();
            if (card.isPresent()) {
                String mifareCode = card.get().getMifareCode();
                activeStudentBean.setMifare(mifareCode);
                activeStudentBean.setIsTemporaryCard(Boolean.toString(card.get().getTemporary()));
                activeStudentBean.setCardIssueDate(card.get().getLastMifareModication().toString());
                activeStudentBean.setCardNumber(card.get().getCardNumber());
            }

            activeStudentBean.setIdentificationNumber(student.getPerson().getDocumentIdNumber());
            activeStudentBean.setFiscalCountryCode(
                    student.getPerson().getFiscalCountry() != null ? student.getPerson().getFiscalCountry().getCode() : "");
            activeStudentBean.setFiscalIdentificationNumber(student.getPerson().getSocialSecurityNumber());
            YearMonthDay dateOfBirthYearMonthDay = student.getPerson().getDateOfBirthYearMonthDay();
            activeStudentBean.setDateOfBirth(dateOfBirthYearMonthDay != null ? dateOfBirthYearMonthDay.toString() : "");

            activeStudentBean.setEmail(student.getPerson().getDefaultEmailAddressValue());

            Country country = student.getPerson().getCountry();
            activeStudentBean.setOriginCountry(country != null ? country.getLocalizedName().getContent(Locale.getDefault()) : "");
            activeStudentBean.setOriginCountryCode(country != null ? country.getCode() : "");

            List<Registration> activeRegistrations = student.getActiveRegistrations();
            if (!activeRegistrations.isEmpty()) {
                if (activeRegistrations.size() > 1) {
                    // This ordering is needed because SAS need to get the most relevant course. 
                    // Since they don't support receiving several courses associated we have to hack the order
                    // away to send the correct one.
                    //
                    // For more information about this check email "RE: Faculdade de Letras - alunos com cursos Formação Livre" 
                    // from 29 September 2016
                    //
                    // 30 September 2016 - Paulo Abrantes
                    Collections.sort(activeRegistrations, COMPARATOR);
                }

                Registration registration = activeRegistrations.iterator().next();
                activeStudentBean.setStudentCode(Integer.toString(registration.getNumber()));
                EducationLevelTypeMapping educationLevelTypeMapping = registration.getDegreeType().getEducationLevelTypeMapping();
                if (educationLevelTypeMapping == null) {
                    //Consider all courses without school level type mapping as the free course 
                    activeStudentBean.setDegreeCode(ActiveDegreesWebService.FREE_COURSES_CODE);
                } else {
                    activeStudentBean.setDegreeCode(registration.getDegree().getCode());
                    activeStudentBean.setOficialDegreeCode(registration.getDegree().getMinistryCode());
                }
                ArrayList<ExecutionYear> sortedExecutionYears = getSortedExecutionYears(registration);
                if (sortedExecutionYears.size() > 0) {
                    ExecutionYear currentExecutionYear = sortedExecutionYears.get(sortedExecutionYears.size() - 1);
                    activeStudentBean.setCurrentExecutionYear(currentExecutionYear.getName());
                    activeStudentBean.setEnroledECTTotal(Double.toString(registration.getEnrolmentsEcts(currentExecutionYear)));
                    LocalDate enrolmentDate = getEnrolmentDate(registration, currentExecutionYear);
                    activeStudentBean.setDateOfRegistration(enrolmentDate != null ? enrolmentDate.toString() : "");
                    activeStudentBean.setRegime(registration.getRegimeType(currentExecutionYear).toString());
                    boolean toPayTuition = TuitionServices.isToPayRegistrationTuition(registration, currentExecutionYear);
                    activeStudentBean.setIsPayingSchool(toPayTuition);
                    activeStudentBean.setCurricularYear(Integer.toString(
                            RegistrationServices.getCurricularYear(registration, currentExecutionYear).getResult()));
                }

                if (sortedExecutionYears.size() > 1) {
                    ExecutionYear previousExecutionYear = sortedExecutionYears.get(sortedExecutionYears.size() - 2);
                    activeStudentBean.setPreviousExecutionYear(previousExecutionYear.getName());
                    activeStudentBean.setEnroledECTTotalInPreviousYear(
                            Double.toString(registration.getEnrolmentsEcts(previousExecutionYear)));
                    activeStudentBean.setApprovedECTTotalInPreviousYear(
                            getApprovedEcts(registration, previousExecutionYear).toString());
                }

            } else {
                return null;
            }

            return activeStudentBean;
        } catch (Throwable t) {
            logger.error("Problems calculating active students cache for student: " + student.getNumber());
            return null;
        }
    }

    private static LocalDate getEnrolmentDate(Registration firstRegistration, ExecutionYear currentExecutionYear) {
        RegistrationDataByExecutionYear registrationDataByExecutionYear =
                firstRegistration.getRegistrationDataByExecutionYearSet().stream()
                        .filter(rdby -> rdby.getExecutionYear().equals(currentExecutionYear)).findFirst().orElse(null);
        return registrationDataByExecutionYear != null ? registrationDataByExecutionYear.getEnrolmentDate() : null;
    }

    private static Double getApprovedEcts(Registration firstRegistration, ExecutionYear previousExecutionYear) {
        return firstRegistration.getEnrolments(previousExecutionYear).stream().filter(e -> e.isApproved())
                .map(e -> e.getEctsCredits()).reduce((n1, n2) -> n1 + n2).orElse(0.0);
    }

    private static ArrayList<ExecutionYear> getSortedExecutionYears(Registration firstRegistration) {
        ArrayList<ExecutionYear> arrayList = new ArrayList<>();
        arrayList.addAll(firstRegistration.getEnrolmentsExecutionYears());
        arrayList.sort((e1, e2) -> e1.compareTo(e2));
        return arrayList;
    }

    private List<Student> getStudentsWithCardsIssuedToday() {
        // We are comparing the card modification date instead of the card issued date
        // This is required since the card issued date may be some day before the card insertion in the system
        return Bennu.getInstance().getCgdCardsSet().stream()
                .filter(card -> isToday(card.getLastMifareModication()) && card.getPerson().getStudent() != null)
                .map(card -> card.getPerson().getStudent()).collect(Collectors.toList());
    }

    private boolean isToday(LocalDate b) {
        LocalDate now = LocalDate.now();
        return now.year().get() == b.year().get() && now.monthOfYear().get() == b.monthOfYear().get() && now.dayOfMonth()
                .get() == b.dayOfMonth().get();
    }

}
