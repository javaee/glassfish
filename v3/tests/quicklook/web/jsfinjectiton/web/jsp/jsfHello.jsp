<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
 <html>
  <head>
    <title>JSF Hello</title>
  </head>
  <body>
    <h:form id="form">
      <h3>
        Hello, and welcome<br>
        <h:outputText value="If injection worked, this sentence should be followed by the injected words -> #{bean.entry} "/><br>
        <h:outputText value="If second injection worked, this sentence should be followed by 'a non-negative number' === #{bean.number} "/>
        
      </h3>
    </h:form>
  </body>
 </html>
</f:view>

