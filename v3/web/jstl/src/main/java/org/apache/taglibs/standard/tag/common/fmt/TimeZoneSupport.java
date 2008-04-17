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

package org.apache.taglibs.standard.tag.common.fmt;

import java.io.IOException;
import java.util.TimeZone;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 * Support for tag handlers for &lt;timeZone&gt;, the time zone tag in
 * JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class TimeZoneSupport extends BodyTagSupport {


    //*********************************************************************
    // Protected state

    protected Object value;                      // 'value' attribute
  

    //*********************************************************************
    // Private state

    private TimeZone timeZone;


    //*********************************************************************
    // Constructor and initialization

    public TimeZoneSupport() {
	super();
	init();
    }

    private void init() {
	value = null;
    }


    //*********************************************************************
    // Collaboration with subtags

    public TimeZone getTimeZone() {
	return timeZone;
    }


    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {

	if (value == null) {
	    timeZone = TimeZone.getTimeZone("GMT");
	} else if (value instanceof String) {
	    if (((String) value).trim().equals("")) {
		timeZone = TimeZone.getTimeZone("GMT");
	    } else {
		timeZone = TimeZone.getTimeZone((String) value);
	    }
	} else {
	    timeZone = (TimeZone) value;
	}

	return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
	try {
	    pageContext.getOut().print(bodyContent.getString());
	} catch (IOException ioe) {
	    throw new JspTagException(ioe.toString(), ioe);
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }


    //*********************************************************************
    // Package-scoped utility methods

    /*
     * Determines and returns the time zone to be used by the given action.
     *
     * <p> If the given action is nested inside a &lt;timeZone&gt; action,
     * the time zone is taken from the enclosing &lt;timeZone&gt; action.
     *
     * <p> Otherwise, the time zone configuration setting
     * <tt>javax.servlet.jsp.jstl.core.Config.FMT_TIME_ZONE</tt>
     * is used.
     *
     * @param pageContext the page containing the action for which the
     * time zone needs to be determined
     * @param fromTag the action for which the time zone needs to be
     * determined
     *
     * @return the time zone, or <tt>null</tt> if the given action is not 
     * nested inside a &lt;timeZone&gt; action and no time zone configuration
     * setting exists
     */
    static TimeZone getTimeZone(PageContext pc, Tag fromTag) {
	TimeZone tz = null;

	Tag t = findAncestorWithClass(fromTag, TimeZoneSupport.class);
	if (t != null) {
	    // use time zone from parent <timeZone> tag
	    TimeZoneSupport parent = (TimeZoneSupport) t;
	    tz = parent.getTimeZone();
	} else {
	    // get time zone from configuration setting
	    Object obj = Config.find(pc, Config.FMT_TIME_ZONE);
	    if (obj != null) {
		if (obj instanceof TimeZone) {
		    tz = (TimeZone) obj;
		} else {
		    tz = TimeZone.getTimeZone((String) obj);
		}
	    }
	}

	return tz;
    }
}
