package test.beans;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;


/**
 * @author <a href="mailto:phil.zampino@oracle.com">Phil Zampino</a>
 */
@CDITest
public class AnotherTestBean {

    private TestProduct tp;


    @Inject
    public AnotherTestBean(@Preferred TestProduct testProduct) {
        this.tp = testProduct;
    }


    @PostConstruct
    public void log() {
        System.out.println("TestProduct injected: " + (tp != null));
    }


}
