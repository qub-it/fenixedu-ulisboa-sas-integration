/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: xpto@qub-it.com
 *
 * 
 * This file is part of FenixEdu Sas.
 *
 * FenixEdu Sas is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Sas is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Sas.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.ulisboa.integration.sas.ui.spring.controller.manageScholarshipCandidacies;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacy;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipData;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipDataChangeLog;
import org.fenixedu.ulisboa.integration.sas.service.sicabe.SicabeExternalService;
import org.fenixedu.ulisboa.integration.sas.ui.spring.controller.SasBaseController;
import org.fenixedu.ulisboa.integration.sas.ui.spring.controller.SasController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SpringFunctionality(app = SasController.class, title = "label.title.manageScholarships", accessGroup = "#academicAdmOffice")
@RequestMapping(ScholarshipCandidaciesController.CONTROLLER_URL)
public class ScholarshipCandidaciesController extends SasBaseController {

    private final int TEMP_INSTITUTION_CODE = 1501;

    public static final String CONTROLLER_URL = "/integration/sas/manageScholarshipCandidacies";

    public static final String JSP_PATH = CONTROLLER_URL.substring(1);

    @RequestMapping
    public String home(Model model) {
        final ExecutionYear executionYear = ExecutionYear.readCurrentExecutionYear();
        return "forward:" + CONTROLLER_URL + "/" + executionYear.getExternalId();
    }

    @RequestMapping(value = "/{executionYearId}", method = RequestMethod.GET)
    public String search(Model model, @PathVariable(value = "executionYearId") ExecutionYear executionYearParam) {

        final List<SasScholarshipCandidacy> scholarshipCandidacies =
                SasScholarshipCandidacy.findAll().stream().filter(c -> c.getExecutionYear() == executionYearParam)
                        .sorted((x, y) -> -(x.getSubmissionDate().compareTo(y.getSubmissionDate()))).collect(Collectors.toList());

        model.addAttribute("scholarshipCandidacies", scholarshipCandidacies);
        model.addAttribute("executionYears", ExecutionYear.readNotClosedExecutionYears().stream()
                .sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE.reversed()).collect(Collectors.toList()));
        model.addAttribute("executionYear", executionYearParam);

        return jspPath("search");
    }

    private static final String _CHANGE_EXECUTION_YEAR_URI = "/changeExecutionYear";
    public static final String CHANGE_EXECUTION_YEAR_URL = CONTROLLER_URL + _CHANGE_EXECUTION_YEAR_URI;

    @RequestMapping(value = "/changeExecutionYear", method = RequestMethod.POST)
    public String changeExecutionYear(Model model, @RequestParam(value = "executionYearId") ExecutionYear executionYearParam) {
        return search(model, executionYearParam);
    }

    private String jspPath(String page) {
        return JSP_PATH + "/" + page;
    }

    private static final String _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_CANDIDACY_ACTION_URI = "/search/viewSasScholarshipCandidacy/";
    public static final String SEARCH_TO_VIEW_SAS_SCHOLARSHIP_CANDIDACY_ACTION_URL =
            CONTROLLER_URL + _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_CANDIDACY_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_CANDIDACY_ACTION_URI + "{oid}", method = RequestMethod.GET)
    public String processSearchToViewSasScholarshipCandidacyAction(
            @PathVariable("oid") SasScholarshipCandidacy sasScholarshipCandidacy, Model model,
            RedirectAttributes redirectAttributes) {

        return redirect(READ_SAS_SCHOLARSHIP_CANDIDACY_URL + sasScholarshipCandidacy.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_SAS_SCHOLARSHIP_CANDIDACY_URI = "/readSasScholarshipCandidacy/";
    public static final String READ_SAS_SCHOLARSHIP_CANDIDACY_URL = CONTROLLER_URL + _READ_SAS_SCHOLARSHIP_CANDIDACY_URI;

    @RequestMapping(value = _READ_SAS_SCHOLARSHIP_CANDIDACY_URI + "{oid}", method = RequestMethod.GET)
    public String readSasScholarshipCandidacy(@PathVariable("oid") SasScholarshipCandidacy sasScholarshipCandidacy, Model model) {
        model.addAttribute("sasScholarshipCandidacy", sasScholarshipCandidacy);

        return jspPath("readSasScholarshipCandidacy");
    }

    private static final String _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_DATA_ACTION_URI = "/search/viewSasScholarshipData/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_DATA_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_SAS_SCHOLARSHIP_DATA_ACTION_URI + "{oid}", method = RequestMethod.GET)
    public String processSearchToViewSasScholarshipDataAction(@PathVariable("oid") SasScholarshipData sasScholarshipData,
            Model model, RedirectAttributes redirectAttributes) {

        return redirect(READ_SAS_SCHOLARSHIP_DATA_URL + sasScholarshipData.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_SAS_SCHOLARSHIP_DATA_URI = "/readSasScholarshipData/";
    public static final String READ_SAS_SCHOLARSHIP_DATA_URL = CONTROLLER_URL + _READ_SAS_SCHOLARSHIP_DATA_URI;

    @RequestMapping(value = _READ_SAS_SCHOLARSHIP_DATA_URI + "{oid}", method = RequestMethod.GET)
    public String readSasScholarshipCandidacy(@PathVariable("oid") SasScholarshipData sasScholarshipData, Model model) {
        model.addAttribute("sasScholarshipData", sasScholarshipData);

        return jspPath("readSasScholarshipData");
    }

    private static final String _SYNC_ENTRIES_URI = "/sync";
    public static final String SYNC_ENTRIES_URL = CONTROLLER_URL + _SYNC_ENTRIES_URI;

    @RequestMapping(value = _SYNC_ENTRIES_URI, method = RequestMethod.POST)
    public String syncSearchEntries(
            @RequestParam("sasScholarshipCandidacyEntries") List<SasScholarshipCandidacy> sasScholarshipCandidacies,
            @PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.fillSasScholarshipCandidacies(sasScholarshipCandidacies, executionYear, TEMP_INSTITUTION_CODE);

        model.addAttribute("infoMessages", sasScholarshipCandidacies.size() + " candidaturas sincronizadas com sucesso.");

        return search(model, executionYear);
    }

    private static final String _SYNC_ALL_ENTRIES_URI = "/syncAll";
    public static final String SYNC_ALL_ENTRIES_URL = CONTROLLER_URL + _SYNC_ALL_ENTRIES_URI;

    @RequestMapping(value = _SYNC_ALL_ENTRIES_URI + "/{executionYearId}", method = RequestMethod.GET)
    public String syncAllEntries(@PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.fillAllSasScholarshipCandidacies(executionYear, TEMP_INSTITUTION_CODE);

        model.addAttribute("infoMessages", "Todas as candidaturas foram sincronizadas com sucesso.");

        return search(model, executionYear);
    }

    private static final String _PROCESS_ENTRIES_URI = "/process";
    public static final String PROCESS_ENTRIES_URL = CONTROLLER_URL + _PROCESS_ENTRIES_URI;

    @RequestMapping(value = _PROCESS_ENTRIES_URI + "/{sasScholarshipCandidacyId}", method = RequestMethod.GET)
    public String processSearchEntries(@PathVariable("sasScholarshipCandidacyId") SasScholarshipCandidacy sasScholarshipCandidacy,
            Model model, RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.processSasScholarshipCandidacies(Collections.singletonList(sasScholarshipCandidacy));

        model.addAttribute("infoMessages", "TODO Candidatura processada com sucesso.");

        return jspPath("readSasScholarshipData");
    }

    private static final String _PROCESS_ALL_ENTRIES_URI = "/processAll";
    public static final String PROCESS_ALL_ENTRIES_URL = CONTROLLER_URL + _PROCESS_ALL_ENTRIES_URI;

    @RequestMapping(value = _PROCESS_ALL_ENTRIES_URI + "/{executionYearId}", method = RequestMethod.GET)
    public String processAll(@PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        //process all entries
        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.processAllSasScholarshipCandidacies();

        model.addAttribute("infoMessages", "TODO: Todas as candidaturas foram processadas com sucesso.");
        return search(model, executionYear);
    }

    private static final String _SEND_ENTRIES_URI = "/send";
    public static final String SEND_ENTRIES_URL = CONTROLLER_URL + _SEND_ENTRIES_URI;

    @RequestMapping(value = _SEND_ENTRIES_URI + "/{sasScholarshipCandidacyId}", method = RequestMethod.GET)
    public String sendSearchEntries(
            @PathVariable(value = "sasScholarshipCandidacyId") SasScholarshipCandidacy sasScholarshipCandidacy, Model model,
            RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.sendSasScholarshipsData(Collections.singletonList(sasScholarshipCandidacy));

        model.addAttribute("infoMessages", "TODO: candidatura enviada com sucesso.");

        return jspPath("readSasScholarshipData");
    }

    private static final String _SEND_ALL_ENTRIES_URI = "/sendAll";
    public static final String SEND_ALL_ENTRIES_URL = CONTROLLER_URL + _SEND_ALL_ENTRIES_URI;

    @RequestMapping(value = _SEND_ALL_ENTRIES_URI + "/{executionYearId}", method = RequestMethod.GET)
    public String sendSearchEntries(@PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.sendAllSasScholarshipsData();

        model.addAttribute("infoMessages", "TODO: Todas as candidaturas foram enviadas com sucesso.");
        return search(model, executionYear);

    }

    private static final String _REMOVE_ENTRIES_URI = "/remove";
    public static final String REMOVE_ENTRIES_URL = CONTROLLER_URL + _REMOVE_ENTRIES_URI;

    @RequestMapping(value = _REMOVE_ENTRIES_URI + "/{sasScholarshipCandidacyId}", method = RequestMethod.GET)
    public String remove(@PathVariable(value = "sasScholarshipCandidacyId") SasScholarshipCandidacy sasScholarshipCandidacy,
            Model model, RedirectAttributes redirectAttributes) {

        String studentName = sasScholarshipCandidacy.getCandidacyName();
        final ExecutionYear executionYear = sasScholarshipCandidacy.getExecutionYear();

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.removeSasScholarshipsCandidacies(Collections.singletonList(sasScholarshipCandidacy));

        model.addAttribute("infoMessages", "Candidatura do aluno " + studentName + " apagada com sucesso.");

        return search(model, executionYear);
    }

    private static final String _REMOVE_ALL_ENTRIES_URI = "/removeAll";
    public static final String REMOVE_ALL_ENTRIES_URL = CONTROLLER_URL + _REMOVE_ALL_ENTRIES_URI;

    @RequestMapping(value = _REMOVE_ALL_ENTRIES_URI + "/{executionYearId}", method = RequestMethod.GET)
    public String removeAll(@PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        SicabeExternalService sicabe = new SicabeExternalService();
        sicabe.removeAllSasScholarshipsCandidacies();

        model.addAttribute("infoMessages", "TODO: Todas as candidaturas foram removidas com sucesso.");
        return search(model, executionYear);

    }

    private static final String _VIEW_LOGS_URI = "/logs";
    public static final String VIEW_LOGS_ENTRIES_URL = CONTROLLER_URL + _VIEW_LOGS_URI;

    @RequestMapping(value = _VIEW_LOGS_URI + "/{executionYearId}", method = RequestMethod.GET)
    public String viewLogs(@PathVariable(value = "executionYearId") ExecutionYear executionYear, Model model,
            RedirectAttributes redirectAttributes) {

        List<SasScholarshipDataChangeLog> logs = Bennu.getInstance().getSasScholarshipDataChangeLogsSet().stream()
                .sorted((x, y) -> -(x.getDate().compareTo(y.getDate()))).collect(Collectors.toList());

        model.addAttribute("sasScholarshipDataChangeLogs", logs);
        return jspPath("viewLogs");
    }

    private static final String _VIEW_LOG_URI = "/log";
    public static final String VIEW_LOG_ENTRIES_URL = CONTROLLER_URL + _VIEW_LOG_URI;

    @RequestMapping(value = _VIEW_LOG_URI + "/{sasScholarshipCandidacyId}", method = RequestMethod.GET)
    public String viewLogs(@PathVariable("sasScholarshipCandidacyId") SasScholarshipCandidacy sasScholarshipCandidacy,
            Model model, RedirectAttributes redirectAttributes) {

        List<SasScholarshipDataChangeLog> logs = Bennu.getInstance().getSasScholarshipDataChangeLogsSet().stream()
                .filter(l -> l.getSasScholarshipCandidacy() == sasScholarshipCandidacy)
                .sorted((x, y) -> -(x.getDate().compareTo(y.getDate()))).collect(Collectors.toList());

        model.addAttribute("sasScholarshipDataChangeLogs", logs);
        model.addAttribute("sasScholarshipCandidacy", sasScholarshipCandidacy);
        return jspPath("viewLogs");

    }

}
