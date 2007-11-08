<%@taglib prefix="my" uri="http://com.acme/test-taglib"%>

<my:custom>
<%! int counter=0; %>
Counter value: <%= ++counter %>
</my:custom>