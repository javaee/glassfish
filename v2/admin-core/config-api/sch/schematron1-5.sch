<?xml version="1.0" encoding="UTF-8"?>
<!-- <!DOCTYPE schema PUBLIC "http://www.ascc.net/xml/schematron" -->
<!--    "http://www.ascc.net/xml/schematron/schematron1-5.dtd"> -->
<schema xmlns="http://www.ascc.net/xml/schematron"
       xmlns:sch="http://www.ascc.net/xml/schematron"
       xml:lang="en"                                       
       xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance"
       xsi:schemaLocation="http://www.ascc.net/xml/schematron
          http://www.ascc.net/xml/schematron/schematron1-5.xsd" 
	fpi="+//IDN ascc.net//SGML XML Schematron 1.5 Schema for Schematron 1.5//EN"
	schemaVersion="2002/08/19"  version="1.5"
        defaultPhase="New"
       icon="http://www.ascc.net/xml/resource/schematron/bilby.jpg">

	<title>Schematron 1.5</title>
	<ns uri="http://www.ascc.net/xml/schematron" prefix="sch"/>

        <p>Copyright (C) 2001 Rick Jelliffe. 
	Freely and openly available under zlib/libpng license.</p>
	<p>This schema is open: it only
 	considers elements in the Schematron namespce. 
	Elements and attributes from other namespaces can be used freely.
 	This schema does not assume that the Schematron schema is the top-level element.
 	</p>
  	<p>This schema uses conservative rules (e.g. no use of key()) to 
  	work with incomplete XSLT-based implementations.</p>


        <phase id="New">
		<p>For creating new documents.</p>
		<active pattern="mini"/>
	</phase>
       <phase id="Draft">
                <p>For fast validation of draft documents.</p>
		<active pattern="required" />
	</phase>
        <phase id="Full">
                <p>For final validation and tracking some tricky problems.</p>
		<active pattern="mini" />
		<active pattern="required" />
		<active pattern="attributes" />
        </phase>

	<pattern name="Minimal Schematron" id="mini">
	<p>These rule establish the smallest possible Schematron document.
	These rules may be handy for beginners with starting documents.</p>
		<rule context="/">
			<assert test="//sch:schema"
			>A Schematron schema should have a schema element. </assert>
                        <report test="count(//sch:schema) > 1"
			>There should only be one schema per document.</report>
			<assert test="//sch:schema/sch:pattern "
			>A Schematron schema should have pattern elements inside the schema element</assert>
			<assert test="//sch:schema/sch:pattern/sch:rule[@context]"
			>A Schematron schema should have rule elements inside the pattern elements. Rule elements should have a context attribute.</assert>
			<assert test="//sch:schema/sch:pattern/sch:rule/sch:assert[@test] 
 			or //sch:schema/sch:pattern/sch:rule/sch:report[@test]" 
			>A Schematron schema should have  assert or report elements inside the rule elements. Assert and report elements should have a test attribute.</assert>
		</rule>
        </pattern>

	<pattern name="Schematron Elements and Required Attributes" id="required">
		<p>Rules defining occurrence rules for Schematron elements
 		and their required attributes. Note that for attributes,
 		it is not that the attribute is being tested for existance,
 		but whether it has a value.</p>
		<p>Some elements require certain children or attributes. 
  		Other elements require certain parents. Schematron can represent 
  		both these kinds of coupling. </p> 

                <rule abstract="true" id="report-n-assert" >
			<assert test="parent::sch:rule"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element rule.</assert>
			<assert test="string-length(normalize-space(text())) &gt; 0"
			>A <name/> element should contain a natural language sentence.</assert>
			<assert test="string-length(normalize-space(@test)) &gt; 0"
			>The element <name/> must have a value for the attribute test. This should be an XSLT expression.</assert>
			<report test="@context"
			>The <name/> element cannot have a context attribute: that should go on the rule element.</report>
		</rule>
                <rule context="sch:assert | sch:report">
			<extends rule="report-n-assert" />
		</rule> 

		<rule context="sch:schema">
			<assert test="count(sch:*) = count(sch:title|sch:ns|sch:phrase|sch:p|sch:pattern|sch:diagnostics|sch:phase)"			
			>The element <name/> should contain only the elements title, ns, phrase, p, pattern, diagnostics or phase from the Schematron namespace.</assert>
			<assert test="sch:pattern"
			>A schema element should contain at least one pattern element.</assert>
			<report test="ancestor::sch:*"
			>A Schematron schema should not appear as a child of another Schematron schema.</report>
			<report test="@defaultPhase and sch:phase and not(@defaultPhase='#ALL') and not(sch:phase[@id= current()/@defaultPhase])"
			>The value of the defaultPhase attribute must match the id of a phase element.</report>
		</rule> 
		<rule context="sch:title">
			<assert test="parent::sch:schema"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element schema.</assert>
			<assert test="count(preceding-sibling::sch:*) = 0"
			>The element <name/> should only appear as the first element from the Schematron namespace in the schema element.</assert>
		</rule>
		<rule context="sch:ns">
			<assert test="parent::sch:schema"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element schema.</assert>
			<assert test="string-length(normalize-space(@prefix)) &gt; 0"
			>The element <name/> must have a value for the attribute prefix.</assert>
			<assert test="string-length(normalize-space(@uri)) &gt; 0"
			>The element <name/> must have a value for the attribute uri.</assert>
			<assert test="count(preceding-sibling::sch:*) = count(preceding-sibling::sch:title)"
			>The <name/> element must come before any other Schematron elements, except the title</assert>
			<report test="*"
			>The <name/> element should be empty.</report>
		</rule>
		<rule context="sch:phase">
			<assert test="parent::sch:schema"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element schema.</assert>
			<assert test="count(preceding-sibling::sch:*) = count(preceding-sibling::sch:phase)
			+ count(preceding-sibling::sch:title) + count(preceding-sibling::sch:ns)
			+ count(preceding-sibling::sch:p)"
			>The <name/> elements must come before any other Schematron elements, except the title, ns and p elements</assert>
		</rule>
		<rule context="sch:active"> 
			<assert test="parent::sch:phase"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element phase.</assert>
			<assert test="string-length(normalize-space(@pattern)) &gt; 0"
			>The element <name/> must have a value for the attribute pattern.</assert>
		</rule>
		<rule context="sch:pattern">
			<assert test="parent::sch:schema"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element schema.</assert>
			<assert test="count(sch:*) = count(sch:rule|sch:p)"
			>The element <name/> should contain only rule and p elements from the Schematron namespace.</assert>
			<assert test="sch:rule"
			>The element <name/> should contain at least one rule element.</assert>
			<assert test="string-length(normalize-space(@name)) &gt; 0"
			>The element <name/> must have a value for the attribute name.</assert>
			<assert test="count(sch:title) &lt; 2"
			>A Schematron schema cannot have more than one title element.</assert>
		</rule>
		<rule context="sch:rule[@abstract='true']">
			<assert test="parent::sch:pattern"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element pattern.</assert>
			<assert test="count(sch:*) = count(sch:assert |sch:report|sch:key|sch:extends ) "
			>The element <name/> should contain only the elements assert, report, key or extends from the Schematron namespace.</assert>
			<assert test="sch:assert | sch:report | sch:extends"
			>The element <name/> should contain at least one assert, report or extends elements.</assert>
			<report test="@test"
			>The <name/> element cannot have a test attribute: that should go on a report or assert element.</report>
			<report test="@context"
			>An abstract rule cannot have a context attribute.</report>
			<assert test="string-length(normalize-space(@id)) &gt; 0"
			>An rule should have an id attribute. </assert>
		</rule>
		<rule context="sch:rule">
			<assert test="parent::sch:pattern"  diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element pattern.</assert>
			<assert test="count(sch:*) = count(sch:assert |sch:report|sch:key|sch:extends ) "
			>The element <name/> should contain only the elements assert, report, key or extends from the Schematron namespace.</assert>
			<assert test="sch:assert | sch:report | sch:extends"
			>The element <name/> should contain at least one assert, report or extends elements.</assert>
			<report test="@test"
			>The <name/> element cannot have a test attribute: that should go on a report or assert element.</report>
			<assert test="string-length(normalize-space(@context)) &gt; 0"
			>A rule should have a context attribute. This should be an XSLT pattern for selecting nodes to make assertions and reports about. (Abstract rules do not require a context attribute.)</assert>
			<assert test="not(@abstract) or (@abstract='false')  or (@abstract='true')"
			>In a rule, the abstract attribute is optional, and can have values 'true' or 'false'</assert>
		</rule>
		<rule context="sch:diagnostics">
			<assert test="parent::sch:schema"  diagnostics="bad-parent"
			>The element <name/> should only appear as a child of the schema element</assert>
			<report test="following-sibling::sch:*"
			>The element <name/> should be the last element in the schema.</report>
		</rule>
		<rule context="sch:diagnostic">
			<assert test="parent::sch:diagnostics"  diagnostics="bad-parent"
			>The element <name/> should only appear in the diagnostics section.</assert>
			<assert test="string-length(normalize-space(@id)) &gt; 0"
			>The element <name/> must have a value for the attribute id. </assert>
		</rule>
		<rule context="sch:key">
			<assert test="parent::sch:rule"  diagnostics="bad-parent"
			>The element <name/> should only appear in a rule.</assert>
			<assert test="string-length(normalize-space(@name)) &gt; 0"
			>The element <name/> must have a value for the attribute name. </assert>
			<assert test="string-length(normalize-space(@path)) &gt; 0"
			>The element <name/> must have a value for the attribute path.   This should be an XPath expression.</assert>
			<report test="*"
			>The <name/> element should be empty.</report>
		</rule>
		<rule context="sch:extends">
			<assert test="parent::sch:rule"  diagnostics="bad-parent"
			>The element <name/> should only appear in a rule.</assert>
			<assert test="string-length(normalize-space(@rule)) &gt; 0"
			>The element <name/> must have a value for the attribute rule. </assert>
			<report test="*"
			>The <name/> element should be empty.</report>
                        <assert test="/*//sch:rule[@abstract='true'][@id = current()/@rule]"
                        >The <name/> element should have an attribute rule which gives the id of an abstract rule.</assert>
		</rule>
		<rule context="sch:p">
			<assert test="parent::sch:*"  diagnostics="bad-parent"
			>The element <name/> should only appear inside an element from the Schematron namespace. It is equivalent to the HTML element of the same name.</assert>
		</rule>
		<rule context="sch:name">
			<assert test="parent::sch:assert | parent::sch:report |parent::sch:p | parent::sch:diagnostic"
			 diagnostics="bad-parent"
			>The element <name/> should only appear inside a Schematron elements p (paragraph) or diagnostic.</assert>
			<report test="*"
			>The <name/> element should be empty.</report>
		</rule>
		<rule context="sch:emph">
			<assert test="parent::sch:p | parent::sch:diagnostic"
			 diagnostics="bad-parent"
			>The element <name/> should only appear inside a Schematron elements p (paragraph) or diagnostic. It is equivalent to the HTML element of the same name.</assert>
		</rule>
		<rule context="sch:dir">
			<assert test="parent::sch:p | parent::sch:diagnostic"
			 diagnostics="bad-parent"
			>The element <name/> should only appear inside a Schematron elements p (paragraph) or diagnostic.</assert>
			<assert test="@value and (@value='rtl' or @value='ltr')"
			>The attribute value of the <name/> element must be lowercase "rtl" or "ltr". It is equivalent to the HTML element of the same name.</assert>
		</rule>
		<rule context="sch:span">
			<assert test="parent::sch:p | parent::sch:diagnostic"
			 diagnostics="bad-parent"
			>The element <name/> should only appear inside a Schematron elements p (paragraph) or diagnostic. It is equivalent to the HTML element of the same name.</assert>
		</rule>
		<rule context="sch:value-of">
			<assert test="parent::sch:diagnostic"   diagnostics="bad-parent"
			>The element <name/> should only appear inside the Schematron element diagnostic.</assert>
			<assert test="string-length(normalize-space(@select)) &gt; 0"
			>The element <name/> must have a value for the attribute select. The value should be an XPath expression.</assert>
			<report test="*"
			>The <name/> element should be empty.</report>
		</rule>
                <rule context="sch:*">
			<report test="1=1" diagnostics="spelling"
			>The <name/> element is not an element from the Schematron 1.5 namespace</report>
                </rule>
	</pattern>

	<pattern name="Schematron Attributes" id="attributes" >
		<p>These rules specify which elements each attribute can belong to, and what they mean.</p>
		<rule context="sch:*">
			<report test="@abstract and not(self::sch:rule)"
			>The boolean attribute abstract can only appear on the element rule. An abstract rule can be used to extend other rules.</report>
			<report test="@class and not(self::sch:span or self::sch:p)"
			>The attribute class can only appear on the elements span and p. It gives a name that can be used by CSS stylesheets.</report>
			<report test="@context and not(self::sch:rule)"
			>The attribute context can only appear on the element rule. It is an XPath pattern.</report>
			<report test="@defaultPhase and not(self::sch:schema)"
			>The attribute defaultPhase can only appear on the element schema. It is the id of the phase that will initially be active.</report>
			<report test="@diagnostics and not(self::sch:assert or self::sch:report)"
			>The attribute diagnostics can only appear on the elements assert and report. It is the id of some relevent diagnostic or hint.</report>
			<report test="@fpi and not(self::sch:schema or self::sch:phase)"
			>The attribute fpi can only appear on the elements schema and phase. It is an ISO Formal Public Identifier.</report>
			<report test="@icon and not(self::sch:schema or self::sch:assert or
      			self::sch:diagnostic or self::sch:key or self::sch:p or self::sch:pattern
      			or self::sch:phase or self::sch:assert )"
			>The attribute icon can only appear on the elements schema, assert , diagnostic, key, p, pattern, phase and report. It is the URL of a small image. </report>
			<report test="@id and not(self::sch:schema or self::sch:assert or
     			self::sch:p or self::sch:pattern or self::sch:phase or 
     			self::sch:report or self::sch:rule or self::sch:diagnostic)"
			>The attribute id can only appear on the elements schema, assert, p, pattern, phase, report, rule and diagnostic. It is a name, it should not start with a number or symbol.</report>
			<report test="@name and not(self::sch:key or self::sch:pattern)"
			>The attribute name can only appear on the elements pattern and key.</report>
			<report test="@ns and not(self::sch:schema)"
			>The attribute ns can only appear on the schema element.  It is the namespace to which the names in the role attributes belong.</report>
			<report test="@path and not(self::sch:key | self::sch:name)"
			>The attribute path can only appear on the element key. It is an XPath path.</report>
			<report test="@pattern and not(self::sch:active)"
			>The attribute pattern can only appear on the element active. It gives the id of a pattern that should be activated in that phase.</report>
			<report test="@prefix and not(self::sch:ns)"
			>The attribute prefix can only appear on the element ns.</report>
			<report test="@role and not(self::sch:assert or self::sch:report or self::sch:rule)"
			>The attribute role can only appear on the element assert, report or rule. It is a simple name, not a phrase.</report>
			<report test="@rule and not(self::sch:extends)"
			>The attribute rule can only appear on the element extends. It is the id of an abstract rule declared elsewhere in the schema.</report>
			<report test="@see and not(self::sch:pattern)"
			>The attribute see can only appear on the element pattern. It is the URL of some documentation for the schema.</report>
			<report test="@select and not(self::sch:value-of)"
			>The attribute select can only appear on the element value-of, with the same meaning as in XSLT. It is an XSLT pattern.</report>
	                <report test="@schemaVersion and not(self::sch:schema)"
			>The attribute schemaVersion can only appear on the element schema. It gives the version of the schema.</report>
			<report test="@subject and not(self::sch:assert or self::sch:report)"
			>The attribute subject can only appear on the elements assert and report. It is an XSLT pattern. </report>
			<report test="@test and not(self::sch:assert or self::sch:report)"
			>The attribute test can only appear on the elements assert and report. It is an XPath expression with the XSLT additional functions.</report>
			<report test="@uri and not(self::sch:ns)"
			>The attribute uri can only appear on the element ns. It is a URI.</report>
			<report test="@value and not(self::sch:dir)"
			>The attribute value can only appear on the element dir. It sets the directionality of text: 'rtl' is right-to-left and 'ltr' is left-to-right.</report>
			<report test="@version and not(self::sch:schema)"
			>The attribute version can only appear on the element schema. It gives the version of Schematron required as major number "." minor number.</report>
			<assert test="not(attribute::*) or attribute::*[string-length(normalize-space(text()))=0]"
			>Every attribute on a Schematron element must have a value if it is specified.</assert>                  
		</rule>
	</pattern>
        <diagnostics>
		<diagnostic id="spelling"
		>Check this is not a spelling error. The recognized element names are
		schema, title, ns, pattern, rule, key, assert, report, diagnostics, diagnostic,
		name, value-of, emph and dir.</diagnostic>
		<diagnostic id="bad-parent"
		>The element appeared inside a <value-of select="name(parent::*)"/>.</diagnostic>
	</diagnostics>
</schema>

