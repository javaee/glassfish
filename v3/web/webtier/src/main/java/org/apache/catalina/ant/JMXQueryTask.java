

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
 * Ant task that implements the JMX Query command 
 * (<code>/jmxproxy/?qry</code>) supported by the Tomcat manager application.
 *
 * @author Vivek Chopra
 * @version $Revision: 1.2 $
 */
public class JMXQueryTask extends AbstractCatalinaTask {

    // Properties

    /**
     * The JMX query string 
     * @see setQuery()
     */
    protected String query      = null;

    // Public Methods
    
    /**
     * Get method for the JMX query string
     * @return Query string
     */
    public String getQuery () {
        return this.query;
    }

    /**
     * Set method for the JMX query string.
    * <P>Examples of query format:
     * <UL>
     * <LI>*:*</LI>
     * <LI>*:type=RequestProcessor,*</LI>
     * <LI>*:j2eeType=Servlet,*</LI>
     * <LI>Catalina:type=Environment,resourcetype=Global,name=simpleValue</LI>
     * </UL>
     * </P> 
     * @param query JMX Query string
     */
    public void setQuery (String query) {
        this.query = query;
    }

    /**
     * Execute the requested operation.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {
        super.execute();
        String queryString = (query == null) ? "":("?qry="+query);
        log("Query string is " + queryString); 
        execute ("/jmxproxy/" + queryString);
    }
}
