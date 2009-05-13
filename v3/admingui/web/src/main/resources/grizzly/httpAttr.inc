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
<!-- grizzly/httpAttr.inc -->


<sun:propertySheetSection id="httpTextField" label="#{pageSession.httpSectionTitle}" >

        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.cometSupport}" >
            <sun:checkbox label="$resource{i18n_web.common.enabled}" selected="#{pageSession.httpMap['EnableCometSupport']}" selectedValue="true" />
        </sun:property>
        <sun:property id="maxC"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.maxConnections}" helpText="$resource{i18n_web.http.maxConnectionsHelp}" >
            <sun:textField id="maxC" columns="$int{20}" text="#{pageSession.httpMap['MaxConnections']}" />
        </sun:property>
        <sun:property id="DefaultVirtServersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.defVirtualServerLabel}" helpText="$resource{i18n_web.http.defVirtualServerLabel}">
            <sun:dropDown id="vs" selected="#{pageSession.httpMap['DefaultVirtualServer']}" labels="$pageSession{vsList}"  values="$pageSession{vsList}" >
            <!beforeCreate
                getChildrenByType(parentObjectNameStr="v3:pp=/domain/configs/config[#{pageSession.configName}],type=http-service" type="virtual-server" result="#{pageSession.vsList}");
            />
           </sun:dropDown>
        </sun:property>
        <sun:property id="ServerNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.serverNameLabel}" helpText="$resource{i18n_web.http.serverNameHelp}" >
            <sun:textField id="ServerName" columns="$int{20}" text="#{pageSession.httpMap['ServerName']}" />
        </sun:property>
        <sun:property id="RedirectPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.http.redirectPortLabel}" helpText="$resource{i18n_web.http.redirectPortHelp}" >
            <sun:textField id="RedirectPort" styleClass="port" columns="$int{20}" text="#{pageSession.httpMap['RedirectPort']}" />
        </sun:property>

</sun:propertySheetSection>