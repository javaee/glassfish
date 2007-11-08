<?xml version="1.0" encoding="UTF-8"?>
<!--

This schematron checks that every element in the input rng document
(TOP.rng) is an element in the element pattern in domain.sch.

Not all referenced files contain elements - some contain data
definitions. Hence the test is

@href contains element implies element is in domain.sch

A implies B is implemented as

        NOT(A) or B

-->
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
  <sch:ns prefix="rng" uri="http://relaxng.org/ns/structure/1.0"/>
  <sch:pattern name="Synchronize top to domain.sch">
    <sch:rule context="rng:include">
      <sch:assert
       test='not(document(@href)//rng:element) or document("../../../sch/domain.sch")//sch:rule[@context=substring-before(current()/@href,".rng")]'
       diagnostics="element">
	Every element included in TOP.rng is present in the elements
	pattern of domain.sch
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="element">
      Missing element: <sch:value-of
      select="substring-before(current()/@href, '.rng')"/>
    </sch:diagnostic>
  </sch:diagnostics>
  
</sch:schema>
