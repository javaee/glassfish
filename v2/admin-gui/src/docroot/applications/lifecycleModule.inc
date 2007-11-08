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
<!-- lifecycleModule.inc -->


    <sun:property id="classNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.lifecycleModule.classname}" helpText="$resource{i18n.lifecycleModule.classnameHelp}" >
        <sun:textField id="classname" styleClass="required" columns="$int{55}" text="#{classname}"  required="#{true}" />      
    </sun:property> 

    <sun:property id="pathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  label="$resource{i18n.lifecycleModule.classPath}" helpText="$resource{i18n.lifecycleModule.classPathHelp}" >
        <sun:textField id="classPath" columns="$int{55}" text="#{classpath}" />      
    </sun:property> 

    <sun:property id="loadOrderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.lifecycleModule.loadOrder}"  helpText="$resource{i18n.lifecycleModule.loadOrderHelp}" >
        <sun:textField id="loadOrder" styleClass="integer" columns="$int{4}" text="#{loadOrder}" />      
    </sun:property> 

    <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
        <sun:textField id="desc" columns="$int{55}" text="#{requestScope.description}" />      
    </sun:property>

    <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
        <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.enabled}" />  
   </sun:property>

   <sun:property id="statusProp2" rendered="#{useString}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" 
        label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
        <sun:staticText  text="#{requestScope.enabledString}" />  
   </sun:property>

    <sun:property id="failProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.lifecycleModule.onLoadFailure}" helpText="$resource{i18n.lifecycleModule.onLoadFailureHelp}" >
        <sun:checkbox label="$resource{i18n.lifecycleModule.preventStart}" selected="#{isFailureFatal}" />  
   </sun:property>   

    "<br /><br />

