
<f:verbatim>
    <script type="text/javascript">

 function uploadInit() {
        setVisible('#{appType}');
        //We need to set a timeout to delay the call to getTextElement inside disable component.
        //otherwise getTextElement will always return null, causing JS error.
        //disableComponent('#{dirPathId}', 'text');
        window.setTimeout("disableComponent('#{dirPathId}', 'text')", 1);
        synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}');
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
}

function fileChooseAction() {
    enableDOMComponent('#{dirPathId}');
    enableBtnComponent('#{dirSelectBtnId}');
    enableBtnComponent('#{filSelectBtnId}');
    disableComponent('#{fileuploadId}', 'file');
}

</script>
</f:verbatim>
