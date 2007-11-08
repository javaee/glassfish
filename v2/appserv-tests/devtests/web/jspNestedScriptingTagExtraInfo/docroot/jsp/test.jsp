<%@taglib prefix="my" uri="http://com.acme/test-taglib"%>

<my:custom name="outer">
  Outer: <%= outer %>
  <my:custom name="inner"/>
  Inner: <%= inner %>
</my:custom>
