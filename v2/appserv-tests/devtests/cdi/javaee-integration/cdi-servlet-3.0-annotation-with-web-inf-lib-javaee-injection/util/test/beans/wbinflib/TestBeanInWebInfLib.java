package test.beans.wbinflib;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class TestBeanInWebInfLib {
    @Inject
    BeanManager bm;
    
//    @Inject //@TestDatabase 
//    EntityManager emf_at_inj;

    @PersistenceContext(unitName="pu1")  
    EntityManager emf_at_pu;

    public String testInjection() {
        if (bm == null)
            return "Bean Manager not injected into the TestBean in WEB-INF/lib";
        System.out.println("BeanManager injected in WEB-INF/lib bean is " + bm);

        System.out.println("EMF injected in WEB-INF/lib bean is " + emf_at_pu);
        if (emf_at_pu == null)
            return "EMF injected via @PersistenceContext is not injected into " +
            		"the TestBean packaged in WEB-INF/lib";
        
        Set<Bean<?>> webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class, new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1)
            return "TestBean in WEB-INF/lib is not available via the WEB-INF/lib "
                    + "Bean's BeanManager";
        System.out.println("BeanManager.getBeans(TestBeanInWebInfLib, Any):" + webinfLibBeans);
        for (Bean b: webinfLibBeans) {
            debug(b);
        }

        // Get the proxy delegate
        org.jboss.weld.manager.BeanManagerImpl delegate =
                                     ((org.jboss.weld.bean.builtin.BeanManagerProxy) bm).delegate();

        Iterable<Bean<?>> accessibleBeans = delegate.getAccessibleBeans();
        System.out.println("BeanManagerImpl.getAccessibleBeans:" + accessibleBeans);
        for (Bean b : accessibleBeans) {
            debug(b);
        }

        Iterable<Bean<?>> beans = delegate.getBeans();
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
