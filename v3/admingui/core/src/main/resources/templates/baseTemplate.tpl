<sun:page>
// commenting this out since there is not support for this yet in V3
  <!beforeCreate 
//      setResourceBundle(key="i18n" bundle="com.sun.enterprise.tools.admingui.resources.Strings")
//	setResourceBundle(key="help" bundle="com.sun.enterprise.tools.admingui.resources.Helplinks")
    />
// commenting this out since there is not support for this yet in V3
// #include "shared/restart.inc"

<sun:html>
    <!insert name="head">
	<sun:head title="Default Title" />
    </insert>
    <sun:body onLoad="javascript: synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}')">
    <sun:form id="form"> 
// commenting this out since there is not support for this yet in V3
// #include treeBreadcrumbs.inc
	<!insert name="pageTitle">
            <sun:title title="Default Page Title" />
	</insert>
	<!insert name="content">
	    "Content Goes Here
	</insert>
	<!insert name="helpkey" />
    </sun:form>
    </sun:body>
</sun:html>
</sun:page>