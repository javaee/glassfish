/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.web.TagDescriptor;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 * dynamic-attributes element in tag element describes whether this tag supports
 * additional attributes with dynamic names. If true, the tag-class must
 * implement the javax.servlet.jsp.tagext.DynamicAttributes interface.
 * Defaults to false.
 * @author Sudipto Ghosh
 *
 */
public class TagClassImplementsValidInterface extends WebTest implements WebCheck {
    public Result check(WebBundleDescriptor descriptor) {

        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        VerifierTestContext context = getVerifierContext();
        Result result = getInitializedResult();
        ClassLoader cl = context.getClassLoader();
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;
        }

        for (TagLibDescriptor tld : tlds) {
            if (tld.getSpecVersion().compareTo("2.0")>=0) {
                for (TagDescriptor tagdesc : tld.getTagDescriptors()) {
                    Class c = null;
                    try {
                        c = Class.forName(tagdesc.getTagClass(), false, cl);
                    } catch (ClassNotFoundException e) {
                       //do nothing
                    }
                    if (tagdesc.getDynamicAttributes().equalsIgnoreCase("true") &&
                            !javax.servlet.jsp.tagext.DynamicAttributes.class.
                            isAssignableFrom(c) ) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass()
                                .getName() +
                                ".failed",
                                "Error: tag class [ {0} ] in [ {1} ] does not " +
                                "implements interface " +
                                "javax.servlet.jsp.tagext.DynamicAttributes",
                                new Object[]{c.getName(), tld.getUri()}));

                    }
                }
            }
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass()
                                    .getName() +
                    ".passed1", "All tag-class defined in the tag lib descriptor" +
                    " files implement valid interface"));
        }
        return result;
    }
}
