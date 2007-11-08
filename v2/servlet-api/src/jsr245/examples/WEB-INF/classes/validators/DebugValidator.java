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


package validators;


import java.io.InputStream;
import java.io.IOException;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;


/**
 * Example tag library validator that simply dumps the XML version of each
 * page to standard output (which will typically be sent to the file
 * <code>$CATALINA_HOME/logs/catalina.out</code>).  To utilize it, simply
 * include a <code>taglib</code> directive for this tag library at the top
 * of your JSP page.
 *
 * @author Craig McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:13:54 $
 */

public class DebugValidator extends TagLibraryValidator {


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    /**
     * Validate a JSP page.  This will get invoked once per directive in the
     * JSP page.  This method will return <code>null</code> if the page is
     * valid; otherwise the method should return an array of
     * <code>ValidationMessage</code> objects.  An array of length zero is
     * also interpreted as no errors.
     *
     * @param prefix The value of the prefix argument in this directive
     * @param uri The value of the URI argument in this directive
     * @param page The page data for this page
     */
    public ValidationMessage[] validate(String prefix, String uri,
                                        PageData page) {

        System.out.println("---------- Prefix=" + prefix + " URI=" + uri +
                           "----------");

        InputStream is = page.getInputStream();
        while (true) {
            try {
                int ch = is.read();
                if (ch < 0)
                    break;
                System.out.print((char) ch);
            } catch (IOException e) {
                break;
            }
        }
        System.out.println();
        System.out.println("-----------------------------------------------");
        return (null);

    }


}
