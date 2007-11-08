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

/*Collection of Woodstock Component Ids on JBI Pages*/

public class JBIIdConstants
{
        public static final String JBI_ROOT_PAGE_LINK
                = "form:tree:JBIRoot:JBIRoot_link";
        
        public static final String JBI_ROOT_SA_LINK
                = "jbiRootForm:propertySheet:pss:saProperty:saText";
        
        public static final String JBI_ROOT_COMP_LINK
                = "jbiRootForm:propertySheet:pss:componentsProperty:componentsText";
        
        public static final String JBI_ROOT_SL_LINK
                = "jbiRootForm:propertySheet:pss:slProperty:slText";
        
        /******Common Ids for Component/Library/Deployment*****/

        //New Button ID to launch Install/Deploy wizard
        public static final String JBI_NEW_INSTALL_BUTTON_ID 
                = "sharedTableForm:sharedTable:topActionsGroup1:newSharedTableButton"; 

        //Uninstall Button ID 
        public static final String JBI_UNINSTALL_BUTTON_ID
                = "sharedTableForm:sharedTable:topActionsGroup1:button1";

        //ID of the first Name Hyperlink in the List Tables of JBI Pages
        public static final String JBI_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID
                ="sharedTableForm:sharedTable:sharedTableRowGroup:0:sharedNamesTableColumn:sharedTableNameHyperlink";
        
        //ID of the first elements started state in the List Tables of JBI Pages
        public static final String JBI_LIST_PAGE_FIRST_ELEM_STATE_ID
                = "sharedTableForm:sharedTable:sharedTableRowGroup:0:sharedStateTableColumn:sharedstateStarted";

        //ID of the Descriptor Tab 
        public static final String JBI_SHOW_DESCRIPTOR_TAB_ID
                = "tabsForm:showTabs:descriptor";
        //ID of the Configurations Tab
        public static final String JBI_SHOW_CONFIGURATION_TAB_ID
                = "tabsForm:showTabs:configuration";
        //ID of the Loggers Tab
        public static final String JBI_SHOW_LOGGERS_TAB_ID
                = "tabsForm:showTabs:loggers";
        //ID of the Targets Tab
        public static final String JBI_SHOW_TARGETS_TAB_ID
                = "tabsForm:showTabs:targets";
        public static final String JBI_SHOW_LOAD_DEFAULTS_BUTTON_ID
                = "jbiShowPropertiesForm:propertyContentPage:loadDefaults";
        public static final String JBI_SHOW_SAVE_BUTTON_ID
                = "jbiShowPropertiesForm:propertyContentPage:topButtons:saveButton";
        

        //Select the first/second element in the List table using selection Checkbox
        public static final String JBI_LIST_PAGE_FIRST_ELEM_CB_ID
                = "sharedTableForm:sharedTable:sharedTableRowGroup:0:sharedSelectedTableColumn:select";
        public static final String JBI_LIST_PAGE_SECOND_ELEM_CB_ID
                = "sharedTableForm:sharedTable:sharedTableRowGroup:1:sharedSelectedTableColumn:select";

        //ID of Operation Dropdown component. Same for component and deployments list page
        public static final String JBI_OPERATION_DROPDOWN_ID
                ="sharedTableForm:sharedTable:topActionsGroup1:dropdown1";
        
        public static final String JBI_EE_SELECT_MULTIPLE_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image";
                   
        public static final String JBI_EE_DESELECT_MULTIPLE_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image";
        
        public static final String JBI_EE_SORT_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:sharedNamesTableColumn:_columnHeader:_primarySortButton";

        /****Constant IDs for JBI Library Page Testing *****/

        //JBI Shared Library Node in left pane tree
        public static final String JBI_LIBRARY_NODE_ID                 
                = "form:tree:JBIRoot:libraries:libraries_link"; 

        //Wizard step1 Upload component ID
        public static final String JBI_LIBRARY_INSTALL_UPLOAD_FIELD_ID
                = "jbiNewLibrary1Form:uploadComp_com.sun.webui.jsf.upload";

        //Radio Button to Choose FileChooser for uploading
        public static final String JBI_LIBRARY_INSTALL_FILECHOOSER_RADIO_ID
                = "jbiNewLibrary1Form:rdBtn2";

        //FileChooser Textfield ID
        public static final String JBI_LIBRARY_INSTALL_FILECHOOSER_FIELD_ID
                = "jbiNewLibrary1Form:txtFld2";

        //Library installation wizard Next button id
        public static final String JBI_LIBRARY_INSTALL_NEXT_BUTTON_ID 
                = "jbiNewLibrary1Form:stepTitle:topButtons:nextStep";

        //Library installtion wizard Finish button id
        public static final String JBI_LIBRARY_INSTALL_FINISH_BUTTON_ID
                = "jbiNewLibrary2Form:stepTitle:topButtons:finishStep"; 


        /***Constant IDs for JBI Binding Component/Service Engine Page Testing ***/

        //Component node in left panel tree
        public static final String JBI_COMPONENT_NODE_ID
                = "form:tree:JBIRoot:bindingsEngines:bindingsEngines_link" ;
        
        public static final String JBI_COMPONENT_FILTER_DROPDOWN_ID
                = "sharedTableForm:sharedTable:topActionsGroup1:filterTypeDropDown_list";

        //Component installation wizard step1 Upload component id
         public static final String JBI_COMPONENT_INSTALL_UPLOAD_FIELD_ID
                = "jbiNewBindingOrEngine1Form:uploadComp_com.sun.webui.jsf.upload";
         
         //Radio Button to Choose FileChooser for uploading
        public static final String JBI_COMPONENT_INSTALL_FILECHOOSER_RADIO_ID
            = "jbiNewBindingOrEngine1Form:rdBtn2";

        //FileChooser Textfield ID
        public static final String JBI_COMPONENT_INSTALL_FILECHOOSER_FIELD_ID
            = "jbiNewBindingOrEngine1Form:txtFld2";

        //Component installation wizard Next button id
        public static final String JBI_COMPONENT_INSTALL_NEXT_BUTTON_ID 
                = "jbiNewBindingOrEngine1Form:stepTitle:topButtons:nextStep";

        // Component installation wizard Finish button id
        public static final String JBI_COMPONENT_INSTALL_FINISH_BUTTON_ID 
                = "jbiNewBindingOrEngine2Form:stepTitle:topButtons:finishStep";
        
        // Binding Component Type Column Id
        public static final String JBI_COMPONENT_LIST_PAGE_FIRST_BC_TYPE_ID 
                = "sharedTableForm:sharedTable:sharedTableRowGroup:0:sharedTypeTableColumn:sharedTypeBindingText";
        
        // Service Engine Type Column Id
        public static final String JBI_COMPONENT_LIST_PAGE_FIRST_SE_TYPE_ID 
                = "sharedTableForm:sharedTable:sharedTableRowGroup:0:sharedTypeTableColumn:sharedTypeEngineText";
        
        /***Constant IDs for JBI Service Assemblies Page Testing ***/

        //Service Assemblies node in left panel tree
        public static final String JBI_SERVICE_ASSEMBLY_NODE_ID
                = "form:tree:JBIRoot:deployments:deployments_link" ;
        
        //Service Assembly installation wizard step1 Upload component id
         public static final String JBI_SERVICE_ASSEMBLY_DEPLOY_UPLOAD_FIELD_ID
                = "jbiNewDeployment1Form:uploadComp_com.sun.webui.jsf.upload";
         
        //Radio Button to Choose FileChooser for copying archives
        public static final String JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_RADIO_ID
            = "jbiNewDeployment1Form:rdBtn2";

        //FileChooser Textfield ID
        public static final String JBI_SERVICE_ASSEMBLY_DEPLOY_FILECHOOSER_FIELD_ID
            = "jbiNewDeployment1Form:txtFld2";

        //Service Assembly installation wizard Next/Finish button id
        public static final String JBI_SERVICE_ASSEMBLY_DEPLOY_NEXT_BUTTON_ID 
                = "jbiNewDeployment1Form:stepTitle:topButtons:nextStep";
        public static final String JBI_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID
                = "jbiNewDeployment2Form:stepTitle:topButtons:finishStep";
        
        
        /*********************************************************************/
        /*************Cluster Console Elements/Widgets Id*********************/
        /*********************************************************************/
        //Install Button ID 
        public static final String JBI_EE_NEW_INSTALL_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:newSharedTableEEButton";
        
        //Uninstall Button ID 
        public static final String JBI_EE_UNINSTALL_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:button1";
        
        //Enable Button ID
        public static final String JBI_EE_ENABLE_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:button2";
        
        //Disable Button ID
        public static final String JBI_EE_DISABLE_BUTTON_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:button3";
        
        //Select the first/second element in the List table using selection Checkbox
        public static final String JBI_EE_LIST_PAGE_FIRST_ELEM_CB_ID
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:0:sharedSelectedTableColumn:select";
        
        //Second elements checkbox id 
        public static final String JBI_EE_LIST_PAGE_SECOND_ELEM_CB_ID
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:1:sharedSelectedTableColumn:select";
        
        //ID of the first Name Hyperlink in the List Tables of JBI Pages
        public static final String JBI_EE_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID
                ="sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:0:sharedNamesTableColumn:sharedTableEENameHyperlink";
        
        //ID of the first elements started state in the List Tables of JBI Pages
        public static final String JBI_EE_LIST_PAGE_FIRST_ELEM_STATUS_ID
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:0:sharedStatusTableColumn:sharedStateText";
                
         // Binding Component Type Column Id
        public static final String JBI_EE_COMPONENT_LIST_PAGE_FIRST_BC_TYPE_ID 
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:0:sharedTypeTableColumn:sharedTypeBindingText";
        
        // Service Engine Type Column Id
        public static final String JBI_EE_COMPONENT_LIST_PAGE_FIRST_SE_TYPE_ID 
                = "sharedTableEEForm:sharedTableEE:sharedTableEERowGroup:0:sharedTypeTableColumn:sharedTypeEngineText";
        
        //ID of DropDown Component 
        public static final String JBI_EE_COMPONENT_TYPE_FILTER_DROPDOWN_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:filterActionDropDown_list";
        
         //ID of  Dropdown component for filtering by state
        public static final String JBI_EE_COMPONENT_STATE_FILTER_DROPDOWN_ID
                = "sharedTableEEForm:sharedTableEE:topActionsGroup1:filterStateDropDown_list";
        
        //Cluster profile Library installation wizard Finish button id
        public static final String JBI_EE_LIBRARY_INSTALL_FINISH_BUTTON_ID
                = "jbiNewLibrary2Form:stepTitle:topButtons:finishStepMultiTarget";
        
        // Cluster profile Component installation wizard Finish button id
        public static final String JBI_EE_COMPONENT_INSTALL_FINISH_BUTTON_ID 
                = "jbiNewBindingOrEngine2Form:stepTitle:topButtons:finishStepMultiTarget";
        
        // Cluster profile Service Assembly installation wizard Finish button id
        public static final String JBI_EE_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID
                = "jbiNewDeployment2Form:stepTitle:topButtons:finishStepMultiTarget";
        
        /*********************************************************************/
        /*************Cluster Profile ->StandAlone Instances List Page Id*******/
        /*********************************************************************/
        public static final String JBI_EE_STANDALONE_INSTANCES_NODE_ID
                = "form:tree:standaloneInstances:standaloneInstances_link";
        
        public static final String JBI_EE_SERVER_NODE_ID
                = "form:tree:standaloneInstances:comsunappservtypeservernameservercategoryconfig:link";
        
        public static final String JBI_EE_SERVER_JBI_TAB_ID
                = "propertyForm:standaloneTabs:jbi";
        
        public static final String JBI_EE_SERVER_SA_LIST_PAGE_ID
                = "tabsForm:standaloneTabs:jbi:serverIntegrationsDeployments";
        
        public static final String JBI_EE_SERVER_COMP_LIST_PAGE_ID
                = "tabsForm:standaloneTabs:jbi:serverIntegrationsBindingsAndEngines";
        
        public static final String JBI_EE_SERVER_SL_LIST_PAGE_ID
                = "tabsForm:standaloneTabs:jbi:serverIntegrationsLibraries";
        
        /*********************************************************************/
        /*************Cluster Profile ->Clusters Widgets List Page Id******/
        /*********************************************************************/
        
        public static final String JBI_EE_CLUSTERS_NODE_ID
                = "form:tree:clusters2:clusters2_link";
        
        public static final String JBI_EE_CLUSTER_NODE_ID
                = "form:tree:clusters2:cluster0:cluster0_link";
        
        public static final String JBI_EE_CLUSTER_JBI_TAB_ID
                = "propertyForm:clusterTabs:clusterJbi";
        
         public static final String JBI_EE_CLUSTER_SA_LIST_PAGE_ID
                = "tabsForm:clusterTabs:clusterJbi:clusterIntegrationsDeployments";
        
        public static final String JBI_EE_CLUSTER_COMP_LIST_PAGE_ID
                = "tabsForm:clusterTabs:clusterJbi:clusterIntegrationsBindingsAndEngines";
        
        public static final String JBI_EE_CLUSTER_SL_LIST_PAGE_ID
                = "tabsForm:clusterTabs:clusterJbi:clusterIntegrationsLibraries";
        
                
                
        /*********************************************************************/
        /*************Cluster Profile ->StandAlone Instances Widgets Id*******/
        /*************Cluster Profile ->Clusters Widgets Id (same IDs as server)*/
        /*********************************************************************/
        
        //Install Button ID 
        public static final String JBI_EE_SERVER_CLUSTER_NEW_INSTALL_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:topActionsGroup1:newSharedSingleTargetTableButton";
                
        //Uninstall Button ID 
        public static final String JBI_EE_SERVER_CLUSTER_UNINSTALL_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:topActionsGroup1:button1";
                    
        //Enable Button ID
        public static final String JBI_EE_SERVER_CLUSTER_ENABLE_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:topActionsGroup1:button2";
                    
        //Disable Button ID
        public static final String JBI_EE_SERVER_CLUSTER_DISABLE_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:topActionsGroup1:button3";
        
        //Select the first/second element in the List table using selection Checkbox
        public static final String JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_CB_ID
                = "sharedSingleTargetTableForm:singleTargetTable:tableRowGroup:0:selectTableColumn:selectCheckbox";
        
        //Second elements checkbox id 
        public static final String JBI_EE_SERVER_CLUSTER_LIST_PAGE_SECOND_ELEM_CB_ID
                = "sharedSingleTargetTableForm:singleTargetTable:tableRowGroup:1:selectTableColumn:selectCheckbox";
        
        //ID of the first Name Hyperlink in the List Tables of JBI Pages
        public static final String JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_NAME_LINK_ID
                ="sharedSingleTargetTableForm:singleTargetTable:tableRowGroup:0:col1:nameHyperlink";
                    
        //ID of the first elements state in the List Tables of Cluster Profile->StandAlone Server JBI Pages
        public static final String JBI_EE_SERVER_CLUSTER_LIST_PAGE_FIRST_ELEM_STATE_ID
                = "sharedSingleTargetTableForm:singleTargetTable:tableRowGroup:0:enabledTableColumn";
        
        //Cluster profile->Server's Library installation wizard Finish button id
        public static final String JBI_EE_SERVER_CLUSTER_LIBRARY_INSTALL_FINISH_BUTTON_ID
                = "jbiNewLibrary2Form:stepTitle:topButtons:finishStepSingleTarget";
        
        // Cluster profile->Server's Component installation wizard Finish button id
        public static final String JBI_EE_SERVER_CLUSTER_COMPONENT_INSTALL_FINISH_BUTTON_ID 
                = "jbiNewBindingOrEngine2Form:stepTitle:topButtons:finishStepSingleTarget";
        
        // Cluster profile->Server's Service Assembly installation wizard Finish button id
        public static final String JBI_EE_SERVER_CLUSTER_SERVICE_ASSEMBLY_DEPLOY_FINISH_BUTTON_ID
                = "jbiNewDeployment2Form:stepTitle:topButtons:finishStepSingleTarget";
        
        // Cluster profile  Server/Cluster select button image ID
        public static final String JBI_EE_SERVER_CLUSTER_SELECT_MULTIPLE_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:_tableActionsTop:_selectMultipleButton:_selectMultipleButton_image";
                    
        // Cluster profile Server/Cluster deselect button image ID
        public static final String JBI_EE_SERVER_CLUSTER_DESELECT_MULTIPLE_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:_tableActionsTop:_deselectMultipleButton:_deselectMultipleButton_image";
        
        // Cluster profile Server/Cluster sort button image ID
        public static final String JBI_EE_SERVER_CLUSTER_SORT_NAME_BUTTON_ID
                = "sharedSingleTargetTableForm:singleTargetTable:tableRowGroup:col1:_columnHeader:_primarySortButton:_primarySortButton_image";
        
        public static final String JBI_EE_SERVER_CLUSTER_MANAGE_TARGETS_BUTTON_ID
                = "sharedTargetsTableForm:sharedTargetsTable:topActionsGroup1:sharedTargetsTableManageTargetsButton";
        
}