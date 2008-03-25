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

/**
 * Deletes given JVM options in server's configuration.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.i18n.StringManager;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name="delete-jvm-options")   //implements the cli command by this "name"
@Scoped(PerLookup.class)            //should be provided "per lookup of this class", not singleton
public final class DeleteJvmOptions implements AdminCommand {

    @Param(name="target", optional=true)
    String target;
    
    //Injection of the config beans is not going to work, because it
    //depends what target is being sent on command line -- this is a temporary measure
    @Inject JavaConfig jc;
    
    @Param(primary=true)
    String optString;
    
    private static final StringManager lsm = StringManager.getManager(ListJvmOptions.class); 
    private static final Logger logger     = Logger.getLogger(DeleteJvmOptions.class.getPackage().getName()); // TODO: change later
    public void execute(AdminCommandContext context) {
        //validate the target first
        logfh("Injected JavaConfig: " + jc);
        final ActionReport report = context.getActionReport();
        List<Joe> joes             = Joe.toJoes(optString);
        report.getTopMessagePart().setMessage(lsm.getStringWithDefault("delete.jvm.options.success", "Command: delete-jvm-options successfully executed"));
        report.getTopMessagePart().setChildrenType("jvm-options");
        try {
            deleteX(jc, Joe.toStrings(joes));
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage("deleted " + joes.size() + " option(s)");
        } catch (Exception e) {
            String msg = lsm.getStringWithDefault("delete.jvm.options.failed",
                    "Command: delete-jvm-options failed", new String[]{e.getMessage()});
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);        
    }

    public DeleteJvmOptions() {
        //for debugging purpose, uncomment the line below to see that a new object is constructed every time!
        logfh(this); //unsafe to generally do this, but I am sending it to a private method in a "final" class
    }

    private static void logfh(Object o) {
        if (logger.isLoggable(Level.FINE)) {
            if (o == null) 
                logger.fine("null reference passed");
            else
                logger.fine("Hashcode of the given object: " + o.hashCode());
        }
    }
    
    /** Adds the JVM option transactionally.
     * @throws java.lang.Exception
     */
    // following should work in the fullness of time ...
    /*
    private static void addX(JavaConfig jc, final String option) throws Exception {
        SingleConfigCode<JavaConfig> scc = new SingleConfigCode<JavaConfig> () {
            public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
                List<String> jvmopts = jc.getJvmOptions();
                jvmopts.add(option);
                return ( jc.getJvmOptions() );
            }
        };
        ConfigSupport.apply(scc, jc);
    }
    */
    //@ForTimeBeing :)
    private static void deleteX(JavaConfig jc, final List <String> newOpts) throws Exception {
        SingleConfigCode<JavaConfig> scc = new SingleConfigCode<JavaConfig> () {
            public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
                List<String> jvmopts = new ArrayList<String>(jc.getJvmOptions()); //copy
                jvmopts.removeAll(newOpts);
                jc.setJvmOptions(jvmopts);
                return true;
            }
        };
        ConfigSupport.apply(scc, jc);
    }
}
