<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="deleteButton" text="$resource{i18n.button.Delete}" disabled="#{false}" primary="#{false}"
            onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
            { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
            else {return false;} "
        >
            <!command
                getUIComponent(clientId="$pageSession{tableableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="enableButton" text="$resource{i18n.button.Enable}" disabled="#{false}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                updateStatus(objectNameStr="#{pageSession.updateObjectNameStr}", selectedRows="${selectedRows}", enabled="true");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="#{pageSession.listLink}");
            />
        </sun:button>

        <sun:button id="disableButton" text="$resource{i18n.button.Disable}" disabled="#{false}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{tableableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
                updateStatus(objectNameStr="#{pageSession.updateObjectNameStr}", selectedRows="${selectedRows}", enabled="false");
                setAttribute(key="selectedRows" value="${selectedRows}");
                redirect(page="#{pageSession.listLink}");
            />
        </sun:button>
     </sun:panelGroup>
 </facet>