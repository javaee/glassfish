<sun:panelGroup id="ucPanel" separator="">
    <sun:imageHyperlink id="imgHlink" url="#{request.contextPath}/updateCenter/update.jsf"
	    style="text-decoration: none"   
	    alt="#{sessionScope.linkMsg}" 
	    toolTip="#{sessionScope.linkMsg}"
	    imageURL="#{request.contextPath}/resource/updateCenter/images/updateCenter.png" 
	    text="#{sessionScope.linkMsg}"
	    style="color:white; margin-left: 5px; text-decoration: none;"
	    rendered="#{sessionScope.showUpdateMsg}">
	<ui:event type="beforeCreate">
	    if ("#{showUpdateMsg}=#{null}") {
		getUpdateComponentCount(count="#{requestScope.numUpdatesAvailable}");
		setSessionAttribute(key="showUpdateMsg", value="#{numUpdatesAvailable != 0}");
		setResourceBundle(key="uc" bundle="org.glassfish.updatecenter.admingui.Strings");
		setSessionAttribute(key="linkMsg" value="$resource{uc.msg.checkingForUpdates}");
		if ("#{numUpdatesAvailable} > 0") {
		    setSessionAttribute(key="linkMsg" value="$resource{uc.msg.updatesAvailable}");
		}
		if ("#{numUpdatesAvailable} = 0") {
		    setSessionAttribute(key="linkMsg" value="$resource{uc.msg.noUpdates}");
		}
	    }
	</ui:event>
    </sun:imageHyperlink>
</sun:panelGroup>
