import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.enterprise.inject.spi.Bean;

public class TestBeanInWebInfLib {
    @Inject
    BeanManager bm;

    @Inject
    TestBean tb;

    public String testInjection() {
        if (bm == null)
            return "Bean Manager not injected into the TestBean in WEB-INF/lib";
        System.out.println("BeanManager in WEB-INF/lib bean is " + bm);
//        if (tb == null) return
//            "Injection of WAR's TestBean into the TestBean in WEB-INF/lib failed";

        // Set warBeans = bm.getBeans(TestBean.class,new
        // AnnotationLiteral<Any>() {});
        // if (warBeans.size() != 1) return "TestBean in WAR is not available "
        // +
        // "via the WEB-INF/lib Bean's BeanManager";

        Set<Bean<?>> webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class,
                new AnnotationLiteral<Any>() {
                });
        if (webinfLibBeans.size() != 1)
            return "TestBean in WEB-INF/lib is not available via the WEB-INF/lib "
                    + "Bean's BeanManager";
        System.out.println("BeanManager.getBeans(TestBeanInWebInfLib, Any):" + webinfLibBeans);
        for (Bean b: webinfLibBeans) {
            debug(b);
        }
        

        
        
        Iterable<Bean<?>> accessibleBeans = ((org.jboss.weld.manager.BeanManagerImpl) bm)
                .getAccessibleBeans();
        System.out.println("BeanManagerImpl.getAccessibleBeans:" + accessibleBeans);
        for (Bean b : accessibleBeans) {
            debug(b);
        }

        Iterable<Bean<?>> beans = ((org.jboss.weld.manager.BeanManagerImpl) bm).getBeans();
        System.out.println("BeanManagerImpl.getBeans:" + beans);
        for (Bean b : beans) {
            debug(b);
        }
        
        // success
        return "";
    }

    private void debug(Bean b) {
        String name = b.getBeanClass().getName();
        if (name.indexOf("Test") != -1) {
            System.out.println(name);
        }

    }
}
