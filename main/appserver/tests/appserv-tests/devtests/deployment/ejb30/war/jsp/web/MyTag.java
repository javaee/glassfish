/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.web.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import javax.annotation.Resource;
import javax.sql.DataSource;

public class MyTag extends TagSupport {

    private @Resource(mappedName="jdbc/__default") DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;

    public int doStartTag() throws JspException {

        try {

            JspWriter jsw = pageContext.getOut();

            int loginTimeout = ds1.getLoginTimeout();
            jsw.print("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            jsw.print("ds2-login-timeout=" + loginTimeout);

            jsw.print(", Hello World");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        } 

        return SKIP_BODY;
    }
}
