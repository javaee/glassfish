/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */

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
