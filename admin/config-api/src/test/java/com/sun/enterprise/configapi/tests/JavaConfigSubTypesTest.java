package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Assert;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.beans.PropertyVetoException;

/**
 * User: Jerome Dochez
 * Date: Apr 7, 2008
 * Time: 11:13:22 AM
 */
public class JavaConfigSubTypesTest extends ConfigPersistence {


    @Test
    public void testSubTypesOfDomain() {
        JavaConfig config = super.getHabitat().getComponent(JavaConfig.class);
        try {
            Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) ConfigBean.unwrap(config));
            boolean found=false;
            for (Class subType : subTypes) {
                Logger.getAnonymousLogger().fine("Found class " + subType);
                if (subType.getName().equals(List.class.getName())) {
                    found=true;
                }
            }
            Assert.assertTrue(found);;
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    Habitat habitat = Utils.getNewHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public Habitat getHabitat() {
        return habitat;
    }

    @Test
    public void doTest() throws TransactionFailure {


        JavaConfig javaConfig = habitat.getComponent(JavaConfig.class);

        ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {
            public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                List<String> jvmOptions = param.getJvmOptions();
                jvmOptions.add("-XFooBar=true");
                return jvmOptions;
            }
        }, javaConfig);

    }

    public boolean assertResult(String s) {
        return s.indexOf("-XFooBar")!=-1;
    }
    
}
