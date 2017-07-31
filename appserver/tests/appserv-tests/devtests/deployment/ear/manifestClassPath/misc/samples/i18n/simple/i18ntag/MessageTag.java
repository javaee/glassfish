/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * MessageTag.java
 *
 * Created on May 21, 2002, 5:17 PM
 */

package samples.i18n.simple.i18ntag;

import java.io.*;
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.servlet.jsp.tagext.*;

/**
 * A simple message taghandler to display localized message from a resource bundle based on jsp request
 * @author  Chand Basha
 * @version	1.0
 */

public class MessageTag extends TagSupport {

    private String key			= null;
	private String bundleName	= null;
	private String language		= null;
    private String country		= null;
    private String variant		= null;

	/**
	 * Set the user preferred language
	 */
	public void setLanguage(String lang) {
        this.language = lang;
	}

    /**
	 * Get the user preferred language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Set the user preferred country
	 */
	public void setCountry(String country) {
		this.country = country;
	}

    /**
	 * Get the user preferred country
	 */
	public String getCountry() {
		return country;
    }

    /**
	 * Set the user preferred variant
	 */
    public void setVariant(String variant) {
	    this.variant = variant;
	}

    /**
	 * Get the user preferred variant
	 */
	public String getVariant() {
	    return variant;
    }

    /**
	 * Set the user preferred resource bundle name
	 */
	public void setName (String name) {
		this.bundleName = name;
	}

    /**
	 * Set the message key required to retrieve message from the resource bundle
	 */
    public void setKey(String key) {
        this.key = key;
    }

    /**
	 * Get the message key required to retrieve message from the resource bundle
	 */
    public String getKey() {
        return key;
    }

    /**
	 * Gets the user preferred resource bundle name
	 */
	public String getName() {
		return bundleName;
	}

    /**
	 * Will be called by the JSP Engine when it encounters the start of the tag
	 */
    public int doStartTag() throws JspTagException {
        return EVAL_BODY_INCLUDE;
    }

    /**
	 * Will be called by the JSP Engine when it encounters the end of the tag
	 */
    public int doEndTag() throws JspTagException {
        try {
			if(language != null) {
				if( country == null)
					country = "";
				if(variant == null)
					variant = "";
			} else language = "en";
			java.util.Locale locale = new Locale(language, country, variant);
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
			String message = bundle.getString(key);
			pageContext.getOut().write("Message from resource bundle:" + message);
		} catch(Exception e) {
            throw new JspTagException("Error: " + e);
        }
        return EVAL_PAGE;
    }
}

