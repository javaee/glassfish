function updateUI() {
    function getDisplay(elem) {
        return document.getElementById(elem).style.display;
    }

    function setDisplay(elem, value) {
        return document.getElementById(elem).style.display = value;
    }


    var sheet = 'propertyForm:jmsPropertySheet';
    var jmsBasicConfig = document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optBasic').checked;
    var jmsCustomConfig = document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optCustom').checked;
    if (!(jmsBasicConfig || jmsCustomConfig)) {
        document.getElementById('propertyForm:propertySheet:propertySectionTextField:jmsConfigTypeProp:optBasic').checked = true;
        jmsBasicConfig = true;
    }
    var baseId = sheet + ':configureJmsClusterSection';
    var clusterType = document.getElementById(baseId+':ClusterTypeProp:clusterType').value;
    var messageStoreType = document.getElementById(baseId+':MessageStoreTypeProp:messageStoreType').value;
    var pwdSel = document.getElementById(baseId+':PswdSelProp:pwdSel').value;

    if (jmsBasicConfig) {
        setDisplay(sheet, 'none');
    } else {
        setDisplay(sheet, 'block');
    }

    if (clusterType == 'enhanced') {
        setDisplay(baseId+':jmsTypeProp:optEmbedded_span', 'none');
        setDisplay(baseId+':ConfigStoreTypeProp', 'none');
        setDisplay(baseId+':MessageStoreTypeProp', 'none');
        if (document.getElementById(baseId + ':jmsTypeProp:optEmbedded').checked) {
            document.getElementById(baseId + ':jmsTypeProp:optLocal').checked = true;
        }
    } else {
        setDisplay(baseId+':jmsTypeProp:optEmbedded_span', 'inline');
        setDisplay(baseId+':ConfigStoreTypeProp', 'table-row');
        setDisplay(baseId+':MessageStoreTypeProp', 'table-row');
    }

    if ((messageStoreType == 'file') && (getDisplay(baseId+':MessageStoreTypeProp') != 'none')) {
        setDisplay(baseId+':DbVendorProp', 'none');
        setDisplay(baseId+':DbUserProp', 'none');
        setDisplay(baseId+':DbUrlProp', 'none');
        setDisplay(baseId+':PswdSelProp', 'none');
        setDisplay(baseId+':PswdTextProp', 'none');
        setDisplay(baseId+':PswdAliasProp', 'none');
    } else {
        setDisplay(baseId+':DbVendorProp', 'table-row');
        setDisplay(baseId+':DbUserProp', 'table-row');
        setDisplay(baseId+':DbUrlProp', 'table-row');
        setDisplay(baseId+':PswdSelProp', 'table-row');
        setDisplay(baseId+':PswdTextProp', 'table-row');
        setDisplay(baseId+':PswdAliasProp', 'table-row');
        
        if (pwdSel == 'password') {
            setDisplay(baseId+':PswdTextProp', 'table-row');
            setDisplay(baseId+':PswdAliasProp', 'none');
        } else {
            setDisplay(baseId+':PswdTextProp', 'none');
            setDisplay(baseId+':PswdAliasProp', 'table-row');
        }
    }

}