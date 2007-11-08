<%@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"%>

<html>
  <head>
    <title>JSP 2.0 Examples - Shuffle Example</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Shuffle Example</h1>
    <hr>
    <p>Try reloading the page a few times.  Both the rows and the columns
    are shuffled and appear different each time.</p>
    <p>Here's how the code works.  The SimpleTag handler called 
    &lt;my:shuffle&gt; accepts three attributes.  Each attribute is a 
    JSP Fragment, meaning it is a fragment of JSP code that can be
    dynamically executed by the shuffle tag handler on demand.  The 
    shuffle tag handler executes the three fragments in a random order.
    To shuffle both the rows and the columns, the shuffle tag is used
    with itself as a parameter.</p>
    <hr>
    <blockquote>
     <font color="#ffffff">
      <table>
        <my:shuffle>
          <jsp:attribute name="fragment1">
            <tr>
              <my:shuffle>
                <jsp:attribute name="fragment1">
                  <my:tile color="#ff0000" label="A"/>
                </jsp:attribute>
                <jsp:attribute name="fragment2">
                  <my:tile color="#00ff00" label="B"/>
                </jsp:attribute>
                <jsp:attribute name="fragment3">
                  <my:tile color="#0000ff" label="C"/>
                </jsp:attribute>
              </my:shuffle>
            </tr>
          </jsp:attribute>
          <jsp:attribute name="fragment2">
            <tr>
              <my:shuffle>
                <jsp:attribute name="fragment1">
                  <my:tile color="#ff0000" label="1"/>
                </jsp:attribute>
                <jsp:attribute name="fragment2">
                  <my:tile color="#00ff00" label="2"/>
                </jsp:attribute>
                <jsp:attribute name="fragment3">
                  <my:tile color="#0000ff" label="3"/>
                </jsp:attribute>
              </my:shuffle>
            </tr>
          </jsp:attribute>
          <jsp:attribute name="fragment3">
            <tr>
              <my:shuffle>
                <jsp:attribute name="fragment1">
                  <my:tile color="#ff0000" label="!"/>
                </jsp:attribute>
                <jsp:attribute name="fragment2">
                  <my:tile color="#00ff00" label="@"/>
                </jsp:attribute>
                <jsp:attribute name="fragment3">
                  <my:tile color="#0000ff" label="#"/>
                </jsp:attribute>
              </my:shuffle>
            </tr>
          </jsp:attribute>
        </my:shuffle>
      </table>
     </font>
    </blockquote>
  </body>
</html>
