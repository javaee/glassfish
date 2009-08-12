package com.sun.ejte.ccl.reporter; 
/**
 * @Class : Reporter 
   @Description : Main class used for Uniform reporting of results
   @Author : Ramesh Mandava
   @Last Modified : Initial Creation by Ramesh Mandava after taking input from 
	Jeanfrancois and other Team members
   @Last Modified : By Ramesh on 1/20/2002 , Added code to use new testIdVector and 
		testCaseIdVector for  preserving order of entry of them and now <tests>
		element is added around multiple tests
   @Last Modified : By Ramesh on 4/5/2002, Taken care of machine name unavailability
		under J2EE. And allowed having . in the path of result file

 *              
 */


import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Calendar;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;



public class Reporter extends Thread implements Serializable {

    /* FIELDS */


    private static Reporter reporterInstance = null;

    private String resultFile="default.xml";

    private FileOutputStream fout;
    private static String ws_home="sqe-pe";   
    transient public PrintWriter out = new PrintWriter(System.out);
    private boolean hasBeenPrinted = false;
    /** testSuiteHash is a Hashtable holding info about different testsuites. */
    private static Hashtable testSuiteHash = new Hashtable();
    

    /** 
	This method is used for setting the TestSuite Info 
     */

    public void setTestSuite( String id, String name, String description )
    {

	TestSuite myTestSuite =  (TestSuite)testSuiteHash.get( id.trim() ); 
	if ( myTestSuite == null )
	{
		myTestSuite = new TestSuite( id, name,description);
		testSuiteHash.put( id.trim(), myTestSuite );
	}
    }

    /** 
	This method is used for setting the TestSuite Info 
     */
    public void setTestSuite( String id, String name)
    {
	TestSuite myTestSuite =  (TestSuite)testSuiteHash.get( id.trim() ); 
	if ( myTestSuite == null )
	{
		myTestSuite = new TestSuite( id.trim(), name);
		testSuiteHash.put( id.trim(), myTestSuite );
	}
    }

    /** 
	This method is used for setting the TestSuite Info 
     */
    public void setTestSuite( String id)
    {
	System.err.println("setTestSuite with id -> " + id );
	TestSuite myTestSuite =  (TestSuite)testSuiteHash.get( id.trim() ); 
	if ( myTestSuite == null )
	{
		myTestSuite = new TestSuite( id.trim());
		testSuiteHash.put( id.trim(), myTestSuite );
	}
    }

    /** 
	After setting Test Suite info. suing setTestSuite, We need to use this addTest method 
      for adding information about particular Test. We need to  pass both TestSuiteId and
      TestId  along with othe info of Test
     */
    public void addTest( String testSuiteId,  String testId, String testName, String testDescription )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}

	Test myTest = new Test( testId, testName, testDescription );
	myTestSuite.addTest( myTest );
    }

    /** 
	After setting Test Suite info. suing setTestSuite, We need to use this addTest method 
      for adding information about particular Test. We need to  pass both TestSuiteId and
      TestId  along with othe info of Test
     */
    public void addTest( String testSuiteId,  String testId, String testName )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}

	Test myTest = new Test( testId, testName );
	myTestSuite.addTest( myTest );
    }

    /** 
	After using setthing Test Suite info. We need to use this addTest method 
      for adding information about particular Test. We need to  pass both TestSuiteId and
      TestId  
     */
    public void addTest( String testSuiteId,  String testId )
    {
	System.out.println("addTest with testSuiteId:: testId -> " + testSuiteId + "::" + testId );
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}

	Test myTest = new Test( testId );
	myTestSuite.addTest( myTest );
    }


     /**
        After adding a Test using addTest , We need to use this setTestStatus method
      for setting Test status(pass/fail) information about particular Test. We need to  pass both TestSuiteId and TestId  al
ong with expected and actual result. This is optional as in some case
     only TestCases will have status
     */
    public void setTestStatus ( String testSuiteId, String testId, String status, String expected, String actual )
    {
        TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );


        if ( myTestSuite== null )
        {
                System.err.println("ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
                System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
                return;
        }

        Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
        if ( myTest == null )
        {
                System.err.println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
                System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId.trim() + "::" + testId.trim() );
                return;
        }
        myTest.setStatus( status);
        myTest.setExpected( expected);
        myTest.setActual( actual);

     }

    /** 
	After adding a Test using addTest , We need to use this setTestStatus method 
      for setting Test status(pass/fail) information about particular Test. We need to  pass both TestSuiteId and TestId  along with status information. This is optional as in some case
     only TestCases will have status
     */
    public void setTestStatus ( String testSuiteId, String testId, String status, String statusDescription )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );

	
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}

	Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId.trim() + "::" + testId.trim() );
		return;
	}
	myTest.setStatus( status);
	myTest.setStatusDescription( statusDescription);

     }

    /** 
	After adding a Test using addTest , We need to use this setTestStatus method 
      for setting Test status(pass/fail) information about particular Test. We need to  pass both TestSuiteId and TestId  along with status information. This is optional as in some case
     only TestCases will have status
     */
    public void setTestStatus ( String testSuiteId, String testId, String status )
    {
	System.out.println("setTestStatus testSuiteId::testId::status -> " + testSuiteId + "::" + testId + "::" + status );
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}

	Test myTest =(Test) myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId.trim() + "::" + testId.trim() );
		return;
	}
	myTest.setStatus( status);
     }

    /** 
	After adding a Test using addTest, We need to use this addTestCase method 
      for adding information about particular TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase( String testSuiteId,  String testId , String testCaseId)
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id -> " + testSuiteId.trim() );
		return;
	}
	Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId.trim() + "::" + testId.trim() );
		return;
	}

	TestCase myTestCase = new TestCase( testCaseId.trim() );
	myTest.addTestCase ( myTestCase );
    }

    /** 
	After adding a Test using addTest, We need to use this addTestCase method 
      for adding information about particular TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase( String testSuiteId,  String testId , String testCaseId, String testCaseName )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
		return;
	}
	Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
		System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId.trim() + "::" + testId.trim() );
		return;
	}

	TestCase myTestCase = new TestCase( testCaseId.trim(), testCaseName );
	myTest.addTestCase ( myTestCase );
    }

    /** 
	After adding a Test using addTest, We need to use this addTestCase method 
      for adding information about particular TestCase corresponding to that Test. We need to  pass TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase( String testSuiteId,  String testId , String testCaseId, String testCaseName , String testCaseDescription )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
		return;
	}
	Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
		return;
	}

	TestCase myTestCase = new TestCase( testCaseId.trim(), testCaseName, testCaseDescription );
	myTest.addTestCase ( myTestCase );
    }

	

     /**
        After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method
      for setting TestCase status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and
 TestCaseId  along with status information. We pass expected and actual information along with pass/fail here
     */
    public void setTestCaseStatus ( String testSuiteId, String testId, String testCaseId,  String status, String expected, String actual )
    {
        TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
        if ( myTestSuite== null )
        {
                System.err.println("ERROR:setTestCaseStatus might have called without setTestSuite. PENDING : Shall we throwException?");
                return;
        }

        Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
        if ( myTest == null )
        {
                System.err.println("ERROR:setTestCaseStatus might have called without addTest. PENDING : Shall we throw Exception?");
                return;
        }
        TestCase myTestCase =(TestCase) myTest.getTestCaseHash().get( testCaseId.trim() );
        if ( myTestCase == null )
        {
                System.err.println("ERROR:setTestCaseStatus might have called without addTestCase. PENDING : Shall we throwException?");
                return;
        }
        myTestCase.setStatus( status);
        myTestCase.setExpected( expected);
        myTestCase.setActual( actual);

     }


    /** 
	After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method 
      for setting TestCase status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId  along with status information.
     Each TestCase will have status
     */
    public void setTestCaseStatus ( String testSuiteId, String testId, String testCaseId,  String status, String statusDescription )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
		return;
	}

	Test myTest = (Test)myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without addTest. PENDING : Shall we throw Exception?");
		return;
	}
	TestCase myTestCase =(TestCase) myTest.getTestCaseHash().get( testCaseId.trim() );
	if ( myTestCase == null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without addTestCase. PENDING : Shall we throw Exception?");
		return;
	}
	myTestCase.setStatus( status);
	myTestCase.setStatusDescription( statusDescription);

     }


    /** 
	After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method 
      for setting TestCase status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId  along with status information.
     only TestCases will have status
     */
    public void setTestCaseStatus ( String testSuiteId, String testId, String testCaseId,  String status )
    {
	TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId.trim() );
	if ( myTestSuite== null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
		return;
	}

	Test myTest =(Test) myTestSuite.getTestHash().get( testId.trim() );
	if ( myTest == null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without addTest. PENDING : Shall we throw Exception?");
		return;
	}
	TestCase myTestCase = (TestCase)myTest.getTestCaseHash().get( testCaseId.trim() );
	if ( myTestCase == null )
	{
		System.err.println("ERROR:setTestCaseStatus might have called without addTestCase. PENDING : Shall we throw Exception?");
		return;
	}
	myTestCase.setStatus( status);

     }
    
    
    /**
     * Change done on 7/10/02 by Deepa Singh
     * Now Reporter will by default create results file in $EJTE_HOME if no results file is specified
     * So no need to pass the environment variable.
     * Reporter.getInstance should create test_results.xml at j2ee-test/
     * 
     */

    
    public static Reporter getInstance( )
    {
	    if ( reporterInstance == null )
	    {
		    //reporterInstance = new Reporter( );
		    String rootpath=new File(".").getAbsolutePath();
		    String ejte_home=rootpath.substring(0,rootpath.indexOf(ws_home));
		    //ejte_home contains OS dependent path separator character without j2ee-test
		    String outputDir=ejte_home+ws_home;
		    reporterInstance = Reporter.getInstance( outputDir + File.separatorChar + "test_results.xml" );
	    }

	    return reporterInstance;

    }
    
    public static Reporter getInstance(String wshome)
    {
        if(reporterInstance == null)
        {
            String rootpath = (new File(".")).getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(wshome));
            String outputDir = ejte_home + wshome;
            reporterInstance = new Reporter(outputDir + File.separatorChar + "test_results.xml");
        }
        return reporterInstance;
    }

      public static Reporter getInstance( String resultFilePath,boolean pathSpecified )
      {
	   if ( reporterInstance == null )
	   {
		reporterInstance = new Reporter(resultFilePath );
		
	   }
	   return reporterInstance;
       }
      
      //done only for the webtests.
    
    /*public static Reporter getInstance(String wshome) {
        if ( reporterInstance == null ) {
            String rootpath=new File(".").getAbsolutePath();
            //System.out.println("wshome from param is :"+wshome);
            //System.out.println("ROOT IS"+rootpath);
            String ejte_home=rootpath.substring(0,rootpath.indexOf(wshome));
            //ejte_home contains OS dependent path separator character without j2ee-test
            String outputDir=ejte_home+wshome;
            reporterInstance = new Reporter(outputDir + File.separatorChar + "test_results.xml");
            //reporterInstance = Reporter.getInstance( outputDir + File.separatorChar + "test_results.xml" );
            //reporterInstance = new Reporter(resultFilePath );
            
        }
        return reporterInstance;
    }*/


    public static Reporter getInstance( OutputStream outStream  )
      {
	   if ( reporterInstance == null )
	   {
		reporterInstance = new Reporter(outStream );
		
	   }
	   return reporterInstance;
       } 

      public static Reporter getInstance( Writer writer  )
      {
	   if ( reporterInstance == null )
	   {
		
		reporterInstance = new Reporter(writer );
		
	   }
	   return reporterInstance;
       } 
    /* CONSTRUCTORS */

     private Reporter( )
     {
      try{
          Runtime.getRuntime().addShutdownHook(this);
      } catch (java.lang.Exception ex){
            //System.out.println("WARNING: Reporter hook thread not created. No XML file will be produced.");
            System.out.println(ex.getMessage());
      }
    }

     private Reporter( String resultFilePath )
     {
      try{
	   resultFile= resultFilePath;

          Runtime.getRuntime().addShutdownHook(this);
      } catch (java.lang.Exception ex){
            //System.out.println("WARNING: Reporter hook thread not created. No XML file will be produced.");
      }
    }
 
   

    private Reporter (OutputStream o) {
	this();
	out = new PrintWriter(o);
    }


    /* constructor for JSPs:
     * the "out" field in a JSP is a JSPWriter
     * which extends PrintWriter.    */

    private Reporter (Writer w) {
	this();
	out = new PrintWriter(w);
    }



 public void generateValidReport( )
 {

//     System.out.println("REPORTER\t Inside generateValidReport");
     FileChannel rChannel = null;
     FileChannel wChannel = null;
     try
     {

	//Now flush all the TestSuite info

	//System.out.println("REPORTER\t generateValidReport.flushAll");
         flushAll();

	String oFileName =null;
	if ( resultFile.lastIndexOf(".") > 0 )   
	{
		oFileName = resultFile.substring(0, resultFile.lastIndexOf(".") ) + "Valid.xml";
	}
	else
	{
		oFileName = resultFile + "Valid.xml";
	}
        wChannel = new FileOutputStream(oFileName).getChannel();

	String osName = System.getProperty("os.name");
	String osVersion = System.getProperty("os.version");
	String currentDate = (new Date()).toString();
	
	String extraXML = "<report> <date> " + currentDate + "</date><configuration>";
	extraXML += "<os>" + osName + osVersion + "</os>";
	extraXML += "<jdkVersion>" + System.getProperty("java.version") + "</jdkVersion>";

	String machineName = "unavailable";
        InputStream in = null;
	try {

	    in  = Runtime.getRuntime().exec("uname -n").getInputStream();
	    byte[] myBytes = new byte[200];
	    in.read(myBytes);
	    machineName = new String( myBytes ).trim();
	}
	catch ( Exception me ) {
	} finally {
            close(in);
        }
	
	extraXML += "<machineName>" + machineName + "</machineName>";
	extraXML += "</configuration> <testsuites>";	

	wChannel.write(ByteBuffer.wrap(extraXML.getBytes()));

        rChannel = new FileInputStream(resultFile).getChannel();
        wChannel.transferFrom(rChannel, wChannel.position(), rChannel.size());
        wChannel.position(wChannel.position() + rChannel.size());

	wChannel.write(ByteBuffer.wrap("</testsuites>\n</report>\n".getBytes()));
        //System.out.println("REPORTER\t File validation complete");

	}
	catch ( Exception e )
	{
		System.out.println("ERROR : " + e );
	} finally {
            close(rChannel);
            close(wChannel);
        }
   }

   public void flushAll ( )
   {
//	System.out.println("REPORTER\t inside flushAll") ;
       InputStream in = null;
       FileOutputStream foutput = null;
       try
	{
		Enumeration testSuiteEnum = testSuiteHash.keys();

		if ( resultFile.equals("default.xml") )
		{
			in  = Runtime.getRuntime().exec("uname -n").getInputStream();
			byte[] myBytes = new byte[200];
			in.read(myBytes);
			String myResultFile = "result_";
			String machineName = new String( myBytes ).trim();
			myResultFile += machineName;

			Calendar  myCalendar  = Calendar.getInstance(); 
			String month = new Integer( myCalendar.get ( Calendar.MONTH)).toString();
			String day = new Integer(myCalendar.get ( Calendar.DAY_OF_MONTH)).toString();
			String year = new Integer(myCalendar.get ( Calendar.YEAR)).toString();
		 	myResultFile += "_" + month + day + year + ".xml";
			resultFile= myResultFile;	

		}

		foutput = new FileOutputStream( resultFile, true );
		while ( testSuiteEnum.hasMoreElements( ) )
		{
			String testSuiteId = (String) testSuiteEnum.nextElement();
			flush( testSuiteId, foutput );
		}

		System.out.println("in flushAll , creating new testSuiteHash");
		// Now take out the TestSuite info from memory
		testSuiteHash = new Hashtable();
	}
	catch ( Exception e )
	{
		System.err.println("ERROR: " + e );
	} finally {
            close(in);
            close(foutput);
        }


   }

     /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite. 
     * @param testSuiteName the test suite's name.
     * @return returns true if the file is succesfully created
     */

     
    public boolean flush(String testSuiteId)
    {
        //System.out.println("REPORTER\t flush(testsuiteID");
	boolean returnVal=false;
        InputStream in = null;
        FileOutputStream foutput = null;
    	try
        {

		 if ( resultFile.equals("default.xml") )
                {
                        in  = Runtime.getRuntime().exec("uname -n").getInputStream();
                        byte[] myBytes = new byte[200];
                        in.read(myBytes);
                        String myResultFile = "result_";
                        String machineName = new String( myBytes ).trim();
                        myResultFile += machineName;

                        Calendar  myCalendar  = Calendar.getInstance(); 
			String month = new Integer( myCalendar.get ( Calendar.MONTH)).toString();
			String day = new Integer(myCalendar.get ( Calendar.DAY_OF_MONTH)).toString();
			String year = new Integer(myCalendar.get ( Calendar.YEAR)).toString();
                        myResultFile += "_" + month + day + year + ".xml";       
                        resultFile= myResultFile;      

                }

		foutput = new FileOutputStream( resultFile, true );
		returnVal= flush( testSuiteId, foutput);
      	}
      	catch ( Exception e )
	{
		System.err.println("ERROR : " + e );
	} finally {
            close(in);
            close(foutput);
        }
	return returnVal;
    }

     /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite. 
     * @param testSuiteName the test suite's name.
     * @param foutput the FileOutputStream in which we need to write.
     * @return returns true if the file is succesfully created
     */
    public boolean flush(String testSuiteId, FileOutputStream foutput){
        //System.out.println("REPORTER\t flush(testsuiteId,fout)");
        try{
            StringBuffer xmlRepresentation = new StringBuffer();

	   /*
            xmlRepresentation.append("<?xml version=\"1.0\"?>\n");
            xmlRepresentation.append("<!DOCTYPE testsuite SYSTEM \"test_suite.dtd\">\n");
	    
            xmlRepresentation.append("<!-- ID are defined as: test suite: test case : local id-->\n");        
	    */

	    TestSuite myTestSuite = (TestSuite)testSuiteHash.get( testSuiteId ); 
	    if ( myTestSuite == null )
	    {
		System.err.println("ERROR: Information for TestSuite Id : " + testSuiteId + " doesn't exist");
		return false;
	    }
	    String testSuiteName = myTestSuite.getName();
	    String testSuiteDescription = myTestSuite.getDescription();

            xmlRepresentation.append("<testsuite>\n" );
            xmlRepresentation.append("  <id> " + testSuiteId + " </id>\n");

	    if (!testSuiteName.equals( ReporterConstants.NA ) )
	    {
		    xmlRepresentation.append("<name>" + testSuiteName.trim() + "</name>\n");
	    }
	    if (!testSuiteDescription.equals( ReporterConstants.NA ) )
	    {
		    xmlRepresentation.append("<description><![CDATA[" + testSuiteDescription + "]]></description>\n");
	    }

	    Hashtable testHash = myTestSuite.getTestHash( );
	    
	    Vector testIdVector = myTestSuite.getTestIdVector( );

	    xmlRepresentation.append("<tests>\n");
	    for ( int ti=0; ti< testIdVector.size(); ti++ )
	    {

		String testId = (String)testIdVector.elementAt(ti );
		Test myTest = (Test)testHash.get( testId.trim() );
		String testName = myTest.getName();
		String testDescription = myTest.getDescription();
		String testStatus = myTest.getStatus();
		String testStatusDescription = myTest.getStatusDescription();

		String testExpected= myTest.getExpected( );
		String testActual= myTest.getActual( );

		xmlRepresentation.append("<test>\n");
                xmlRepresentation.append("<id>" + testId + "</id>\n");
	
		if (!testName.equals( ReporterConstants.NA ) )
		{
		    xmlRepresentation.append("<name>" + testName + "</name>\n");
		}

		if (!testDescription.equals( ReporterConstants.NA ) )
		{
		    xmlRepresentation.append("<description><![CDATA[" + testDescription + "]]></description>\n");
		}
		if (!testStatus.equals( ReporterConstants.OPTIONAL ) )
		{
			if (!testStatusDescription.equals( ReporterConstants.OPTIONAL ) )
			{
			    xmlRepresentation.append("<status value=\"" + testStatus + "\"><![CDATA[" + testStatusDescription + "]]></status>\n");
			}
			else if ( ( testExpected != null ) && ( testActual != null ) ) 
			{
			    xmlRepresentation.append("<status value=\"" + testStatus + "\"> <expected><![CDATA[" + testExpected + "]]></expected><actual><![CDATA[" + testActual +   "]]></actual></status>\n");
			}
			else {
			    xmlRepresentation.append("<status value=\"" + testStatus + "\">" + "</status>\n");
			}
		}

		Hashtable testCaseHash = myTest.getTestCaseHash();
		Vector testCaseIdVector = myTest.getTestCaseIdVector( );
		
		/*
		if ( testCaseIdVector.size( ) < 1 )
		{
			// This means there are no test cases and Test has the status info
			xmlRepresentation.append("</test>\n");

		}
		*/
		if ( testCaseIdVector.size() >= 1 )
		{
			xmlRepresentation.append("<testcases>\n");
		
		     for ( int tc=0; tc< testCaseIdVector.size(); tc++ )
		     {
			String testCaseId = (String)testCaseIdVector.elementAt( tc );
			TestCase myTestCase = (TestCase)testCaseHash.get( testCaseId.trim() );

			String testCaseName = myTestCase.getName();
			String testCaseDescription = myTestCase.getDescription();
			String testCaseStatus = myTestCase.getStatus();
			String testCaseStatusDescription = myTestCase.getStatusDescription();
			String testCaseExpected = myTestCase.getExpected();
			String testCaseActual = myTestCase.getActual();

			xmlRepresentation.append("<testcase>\n");
			xmlRepresentation.append("<id> " + testCaseId + "</id>\n");
			if (!testCaseName.equals( ReporterConstants.NA ) )
			{
			    xmlRepresentation.append("<name>" + testCaseName + "</name>\n");
			}

			if (!testCaseDescription.equals( ReporterConstants.NA ) )
			{
			    xmlRepresentation.append("<description><![CDATA[" + testCaseDescription + "]]></description>\n");
			}
			if (!testCaseStatusDescription.equals( ReporterConstants.NA ) )
			{
			    xmlRepresentation.append("<status value=\"" + testCaseStatus + "\"><![CDATA[" + testCaseStatusDescription + "]]></status>\n");
			}
			else if ( ( testCaseExpected != null ) && ( testCaseActual != null ) ) 
			{
			    xmlRepresentation.append("<status value=\"" + testCaseStatus + "\"> <expected><![CDATA[" + testCaseExpected + "]]></expected><actual><![CDATA[" + testCaseActual +   "]]></actual></status>\n");
			}
			else
			{
			    xmlRepresentation.append("<status value=\"" + testCaseStatus + "\">" + "</status>\n");
			}
			xmlRepresentation.append("</testcase>\n");
                    }

                xmlRepresentation.append("</testcases>\n");

		}

                xmlRepresentation.append("</test>\n");
	     }
	     
              xmlRepresentation.append("</tests>\n" );
              xmlRepresentation.append("</testsuite>\n" );
             boolean writeResult =  writeXMLFile(xmlRepresentation, foutput);
		// Now remove TestSuite from Hashtable: PENDING 
	     if ( writeResult==true )
	     {
		// If we could write teh content properly then remove the TestSuite from Hashtable
	     testSuiteHash.remove( testSuiteId.trim() );
	     }
	    return writeResult;
        } catch (java.lang.Exception ex){
            return false;
        }
    }
    
    private boolean writeXMLFile(StringBuffer xmlStringBuffer){
        PrintWriter writer = null;
        try{
            writer = new PrintWriter(new BufferedWriter(new FileWriter(resultFile)));
            writer.println( xmlStringBuffer.toString() );
            writer.flush();
        } catch(java.io.IOException ex){
            ex.printStackTrace();
            return false;
        } finally {
            close(writer);
        }    
        return true;        
    }

    private boolean writeXMLFile(StringBuffer xmlStringBuffer, FileOutputStream  fout){
        try{
	    fout.write( xmlStringBuffer.toString().getBytes() );
	    fout.flush( );
        } catch(java.io.IOException ex){
            ex.printStackTrace();
            return false;
        }    
        return true;        
    }
 
    public void run(){
        //System.out.println("REPORTER\t Inside run");
        generateValidReport();
    }

    private void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch(IOException ioe) {
                // ignore
            }
        }
    }

    private void close(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch(IOException ioe) {
                // ignore
            }
        }
    }

    private void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch(IOException ioe) {
                // ignore
            }
        }
    }

    private void close(FileChannel fileChannel) {
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch(IOException ioe) {
                // ignore
            }
        }
    }
} // end class Reporter
