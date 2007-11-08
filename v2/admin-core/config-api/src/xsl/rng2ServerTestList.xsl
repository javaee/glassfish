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
 

    <xsl:template match="rng:grammar">

    <!-- We're only interested in elements, and their attributes. We
    use the elements as the drives, deriving the name of the
    corresponding collections of attributes from the element name
    -->
<xsl:text> &#10;</xsl:text>
<xsl:comment>
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
</xsl:comment>
<xsl:text>&#10; &#10;</xsl:text>
    <elements>
      <xsl:apply-templates select="rng:define/rng:element">
	<xsl:sort select="@name"/>
      </xsl:apply-templates>
    </elements>
  </xsl:template>

  <!--
    For each element we need to construct an output element with the following attributes:
    name - the name of the element in the dtd
    test-name - a camel case Java name (this is used as the default class to load)
    custom-class - a validation class to be loaded for certain elements
    
    
     - the name of the element (if any) which is the primary key for the element

    Only attributes can be children of the element
  -->
  <xsl:template match="rng:element">
    <xsl:variable name="n" select="normalize-space(string(@name))"/>
    <element name="{$n}">
      <xsl:if test="./@ias:exclusive-children-list">
          <xsl:attribute name="exclusive-list"><xsl:value-of select="./@ias:exclusive-children-list"/></xsl:attribute>
      </xsl:if>
      <xsl:variable name="keyattrname" select="//rng:define[@name=concat(current()/@name,'-attlist')]//rng:attribute[@ias:type='key']/@name" />
      <xsl:if test="$keyattrname">
          <xsl:attribute name="key"><xsl:value-of select="$keyattrname"/></xsl:attribute>
      </xsl:if>
      
      <xsl:call-template name="add-subelements-lists"/>
      
      <xsl:apply-templates select="//rng:define[@name=concat(current()/@name,'-attlist')]//rng:attribute">
	<xsl:sort select="@name"/>
      </xsl:apply-templates>
    </element>
  </xsl:template>

  <xsl:template name="add-subelements-lists">
           <xsl:variable name="required-list">
                <xsl:call-template name="get-required-subelements-lists">
                    <xsl:with-param name="elem-name" >
                         <xsl:value-of select="@name"/>
                    </xsl:with-param>
                </xsl:call-template>   
           </xsl:variable>
       <xsl:if test="string($required-list)" >
<!--           <sub-elements>  -->
               <xsl:if test="string($required-list)" >
                 <xsl:attribute name="required-children">
                    <xsl:value-of select="substring($required-list,2)"/>
                 </xsl:attribute>
               </xsl:if>
<!--           </sub-elements> -->
       </xsl:if>    
  </xsl:template>
  <xsl:template name="get-required-subelements-lists">
       <xsl:param name="elem-name" />
       <xsl:for-each select="./rng:ref" >
            <xsl:if test="not(@name=concat($elem-name,'-attlist'))">
               <xsl:value-of select="concat(',',@name)"/>
            </xsl:if>
        </xsl:for-each>
       <xsl:for-each select="./rng:oneOrMore/rng:ref" >
            <xsl:value-of select="concat(concat(',',@name),'*')"/>
        </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="rng:attribute[parent::rng:optional]">
    <optional-attribute name="{@name}">
      <xsl:call-template name="make-attr" />
    </optional-attribute>
  </xsl:template>

  <xsl:template match="rng:attribute">
    <attribute name="{@name}">
      <xsl:call-template name="make-attr" />
    </attribute>
  </xsl:template> 
  
  <xsl:template name="make-attr">
      <xsl:variable name="explicit-type" select="rng:ref | rng:data | rng:choice"/>
      <xsl:choose>
      <xsl:when test="$explicit-type">
	<xsl:apply-templates select="$explicit-type"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:attribute name="type">string</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="copy-ias-attrs" />
  </xsl:template>
  
  <xsl:template name="copy-ias-attrs">
      <xsl:variable name="belongs-to"  select="@ias:belongs-to"/>
      <xsl:if test="$belongs-to" >
         <xsl:attribute name="belongs-to">
             <xsl:value-of select="$belongs-to" />
         </xsl:attribute>
      </xsl:if>   
      <xsl:variable name="references-to"  select="@ias:references-to"/>
      <xsl:if test="$references-to" >
           <xsl:attribute name="references-to">
               <xsl:value-of select="$references-to" />
           </xsl:attribute>
      </xsl:if>   
      <xsl:variable name="le-than"  select="@ias:le-than"/>
      <xsl:if test="$le-than" >
           <xsl:attribute name="le-than">
               <xsl:value-of select="$le-than" />
           </xsl:attribute>
      </xsl:if>   
      <xsl:variable name="ge-than"  select="@ias:ge-than"/>
      <xsl:if test="$ge-than" >
           <xsl:attribute name="ge-than">
               <xsl:value-of select="$ge-than" />
           </xsl:attribute>
      </xsl:if>   
      <xsl:variable name="gt-than"  select="@ias:gt-than"/>
      <xsl:if test="$gt-than" >
           <xsl:attribute name="gt-than">
               <xsl:value-of select="$gt-than" />
           </xsl:attribute>
      </xsl:if>   
      <xsl:variable name="ls-than"  select="@ias:ls-than"/>
      <xsl:if test="$ls-than" >
           <xsl:attribute name="ls-than">
               <xsl:value-of select="$ls-than" />
           </xsl:attribute>
      </xsl:if>   
  </xsl:template>
  
  
  
  <xsl:template match="rng:ref">
    <xsl:apply-templates select="//rng:define[@name=current()/@name]"/>
  </xsl:template>

  <xsl:template match="rng:define">
    <xsl:apply-templates select="rng:*"/>
  </xsl:template>

  <xsl:template match="rng:define[@name='file-type']">
    <xsl:attribute name="type">file</xsl:attribute>
  </xsl:template>

  <xsl:template match="rng:define[@name='classname-type']">
    <xsl:attribute name="type">classname</xsl:attribute>
  </xsl:template>

  <xsl:template match="rng:define[@name='IPAddress-type']">
    <xsl:attribute name="type">address</xsl:attribute>
  </xsl:template>

<!--  
  <xsl:template match="rng:define[@name='jndi-unique-type']">
    <xsl:attribute name="type">jndi-unique</xsl:attribute>
  </xsl:template>
-->

  <xsl:template match="rng:data">
    <xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
    <xsl:apply-templates select="rng:param"/>
  </xsl:template>

  <xsl:template match="rng:data[@type='nonNegativeInteger']">
    <xsl:call-template name="make-integer-type"/>
  </xsl:template>

  <xsl:template match="rng:data[@type='positiveInteger']">
    <xsl:call-template name="make-integer-type">
      <xsl:with-param name="lowest">1</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="rng:data[@type='integer']">
    <xsl:call-template name="make-integer-type">
      <xsl:with-param name="lowest">
          <xsl:value-of select="param[@name='minInclusive']"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="make-integer-type">
    <xsl:param name="lowest">0</xsl:param>
    <xsl:attribute name='type'>integer</xsl:attribute>
    <xsl:attribute name='range'>
      <xsl:call-template name='get-lower-bound'>
	<xsl:with-param name='lowest' select="$lowest"/>
	<xsl:with-param name="selector" select="rng:param[starts-with(@name, 'min')]"/>
      </xsl:call-template>
      <xsl:text>,</xsl:text>
      <xsl:call-template name='get-upper-bound'>
	<xsl:with-param name="selector" select="rng:param[starts-with(@name, 'max')]"/>
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name='get-lower-bound'>
    <xsl:param name="lowest">0</xsl:param>
    <xsl:param name="selector"/>
    <xsl:choose>
      <xsl:when test="not($selector)">
	<xsl:value-of select="$lowest"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="min">
	  <xsl:with-param name="p1" select="$lowest"/>
	  <xsl:with-param name="p2"><xsl:apply-templates select="$selector"/></xsl:with-param>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name='get-upper-bound'>
    <xsl:param name="selector"/>
    <xsl:choose>
      <xsl:when test="not($selector)">NA</xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="$selector"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="min">
    <xsl:param name="p1"/>
    <xsl:param name="p2"/>
    <xsl:choose>
      <xsl:when test="$p1 and $p1 >= $p2">
	<xsl:value-of select="number($p1)"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="number($p2)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:param[@name='minInclusive' or @name='maxInclusive']">
    <xsl:value-of select="number(.)"/>
  </xsl:template>

  <xsl:template match="rng:param[@name='minExclusive']">
    <xsl:value-of select="ceiling(.)"/>
  </xsl:template>

  <xsl:template match="rng:param[@name='maxExclusive']">
    <xsl:value-of select="floor(.)"/>
  </xsl:template>
  
  <xsl:template match="rng:param[@name='pattern']">
    <xsl:attribute name="regex"><xsl:value-of select="string(.)"/></xsl:attribute>
  </xsl:template>


  <xsl:template match="rng:choice">
    <xsl:attribute name="type">string</xsl:attribute>
    <xsl:attribute name="enumeration">
    <xsl:apply-templates select="rng:value"/>

    </xsl:attribute>
  </xsl:template>

  <xsl:template match="rng:value">
    <xsl:if test="position() > 1">
    <xsl:text>,</xsl:text></xsl:if>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  
 <xsl:template match="text()"/>
</xsl:stylesheet>
