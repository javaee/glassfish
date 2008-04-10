package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Module;

import java.util.List;
import java.beans.PropertyVetoException;

/**
 * User: Jerome Dochez
 * Date: Apr 8, 2008
 * Time: 9:45:21 PM
 */
@Ignore
public class CollectionsAccessTest extends ConfigApiTest  {


    public String getFileName() {
        return "DomainTest";
    }

    @Test(expected=IllegalStateException.class)
    public void unprotectedAccess() throws IllegalStateException {
        Applications apps = getHabitat().getComponent(Applications.class);
        assertTrue(apps!=null);
        apps.getModules().add(null);
    }

    @Test(expected= TransactionFailure.class)
    public void semiProtectedTest() throws TransactionFailure {
        final Applications apps = getHabitat().getComponent(Applications.class);
        assertTrue(apps!=null);
        ConfigSupport.apply(new SingleConfigCode<Applications>() {
            public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                // this is the bug, we should not get the list from apps but from param.
                List<Module> modules = apps.getModules();
                Module m = ConfigSupport.createChildOf(param, Module.class);
                modules.add(m); // should throw an exception
                return m;
            }
        }, apps);
    }

    @Test
    public void protectedTest() throws TransactionFailure {
        final Applications apps = getHabitat().getComponent(Applications.class);
        assertTrue(apps!=null);
        ConfigSupport.apply(new SingleConfigCode<Applications>() {
            public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                List<Module> modules = param.getModules();
                Module m = ConfigSupport.createChildOf(param, Module.class);
                modules.add(m);
                modules.remove(m);
                return m;
            }
        }, apps);
    }    
}
