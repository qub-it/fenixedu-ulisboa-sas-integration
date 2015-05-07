package org.fenixedu.ulisboa.integration.sas.webservices;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.SchoolLevelType;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.integration.sas.dto.ActiveDegreeBean;
import org.fenixedu.ulisboa.integration.sas.service.process.SchoolLevelTypeMapping;

import com.qubit.solution.fenixedu.bennu.webservices.services.server.BennuWebService;

@WebService
public class ActiveDegreesWebService extends BennuWebService {
    @WebMethod
    public Collection<ActiveDegreeBean> getActiveDegrees() {
        return populateActiveDegrees();
    }

    //Consider moving this logic to a different place
    private Collection<ActiveDegreeBean> populateActiveDegrees() {
        return Bennu.getInstance().getDegreesSet().stream().map(d -> populateActiveDegree(d)).collect(Collectors.toList());
    }

    private ActiveDegreeBean populateActiveDegree(Degree degree) {
        ActiveDegreeBean activeDegreeBean = new ActiveDegreeBean();

        ExecutionYear currentExecutionYear = ExecutionYear.readCurrentExecutionYear();

        activeDegreeBean.setDegreeCode(degree.getCode());
        activeDegreeBean.setDesignation(normalizeString(degree.getNameFor(currentExecutionYear).getContent(Locale.getDefault())));

        SchoolLevelType schoolLevelTypeFor = SchoolLevelTypeMapping.getSchoolLevelTypeFor(degree.getDegreeType());
        activeDegreeBean.setSchoolLevel(schoolLevelTypeFor != null ? schoolLevelTypeFor.getLocalizedName() : "");
        //TODO analyse how to represent a degree with multiple cycles        
        activeDegreeBean.setCycle(getDegreeCyclesString(degree));

        activeDegreeBean.setDuration(Integer.toString(getDegreeDuration(degree, currentExecutionYear)));

        activeDegreeBean.setOficialCode(degree.getMinistryCode());
        return activeDegreeBean;
    }

    // The degree duration must be calculted from a degree curricular plan, not from a degree
    // In this case we are assuming that diferent curricular plans for the same degree will have the same duration
    // This is usually valid since changing the duration of a degree may imply the creation of a new degree (with a different degree code)
    private int getDegreeDuration(Degree degree, ExecutionYear currentExecutionYear) {
        List<DegreeCurricularPlan> degreeCurricularPlansForYear = degree.getDegreeCurricularPlansForYear(currentExecutionYear);
        if (degreeCurricularPlansForYear.isEmpty()) {
            System.out.println("Degree " + degree.getName()
                    + " has no degree curricular plans for the current execution year. Unable to calculate duration");
            return 0;
        }
        return degreeCurricularPlansForYear.iterator().next().getDurationInYears();
    }

    private String[] getDegreeCyclesString(Degree degree) {
        Collection<CycleType> cycleTypes = degree.getDegreeType().getCycleTypes();
        String[] values = new String[cycleTypes.size()];

        int i = 0;
        for (CycleType ct : cycleTypes) {
            values[i++] = ct.getWeight().toString();
        }

        return values;
    }

    //Some schools may have degrees with fully capitalized names. Normalize it 
    private String normalizeString(String string) {
        if (!StringUtils.isEmpty(string)) {
            String[] split = string.split(" ");
            String output = "";
            for (int i = 0; i < split.length; i++) {
                if (i != 0) {
                    output += " ";
                }
                String part = split[i];
                output += part.substring(0, 1).toUpperCase();

                if (part.length() > 1) {
                    output += part.substring(1, part.length()).toLowerCase();
                }
            }
            return output;
        }
        return "";
    }
}
