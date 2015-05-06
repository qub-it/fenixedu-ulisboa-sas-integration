package org.fenixedu.ulisboa.integration.sas.service;

import java.util.function.Consumer;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;

//Operations to allow "caching"/"indexing" which enrolments were performed in the current day
public class DailyEnrolmentsIndexing {

    public static void bindToSignals() {
        Signal.register(Enrolment.SIGNAL_CREATED, associateWithBennu());
        // We need a deleted signal which was not created yet
        //Signal.register(Enrolment.SIGNAL_DELETED, associateWithBennu());
    }

    private static Consumer<DomainObjectEvent<Enrolment>> associateWithBennu() {
        return enrolmentEvent -> enrolmentEvent.getInstance().setBennuForWhichIsDaily(Bennu.getInstance());
    }

    private static Consumer<Enrolment> dessociateWithBennu() {
        return enrolment -> enrolment.setBennuForWhichIsDaily(null);
    }

    public static void performDailyClean() {
        Bennu.getInstance().getDailyEnrolmentsSet().forEach(dessociateWithBennu());
    }
}
