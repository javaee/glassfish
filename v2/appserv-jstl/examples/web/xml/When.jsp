<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse / When</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse / When</h3>

<x:parse var="a">
  <a>
   <b>
    <c foo="bar">
     foo
    </c>
   </b>
   <d>
     bar
   </d>
  </a>
</x:parse>

<x:choose>
  <x:when select='$a//c[@foo="bar"]'>
    @foo = bar
  </x:when>
  <x:when select='$a//c[@foo="foo"]'>
    @foo = foo
  </x:when>
  <x:otherwise>
    @foo not recognized
  </x:otherwise>
</x:choose>

<br />

<x:choose>
  <x:when select='$a//c[@foo="foo"]'>
    @foo = foo
  </x:when>
  <x:when select='$a//c[@foo="bar"]'>
    @foo = bar
  </x:when>
  <x:otherwise>
    @foo not recognized
  </x:otherwise>
</x:choose>

<br />

<x:choose>
  <x:when select='$a//c[@foo="barr"]'>
    @foo = barr
  </x:when>
  <x:when select='$a//c[@foo="fooo"]'>
    @foo = fooo
  </x:when>
  <x:otherwise>
    @foo not recognized
  </x:otherwise>
</x:choose>

<br />

<hr />

</body>
</html>
