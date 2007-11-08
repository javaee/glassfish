<?xml version="1.0"?>
<!-- 
THis stylesheet will consume a .rng file and produce a corresponding
ServerTestList.xml file for the dynamic validation subsystem.

This stylesheet has not been designed to work with a general RELAX NG
grammar. 

-->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0"
                xmlns:rng="http://relaxng.org/ns/structure/1.0"
                xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0"
                xmlns:x="http://www.w3.org/1999/xhtml"
                xmlns:ias="http://www.sun.com/ias/validation"
		exclude-result-prefixes="rng a x sch"
                >

    <xsl:output method="xml" indent="yes"/>
 
    <xsl:template match="foo">
         <xsl:call-template name="top_comments"/>
         <xsl:apply-templates select="name-lists"/>
    </xsl:template>

    <xsl:template name="top_comments">
<xsl:for-each select="name-lists/comment()">
<xsl:comment>
<xsl:value-of select="."/>
</xsl:comment>
</xsl:for-each>
    </xsl:template>
    
    <xsl:template match="name-lists">
        <xsl:element name="name-lists">
            <xsl:for-each select="name-list" >
                <xsl:call-template name="name-domain" />
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="name-domain">
        <xsl:variable name="nm" select="@name" />
        <xsl:copy>
           <xsl:for-each select="@*" >
                <xsl:copy/>
           </xsl:for-each>
           <xsl:call-template name="forms-from" >
                <xsl:with-param name="name">
                    <xsl:value-of select="string($nm)" />
                </xsl:with-param>
           </xsl:call-template>
        </xsl:copy>
    </xsl:template>
    <xsl:template name="forms-from">
       <xsl:param name="name"/>
       <xsl:for-each select="//@ias:belongs-to" >
            <xsl:variable name="in-list">
                <xsl:call-template name="is-in-list" >
                   <xsl:with-param name="member-to-test" >
                        <xsl:value-of select="string($name)"/>
                   </xsl:with-param>
                   <xsl:with-param name="list" >
                     <xsl:value-of select="string(.)"/>
                   </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$in-list='true'" >
                <xsl:variable name="parent-element">
                    <xsl:call-template name="get-parent-element" />
                </xsl:variable>
                <xsl:call-template name="create-forms-from-element" >
                    <xsl:with-param name = "elem-name" >
                        <xsl:value-of select="string($parent-element)"/>
                    </xsl:with-param>
                    <xsl:with-param name = "accum-str" >
                        <xsl:value-of select="concat('/@',../@name)"/>
                    </xsl:with-param>
                    <xsl:with-param name = "generated-elem" >
                        <xsl:value-of select="string('forms-from')"/>
                    </xsl:with-param>
                </xsl:call-template>
           </xsl:if>
       </xsl:for-each>
       <xsl:for-each select="//@ias:references-to" >
            <xsl:variable name="in-list">
                <xsl:call-template name="is-in-list" >
                   <xsl:with-param name="member-to-test" >
                        <xsl:value-of select="string($name)"/>
                   </xsl:with-param>
                   <xsl:with-param name="list" >
                     <xsl:value-of select="string(.)"/>
                   </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$in-list='true'" >
                <xsl:variable name="parent-element">
                    <xsl:call-template name="get-parent-element" />
                </xsl:variable>
                <xsl:call-template name="create-forms-from-element" >
                    <xsl:with-param name = "elem-name" >
                        <xsl:value-of select="string($parent-element)"/>
                    </xsl:with-param>
                    <xsl:with-param name = "accum-str" >
                        <xsl:value-of select="concat('/@',../@name)"/>
                    </xsl:with-param>
                    <xsl:with-param name = "generated-elem" >
                        <xsl:value-of select="string('references-to')"/>
                    </xsl:with-param>
                </xsl:call-template>
           </xsl:if>
       </xsl:for-each>
    </xsl:template>

    <xsl:template name="get-parent-element">
      <xsl:value-of select="substring-before(concat(../../@name,../../../@name),'-attlist')" />
    </xsl:template>

    <xsl:template name="is-in-list">
      <xsl:param name = "member-to-test" />
      <xsl:param name = "list" />
                <xsl:choose>
                    <xsl:when test="contains($list,',')">
                        <xsl:choose>
                           <xsl:when test="$member-to-test=substring-before($list,',')">
                                <xsl:value-of select="'true'"/>
                           </xsl:when>
                           <xsl:otherwise>
                                <xsl:call-template name="is-in-list" >
                                   <xsl:with-param name="member-to-test" >
                                        <xsl:value-of select="string($member-to-test)"/>
                                   </xsl:with-param>
                                   <xsl:with-param name="list" >
                                     <xsl:value-of select="substring-after($list,',')"/>
                                   </xsl:with-param>
                                </xsl:call-template>
                           </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                       <xsl:if test="$member-to-test=$list">
                            <xsl:value-of select="'true'"/>
                       </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
    </xsl:template>

    <xsl:template name="create-forms-from-element">
         <xsl:param name = "elem-name" />
         <xsl:param name = "accum-str" />
         <xsl:param name = "generated-elem" />
           
         <xsl:choose> 
            <xsl:when test="$elem-name='domain'" >
                <xsl:choose>
                    <xsl:when test="$generated-elem='forms-from'">
                        <xsl:element name="forms-from">
                            <xsl:attribute name="xpath">
                                <xsl:value-of select="concat(concat('/',$elem-name),$accum-str)"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="referenced-by">
                            <xsl:attribute name="xpath">
                                <xsl:value-of select="concat(concat('/',$elem-name),$accum-str)"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
               <xsl:for-each select="//rng:define/rng:element//rng:ref[@name=$elem-name]" >
                        <xsl:call-template name="create-forms-from-element" >
                            <xsl:with-param name="elem-name" >
                                 <xsl:value-of select="./ancestor::rng:element/@name"/>
                            </xsl:with-param>
                            <xsl:with-param name="accum-str" >
                                <xsl:choose>
                                    <xsl:when test='ancestor::rng:zeroOrMore|ancestor::rng:oneOrMore'>
                                        <xsl:value-of select="concat(concat('/',concat($elem-name,'[*]')),$accum-str)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="concat(concat('/',$elem-name),$accum-str)"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="generated-elem" >
                                 <xsl:value-of select="string($generated-elem)"/>
                            </xsl:with-param>
                        </xsl:call-template>
               </xsl:for-each>
            </xsl:otherwise>
         </xsl:choose> 
            
    </xsl:template>
</xsl:stylesheet>
