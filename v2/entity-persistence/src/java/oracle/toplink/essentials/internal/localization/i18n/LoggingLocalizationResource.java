/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.internal.localization.i18n;

import java.util.ListResourceBundle;

import oracle.toplink.essentials.internal.helper.Helper;


/**
 * English ResourceBundle for LoggingLocalization messages.
 *
 * @author Shannon Chen
 * @since TOPLink/Java 5.0
 */
public class LoggingLocalizationResource extends ListResourceBundle {
    static final Object[][] contents = {
        { "topLink_version", "TopLink, version: {0}" },
        { "application_server_name_and_version", "Server: {0}" },
        { "login_successful", "{0} login successful" },
        { "logout_successful", "{0} logout successful" },
        

        { "connected_user_database_driver", "Connected: {0}{6}User: {1}{6}Database: {2}  Version: {3}{6}Driver: {4}  Version: {5}" },
        { "connected_user_database", "Connected: {3}{4}User: {0}{3}{4}Database: {1}  Version: {2}" },
        { "JDBC_driver_does_not_support_meta_data", "Connected: unknown (JDBC Driver does not support meta data.)" },
        { "connecting", "connecting({0})" },
        { "disconnect", "disconnect" },
        { "reconnecting", "reconnecting({0})" },
        { "connected_sdk", "Connected: SDK" },
        
        { "no_session_found", "Could not find the session with the name [{0}] in the session.xml file [{1}]" },

        { "identitymap_for", "{0}{1} for: {2}" },
        { "includes", "(includes: " },
        { "key_object_null", "{0}Key: {1}{2}Object: null" },
        { "key_identity_hash_code_object", "{0}Key: {1}{2}Identity Hash Code: {3}{2}Object: {4}" },
        { "elements", "{0}{1} elements" },
        { "unitofwork_identity_hashcode", "{0}UnitOfWork identity hashcode: {1}" },
        { "deleted_objects", "Deleted Objects:" },
        { "all_registered_clones", "All Registered Clones:" },
        { "new_objects", "New Objects:" },
        

		{ "failed_to_propogate_to", "CacheSynchronization : Failed to propagate to {0}.  {1}" },
        { "exception_thrown_when_attempting_to_shutdown_cache_synch", "Exception thrown when attempting to shutdown cache synch: {0}" },
        { "corrupted_session_announcement", "SessionID: {0}  Discovery manager received corrupted session announcement - ignoring." },
        { "exception_thrown_when_attempting_to_close_listening_topic_connection", "Exception thrown when attempting to close listening topic connection: {0}" },
        { "retreived_unknown_message_type", "Retreived unknown message type: {0} from topic: {1}; ignoring" },
        { "retreived_null_message", "Retreived null message from topic: {0}; ignoring" },
        { "received_unexpected_message_type", "Received unexpected message type: {0} from topic: {1}; ignoring" },
        { "problem_adding_remote_connection", "Problem adding remote connection: {0}" },
        

        { "error_in_codegen", "Error during generation of concrete bean class." },
        { "error_during_PersistenceManager_setup_for_bean", "Error during PersistenceManager setup for bean: {0}" },
        { "error_in_create", "Error in create." },
        { "error_executing_ejbHome", "Error executing ejbHome: {0}" },
        { "error_in_remove", "Error in remove." },
        { "table_existed_during_creation", "Table creation failed. If it already exists, it must be dropped first. This can be done manually or by setting the db-table-gen attribute in orion-ejb-jar.xml." },
        { "an_error_occured_trying_to_undeploy_bean", "An error occurred trying to undeploy bean (after deployment failure): {0}" },
        { "an_error_occured_executing_findByPrimaryKey", "An error occurred executing findByPrimaryKey: {0}" },
        { "an_error_occured_preparing_bean", "An error occurred preparing bean for invocation: {0}" },
        { "an_error_executing_finder", "An error occurred executing finder: {0}" },
        { "an_error_executing_ejbSelect", "An error occurred executing ejbSelect: {0}" },
        { "ejbSelect2", "EjbSelect: {0}" },
        { "error_getting_transaction_status", "Error getting transaction status.  {0}" },
        { "removeEJB_return", "removeEJB return: {0}" },
        { "multiple_ds_not_supported", "TopLink CMP does not support multiple datasources, and will only use one of them named ({0}), which is specified in the orion-ejb-jar.xml, and being associated with an entity bean defined last in the corresponding ejb-jar.xml." },
        { "failed_to_find_mbean_server", "Failed to find MBean Server: {0}" },
        { "problem_while_registering", "Problem while registering: {0}" },
        { "objectchangepolicy_turned_off", "Change tracking turned off for: {0}" },
        { "External_transaction_controller_not_defined_by_server_platform", "The DatabaseSession has an external transaction controller defined " + "by something other than the ServerPlatform. TopLink will permit the " + "override of the external transaction controller, but we recommend " + "you consider the alternative of subclassing " + "oracle.toplink.essentials.platform.server.ServerPlatformBase " + "and override getExternalTransactionControllerClass()." },
        

        { "extra_cmp_field", "There is an abstract getter and/or setter defined on the [{0}] " + "abstract bean class but the corresponding cmp field [{1}] " + "is not declared in the ejb-jar.xml." },
        { "extra_ejb_select", "There is an abstract ejbSelect defined on the [{0}] " + "abstract bean class but the corresponding ejbSelect [{1}{2}] " + "entry is not declared in the ejb-jar.xml." },
        { "extra_finder", "There is a finder defined on the [{0}] " + "home interface(s) but the corresponding finder [{1}{2}] " + "entry is not declared in the ejb-jar.xml." },
        { "cmp_and_cmr_field", "The ejb-jar.xml entry for [{0}] contains both a <cmp-field> and <cmr-field> entry for the attribute [{1}].  The <cmp-field> entry will be ignored." },
        

        { "toplink_cmp_bean_name_xml_deprecated", "Support for toplink-cmp-bean_name.xml is deprecated." + "Please refer to the documentation for the use of toplink-ejb-jar.xml" },
        

        { "drop_connection_on_error", "Warning: Dropping remote command connection to {0} on error {1}" },
        { "received_corrupt_announcement", "Warning: Discovery manager could not process service announcement due to {0} - ignoring announcement" },
        { "missing_converter", "Warning: Cannot convert command {0} due to missing CommandConverter - ignoring command" },
        { "failed_command_propagation", "Error: Failed trying to propagate command to {0} due to {1}" },
        { "exception_thrown_when_attempting_to_close_connection", "Warning: exception thrown when attempting to close connection" },
        { "error_executing_remote_command", "{0} command failed due to: {1}" },
        { "problem_adding_connection", "Could not add remote connection from {0} due to error: {1}" },
        { "problem_reconnect_to_jms", "Could not reconnect to JMS Topic name {0} due to error: {1}" },
        

        { "toplink_severe", "[TopLink Severe]: " },
        { "toplink_warning", "[TopLink Warning]: " },
        { "toplink_info", "[TopLink Info]: " },
        { "toplink_config", "[TopLink Config]: " },
        { "toplink_fine", "[TopLink Fine]: " },
        { "toplink_finer", "[TopLink Finer]: " },
        { "toplink_finest", "[TopLink Finest]: " },
        { "toplink", "[TopLink]: " },
        { "an_error_occured_initializing_dms_listener", "Exception thrown when initializing DMS embedded listener and the SPY Servlet" },
        

        { "input_minimum_arguments", "The command line input arguments must at least include -s, -a or -x, and -o." },
        { "src_pm_name_first_argument", "You must specify the source PM name at the first input argument as either: -sOc4j-native or -sWeblogic." },
        { "oc4j_native_migration_start", "OC4J-Native CMP -> OC4J TopLink CMP Migration STARTS......" },
        { "weblogic_native_migration_start", "WebLogic-Native CMP -> OC4J TopLink CMP Migration STARTS......" },
        { "must_define_migration_output_dir", "You must define an output directory for the migration tool" },
        { "migration_output_dir_not_valid", "The output directory ({0}) you defined is not valid" },
        { "migration_input_dir_not_valid", "The input directory ({0}) you defined is not valid" },
        { "input_and_output_dir_be_different", "You must define an output directory different from the input directory." },
        { "input_archive_format_not_supported", "Migration utility supports .ear and .jar and input archive format. The input file format as ({0}) is not supported." },
        { "archive_not_found_in_input", "The archive file ({0}) is not existed under input directory ({1})." },
        { "input_not_both_archive_and_xml", "You use either -e to specify the archive file name, or -x to signal that descriptor files under the input directory will be migrated, but not both." },
        { "input_at_least_either_archive_or_xml", "You use either -e to specify the migarted archive file name, or -x to signal that descriptor xml files under the input directory will be migrated, and you must specify and only specify one of them." },
        { "ejb_jar_xml_not_found_in_input", "The ejb-jar.xml is not present under input directory ({0})." },
        { "orion_ejb_jar_xml_not_found_in_input", "orion-ejb-jar.xml is not existed under input directory ({0}) you specified." },
        { "weblogic_ejb_jar_xml_not_found_in_input", "weblogic-ejb-jar.xml is not existed under input directory ({0}) you specified." },
        { "toplink_ejb_jar_xml_found_in_input", "The toplink-ejb-jar.xml is under input directory ({0}). You have to remove the toplink descriptor away from the input directory to process the migration." },
        { "migration_successful", "Migration Successful!" },
        { "migration_failed", "Migration Failed." },
        { "mw_project_generated_and_under", "The migrated TopLink Mapping Workbench project files are under ({0})." },
        { "log_file_under_output_dir", "There is a log file called ({0}) under output directory ({1})." },
        { "parse_ejb_jar_with_validation_fails", "Parsing ejb-jar.xml with validation fails with error ({0}). The migration tool will parse the xml file without validation."},
        { "jar_entry_not_migratable", "The jar entry ({0}) in the input EAR file ({1}) is not migratable." },
        { "jar_entry_has_been_migrated", "The native cmp descriptor file in the jar entry ({0}) from the input EAR file ({1}) has been migrated." },
        { "no_jar_entry_migratable_in_ear", "None of the jar entry in the input EAR file ({0}) is migratable." },
        { "invalid_command_line_argument", "The command line argument ({0}) is invalid" },
        { "column_size_not_migrated", "DB column size ({0}) is not migrated. See migration doc for details." },
        { "associate_using_third_table_not_migrated", "Oc4j native CMP feature -DassociateUsingThirdTable=true to map 1:m using a relation table is not migrated. You must re-map cmr field ({1}) of the entity ({0}) as one-to-many mapping through Mapping Workbench after the migration process." },
        { "delay_updates_until_commit_not_supported", "Oc4j native CMP feature 'delay-updates-until-commit' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "do_select_before_insert_not_supported", "Oc4j native CMP feature 'do-select-before-insert' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "no_exclusive_write_access_not_supported", "Oc4j native CMP feature 'no-exclusive-write-access' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "force_update_not_supported", "Oc4j native CMP feature 'force-update' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "isolation_level_not_supported", "Oc4j native CMP setting 'isolation-level' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "force_update_not_supported", "Oc4j native CMP feature 'primarykey-lazy-loading' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "max_instance_not_supported", "Oc4j native CMP setting 'max-instance' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "max_tx_retries_not_supported", "Oc4j native CMP setting 'max-tx-retries' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "min_instance_not_supported", "Oc4j native CMP setting 'min-instance' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "update_all_fields_not_supported", "Oc4j native CMP feature 'update-all-fields' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "validity_timeout_not_supported", "Oc4j native CMP setting 'validity-timeout' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "call_timeout_not_migrated", "The call-timeout value={1} is defined in entity {0} in orion-ejb-jar.xml" + " but no persistent mappings defined in the descriptor file, therefore no migration will occur. " + " If you use TopLink default mapping later to generate the TopLink mapping descriptor, be aware that the call-timeout " + " setting will be lost as TopLink default mapping has no access to native descriptor file. " + " You then need to reset the call timeout mamually or through TopLink Mapping Workbench after default mapping generation." },
        { "optimistic_locking_not_supported", "Oc4j native CMP setting locking-mode='optimistic' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "old_pessimistic_locking_not_supported", "Oc4j native CMP setting 'old-pessimistic-locking' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "locking_mode_not_valid", "Locking mode({1}) on entity({0} in orion-ejb-jar.xml is not well defined and therefore not migrated." },
        { "verifiy_columns_read_locking_not_supported", "Optimistic setting 'Read' on 'verifiy-columns' in entity ({0}) is not directly supported in TopLink CMP. See migration doc for details.." },
        { "verifiy_rows_read_locking_not_supported", "Optimistic setting 'Read' on 'verifiy-rows' in entity ({0}) is not directly supported in TopLink CMP. See migration doc for details.." },
        { "weblogic_ql_not_supported", "WebLogic-QL({0}) of the method({1} of the entity({2}) is not migrated as TopLink does not support WebLogic QL language." },
        { "input_orione_ejb_jar_augmented", "Input orion-ejb-jar.xml file has been augmented to specify TopLink as OC4J's Persistent Manager" },
        { "template_orion_ejb_jar_created", "A templated orion-ejb-jar.xml file has been created" },
        { "create_default_dbms_tables_not_supported", "WLS native CMP setting 'create-default-dbms-tables' is not directly supported in TopLink CMP. See migration doc for details." },
        { "default_dbms_tables_ddl_not_supported", "WLS native CMP setting 'default-dbms-tables-ddl' is not directly supported in TopLink CMP. See migration doc for details." },
        { "enable_batch_operations_as_true_not_supported", "WLS native CMP setting 'enable-batch-operations-as-true' is not directly supported in TopLink CMP. See migration doc for details." },
        { "validate_db_schema_with_not_supported", "WLS native CMP setting 'validate-db-schema-with' is not directly supported in TopLink CMP. See migration doc for details." },
        { "automatic_key_generation_not_supported", "WLS native CMP setting 'automatic-key-generation' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "check_exist_on_method_as_true_not_supported", "WLS native CMP setting 'check-exists-on-method-as-true' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "delay_database_insert_until_ejb_create_not_supported", "WLS native CMP setting 'delay-database-insert-until-ejbCreate' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "delay_database_insert_until_ejb_post_create_not_supported", "WLS native CMP setting 'delay-database-insert-until-ejbPostCreate' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "field_group_not_supported", "WLS native CMP setting 'field-group' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "relationship_cacheing_not_supported", "WLS native CMP setting 'relationship-caching' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "weblogic_query_not_supported", "WLS native CMP setting 'weblogic-query' on entity({0}) is not directly supported in TopLink CMP. See migration doc for details." },
        { "sequence_cachekey_improper_format", "WLS native CMP setting 'key-cache-size' on entity ({0}) is ill-formatted with value ({1})" },
        { "dir_cleaned_for_mw_files", "Files and sub-directories under directory {0} have been deleted in order to create a clean directory for the new generated TopLink Mapping Workbench project files" },
        { "mapping_not_supported_by_mw", "The TopLink mapping {0} is not supported by the mapping workbench" },
        { "toplink_ejb_jar_in_jar", "toplink-ejb-jar.xml is included in jar({0}) file, no migration therefore will be performed for this jar." },
        { "migration_tool_usage", "The Oracle TopLink Migration Tool Usage:" + Helper.cr() + Helper.getTabs(1) + "java oracle.toplink.essentials.tools.migration.TopLinkCmpMigrator {0}<''{1}''|''{2}''> [{3}<input dir>] [{4}[<ear>|<jar>] | {5}] {6}<output dir> [{7}] " + Helper.cr() + "Where:" + Helper.cr() + Helper.getTabs(1) + "{0}: The name of the native PM of the source input file(s). It is either ''Oc4j-native'' or ''WebLogic''." + Helper.cr() + Helper.getTabs(1) + "{3}: Input directory contains to-be-migrated descriptor files or archive file containing them. If not specified, the current directory is used as input directory." + Helper.cr() + Helper.getTabs(1) + "{4}: The archive (.ear or .jar) file containing ejb-jar.xml and native descriptor file like orion-ejb-jar.xml with the native CMP defined to be migrated." + Helper.cr() + Helper.getTabs(1) + "{5}: It signals that the descriptor files under the input directory is to be migrated." + Helper.cr() + Helper.getTabs(1) + "{6}: The (absolute or relative to the current) output directory that the migrated file(s) and log file output into. It must be specified." + Helper.cr() + Helper.getTabs(1) + "{7}: Verbose mode. The verbose logging messages would be printed out to the console." + Helper.cr() },
        { "migration_tool_usage_exmaple", "Examples:" + Helper.cr() + Helper.getTabs(1) + "Example-1 (Oc4j-native ->Oc4j TopLink):  java oracle.toplink.essentials.tools.migration.TopLinkCmpMigrator " + "-sOc4j-native -iC:/mywork/in -aEmployee.ear -oC:/mywork/out -v" + Helper.cr() + Helper.getTabs(1) + "Example-2 (WebLogic -> Oc4j TopLink) :  java oracle.toplink.essentials.tools.migration.TopLinkCmpMigrator " + "-sWebLogic -x -oC:/mywork/out" },
        { "jta_cannot_be_disabled_in_cmp", "When using Container Managed Persistence (CMP), JTA cannot be disabled. TopLink will act as if JTA is enabled." },
    
        { "metadata_default_alias", "The alias name for the entity class [{0}] is being defaulted to: {1}." },
        { "metadata_default_map_key_attribute_name", "The map key attribute name for the mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_table_name", "The table name for entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_table_schema", "The table schema for entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_table_catalog", "The table catalog for entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_join_table_name", "The join table name for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_join_schema", "The join table schema for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_join_catalog", "The join table catalog for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_secondary_table_name", "The secondary table name for the entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_secondary_schema", "The secondary table name for the entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_secondary_catalog", "The secondary table name for the entity [{0}] is being defaulted to: {1}." },
        { "metadata_default_column", "The column name for element [{0}] is being defaulted to: {1}." },
        { "metadata_default_pk_column", "The primary key column name for the mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_fk_column", "The foreign key column name for the mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_source_pk_column", "The source primary key column name for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_source_fk_column", "The source foreign key column name for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_target_pk_column", "The target primary key column name for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_target_fk_column", "The target foreign key column name for the many to many mapping [{0}] is being defaulted to: {1}." },
        { "metadata_default_discriminator_column", "The discriminator column name for the root inheritance class [{0}] is being defaulted to: {1}." },
        { "metadata_default_inheritance_pk_column", "The primary key column name for the inheritance class [{0}] is being defaulted to: {1}." },
        { "metadata_default_inheritance_fk_column", "The foreign key column name for the inheritance class [{0}] is being defaulted to: {1}." },
        { "metadata_default_secondary_table_pk_column", "The secondary table primary key column name for element [{0}] is being defaulted to: {1}." },
        { "metadata_default_secondary_table_fk_column", "The secondary table foreign key column name for element [{0}] is being defaulted to: {1}." },
        { "metadata_default_one_to_one_mapping", "Element [{0}] is being defaulted to a one to one mapping." },
        { "metadata_default_one_to_many_mapping", "Element [{0}] is being defaulted to a one to many mapping." },
        { "metadata_default_one_to_one_reference_class", "The target entity (reference) class for the one to one mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_one_to_many_reference_class", "The target entity (reference) class for the one to many mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_many_to_one_reference_class", "The target entity (reference) class for the many to one mapping element [{0}] is being defaulted to: {1}." },
        { "metadata_default_many_to_many_reference_class", "The target entity (reference) class for the many to many mapping element [{0}] is being defaulted to: {1}." },
        
        { "metadata_warning_ignore_mapping", "A mapping for the element [{1}] is already defined on the descriptor for the entity [{0}]. Ignoring the mapping annotation." },
        { "metadata_warning_ignore_version_locking", "An optimistic locking policy is already defined on the descriptor for the entity [{0}]. Ignoring version specification on element [{1}]." },
        { "metadata_warning_ignore_lob", "Ignoring lob specification on element [{1}] within entity class [{0}] since a @Convert is specified." },
        { "metadata_warning_ignore_temporal", "Ignoring temporal specification on element [{1}] within entity class [{0}] since a @Convert is specified." },
        { "metadata_warning_ignore_serialized", "Ignoring default serialization on element [{1}] within entity class [{0}] since a @Convert is specified." },
        { "metadata_warning_ignore_enumerated", "Ignoring enumerated specification on element [{1}] within entity class [{0}] since a @Convert is specified." },
        { "metadata_warning_ignore_primary_key", "Primary key fields are already defined on the descriptor for the entity [{0}]. Ignoring id specification on element [{1}]." },
        { "metadata_warning_ignore_embedded_id", "Primary key fields are already defined on the descriptor for the entity [{0}]. Ignoring embedded id specification on element [{1}]." },
        { "metadata_warning_ignore_inheritance", "Inheritance information is already defined on the descriptor for the entity [{0}]. Ignoring inheritance information." },
        { "metadata_warning_ignore_basic_fetch_lazy", "Ignoring LAZY fetch type on element [{1}] within entity class [{0}]. All basic mappings default to use EAGER fetching." },
        
        { "annotation_warning_ignore_annotation", "The annotation [{0}] on the element [{1}] is being ignored because of an XML metadata-complete setting of true for this class." },
        { "annotation_warning_ignore_id_class", "Primary key fields are already defined on the descriptor for the entity [{0}]. Ignoring @IdClass [{1}]." },
        { "annotation_warning_ignore_table", "Table information is already defined on the descriptor for the entity [{0}]. Ignoring any @Table information and defaults." },
        { "annotation_warning_ignore_secondary_table", "Table information is already defined on the descriptor for the entity [{0}]. Ignoring any @SecondaryTable(s) information." },
        { "annotation_warning_ignore_named_query", "Ignoring the @NamedQuery [{1}] specified on class [{0}] since a query with that name already exists." },
        { "annotation_warning_ignore_named_native_query", "Ignoring the @NamedNativeQuery [{1}] specified on class [{0}] since a query with that name already exists." },
        
        { "orm_warning_exception_loading_orm_xml_file", "An exception was thrown loading ORM XML file [{0}] : [{1}]" },
        { "orm_could_not_find_orm_xml_file", "ORM XML file [{0}] could not be found." },
        { "orm_warning_ignore_mapping_on_write", "Only DirectToField and TypeConversion (BLOB/CLOB) mappings are allowed for embeddable-attributes.  The mapping for the attribute [{1}] on the descriptor for the entity [{0}] will not be written to the entity-mappings file." },
        { "orm_warning_ignore_embedded_id", "Primary key fields are already defined on the descriptor for the entity [{0}]. Ignoring embedded-id primary key fields on attribute [{1}]." },
        { "orm_warning_ignore_id_class", "Primary key fields are already defined on the descriptor for the entity [{0}]. Ignoring id-class [{1}]." },        
        { "orm_warning_ignore_query_hint_unsupported_type", "Ignoring the query hint [{0}] for named/named-native query [{1}] on entity class [{2}] as it is not supported in XML."},
        { "orm_warning_ignore_query_hint_unknown_type", "Ignoring the query hint [{0}] for named/named-native query [{1}] on entity class [{2}] as its type cannot be determined." },
        { "orm_warning_ignore_named_query", "Ignoring the named-query [{1}] defined in the XML document [{0}] as a query with that name already exists." },
        { "orm_warning_ignore_named_native_query", "Ignoring the named-native-query [{1}] defined in the XML document [{0}] as a query with that name already exists." },
        { "orm_warning_ignore_table", "Table information is already defined on the descriptor for the entity [{0}]. Ignoring any table element information or defaults." },
        { "orm_warning_ignore_secondary_table", "Table information is already defined on the descriptor for the entity [{0}]. Ignoring secondary-table element information." },
        
        { "weaver_null_project", "Weaver session'''s project cannot be null"},
        { "weaver_disable_by_system_property", "Weaving disabled by system property {0}"},		
        { "weaver_change_tracking_disabled_missing_field", "Weaving for change tracking not enabled for class [{0}] because it does not have field [{1}]."},
        { "weaver_change_tracking_disabled_not_supported", "Weaving for change tracking not enabled for class [{0}] because it is not supported by the mapping for field [{1}]."},
        { "weaver_not_overwriting", "Weaver is not overwriting class {0} because it has not been set to overwrite."},
        { "weaver_could_not_write", "Weaver encountered an exception while trying to write class {0} to the file system.  The exception was: {1}"},
        { "weaver_failed", "Weaver failed while processing class [{0}]. The exception was: {1}"},

        { "relational_descriptor_support_only", "The default table generator currently only supports generating default table schema from a relational project."},

        { "config_factory", "Config factory: ({0}) = ({1})"},
        { "class_list_created_by", "Class list created by ({0}).({1})() method."},
        { "jar_file_url_exception", "Exception while parsing persistence.xml.  Jar file location could not be found: {0}"},
        { "error_loading_xml_file", "Exception while loading ORM xml file: {0}: {1}"},
        { "exception_loading_entity_class", "An exception while trying to initialize persistence.  {1} occured while trying to load entity class: {0}."},
    
        { "update_all_query_cannot_use_binding_on_this_platform", "UpdateAllQuery cannot use binding on this database platform. Changed query setting to execute without binding." },

        { "dbPlaformHelper_defaultingPlatform", "Not able to detect platform for vendor name [{0}]. Defaulting to [{1}]. The database dialect used may not match with the database you are using. Please explicitly provide a platform using property 'toplink.platform.class.name'."},
        { "dbPlaformHelper_noMappingFound", "Can not load resource [{0}] that loads mapping from vendor name to database platform. Autodetection of database platform will not work."},
        
        { "resource_local_persistence_init_info_ignores_jta_data_source", "PersistenceUnitInfo {0} has transactionType RESOURCE_LOCAL and therefore jtaDataSource will be ignored"},
        { "deprecated_property", "property {1} is deprecated, property {0} should be used instead."},
        { "persistence_unit_processor_error_loading_class", "{0}: {1} was thrown on attempt of PersistenceLoadProcessor to load class {2}. The class is ignored."},
        
        { "attempted_to_open_url_as_jar", "{1} was thrown on attempt to open {0} as a jar."},
        { "attempted_to_open_url_as_directory", "{1} was thrown on attempt to open {0} as a directory."},
        { "attempted_to_open_entry_in_url_as_jar", "{2} was thrown on attempt to open {0} as a jar and access entry: {1}."},
        { "attempted_to_open_file_url_as_directory", "{2} was thrown on attempt to open {0} as a directory and access entry: {1}."},
        { "invalid_datasource_property_value", "{1} is not a valid object to be passed in for property {0}.  Valid values are String or instances of javax.sql.DataSource."}
        
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
