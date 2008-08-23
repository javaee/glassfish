<!initPage
#include "/templates/adminConsoleInit.inc"
    getTableList(Properties="#{amxConfig.propertyConfigMap}", TableList=>$attribute{tableList});
/>
<!composition template="/templates/sheetTableTemplate.tpl">
    <!define name="content">
#include "/templates/sheet.inc"
#include "/templates/table.inc"
    </define>
</composition>