<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse / Out</title>
</head>

<body bgcolor="#FFFFFF">

<h3>&lt;x:parse&gt; / &lt;x:out&gt;</h3>

<c:import var="docString" url="games.xml"/>

<x:parse var="doc" doc="${docString}"/>

<table border=1>
  <tr>
    <td valign="top"><pre><c:out value="${docString}"/></pre></td>
    <td valign="top">
      <table border=1>
        <tr>
          <th>Expression</th>
          <th>Result</th>
        </tr>
<%--
        <tr>
          <td>3 + 3</td>
          <td><pre><x:out select="3 + 3"/></pre></td>
        </tr>
--%>
        <tr>
          <td>$doc//sport</td>
          <td><pre><x:out select="$doc//sport"/></pre></td>
        </tr>
        <tr>
          <td>$doc/games/country/*</td>
          <td><pre><x:out select="$doc/games/country/*"/></pre></td>
        </tr>
        <tr>
          <td>$doc//*</td>
          <td><pre><x:out select="$doc//*"/></pre></td>
        </tr>
        <tr>
          <td>$doc/games/country</td>
          <td><pre><x:out select="$doc/games/country"/></pre></td>
        </tr>
        <tr>
          <td>$doc/games/country[last()]</td>
          <td><pre><x:out select="$doc/games/country[last()]"/></pre></td>
        </tr>
        <tr>
          <td>$doc//@id</td>
          <td><pre><x:out select="$doc//@id"/></pre></td>
        </tr>
        <tr>
          <td>$doc//country[@id='Denmark']</td>
          <td><pre><x:out select="$doc//country[@id='Denmark']"/></pre></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

</body>
</html>
