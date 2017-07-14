package soapfault.ejb;

public class SimpleSoapException extends Exception {
    
    private String reason;
    
    public SimpleSoapException(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}