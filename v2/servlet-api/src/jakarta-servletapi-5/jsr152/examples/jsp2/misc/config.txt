<%@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"%>
    <h1>JSP 2.0 Examples - JSP Configuration</h1>
    <hr>
    <p>Using a &lt;jsp-property-group&gt; element in the web.xml 
    deployment descriptor, this JSP page has been configured in the
    following ways:</p>
    <ul>
      <li>Uses &lt;include-prelude&gt; to include the top banner.</li>
      <li>Uses &lt;include-coda&gt; to include the bottom banner.</li>
      <li>Uses &lt;scripting-invalid&gt; true to disable 
	  &lt;% scripting %&gt; elements</li>
      <li>Uses &lt;el-ignored&gt; true to disable ${EL} elements</li>
      <li>Uses &lt;page-encoding&gt; ISO-8859-1 to set the page encoding (though this is the default anyway)</li>
    </ul>
    There are various other configuration options that can be used.

