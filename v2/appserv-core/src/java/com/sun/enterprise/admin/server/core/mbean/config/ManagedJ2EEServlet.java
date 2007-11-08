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

package com.sun.enterprise.admin.server.core.mbean.config;

/**
    Represents a manageable J2EE servlet. Servlet will expose some manageable
    attributes defined by its descriptor and they will be made available.
    For every Servlet that is loaded by a running Server Instance, there will
    be a correponding MBean registered in the MBeanServer. Note that the life
    cycle of Servlet is not manageable by this interface, because it is
    not exposed by its descriptor.
    <p>
    The Servlet can be running as a part of deployed application or an
    independently deployed module. In case the servlet is loaded from an
    independently deployed module, the application name (in the ObjectName)
    is treated as default application name or null.
    <p>
    Object Name of this MBean is:
        ias:type=J2EEServlet, AppName=<appName>, ModuleName=<modName>,
        ServletName=<servletName>
*/
public class ManagedJ2EEServlet extends AdminBase
{
    /** Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object.  */
    protected Object getImplementingMBean() {
        return ( this );
    }
}
