<!initPage
#include "/templates/adminConsoleInit.inc"
    getTableList(Properties="#{amxConfig.propertyConfigMap}", TableList=>$attribute{tableList});
/>
<!composition template="/templates/propSheetPropTableTemplate.tpl">
    <!define name="content">
#include "/templates/sheet.inc"
#include "/templates/propertyTable.inc"
    </define>
</composition>