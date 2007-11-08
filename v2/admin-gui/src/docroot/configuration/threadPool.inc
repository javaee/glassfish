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
<!-- configuration/threadPool.inc -->    
<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="loadDefaults" rendered="#{edit}" style="margin-left: 8pt"  primary="#{false}" text="$resource{i18n.button.LoadDefaults}" >    
        <!command
        getDefaultThreadPoolValues(ConfigName="#{configName}", ThreadPoolId="#{poolId}", MinPoolSize=>$attribute{minPoolSize}, 
        MaxPoolSize=>$attribute{maxPoolSize}, IdleTimeout=>$attribute{idleTimeout}, NoWorkQ=>$attribute{noWorkQ});
        />
    </sun:button>    
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
            <!command
            prepareSuccessfulMsg();
            saveThreadPoolValues(
            ConfigName="#{configName}"
            ThreadPoolId="#{poolId}"
            Edit="#{edit}"
            MinPoolSize="#{minPoolSize}"
            MaxPoolSize="#{maxPoolSize}"
            IdleTimeout="#{idleTimeout}"
            NoWorkQ="#{noWorkQ}"
            );
            />    
        </sun:button>

    <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
            onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
        <!command
            saveThreadPoolValues(
            ConfigName="#{configName}"
            ThreadPoolId="#{poolId}"
            Edit="#{edit}"
            MinPoolSize="#{minPoolSize}"
            MaxPoolSize="#{maxPoolSize}"
            IdleTimeout="#{idleTimeout}"
            NoWorkQ="#{noWorkQ}"
            );
            setAttribute(key="configName", value="#{configName}");
            navigate(page="configuration/threadPools.jsf") 
        />
    </sun:button>
    <sun:button id="cancelButton" rendered="#{!edit}" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            redirect(page="threadPools.jsf?configName=#{configName}");
        />
  </sun:button>

</sun:panelGroup>
</facet>   
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
        <event>
            <!beforeCreate
              getThreadPoolValues(Edit="#{edit}", ConfigName="#{configName}", ThreadPoolId="#{poolId}", 
              MinPoolSize=>$attribute{minPoolSize}, MaxPoolSize=>$attribute{maxPoolSize}, 
              IdleTimeout=>$attribute{idleTimeout}, NoWorkQ=>$attribute{noWorkQ});
            />
        </event>
        <!-- Text Field section -->               
        <sun:propertySheetSection id="propertySectionTextField">
            <sun:property id="ThreadPoolIdProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.threadPoolIdLabel}" helpText="$resource{i18n.edThread.threadPoolIdHelp}">
                <sun:staticText id="ThreadPoolId" text="#{poolId}" /> 
            </sun:property>  
            <sun:property id="ThreadPoolIdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.threadPoolIdLabel}" helpText="$resource{i18n.edThread.threadPoolIdHelp}">
                <sun:textField id="ThreadPoolIdText" styleClass="required" text="#{poolId}" columns="$int{55}" required="#{true}" /> 
            </sun:property> 
            <sun:property id="MinPoolSizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.minPoolSizeLabel}" helpText="$resource{i18n.edThread.minPoolSizeHelp}">
                <sun:textField id="MinPoolSize" styleClass="integer" columns="$int{20}" text="#{requestScope.minPoolSize}" />      
            </sun:property>
            <sun:property id="MaxPoolSizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.maxPoolSizeLabel}" helpText="$resource{i18n.edThread.maxPoolSizeHelp}">
                <sun:textField id="MaxPoolSize" styleClass="integer" columns="$int{20}" text="#{requestScope.maxPoolSize}" />      
            </sun:property>
            <sun:property id="IdleTimeoutProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.idleTimeoutLabel}" helpText="$resource{i18n.edThread.idleTimeoutHelp}">
                <sun:textField id="IdleTimeout" styleClass="integer" columns="$int{20}" text="#{requestScope.idleTimeout}" />      
                <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
            </sun:property>
            <sun:property id="NoWorkQProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edThread.noWorkQLabel}" helpText="$resource{i18n.edThread.noWorkQHelp}">
                <sun:textField id="NoWorkQ" styleClass="integer" columns="$int{20}" text="#{requestScope.noWorkQ}" />      
            </sun:property>
        </sun:propertySheetSection>                            
</sun:propertySheet>    

