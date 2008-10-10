<!initPage
    setResourceBundle(key="theme" bundle="org.glassfish.admingui.customtheme.Strings");
/>
<sun:versionPage id="version"
    productImageURL="/resource/images/VersionProductName.png"
    productImageDescription="$resource{theme.versionImage.description}"
    versionString="$pageSession{fullVersion}"
    copyrightString="$resource{i18n.copyright.shortMessage}"
/>