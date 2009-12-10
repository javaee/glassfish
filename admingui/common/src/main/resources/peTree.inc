<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- peTree.jsf 

JVM             common  100
Logger Setting  common  150

Web Container    web  200
EJB Container    ejb-lite  250
Ruby Container   jruby    280
JMS             jms-plugin  300
Security       common  350
Transaction    jts  400
HTTP Srvice    web  450
Virtual Server web  500
Network Config web  550
ORB            corba  600
Thread Pools   web  650
Admin Service  common 700
Connector Service   750
monitoring     web  800

update tool    updatetool 800
-->



<event>
    <!beforeCreate
	#getConfigNameOfTarget(target="server" configName=>$page{configName});
	setPageSessionAttribute(key="configName" value="server-config");
    />
</event>
 

//<sun:body  onLoad="fixTree(); setTreeWidth('#{treeId}');" >
  <sun:form id="treeForm">
    <h:commandButton id="update" style="display: none;">

	<!-- KEEP THIS IN SYNCH WITH clusterProfileTree.jsf -->
	<!command
            setResourceBundle(key="i18n" bundle="org.glassfish.admingui.core.Strings");

	    // Find the old component
	    <!-- It is safe to use #{param} in this handler -->
            convertStringToMap(str="#{param.params}", delimiter="&", result="params");

	    getUIComponent(clientId="#{params.updateTreeNode}", component="#{oldComp}");

	    setAttribute(key="parent" value="#{oldComp.parent}");

	    // Replace it with something temporary to hold position
	    // Needed, or it it will just find the old one vs. create it
	    createComponent(type="staticText" component=>$attribute{temp});
	    replaceUIComponent(old="${oldComp}" new="$attribute{temp}");

	    // Rengenerate it
	    getLayoutComponent(
		viewId="#{params.viewId}"
		clientId="#{params.relId}"
		component=>$attribute{desc});
	    buildUIComponentTree(layoutElement="$attribute{desc}" parent="#{parent}" result=>$attribute{newComp});
	    replaceUIComponent(old="${temp}" new="$attribute{newComp}");
	/>
    </h:commandButton>
    <sun:tree id="tree" 
	      text="$resource{i18n.tree.commonTasks}"
	      url="/common/commonTask.jsf"
	      imageURL="/resource/images/common_tasks.gif"
	      clientSide="#{true}">
	      <!facet content>
		// Doing this avoids the JS which highlights the entire tree.
		<sun:hyperlink id="ct" url="/common/commonTask.jsf" text="$resource{i18n.tree.commonTasks}" />
	      </facet>
	  <!afterCreate
	      getClientId(component="$this{component}" clientId=>$page{treeId});
	  />

        <!--  ===================================== Registration  ======================  -->
        <sun:treeNode id="registration"
            rendered="#{sessionScope.showRegTreeNode}"
            text="$resource{i18n.tree.registration}"
            url="/common/sysnet/registration.jsf"
            >
            <!facet image>
                <sun:imageHyperlink imageURL="/resource/images/registration.gif" url="/common/sysnet/registration.jsf"
                    alt="$resource{i18n.tree.registration}" border="0" immediate="true" />
            </facet>
        </sun:treeNode>

        <sun:treeNode id="support" rendered="#{!sessionScope._noNetwork}"
                text="$resource{i18n.tree.support}"
                url="/common/sysnet/support.jsf"
                >
                <!facet image>
                    <sun:imageHyperlink imageURL="/resource/images/support.gif" url="/common/sysnet/support.jsf"  
                        border="0" immediate="true" alt="$resource{i18n.tree.support}"/>
                </facet>
        </sun:treeNode>

        <!--  ===================================== Application Server  ======================  -->
        <sun:treeNode id="applicationServer"
                      text="$resource{i18n.tree.appServer}"
                      url="/common/appServer/serverInstGeneralPe.jsf?instanceName=server"
                      imageURL="/resource/images/instance.gif"
                      >
            <!facet image>
                <sun:imageHyperlink imageURL="/resource/images/instance.gif" url="/common/appServer/serverInstGeneralPe.jsf?instanceName=server"
                    border="0" immediate="true" alt="$resource{i18n.tree.appServer}"/>
            </facet>

        </sun:treeNode>

        <!--  ===================================== Applications ======================  -->

        <dynamicTreeNode id="applications"
            treeAdaptorClass="org.glassfish.admingui.common.tree.MBeanTreeAdaptor"
            objectName="amx:pp=/domain,type=applications"
            text="$resource{i18n.tree.applications}"
            url="/common/applications/applications.jsf"
            expanded="$boolean{false}"
            attributeName="name"
            childImageURL="/resource/images/webModule.gif"
            childExpanded="$boolean{false}"
            childURL="/common/applications/applicationEdit.jsf?appName=#{$this{valueBinding}.text}"
        >
            <!facet image>
                <sun:iconHyperlink id="abc" icon="TREE_FOLDER" url="/common/applications/applications.jsf" alt="$resource{i18n.tree.applications}"
                    border="$int{0}" immediate="$boolean{true}" />
            </facet>
            <!filterTree filterOutLifecycle() />
        </dynamicTreeNode>


        <dynamicTreeNode id="lifecycle"
            treeAdaptorClass="org.glassfish.admingui.common.tree.MBeanTreeAdaptor"
            objectName="amx:pp=/domain,type=applications"
            text="$resource{i18n.tree.lifecycle}"
            url="/common/applications/lifecycles.jsf"
            expanded="$boolean{false}"
            attributeName="name"
            childImageURL="/resource/images/webModule.gif"
            childExpanded="$boolean{false}"
            childURL="/common/applications/lifecycleEdit.jsf?appName=#{$this{valueBinding}.text}"
        >
            <!facet image>
                <sun:iconHyperlink id="abc" icon="TREE_FOLDER" url="/common/applications/lifecycles.jsf"
                    alt="$resource{i18n.tree.lifecycle}" border="$int{0}" immediate="$boolean{true}" />
            </facet>
            <!filterTree filterOutNonLifecycle() />
        </dynamicTreeNode>


        <!--  ===================================== Resources ======================  -->

        <sun:treeNode  id="resources"
            text="$resource{i18n.tree.resources}"
            url="/common/resourceNode/resources.jsf"
            imageURL="/resource/images/resources.gif"
            expanded="$boolean{true}"
        />

         <!--  ===================================== Configurations ======================  -->

         <sun:treeNode id="configuration"
            text="$resource{i18n.tree.configuration}"
            url="/common/configuration/configuration.jsf?configName=server-config"
            imageURL="/resource/images/configurations.gif"
            expanded="$boolean{true}"
        />
    </sun:tree>
    <event>
	<!afterCreate
	    getUIComponent(clientId="#{pageSession.treeId}"  component=>$attribute{component});
	    includeIntegrations(type="org.glassfish.admingui:navNode" root="#{requestScope.component}");
	/>
    </event>
  </sun:form>

//</sun:body>

<!-- We only want to set the width for IE browser only. -->
<f:verbatim>
<script type="text/javascript">
function setTreeWidth(treeId) {
    var version = navigator.appVersion;
    var ix = version.indexOf('MSIE');
    if (ix == -1){
        return;
    }
    var foo = document.getElementById(treeId);
    foo.style.width="40em";
}
</script>
</f:verbatim>
