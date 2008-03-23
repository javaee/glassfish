

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


import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;


/**
 * Representation of a filter definition for a web application, as represented
 * in a <code>&lt;filter&gt;</code> element in the deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:40 $
 */

public class FilterDef implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The description of this filter.
     */
    private String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * The display name of this filter.
     */
    private String displayName = null;

    public String getDisplayName() {
        return (this.displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * The fully qualified name of the Java class that implements this filter.
     */
    private String filterClass = null;

    public String getFilterClass() {
        return (this.filterClass);
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }


    /**
     * The name of this filter, which must be unique among the filters
     * defined for a particular web application.
     */
    private String filterName = null;

    public String getFilterName() {
        return (this.filterName);
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }


    /**
     * The large icon associated with this filter.
     */
    private String largeIcon = null;

    public String getLargeIcon() {
        return (this.largeIcon);
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }


    /**
     * The set of initialization parameters for this filter, keyed by
     * parameter name.
     */
    private Map parameters = new HashMap();

    public Map getParameterMap() {

        return (this.parameters);

    }


    /**
     * The small icon associated with this filter.
     */
    private String smallIcon = null;

    public String getSmallIcon() {
        return (this.smallIcon);
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add an initialization parameter to the set of parameters associated
     * with this filter.
     *
     * @param name The initialization parameter name
     * @param value The initialization parameter value
     */
    public void addInitParameter(String name, String value) {

        parameters.put(name, value);

    }


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("FilterDef[");
        sb.append("filterName=");
        sb.append(this.filterName);
        sb.append(", filterClass=");
        sb.append(this.filterClass);
        sb.append("]");
        return (sb.toString());

    }


}
