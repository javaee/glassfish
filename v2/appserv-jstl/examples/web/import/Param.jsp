<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  --   These URLs likely don't exist, but the error that gets reported 
  --   back should help you see how <param> modifies the URL.
  --
  --%>

<c:import url="http://localhost/foo" charEncoding="foo">
  <c:param name="a" value="b"/>
  <c:param name="c" value="d"/>
  <c:param name="equals" value="="/>                      <%-- encoded --%>
</c:import>
