<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
<!--   <sch:pattern name="Message Ids are Unique"> -->
<!--     <sch:rule context="sch:assert | sch:diagnostic"> -->
<!--       <sch:assert test="not(preceding::sch:assert[@id=current()/@id] | preceding::sch:diagnostic[@id=current()/@id]) " -->
<!-- 		  diagnostics="d1"> -->
<!-- 	Message id is not unique -->
<!--       </sch:assert> -->
<!--     </sch:rule> -->
<!--   </sch:pattern> -->
  <sch:pattern name="Message Ids are Unique">
    <sch:rule context="sch:*[@id]">
      <sch:assert test="not(preceding::sch:*[@id][@id=current()/@id])"
		  diagnostics="d1">
	Message id is not unique
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  <sch:diagnostics>
    <sch:diagnostic id="d1">
      The id is: <sch:value-of select="./@id"/>
    </sch:diagnostic>
  </sch:diagnostics>
  
</sch:schema>
