
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.rails;

import org.jvnet.hk2.annotations.Service;
import com.sun.grizzly.jruby.RubyObjectPool;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import com.sun.logging.LogDomains;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ContractProvider for Rails
 *
 * @author Jerome Dochez
 */
@Service(name="com.sun.enterprise.rails.RailsContainer")
public class RailsContainer implements Container, PostConstruct, PreDestroy {

    private Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);
    private String jrubyLib = null;

    private int numberOfRuntime = 1;

    private RubyObjectPool pool = null;
    
    public void postConstruct() {

        String jrubyBase = System.getProperty("jruby.base");
        String jrubyHome = System.getProperty("jruby.home");
        String jrubyShell = System.getProperty("jruby.shell");
        String jrubyScript = System.getProperty("jruby.script");
        String jrubyRuntime = System.getProperty("jruby.runtime");
        String railsEnv = System.getProperty("RAILS_ENV");

        if (jrubyHome == null) {
            logger.severe("jruby.home cannot be null");
            return;
        }
        if (jrubyBase == null) {
            jrubyBase = jrubyHome;
        }
        if (jrubyShell == null) {
            jrubyShell = "";
        }
        if (jrubyScript == null) {
            jrubyScript = "";
        }
        
        // If the user is running V3 as a gem, and has provided a --jruby.runtime
        // property value, we would use that value. In that case the numberOfRuntime
        // would already be set, hence avoid the check below and continue.
        if (numberOfRuntime == 1) {
            // For now using the numberOfRuntimes provided by the user as is, later
            // this would be tied to the mode of deployment production/development/test
            // In the near future provide a jruby-container element in the domain.xml
            // that could have all the jruby related information as part of it.
            if (jrubyRuntime != null) {
                try {
                    numberOfRuntime = Integer.parseInt(jrubyRuntime);
                } catch (NumberFormatException ex) {
                    logger.log(Level.WARNING, "Invalid number of Runtimes specified");
                }
            }
            if ((railsEnv != null && railsEnv.equalsIgnoreCase("production")) &&
                (jrubyRuntime == null)) {
                // By default if the user has defined a production environment and
                // not provided the number of runtimes to start we start with a 
                // default value of '3'. Why '3' (have no idea, just a number that 
                // I decided on).
                numberOfRuntime = 3;
            }
        }
        
        System.setProperty("jruby.script", jrubyScript);
        System.setProperty("jruby.shell", jrubyShell);
        System.setProperty("jruby.base", jrubyBase);
        // Do we really need to set system property for this
        // System.setProperty("jruby.runtime", Integer.valueOf(numberOfRuntime).toString());

        jrubyLib = System.getProperty("jruby.lib");

        if (jrubyLib == null) {
            jrubyLib = (new File(jrubyHome, "lib")).getPath();
            System.setProperty("jruby.lib", jrubyLib);
        }
        
        initializeRubyRuntime();
        
        return;
    }
    
    protected void initializeRubyRuntime() {
        pool = new RubyObjectPool();
        pool.setNumberOfRuntime(numberOfRuntime);
        pool.setJrubyLib(jrubyLib);
    }    
    
    RubyObjectPool getPool() {
        return pool;
    }
    

    public URL getInfoSite() {
        try {
            return new URL("http://jruby.dev.java.net");
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public Class<? extends Deployer> getDeployer() {
        return RailsDeployer.class;
    }

    public String getName() {
        return "Rails";
    }

    public void preDestroy() {
        if (pool!=null) {
            pool.stop();
            pool = null;
        }
    }
                
}
