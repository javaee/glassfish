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
	    if ("#{sessionScope._updateCountCalculated}=#{null}") {
		getUpdateComponentCount(count="#{sessionScope.numUpdatesAvailable}");
		setResourceBundle(key="uc" bundle="org.glassfish.updatecenter.admingui.Strings");
                setSessionAttribute(key="linkMsg" value="$resource{uc.msg.checkForUpdates}");
                setSessionAttribute(key="showUpdateMsg", value="#{true}")
		if ("#{sessionScope.numUpdatesAvailable} > 0") {
		    setSessionAttribute(key="linkMsg" value="$resource{uc.msg.updatesAvailable}");
		}
		if ("#{sessionScope.numUpdatesAvailable} = 0") {
		    setSessionAttribute(key="linkMsg" value="$resource{uc.msg.noUpdates}");
                    setSessionAttribute(key="showUpdateMsg", value="#{false}")
		}
                setSessionAttribute(key="_updateCountCalculated", value="true");
	    }
	</ui:event>
    </sun:imageHyperlink>
</sun:panelGroup>
