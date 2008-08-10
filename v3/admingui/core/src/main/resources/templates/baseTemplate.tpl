<sun:page>
    <!beforeCreate 
        //setResourceBundle(key="i18n" bundle="core.Strings")
        //setResourceBundle(key="help" bundle="#{helpBundle}")
        compare(obj1="$pageSession{configName}" obj2="server-config" objEqual=>$attribute{isServerConfig});
        setPageSessionAttribute(key="showIt" value="$boolean{false}");
        if ($session{supportCluster} & !${isServerConfig}){
            setPageSessionAttribute(key="showIt" value="$boolean{true}");
        }
    />
<!-- #include "/shared/restart.inc" -->
<!--
<event>
    <!beforeEncode
        if(! $session{supportCluster}){
            checkRestart(RestartRequired=>$attribute{restartRequired});
        }
        if($session{supportCluster}){
            setAttribute(key="restartRequired" value="$boolean{false}");
        }
    />
</event>
-->

<sun:html>
    <!insert name="head">
	<sun:head title="#{pageTitle}">
            <!insert name="headExtra" />
	</sun:head>
    </insert>
    <sun:body onLoad="javascript: synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}')">

    <sun:form id="form"> 
        "<div id="breadcrumbs" style="border: 1px solid #E5E9ED; background-color: #E5E9ED">
        <!-- Make sure we have the scripts loaded -->
        <sun:script url="/resource/js/adminjsf.js" />
        <!-- start the breadcrumbs -->
        <sun:breadcrumbs id="boom" pages="#{pageSession.breadCrumbs}"style="background-color: #E5E9ED">
            <!beforeCreate
                dummyHyperlinkArray(links=>$pageSession{breadCrumbs});
            />
            <!afterCreate
            setPageSessionAttribute(key="boom" value="$this{clientId}");
                setPageSessionAttribute(key="hasPageURL", value="#{false}");
                if ("#{pageSession.pageURLForBreadCrumb}"){
                    setPageSessionAttribute(key="pageURL", value="#{pageSession.pageURLForBreadCrumb}");
                    setPageSessionAttribute(key="hasPageURL", value="#{true}");
                }
                if ("!#{pageSession.hasPageURL}"){
                    setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.request.queryString}");
                    if ("#{pageSession.pageURL}") {
                        setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.requestServletPath}#{facesContext.externalContext.requestPathInfo}?#{pageSession.pageURL}");
                    }
                    if ("!#{pageSession.pageURL}") {
                        setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.requestServletPath}#{facesContext.externalContext.requestPathInfo}");
                    }
                }
            />
        </sun:breadcrumbs>
        
        <!-- Make an invisible button to handle the Ajax action -->
        <sun:button id="button" visible="#{false}">
            <!afterCreate
            setPageSessionAttribute(key="ajaxButton" value="$this{clientId}");
            />
            <!command
            createHyperlinkArray(links=>$pageSession{breadCrumbs});
            />
        </sun:button>
        
        <!-- Fire this during the onload of the page -->
        <f:verbatim>
        <script type="text/javascript">
            var myonload = new Object();
            myonload.oldonload = window.onload;
            myonload.newonload = function() {
            if ('#{pageSession.pageURL}' != '') {
                admingui.nav.selectTreeNodeWithURL('#{pageSession.pageURL}');
            }
            admingui.nav.calculateBreadCrumbs(document.getElementById('#{pageSession.ajaxButton}'), '#{pageSession.boom}', 0);
            if (myonload.oldonload) {
                myonload.oldonload();
            }
            };
            window.onload = myonload.newonload;
        </script>
        </f:verbatim>
        
        "</div>
        <sun:title title="#{pageTitle}" helpText="#{helpText}">
            <!insert name="titleExtra" />
            <!if #{loadDefaultsHandlers}>
            <sun:button id="loadDefaults" style="margin-left: 8pt" primary="#{false}" text="$resource{i18n.button.LoadDefaults}" >    
                <!command
                    #{loadDefaultsHandlers}
                />
            </sun:button>
            </if>

            <!-- Buttons  -->
            <!if #{pageButtonsTopHandlers}>
            <!facet pageButtonsTop>
            <sun:panelGroup id="topButtons">
                <sun:button id="saveButton" text="$resource{i18n.button.Save}" >
                    <!command
                        #{pageButtonsTopHandlers}
                    />
                </sun:button>                      
            </sun:panelGroup>
            </facet>
            </if>
        </sun:title>

	<!insert name="content">
	    "Content Goes Here
	</insert>

	<!insert name="helpkey" />
    </sun:form>
    </sun:body>
</sun:html>
</sun:page>