<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command gf.redirect(page="#{pageSession.createLink}") />
        </sun:button>

        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
            onClick="if (getConfirm(this,'$pageSession{confirmDeleteMsg}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            gf.deleteCascade(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}", selectedRows="${selectedRows}", id="jndiName");
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}" >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            // enable the resource for all targets
            foreach (var="row" list="#{requestScope.selectedRows}") {
                gf.createAttributeMap(keys={"enabled"} values={"true"} map="#{requestScope.attrMap}");
                urlencode(value="#{row['name']}" result="#{requestScope.encodedResName}");
                setAttribute(key="targetUrlsList" value="#{row['targetUrls']}");
                foreach (var="targetUrl" list="#{requestScope.targetUrlsList}") {
                    gf.restRequest(
                        endpoint="#{requestScope.targetUrl}/resource-ref/#{requestScope.encodedResName}"
                        attrs="#{requestScope.attrMap}"
                        method="POST"
                        result="#{requestScope.foo}");
                }
            }
            setAttribute(key="selectedRows" value="${selectedRows}");
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}" >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            // disable the resource for all targets
            foreach (var="row" list="#{requestScope.selectedRows}") {
                gf.createAttributeMap(keys={"enabled"} values={"false"} map="#{requestScope.attrMap}");
                urlencode(value="#{row['name']}" result="#{requestScope.encodedResName}");
                setAttribute(key="targetUrlsList" value="#{row['targetUrls']}");
                foreach (var="targetUrl" list="#{requestScope.targetUrlsList}") {
                    gf.restRequest(
                        endpoint="#{requestScope.targetUrl}/resource-ref/#{requestScope.encodedResName}"
                        attrs="#{requestScope.attrMap}"
                        method="POST"
                        result="#{requestScope.foo}");
                }
            }
            setAttribute(key="selectedRows" value="${selectedRows}");
            gf.redirect(page="#{pageSession.listLink}");
        />
        </sun:button>
     </sun:panelGroup>
 </facet>
