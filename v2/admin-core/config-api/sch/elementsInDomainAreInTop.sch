<?xml version="1.0" encoding="UTF-8" ?>
<!--
This schematron checks that every element in the elements pattern
within the input schematron (domain.sch) is contained in the TOP.rng file.
-->
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" >
  <sch:ns prefix="rng" uri="http://relaxng.org/ns/structure/1.0"/>
  <sch:pattern name="Synchronize domain to rng">
    <sch:rule context="sch:pattern[@id='elements']/sch:rule">
      <sch:assert
       test="document('../../../rng/TOP.rng')//rng:include[@href=concat(current()/@context, '.rng')]"
       diagnostics="context">
        Each rule's context corresponds to a file included in TOP.rng
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="context">
      Missing context: <sch:value-of select="@context"/>
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
