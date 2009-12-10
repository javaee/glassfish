<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />

        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <!insert name="listTableDeleteButton">
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
            { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
            else {return false;} "
        >
        <!command
            getUIComponent(clientId="$pageSession{tableableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
            setAttribute(key="selectedRows" value="${selectedRows}");
            if (#{pageSession.additionalDeleteHandler}){
                gf.invokeHandler(handler="#{pageSession.additionalDeleteHandler}", args="#{pageSession.additionalDeleteHandlerArgs}" );
            }
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
        </insert>

     </sun:panelGroup>
 </facet>
