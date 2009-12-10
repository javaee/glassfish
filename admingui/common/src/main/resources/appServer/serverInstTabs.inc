
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
<!-- appServer/serverInstTabs.jsf -->

<sun:tabSet id="serverInstTabs" immediate="true" selected="#{sessionScope.serverInstTabs}">

    <sun:tab id="general" immediate="true" text="$resource{i18n.common.General}" toolTip="$resource{i18n.common.General} Tab" >
        <!command
        setSessionAttribute(key="serverInstTabs" value="general");
        gf.redirect(page="#{request.contextPath}/common/appServer/serverInstGeneralPe.jsf");
	    />       
    </sun:tab>

    <sun:tab id="advanced" immediate="true" text="$resource{i18n.headings.Advanced}" toolTip="$resource{i18n.headings.Advanced} Tab">
        <!command
        setSessionAttribute(key="serverInstTabs" value="advanced");
        gf.redirect(page="#{request.contextPath}/common/appServer/serverInstAppsConfig.jsf?configName=server-config");
        />
        <sun:tab id="appConfig" immediate="true" text="$resource{i18n.headings.AppConfig}" toolTip="$resource{i18n.headings.AppConfig} Tab" >
            <!command
            setSessionAttribute(key="serverInstTabs" value="appConfig");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstAppsConfig.jsf?configName=server-config");
            />
        </sun:tab>
        <sun:tab id="domainAttrs" immediate="true" text="$resource{i18n.headings.DomainAttributes}" toolTip="$resource{i18n.headings.DomainAttributes} Tab" >
            <!command
            setSessionAttribute(key="serverInstTabs" value="domainAttrs");
            gf.redirect(page="#{request.contextPath}/common/appServer/serverInstDomainAttrs.jsf?configName=server-config");
            />
        </sun:tab>
    </sun:tab>

      <sun:tab id="adminPassword" immediate="true" text="$resource{i18n.headings.AdminPassword}" toolTip="$resource{i18n.headings.AdminPassword} Tab" >
	    <!command
        setSessionAttribute(key="serverInstTabs" value="adminPassword");
		gf.redirect(page="#{request.contextPath}/common/appServer/serverInstAdminPassword.jsf");
	    />
    </sun:tab>

    <sun:tab id="token" immediate="true" text="$resource{i18n.systemProps.tableTitle}" toolTip="$resource{i18n.systemProps.tableTitle} Tab">
        <!command
            setSessionAttribute(key="serverInstTabs" value="token");
            gf.redirect(page="#{request.contextPath}/common/appServer/token.jsf");
        />
    </sun:tab>

</sun:tabSet>
<event>
    <!afterCreate
	includeIntegrations(type="org.glassfish.admingui:serverInstTab");
    />
</event>
