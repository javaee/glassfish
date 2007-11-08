<?xml version="1.0" encoding="UTF-8"?>
<!--
This schematron ensures that the input file (some kind of schematron) conforms to the
requirements for the test system

i.e. a test is contained as a child of a rule
that each test within a rule has an assertionID which matches one of
the assertion ids of the containing rule
that each contained test that shares the same assertionID has a unique
id
-->
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
	    xmlns:t="http:///xsl-tests">
  <sch:ns prefix="t" uri="http:///xsl-tests"/>
  <sch:pattern name="all" >
    <sch:rule context="t:test">
      <sch:assert test="ancestor::sch:rule">
	Each test is contained by a sch:rule element
      </sch:assert>
      <sch:assert test="ancestor::sch:rule/sch:assert[@id =
		  current()/@assertionId] |
		  //sch:rule[current()/ancestor::sch:rule/sch:extends/@rule = @id]/sch:assert[@id = current()/@assertionId]"
		  diagnostics="diagnostic-test-id
		  diagnostic-wrong-assertionId diagnostic-parent-assert-id">
	Each test has an assertionId attribute that matches one of the
	ancestor sch:rule assert id elements or which matches the
	assertion id of an extended assertion
      </sch:assert>
      <sch:assert test="not(preceding-sibling::t:test[@id =
		  current()/@id][@assertionId = current()/@assertionId])"
		  diagnostics="diagnostic-test-id">
	Each test within a sch:rule that tests a specific assertion is
	uniquely identified by its id attribute and the id of the
	assertion it is testing
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="diagnostic-parent-assert-id">
      parent assertion ids are: <sch:value-of select="ancestor::sch:rule/sch:assert/@id"/>
    </sch:diagnostic>
    <sch:diagnostic id="diagnostic-test-id">
      Non-unique test has id: <sch:value-of select="@id"/> ruleId:
      <sch:value-of select="@ruleId"/> and assertionId: <sch:value-of select="@assertionId"/>
    </sch:diagnostic>
    <sch:diagnostic id="diagnostic-wrong-assertionId">
      For this test the assertionId is: <sch:value-of select="@assertionId"/>
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
