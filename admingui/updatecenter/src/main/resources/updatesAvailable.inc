<!initPage
    setResourceBundle(key="uc" bundle="org.glassfish.updatecenter.admingui.Strings");
    getUpdateComponentCount(count=>$attribute{numUpdatesAvailable});
    setAttribute(key="updatesAvailable", value="#{numUpdatesAvailable > 0}");
/>
"<span style="margin: 5px 5px 0px 5px">
    <sun:image id="updatesAvailableImage" rendered="#{updatesAvailable}"
        alt="$resource{uc.msg.updatesAvailable}" icon="ALERT_INFO_MEDIUM"/>
    <sun:hyperlink id="updatesAvailableLink" rendered="#{updatesAvailable}"
        style="color:white; margin-left: 5px;"
        text="$resource{uc.msg.updatesAvailable}"
        target="main"
        url="#{request.contextPath}/updateCenter/update.jsf">
    </sun:hyperlink>
"</span>
<f:verbatim>
<script type="text/javascript">
if (#{updatesAvailable}) {
    showLargeHeaderFrame(true);
}
</script>
</f:verbatim>
