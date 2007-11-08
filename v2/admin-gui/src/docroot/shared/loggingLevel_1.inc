<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
-->
<!-- shared/loggingLevel_1.inc -->
<!-- when including this page, should followed by including loggingLevel_2.inc to make a complete page -->

<sun:page id="page1">
    <!beforeCreate
        setResourceBundle(key="i18n" bundle="com.sun.enterprise.tools.admingui.resources.Strings")
        setPageSessionAttribute(key="configName", value="$requestParameter{configName}");   
        getServerModuleLogLevels(ConfigName="#{configName}", Admin=>$attribute{admin}, 
            Classloader=>$attribute{classLoader}, Configuration=>$attribute{configuration}, 
            Connector=>$attribute{connector}, Corba=>$attribute{corba}, Javamail=>$attribute{javamail}, 
            Deployment=>$attribute{deployment}, Jaxr=>$attribute{jaxr}, Jaxrpc=>$attribute{jaxrpc}, 
            Jms=>$attribute{jms}, Jta=>$attribute{jta}, Jts=>$attribute{jts}, MDB=>$attribute{mdb}, 
            Root=>$attribute{root}, Saaj=>$attribute{saaj}, Security=>$attribute{security}, SelfManagement=>$attribute{selfManagement}, 
            Server=>$attribute{server}, Util=>$attribute{util}, Verifier=>$attribute{verifier}, WEB=>$attribute{web}, 
            Naming=>$attribute{naming}, EJB=>$attribute{ejb}, Jbi=>$attribute{jbi}, Jaxws=>$attribute{jaxws},
            NodeAgent=>$attribute{nodeAgent}, Synchronization=>$attribute{synchronization}, Gms=>$attribute{gms},
            );

        getModuleLogLevelProperties(ConfigName="#{configName}", Properties=>$page{props});
        getTableList(Properties="#{props}", TableList=>$attribute{tableList});
        getPredefinedLogLevels(levelsLabel=>$attribute{levelsLabel}, levelsValue=>$attribute{levelsValue});

    />
#include "shared/restart.inc"
    <sun:html id="html2">
        <sun:head id="propertyhead" title="$resource{i18n.log.LogLevelsPageTitle}">
            <sun:script url="../js/restart.js" />
        </sun:head>
        <sun:body id="body3" onLoad="javascript: synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}')">
            <sun:form id="propertyForm">
#include treeBreadcrumbs.inc
