<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse / ForEach</title>
</head>

<body bgcolor="#FFFFFF">

<h3>&lt;x:parse&gt; / &lt;x:forEach&gt;</h3>

<c:import var="docString" url="ForEachDoc.xml"/>

<x:parse var="document" doc="${docString}"/>

<table border=1>
  <tr>
    <td valign="top"><pre><c:out value="${docString}"/></pre></td>
    <td valign="top">
      <table border=1>
        <tr>
          <th>Expression</th>
          <th>Result</th>
        </tr>
        <tr>
          <td><pre>
&lt;x:forEach select="$document//a" varStatus="status">
  ${status.index}:  &lt;x:out select="."/> &lt;br>
&lt;x:forEach>
</pre>
          </td>
          <td>
<x:forEach select="$document//a" varStatus="status">
  ${status.index}:  <x:out select="."/>
  <br />
</x:forEach>
</td>
        </tr>

        <tr>
          <td><pre>
&lt;x:forEach select="$document//a" begin="1" end="2" varStatus="status">
  -> &lt;x:out select="."/> &lt;br>
&lt;x:forEach>
</pre>
          </td>
          <td>
<x:forEach select="$document//a" begin="1" end="2" varStatus="status">
  ${status.index}: <x:out select="."/>
  <br />
</x:forEach>
</td>
        </tr>
        
        <tr>
          <td><pre>
&lt;x:forEach select="$document//a" varStatus="status">
  ${status.index}:  
  &lt;x:if select=".//d">
    &lt;d&gt; element present
  &lt;x:if>
  &lt;br>
&lt;x:forEach>
</pre>
          </td>
          <td>
<x:forEach select="$document//a" varStatus="status">
  ${status.index}:  
  <x:if select=".//d">
    &lt;d&gt; element present
  </x:if>
  <br />
</x:forEach>
</td>
        </tr>      
      </table>
    </td>
  </tr>
</table>

</body>
</html>
