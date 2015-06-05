package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;

@BennuSpringModule(basePackages = "org.fenixedu.ulisboa.integration.sas", bundles = SasSpringConfiguration.BUNDLE)
public class SasSpringConfiguration {
    public static final String BUNDLE = "SasResources";
}
