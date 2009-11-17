<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="deleteConnButton" text="$resource{i18n.button.Delete}" disabled="#{false}" primary="#{false}" rendered="#{isConnPage}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType2}", selectedRows="${selectedRows}" key="JndiName");
                deleteChildren(objectNameStr="amx:pp=/domain/servers,type=server,name=server", type="resource-ref", selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");


                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="deleteDestButton" text="$resource{i18n.button.Delete}" disabled="#{false}" primary="#{false}" rendered="#{!isConnPage}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
                deleteChildren(objectNameStr="amx:pp=/domain/servers,type=server,name=server", type="resource-ref", selectedRows="${selectedRows}");
                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="enableButton" text="$resource{i18n.button.Enable}" disabled="#{false}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                updateStatus(resType="#{pageSession.childType}", selectedRows="${selectedRows}", enabled="true");
                setAttribute(key="selectedRows" value="${selectedRows}");
                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="disableButton" text="$resource{i18n.button.Disable}" disabled="#{false}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                updateStatus(resType="#{pageSession.childType}", selectedRows="${selectedRows}", enabled="false");
                setAttribute(key="selectedRows" value="${selectedRows}");
                gf.redirect(page="#{pageSession.listLink}");
            />
        </sun:button>
     </sun:panelGroup>
 </facet>