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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.util.regex.Pattern;

/**
 * Configuration change category. Configuration changes can be categorized by
 * their associated attributes. All config attributes are identified by their
 * XPath location and a regular expression matching one or more attribues XPath
 * defines a category of Configuration Changes.
 */
public class ConfigChangeCategory {

    private String configChangeCategoryName;
    private Pattern configXPathPattern;

    /**
     * Create a new ConfigChangeCategory
     */
    public ConfigChangeCategory() {
    }

    /**
     * Create a new ConfigChangeCategory using specified name and regular
     * expression pattern expressed as string.
     * @throws PatternSyntaxException if regular expression can not be compiled
     */
    public ConfigChangeCategory(String name, String regex) {
        configChangeCategoryName = name;
        configXPathPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Create a new ConfigChangeCategory using specified name and regular
     * expression pattern.
     */
    public ConfigChangeCategory(String name, Pattern regex) {
        configChangeCategoryName = name;
        configXPathPattern = regex;
    }

    /**
     * Get name of this configuration change category.
     */
    public String getName() {
        return configChangeCategoryName;
    }

    /**
     * Set name of this configuration change category to specified value.
     */
    public void setName(String name) {
        configChangeCategoryName = name;
    }

    /**
     * Get regular expression pattern on attribute XPath that defines this
     * category.
     */
    public Pattern getConfigXPathPattern() {
        return configXPathPattern;
    }

    /**
     * Set regular expression pattern over attribute XPath that defines this
     * category.
     * @throws PatternSyntaxException if regular expression can not be compiled
     */
    public void setConfigXPathPattern(String regex) {
        configXPathPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Set regular expression pattern over attribute XPath that defines this
     * category.
     */
    public void setConfigXPathPattern(Pattern regex) {
        configXPathPattern = regex;
    }

    /**
     * Two config change categories are same if they share the same regular
     * expression pattern (irrespective of the name given to them).
     */
    public boolean equals(ConfigChangeCategory other) {
        if (configXPathPattern == null) {
            if (other.configXPathPattern == null) {
                return ((configChangeCategoryName != null) ?
                        configChangeCategoryName.equals(
                            other.configChangeCategoryName) :
                        (other.configChangeCategoryName == null));
            } else {
                return false;
            }
        } else {
            return configXPathPattern.equals(other.configXPathPattern);
        }
    }
}
