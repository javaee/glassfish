<%@ taglib uri="/WEB-INF/tlds/t21.tld" prefix="t" %>
<%@ page deferredSyntaxAllowedAsLiteral="true" %>

<% pageContext.setAttribute("foo", "FooValue"); %>
<t:test lit="\#{abc}" expr="\#{xyz}" deferred="\#{foo}"/>
<t:test lit="#{abc}" expr="#{xyz}" deferred="#{foo}"/>

