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
 * BaseBean.java
 */

package com.sun.jbi.jsf.framework.common;

import com.sun.data.provider.TableDataProvider;
import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.resources.Messages;
import com.sun.jbi.jsf.framework.services.ServiceManager;
import com.sun.jbi.jsf.framework.services.ServiceManagerFactory;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 *
 * @author Sun Microsytems
 */
public class BaseBean implements Serializable {

    transient private Logger logger = Logger.getLogger(BaseBean.class.getName());

    /** data provider */
    protected TableDataProvider provider = null;                  
    /** component name e.g bpel-service-engine */
    protected String componentName;
    /** component type  e.g. SE */
    protected String componentType;
    /** component container type (SE/BC/_s_) where  _s_ refers to self  */
    protected String cType;
    /** component container name */
    protected String cName;
    /** parent name */
    protected String pName;
    /** target name */
    protected String tName;
    /** service unit name */
    protected String suName;
    /** service manager */
    protected ServiceManager serviceManager;
    

    /** Creates a new instance of BaseBean */
    public BaseBean() {
    }

    protected void getRequestParameters() {
        HttpServletRequest request = getRequest();
        componentName = (String)getParameter(request,GenericConstants.COMPONENT_NAME);
        componentType= (String)getParameter(request,GenericConstants.COMPONENT_TYPE);
        cType = (String)getParameter(request,GenericConstants.COMPONENT_CTYPE);
        cName = (String)getParameter(request,GenericConstants.COMPONENT_CNAME);
        pName = (String)getParameter(request,GenericConstants.COMPONENT_PNAME);
        tName = (String)getParameter(request,GenericConstants.COMPONENT_TNAME);
        suName = (String)getParameter(GenericConstants.COMPONENT_SUNAME);
        // validate parameters
        validateRequestParameters();
    }
    

    protected void validateRequestParameters() {
        if ( cType==null ) {
            cType = GenericConstants.HASH_SEPARATOR;        // self
        }
        if ( cName==null ) {
            cName = GenericConstants.HASH_SEPARATOR;        // self
        }
    }

    

    protected Object getParameter(HttpServletRequest request,String paramName) {
    	Object paramValue = request.getParameter(paramName);
    	if ( paramValue==null ) {
            paramValue = request.getSession().getAttribute(paramName);
    	}
    	return paramValue;
    }

    
    /**
     * getParameter
     * @param paramName     name of parameter
     */

    protected Object getParameter(String paramName) {
        HttpServletRequest request = getRequest();
        Object paramValue = getParameter(request,paramName);
        return paramValue;
    }


    /**
     * setParameter - store parameter in session
     * @param   key
     * @param   value
     */
    protected void  setParameter(String key, Object value) {
        HttpServletRequest request = getRequest();
        request.getSession().setAttribute(key,value);
    }

    protected HttpServletRequest getRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext ex = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ex.getRequest();       
        return request;
    }

    protected void getServiceManager() {
        serviceManager = ServiceManagerFactory.getServiceManager(componentName,componentType);
    }

    protected  void setup() {
        getRequestParameters();
        getServiceManager();
    }
    

    public String getName() {
        String name = componentName;
        if ( GenericConstants.SU_TYPE.equals(componentType) ) {
            if ( pName!=null ) {
                name = pName+"-"+componentName;
            }
        }
        return name;
    }


    public String getTitle(String msgid) {
        return getName()+" - " + Messages.getString(msgid);
    }    


    public String getTableTitle(String bcTableMsgId, String seTableMsgId, String suTableMsgId) {
        String label = "";
        if ( GenericConstants.BC_TYPE.equals(componentType) ) {
            label = Messages.getString(bcTableMsgId);
        } else if ( GenericConstants.SE_TYPE.equals(componentType) ) {
            label = Messages.getString(seTableMsgId);
        } else if ( GenericConstants.SU_TYPE.equals(componentType) ) {
            label = Messages.getString(suTableMsgId);
        } else if ( GenericConstants.SA_TYPE.equals(componentType) ) {
            label = Messages.getString(suTableMsgId);
        }
        return label;
    }    
    

}

