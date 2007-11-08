<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xsd = "http://www.w3.org/2001/XMLSchema"
version="1.0">
  <!--
"specials"

 debug-options & rmic-options are specially handled cos in the schema
theyve got values of "FIXME_NOW"!!!
-->

  <xsl:output method="xml"/>

  <xsl:template match="/xsd:schema">
    <xsl:copy-of select="comment()[1]"/>

    <xsl:text>
      
    </xsl:text>
    <xsl:copy-of select="comment()[2]"/>

    <xsl:text>

    </xsl:text>
    <xsl:comment> ENTITY Definitions for the base types from XML Schema </xsl:comment>
      <xsl:call-template name="make-schema-types"/>
    <xsl:comment> ENTITY Definitions for the dtd </xsl:comment>
    <xsl:apply-templates select="./xsd:simpleType"/>
    <xsl:text>&#xa;</xsl:text>

<xsl:copy-of select="comment()[7]"/>

<xsl:apply-templates select="./xsd:element[@name]"/>
  </xsl:template>


<xsl:template match="xsd:element[@name]">
  <xsl:for-each select="child::comment()">
    <xsl:text>&#xa;</xsl:text>
    <xsl:copy-of select="."/>
  </xsl:for-each>
  <xsl:call-template name="makeElement"/>
  <xsl:call-template name="makeAttList"/>
</xsl:template>

  <!-- ************* ENTITY DEFINITION *************** -->
<xsl:template match="xsd:simpleType">
  <xsl:for-each select="child::comment()">
    <xsl:text>&#xa;</xsl:text>
    <xsl:copy-of select="."/>
  </xsl:for-each>
  <xsl:text disable-output-escaping='yes'>
&lt;!ENTITY % </xsl:text>
  <xsl:value-of select="@name"/>
  <xsl:text> "</xsl:text>
  <xsl:choose>
    <xsl:when test="count(.//xsd:enumeration) > 0">
      <xsl:text>(</xsl:text>
      <xsl:for-each select=".//xsd:enumeration/@value">
        <xsl:if test="position() &gt; 1"><xsl:text> | </xsl:text></xsl:if>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>CDATA</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text disable-output-escaping='yes'>"></xsl:text>
</xsl:template>

  <!-- ************* ELEMENT DEFINITION *************** -->
<xsl:template name="makeElement">
  <xsl:text disable-output-escaping='yes'>
&lt;!ELEMENT </xsl:text>
  <xsl:value-of select="@name"/>
  <xsl:text> </xsl:text>
  <xsl:choose>
    <xsl:when test="count(.//xsd:element) = 0">
      <xsl:choose>
        <xsl:when test="@type">
          <xsl:text>(#PCDATA)</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>EMPTY</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text disable-output-escaping='yes'>&gt;</xsl:text>
</xsl:template>

<xsl:template match="xsd:complexType">
  <xsl:apply-templates select="xsd:sequence | xsd:choice"/>
</xsl:template>

<xsl:template match="xsd:sequence">
  <xsl:text>(</xsl:text>
  <xsl:for-each select="*">
    <xsl:if test="position() > 1"><xsl:text>, </xsl:text></xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:text>)</xsl:text>
  <xsl:call-template name="cardinality"/>
</xsl:template>

<xsl:template match="xsd:choice">
  <xsl:text>(</xsl:text>
  <xsl:for-each select="*">
    <xsl:if test="position() > 1"><xsl:text>| </xsl:text></xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:text>)</xsl:text>
  <xsl:call-template name="cardinality"/>
</xsl:template>

<xsl:template match="xsd:element[@ref]">
  <xsl:value-of select="@ref"/>
  <xsl:call-template name="cardinality"/>
</xsl:template>

<xsl:template name="cardinality">
  <xsl:choose>
    <xsl:when test="not(@minOccurs or @maxOccurs) or (@minOccurs='1' and @maxOccurs='1') or (@minOccurs='1' and not(@maxOccurs))"/>
    <xsl:when test="@minOccurs='0'">
      <xsl:choose>
        <xsl:when test="not(@maxOccurs) or @maxOccurs='1'">
          <xsl:text>?</xsl:text>
        </xsl:when>
        <xsl:when test="@maxOccurs='unbounded'">
          <xsl:text>*</xsl:text>
        </xsl:when>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="@maxOccurs='unbounded' and (not(@minOccurs) or @minOccurs>0)">
      <xsl:text>+</xsl:text>
    </xsl:when>
  </xsl:choose>
</xsl:template>

  <!-- ************* END ELEMENT DEFINITION *************** -->

  <!-- ************* ATTLIST DEFINITION *************** -->

<xsl:template name="makeAttList">
  <xsl:variable name="attrs" select=".//xsd:attribute"/>
  <xsl:if test="count($attrs) > 0">
    <xsl:text disable-output-escaping='yes'>
&lt;!ATTLIST </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:for-each select="$attrs">
      <xsl:apply-templates select="."/>
      <xsl:if test="position() != last()">
        <xsl:text>
        </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text disable-output-escaping='yes'>&gt;</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template match="xsd:attribute">
  <xsl:value-of select="@name"/>
  <xsl:text> </xsl:text>
    <xsl:call-template name="attribute-type"/>
  <xsl:text> </xsl:text>
  <xsl:choose>
    <xsl:when test="@default">
      <xsl:text>"</xsl:text><xsl:value-of select="@default"/><xsl:text>"</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="@use = 'required'">
          <xsl:text> #REQUIRED</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text> #IMPLIED</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="attribute-type">
  <xsl:choose>
    <xsl:when test="@type">
      <!--      <xsl:text>%</xsl:text><xsl:value-of select="@type"/><xsl:text>;</xsl:text> -->
      <xsl:call-template name="make-entity-reference">
        <xsl:with-param name="ref" select="@type"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="./xsd:simpleType">
      <xsl:text>(</xsl:text>
      <xsl:for-each select=".//xsd:enumeration/@value">
        <xsl:if test="position() > 1"><xsl:text>| </xsl:text></xsl:if>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>)</xsl:text>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="make-entity-reference">
  <xsl:param name='ref'/>
  <xsl:text>%</xsl:text><xsl:value-of select="translate($ref, ':', '-')"/><xsl:text>;</xsl:text>
</xsl:template>


  <!-- ************* END ATTLIST DEFINITION *************** -->

    <xsl:template name="make-schema-types">
      <xsl:text disable-output-escaping='yes'>
&lt;!--                    ENTITIES represents the ENTITIES attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of ENTITIES is the set of
                        finite, non-zero-length sequences of
                        ENTITYs that have been declared as
                        unparsed entities in a document type
                        definition. The lexical space of
                        ENTITIES is the set of white space
                        separated lists of tokens, of which each
                        token is in the lexical space of
                        ENTITY. The itemType of ENTITIES is
                        ENTITY.                                    -->
&lt;!ENTITY % xsd-ENTITIES "CDATA"                                      >
&lt;!--                    xsd-ENTITY                                 -->
&lt;!--                    ENTITY represents the ENTITY attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of ENTITY is the set of all
                        strings that match the NCName production
                        in [Namespaces in XML] and have been
                        declared as an unparsed entity in a
                        document type definition. The lexical
                        space of ENTITY is the set of all strings
                        that match the NCName production in
                        [Namespaces in XML]. The base type of
                        ENTITY is NCName.                          -->
&lt;!ENTITY % xsd-ENTITY  "CDATA"                                       >
&lt;!--                    xsd-ID                                     -->
&lt;!--                    ID represents the ID attribute type from
                        [XML 1.0 (Second Edition)]. The value
                        space of ID is the set of all strings
                        that match the NCName production in
                        [Namespaces in XML]. The lexical space
                        of ID is the set of all strings that
                        match the NCName production in
                        [Namespaces in XML]. The base type of ID
                        is NCName.                                 -->
&lt;!ENTITY % xsd-ID      "CDATA"                                       >
&lt;!--                    xsd-IDREF                                  -->
&lt;!--                    IDREF represents the IDREF attribute type
                        from [XML 1.0 (Second Edition)]. The
                        value space of IDREF is the set of all
                        strings that match the NCName production
                        in [Namespaces in XML]. The lexical
                        space of IDREF is the set of strings that
                        match the NCName production in
                        [Namespaces in XML]. The base type of
                        IDREF is NCName.                           -->
&lt;!ENTITY % xsd-IDREF   "CDATA"                                       >
&lt;!--                    xsd-IDREFS                                 -->
&lt;!--                    IDREFS represents the IDREFS attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of IDREFS is the set of
                        finite, non-zero-length sequences of
                        IDREFs. The lexical space of IDREFS is
                        the set of white space separated lists of
                        tokens, of which each token is in the
                        lexical space of IDREF. The itemType
                        of IDREFS is IDREF.                        -->
&lt;!ENTITY % xsd-IDREFS  "CDATA"                                       >
&lt;!--                    xsd-NCName                                 -->
&lt;!--                    NCName represents XML non-colonized
                        Names. The value space of NCName is the
                        set of all strings which match the
                        NCName production of [Namespaces in
                        XML]. The lexical space of NCName is the
                        set of all strings which match the
                        NCName production of [Namespaces in
                        XML]. The base type of NCName is Name.   -->
&lt;!ENTITY % xsd-NCName  "CDATA"                                       >
&lt;!--                    xsd-NMTOKEN                                -->
&lt;!--                    NMTOKEN represents the NMTOKEN attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of NMTOKEN is the set of
                        tokens that match the Nmtoken production
                        in [XML 1.0 (Second Edition)]. The
                        lexical space of NMTOKEN is the set of
                        strings that match the Nmtoken
                        production in [XML 1.0 (Second
                        Edition)]. The base type of NMTOKEN is
                        token.                                     -->
&lt;!ENTITY % xsd-NMTOKEN "CDATA"                                       >
&lt;!--                    xsd-NMTOKENS                               -->
&lt;!--                    NMTOKENS represents the NMTOKENS attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of NMTOKENS is the set of
                        finite, non-zero-length sequences of
                        NMTOKENs. The lexical space of
                        NMTOKENS is the set of white space
                        separated lists of tokens, of which each
                        token is in the lexical space of
                        NMTOKEN. The itemType of NMTOKENS is
                        NMTOKEN.                                   -->
&lt;!ENTITY % xsd-NMTOKENS "CDATA"                                      >
&lt;!--                    xsd-NOTATION                               -->
&lt;!--                    NOTATION represents the NOTATION attribute
                        type from [XML 1.0 (Second Edition)]. The
                        value space of NOTATION is the set
                        QNames. The lexical space of NOTATION is
                        the set of all names of notations declared
                        in the current schema.                     -->
&lt;!ENTITY % xsd-NOTATION "CDATA"                                      >
&lt;!--                    xsd-Name                                   -->
&lt;!--                    Name represents XML Names. The value
                        space of Name is the set of all strings
                        which match the Name production of [XML
                        1.0 (Second Edition)]. The lexical space
                        of Name is the set of all strings which
                        match the Name production of [XML 1.0
                        (Second Edition)]. The base type of Name
                        is token.                                  -->
&lt;!ENTITY % xsd-Name    "CDATA"                                       >
&lt;!--                    xsd-QName                                  -->
&lt;!--                    QName represents XML qualified names. The
                        value space of QName is the set of
                        tuples {namespace name, local part}, where
                        namespace name is an anyURI and local part
                        is an NCName. The lexical space of QName
                        is the set of strings that match the
                        QName production of [Namespaces in XML].   -->
&lt;!ENTITY % xsd-QName   "CDATA"                                       >
&lt;!--                    xsd-anyURI                                 -->
&lt;!--                    anyURI represents a Uniform Resource
                        Identifier Reference (URI). An anyURI
                        value can be absolute or relative, and may
                        have an optional fragment identifier
                        (i.e., it may be a URI Reference). This
                        type should be used to specify the
                        intention that the value fulfills the role
                        of a URI as defined by [RFC 2396], as
                        amended by [RFC 2732].                     -->
&lt;!ENTITY % xsd-anyURI  "CDATA"                                       >
&lt;!--                    xsd-base64Binary                           -->
&lt;!--                    base64Binary represents Base64-encoded
                        arbitrary binary data. The value space
                        of base64Binary is the set of
                        finite-length sequences of binary
                        octets. For base64Binary data the entire
                        binary stream is encoded using the Base64
                        Content-Transfer-Encoding defined in
                        Section 6.8 of [RFC 2045].                 -->
&lt;!ENTITY % xsd-base64Binary
                       "CDATA"                                       >
&lt;!--                    xsd-boolean                                -->
&lt;!--                    boolean has the value space required to
                        support the mathematical concept of
                        binary-valued logic: {true, false}.        -->
&lt;!ENTITY % xsd-boolean "CDATA"                                       >
&lt;!--                    xsd-byte                                   -->
&lt;!--                    byte is derived from short by setting
                        the value of maxInclusive to be 127 and
                        minInclusive to be -128. The base type
                        of byte is short.                          -->
&lt;!ENTITY % xsd-byte    "CDATA"                                       >
&lt;!--                    xsd-date                                   -->
&lt;!--                    date represents a calendar date. The
                        value space of date is the set of
                        Gregorian calendar dates as defined in 
                        5.2.1 of [ISO 8601]. Specifically, it is a
                        set of one-day long, non-periodic
                        instances e.g. lexical 1999-10-26 to
                        represent the calendar date 1999-10-26,
                        independent of how many hours this day has.-->
&lt;!ENTITY % xsd-date    "CDATA"                                       >
&lt;!--                    xsd-dateTime                               -->
&lt;!--                    dateTime represents a specific instant of
                        time. The value space of dateTime is the
                        space of Combinations of date and time of
                        day values as defined in  5.4 of [ISO
                        8601].                                     -->
&lt;!ENTITY % xsd-dateTime "CDATA"                                      >
&lt;!--                    xsd-decimal                                -->
&lt;!--                    decimal represents arbitrary precision
                        decimal numbers. The value space of
                        decimal is the set of the values i 
                        10^-n, where i and n are integers such
                        that n >= 0. The order-relation on
                        decimal is: x &lt; y iff y - x is positive.   -->
&lt;!ENTITY % xsd-decimal "CDATA"                                       >
&lt;!--                    xsd-double                                 -->
&lt;!--                    The double datatype corresponds to IEEE
                        double-precision 64-bit floating point
                        type [IEEE 754-1985]. The basic value
                        space of double consists of the values m
                         2^e, where m is an integer whose
                        absolute value is less than 2^53, and e is
                        an integer between -1075 and 970,
                        inclusive. In addition to the basic value
                        space described above, the value space
                        of double also contains the following
                        special values: positive and negative
                        zero, positive and negative infinity and
                        not-a-number. The order-relation on
                        double is: x &lt; y iff y - x is
                        positive. Positive zero is greater than
                        negative zero. Not-a-number equals itself
                        and is greater than all double values
                        including positive infinity.               -->
&lt;!ENTITY % xsd-double  "CDATA"                                       >
&lt;!--                    xsd-duration                               -->
&lt;!--                    duration represents a duration of
                        time. The value space of duration is a
                        six-dimensional space where the
                        coordinates designate the Gregorian year,
                        month, day, hour, minute, and second
                        components defined in  5.5.3.2 of [ISO
                        8601], respectively. These components are
                        ordered in their significance by their
                        order of appearance i.e. as year, month,
                        day, hour, minute, and second.             -->
&lt;!ENTITY % xsd-duration "CDATA"                                      >
&lt;!--                    xsd-float                                  -->
&lt;!--                     float corresponds to the IEEE
                        single-precision 32-bit floating point
                        type [IEEE 754-1985]. The basic value
                        space of float consists of the values m 
                        2^e, where m is an integer whose absolute
                        value is less than 2^24, and e is an
                        integer between -149 and 104,
                        inclusive. In addition to the basic value
                        space described above, the value space
                        of float also contains the following
                        special values: positive and negative
                        zero, positive and negative infinity and
                        not-a-number. The order-relation on
                        float is: x &lt; y iff y - x is
                        positive. Positive zero is greater than
                        negative zero. Not-a-number equals itself
                        and is greater than all float values
                        including positive infinity.               -->
&lt;!ENTITY % xsd-float   "CDATA"                                       >
&lt;!--                    xsd-gDay                                   -->
&lt;!--                    gDay is a gregorian day that recurs,
                        specifically a day of the month such as
                        the 5th of the month. Arbitrary recurring
                        days are not supported by this
                        datatype. The value space of gDay is the
                        space of a set of calendar dates as
                        defined in  3 of [ISO
                        8601]. Specifically, it is a set of
                        one-day long, monthly periodic instances.  -->
&lt;!ENTITY % xsd-gDay    "CDATA"                                       >
&lt;!--                    xsd-gMonth                                 -->
&lt;!--                    gMonth is a gregorian month that recurs
                        every year. The value space of gMonth is
                        the space of a set of calendar months as
                        defined in  3 of [ISO
                        8601]. Specifically, it is a set of
                        one-month long, yearly periodic instances. -->
&lt;!ENTITY % xsd-gMonth  "CDATA"                                       >
&lt;!--                    xsd-gMonthDay                              -->
&lt;!--                    gMonthDay is a gregorian date that recurs,
                        specifically a day of the year such as the
                        third of May. Arbitrary recurring dates
                        are not supported by this datatype. The
                        value space of gMonthDay is the set of
                        calendar dates, as defined in  3 of [ISO
                        8601]. Specifically, it is a set of
                        one-day long, annually periodic instances. -->
&lt;!ENTITY % xsd-gMonthDay
                       "CDATA"                                       >
&lt;!--                    xsd-gYear                                  -->
&lt;!--                    gYear represents a gregorian calendar
                        year. The value space of gYear is the
                        set of Gregorian calendar years as defined
                        in  5.2.1 of [ISO 8601]. Specifically, it
                        is a set of one-year long, non-periodic
                        instances e.g. lexical 1999 to represent
                        the whole year 1999, independent of how
                        many months and days this year has.        -->
&lt;!ENTITY % xsd-gYear   "CDATA"                                       >
&lt;!--                    xsd-gYearMonth                             -->
&lt;!--                    gYearMonth represents a specific gregorian
                        month in a specific gregorian year. The
                        value space of gYearMonth is the set of
                        Gregorian calendar months as defined in 
                        5.2.1 of [ISO 8601]. Specifically, it is a
                        set of one-month long, non-periodic
                        instances e.g. 1999-10 to represent the
                        whole month of 1999-10, independent of how
                        many days this month has.                  -->
&lt;!ENTITY % xsd-gYearMonth
                       "CDATA"                                       >
&lt;!--                    xsd-hexBinary                              -->
&lt;!--                    hexBinary represents arbitrary hex-encoded
                        binary data. The value space of
                        hexBinary is the set of finite-length
                        sequences of binary octets.                -->
&lt;!ENTITY % xsd-hexBinary
                       "CDATA"                                       >
&lt;!--                    xsd-int                                    -->
&lt;!--                    int is derived from long by setting the
                        value of maxInclusive to be 2147483647
                        and minInclusive to be -2147483648. The
                        base type of int is long.                -->
&lt;!ENTITY % xsd-int     "CDATA"                                       >
&lt;!--                    xsd-integer                                -->
&lt;!--                    integer is derived from decimal by
                        fixing the value of fractionDigits to be
                        0. This results in the standard
                        mathematical concept of the integer
                        numbers. The value space of integer is
                        the infinite set
                        {...,-2,-1,0,1,2,...}. The base type of
                        integer is decimal.                        -->
&lt;!ENTITY % xsd-integer "CDATA"                                       >
&lt;!--                    xsd-language                               -->
&lt;!--                    language represents natural language
                        identifiers as defined by [RFC 1766]. The
                        value space of language is the set of
                        all strings that are valid language
                        identifiers as defined in the language
                        identification section of [XML 1.0 (Second
                        Edition)]. The lexical space of language
                        is the set of all strings that are valid
                        language identifiers as defined in the
                        language identification section of [XML
                        1.0 (Second Edition)]. The base type of
                        language is token.                         -->
&lt;!ENTITY % xsd-language "CDATA"                                      >
&lt;!--                    xsd-long                                   -->
&lt;!--                    long is derived from integer by setting
                        the value of maxInclusive to be
                        9223372036854775807 and minInclusive to
                        be -9223372036854775808. The base type
                        of long is integer.                        -->
&lt;!ENTITY % xsd-long    "CDATA"                                       >
&lt;!--                    xsd-negativeInteger                        -->
&lt;!--                    nonNegativeInteger is derived from
                        integer by setting the value of
                        minInclusive to be 0. This results in
                        the standard mathematical concept of the
                        non-negative integers. The value space
                        of nonNegativeInteger is the infinite set
                        {0,1,2,...}. The base type of
                        nonNegativeInteger is integer.             -->
&lt;!ENTITY % xsd-negativeInteger
                       "CDATA"                                       >
&lt;!--                    xsd-nonNegativeInteger                     -->
&lt;!--                    nonNegativeInteger is derived from
                        integer by setting the value of
                        minInclusive to be 0. This results in
                        the standard mathematical concept of the
                        non-negative integers. The value space
                        of nonNegativeInteger is the infinite set
                        {0,1,2,...}. The base type of
                        nonNegativeInteger is integer.             -->
&lt;!ENTITY % xsd-nonNegativeInteger
                       "CDATA"                                       >
&lt;!--                    xsd-nonPositiveInteger                     -->
&lt;!--                    nonPositiveInteger is derived from
                        integer by setting the value of
                        maxInclusive to be 0. This results in
                        the standard mathematical concept of the
                        non-positive integers. The value space
                        of nonPositiveInteger is the infinite set
                        {...,-2,-1,0}. The base type of
                        nonPositiveInteger is integer.             -->
&lt;!ENTITY % xsd-nonPositiveInteger
                       "CDATA"                                       >
&lt;!--                    xsd-normalizedString                       -->
&lt;!--                    normalizedString represents white space
                        normalized strings. The value space of
                        normalizedString is the set of strings
                        that do not contain the carriage return
                        (#xD), line feed (#xA) nor tab (#x9)
                        characters. The lexical space of
                        normalizedString is the set of strings
                        that do not contain the carriage return
                        (#xD) nor tab (#x9) characters. The base
                        type of normalizedString is string.       -->
&lt;!ENTITY % xsd-normalizedString
                       "CDATA"                                       >
&lt;!--                    xsd-positiveInteger                        -->
&lt;!--                    positiveInteger is derived from
                        nonNegativeInteger by setting the value of
                        minInclusive to be 1. This results in
                        the standard mathematical concept of the
                        positive integer numbers. The value
                        space of positiveInteger is the infinite
                        set {1,2,...}. The base type of
                        positiveInteger is nonNegativeInteger.     -->
&lt;!ENTITY % xsd-positiveInteger
                       "CDATA"                                       >
&lt;!--                    xsd-short                                  -->
&lt;!--                    short is derived from int by setting the
                        value of maxInclusive to be 32767 and
                        minInclusive to be -32768. The base
                        type of short is int.                     -->
&lt;!ENTITY % xsd-short   "CDATA"                                       >
&lt;!--                    xsd-string                                 -->
&lt;!--                    The string datatype represents character
                        strings in XML. The value space of
                        string is the set of finite-length
                        sequences of characters (as defined in
                        [XML 1.0 (Second Edition)]) that match
                        the Char production from [XML 1.0 (Second
                        Edition)]. A character is an atomic unit
                        of communication; it is not further
                        specified except to note that every
                        character has a corresponding Universal
                        Character Set code point, which is an
                        integer.                                   -->
&lt;!ENTITY % xsd-string  "CDATA"                                       >
&lt;!--                    xsd-time                                   -->
&lt;!--                    time represents an instant of time that
                        recurs every day. The value space of
                        time is the space of time of day values as
                        defined in  5.3 of [ISO
                        8601]. Specifically, it is a set of
                        zero-duration daily time instances.        -->
&lt;!ENTITY % xsd-time    "CDATA"                                       >
&lt;!--                    xsd-token                                  -->
&lt;!--                    token represents tokenized strings. The
                        value space of token is the set of
                        strings that do not contain the line feed
                        (#xA) nor tab (#x9) characters, that have
                        no leading or trailing spaces (#x20) and
                        that have no internal sequences of two or
                        more spaces. The lexical space of token
                        is the set of strings that do not contain
                        the line feed (#xA) nor tab (#x9)
                        characters, that have no leading or
                        trailing spaces (#x20) and that have no
                        internal sequences of two or more
                        spaces. The base type of token is
                        normalizedString.                          -->
&lt;!ENTITY % xsd-token   "CDATA"                                       >
&lt;!--                    xsd-unsignedByte                           -->
&lt;!--                    unsignedByte is derived from
                        unsignedShort by setting the value of
                        maxInclusive to be 255. The base type
                        of unsignedByte is unsignedShort.          -->
&lt;!ENTITY % xsd-unsignedByte
                       "CDATA"                                       >
&lt;!--                    xsd-unsignedInt                            -->
&lt;!--                    unsignedInt is derived from unsignedLong
                        by setting the value of maxInclusive to
                        be 4294967295. The base type of
                        unsignedInt is unsignedLong.               -->
&lt;!ENTITY % xsd-unsignedInt
                       "CDATA"                                       >
&lt;!--                    xsd-unsignedLong                           -->
&lt;!--                    unsignedLong is derived from
                        nonNegativeInteger by setting the value of
                        maxInclusive to be
                        18446744073709551615. The base type of
                        unsignedLong is nonNegativeInteger.        -->
&lt;!ENTITY % xsd-unsignedLong
                       "CDATA"                                       >
&lt;!--                    xsd-unsignedShort                          -->
&lt;!--                    unsignedShort is derived from
                        unsignedInt by setting the value of
                        maxInclusive to be 65535. The base
                        type of unsignedShort is unsignedInt.     -->
&lt;!ENTITY % xsd-unsignedShort
                       "CDATA"                                       >
      </xsl:text>
    </xsl:template>
</xsl:stylesheet>

