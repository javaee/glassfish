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

<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
#include "virtualServerButtons.inc"
    </sun:panelGroup>
</facet>

<!facet pageButtonsBottom>
    <sun:panelGroup id="bottomButtons">
#include "virtualServerButtons.inc"
    </sun:panelGroup>
</facet>
        

<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet" requiredFields="true">		
        <!beforeCreate
            getVirtualServerSettings(
                Edit="#{edit}",
                ConfigName="#{configName}", 
                Name="#{name}",
                Hosts=>$attribute{hosts},
                Properties=>$page{props}, 
                StateOption=>$page{state}, 
                Http=>$attribute{http}, 
                Web=>$attribute{web}, 
                LogFile=>$attribute{logFile},
                docroot=>$attribute{docroot}
                accesslog=>$attribute{accesslog}
                accessLogBufferSize=>$attribute{accessLogBufferSize}
                accessLogWriteInterval=>$attribute{accessLogWriteInterval}
                sso=>$attribute{sso}
                accessLoggingFlag=>$page{accessLoggingFlag}
            ); 
            getModulesByType( type="web" modules="#{requestScope.modules}");
            getTableList(Properties="#{props}", TableList=>$attribute{tableList});
        />

        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.Id}" >
                <sun:staticText id="IdStatic" text="#{name}"/> 
            </sun:property> 

            <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.Id}">
                <sun:textField id="IdText" styleClass="required" text="#{name}" columns="$int{55}" required="#{true}" /> 
            </sun:property> 

            <sun:property id="hostsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.Hosts}" helpText="$resource{i18n.vs.HostsHelp}">
                <sun:textField id="Hosts" styleClass="required" columns="$int{55}" text="#{hosts}" required="#{true}"/> 
            </sun:property>   
                    
            <sun:property id="stateProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.State}">                           
                 <sun:radioButton id="on"  name="stateGp" label="$resource{i18n.vs.StateOn}" selected="$pageSession{state}" selectedValue="on" />                              
                 # <sun:helpInline id="flag1H" style="padding: 4pt" style="font-size: 8pt" text=""/>
                 "<br />
                 <sun:radioButton id="off"  name="stateGp" label="$resource{i18n.vs.StateOff}" selected="$pageSession{state}" selectedValue="off" />                              
                 "<br />
                 <sun:radioButton id="disabled"  name="stateGp" label="$resource{i18n.vs.StateDisabled}" selected="$pageSession{state}" selectedValue="disabled" />                              
                 "<br />
             </sun:property>

            <sun:property id="httpProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.HttpListeners}" helpText="$resource{i18n.vs.HttpListenersHelp}">
                <sun:textField id="Http" columns="$int{55}" text="#{http}"/> 
            </sun:property>    
            <sun:property id="webProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.DefaultWeb}" helpText="$resource{i18n.vs.DefaultWebHelp}">                           
                <sun:dropDown id="Web" labels="$attribute{modules}" selected="#{web}" >
                </sun:dropDown>
            </sun:property> 
            <sun:property id="logFileProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.LogFile}" helpText="$resource{i18n.vs.LogFileHelp}">
                <sun:textField id="LogFile" columns="$int{55}" text="#{logFile}" />
            </sun:property>  

            <sun:property id="docroot"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.docroot}" helpText="$resource{i18n.vs.docrootHelp}">
                <sun:textField id="docroot" columns="$int{55}" text="#{docroot}" />
            </sun:property>  

            <sun:property id="sso" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.sso}">
                <sun:checkbox label="$resource{i18n.common.Enabled}"  selected="#{sso}" />  
            </sun:property>

        </sun:propertySheetSection>

        <sun:propertySheetSection id="al" label="$resource{i18n.vs.accessLog}">

            <sun:property id="enableLog"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.accessLogging}">

                 <sun:radioButton id="httpS"  name="accessLoggingFlagGp" label="$resource{i18n.vs.controlByHS}" selected="$pageSession{accessLoggingFlag}" selectedValue="off" />                              
                 " <b>$resource{i18n.vs.httpLink, $pageSession{configName}}</b>              
                 # <sun:helpInline id="flag1H" style="padding: 4pt" style="font-size: 8pt" text=""/>
                 "<br />
                 <sun:radioButton id="ena"  name="accessLoggingFlagGp" label="$resource{i18n.vs.alwaysEnable}" selected="$pageSession{accessLoggingFlag}" selectedValue="true" />                              
                 "<br />
                 <sun:radioButton id="dis"  name="accessLoggingFlagGp" label="$resource{i18n.vs.alwaysDisable}" selected="$pageSession{accessLoggingFlag}" selectedValue="false" />                              
                 "<br />
             </sun:property>  

            <sun:property id="accesslog"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.path}" helpText="$resource{i18n.vs.pathHelp}">
                <sun:textField id="accesslog" columns="$int{55}" text="#{accesslog}" />
            </sun:property> 

            <sun:property id="accessLogBufferSize"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.accessLogBufferSize}" helpText="$resource{i18n.vs.accessLogBufferSizeHelp}">
                <sun:textField id="accessLogBufferSize" styleClass="intAllowMinus" columns="$int{55}" text="#{accessLogBufferSize}" />
                <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Bytes}"/>
                </sun:property> 
            <sun:property id="accessLogWriteInterval"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.vs.accessLogWriteInterval}" helpText="$resource{i18n.vs.accessLogWriteIntervalHelp}">
                <sun:textField id="accessLogWriteInterval" styleClass="intAllowMinus" columns="$int{55}" text="#{accessLogWriteInterval}" />
                <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
            </sun:property> 
            
        </sun:propertySheetSection>
</sun:propertySheet>    

