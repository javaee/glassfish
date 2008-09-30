if (!#{pageSession.configName}) {
    getRequestValue(key="configName" value=>$page{configName});
}
if (#{pageSession.configName}) {
    getConfigConfig(configName="#{configName}" configConfig=>$attribute{config}); 
    setAttribute(key="amxConfigName", value="#{amxConfigName}");
    setPageSessionAttribute(key="amxConfig" value="#{config.$attribute{amxConfigName}}");
    // This probably needs to move to propertyTable.inc
    if (#{amxConfigAttributes}) {
        if (!#{pageSession.amxConfigAttributes}) {
            setPageSessionAttribute(key="amxConfigAttributes" value="#{amxConfigAttributes}");
        }
        if (!#{configMap}) {
            createAmxConfigMap(moduleConfig="#{amxConfig}", properties="#{amxConfigAttributes}", configMap=>$pageSession{configMap});
        }
    }
}
setResourceBundle(key="help_web" bundle="org.glassfish.web.admingui.Helplinks");