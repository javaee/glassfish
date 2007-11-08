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
 *   $Id: MBeanNamingInfo.java,v 1.4 2006/05/08 17:18:54 kravtch Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanNamingInfo.java,v $
 *   Revision 1.4  2006/05/08 17:18:54  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.3  2005/12/25 03:47:39  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:45  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.10  2004/11/14 07:04:22  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.9  2004/10/25 01:49:09  sg112326
 *   fix for bug # 6157490, 6189188
 *
 *   Ran EE QL
 *
 *   Revision 1.8  2004/10/22 22:07:33  kravtch
 *   Dots in element's names are escaped during dotted names registration.
 *   Reviewer: Sridatta
 *   Bug #6182305
 *   Tests: QLT PE/EE; devtest
 *
 *   Revision 1.7  2004/02/20 03:56:16  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.6.4.1  2004/02/02 07:25:21  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6  2003/08/15 23:08:29  kravtch
 *   DottedName Support (generation and call to manager)
 *   notifyRegisterMBean/UnregisterMBean are implemented;
 *   dotted name related opeartions are added to NaminDescriptor and NamingInfo
 *   removeChild support is added;
 *
 *   Revision 1.5  2003/07/29 18:59:36  kravtch
 *   MBeanRegistryEntry:
 *   	- support for toFormatString();
 *   	- instantiateMBean() method modified to instantiate runtime MBeans as well;
 *   MBeanRegistryFactory:
 *   	- fixed bug in getRuntimeRegistry();
 *   MBeanNamingInfo:
 *   	- less strict requirements for parm_list_array size in constructor (can be more then needed);
 *   BaseRuntimeMBean:
 *   	- exception ClassCastException("Managed resource is not a Jsr77ModelBean") handling;
 *   ManagedJsr77MdlBean:
 *   	- call managed bean bug fixed ( getDeclaredMethod()->getMethod())
 *   admin/dtds/runtime-mbeans-descriptors.xml - modified to represent new runtime mbeans;
 *
 *   Revision 1.4  2003/06/25 20:03:41  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta.naming;

//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

//i18n import
//import com.iplanet.ias.util.i18n.StringManager;


/**
 * Provides naming support for Mbeans
 */
public class MBeanNamingInfo
{
    String[] m_ids = null;
    MBeanNamingDescriptor m_descr = null;

	// i18n StringManager
//	private static StringManager localStrings =
//		StringManager.getManager( MBeanNamingInfo.class );

    public MBeanNamingInfo(/*int category, */ MBeanNamingDescriptor descr, String type, String[] params) throws MBeanNamingException
    {
       this(/*category,*/ descr, type, params, true);
    }
    
    public MBeanNamingInfo(/*int category,*/ MBeanNamingDescriptor descr, String type, String[] params, boolean bTestParamSize) throws MBeanNamingException
    {
        m_descr = descr;
        if(m_descr==null) {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_type" + type );
            throw new MBeanNamingException( msg );
		}

        if(bTestParamSize)
        {
            int parmSize = (params==null)?0:params.length;
            if(m_descr.getParmListSize()>parmSize) {
				String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.wrong_parameters_array_size"+ type );
                throw new MBeanNamingException( msg );
			}
        }
        
        m_ids   = new String[m_descr.getParmListSize()];
        for(int i=0; i<m_ids.length; i++)
            m_ids[i] = params[i];
    }
    
    public MBeanNamingInfo(MBeanNamingDescriptor descr, String dottedName) throws MBeanNamingException, MalformedObjectNameException
    {
        m_descr = descr; //MBeansNaming.findNamingDescriptor(dottedName);
        if(m_descr==null) {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_dotted_name"+dottedName );
            throw new MBeanNamingException( msg );
		}
        m_ids   = m_descr.extractParmList(dottedName);
    }
    
    public MBeanNamingInfo(MBeanNamingDescriptor descr, ObjectName objectName) throws MBeanNamingException
    {
        m_descr = descr; //MBeansNaming.findNamingDescriptor(objectName);
        if(m_descr==null) {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_not_found_for_object_name"+ objectName );
            throw new MBeanNamingException( msg );
		}
        m_ids   = m_descr.extractParmList(objectName);
    }
    
    //******************************M A P P I N G S***************************
    public ObjectName getObjectName() throws MalformedObjectNameException
    {
        return m_descr.createObjectName(m_ids);
    }

    private String escapeString(String str, String idPrefix)
    {
        if(str == null)
            return null;
        if(idPrefix==null)
            return str.replace('.', '`').replaceAll("`","\\\\.");
        else
            return idPrefix+str.replace('.', '`').replaceAll("`","\\\\.");
    }
    
    private String[] escapeStrings(String[] arr, String idPrefix)
    {
        if(arr == null)
            return null;
        String[] ret = new String[arr.length];
        for(int i=0; i<arr.length; i++)
        {
            ret[i] = escapeString(arr[i], idPrefix);
        }
        return ret;
    }
    public String[] getDottedNames() throws MalformedObjectNameException
    {
        return getDottedNames(null);
    }
    
    public String[] getDottedNames(String idPrefix) throws MalformedObjectNameException
    {
        return m_descr.createDottedNames(escapeStrings(m_ids, idPrefix));
    }
    
    public String[] getLocationParams() 
    {
        return m_ids;
    }

    public String getXPath() 
    {
        return m_descr.createXPath(m_ids);
    }

    public String getType() 
    {
        return m_descr.getType();
    }

    public int getMode() 
    {
        return m_descr.getMode();
    }

    public boolean isModeConfig() 
    {
        int mode = m_descr.getMode();
        return ((mode&MBeansNaming.MODE_CONFIG)!=0);
    }

    public boolean isModeMonitorable() 
    {
        int mode = m_descr.getMode();
        return ((mode&MBeansNaming.MODE_MONITOR)!=0);
    }

/*    public ConfigMBeanBase constructConfigMBean() throws MBeanNamingException
    {
        String className = MBeansNaming.CONFIG_MBEANS_BASE_CLASS_PREFIX + m_descr.getMBeanClassName();
        Class cl;
        ConfigMBeanBase configMBean;
        try
        {
            cl = Class.forName(className);
            //create configMBean by defaul constructor;
            configMBean  = (ConfigMBeanBase)cl.newInstance();
        }
        catch (Exception e)
        {
			String msg = ( "admin.server.core.mbean.config.naming.mbeannamingdescriptor_couldnot_create_mbean_class"+ className );
            throw new MBeanNamingException( msg );
        }
        configMBean.initialize(this);
        return configMBean;
    }
*/
    public String getServerInstanceName()  throws MBeanNamingException
    {
        if(m_ids==null || m_ids.length==0)
        {
            String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.wrong_parameters_array_size"+ m_descr.getType() );
            throw new MBeanNamingException( msg );
        }
        return m_ids[0];
    }

}
