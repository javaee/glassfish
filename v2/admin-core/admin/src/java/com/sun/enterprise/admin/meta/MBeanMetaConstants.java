/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 *   $Id: MBeanMetaConstants.java,v 1.6 2006/05/08 17:18:53 kravtch Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanMetaConstants.java,v $
 *   Revision 1.6  2006/05/08 17:18:53  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.5  2006/03/12 01:26:56  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.4  2005/12/25 03:47:36  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.3  2005/08/16 22:19:31  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:43  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.10  2004/11/14 07:04:20  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.9  2004/06/04 19:13:59  kravtch
 *   Reviewer: Nazrul
 *   Support for "dynamicallyReconfigurable" MBean descriptor field is added to infrastructure.
 *   Tests passed: QLT PE/EE
 *
 *   Revision 1.8  2004/03/02 18:26:32  kravtch
 *   MBean's Descriptor field ElementChangeEvent support added (Constant, get method).
 *   MBeanRegistryFactory.setAdminMBeanRegistry() added for tester
 *
 *   Revision 1.7  2004/02/20 03:56:14  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.6.4.2  2004/02/02 07:25:18  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6.4.1  2003/12/23 01:51:45  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.6  2003/10/11 00:00:03  kravtch
 *   Bug 4933034
 *   Reviewer: Abhijit
 *   New field in attribute descriptor marks attributes which allows empty values.
 *   admin-descrptors file modified for "http-listener" mbean, adding:
 *       <attribute name="server_name" >
 *           <descriptor>
 *              <field name="emptyValueAllowed" value="true" />
 *           </descriptor>
 *       </attribute>
 *   BaseConfigMBean class modified to analyse this flag and properly perform setAttribute().
 *
 *   Revision 1.5  2003/08/07 00:41:06  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.4  2003/07/18 20:14:44  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.3  2003/06/25 20:03:40  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.FeatureInfo;
import com.sun.org.apache.commons.modeler.FieldInfo;
import com.sun.org.apache.commons.modeler.AttributeInfo;
import com.sun.org.apache.commons.modeler.OperationInfo;
import com.sun.org.apache.commons.modeler.ParameterInfo;

//JMX imports
import javax.management.Descriptor;
import javax.management.ObjectName;
import javax.management.AttributeList;
import com.sun.enterprise.config.ConfigBeansFactory;

/**
 *
 */
public interface MBeanMetaConstants
{
    final public static char ATTRIBUTE_NAME_DELIMITER_SYMBOL   = '-';

    //
    final public static String JSR77BEAN_FIELD_NAME   = "Jsr77MdlBeanClass";
    final public static String CLINAME_FIELD_NAME     = "CLIName";
    final public static String XPATH_FIELD_NAME       = "xpath";
    final public static String OBJECTNAME_FIELD_NAME  = "ObjectName";
    final public static String PRINTNAME_FIELD_NAME  = "printName";

    //descriptor
    final public static String GETTER_FIELD_NAME = "getter";
    final public static String SETTER_FIELD_NAME = "setter";
    
    final public static String CHILD_FIELD_NAME = "child";
    final public static String MULTI_FIELD_NAME = "multi";
    final public static String NMTYPE_FIELD_NAME = "namingType";
    final public static String NMLOCATION_FIELD_NAME = "namingLocation";
    final public static String DOMAIN_FIELD_NAME = "domainName";
    final public static String EMPTYVALUEALLOWED_FIELD_NAME = "emptyValueAllowed";
    final public static String ELEMENTCHANGEEVENT_FIELD_NAME = "elementChangeEvent";
    final public static String DYNAMICALLY_RECONFIGURABLE_LIST_FIELD_NAME = "dynamicallyReconfigurable";

    final public static String WHERE_LOCATED_FIELD_NAME = "bean";
    final public static     String LOCATED_IN_MBEAN       = "MBEAN";
    final public static     String LOCATED_IN_CONFIGBEAN  = "CBEAN";
    final public static     String LOCATED_IN_RUNTIMEBEAN = "RBEAN";

    final public static String GET_LISTNAMES_OP_SUFFIX  = "NamesList";

    final public static String CONFIG_BEAN_REF          = "ConfigBeanReference";
    final public static String JSR77_MODEL_BEAN_REF     = "Jsr77ModelBeanReference";
    
    //introspector modes
    final static int  EXPOSE_GETTERS        = 0x0001;
    final static int  EXPOSE_SETTERS        = 0x0002;
    final static int  EXPOSE_CREATECHILD    = 0x0004;
    final static int  EXPOSE_GETCHILD       = 0x0008;
    final static int  EXPOSE_DESTROYCHILD   = 0x0010;

    final static int  EXPOSE_ALL            = 0xFFFF;
    final static int  EXPOSE_RUNTIME_WITH_MODEL    = EXPOSE_GETTERS;
    final static int  EXPOSE_RUNTIME_WITHOUT_MODEL = (EXPOSE_GETTERS+EXPOSE_GETCHILD);
    
    // config mbean target types
    final public static int  TARGET_TYPE_DOMAIN        = 1;
    final public static int  TARGET_TYPE_SERVER        = 2;
    final public static int  TARGET_TYPE_CLUSTER       = 3;
    final public static int  TARGET_TYPE_CONFIG        = 4;
    final public static int  TARGET_TYPE_NODEAGENT     = 5;
    final public static int  TARGET_TYPE_APPLICATION   = 6;
    final public static int  TARGET_TYPE_RESOURCE      = 7;
    
}
