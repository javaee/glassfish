<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
            xmlns="http:///xsl-tests">
  <sch:pattern name="test">
    <sch:rule context="/">
      <sch:assert test="test-results">
        There must be a collection of test results
      </sch:assert>
    </sch:rule>
    <sch:rule context="test-results">
      <sch:assert test="count(test-result) = @count" diagnostics="expected-count">
        There must be as many test results as tests!
      </sch:assert>
    </sch:rule>
    <sch:rule context="test-result">
      <sch:assert test="count(./nonAssertion) = @expectedNonAssertions" diagnostics="identity negative">
Negative test failed.
      </sch:assert>
      <sch:assert test="count(./assertion) = @expectedAssertions" diagnostics="identity positive">
Positive test failed
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="identity">
      In test &lt;test ruleId=&quot;<sch:value-of select="@ruleId"/>&quot; assertionId=&quot;<sch:value-of select="@assertionId"/>&quot; id=&quot;<sch:value-of select="normalize-space(@id)"/>&quot;&gt; &#xa;
    </sch:diagnostic>
    <sch:diagnostic id="negative">
Expected <sch:value-of select="0+@expectedNonAssertions"/> non assertions; got <sch:value-of select="count(./nonAssertion)"/>
    </sch:diagnostic>
    <sch:diagnostic id="positive">
Expected <sch:value-of select="0+@expectedAssertions"/> assertions; got <sch:value-of select="count(./assertion)"/>
    </sch:diagnostic>
    <sch:diagnostic id="expected-count">
      Expected <sch:value-of select="@count"/> test-results but got <sch:value-of select="count(//test-result)"/> 
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
