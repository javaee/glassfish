

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


package org.apache.catalina.ant;


import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant task that implements the JMX Set command (<code>/jmxproxy/?set</code>)
 * supported by the Tomcat manager application.
 *
 * @author Vivek Chopra
 * @version $Revision: 1.2 $
 */
public class JMXSetTask extends AbstractCatalinaTask {

    // Properties

    /**
     * The full bean name
     */
    protected String bean      = null;

    /**
     * The attribute you wish to alter
     */
    protected String attribute = null;

    /**
     * The new value for the attribute
     */
    protected String value     = null;

    // Public Methods
    
    /**
     * Get method for the bean name
     * @return Bean name
     */
    public String getBean () {
        return this.bean;
    }

    /**
     * Set method for the bean name
     * @param bean Bean name
     */
    public void setBean (String bean) {
        this.bean = bean;
    }

    /**
     * Get method for the attribute name
     * @return Attribute name
     */
    public String getAttribute () {
        return this.attribute;
    }

    /**
     * Set method for the attribute name
     * @param attribute Attribute name
     */
    public void setAttribute (String attribute) {
        this.attribute = attribute;
    }

    /**
     * Get method for the attribute value
     * @return Attribute value
     */
    public String getValue () {
        return this.value;
    }

    /**
     * Set method for the attribute value
     * @param attribute Attribute value
     */
    public void setValue (String value) {
        this.value = value;
    }

    /**
     * Execute the requested operation.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {
        super.execute();
        if (bean == null || attribute == null || value == null) {
            throw new BuildException
                ("Must specify 'bean', 'attribute' and 'value' attributes");
        }
        log("Setting attribute " + attribute +
                            " in bean " + bean +
                            " to " + value); 
        execute("/jmxproxy/?set=" + bean 
                + "&att=" + attribute 
                + "&val=" + value);
    }
}
