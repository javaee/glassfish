<%@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"%>
<html>
  <head>
    <title>JSP 2.0 Examples - Dynamic Attributes</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Dynamic Attributes</h1>
    <hr>
    <p>This JSP page invokes a custom tag that accepts a dynamic set 
    of attributes.  The tag echoes the name and value of all attributes
    passed to it.</p>
    <hr>
    <h2>Invocation 1 (six attributes)</h2>
    <ul>
      <my:echoAttributes x="1" y="2" z="3" r="red" g="green" b="blue"/>
    </ul>
    <h2>Invocation 2 (zero attributes)</h2>
    <ul>
      <my:echoAttributes/>
    </ul>
    <h2>Invocation 3 (three attributes)</h2>
    <ul>
      <my:echoAttributes dogName="Scruffy" 
	   		 catName="Fluffy" 
			 blowfishName="Puffy"/>
    </ul>
  </body>
</html>
