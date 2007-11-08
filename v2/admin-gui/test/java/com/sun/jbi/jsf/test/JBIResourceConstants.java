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


/* i18n Resource Text on JBI Pages*/

public class JBIResourceConstants
{
    
        //Root Page Components Child Nodes
        public static final String JBI_ROOT_BC_NODE = "Binding Components";
        public static final String JBI_ROOT_SE_NODE = "Service Engines";
    
        //List Shared Table Constants
        public static final String JBI_LIST_TABLE_DESC_HEADER  = "Description";
        public static final String JBI_LIST_TABLE_STATE_HEADER = "State";
        public static final String JBI_LIST_TABLE_NAME_HEADER  = "Name";
        public static final String JBI_LIST_TABLE_TYPE_HEADER  = "Type";
    
        //List Shared Library Page Text Constants
        public static final String JBI_LIST_LIB_PAGE_TITLE       = "Shared Libraries";
        public static final String JBI_LIST_LIB_PAGE_INLINE_HELP
                = "Manage Java Business Integration Shared Libraries";
        public static final String JBI_LIST_LIB_TABLE_TITLE_TEXT 
                = "JBI Shared Libraries";
    
    
        //List Components Page Text Constants
        public static final String JBI_LIST_COMP_PAGE_TITLE       = "Components";
        public static final String JBI_LIST_COMP_PAGE_INLINE_HELP 
                = "Manage Java Business Integration Binding Components and Service Engines";
        public static final String JBI_LIST_COMP_TABLE_TITLE_TEXT 
                = "JBI Binding Components and Service Engines";
        public static final String JBI_LIST_BC_TYPE_TEXT = "Binding";
        public static final String JBI_LIST_SE_TYPE_TEXT = "Engine";
        
         //List Service Assemblies Page Text Constants
        public static final String JBI_LIST_SA_PAGE_TITLE       = "Service Assemblies";
        public static final String JBI_LIST_SA_PAGE_INLINE_HELP 
                = "Manage Java Business Integration Service Assemblies";
        public static final String JBI_LIST_SA_TABLE_TITLE_TEXT 
                = "JBI Service Assemblies";
        
        //Lifecycle Operation Resource Constatns
        public static final String JBI_OPERATION_FAIL_ALERT_SUMMARY 
                = "Requested JBI LifeCycle operation for all selections failed";
        public static final String JBI_OPERATION_STARTED_STATE = "Started";
        public static final String JBI_OPERATION_STOPPED_STATE = "Stopped";
        public static final String JBI_OPERATION_SHUTDOWN_STATE = "Shutdown";
        
        
        //Wizard Component Steps Resource Constants
        public static final String JBI_COMP_INSTALL_WIZ_STEP1_TITLE
                = "Install JBI Binding or Engine (Step 1 of 2)";
        public static final String JBI_COMP_INSTALL_WIZ_STEP2_TITLE
                = "Install JBI Binding or Engine (Step 2 of 2)";
        public static final String JBI_COMP_INSTALL_CONF_PROP_TBL_TITLE
                = "Configuration Properties: Default and Override Values";
        public static final String JBI_COMP_INSTALL_PROP_TBL_TITLE
                = "Additional Properties";
        
        //Alert Messages for negative test archives
        public static final String JBI_EMPTY_ARCHIVE_MSG
                = "has no entries";
        public static final String JBI_NO_ARCHIVE_MSG
                = "File not found";
        public static final String JBI_CORRUPT_ARCHIVE_MSG
                = "not an archive";
        public static final String JBI_MISSING_JBI_XML_ARCHIVE_MSG
                = "Archive is missing required JBI XML";
        public static final String JBI_NOT_WELL_FORMED_JBI_XML_ARCHIVE_MSG
                = "Archive contains JBI XML that is not well formed";
        public static final String JBI_MISMATCHING_ARCHIVE_MSG
                = "The JBI Archive is not of the expected type";
        //Show Pages 
        public static final String JBI_SHOW_GENERAL_TAB
                = "General";
        public static final String JBI_SHOW_DESCRIPTOR_TAB
                = "Descriptor";
        
        //Show Component Pages
        public static final String JBI_SHOW_CONFIGURATION_TAB
                ="Configuration";
        public static final String JBI_SHOW_LOGGERS_TAB
                = "Loggers";
        public static final String JBI_SHOW_TARGETS_TAB
                = "Targets";
                
        //Deletion Message
        public static final String JBI_INSTALLABLE_DELETION_MSG
                = "Selected applications will be removed from this target.  Continue";
        
        public static final String JBI_SHOW_SL_INLINE_HELP
                = "View the details for this JBI Shared Library";
        public static final String JBI_SHOW_SA_INLINE_HELP
                = "View the details for this Java Business Integration Service Assembly";
        public static final String JBI_SHOW_DESCRIPTOR_INLINE_HELP
                = "View JBI archive metadata 'jbi.xml' contents";
        public static final String JBI_SHOW_TARGETS_SA_INLINE_HELP
                = "View target clusters and standalone server instances for this JBI Service Assembly";
        public static final String JBI_SHOW_TARGETS_SL_INLINE_HELP
                = "View target clusters and standalone server instances for this JBI Shared Library";
        
        
        //****************************************************************/
        /***********CLuster Profile Messages*****************************/
        //****************************************************************/
        
        
        //State of components
        public static final String JBI_EE_STATUS_ENABLED_ON_ALL_TARGETS
                = "Enabled on All Targets";
        public static final String JBI_EE_STATUS_DISABLED_ON_ALL_TARGETS
                = "Disabled on All Targets";
         public static final String JBI_EE_STATE_ENABLED_DROPDOWN
                = "Enabled";
         public static final String JBI_EE_STATE_DISABLED_DROPDOWN
                = "Disabled";
        //Deletion Message for Library uninstallation alert box
        public static final String JBI_EE_LIB_DELETION_MSG
                = "The selected Shared Library(ies) will be uninstalled from all targets and from the domain.  Continue?";
         
        //Deletion Message for SA undeployment alert box
        public static final String JBI_EE_SA_DELETION_MSG
                = "The selected  Service Assembly(ies) will be undeployed from all targets and from the domain.  Continue?";
        //Deletion Message for component uninstallation alert box
        public static final String JBI_EE_COMP_DELETION_MSG
                = "The selected component(s) will be uninstalled from all targets and from the domain.  Continue?";
        
        /**********************************************************************/
        /*****************Server Cluster Pages Related Messages**************/
        /**********************************************************************/
        
         public static final String JBI_EE_SERVER_CLUSTER_LIST_TABLE_HEADER_STATUS_ENABLED
                 = "Enabled";
        public static final String JBI_EE_STATUS_ENABLED_ON_SERVER_CLUSTER
                = "Enabled (Started)";
        public static final String JBI_EE_STATUS_DISABLED_ON_SERVER_CLUSTER
                = "Disabled (Shut Down)";
        
        public static final String JBI_EE_SERVER_CLUSTER_LIB_DELETION_MSG
                = "The selected Shared Library(ies) will be uninstalled from the current target only.  Continue?";
        
        public static final String JBI_EE_SERVER_CLUSTER_COMP_DELETION_MSG
                = "The selected component(s) will be uninstalled from the current target only.  Continue?";
        
        public static final String JBI_EE_SERVER_CLUSTER_SA_DELETION_MSG
                = "The selected Service Assembly(ies) will be undeployed from the current target only.  Continue?";
        
        public static final String JBI_EE_SERVER_CLUSTER_LIST_COMP_TABLE_TITLE_TEXT
                = "Binding Components and Service Engines";
        
       //Cluster Inline Page Help
        public static final String JBI_EE_CLUSTER_INLINE_HELP_SUFFIX
                = " on this cluster";
       
        public static final String JBI_EE_CLUSTER_LIST_LIB_PAGE_INLINE_HELP
                = JBI_LIST_LIB_PAGE_INLINE_HELP + JBI_EE_CLUSTER_INLINE_HELP_SUFFIX;
        
        public static final String JBI_EE_CLUSTER_LIST_COMP_PAGE_INLINE_HELP
                = JBI_LIST_COMP_PAGE_INLINE_HELP + JBI_EE_CLUSTER_INLINE_HELP_SUFFIX;
        
        public static final String JBI_EE_CLUSTER_LIST_SA_PAGE_INLINE_HELP
                = JBI_LIST_SA_PAGE_INLINE_HELP + JBI_EE_CLUSTER_INLINE_HELP_SUFFIX;
        
        //Stand Alone Instances 
        public static final String JBI_EE_SERVER_INLINE_HELP_SUFFIX
                = " on this stand-alone instance";
       
        public static final String JBI_EE_SERVER_LIST_LIB_PAGE_INLINE_HELP
                = JBI_LIST_LIB_PAGE_INLINE_HELP + JBI_EE_SERVER_INLINE_HELP_SUFFIX;
        
        public static final String JBI_EE_SERVER_LIST_COMP_PAGE_INLINE_HELP
                = JBI_LIST_COMP_PAGE_INLINE_HELP + JBI_EE_SERVER_INLINE_HELP_SUFFIX;
        
        public static final String JBI_EE_SERVER_LIST_SA_PAGE_INLINE_HELP
                = JBI_LIST_SA_PAGE_INLINE_HELP + JBI_EE_SERVER_INLINE_HELP_SUFFIX;
        
        
        
}