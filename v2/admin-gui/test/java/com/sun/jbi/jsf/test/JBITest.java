/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.jbi.jsf.test;

import com.thoughtworks.selenium.*;

import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.testng.annotations.*;

/*  
 * Before running the test, please ensure that your Selenium Remote Server
 * is up and running. Server can be started using the java -jar selenium-server.jar
 */

public class JBITest extends SeleneseTestCase
{
        private DefaultSelenium selenium;

        private static final String ADMINGUI_URL = "http://localhost:4848";
        private static final String LOGIN_USERNAME_VALUE = "admin";
        private static final String LOGIN_PASSWD_VALUE = "adminadmin";
        private static final String JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE = "valid-sl-only.zip";
        private static final String JBI_COMP_BC1_TEST_ARCHIVE = "bc1.zip";
        private static final String JBI_COMP_BC2_TEST_ARCHIVE = "bc2.zip";
        private static final String JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE = "sa-for-bc1-and-bc2.zip";
        private static final String JBI_CORRUPT_ARCHIVE = "corrupted-archive.zip";
        private static final String JBI_EMPTY_ARCHIVE = "empty.zip";
        private static final String JBI_MISSING_JBI_XML_ARCHIVE = "missing-jbi-xml.zip";
        private static final String JBI_NOT_WELL_FORMED_JBI_XML_ARCHIVE = "not-well-formed-jbi-xml.zip";

        //For windows testing,	 path has to be in this format
        private static String TIMEOUT_PERIOD ="10000";

        //Constant IDs of Login Page Components
        private final static String LOGIN_PASSWORD_INPUT_ID  = "Login.password";
        private final static String LOGIN_USERNAME_INPUT_ID  = "Login.username";

    
    @BeforeSuite(alwaysRun = true)
    private void configure()
    {
        /*Uncomment the following line when chrome url does not work. It will not handle upload use cases well.
        Please modify the location of firefox binary on local hard drive before launching the test*/
        //selenium = new DefaultSelenium("localhost", 4444, "*firefox //space0/firefox/firefox/firefox-bin", ADMINGUI_URL);
        //selenium = new DefaultSelenium("localhost", 4444, "*firefox", ADMINGUI_URL);

        /*Uncomment the following line to use secure chrome url and test upload use cases. Please modify the location of firefox binary on your hard-disk*/
         selenium = new DefaultSelenium("localhost", 4444, "*chrome", ADMINGUI_URL);
         //Spacify the path in the following way when the firefox binary is not located at default location
         //selenium = new DefaultSelenium("localhost", 4444, "*chrome //space0/firefox/firefox/firefox-bin", ADMINGUI_URL); 


        /* Uncomment the line below if you want to run the test on Internet Explorer.*/
        //selenium = new DefaultSelenium("localhost", 4444, "*iexplore", ADMINGUI_URL);

        selenium.start();
        selenium.open(ADMINGUI_URL);
    }

    // Cleanup the selenium environment
    @AfterSuite(alwaysRun = true)    
    private void stopTest()
    {
        selenium.stop();
    }

     /*Logs into the console using default username and password before starting all tests*/
     //@BeforeSuite(dependsOnMethods = { "configure" })
     @BeforeClass(alwaysRun = true)
     private void runLogin()
     {
         try
         {
             
              selenium.type(LOGIN_USERNAME_INPUT_ID, LOGIN_USERNAME_VALUE);
              selenium.type(LOGIN_PASSWORD_INPUT_ID, LOGIN_PASSWD_VALUE);

              //Click the LoginButton
              selenium.click("loginButton");
              selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
         }
         catch (Exception t)
         {
              t.printStackTrace(); 
              //ToDo Add Logging statements
              System.out.println("Login failed: " + t.getMessage());
         }
     }
     
    
        /*Install Shared Library using the FileCHooser radiobutton*/
        @Test(groups = {"copySL"})
	private void runCopyShareLibraryTest()
	{
            try
            {
                navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
                clickNewButton();
                copyArchive(JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID,
                        JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID,
                        JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, 
                        JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);

                selenium.click(JBIIdConstants.JBI_LIBRARY_INSTALL_FINISH_BUTTON_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                //Verify that the finish step brought back to the list Shared library page
                verifyTrue(selenium.isTextPresent("Manage Java Business Integration Shared Libraries"));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));	
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //ToDo Add Logging statements
                System.out.println("FileChooser installation of Shared Library failed:" +e.getMessage());
            }
	}
        
    //Install Components/Service Assemblies using non-upload path meant to install local packages
//    public void runCopyComponentServiceAssembly()
//    {
//        runCopyComponent(JBI_COMP_BC1_TEST_ARCHIVE);
//        runCopyComponent(JBI_COMP_BC2_TEST_ARCHIVE);
//        runCopyDeployment(JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE);
//    }

    /*Install Component using FileChooser Path for local components*/
    @Test(groups = {"copyComp"}, dataProvider = "compInstallationData", dependsOnGroups = {"copySL"})     
    public void runCopyComponentTest(String aCompName)
    {
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
            //Choose the second radio button for installing local components
            clickNewButton();
            copyArchive(JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID,
                aCompName, 
                JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
            
            //Verify that the second step of install wizard has the right heading text and two configuration table presence
            selenium.click(JBIIdConstants.JBI_COMPONENT_INSTALL_FINISH_BUTTON_ID);
    }

    /*Deploy Service Assembly using FileChooser path*/
    //@Parameters ({"saName"})
    //@Test(groups = {"copySA"}, dependsOnMethods = {"runCopyComponent"})
    public void runCopyDeploymentTest(String aDeploymentName)
    {
        navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
        clickNewButton();
        copyArchive(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID,
            JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID,
            aDeploymentName, 
            JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);

        selenium.click(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);	
    }
   
    
    /********************************************************************/
    /*Test invalid archives for shared libary installation using copy path
     ********************************************************************/
    @Test(groups = {"invalidCopySL"})
    private void runInvalidArchiveSLTest()
    {
        navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
        clickNewButton();
        invalidArchiveTest(JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID,
                 JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID,
                 JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
       
        /*Testing  Wrong  Archive type and asserting the right alert message is displayed*/
        copyArchive(JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID,
                JBI_COMP_BC1_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
        
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID,
                JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
        
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
    }
    /********************************************************************/
    
    
    
    
    /*Test Installation of Shared Library works using the upload path*/
    @Test(groups = {"uploadSL"}, dependsOnGroups = {"invalidCopySL"})
    private void runUploadShareLibraryTest() 
    {
        navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);

        //occassionaly the Shared Libraries link is not found and then we need to uncomment 
        //following statement
        clickNewButton();
        installArchiveFirstStep(JBIIdConstants.JBI_LIBRARY_INSTALL_UPLOAD_FIELD_ID,
                JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
        selenium.click(JBIIdConstants.JBI_LIBRARY_INSTALL_FINISH_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //Verify that the finish step brought back to the list Shared library page
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));	
    }

	/*Uninstall the component after changing its state to shutdown*/
	/*This Method installs two binding components manage-binding1, manage-binding2 and  one Deployable archive is  esbadmin00002sa-for-bc1-and-bc2.zip
	from admin-gui/test/data directory . respective archives are bc1.zip, bc2.zip and  sa-for-bc1-and-bc2.zip
	After component installation this test deploys a Service Assembly esbadmin00002 with two SU's whose target components
	are manage-binding1 and manage-binding2. */

//        public void runUploadCompSA() 
//        {
//            runUploadComponent(JBI_COMP_BC1_TEST_ARCHIVE);
//            runUploadComponent(JBI_COMP_BC2_TEST_ARCHIVE);
//            runUploadServiceAssembly(JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE);
//         }
    
        
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path
     ************************************Comp********************************/
    @Test(groups = {"invalidCopyComp"}, dependsOnGroups = {"uploadSL"})
    private void runInvalidArchiveCompTest()
    {
        navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
        clickNewButton();
        invalidArchiveTest(JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID,
                JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
        
        /*Testing  Wrong  Archive type and asserting the right alert message is displayed*/
        copyArchive(JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID,
                JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID,
                JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
    }
    /********************************************************************/

        @DataProvider
	public Object[][] compInstallationData() 
	{
            return new Object[][] {
            new Object[] { JBI_COMP_BC1_TEST_ARCHIVE },
            new Object[] { JBI_COMP_BC2_TEST_ARCHIVE },
            };
	}

	 /*Install Component Using Upload Path*/
    @Test(groups = {"installComp"}, dataProvider = "compInstallationData", dependsOnGroups = {"invalidCopyComp"}) 
	 //@Test(groups = { "installComp" }, parameters = { "compName" } )
	 //@Parameters( value = { "compName" } )
	 //@Test(groups = { "installComp" } )
     public void runUploadComponentTest(String aCompName)
     {
          navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
          clickNewButton();
          installArchiveFirstStep(JBIIdConstants.JBI_COMPONENT_INSTALL_UPLOAD_FIELD_ID, 
                  aCompName, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);

          //Verify that the second step of install wizard has the right heading text and two configuration table presence
          verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_WIZ_STEP2_TITLE));
          verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_CONF_PROP_TBL_TITLE));
          verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_PROP_TBL_TITLE));

          selenium.click(JBIIdConstants.JBI_COMPONENT_INSTALL_FINISH_BUTTON_ID);
     }
    
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path
     ************************************Comp********************************/
    @Test(groups = {"invalidCopySA"}, dependsOnGroups = {"installComp"})
    private void runInvalidArchiveSATest()
    {
        navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
        clickNewButton();
        invalidArchiveTest(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID,
                JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
        
        /*Testing  Wrong  Archive (Library/Binding Component )type and asserting the right alert
         message is displayed*/
        copyArchive(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID,
                JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID,
                JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID,
                JBI_COMP_BC1_TEST_ARCHIVE, JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
    }
    /********************************************************************/

    /*Deploy Service Assembly using Upload Path*/
    @Parameters ({"saName"})
    @Test(groups = {"deploySA"}, dependsOnGroups = {"invalidCopySA"})
     public void runUploadServiceAssemblyTest(String aDeploymentName)
     {
         try
         {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            //Deploy Service Assembly whose SU targets the previously installed two components
            //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            //selenium.waitForPageToLoad("9000");
            clickNewButton();
            installArchiveFirstStep(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_UPLOAD_FIELD_ID,
                    aDeploymentName, JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
            
            verifyTrue(selenium.isTextPresent("Deploy JBI Service Assembly (Step 2 of 2)"));
            selenium.click(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            verifyTrue(selenium.isTextPresent("JBI Service Assemblies"));			
         }
         catch (Exception e)
        {
              e.printStackTrace();
              //ToDo Add Logging statements
             System.out.println("Deploying/Uploading SA failed:" +e.getMessage());
         }
     }
        
        /*Test the Table of the Library page shows up fine and has major components*/
        @Test(groups = {"listSL"}, dependsOnGroups = {"deploySA"} )
        private void runListLibraryTest()
        {
            try
            {
                navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

                //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
                //Components . Also check Install, Uninstall buttons, Operations DropDown, Filter by State/Type DropDown presence.

                //To do: Make Library specific tests
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_TITLE));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_INLINE_HELP));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_TABLE_TITLE_TEXT));                
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_DESC_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));

                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID));

                selenium.click(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID);

                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_UNINSTALL_BUTTON_ID));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //ToDo Add Logging statements
                System.out.println("Listing Table of Library failed:" + e.getMessage());
            }
        }

        /*Test the Table of the Components page shows up fine and has major components*/
        @Test(groups = {"listComp"}, dependsOnGroups = {"listSL"} )
	private void runListComponentTest()
	{
            try
            {
                navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

                //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
                //Components . Also check Install, Uninstall buttons, Operations DropDown, Filter by State/Type DropDown presence.

                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_COMP_PAGE_TITLE));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_COMP_PAGE_INLINE_HELP));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_COMP_TABLE_TITLE_TEXT));                
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_DESC_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_STATE_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_TYPE_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));

                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:topActionsGroup1:filterByComponentStateDropDown_list"));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:topActionsGroup1:filterTypeDropDown_list"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID));

                selenium.click(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID);

                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_UNINSTALL_BUTTON_ID));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_OPERATION_DROPDOWN_ID));
                selenium.click("sharedTableForm:sharedTable:sharedTableRowGroup:sharedNamesTableColumn:_columnHeader:_primarySortButton:_primarySortButton_image");
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //ToDo Add Logging statements
                System.out.println("Listing Table of Components failed:" + e.getMessage());
            }
	}
        
        /*Test the Table of the Components page shows up fine and has major components*/
        @Test(groups = {"listServiceAssembly"}, dependsOnGroups = {"listComp"} )
	private void runListServiceAssemblyTest()
        {
            try
            {
                navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

                //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
                //Components .

                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_SA_PAGE_TITLE));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_SA_PAGE_INLINE_HELP));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_SA_TABLE_TITLE_TEXT));                
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_DESC_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_STATE_HEADER));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));
                
                // Also check Install, Uninstall buttons, Operations DropDown, Filter by State/Type DropDown presence.

                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));
                verifyTrue(selenium.isElementPresent("sharedTableForm:sharedTable:topActionsGroup1:filterByAssemblyStateDropDown_list"));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID));

                selenium.click(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID);

                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_UNINSTALL_BUTTON_ID));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_OPERATION_DROPDOWN_ID));
                //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //ToDo Add Logging statements
                System.out.println("Listing Table of Service Assembly failed:" + e.getMessage());
            }
        }


        /*Test Show pages of Shared Library works and has two tabs (General, Descriptor)*/
        @Test(groups = {"showSL"}, dependsOnGroups = {"listServiceAssembly"})
	private void runShowShareLibraryTest() 
	{
            try
            {
                navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
                //occassionaly the Shared Libraries link is not found and then we need to uncomment 
                //following statement
                clickNameHyperLink();
                
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_GENERAL_TAB));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_SL_INLINE_HELP));
                verifyTrue(selenium.isTextPresent("Description:"));
                verifyTrue(selenium.isTextPresent("- Shared Library General Properties"));
                
                selenium.click(JBIIdConstants.JBI_SHOW_DESCRIPTOR_TAB_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_TAB));
                verifyTrue(selenium.isTextPresent("Shared Library Descriptor"));
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_INLINE_HELP));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("Show tabs  Shared library failed:" +e.getMessage());
            }
	}
    
    	/*Click Name link in List Components page to see the details of each component 
	Verify that Components have 5 tabs */
	@Test(groups = {"showComp"}, dependsOnGroups = {"showSL"})
	private void runShowComponentTest()
	{
            try
            {
                navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
                
                //Choose the first elements name which is a hyperlink in the list table.
                clickNameHyperLink();
                
                //Verify that the General Tab is the tab open by default
                assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_GENERAL_TAB));
                
                //Click the Configuration Tab 
                selenium.click(JBIIdConstants.JBI_SHOW_CONFIGURATION_TAB_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_CONFIGURATION_TAB));

                //Click the Descriptor Tab
                selenium.click(JBIIdConstants.JBI_SHOW_DESCRIPTOR_TAB_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_TAB));

                //Click the Loggers Tab and check for the presence of Load Deafults and Save Button
                selenium.click(JBIIdConstants.JBI_SHOW_LOGGERS_TAB_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_LOGGERS_TAB));
                
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_SHOW_LOAD_DEFAULTS_BUTTON_ID));
                verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_SHOW_SAVE_BUTTON_ID));
            }
            catch (Exception e)
            {
                //e.printStackTrace();
                //ToDo Add Logging statements
                 System.out.println("Show Components test case failure:" +e.getMessage());
            }
	}

    /*Tests the show detail pages of Service Assemblies in developer-profile */
    @Test(groups = {"showSA"}, dependsOnGroups = {"showComp"})
    private void runShowDeploymentTest()
    {
        //Testing JBI Deployments Show pages which have 2 tabs General and Descriptor tab
        try
        {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            //Check the presence of 2 tabs in the Show Deployments page
            //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", "1000");

            //Click the name of the first element in the Service assembly list table which is a hyperlink
            clickNameHyperLink();
            
            //Check whether the first and default page is General tab
	    assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_GENERAL_TAB));
            assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_SA_INLINE_HELP));

            //Click the Descriptor tab and verify the inline Help's contents
            selenium.click(JBIIdConstants.JBI_SHOW_DESCRIPTOR_TAB_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_TAB));
            assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_INLINE_HELP));
            //checkForVerificationErrors();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Show Deployments test case failure " + e.getMessage());
        }
    }

    
    /****Filter Listed Components by Binding then Engine. 
     This test needs at least one Binding Component and one Service Engine to pass****/
    @Test(groups = {"filterComp"}, dependsOnGroups = {"showSA"})
    private void runFilterComponents()
    {
        navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_COMPONENT_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_LIST_BC_TYPE_TEXT,
                        JBIResourceConstants.JBI_LIST_BC_TYPE_TEXT,
                        JBIIdConstants.JBI_COMPONENT_LIST_PAGE_FIRST_BC_TYPE_ID);
        
        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_COMPONENT_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_LIST_SE_TYPE_TEXT,
                        JBIResourceConstants.JBI_LIST_SE_TYPE_TEXT,
                        JBIIdConstants.JBI_COMPONENT_LIST_PAGE_FIRST_SE_TYPE_ID);
    }
    
	/******Operate on the component : Tested transitions
	a) Start->Stop->Shutdown->Start
	b) Start->Shutdown->Start
	c) Start->Stop->Start
	d) Start->Stop->Shutdown->Stop->Start*******/

        @Test(groups = {"operateComp"}, dependsOnGroups = {"filterComp"})
	private void runOperateComponentTest()
	{
            try
            {
                navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
                selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
                
                operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
                operate("Stop", JBIResourceConstants.JBI_OPERATION_STOPPED_STATE);
                operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
                operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
                operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
                operate("Stop", JBIResourceConstants.JBI_OPERATION_STOPPED_STATE);
                operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
                operate("Stop", JBIResourceConstants.JBI_OPERATION_STOPPED_STATE);
                operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
            }
            catch (Exception e)
            {
                 e.printStackTrace();
                 //ToDo Add Logging statements
                 System.out.println("Operating Components failed:" +e.getMessage());
            }
	}
       
        
    /******Operate the service assembly : Tested transitions
     *a) Start->Stop->Shutdown->Start
     *b) Start->Stop->Start
     *c) Start->Shutdown->Start
    */
        
    @Test(groups = {"operateSA"}, dependsOnGroups = {"operateComp"})
    private void runOperateServiceAssemblyTest()
    {
	//Testing JBI Deployments lifecycle operations 
        try
        {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            //Stop/Shutdown the Service Assembly 
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
            operate("Stop", JBIResourceConstants.JBI_OPERATION_STOPPED_STATE);
            operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
            operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
            operate("Stop", JBIResourceConstants.JBI_OPERATION_STOPPED_STATE);
            operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
            operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
            
//            Expected to Fail            
//            selectDropDownAndVerifyResult(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID,
//                    JBIIdConstants.JBI_OPERATION_DROPDOWN_ID, 
//                   "Stop",
//                    JBIResourceConstants.JBI_OPERATION_STOPPED_STATE,
//                    JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_STATE_ID);
            operate("Start", JBIResourceConstants.JBI_OPERATION_STARTED_STATE);
	}	
	catch (Exception e)
	{
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Operation Service Assembly test case failure " + e.getMessage());
	}
    }
  
    /*Tests Uninstallation of JBI Shared Library works */
    @Test(groups = { "uninstallSL"} , dependsOnGroups = {"operateSA"})
    private void runUninstallShareLibrary() 
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            //Assuming that first element is Sun Wsdl library and second is valid-sl-only 
            //selecting the second element in the table for uninstallation
            uninstallCheckedElement(JBIIdConstants.JBI_LIST_PAGE_SECOND_ELEM_CB_ID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Uninstalling Shared library failed:" +e.getMessage());
        }
    }

    /*Uninstall Component*/
    //This test cannot pass if the first component has a dependenet Service Assembly deployed to it
    //@Test(groups = {"uninstallComp"}, dependsOnGroups = {"uninstallSL"})
    private void runUninstallComponent()
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
            
            //Choose the components for uninstallation. 
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Change the state of the components to shutdown, as components in started state cannot be uninstalled
            changeStateUninstall();
            
            //Verify that after uninstallation the user is still on the list components page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));
        }
        catch (Exception e)
        {
             e.printStackTrace();
             //ToDo Add Logging statements
             System.out.println("Uninstalling components test case failure:" +e.getMessage());
        }
    }

    /*Tests uninstallation of SA and components. Service Assembly and components  have to be in shutdown
    state before uninstalling. This tests change their states to shutdown before deleting them*/
    @Test(groups = {"undeploySA"}, dependsOnGroups = {"uninstallSL"})
    private void runUndeploySATest()
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            
            //Select the first element in the Service Assembly table . Assuming that this is the only installed
            //SA and in shutdown state.
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            changeStateUninstall();
            
            //Choose the components for uninstallation. 
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Change the state of the components to shutdown, as components in started state cannot be uninstalled
            changeStateUninstall();
            changeStateUninstall();
            
            //Verify that after uninstallation the user is still on the list components page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Undeploying SA/Target components failed:" +e.getMessage());
        }
    }
  
    /************************************************************************/
    /*****************Common Functions***************************************/
    /************************************************************************/
    
    private void operate(String aOperateId, String aExpectedStateId)
    {
            selectDropDownAndVerifyResult(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID,
                JBIIdConstants.JBI_OPERATION_DROPDOWN_ID,
                aOperateId,
                aExpectedStateId,
                JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_STATE_ID);
    }
    
    private void invalidArchiveTest(String aRadioId, String aFieldId, String aNextBtnId)
    {
     
        /*Testing No Archive and asserting the right alert message shows up*/
        copyArchive(aRadioId, aFieldId, "", aNextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_NO_ARCHIVE_MSG));
        
        /*Testing Empty Archive and asserting the right alert message shows up*/
        copyArchive(aRadioId, aFieldId, JBI_EMPTY_ARCHIVE, aNextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EMPTY_ARCHIVE_MSG));
        
        /*Testing Corrupted Archive and asserting the right alert message is displayed*/
        copyArchive(aRadioId, aFieldId, JBI_CORRUPT_ARCHIVE, aNextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_CORRUPT_ARCHIVE_MSG));
        
        /*Testing Not well formed JBI Xml Archive and asserting the right alert message is displayed*/
        copyArchive(aRadioId, aFieldId, JBI_NOT_WELL_FORMED_JBI_XML_ARCHIVE, aNextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_NOT_WELL_FORMED_JBI_XML_ARCHIVE_MSG));
        
        /*Testing  Missing JBI XML Archive and asserting the right alert message is displayed*/
        copyArchive(aRadioId, aFieldId, JBI_MISSING_JBI_XML_ARCHIVE, aNextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISSING_JBI_XML_ARCHIVE_MSG));
    }
        
    /*Method to get the test data archive path , tested on windows*/
    private String getTestFilePath(String aArchiveName)
    {
        java.io.File f = new java.io.File("./test/data", aArchiveName);
        String result = f.getAbsolutePath();
        return result;
    }
    
    /*Navigate to the page whose Id is passed*/
    private void navigateToListPage(String aPageId)
    {
        selenium.selectFrame("relative=up");
        selenium.selectFrame("index");
        selenium.click(aPageId);
        selenium.selectFrame("relative=up");
        selenium.selectFrame("main");
    }
    
    /*Click the Install/Deply Button From List Page*/
    private void clickNewButton()
    {
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
    }
    
    /*Try Installing passed Empty archive at the passed text field in installation wizard*/
    private void copyArchive(String aRadioId, String aTextFieldId, String aArchiveId,
            String aButtonId)
    {
        selenium.click(aRadioId);
        installArchiveFirstStep(aTextFieldId, aArchiveId, aButtonId);
    }
    
    private void installArchiveFirstStep(String aTextFieldId, String aArchiveId,
            String aButtonId)
    {
        selenium.type(aTextFieldId, getTestFilePath(aArchiveId));
        selenium.click(aButtonId);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
    }
    
    /*Click the Name hyperlink in first Row of List Tables */
    private void clickNameHyperLink()
    {
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
    }
    
    /*Select a value from DropDown after checking one row and verify the Expected Result */
    private void selectDropDownAndVerifyResult(String aCheckBoxId, //Which row is to be targeted
                    String aDropDownComponentId,     //Dropdown component's id
                   String aLabelVal,                //Which label to chose
                   String aExpectedValue,           //What is the final expected value
                   String aTargetCompId)            //Which components value is expected to change
    {
            selenium.click(aCheckBoxId);
            selectDropDownNoCBAndVerifyResult(aDropDownComponentId, aLabelVal, aExpectedValue, aTargetCompId);
    }
    
    /*Select a value from DropDown without checking any row and verify the Expected Result */
    private void selectDropDownNoCBAndVerifyResult(String aDropDownComponentId,     //Dropdown component's id
                   String aLabelVal,                //Which label to chose
                   String aExpectedValue,           //What is the final expected value
                   String aTargetCompId)            //Which components value is expected to change
    {
            String selectIndex =  "label=" + aLabelVal ;
            selenium.select(aDropDownComponentId, selectIndex);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            verifyEquals(aExpectedValue, selenium.getText(aTargetCompId));
    }
    
    private void changeStateUninstall()
    {
        operate("Shut Down", JBIResourceConstants.JBI_OPERATION_SHUTDOWN_STATE);
        uninstallCheckedElement(JBIIdConstants.JBI_LIST_PAGE_FIRST_ELEM_CB_ID);
    }
            
    
    /*Click the Uninstall button after selecting passed checbox in the list page*/
    private void uninstallCheckedElement(String aCheckBoxId)//Select which Row needs to be uninstalled
                   
    {
        selenium.click(aCheckBoxId);
        selenium.click(JBIIdConstants.JBI_UNINSTALL_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.getConfirmation().matches(JBIResourceConstants.JBI_INSTALLABLE_DELETION_MSG);
    }
}