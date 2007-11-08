<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
  <sch:ns prefix="rng" uri="http://relaxng.org/ns/structure/1.0"/>

  <sch:p>
    This schematron checks the domain.rng to ensure that all elements
    are properly referenced. i.e. all definitions are referenced and all
    references have definitions
  </sch:p>
  <sch:pattern name="everything">
    <sch:rule context="rng:define">
      <sch:key name="definitions" path="@name"/>
      <sch:assert test="key('refs', @name)"
		  diagnostics="unreferencedDefn">
	Every definition is referenced
      </sch:assert>
    </sch:rule>
    <sch:rule context="rng:ref">
      <sch:key name="refs" path="@name"/>
      <sch:assert test="key('definitions', @name)"
		  diagnostics="undefinedRef">
	Every reference is defined
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="undefinedRef">
      Undefined refence is: <sch:value-of select="@name"/>
    </sch:diagnostic>
    <sch:diagnostic id="unreferencedDefn">
      Unreferenced definition is: <sch:value-of select="@name"/>
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
