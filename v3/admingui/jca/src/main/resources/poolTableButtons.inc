<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command 
                setSessionAttribute(key="valueMap" value="#{null}")
                setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                gf.redirect(page="#{pageSession.createLink}") 
            />
        </sun:button>

        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{tableableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            if ( #{pageSession.jcapool}) {
                deleteCascade(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}",  dependentType="#{pageSession.dependentType}", selectedRows="${selectedRows}");
            }
            if (! #{pageSession.jcapool}) {
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
            } 
            setAttribute(key="selectedRows" value="${selectedRows}");
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
     </sun:panelGroup>
 </facet>
