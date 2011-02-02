import javax.enterprise.inject.Produces;


public class TestLoggerProducer {
    @Produces
    public org.slf4j.Logger getLogger(){
        org.slf4j.Logger l = org.slf4j.LoggerFactory.getLogger(TestLoggerProducer.class);
        System.out.println("getLogger:: " + l);
        return l;
    }

}
