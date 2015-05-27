package org.fenixedu.ulisboa.integration.sas.service;

import java.util.function.Consumer;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;

//Operations to allow "caching"/"indexing" which enrolments were performed in the current day
public class DailyEnrolmentsIndexing {

    public static void bindToSignals() {
        Signal.register(Enrolment.SIGNAL_CREATED, associateWithBennu());
        FenixFramework.getDomainModel().registerDeletionListener(Enrolment.class, new DeletionListener<Enrolment>() {

            @Override
            public void deleting(Enrolment arg0) {
                arg0.setBennuForWhichIsDaily(null);
            }
        });
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
