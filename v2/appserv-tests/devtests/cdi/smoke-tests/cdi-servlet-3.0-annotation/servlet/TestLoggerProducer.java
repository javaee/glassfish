import javax.enterprise.inject.Produces;
import org.jboss.logging.Logger;


public class TestLoggerProducer {
    @Produces
    public org.jboss.logging.Logger getLogger(){
        Logger l = Logger.getLogger(TestLoggerProducer.class);
        System.out.println("getLogger:: " + l);
        return l;
    }

}
