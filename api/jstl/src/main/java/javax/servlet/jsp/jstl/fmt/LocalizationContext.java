/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.jsp.jstl.fmt;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class representing an I18N localization context.
 *
 * <p> An I18N localization context has two components: a resource bundle and
 * the locale that led to the resource bundle match.
 *
 * <p> The resource bundle component is used by &lt;fmt:message&gt; for mapping
 * message keys to localized messages, and the locale component is used by the
 * &lt;fmt:message&gt;, &lt;fmt:formatNumber&gt;, &lt;fmt:parseNumber&gt;, &lt;fmt:formatDate&gt;,
 * and &lt;fmt:parseDate&gt; actions as their formatting or parsing locale, respectively.
 *
 * @author Jan Luehe
 */

public class LocalizationContext {

    // the localization context's resource bundle
    final private ResourceBundle bundle;

    // the localization context's locale
    final private Locale locale;

    /**
     * Constructs an empty I18N localization context.
     */
    public LocalizationContext() {
	bundle = null;
	locale = null;
    }

    /**
     * Constructs an I18N localization context from the given resource bundle
     * and locale.
     *
     * <p> The specified locale is the application- or browser-based preferred
     * locale that led to the resource bundle match.
     *
     * @param bundle The localization context's resource bundle
     * @param locale The localization context's locale
     */
    public LocalizationContext(ResourceBundle bundle, Locale locale) {
	this.bundle = bundle;
	this.locale = locale;
    }

    /**
     * Constructs an I18N localization context from the given resource bundle.
     *
     * <p> The localization context's locale is taken from the given
     * resource bundle.
     *
     * @param bundle The resource bundle
     */
    public LocalizationContext(ResourceBundle bundle) {
	this.bundle = bundle;
	this.locale = bundle.getLocale();
    }

    /** 
     * Gets the resource bundle of this I18N localization context.
     * 
     * @return The resource bundle of this I18N localization context, or null
     * if this I18N localization context is empty
     */ 
    public ResourceBundle getResourceBundle() {
	return bundle;
    }

    /** 
     * Gets the locale of this I18N localization context.
     *
     * @return The locale of this I18N localization context, or null if this
     * I18N localization context is empty, or its resource bundle is a
     * (locale-less) root resource bundle.
     */ 
    public Locale getLocale() {
	return locale;
    }
}

