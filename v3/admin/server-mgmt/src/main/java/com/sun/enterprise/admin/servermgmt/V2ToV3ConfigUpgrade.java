/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt;

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Change the jvm-options from v2 to v3
 * @author Byron Nevins
 */

@Service
@Scoped(PerLookup.class)
public class V2ToV3ConfigUpgrade  implements ConfigurationUpgrade, PostConstruct {
    @Inject
    JavaConfig jc;

    public void postConstruct() {
        try {
            oldJvmOptions = Collections.unmodifiableList(jc.getJvmOptions());
            doAdditions();
            doRemovals();
            ConfigSupport.apply(new JavaConfigChanger(), jc);
        }
        catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                "Failure while upgrading jvm-options from V2 to V3", e);
            throw new RuntimeException(e);
        }
    }

    private void doRemovals() {
        // copy options from old to new.  Don't add items on the removal list
        // note that the remove list also has all the items we just added with
        // doAdditions() so that we don't get duplicate messes.
        for(String s : oldJvmOptions) {
            if(!shouldRemove(s))
                newJvmOptions.add(s);
        }
    }

    private void doAdditions() {
        // add new options
        for(String s : ADD_LIST) {
            newJvmOptions.add(s);
        }
    }

    private boolean shouldRemove(String option) {
        if(!ok(option))
            return true;

        for(String s : REMOVAL_LIST)
            if(option.startsWith(s))
                return true;

        return false;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private         List<String> oldJvmOptions;
    private final   List<String> newJvmOptions = new ArrayList<String>();

    private static final String[] REMOVAL_LIST = new String[] {
            "-Djavax.management.builder.initial",
            "-Dsun.rmi.dgc.server.gcInterval",
            "-Dsun.rmi.dgc.client.gcInterval",
            "-Dcom.sun.enterprise.taglibs",
            "-Dcom.sun.enterprise.taglisteners",

            // the following are items from the add list...
            "-XX:+UnlockDiagnosticVMOptions",
            "-XX:+LogVMOutput",
            "-XX:LogFile",
            "-DANTLR_USE_DIRECT_CLASS_LOADING",
        };

    private static final String[] ADD_LIST = new String[] {
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+LogVMOutput",
        "-XX:LogFile=${com.sun.aas.instanceRoot}/logs/jvm.log",
        "-DANTLR_USE_DIRECT_CLASS_LOADING=true",
    };

    private class JavaConfigChanger implements SingleConfigCode<JavaConfig> {
        public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
           jc.setJvmOptions(newJvmOptions);
           return jc;
        }
    }
}
