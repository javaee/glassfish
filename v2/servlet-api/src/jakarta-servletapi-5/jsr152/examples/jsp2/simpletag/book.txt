<%@ taglib prefix="my" uri="/WEB-INF/jsp2/jsp2-example-taglib.tld" %>
<html>
  <head>
    <title>JSP 2.0 Examples - Book SimpleTag Handler</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Book SimpleTag Handler</h1>
    <hr>
    <p>Illustrates a semi-realistic use of SimpleTag and the Expression 
    Language.  First, a &lt;my:findBook&gt; tag is invoked to populate 
    the page context with a BookBean.  Then, the books fields are printed 
    in all caps.</p>
    <br>
    <b><u>Result:</u></b><br>
    <my:findBook var="book"/>
    <table border="1">
        <thead>
	    <td><b>Field</b></td>
	    <td><b>Value</b></td>
	    <td><b>Capitalized</b></td>
	</thead>
	<tr>
	    <td>Title</td>
	    <td>${book.title}</td>
	    <td>${my:caps(book.title)}</td>
	</tr>
	<tr>
	    <td>Author</td>
	    <td>${book.author}</td>
	    <td>${my:caps(book.author)}</td>
	</tr>
	<tr>
	    <td>ISBN</td>
	    <td>${book.isbn}</td>
	    <td>${my:caps(book.isbn)}</td>
	</tr>
    </table>
  </body>
</html>
