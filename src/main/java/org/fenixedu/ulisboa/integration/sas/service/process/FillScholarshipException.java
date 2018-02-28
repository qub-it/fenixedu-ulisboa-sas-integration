package org.fenixedu.ulisboa.integration.sas.service.process;

public class FillScholarshipException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public FillScholarshipException(String message) {
        super(message);
    }
    
    public FillScholarshipException() {
        super();
    }

}