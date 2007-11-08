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
<!-- shared/instanceGeneral.inc -->

<sun:title id="propertyContentPage" title="$resource{i18n.instance.GeneralTitle}"
    helpText="$resource{i18n.standalone.GeneralPageHelp}"> 
 <!-- Buttons  -->
    <sun:button id="startInstance" text="$resource{i18n.button.startInstance}" rendered="#{!isAdminServer}" disabled="#{running}" 
        onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');">
        <!command  startServerInstance(instanceName="#{instanceName}");
                   setAttribute(key="instanceName" value="$pageSession{instanceName}");
                   navigate(page="$pageSession{pageName}"); />
    </sun:button>                     
    <sun:button id="stopInstance" text="$resource{i18n.button.stopInstance}" disabled="#{!running}" 
        onClick="if (getConfirm(this, '$resource{i18n.msg.JS.confirmStopInstance}') )
                 { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                  else {return false;} " >
        <!command
            if($pageSession{isAdminServer}){
		redirect("#{facesContext.externalContext.requestContextPath}/shutdown.jsf");
            }
            if(!$pageSession{isAdminServer}){
                stopServerInstance(instanceName="#{instanceName}") 
                setAttribute(key="instanceName" value="$pageSession{instanceName}");
                navigate(page="$pageSession{pageName}");
            }
        />
    </sun:button>
    <sun:button id="logViewer" text="$resource{i18n.button.logViewer}"  disabled="#{!running}"   
        onClick="javascript:var win=window.open('../logViewer/logViewer.jsf?instanceName=#{instanceName}&loglevel=INFO&logFile=server.log&viewResults=true','LogViewerWindow','width='+(screen.width-300)+',height='+(screen.height-75)+',top=0,left=0,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no');win.focus(); return false;"
    />
    <sun:button id="rotateLog" text="$resource{i18n.button.rotateLog}"  primary="#{false}"
        onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');">
        <!command rotateLogFile(instanceName="#{instanceName}") 
               redirect(page="../$pageSession{pageName}?instanceName=#{instanceName}&clusterName=#{clusterName}");                
        />
    </sun:button>
    <sun:button id="jndiBrowsing" text="$resource{i18n.button.jndiBrowsing}" 
        onClick="javascript:var win=window.open('../jndiTree/jndiNavFrame.jsf?instanceName=#{instanceName}','JndiBrowsingWindow','width='+(screen.width/2)+',height='+(screen.height/2)+',top=0,left=0,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no');win.focus();return false; var f=document.form;if (f != null) {f.submit(); return true}">
    </sun:button>
    <sun:button id="recoverTrans" text="$resource{i18n.button.recoverTrans}" primary="#{false}" >                     
        <!command  
        setAttribute(key="clusterName" value="$pageSession{clusterName}" );
        setAttribute(key="instanceName" value="$pageSession{instanceName}");
        setAttribute(key="pageName" value="$pageSession{pageName}");
        navigate(page="appServer/recoverTransactions.jsf"); 
        />
   </sun:button>
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertySectionTextField">
        <sun:property id="nameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <sun:staticText id="name" text="#{instanceName}" />
        </sun:property>
        <sun:property id="statusProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            
        <sun:staticText id="status" text="#{status}" />
        </sun:property>

        <sun:property id="lb"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.lbLabel}" >
            <event>
                <!beforeEncode
                    getLBforTarget(targetName="#{instanceName}", isCluster="$boolean{false}" result=>$page{listOfLBs} hasLB=>$page{hasLB});
                />				    
            </event>
            <foreach key="lb" list="#{listOfLBs}">
                <sun:hyperlink url="/loadBalancer/loadBalancerGeneral.jsf?lbName=#{lb.lbName}" text="#{lb.lbName} " />
                <sun:staticText id="dummy" text=" , "  />
            </foreach>

            <sun:button id="lbstart" text="$resource{i18n.button.startLB}"  rendered="#{hasLB}" 
                ####  we want to always enable this button, refer to bug#6571406  disabled="#{!running}"  ###
                onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');">
                <!command  
                    changeInstanceLoadBalancing(instanceName="#{instanceName}" enabled="#{true}", cluster="#{clusterName}");
                    setAttribute(key="instanceName" value="$pageSession{instanceName}");
                    navigate(page="$pageSession{pageName}"); />
            </sun:button> 

            <sun:button id="lbStop" text="$resource{i18n.button.quiesce}"  rendered="#{hasLB}"  disabled="#{!running}" 
                onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');">
                <!command  
                    changeInstanceLoadBalancing(instanceName="#{instanceName}"   enabled="#{false}", cluster="#{clusterName}" );
                    setAttribute(key="instanceName" value="$pageSession{instanceName}");
                    navigate(page="$pageSession{pageName}"); />
            </sun:button>
        </sun:property>


       <sun:property id="httpPortsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.httpPortsLabel}">
            <sun:staticText id="httpPorts" text="#{httpPorts}" />
        </sun:property>   
       <sun:property id="iiopPortsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.iiopPortsLabel}">
            <sun:staticText id="iiopPorts" text="#{iiopPorts}" />
        </sun:property>  
        <sun:property id="jvmProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.JVMLabel}">
            <sun:hyperlink id="jvmlink" text="$resource{i18n.inst.jvmReport}" 
                onClick="javascript:
                var jvmWin = window.open('../appServer/jvmReport.jsf?instanceName=#{instanceName}&pageTitle=$resource{i18n.jvm.jvmReportEEPageName, #{instanceName}}','JVMReport','width='+(screen.width-300)+',height='+(screen.height-75)+',top=0,left=0,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no');
                jvmWin.focus();return false;" />
        </sun:property>   
        <sun:property id="configProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.standalone.Config}">
            <sun:hyperlink id="configlink" text="#{config}" 
                url="/configuration/configuration.jsf?configName=#{config}" />
        </sun:property>   
       <sun:property id="nodeAgentProp"  rendered="#{!isAdminServer}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.standalone.NodeAgent}">
            <sun:hyperlink id="nodeAgentlink" text="#{nodeAgent}" 
                url="/nodeAgent/nodeAgentGeneral.jsf?nodeAgentName=#{nodeAgent}" />
        </sun:property>                               
       <sun:property id="nodeAgentStatusProp"  rendered="#{!isAdminServer}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.standalone.NodeAgentStatus}">
           <sun:staticText id="nodeAgentStatus" text="#{nodeAgentStatus}" /> 
        </sun:property>    
       <sun:property id="configDirProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.configDirLabel}">
           <sun:staticText id="configDir" text="#{configDir}" />
        </sun:property>        
       <sun:property id="versionProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.versionLabel}">
           <sun:staticText id="version" text="#{version}" />
        </sun:property>                                
       <sun:property id="debugProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.inst.debugLabel}">
           <sun:staticText id="debug" text="#{debugPort}" />
           "<br /><br />
        </sun:property>

        <sun:property id="updateProp"  rendered="#{isAdminServer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" label="#{updateCenterMsg}" >    
            <sun:staticText id="update" text=" " />
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>    
