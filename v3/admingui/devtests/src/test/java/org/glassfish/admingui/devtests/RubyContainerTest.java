/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 11, 2010
 * Time: 2:46:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RubyContainerTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_RUBY_CONTAINER = "Container to host Ruby web applications such as Ruby on Rails, Merb, Sinatra or any Rack based Ruby application.";

    @Test
    public void testRubyContainer() {
        final String initialPoolSize = Integer.toString(generateRandomNumber(10));
        final String minPoolSize = Integer.toString(generateRandomNumber(10));
        final String maxPoolSize = Integer.toString(generateRandomNumber(10));

        clickAndWait("treeForm:tree:configuration:jruby:jruby_link", TRIGGER_RUBY_CONTAINER);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime:jruntime", initialPoolSize);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime-mim:jruntime-mim", minPoolSize);
        selenium.type("form1:propertySheet:propertySectionTextField:jruntime-max:jruntime-max", maxPoolSize);
        String button = "form1:propertyContentPage:topButtons:newButton";
        if (!selenium.isElementPresent(button)) {
            button = "form1:propertyContentPage:topButtons:saveButton";
        }
        clickAndWait(button, MSG_NEW_VALUES_SAVED);
        clickAndWait("treeForm:tree:ct", "Please Register");

        clickAndWait("treeForm:tree:configuration:jruby:jruby_link", TRIGGER_RUBY_CONTAINER);
        assertEquals(initialPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime:jruntime"));
        assertEquals(minPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime-mim:jruntime-mim"));
        assertEquals(maxPoolSize, selenium.getValue("form1:propertySheet:propertySectionTextField:jruntime-max:jruntime-max"));
    }
}
