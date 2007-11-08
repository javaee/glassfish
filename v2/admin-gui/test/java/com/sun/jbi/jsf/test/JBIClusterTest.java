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

public class JBIClusterTest extends SeleneseTestCase
{
        private DefaultSelenium selenium;

        private static final String ADMINGUI_URL                       = "http://localhost:4848";
        private static final String LOGIN_USERNAME_VALUE               = "admin";
        private static final String LOGIN_PASSWD_VALUE                 = "adminadmin";
        private static final String JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE = "valid-sl-only.zip";
        private static final String JBI_COMP_BC1_TEST_ARCHIVE          = "bc1.zip";
        private static final String JBI_COMP_BC2_TEST_ARCHIVE          = "bc2.zip";
        private static final String JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE    = "sa-for-bc1-and-bc2.zip";
        private static final String JBI_CORRUPT_ARCHIVE                = "corrupted-archive.zip";
        private static final String JBI_EMPTY_ARCHIVE                  = "empty.zip";
        private static final String JBI_MISSING_JBI_XML_ARCHIVE        = "missing-jbi-xml.zip";
        private static final String JBI_NOT_WELL_FORMED_JBI_XML_ARCHIVE= "not-well-formed-jbi-xml.zip";

        //Timeout value for most of the commands which needs page to load completely.
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

     /**********************************************************************************/
     /*Test JBI Root Page and Presence of three links and two child elements of Components
     ***********************************************************************************/
     @Test(groups = {"runJBIRoot"})
     private void runJBIRootPageTest()
     {
         navigateToListPage(JBIIdConstants.JBI_ROOT_PAGE_LINK);
         //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
         verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_ROOT_SA_LINK));
         verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_ROOT_COMP_LINK));
         verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_ROOT_BC_NODE));
         verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_ROOT_SE_NODE));
         verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_ROOT_SL_LINK));
     }
    
    /********************************************************************/
    /*Test invalid archives for shared libary installation using copy path
     ********************************************************************/
    @Test(groups = {"invalidCopySL"}, dependsOnGroups = {"runJBIRoot"})
    private void runInvalidArchiveSLTest()
    {
        navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
        invalidSLTest();
    }
    /********************************************************************/
    
    /*Test Installation of Shared Library works using the upload path*/
    @Test(groups = {"uploadSL"}, dependsOnGroups = {"invalidCopySL"})
    private void runUploadShareLibraryTest() 
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);

            //occassionaly the Shared Libraries link is not found and then we need to uncomment 
            //following statement
            clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
            installArchiveFirstStep(JBIIdConstants.JBI_LIBRARY_INSTALL_UPLOAD_FIELD_ID,
                    JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
            selenium.click(JBIIdConstants.JBI_EE_LIBRARY_INSTALL_FINISH_BUTTON_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify that the finish step brought back to the list Shared library page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_NEW_INSTALL_BUTTON_ID));	
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Uploading Shared library failed:" +e.getMessage());
        }
    }

       
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path
     ************************************Comp********************************/
    @Test(groups = {"invalidCopyComp"}, dependsOnGroups = {"uploadSL"})
    private void runInvalidArchiveCompTest()
    {
        navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
        invalidCompTest();
    }
    
    
    /********************************************************************/
    /*Data Provider which provides two component Archives for component installation
     ********************************************************************/

        @DataProvider
	public Object[][] compInstallationData() 
	{
            return new Object[][] {
            new Object[] { JBI_COMP_BC1_TEST_ARCHIVE },
            new Object[] { JBI_COMP_BC2_TEST_ARCHIVE },
            };
	}

     /********************************************************************/
     /*********Install Component Using Upload Path************************/
     /********************************************************************/
	
    @Test(groups = {"installComp"}, dataProvider = "compInstallationData", dependsOnGroups = {"invalidCopyComp"}) 
     public void runUploadComponentTest(String aCompName)
     {
         try
         {
              navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
              clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
              installArchiveFirstStep(JBIIdConstants.JBI_COMPONENT_INSTALL_UPLOAD_FIELD_ID, 
                      aCompName, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
              
              //Verify that the second step of install wizard has the right heading text and two configuration table presence
              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_WIZ_STEP2_TITLE));
              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_CONF_PROP_TBL_TITLE));
              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_PROP_TBL_TITLE));

              selenium.click(JBIIdConstants.JBI_EE_COMPONENT_INSTALL_FINISH_BUTTON_ID);
         }
         catch (Exception e)
         {
              e.printStackTrace();
              //ToDo Add Logging statements
             System.out.println("Uploading Components failed:" +e.getMessage());
         }
     }
    
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path
     ************************************Comp********************************/
    @Test(groups = {"invalidCopySA"}, dependsOnGroups = {"installComp"})
    private void runInvalidArchiveSATest()
    {
        navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
        invalidSATest();
    }
    /********************************************************************/
    /************Deploy Service Assembly using Upload Path****************/
    /********************************************************************/
    
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
            clickNewButton(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID);
            installArchiveFirstStep(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_UPLOAD_FIELD_ID,
                    aDeploymentName, JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
            
            verifyTrue(selenium.isTextPresent("Deploy JBI Service Assembly (Step 2 of 2)"));
            selenium.click(JBIIdConstants.JBI_EE_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID);
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
    
     /********************************************************************/
     /*Test the Table of the Library page shows up fine and has major components*/
     /********************************************************************/
    
    @Test(groups = {"listSL"}, dependsOnGroups = {"deploySA"} )
    private void runListLibraryTest()
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
            //Components . Also check Install, Uninstall buttons presence.

            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_TITLE));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_INLINE_HELP));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_TABLE_TITLE_TEXT));
            runListCommonEE();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Library failed:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
    /**************************************************************************/
   
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
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));
            verifyTrue(selenium.isElementPresent("sharedTableEEForm:sharedTableEE:topActionsGroup1:filterByComponentStateDropDown_list"));
            verifyTrue(selenium.isElementPresent("sharedTableEEForm:sharedTableEE:topActionsGroup1:filterActionDropDown_list"));
            runListCommonEE();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Components failed:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
     /**************************************************************************/
   
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
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));

            // Also check Install, Uninstall buttons, Operations DropDown, Filter by State/Type DropDown presence.
            verifyTrue(selenium.isElementPresent("sharedTableEEForm:sharedTableEE:topActionsGroup1:filterByAssemblyStateDropDown_list"));
            runListCommonEE();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Service Assembly failed:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test Show pages of Shared Library works and has two tabs (General, Descriptor)*/
    /**************************************************************************/
   
    @Test(groups = {"showSL"}, dependsOnGroups = {"listServiceAssembly"})
    private void runShowShareLibraryTest() 
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
            //occassionaly the Shared Libraries link is not found and then we need to uncomment 
            //following statement
            clickNameHyperLink(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowShareLibraryCommonTest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Show tabs  Shared library failed:" +e.getMessage());
        }
    }

    /**************************************************************************/
    /*Click Name link in List Components page to see the details of each component 
    Verify that Components have 5 tabs */
    /**************************************************************************/
   
    @Test(groups = {"showComp"}, dependsOnGroups = {"showSL"})
    private void runShowComponentTest()
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);

            //Choose the first elements name which is a hyperlink in the list table.
            clickNameHyperLink(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowComponentCommonTest();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //ToDo Add Logging statements
             System.out.println("Show Components test case failure:" +e.getMessage());
        }
    }

    /**************************************************************************/
    /*Tests the show detail pages of Service Assemblies in developer-profile */
    /**************************************************************************/
   
    @Test(groups = {"showSA"}, dependsOnGroups = {"showComp"})
    private void runShowServiceAssemblyTest()
    {
        //Testing JBI Deployments Show pages which have 2 tabs General and Descriptor tab
        try
        {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            //Click the name of the first element in the Service assembly list table which is a hyperlink
            clickNameHyperLink(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowServiceAssemblyCommonTest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Show Deployments test case failure " + e.getMessage());
        }
    }

    /**************************************************************************/
    /*******Filter Listed Components by Binding then Engine. This test *******/
    /** needs at least one Binding Component and one Service Engine to pass****/
    /** needs at least one Enabled and one Disabled Component to pass**********/
    /**************************************************************************/

    @Test(groups = {"filterComp"}, dependsOnGroups = {"showSA"})
    private void runFilterComponents()
    {
        navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        
        //Disable One Component So that Disable DropDown Filter by State has at least one element
        operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS,
                    JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
        
        //Filter By State
        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_EE_COMPONENT_STATE_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_EE_STATE_ENABLED_DROPDOWN,
                        JBIResourceConstants.JBI_EE_STATUS_ENABLED_ON_ALL_TARGETS,
                        JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
//        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_EE_COMPONENT_STATE_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_EE_STATE_DISABLED_DROPDOWN,
                        JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS,
                        JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
        
        //selenium.select(JBIIdConstants.JBI_EE_COMPONENT_STATE_FILTER_DROPDOWN_ID, "label= No Targets");
        selenium.select(JBIIdConstants.JBI_EE_COMPONENT_STATE_FILTER_DROPDOWN_ID, "label=Show All");
        
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        ///Filter By Type
        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_EE_COMPONENT_TYPE_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_LIST_BC_TYPE_TEXT,
                        JBIResourceConstants.JBI_LIST_BC_TYPE_TEXT,
                        JBIIdConstants.JBI_EE_COMPONENT_LIST_PAGE_FIRST_BC_TYPE_ID);
      
        selenium.select(JBIIdConstants.JBI_EE_COMPONENT_TYPE_FILTER_DROPDOWN_ID, "label=Show All");
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
  
        selectDropDownNoCBAndVerifyResult(JBIIdConstants.JBI_EE_COMPONENT_TYPE_FILTER_DROPDOWN_ID,
                        JBIResourceConstants.JBI_LIST_SE_TYPE_TEXT,
                        JBIResourceConstants.JBI_LIST_SE_TYPE_TEXT,
                        JBIIdConstants.JBI_EE_COMPONENT_LIST_PAGE_FIRST_SE_TYPE_ID);
    }

    /**************************************************************************/
    /***************Operate on the component : Tested transitions*************/
    /*************a) Enable->Disable->Enable On all Targets*******************/
    /**************************************************************************/
   
    @Test(groups = {"operateComp"}, dependsOnGroups = {"filterComp"})
    private void runOperateComponentTest()
    {
        try
        {
            //Operate Components using Enable/Disable Buttons
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runOperateCompSAEE();
        }
        catch (Exception e)
        {
             e.printStackTrace();
             //ToDo Add Logging statements
             System.out.println("Operating Components failed:" +e.getMessage());
        }   
    }
       
    /**************************************************************************/
    /******Operate the service assembly : Tested transitions******************/
    /**********a) Enable->Disable->Enable************************************/
    /**************************************************************************/
   
    @Test(groups = {"operateSA"}, dependsOnGroups = {"operateComp"})
    private void runOperateServiceAssemblyTest()
    {
        //Testing JBI Deployments lifecycle operations 
        try
        {
            //Enable/Disable the Service Assembly 
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runOperateCompSAEE();
        }	
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Operation Service Assembly test case failure " + e.getMessage());
        }
    }
   
    /**************************************************************************/
    /***********Tests Uninstallation of JBI Shared Library works *************/
    /**************************************************************************/
   
    @Test(groups = { "uninstallSL"} , dependsOnGroups = {"operateSA"})
    private void runUninstallShareLibraryTest() 
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_LIBRARY_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            //Assuming that first element is Sun Wsdl library and second is valid-sl-only 
            //selecting the second element in the table for uninstallation
            uninstallChecked(JBIIdConstants.JBI_EE_LIST_PAGE_SECOND_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_LIB_DELETION_MSG);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Uninstalling Shared library failed:" +e.getMessage());
        }
    }
    
    /**************************************************************************/
    /*Uninstall Component*/
    //This test cannot pass if the first component has a dependenet Service Assembly deployed to it
     /**************************************************************************/
   @Test(groups = {"uninstallComp"}, dependsOnGroups = {"undeploySA"})
    private void runUninstallComponentTest()
    {
        try
        {
            //Change the state of the components to shutdown, as components in started state cannot be uninstalled
            operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS,
                    JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
                
            uninstallChecked(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_COMP_DELETION_MSG);
        }
        catch (Exception e)
        {
             e.printStackTrace();
             //ToDo Add Logging statements
             System.out.println("Uninstalling components test case failure:" +e.getMessage());
        }
    }

   /**************************************************************************/
    /*Tests uninstallation of SA and components. Service Assembly and components  have to be in shutdown
    state before uninstalling. This tests change their states to shutdown before deleting them*/
   /**************************************************************************/
    
      @Test(groups = {"undeploySA"}, dependsOnGroups = {"uninstallSL"})
      private void runUndeployServiceAssemblyTest()
    {
        try
        {
            navigateToListPage(JBIIdConstants.JBI_SERVICE_ASSEMBLY_NODE_ID);
            
            //Select the first element in the Service Assembly table . Assuming that this is the only installed
            //SA and in shutdown state.
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
            operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS,
                    JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
            
            uninstallChecked(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_SA_DELETION_MSG);
            
            //Choose the components for uninstallation. 
            navigateToListPage(JBIIdConstants.JBI_COMPONENT_NODE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
            //sort the Components by Name so that the top two are manage-binding 1 and 2 which are
            //target components for  undeployed SA's SU's.

            selenium.click(JBIIdConstants.JBI_EE_SORT_BUTTON_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runUninstallComponentTest();
            runUninstallComponentTest();
            //Verify that after uninstallation the user is still on the list components page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Undeploying SA/Target components failed:" +e.getMessage());
        }
    }
      
      
    /*********************************************************************/
    /*************Cluster Profile ->StandAlone Instancens Test Cases*****/
    /*********************************************************************/

      
     /********************************************************************/
    /*Test invalid archives for shared libary installation using copy path
     ********************************************************************/
    
    //@Test(groups = {"invalidCopySLSvr"}, dependsOnGroups = {"undeploySA"})  
    
    @Test(groups = {"invalidCopySLSvr"})  
    private void runInvalidArchiveSLServerTest()
    {
        navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_SL_LIST_PAGE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        invalidSLTest();
    }
    
    /********************************************************************/
    
    /*Test Installation of Shared Library works using the upload path*/
    @Test(groups = {"uploadSLSvr"}, dependsOnGroups = {"invalidCopySLSvr"})
    private void runUploadShareLibraryServerTest() 
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_SL_LIST_PAGE_ID);
            
            //occassionaly the Shared Libraries link is not found and then we need to uncomment 
            //following statement
            clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
            installArchiveFirstStep(JBIIdConstants.JBI_LIBRARY_INSTALL_UPLOAD_FIELD_ID,
                    JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIBRARY_INSTALL_FINISH_BUTTON_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify that the finish step brought back to the list Shared library page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID));	
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Uploading Shared library failed for Server:" +e.getMessage());
        }
    }

    
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path 
     *For Cluster profile Server instances***************************/
     /********************************************************************/
    @Test(groups = {"invalidCopyCompSvr"}, dependsOnGroups = {"uploadSLSvr"})
    private void runInvalidArchiveCompServerTest()
    {
        navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        invalidCompTest();
    }

     /********************************************************************/
     /*********Install Component Using Upload Path for Cluster**************/
     /********************************************************************/
	
    @Test(groups = {"installCompSvr"}, dataProvider = "compInstallationData",
                    dependsOnGroups = {"invalidCopyCompSvr"}) 
     public void runUploadComponentServerTest(String aCompName)
     {
         try
         {
              navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
              clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
              installArchiveFirstStep(JBIIdConstants.JBI_COMPONENT_INSTALL_UPLOAD_FIELD_ID, 
                      aCompName, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);
              
              //Verify that the second step of install wizard has the right heading text and two configuration table presence
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_WIZ_STEP2_TITLE));
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_CONF_PROP_TBL_TITLE));
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_PROP_TBL_TITLE));

              selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_COMPONENT_INSTALL_FINISH_BUTTON_ID);
         }
         catch (Exception e)
         {
              e.printStackTrace();
              //ToDo Add Logging statements
             System.out.println("Uploading Components failed for Server:" +e.getMessage());
         }
     }
    
    /********************************************************************/
    /*Test invalid archives for SA Deployment using copy path
     ********************************************************************/
    @Test(groups = {"invalidCopySASvr"}, dependsOnGroups = {"installCompSvr"})
    private void runInvalidArchiveServiceAssemblyServerTest()
    {
        navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
        //workaround to use common function, SA page is first page displayed
        //No clicking of tab is required which navigate does..
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
        
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        invalidSATest();
    }
    
    
    /********************************************************************/
    /************Deploy Service Assembly for Servers using Upload Path***/
    /********************************************************************/
    
    @Parameters ({"saName"})
    @Test(groups = {"deploySASvr"}, dependsOnGroups = {"invalidCopySASvr"})
     public void runUploadServiceAssemblyServerTest(String aDeploymentName)
     {
         try
         {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            //workaround to use common function, SA page is first page displayed
            //No clicking of tab is required which navigate does..
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
            
            //Deploy Service Assembly whose SU targets the previously installed two components
            //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            //selenium.waitForPageToLoad("9000");
            uploadSAServerCluster(aDeploymentName);
         }
         catch (Exception e)
        {
              e.printStackTrace();
              //ToDo Add Logging statements
             System.out.println("Deploying/Uploading SA failed for Server path:" +e.getMessage());
         }
     }
    
    
    /********************************************************************/
     /*Test the Table of the Library page shows up fine and has major components*/
     /********************************************************************/
    
    @Test(groups = {"listSLSvr"}, dependsOnGroups = {"deploySASvr"} )
    private void runListLibraryServerTest()
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_SL_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
            //Components . Also check Install, Uninstall buttons presence.
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_LIST_LIB_PAGE_INLINE_HELP));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_TITLE)); //Tests Page Title & Table Title
            runListCommonServerCluster();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Library failed Server:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
    /**************************************************************************/
   
    @Test(groups = {"listCompSvr"}, dependsOnGroups = {"listSLSvr"} )
    private void runListComponentServerTest()
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
            //Components . Also check Install, Uninstall buttons, Enable/Disable Buttons presence.
            
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_COMP_PAGE_TITLE));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_LIST_COMP_PAGE_INLINE_HELP));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_CLUSTER_LIST_COMP_TABLE_TITLE_TEXT));
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_TYPE_HEADER));
            runListCompSAServerCluster();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Components failed Server:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
     /**************************************************************************/
   
    @Test(groups = {"listServiceAssemblySvr"}, dependsOnGroups = {"listCompSvr"} )
    private void runListServiceAssemblyServerTest()
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            //workaround to use common function, SA page is first page displayed
            //No clicking of tab is required which navigate does..
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

            //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
            //Components .
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_SA_PAGE_TITLE));//Page & Table Title
            verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_LIST_SA_PAGE_INLINE_HELP));
            runListCompSAServerCluster();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Listing Table of Service Assembly faile Server:" + e.getMessage());
        }
    }

    /**************************************************************************/
    /*Test Show pages of Shared Library works and has two tabs (General, Descriptor)*/
    /**************************************************************************/
   
    @Test(groups = {"showSLSvr"}, dependsOnGroups = {"listServiceAssemblySvr"})
    private void runShowShareLibraryServerTest() 
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_SL_LIST_PAGE_ID);
            clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowShareLibraryCommonTest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Show tabs  Shared library failed Server:" +e.getMessage());
        }
    }
    
    
     /**************************************************************************/
    /*Click Name link in List Components page to see the details of each component 
    Verify that Components have 5 tabs */
    /**************************************************************************/
   
    @Test(groups = {"showCompSvr"}, dependsOnGroups = {"showSLSvr"})
    private void runShowComponentServerTest()
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            //occassionaly the Shared Libraries link is not found and then we need to uncomment 
            //following statement
            clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowComponentCommonTest();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //ToDo Add Logging statements
             System.out.println("Show Components test case failure Server:" +e.getMessage());
        }
    }

    /**************************************************************************/
    /*Tests the show detail pages of Service Assemblies in developer-profile */
    /**************************************************************************/
   
    @Test(groups = {"showSASvr"}, dependsOnGroups = {"showCompSvr"})
    private void runShowServiceAssemblyServerTest()
    {
        //Testing JBI Deployments Show pages which have 2 tabs General and Descriptor tab
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            //workaround to use common function, SA page is first page displayed
            //No clicking of tab is required which navigate does..
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
            clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
            runShowServiceAssemblyCommonTest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Show Deployments test case failure Server " + e.getMessage());
        }
    }

    
    /**************************************************************************/
    /***************Operate on the component : Tested transitions*************/
    /*************a) Enable->Disable->Enable On all Targets*******************/
    /**************************************************************************/
   
    @Test(groups = {"operateCompSvr"}, dependsOnGroups = {"showSASvr"})
    private void runOperateComponentServerTest()
    {
        try
        {
            //Operate Components using Enable/Disable Buttons
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runOperateCompSAServerCluster();
        }
        catch (Exception e)
        {
             e.printStackTrace();
             //ToDo Add Logging statements
             System.out.println("Operating Components failed Server:" +e.getMessage());
        }
    }
       
    /**************************************************************************/
    /******Operate the service assembly : Tested transitions******************/
    /**********a) Enable->Disable->Enable************************************/
    /**************************************************************************/
   
    @Test(groups = {"operateSASvr"}, dependsOnGroups = {"operateCompSvr"})
    private void runOperateServiceAssemblyServerTest()
    {
        //Testing JBI Deployments lifecycle operations 
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            //workaround to use common function, SA page is first page displayed
            //No clicking of tab is required which navigate does..
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
            //Enable/Disable the Service Assembly 
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
            runOperateCompSAServerCluster();
        }	
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Operation Service Assembly test case failure Server" + e.getMessage());
        }
    }
   
    /**************************************************************************/
    /***********Tests Uninstallation of JBI Shared Library works *************/
    /**************************************************************************/
   
    @Test(groups = { "uninstallSLSvr"} , dependsOnGroups = {"operateSASvr"})
    private void runUninstallShareLibraryServerTest() 
    {
        try
        {
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_SL_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            //Assuming that first element is Sun Wsdl library and second is valid-sl-only 
            //selecting the second element in the table for uninstallation
            uninstallChecked(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_SECOND_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_LIB_DELETION_MSG);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Uninstalling Shared library failed Server:" +e.getMessage());
        }
    }
  
   /**************************************************************************/
    /*Tests uninstallation of SA and components. Service Assembly and components  have to be in shutdown
    state before uninstalling. This tests change their states to shutdown before deleting them*/
   /**************************************************************************/
    
      @Test(groups = {"undeploySASvr"}, dependsOnGroups = {"uninstallSLSvr"})
      private void runUndeployServiceAssemblyServerTest()
    {
        try
        {
            //Navigate to SA Tab after visiting Components Tab..
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_SERVER_SA_LIST_PAGE_ID);
            
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runUndeploySAServerClusterTest();

            //Choose the components for uninstallation. 
            navigateToSvrListPage(JBIIdConstants.JBI_EE_SERVER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
            //sort the Components by Name so that the top two are manage-binding 1 and 2 which are
            //target components for  undeployed SA's SU's.
            selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_SORT_NAME_BUTTON_ID);
            runUninstallComponentServerClusterTest();
            runUninstallComponentServerClusterTest();
            //Verify that after uninstallation the user is still on the list components page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //ToDo Add Logging statements
            System.out.println("Undeploying SA/Target components failed CLuster:" +e.getMessage());
        }
    }
    
    /*********************************************************************/
    /*************Cluster Profile ->Clusters Tests ***********************/
    /*********************************************************************/
    
    /**************Pre-Condition*****************************************/
    /****Have a Running CLuster with at least one running instance *****/  
      
  
    //Use the following dependency when You want to Chain the Server tests 
    //followed by Cluster tests
    //@Test(groups = {"invalidCopySLClstr"}, dependsOnGroups = {"undeploySASvr"})  
    
    //Run only Cluster Node's ->JBI Tab tests 
    @Test(groups = {"invalidCopySLClstr"})  
    private void runInvalidArchiveSLClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        invalidSLTest();
    }
    
     /********************************************************************/
    /*Test Installation of Shared Library works using the upload path*/
    /********************************************************************/
    
    @Test(groups = {"uploadSLClstr"}, dependsOnGroups = {"invalidCopySLClstr"})
    private void runUploadShareLibraryClusterTest() 
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);

        //occassionaly the Shared Libraries link is not found and then we need to uncomment 
        //following statement
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        installArchiveFirstStep(JBIIdConstants.JBI_LIBRARY_INSTALL_UPLOAD_FIELD_ID,
                JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID);
        selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIBRARY_INSTALL_FINISH_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //Verify that the finish step brought back to the list Shared library page
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID));	
    }
    
    
    /********************************************************************/
    /*Test invalid archives for Component installation using copy path*/
     /********************************************************************/
    
    @Test(groups = {"invalidCopyCompClstr"}, dependsOnGroups = {"uploadSLClstr"})
    private void runInvalidArchiveComponentClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        invalidCompTest();
    }
  
     /********************************************************************/
     /*********Install Component Using Upload Path for Cluster**************/
     /********************************************************************/
	
    @Test(groups = {"installCompClstr"}, dataProvider = "compInstallationData",
                    dependsOnGroups = {"invalidCopyCompClstr"}) 
     public void runUploadComponentClusterTest(String aCompName)
     {
          navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
          clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
          installArchiveFirstStep(JBIIdConstants.JBI_COMPONENT_INSTALL_UPLOAD_FIELD_ID, 
                  aCompName, JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID);

          //Verify that the second step of install wizard has the right heading text and two configuration table presence
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_WIZ_STEP2_TITLE));
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_CONF_PROP_TBL_TITLE));
//              verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_COMP_INSTALL_PROP_TBL_TITLE));

          selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_COMPONENT_INSTALL_FINISH_BUTTON_ID);
     }
    
    
    /********************************************************************/
    /*Test invalid archives for SA Deployment using copy path
     ********************************************************************/
    @Test(groups = {"invalidCopySAClstr"}, dependsOnGroups = {"installCompClstr"})
    private void runInvalidArchiveSAClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_CLUSTER_SA_LIST_PAGE_ID);
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        invalidSATest();
    }
    
    
    /********************************************************************/
    /************Deploy Service Assembly for Clusters using Upload Path***/
    /********************************************************************/
    
    @Parameters ({"saName"})
    @Test(groups = {"deploySAClstr"}, dependsOnGroups = {"invalidCopySAClstr"})
     public void runUploadServiceAssemblyClusterTest(String aDeploymentName)
     {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_CLUSTER_SA_LIST_PAGE_ID);

        //Deploy Service Assembly whose SU targets the previously installed two components
        //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        //selenium.waitForPageToLoad("9000");
        uploadSAServerCluster(aDeploymentName);
     }

     /********************************************************************/
     /*Test the Table of the Library page shows up fine and has major components*/
     /********************************************************************/
    
    @Test(groups = {"listSLClstr"}, dependsOnGroups = {"deploySAClstr"} )
    private void runListLibraryClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
        //Components . Also check presence of Install, Uninstall buttons
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_CLUSTER_LIST_LIB_PAGE_INLINE_HELP));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_LIB_PAGE_TITLE)); //Tests Page Title & Table Title
        runListCommonServerCluster();
    }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
    /**************************************************************************/
   
    @Test(groups = {"listCompClstr"}, dependsOnGroups = {"listSLClstr"} )
    private void runListComponentClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table Select/Deselect All
        //Components . Also check Install, Uninstall buttons, Operations DropDown, Filter by State/Type DropDown presence.
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_COMP_PAGE_TITLE));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_CLUSTER_LIST_COMP_PAGE_INLINE_HELP));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_CLUSTER_LIST_COMP_TABLE_TITLE_TEXT));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_TYPE_HEADER));
        runListCompSAServerCluster();
     }

    /**************************************************************************/
    /*Test the Table of the Components page shows up fine and has major components*/
     /**************************************************************************/
   
    @Test(groups = {"listServiceAssemblyClstr"}, dependsOnGroups = {"listCompClstr"} )
    private void runListServiceAssemblyClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //Verify the presence of Heading, Inline Help Text, Tables Header, Column header and table
        //Select/Deselect All Components .
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_SA_PAGE_TITLE));//Page & Table Title
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_CLUSTER_LIST_SA_PAGE_INLINE_HELP));
        runListCompSAServerCluster();
    }
    /**************************************************************************/
    /*Test Show pages of Shared Library works and has two tabs (General, Descriptor)*/
    /**************************************************************************/
   
    @Test(groups = {"showSLClstr"}, dependsOnGroups = {"listServiceAssemblyClstr"})
    private void runShowShareLibraryClusterTest() 
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);
        clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
        runShowShareLibraryCommonTest();
    }

    /**************************************************************************/
    /*Click Name link in List Components page to see the details of each component 
    Verify that Components have 5 tabs */
    /**************************************************************************/
   
    @Test(groups = {"showCompClstr"}, dependsOnGroups = {"showSLClstr"})
    private void runShowComponentClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        //occassionaly the Shared Libraries link is not found and then we need to uncomment 
        //following statement
        clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
        runShowComponentCommonTest();
    }

    /**************************************************************************/
    /*Tests the show detail pages of Service Assemblies in developer-profile */
    /**************************************************************************/
   
    @Test(groups = {"showSAClstr"}, dependsOnGroups = {"showCompClstr"})
    private void runShowServiceAssemblyClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_CLUSTER_SA_LIST_PAGE_ID);
        clickNameHyperLink(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID);
        runShowServiceAssemblyCommonTest();
    }

    
    /**************************************************************************/
    /***************Operate on the component : Tested transitions*************/
    /*************a) Enable->Disable->Enable On all Targets*******************/
    /**************************************************************************/
   
    @Test(groups = {"operateCompClstr"}, dependsOnGroups = {"showSAClstr"})
    private void runOperateComponentClusterTest()
    {
        //Operate Components using Enable/Disable Buttons
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        runOperateCompSAServerCluster();
    }
       
    /**************************************************************************/
    /******Operate the service assembly : Tested transitions******************/
    /**********a) Enable->Disable->Enable************************************/
    /**************************************************************************/
   
    @Test(groups = {"operateSAClstr"}, dependsOnGroups = {"operateCompClstr"})
    private void runOperateServiceAssemblyClusterTest()
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
        //Enable/Disable the Service Assembly 
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        runOperateCompSAServerCluster();
    }
   
    /**************************************************************************/
    /***********Tests Uninstallation of JBI Shared Library works *************/
    /**************************************************************************/
   
    @Test(groups = { "uninstallSLClstr"} , dependsOnGroups = {"operateSAClstr"})
    private void runUninstallShareLibraryClusterTest() 
    {
        navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_SL_LIST_PAGE_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        //Assuming that first element is Sun Wsdl library and second is valid-sl-only 
        //selecting the second element in the table for uninstallation
        uninstallChecked(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_SECOND_ELEM_CB_ID, 
                JBIResourceConstants.JBI_EE_SERVER_CLUSTER_LIB_DELETION_MSG);
    }
   
   /**************************************************************************/
    /*Tests uninstallation of SA and components. Service Assembly and components  have to be in shutdown
    state before uninstalling. This tests change their states to shutdown before deleting them*/
   /**************************************************************************/
    
      @Test(groups = {"undeploySAClstr"}, dependsOnGroups = {"uninstallSLClstr"})
      private void runUndeployServiceAssemblyClusterTest()
      {
            navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            selenium.click(JBIIdConstants.JBI_EE_CLUSTER_SA_LIST_PAGE_ID);
            
            //Select the first element in the Service Assembly table . Assuming that this is the only installed
            //SA and in shutdown state.
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            runUndeploySAServerClusterTest();
            //Choose the components for uninstallation. 
            navigateToClstrListPage(JBIIdConstants.JBI_EE_CLUSTER_COMP_LIST_PAGE_ID);
            selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
            
            selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_SORT_NAME_BUTTON_ID);
            //Navigate to the Components page and uninstall the top two sorted components after disabling them
	    runUninstallComponentServerClusterTest();
            runUninstallComponentServerClusterTest();
            
            //Verify that after uninstallation the user is still on the list components page
            verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID));
    }
      
      
      
    /************************************************************************/
    /*****************Common Functions***************************************/
    /************************************************************************/
 
    /**************Commmon Methods for JBI Node Tests*********************/  
  
    //JBI Node List pages common widgets
    private void runListCommonEE()
    {
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SELECT_MULTIPLE_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_DESELECT_MULTIPLE_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_NEW_INSTALL_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID));

        selenium.click(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID);
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_UNINSTALL_BUTTON_ID));
    }
     
    private void runOperateCompSAEE()
    {
        operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                JBIIdConstants.JBI_EE_ENABLE_BUTTON_ID,
                JBIResourceConstants.JBI_EE_STATUS_ENABLED_ON_ALL_TARGETS,
                JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);

        operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                JBIIdConstants.JBI_EE_DISABLE_BUTTON_ID,
                JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS,
                JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);

        operateCheckedElement(JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID,
                JBIIdConstants.JBI_EE_ENABLE_BUTTON_ID,
                JBIResourceConstants.JBI_EE_STATUS_ENABLED_ON_ALL_TARGETS,
                JBIIdConstants.JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID);
    }
    
  /**************Commmon Methods for Stand Alone Instances/Cluster Node Tests*********************/    
     private void uploadSAServerCluster(String aDeploymentName)
     {
        clickNewButton(JBIIdConstants.JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID);
        installArchiveFirstStep(JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_UPLOAD_FIELD_ID,
                aDeploymentName, JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID);
        verifyTrue(selenium.isTextPresent("Deploy JBI Service Assembly (Step 2 of 2)"));
        selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        verifyTrue(selenium.isTextPresent("JBI Service Assemblies"));			
     }
     
    //Checking Common Elements in Cluster Profile List Pages Of Components/Service Assemblies  
    private void runListCompSAServerCluster()
    {
        //Components/SA's List Page
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_EE_SERVER_CLUSTER_LIST_TABLE_HEADER_STATUS_ENABLED));
        runListCommonServerCluster();
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_ENABLE_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_DISABLE_BUTTON_ID));
    }
    
    //Checking Common ELements in Cluster Profile List Pages of SL/Comp/SA
    private void runListCommonServerCluster()
    {
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_LIST_TABLE_NAME_HEADER));
        // Also check Install, Remove/Delete buttons,Enable/Disable Buttons, Select/Deselect button image presence.

        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_SELECT_MULTIPLE_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_DESELECT_MULTIPLE_BUTTON_ID));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID));

        selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID);
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_UNINSTALL_BUTTON_ID));
    }
    
    //Cluster and Stand Alone Instances share the same ID for Component Operate widgets
    private void runOperateCompSAServerCluster()
    {
            operateCheckedElement(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_ENABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_ENABLED_ON_SERVER_CLUSTER,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID);
            
            operateCheckedElement(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_SERVER_CLUSTER,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID);
            
            operateCheckedElement(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_ENABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_ENABLED_ON_SERVER_CLUSTER,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID);
     }
    
    private void runUninstallComponentServerClusterTest()
    {
            //Change the state of the components to shutdown, as components in started state cannot be uninstalled
            operateCheckedElement(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_SERVER_CLUSTER,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID);
                
            uninstallChecked(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_SERVER_CLUSTER_COMP_DELETION_MSG);
    }
     
    private void runUndeploySAServerClusterTest()
    {
            //Disable The Service Assembly as Enabled SA cannot be undeployed
            operateCheckedElement(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_DISABLE_BUTTON_ID,
                    JBIResourceConstants.JBI_EE_STATUS_DISABLED_ON_SERVER_CLUSTER,
                    JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID);
            
            uninstallChecked(JBIIdConstants.JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID,
                    JBIResourceConstants.JBI_EE_SERVER_CLUSTER_SA_DELETION_MSG);
    }
    
    private void runShowShareLibraryCommonTest()
    {
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_GENERAL_TAB));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_SL_INLINE_HELP));
        verifyTrue(selenium.isTextPresent("Description:"));
        verifyTrue(selenium.isTextPresent("- Shared Library General Properties"));

        selenium.click(JBIIdConstants.JBI_SHOW_DESCRIPTOR_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_TAB));
        verifyTrue(selenium.isTextPresent("Shared Library Descriptor"));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_INLINE_HELP));

         //Click the Targets tab and verify the inline help's contents
        selenium.click(JBIIdConstants.JBI_SHOW_TARGETS_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_TARGETS_TAB));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_TARGETS_SL_INLINE_HELP));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_MANAGE_TARGETS_BUTTON_ID));
    }
    
    //Show Pages for Component are same for JBI Node, StandAlone Instances and Clusters
    private void runShowComponentCommonTest()
    {
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

        verifyTrue(selenium.isElementPresent("jbiShowPropertiesForm:propertyContentPage:loadDefaults"));
        verifyTrue(selenium.isElementPresent("jbiShowPropertiesForm:propertyContentPage:topButtons:saveButton"));

        //Click the Targets Tab and check for presence of "Manage Targets" Button
        selenium.click(JBIIdConstants.JBI_SHOW_TARGETS_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_TARGETS_TAB));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_MANAGE_TARGETS_BUTTON_ID));
    }
    
    //Show Pages for Component are same for JBI Node, StandAlone Instances and Clusters
    private void runShowServiceAssemblyCommonTest()
    {
        //Check whether the first and default page is General tab
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_GENERAL_TAB));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_SA_INLINE_HELP));

        //Click the Descriptor tab and verify the Inline help's contents
        selenium.click(JBIIdConstants.JBI_SHOW_DESCRIPTOR_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_TAB));
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_DESCRIPTOR_INLINE_HELP));

        //Click the Targets tab and verify the inline help's contents
        selenium.click(JBIIdConstants.JBI_SHOW_TARGETS_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_TARGETS_TAB));
        verifyTrue(selenium.isTextPresent(JBIResourceConstants.JBI_SHOW_TARGETS_SA_INLINE_HELP));
        verifyTrue(selenium.isElementPresent(JBIIdConstants.JBI_EE_SERVER_CLUSTER_MANAGE_TARGETS_BUTTON_ID));
    }
    
     //All the invalid SL Archives are tested for right validation error messages in Installation wizard 
     private void invalidSLTest()
     {
            
         String radioId = JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID;
         String fieldId = JBIIdConstants.JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID;
         String nextBtnId = JBIIdConstants.JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID;
         
         //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
         invalidArchiveTest(radioId, fieldId, nextBtnId);
         
        /*Testing  Wrong  Archive type (Component/Service Assembly)and asserting the right
         alert message is displayed*/
        copyArchive(radioId, fieldId, JBI_COMP_BC1_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(radioId, fieldId, JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
     }
     
           
     //All the invalid Comp Archives are tested for right validation error messages in Installation wizard 
     private void invalidCompTest()
     {
         String radioId = JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID;
         String fieldId = JBIIdConstants.JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID;
         String nextBtnId = JBIIdConstants.JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID;
         selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        
         invalidArchiveTest(radioId, fieldId, nextBtnId);
        
        /*Testing  Wrong  Archive (Library/Service Assembly) type and asserting the right
         alert message is displayed*/
        copyArchive(radioId, fieldId, JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(radioId, fieldId, JBI_SA_FOR_BC1_BC2_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
    }
    
     //All the invalid SA Archives are tested for right validation error messages in Installation wizard 
     private void invalidSATest()
     {  
         String radioId = JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID;
         String fieldId = JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID;
         String nextBtnId = JBIIdConstants.JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID;
        
         invalidArchiveTest(radioId, fieldId, nextBtnId);

         /*Testing  Wrong  Archive (Library/Binding Component )type and asserting the right alert
         message is displayed*/
        copyArchive(radioId, fieldId, JBI_LIB_VALID_SL_ONLY_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
        copyArchive(radioId, fieldId, JBI_COMP_BC1_TEST_ARCHIVE, nextBtnId);
        assertTrue(selenium.isTextPresent(JBIResourceConstants.JBI_MISMATCHING_ARCHIVE_MSG));
        
    }
     
     //Common set of negative archive test files which are tested for validation error messages
     //On All (Shared Library/Component/Service Assembly) wizards 
     
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
    
    /*Navigate to the Cluster Profile Server's JBI page whose Id is passed*/
    private void navigateToSvrListPage(String aPageId)
    {
        selenium.selectFrame("relative=up");
        selenium.selectFrame("index");
        selenium.click(JBIIdConstants.JBI_EE_STANDALONE_INSTANCES_NODE_ID);
        selenium.click(JBIIdConstants.JBI_EE_SERVER_NODE_ID);
        selenium.selectFrame("relative=up");
        selenium.selectFrame("main");
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_SERVER_JBI_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(aPageId);
    }
    
    /*Navigate to the Cluster Profile Cluster's JBI page whose Id is passed*/
    private void navigateToClstrListPage(String aPageId)
    {
        selenium.selectFrame("relative=up");
        selenium.selectFrame("index");
        selenium.click(JBIIdConstants.JBI_EE_CLUSTERS_NODE_ID);
        selenium.click(JBIIdConstants.JBI_EE_CLUSTER_NODE_ID);
        selenium.selectFrame("relative=up");
        selenium.selectFrame("main");
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(JBIIdConstants.JBI_EE_CLUSTER_JBI_TAB_ID);
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(aPageId);
    }
    
    /*Click the Install/Deply Button From List Page*/
    private void clickNewButton(String aButtonId)
    {
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(aButtonId);
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
    private void clickNameHyperLink(String aLinkId)
    {
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        selenium.click(aLinkId);
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
    
    /* Select the Component/Assembly top operate and Click the operation (Enable/Disable)button */
    private void operateCheckedElement(String aCheckBoxId, //CheckBoxId of any row which needs to be operated on
            String aButtonId,      //Operate(Enable/Disable) Button's Id 
            String aExpectedValue, //Expected value of any clomun after successful completion of operation
            String aColumnId)      //Columns element Id
    {
        selenium.click(aCheckBoxId);
        selenium.click(aButtonId); 
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
        verifyEquals(aExpectedValue, selenium.getText(aColumnId));
    }
    
    
    /*Click the Uninstall button after selecting passed checkbox ID and Confirm the Alert MessageS */
    private void uninstallChecked(String aCheckBoxId, String aAlertMessage)
    {
        uninstallCheckedElement(aCheckBoxId);
        selenium.getConfirmation().matches(aAlertMessage);
}
    
    private void uninstallCheckedElement(String aCheckBoxId)//Select which Row needs to be uninstalled
    {
        selenium.click(aCheckBoxId);
        //selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);

        //If not called from Server/Cluster Page 
        if(aCheckBoxId.indexOf("Target")!=-1)
        {
            selenium.click(JBIIdConstants.JBI_EE_SERVER_CLUSTER_UNINSTALL_BUTTON_ID);
        }
        else
        {
            selenium.click(JBIIdConstants.JBI_EE_UNINSTALL_BUTTON_ID);
        }
        selenium.waitForCondition("selenium.browserbot.isNewPageLoaded()", TIMEOUT_PERIOD);
    }
}
