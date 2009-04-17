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
 * accompanied this code.  If applicable, add the following below the Licensep
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
package org.glassfish.admin.amx.impl.mbean;

import java.util.Set;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.Tools;
import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

public class ToolsImpl extends AMXImplBase // implements Tools
{

    public ToolsImpl(final ObjectName parent) {
        super(parent, Tools.class);
    }

    private static ObjectName newObjectName(final String s )
    {
        try {
            return new ObjectName(s);
        }
        catch (final Exception e ) {
        }
        return null;
    }
    
    static private final String WILD_SUFFIX = ",*";
    static private final String WILD_ALL = "*";
    
    public String getInfo()
    {
        return info( "*" );
    }
    
    
    public String infoType( final String type)
    {
        return info( "*:type=" + type + WILD_SUFFIX );
    }
    
    public String info(final String searchStringIn) {
        final String domain = getObjectName().getDomain();
        final MBeanServer server = getMBeanServer();
        
        ObjectName pattern = newObjectName(searchStringIn);
        if ( pattern == null && ( searchStringIn.length() == 0 || searchStringIn.equals(WILD_ALL)) )
        {
            pattern = newObjectName("*:*");
        }
        
        if ( pattern == null )
        {
            String temp = searchStringIn;
            
            final boolean hasProps = temp.indexOf("=") > 0;
            final boolean hasDomain = temp.indexOf(":") >= 0;
            final boolean isPattern = temp.endsWith(WILD_SUFFIX);
            
            if ( ! (hasProps || hasDomain || isPattern) )
            {
                // try it as a type
                pattern = newObjectName( "*:type=" + temp + WILD_SUFFIX );
                
                // if no luck try it as a j2eeType
                if ( pattern == null )
                {
                    pattern = newObjectName( "*:j2eeType=" + temp + WILD_SUFFIX );
                }
                
                // if no luck try it as a name
                if ( pattern == null )
                {
                    pattern = newObjectName( "*:name=" + temp + WILD_SUFFIX );
                }
            }        
                
            if ( pattern == null ) {
                return "No MBeans found for: " + searchStringIn;
            }
        }
        
        final Set<ObjectName> objectNames = server.queryNames( pattern, null);
        
        final String NL = StringUtil.NEWLINE();
        final StringBuffer buf = new StringBuffer();
        for( final ObjectName objectName : objectNames )
        {
            final MBeanInfo mbeanInfo = ProxyFactory.getInstance(server).getMBeanInfo(objectName);
            
            buf.append( "MBeanInfo for " + objectName + NL);
            buf.append( JMXUtil.toString(mbeanInfo) );
            buf.append( NL + NL + NL + NL );
        }
        
        buf.append( "Matched " + objectNames.size() + " mbean(s)." );
        
        return buf.toString();
    }
}




























