<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: SQL action examples</title>
</head>
<body bgcolor="#FFFFFF">

<h1>SQL Query Execution using an iterator</h1>


<!-- NOTE: the sql:setDataSource tag is for prototyping and simple applications. You should really use a DataSource object instead --!>

<sql:setDataSource
  var="example"
  driver="${sessionScope.myDbDriver}"
  url="${sessionScope.myDbUrl}"
  user="${sessionScope.myDbUserName}"
  password="${sessionScope.myDbPassword}"
/>

<sql:transaction dataSource="${example}">

  <sql:update var="newTable">
    create table mytable (
      nameid int primary key,
      name varchar(80)
    )
  </sql:update>

  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (1,'Paul Oakenfold')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (2,'Timo Maas')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (3,'Paul van Dyk')
  </sql:update>


  <sql:query var="deejays">
    SELECT * FROM mytable
  </sql:query>

</sql:transaction>

<hr>

<h2>Iterating over each Row of the result</h2>

<table border="1">
  <c:forEach var="row" items="${deejays.rowsByIndex}">
    <tr>
      <c:forEach var="column" items="${row}">
        <td><c:out value="${column}"/></td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>

<hr>

<h2>Iterating over Columns without knowing the index</h2>

<table border="1">
  <c:forEach var="row" items="${deejays.rows}">
  <tr>
    <td>Name: <c:out value="${row.NAMEID}"/></td>
    <td>Value: <c:out value="${row.NAME}"/></td>
  </tr>
  </c:forEach>
</table>

<hr>

<h2>Putting it all together</h2>

<%-- Easiest example showing how to populate a table --%>
<table border="1">
  <tr>
  <%-- Get the column names for the header of the table --%>
  <c:forEach var="columnName" items="${deejays.columnNames}">
    <th><c:out value="${columnName}"/></th>
  </c:forEach>

  <%-- Get the value of each column while iterating over rows --%>
  <c:forEach var="row" items="${deejays.rowsByIndex}">
    <tr>
      <c:forEach var="column" items="${row}">
        <td><c:out value="${column}"/></td>
      </c:forEach>
  </c:forEach>
</table>


<sql:update var="newTable" dataSource="${example}">
  drop table mytable
</sql:update>


</body>
</html>
