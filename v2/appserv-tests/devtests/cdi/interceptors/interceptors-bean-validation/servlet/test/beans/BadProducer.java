package test.beans;


import javax.enterprise.inject.Produces;


/**
 * @author <a href="mailto:phil.zampino@oracle.com">Phil Zampino</a>
 */
public class BadProducer {

    @Produces @Preferred
    public TestProduct getTestProduct() {
        System.out.println(getClass().getName() + "#getTestProducer() invoked...returning null");
        return null; // This producer is bad because it always returns null
    }

    @Produces @Preferred
    public String getPreferredString() {
        return null;
    }

}
