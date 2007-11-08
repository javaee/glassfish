<%
javax.el.ValueExpression v1 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${A+B}", pageContext, Object.class, null);
javax.el.ValueExpression v2 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${ A + B }", pageContext, Object.class, null);
out.write("Comparing ${A+B} with ${ A + B }: " + (v1.equals(v2)? "PASS\n": "FAIL\n"));

v1 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${A}", pageContext, Object.class, null);
v2 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("#{ A  }", pageContext, Object.class, null);
out.write("Comparing ${A} with #{A}: " + (v1.equals(v2)? "PASS\n": "FAIL\n"));

/*
v1 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${a.b}", pageContext, Object.class, null);
v2 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${a['b']}", pageContext, Object.class, null);
out.write(v1.equals(v2)? "equals": "not equal");
out.write("</br>\n");
*/
v1 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${A<B}", pageContext, Object.class, null);
v2 =  org.apache.jasper.runtime.PageContextImpl.getValueExpression("${ A lt B }", pageContext, Object.class, null);
out.write("Comparing ${A<B} with ${ A lt B }: " + (v1.equals(v2)? "PASS\n": "FAIL\n"));
%>
