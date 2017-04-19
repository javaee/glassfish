package test.beans;
import javax.enterprise.inject.Produces;


public class TestLoggerProducer {
    @Produces
    public org.jboss.logging.Logger getLogger(){
        org.jboss.logging.Logger l = org.jboss.logging.Logger.getLogger(TestLoggerProducer.class);
        System.out.println("getLogger:: " + l);
        return l;
    }

}
