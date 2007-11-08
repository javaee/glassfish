<?xml version="1.0" ?>

<!-- 
   Copyright (c) 2002 by Sun Microsystems, Inc. All Rights Reserved. 
   Use is subject to license terms. 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	        xmlns:fo="http://www.w3.org/1999/XSL/Format"
		version="1.0" >
    <xsl:output method="text" indent="yes"/>
    
    <xsl:template match="/">
	<xsl:apply-templates />
    </xsl:template>

    <xsl:template match="static-verification">

	---------------------------
        STATIC VERIFICATION RESULTS
	---------------------------
	<xsl:apply-templates select="failure-count" />
        <xsl:apply-templates select="application" />
        <xsl:apply-templates select="appclient" />
        <xsl:apply-templates select="ejb" />
	<xsl:apply-templates select="web" />
	<xsl:apply-templates select="other" />
        <xsl:apply-templates select="connector" />
        <xsl:apply-templates select="error" />
	

    </xsl:template>

    <xsl:template match="application">
	
	-------------------------------------
	RESULTS FOR APPLICATION-RELATED TESTS
	-------------------------------------

        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>
    
    <xsl:template match="appclient">

	-----------------------------------
        RESULTS FOR APPCLIENT-RELATED TESTS
	-----------------------------------

        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>

    <xsl:template match="ejb">
        
	-----------------------------
	RESULTS FOR EJB-RELATED TESTS
	-----------------------------
	
        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>

    <xsl:template match="web">

	-----------------------------
	RESULTS FOR WEB-RELATED TESTS
	-----------------------------

        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>

    <xsl:template match="other">

	-----------------------------------
	RESULTS FOR OTHER XML-RELATED TESTS
	-----------------------------------

        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>

    <xsl:template match="connector">

	-----------------------------------
	RESULTS FOR CONNECTOR-RELATED TESTS
	-----------------------------------

        <xsl:apply-templates select="failed" />
        <xsl:apply-templates select="passed" />
        <xsl:apply-templates select="warning" />
	<xsl:apply-templates select="not-applicable" />
    </xsl:template>

    

    <xsl:template match="error">
	
	-----------------------------------------------------
	ERRORS THAT OCCURED WHILE RUNNING STATIC VERIFICATION
	----------------------------------------------------- 

	Error Name : <xsl:value-of select="error-name" />
        Error Description : <xsl:value-of select="error-description" />
	
    </xsl:template>

    <xsl:template match="failure-count">

	----------------------------------
	NUMBER OF FAILURES/WARNINGS/ERRORS
	----------------------------------

	# of Failures : <xsl:value-of select="failure-number" />
        # of Warnings : <xsl:value-of select="warning-number" />
	# of Errors : <xsl:value-of select="error-number" />
	
    </xsl:template>

    <xsl:template match="failed">

	--------------
	FAILED TESTS : 
	--------------
	<xsl:for-each select="test" >

	 Test Name : <xsl:value-of select="test-name" />
	 Test Assertion : <xsl:value-of select="test-assertion" />
	 Test Description : <xsl:value-of select="test-description" />
	
	</xsl:for-each>

    </xsl:template>

    <xsl:template match="passed">

	---------------
	PASSED TESTS :
	---------------

	<xsl:for-each select="test" >

	 Test Name : <xsl:value-of select="test-name" />
	 Test Assertion : <xsl:value-of select="test-assertion" />
	 Test Description : <xsl:value-of select="test-description" />

	</xsl:for-each>

    </xsl:template>

    <xsl:template match="warning">

	-----------
	WARNINGS :
	-----------

	<xsl:for-each select="test" >

	 Test Name : <xsl:value-of select="test-name" />
	 Test Assertion : <xsl:value-of select="test-assertion" />
	 Test Description : <xsl:value-of select="test-description" />

	</xsl:for-each>

    </xsl:template>

	<xsl:template match="not-applicable">

	---------------------
	NOTAPPLICABLE TESTS :
	---------------------

	<xsl:for-each select="test" >

	 Test Name : <xsl:value-of select="test-name" />
	 Test Assertion : <xsl:value-of select="test-assertion" />
	 Test Description : <xsl:value-of select="test-description" />

	</xsl:for-each>

    </xsl:template>

    
</xsl:stylesheet>
