<sun:button id="saveButton" text="$resource{i18n.button.Save}" >
    <!command
        if (#{tableInUse}) {
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{newProps});
            savePropertiesMap(destination="#{amxConfig}", values="#{newProps}");
        }
        updateAmxConfig(moduleConfig="#{amxConfig}", configMap="$pageSession{configMap}");
        if (#{displayFeedback}){
            prepareSuccessfulMsg();
        }
    />
</sun:button>