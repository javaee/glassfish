<sun:masthead id="Masthead" productImageURL="#{request.contextPath}/resource/customTheme/images/masthead-product_name_open1.png" 
              style="border-width: 0px"
              productImageDescription="Glassfish v3 Administration Console"
              productImageWidth="324"
              productImageHeight="23"
              userInfo="#{userName}"  userInfoLabel="$resource{i18n.masthead.userInfoLabel}"
              roleInfo="#{domainName}" roleInfoLabel="$resource{i18n.masthead.domainInfoLabel}"
              serverInfo="#{serverName}" serverInfoLabel="$resource{i18n.masthead.serverInfoLabel}"
              dateTime="$boolean{false}" notificationMsg="$attribute{null}">
    <!facet consoleLink>
        <sun:hyperlink id="homeLink" toolTip="$resource{i18n.homeLinkTooltip}" target="_top" text="$resource{i18n.masthead.Home}" url="/index.jsf" />
    </facet>
    <!facet versionLink>
        <sun:hyperlink id="versionLink" toolTip="$resource{i18n.versionTooltip}" text="$resource{i18n.masthead.Version}" onClick="javascript: var versionWin = window.open('version.jsf','VersionWindow','scrollbars,resizable,width=700,height=500,top='+((screen.height - (screen.height/1.618)) - (500/2))+',left='+((screen.width-650)/2) ); versionWin.focus(); return false;" onKeyPress="javascript: return true;" />
    </facet>
    <!facet helpLink>
        <sun:helpWindow id="helpLink" windowTitle="$resource{i18n.helpWindowTitle}" text="$resource{i18n.masthead.Help}" toolTip="$resource{i18n.helpWindowTooltip}" helpFile="CONTEXT_HELP.html" onClick="doHelp(this); return false; " onKeyPress="doHelp(this); return false; " />
    </facet>
    <!facet logoutLink>
        <sun:button id="logoutLink" rendered="#{!anonLogin}" toolTip="$resource{i18n.logoutTooltip}" target="_top" text="$resource{i18n.masthead.Logout}"
            onClick="if (!confirm('$resource{i18n.msg.JS.confirmLogout}')) return false;" onKeyPress="javascript: return true;" url="/" >
                <!command
                    logout();
                    redirect("#{request.contextPath}/index.jsf");
                />
        </sun:button>
    </facet>
    <!facet statusArea>
        <sun:panelGroup id="statusPanel" separator="">
            "<span style="margin: 5px 5px 0px 5px">
                <sun:image id="alertImage" rendered="$session{restartRequired}" alt="$resource{i18n.masthead.restartRequired}" icon="ALERT_WARNING_MEDIUM"/>
                <sun:hyperlink id="restartLink" rendered="$session{restartRequired}"
                               style="color:white; margin-left: 5px;"
                               text="$resource{i18n.masthead.restartRequired}"
                               toolTip="$resource{i18n.masthead.restartStatusMsg}"
                               onMouseOver="window.status='$resource{i18n.masthead.restartStatusMsg}'; return true"
                               onMouseOut="window.status=''; return true"
                               onBlur="window.status=''; return true"
                               onFocus="window.status='$resource{i18n.masthead.restartStatusMsg}'; return true"
                               url="#{request.contextPath}/restart.jsf">
                </sun:hyperlink>
            "</span>
            <!afterCreate
            getClientId(component="$this{component}" clientId=>$attribute{panelGroupId})
            getUIComponent(clientId="#{panelGroupId}"  component=>$attribute{pg})
            includeIntegrations(type="org.glassfish.admingui:mastheadStatusArea" root="#{pg}");
            />
        </sun:panelGroup>
    </facet>
</sun:masthead>
