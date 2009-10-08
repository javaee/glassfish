#<!initPage
    #getUpdateComponentCount(count=>$attribute{numUpdatesAvailable});
    #setAttribute(key="showUpdateMsg", value="#{numUpdatesAvailable > 0}");
#/>

<sun:panelGroup id="ucPanel" separator="">
    <sun:imageHyperlink id="imgHlink" url="#{request.contextPath}/updateCenter/update.jsf"
        style="text-decoration: none"   
        alt="#{pageSession.linkMsg}" 
	toolTip="#{pageSession.linkMsg}"
        #icon="#{pageSession.linkIcon}" 
        imageURL="#{request.contextPath}/resource/updateCenter/images/updateCenter.png" 
        text="#{pageSession.linkMsg}"
        style="color:white; margin-left: 5px; text-decoration: none;"
        rendered="#{pageSession.showUpdateMsg}"
    />
    <!afterCreate
	setPageSessionAttribute(key="showUpdateMsg" value="false");
	setResourceBundle(key="uc" bundle="org.glassfish.updatecenter.admingui.Strings");
	setPageSessionAttribute(key="linkMsg" value="$resource{uc.msg.checkingForUpdates}");
	#setPageSessionAttribute(key="linkIcon" value="ALERT_INFO_MEDIUM");
        setPageSessionAttribute(key="ucLink" value="$this{clientId}");
    />

</sun:panelGroup>

      <!-- Make an invisible button to handle the Ajax action -->
      <sun:button id="button" visible="#{false}">
            <!afterCreate
                setPageSessionAttribute(key="ajaxButton" value="$this{clientId}");
            />
            <!command
                #println("Call updateComponentCount");
                getUpdateComponentCount(count=>$attribute{numUpdatesAvailable});
                setPageSessionAttribute(key="showUpdateMsg", value="#{numUpdatesAvailable != 0}");
		setResourceBundle(key="uc" bundle="org.glassfish.updatecenter.admingui.Strings");
		if ("#{numUpdatesAvailable} > 0") {
		    setPageSessionAttribute(key="linkMsg" value="$resource{uc.msg.updatesAvailable}");
		}
		if ("#{numUpdatesAvailable} = 0") {
		    setPageSessionAttribute(key="linkMsg" value="$resource{uc.msg.noUpdates}");
		}
                #println("numUpdatesAvailable = #{numUpdatesAvailable}");
            />
      </sun:button>
<sun:script>
<f:verbatim>
    var myonload = new Object();
    myonload.oldonload = window.onload;
    myonload.newonload = function() {
	if (myonload.oldonload) {
	    myonload.oldonload();
	}
	admingui.nav.submitAjaxRequest(document.getElementById('#{pageSession.ajaxButton}'), '#{pageSession.ucLink}', 0);
    };
    window.onload = myonload.newonload;

    //if (#{pageSession.showUpdateMsg}) {
    //  showLargeHeaderFrame(true);
    //}
</f:verbatim>
</sun:script>

