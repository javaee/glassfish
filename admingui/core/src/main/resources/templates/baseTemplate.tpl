<!initPage
    if (#{i18nBundle}) {
        setResourceBundle(key="i18n" bundle="#{i18nBundle}")
    }
    if (#{helpBundle}) {
        setResourceBundle(key="help" bundle="#{helpBundle}")
    }
/>
<sun:page>
#include "/shared/restart.inc"

<sun:html>
<!insert name="head">
    <sun:script url="/resource/js/adminjsf.js" />
    <sun:head title="#{pageTitle}">
        <!insert name="headExtra" />
    </sun:head>
</insert>
    <sun:body onLoad="#{bodyOnLoad}">
        <sun:form id="form">
            <!insert name="formExtra" />
            <sun:title id="topTitle" title="#{pageTitle}" helpText="#{helpText}">
                <!insert name="titleExtra" />

                <!-- Buttons  -->
                <!facet pageButtonsTop>
                    <sun:panelGroup id="topButtons">
                        <!insert name="pageButtonsTop" />
                    </sun:panelGroup> 
                </facet>
            </sun:title>

            <!insert name="content">
                "Content Goes Here
            </insert>

            <sun:title id="bottomTitle" rendered="#{useBottomTitle == 'true'}">
                <!-- Buttons  -->
                <!facet pageButtonsBottom>
                    <sun:panelGroup id="bottomButtons">
                        <!insert name="pageButtonsBottom" />
                    </sun:panelGroup>
                </facet>
            </sun:title>

            //<!insert name="helpkey" />
            <sun:hidden id="helpKey" value="#{helpKey}" />
        </sun:form>
    </sun:body>
    <!insert name="htmlExtra" />
</sun:html>
</sun:page>