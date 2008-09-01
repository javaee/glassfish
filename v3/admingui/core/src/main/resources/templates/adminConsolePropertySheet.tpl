<!initPage
#include "/templates/adminConsoleInit.inc"
setAttribute(key="displayFeedback" value="#{displayFeedback}");
/>
<!composition template="/templates/propertySheetTemplate.tpl">
    <!define name="formExtra">
#include "/templates/adminConsoleForm.inc"
    </define> 
    <!define name="titleExtra">
        <sun:button id="loadDefaults" style="margin-left: 8pt" primary="#{false}" text="$resource{i18n.button.LoadDefaults}" >    
            <!command
                loadDefaultAmxConfigAttributes(amxConfig="#{amxConfig}", configMap="$pageSession{configMap}");
            />
        </sun:button>
    </define>
    <!define name="pageButtonsTop">
#include "/templates/adminConsolePageButtonsTop.inc"
    </define>
</composition>