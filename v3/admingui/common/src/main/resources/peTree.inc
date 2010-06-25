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

// NOTE: Do not include anything outside the sun:form component... it will
// NOTE: not get used on tree refresh.

//<sun:body  onLoad="fixTree(); setTreeWidth('#{treeId}');" >
  <sun:form id="treeForm">
    <!beforeCreate
	#getConfigNameOfTarget(target="server" configName=>$attribute{configName});
	// FIXME: "server-config" should not be hard-coded, it should be
	// FIXME: the 0th element of this config list
	setAttribute(key="configName" value="server-config");
    />
    <h:commandButton id="update" style="display: none;">
	<!command
            setResourceBundle(key="i18n" bundle="org.glassfish.admingui.core.Strings");

	    // Must use entire treeForm so that all IntegrationPoints are considered
	    getUIComponent(clientId="treeForm", component="#{requestScope.oldComp}");
	    setAttribute(key="parent" value="#{requestScope.oldComp.parent}");

	    // Replace it with something temporary to hold position
	    // Needed, or it it will just find the old one vs. create it
	    createComponent(type="staticText" component=>$attribute{temp});
	    replaceUIComponent(old="$attribute{oldComp}" new="$attribute{temp}");

	    // Rengenerate entire tree (must create everything to be complete)
	    getLayoutComponent(
		viewId="/common/peTree.inc"
		clientId="treeForm"
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
            expanded="false"
            attributeName="name"
            childImageURL="/resource/images/webModule.gif"
            childExpanded="false"
            childURL="/common/applications/applicationEdit.jsf?appName=#{$this{valueBinding}.text}"
        >
            <!facet image>
                <sun:iconHyperlink id="abc" icon="TREE_FOLDER" url="/common/applications/applications.jsf" alt="$resource{i18n.tree.applications}"
                    border="$int{0}" immediate="true" />
            </facet>
            <!filterTree filterOutLifecycle() />
        </dynamicTreeNode>


        <dynamicTreeNode id="lifecycle"
            treeAdaptorClass="org.glassfish.admingui.common.tree.MBeanTreeAdaptor"
            objectName="amx:pp=/domain,type=applications"
            text="$resource{i18n.tree.lifecycle}"
            url="/common/applications/lifecycles.jsf"
            expanded="false"
            attributeName="name"
            childImageURL="/resource/images/webModule.gif"
            childExpanded="false"
            childURL="/common/applications/lifecycleEdit.jsf?appName=#{$this{valueBinding}.text}"
        >
            <!facet image>
                <sun:iconHyperlink id="abc" icon="TREE_FOLDER" url="/common/applications/lifecycles.jsf"
                    alt="$resource{i18n.tree.lifecycle}" border="$int{0}" immediate="true" />
            </facet>
            <!filterTree filterOutNonLifecycle() />
        </dynamicTreeNode>


        <!--  ===================================== Resources ======================  -->

        <sun:treeNode  id="resources"
            text="$resource{i18n.tree.resources}"
            url="/common/resourceNode/resources.jsf"
            imageURL="/resource/images/resources.gif"
            expanded="true"
        />

        <!--  ===================================== Clusters ======================  -->

        <sun:treeNode  id="clusters"
            text="$resource{i18n.tree.clusters}"
            url="/common/cluster/clusters.jsf"
            imageURL="/resource/images/cluster.gif"
            expanded="true"
        />

        <!--  ===================================== Standalone Instances ======================  -->

        <sun:treeNode  id="standaloneInstances"
            text="$resource{i18n.tree.standaloneInstances}"
            url="/common/appServer/standaloneInstances.jsf"
            imageURL="/resource/images/instance.gif"
            expanded="true"
        >
        </sun:treeNode>
        <!--  ===================================== Nodes ======================  -->

        <sun:treeNode  id="nodes"
            text="$resource{i18nc.tree.nodes}"
            url="/common/node/nodes.jsf"
            imageURL="/resource/images/nodes.gif"
            expanded="true"
        />

         <!--  ===================================== Configurations ======================  -->
        <sun:treeNode id="configurations"
		text="$resource{i18n.tree.configurations}"
// FIXME: Should be configurations.jsf
		url="/common/configuration/configuration.jsf"
		imageURL="/resource/images/configurations.gif"
		expanded="true">
	    <!afterCreate
		setAttribute(key="configsTreeNode" value="$this{component}");
	    />
	    <sun:treeNode id="${configName}" 
		    text="${configName}"
		    url="/configuration/configuration.jsf?configName=${configName}"
		    imageURL="/resource/common/images/configurations.gif"
		    expanded="false">
		<!afterCreate
		    setAttribute(key="configTreeNodeLE" value="$this{layoutElement}");
		/>
	    </sun:treeNode>
	</sun:treeNode>
    </sun:tree>
    <event>
	<!afterCreate
	    includeIntegrations(type="org.glassfish.admingui:navNode" root="#{pageSession.treeId}");
// FIXME: JSFT 2.0.3 is needed for this loop... we also need to pull the actual config list instead of hard-coding it!!
	    //foreach(var="configName" list={"default-config" "my-config"}) {
	    foreach(var="configName" list={}) {
		buildUIComponentTree(layoutElement="$attribute{configTreeNodeLE}" parent="$attribute{configsTreeNode}");
		includeIntegrations(type="org.glassfish.admingui:navNode" root="#{pageSession.treeId}");
	    }
	/>
    </event>

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
  </sun:form>

//</sun:body>
