

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.deploy;


import org.apache.catalina.util.RequestUtil;
import java.io.Serializable;


/**
 * Representation of a filter mapping for a web application, as represented
 * in a <code>&lt;filter-mapping&gt;</code> element in the deployment
 * descriptor.  Each filter mapping must contain a filter name plus either
 * a URL pattern or a servlet name.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2007/01/23 00:06:56 $
 */

public class FilterMap implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The name of this filter to be executed when this mapping matches
     * a particular request.
     */
    
    public static final int ERROR = 1;
    public static final int FORWARD = 2;
    public static final int FORWARD_ERROR =3;  
    public static final int INCLUDE = 4;
    public static final int INCLUDE_ERROR  = 5;
    public static final int INCLUDE_ERROR_FORWARD  =6;
    public static final int INCLUDE_FORWARD  = 7;
    public static final int REQUEST = 8;
    public static final int REQUEST_ERROR = 9;
    public static final int REQUEST_ERROR_FORWARD = 10;
    public static final int REQUEST_ERROR_FORWARD_INCLUDE = 11;
    public static final int REQUEST_ERROR_INCLUDE = 12;
    public static final int REQUEST_FORWARD = 13;
    public static final int REQUEST_INCLUDE = 14;
    public static final int REQUEST_FORWARD_INCLUDE= 15;
    
    // represents nothing having been set. This will be seen 
    // as equal to a REQUEST
    private static final int NOT_SET = -1;
    
    private int dispatcherMapping=NOT_SET;
    
    private String filterName = null;    

    public String getFilterName() {
        return (this.filterName);
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }


    /**
     * The servlet name this mapping matches.
     */
    private String servletName = null;

    public String getServletName() {
        return (this.servletName);
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }


    /**
     * The URL pattern this mapping matches.
     */
    private String urlPattern = null;

    public String getURLPattern() {
        return (this.urlPattern);
    }

    public void setURLPattern(String urlPattern) {
        this.urlPattern = RequestUtil.URLDecode(urlPattern);
    }
    
    /**
     *
     * This method will be used to set the current state of the FilterMap
     * representing the state of when filters should be applied:
     *
     *        ERROR
     *        FORWARD
     *        FORWARD_ERROR
     *        INCLUDE
     *        INCLUDE_ERROR
     *        INCLUDE_ERROR_FORWARD
     *        REQUEST
     *        REQUEST_ERROR
     *        REQUEST_ERROR_INCLUDE
     *        REQUEST_ERROR_FORWARD_INCLUDE
     *        REQUEST_INCLUDE
     *        REQUEST_FORWARD,
     *        REQUEST_FORWARD_INCLUDE
     *
     */
    public void setDispatcher(String dispatcherString) {
        String dispatcher = dispatcherString.toUpperCase();
        
        if (dispatcher.equals("FORWARD")) {

            // apply FORWARD to the global dispatcherMapping.
            switch (dispatcherMapping) {
                case NOT_SET  :  dispatcherMapping = FORWARD; break;
                case ERROR : dispatcherMapping = FORWARD_ERROR; break;
                case INCLUDE  :  dispatcherMapping = INCLUDE_FORWARD; break;
                case INCLUDE_ERROR  :  dispatcherMapping = INCLUDE_ERROR_FORWARD; break;
                case REQUEST : dispatcherMapping = REQUEST_FORWARD; break;
                case REQUEST_ERROR : dispatcherMapping = REQUEST_ERROR_FORWARD; break;
                case REQUEST_ERROR_INCLUDE : dispatcherMapping = REQUEST_ERROR_FORWARD_INCLUDE; break;
                case REQUEST_INCLUDE : dispatcherMapping = REQUEST_FORWARD_INCLUDE; break;
            }
        } else if (dispatcher.equals("INCLUDE")) {
            // apply INCLUDE to the global dispatcherMapping.
            switch (dispatcherMapping) {
                case NOT_SET  :  dispatcherMapping = INCLUDE; break;
                case ERROR : dispatcherMapping = INCLUDE_ERROR; break;
                case FORWARD  :  dispatcherMapping = INCLUDE_FORWARD; break;
                case FORWARD_ERROR  :  dispatcherMapping = INCLUDE_ERROR_FORWARD; break;
                case REQUEST : dispatcherMapping = REQUEST_INCLUDE; break;
                case REQUEST_ERROR : dispatcherMapping = REQUEST_ERROR_INCLUDE; break;
                case REQUEST_ERROR_FORWARD : dispatcherMapping = REQUEST_ERROR_FORWARD_INCLUDE; break;
                case REQUEST_FORWARD : dispatcherMapping = REQUEST_FORWARD_INCLUDE; break;
            }
        } else if (dispatcher.equals("REQUEST")) {
            // apply REQUEST to the global dispatcherMapping.
            switch (dispatcherMapping) {
                case NOT_SET  :  dispatcherMapping = REQUEST; break;
                case ERROR : dispatcherMapping = REQUEST_ERROR; break;
                case FORWARD  :  dispatcherMapping = REQUEST_FORWARD; break;
                case FORWARD_ERROR  :  dispatcherMapping = REQUEST_ERROR_FORWARD; break;
                case INCLUDE  :  dispatcherMapping = REQUEST_INCLUDE; break;
                case INCLUDE_ERROR  :  dispatcherMapping = REQUEST_ERROR_INCLUDE; break;
                case INCLUDE_FORWARD : dispatcherMapping = REQUEST_FORWARD_INCLUDE; break;
                case INCLUDE_ERROR_FORWARD : dispatcherMapping = REQUEST_ERROR_FORWARD_INCLUDE; break;
            }
        }  else if (dispatcher.equals("ERROR")) {
            // apply ERROR to the global dispatcherMapping.
            switch (dispatcherMapping) {
                case NOT_SET  :  dispatcherMapping = ERROR; break;
                case FORWARD  :  dispatcherMapping = FORWARD_ERROR; break;
                case INCLUDE  :  dispatcherMapping = INCLUDE_ERROR; break;
                case INCLUDE_FORWARD : dispatcherMapping = INCLUDE_ERROR_FORWARD; break;
                case REQUEST : dispatcherMapping = REQUEST_ERROR; break;
                case REQUEST_INCLUDE : dispatcherMapping = REQUEST_ERROR_INCLUDE; break;
                case REQUEST_FORWARD : dispatcherMapping = REQUEST_ERROR_FORWARD; break;
                case REQUEST_FORWARD_INCLUDE : dispatcherMapping = REQUEST_ERROR_FORWARD_INCLUDE; break;
            }
        }
    }
    
    public int getDispatcherMapping() {
        // per the SRV.6.2.5 absence of any dispatcher elements is
        // equivelant to a REQUEST value
        if (dispatcherMapping == NOT_SET) return REQUEST;
        else return dispatcherMapping; 
    }


    public void setDispatcherMapping(int mapping) {
        dispatcherMapping = mapping;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("FilterMap[");
        sb.append("filterName=");
        sb.append(this.filterName);
        if (servletName != null) {
            sb.append(", servletName=");
            sb.append(servletName);
        }
        if (urlPattern != null) {
            sb.append(", urlPattern=");
            sb.append(urlPattern);
        }
        sb.append("]");
        return (sb.toString());

    }


}
