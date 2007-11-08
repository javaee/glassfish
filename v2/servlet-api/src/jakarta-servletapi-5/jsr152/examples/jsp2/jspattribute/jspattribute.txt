<%@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"%>

<html>
  <head>
    <title>JSP 2.0 Examples - jsp:attribute and jsp:body</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - jsp:attribute and jsp:body</h1>
    <hr>
    <p>The new &lt;jsp:attribute&gt; and &lt;jsp:body&gt; 
    standard actions can be used to specify the value of any standard
    action or custom action attribute.</p>
    <p>This example uses the &lt;jsp:attribute&gt;
    standard action to use the output of a custom action invocation
    (one that simply outputs "Hello, World!") to set the value of a
    bean property.  This would normally require an intermediary
    step, such as using JSTL's &lt;c:set&gt; action.</p>
    <br>
    <jsp:useBean id="foo" class="jsp2.examples.FooBean">
      Bean created!  Setting foo.bar...<br>
      <jsp:setProperty name="foo" property="bar">
        <jsp:attribute name="value">
	  <my:helloWorld/>
        </jsp:attribute>
      </jsp:setProperty>
    </jsp:useBean>
    <br>
    Result: ${foo.bar}
  </body>
</html>
