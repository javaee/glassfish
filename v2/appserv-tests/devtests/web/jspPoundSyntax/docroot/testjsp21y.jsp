<%@ taglib uri="/WEB-INF/tlds/t21.tld" prefix="t" %>

<%-- Should cause an error --%>
<t:test lit="\#{abc}" expr="#{xyz}"/>
