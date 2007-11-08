/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */
package com.sun.enterprise.ee.server.pluggable;

import com.sun.enterprise.server.pluggable.TomcatPluggableFeatureImpl;
import com.sun.enterprise.server.pluggable.InternalServicesList;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.server.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.pluggable.Utils;
import com.sun.enterprise.deployment.pluggable.DeploymentFactory;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.event.pluggable.NotificationFactory;
import com.sun.enterprise.admin.target.TargetFactory;
import com.sun.enterprise.autotxrecovery.TransactionRecovery;
import com.sun.enterprise.web.SchemaUpdater;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.diagnostics.DiagnosticAgent;


import com.sun.enterprise.server.pluggable.LoggingSupport;
import com.sun.enterprise.server.pluggable.ApplicationLoaderFactory;
import com.sun.enterprise.server.pluggable.LBFeatureFactory;

/**
 * Properties that define implementation classes for pluggable features
 * used for the EE (Enterprise Edition) version of S1AS. An instance of this class
 * should be passed to getInstance() method of PluggableFeatureFactoryImpl
 * to create and have access to pluggable features.
 */
public class EEPluggableFeatureImpl extends TomcatPluggableFeatureImpl {

    /**
     * Default constructor. 
     */
    public EEPluggableFeatureImpl() {
        super();
        featureImplClasses.setProperty(Utils.getNQClassName(AdminContext.class),
            "com.sun.enterprise.ee.admin.server.core.EEAdminContextImpl");
        featureImplClasses.setProperty(Utils.getNQClassName(InternalServicesList.class),
            "com.sun.enterprise.ee.server.EETomcatServices");
        featureImplClasses.setProperty(Utils.getNQClassName(DeploymentFactory.class),
            "com.sun.enterprise.ee.deployment.pluggable.EEDeploymentFactory");
        featureImplClasses.setProperty(Utils.getNQClassName(NotificationFactory.class),
            "com.sun.enterprise.ee.admin.event.pluggable.EENotificationFactory");
        featureImplClasses.setProperty(Utils.getNQClassName(TargetFactory.class),
            "com.sun.enterprise.ee.admin.target.EETargetFactory");
        featureImplClasses.setProperty(Utils.getNQClassName(WebContainerStartStopOperation.class),
            "com.sun.enterprise.ee.web.sessmgmt.EEWebContainerStartStopOperation");
        featureImplClasses.setProperty(Utils.getNQClassName(SchemaUpdater.class),
            "com.sun.enterprise.ee.admin.hadbmgmt.EESchemaUpdater");        
        featureImplClasses.setProperty(Utils.getNQClassName(SecuritySupport.class),
            "com.sun.enterprise.ee.security.EESecuritySupportImpl");
        featureImplClasses.setProperty(Utils.getNQClassName(WebContainerFeatureFactory.class),
            "com.sun.enterprise.ee.web.EEWebContainerFeatureFactoryImpl");
        featureImplClasses.setProperty(Utils.getNQClassName(LBFeatureFactory.class),
            "com.sun.enterprise.ee.admin.lbadmin.pluggable.EELBFeatureFactoryImpl");
        featureImplClasses.setProperty(Utils.getNQClassName(DiagnosticAgent.class),
             "com.sun.enterprise.ee.diagnostics.EEDiagnosticAgent");

        featureImplClasses.setProperty(Utils.getNQClassName(TransactionRecovery.class), 
                "com.sun.enterprise.ee.server.autotxrecovery.EEAutoTransactionRecoveryServiceImpl");

    }

}
