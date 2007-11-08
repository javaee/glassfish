<%@ taglib uri="/WEB-INF/tlds/t12.tld" prefix="t" %>

<t:test lit="#{abc}" expr="#{xyz}"/>
<t:test lit="\#{abc}" expr="\#{xyz}"/>
<t:test lit="\abc" expr="\xyz"/>
