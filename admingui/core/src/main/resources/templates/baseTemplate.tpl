<sun:page>
// commenting this out since there is not support for this yet in V3
  <!beforeCreate 
      setResourceBundle(key="i18n" bundle="core.Strings")
    #setResourceBundle(key="help" bundle="core.Helplinks")
    />
// commenting this out since there is not support for this yet in V3
// #include "shared/restart.inc"

<sun:html>
    <!insert name="head">
// Remove for now
//	<sun:head title="Default Title" />
	<sun:markup tag="head">
	    <sun:markup tag="title">
		<staticText value="jMaki Charting!" />
	    </sun:markup>
	</sun:markup>
    </insert>
// Remove for now
//    <sun:body onLoad="javascript: synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}')">
<sun:markup tag="body">
    <sun:form id="form"> 
// Remove for now
// #include treeBreadcrumbs.inc
	<!insert name="pageTitle">
            <sun:title title="Default Page Title" />
	</insert>
	<!insert name="content">
	    "Content Goes Here
	</insert>
	<!insert name="helpkey" />
    </sun:form>
</sun:markup>
// Remove for now
//    </sun:body>
</sun:html>
</sun:page>
