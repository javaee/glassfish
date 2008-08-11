<!initPage
	setAttribute(key="tableInUse" value="#{true}");
    getTableList(Properties="#{amxConfig.propertyConfigMap}", TableList=>$attribute{tableList});
/>
<!-- Table .... -->
<sun:table id="basicTable"  style="padding: 10pt" title="$resource{i18n.common.AdditionalProperties}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)" >
    <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
/>
<!-- Actions (Top) -->
    <!facet actionsTop>
    <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="addSharedTableButton" disabled="#{false}" text="$resource{i18n.button.AddProperty}" >   
            <!command
                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                addRowToTable(TableRowGroup="$attribute{tableRowGroup}", NameList={"name", "value"});
            />                                 
        </sun:button>
        <sun:button id="button1" text="$resource{i18n.button.DeleteProperties}" disabled="#{true}" primary="#{false}">
            <!command
                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{trg});   
                getSelectedTableRowKeys(tableRowGroup="${trg}" rowKeys=>$attribute{rowKeys});
                deleteTableRows(tableRowGroup="${trg}" rowKeys="${rowKeys}");
                commitTableRowGroup(tableRowGroup="${trg}");  
            />                                
        </sun:button>
    </sun:panelGroup>
    </facet>       
    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{tableList}"}  sourceVar="td">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{propertyTableRowGroupId});
        />
        <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col1">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('initAllRows(); changeOneTableButton()', 0)" 
            />
        </sun:tableColumn>                    
        <sun:tableColumn headerText="$resource{i18n.common.PropertyName}" sort="name" rowHeader="$boolean{false}" id="col2">
            <sun:textField columns="$int{30}" id="col1St" value="#{td.value.name}" />
        </sun:tableColumn>
        <sun:tableColumn headerText="$resource{i18n.common.PropertyValue}" sort="value" rowHeader="$boolean{false}" id="col3">
            <sun:textField columns="$int{55}" id="col1St" value="#{td.value.value}" />
        </sun:tableColumn>
        "<br/>
    </sun:tableRowGroup>
</sun:table>