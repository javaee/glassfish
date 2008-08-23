<!initPage
#include "/templates/adminConsoleInit.inc"
    getTableList(Properties="#{amxConfig.propertyConfigMap}", TableList=>$attribute{tableList});
/>
<!composition template="/templates/properyTableTemplate.tpl">
</composition>