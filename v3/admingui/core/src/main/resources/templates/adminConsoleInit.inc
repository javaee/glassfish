if (!#{pageSession.configName}) {
    getRequestValue(key="configName" value=>$page{configName});
}
if (#{pageSession.configName}) {
    getConfigConfig(configName="#{configName}" configConfig=>$attribute{config}); 
    setAttribute(key="amxConfigName", value="#{amxConfigName}");
    setPageSessionAttribute(key="amxConfig" value="#{config.$attribute{amxConfigName}}");
    // This probably needs to move to table.inc
    if (#{amxConfigAttributes}) {
        if (!#{pageSession.amxConfigAttributes}) {
            setPageSessionAttribute(key="amxConfigAttributes" value="#{amxConfigAttributes}");
        }
        createAmxConfigMap(moduleConfig="#{amxConfig}", properties="#{amxConfigAttributes}", configMap=>$pageSession{configMap});
    }
}
setAttribute(key="i18nBundle", value="core.Strings");