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
<!-- configuration/threadPoolAttrs.inc -->

<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
            <sun:staticText id="name" text="#{pageSession.Name}"/>
        </sun:property> 
        <sun:property id="nameProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18n_web.threadPool.nameHelp}">
            <sun:textField id="nameText" text="#{pageSession.valueMap['Name']}" columns="$int{55}" styleClass="required" required="#{true}" />
        </sun:property>

        <sun:property id="classname"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.classname}" helpText="$resource{i18n_web.threadPool.classnameHelp}">
        <sun:textField id="classname" styleClass="required" columns="$int{50}" text="#{pageSession.valueMap['Classname']}" />
        </sun:property>

        <sun:property id="max"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.maxQueue}" helpText="$resource{i18n_web.threadPool.maxQueueHelp}">
        <sun:textField id="max" styleClass="intAllowMinusOne" columns="$int{10}" text="#{pageSession.valueMap['MaxQueueSize']}" />
        </sun:property>

        <sun:property id="maxThread"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.maxThread}" helpText="$resource{i18n_web.threadPool.maxThreadHelp}">
        <sun:textField id="maxThread" styleClass="intAllowMinusOne" columns="$int{10}" text="#{pageSession.valueMap['MaxThreadPoolSize']}" />
        </sun:property>

        <sun:property id="minThread"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.minThread}" helpText="$resource{i18n_web.threadPool.minThreadHelp}">
        <sun:textField id="minThread" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['MinThreadPoolSize']}" />
        </sun:property>

        <sun:property id="timeout"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.threadPool.timeout}" helpText="$resource{i18n_web.threadPool.timeoutHelp}">
        <sun:textField id="timeout" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['IdleThreadTimeoutSeconds']}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>

    </sun:propertySheetSection>
    </sun:propertySheet>