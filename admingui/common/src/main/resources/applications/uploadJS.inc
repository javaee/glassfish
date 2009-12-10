
<f:verbatim>
<script type="text/javascript">

function uploadInit() {
        //We need to set a timeout to delay the call to getTextElement inside disable component.
        //otherwise getTextElement will always return null, causing JS error.
        //disableComponent('#{dirPathId}', 'text');
        window.setTimeout("disableComponent('#{dirPathId}', 'text')", 1);
        if(getSelectedValueFromForm(document.forms['form'], 'uploadRdBtn')=='serverSide'){
            enableDOMComponent('#{dirPathId}'); 
            enableBtnComponent('#{dirSelectBtnId}'); 
            enableBtnComponent('#{filSelectBtnId}');
            disableComponent('#{fileuploadId}', 'file'); 
        }
  }
  
function uploadRdBtnAction() {
    //disableDOMComponent('#{dirPathId}');
    window.setTimeout("disableComponent('#{dirPathId}', 'text')", 1);
    disableBtnComponent('#{dirSelectBtnId}');
    disableBtnComponent('#{filSelectBtnId}');
    enableComponent('#{fileuploadId}', 'file');
    comp = getTextElement('#{pageSession.radioChoosenId}');
    comp.value='client';
}

function fileChooseAction() {
    enableDOMComponent('#{dirPathId}');
    enableBtnComponent('#{dirSelectBtnId}');
    enableBtnComponent('#{filSelectBtnId}');
    disableComponent('#{fileuploadId}', 'file');
    comp = getTextElement('#{pageSession.radioChoosenId}');
    comp.value='local';
}

function showPropertySheet(propSheetId, obj, appNameId, contextRootId, appName){
    var cc = null;
    var comp = null;

    var sheets = '#{sessionScope.appTypeString}'.split(',');
    if (propSheetId.length <=0){
        for( ix=0; ix < sheets.length; ix++){
            comp = obj.document.getElementById('form:' + sheets[ix]);
            if (comp != null)
                comp.style.display='none';
        }
    }else{
        for (i=0; i < sheets.length; i++){
            cc = obj.document.getElementById('form:'+sheets[i]);
            if (cc == null){
                continue;
            }
            if (propSheetId == sheets[i]){
                 cc.style.display='block';
            }else{
                cc.style.display='none';
            }
        }
    }

    if (appName != undefined ){
        setAppName(appNameId, appName, obj);
        //may as well set context root if it exist.
        component = obj.getTextElement(contextRootId);
        if (component != null){
            component.value = getPrefix(appName);
        }
     }
}


function setAppName(appNameId, appName, obj){

    var pfex = getPrefix(appName);
    var sfex = getSuffix(appName);

    var sfex2 = sfex.substr(1);   //remove the '.'
    // Fill in application name
    if (appNameId==null || appNameId.length <=0){
        // shouldn't be.
    }else{
        var ix = appNameId.indexOf(":");
        var ix2 = appNameId.substr(ix+1).indexOf(":");
        var str3 = appNameId.substr(ix+1+ix2);
        var sheets = '#{sessionScope.appTypeString}'.split(',');
        for( idx=0; idx < sheets.length; idx++){
            comp = obj.getTextElement('form:'+sheets[idx]+str3);
            if (comp != null){
                comp.value=pfex;
            }
        }
    }
}

function setFieldValue(appNameId, value, dropDownProp, typeId, contextRootId, extensionId, obj) {
    appName = extractName(value);

    var pfex = getPrefix(appName);
    var sfex = getSuffix(appName);
    var sfex2 = '';

    obj.getTextElement(extensionId).value=sfex;
    var appTypes = ','+'#{sessionScope.appTypeString}'+',';

    //If no extension for file choosen, or no plugin for that extension, show dropDown type and don't fill in anything, then return;
    if (sfex != null && sfex.length > 0){
        sfex2 = sfex.substr(1);
        var tests = ','+sfex2+',';
        var inx = appTypes.indexOf(tests) ;
        if (inx == -1){
            sfex2 = '';
        }
    }
    obj.getSelectElement(typeId).value = sfex2;
    obj.document.getElementById(dropDownProp).style.display = 'block';
    showPropertySheet(sfex2, obj, appNameId, contextRootId, appName);
}

function populateDirAndAppName(fileChooserId, dirPathId, appNameId, typeId, dropDownProp, contextRootId, extensionId){
    var fc = document.getElementById(fileChooserId).getSelectionValue();
    window.opener.getTextElement(dirPathId).value = fc;
    //for redeploy, there is no dropdown for app type, there is no need to fill in any field.
    if (dropDownProp != ""){
        setFieldValue(appNameId, fc, dropDownProp, typeId, contextRootId, extensionId, window.opener);
    }
}

function checkFileInputRequired(componentId, reqMsg){
    var component = getFileInputElement(componentId);
    var value = component.value;
    var result = (value != '') && (isWhitespace(value) == false);
    if (result == false) {
        if (reqMsg == '') {
            showAlert(getLabel(component) + ' is a required field.');
        } else {
            showAlert(reqMsg + ' ' + getLabel(component));
        }
        component.select();
        component.focus();
    }
    return result;
}


</script>
</f:verbatim>
