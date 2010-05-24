<f:verbatim>
    <script type="text/javascript">
        function enableDisableLazyConnection(la){
    var c = getTextElement(la);
    var selected = c.checked;
    if(selected) {
        #{themeJavascript.JS_PREFIX}.checkbox.setChecked('#{enlistId}', true);
        disableComponent('#{enlistId}', 'text');
    }
    if(!selected) {
        enableComponent('#{enlistId}', 'text'); 
    }
    }
    </script>
</f:verbatim>   