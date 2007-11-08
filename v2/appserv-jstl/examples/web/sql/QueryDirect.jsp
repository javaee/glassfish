<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: SQL action examples</title>
</head>
<body bgcolor="#FFFFFF">

<h1>SQL Direct Query Execution</h1>
<p>This example demonstrates how the row and columns can be directly accessed using various direct mechanisms.<p>


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

<h2>Using the Row index and Column name</h2>
Row[0].NAMEID: <c:out value="${deejays.rows[0].NAMEID}" />
<br>
Row[0].NAME: <c:out value="${deejays.rows[0].NAME}" />
<br>
Row[1].NAMEID: <c:out value="${deejays.rows[1].NAMEID}" />
<br>
Row[1].NAME: <c:out value="${deejays.rows[1].NAME}" />
<br>
Row[2].NAMEID: <c:out value="${deejays.rows[2].NAMEID}" />
<br>
Row[2].NAME: <c:out value="${deejays.rows[2].NAME}" />
<br>

<hr>

<h2>Using the Row and Column index</h2>
Row[0][0]: <c:out value="${deejays.rowsByIndex[0][0]}" />
<br>
Row[0][1]: <c:out value="${deejays.rowsByIndex[0][1]}" />
<br>
Row[1][0]: <c:out value="${deejays.rowsByIndex[1][0]}" />
<br>
Row[1][1]: <c:out value="${deejays.rowsByIndex[1][1]}" />
<br>
Row[2][0]: <c:out value="${deejays.rowsByIndex[2][0]}" />
<br>
Row[2][1]: <c:out value="${deejays.rowsByIndex[2][1]}" />
<br>

<sql:update var="newTable" dataSource="${example}">
  drop table mytable
</sql:update>

</body>
</html>
