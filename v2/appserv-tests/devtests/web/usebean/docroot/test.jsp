<%@ page errorPage="err.jsp" %>

The class specified in the class attribute is not valid, compilation
should not raise error because errorOnUseBeanClassAttribute is false
by default.

<jsp:useBean id="neuter" class="java.util.AbstractList"/>
