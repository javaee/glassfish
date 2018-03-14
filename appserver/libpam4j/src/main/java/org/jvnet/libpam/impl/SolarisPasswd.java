/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package org.jvnet.libpam.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jvnet.libpam.impl.CLibrary.passwd;

/**
 * Solaris passwd
 *
 * struct passwd {
 *   char    *pw_name;
 *   char    *pw_passwd;
 *   uid_t   pw_uid;
 *   gid_t   pw_gid;
 *   char    *pw_age;
 *   char    *pw_comment;
 *   char    *pw_gecos;
 *   char    *pw_dir;
 *   char    *pw_shell;
 * };
 *
 * @author Sebastian Sdorra
 */
public class SolarisPasswd extends passwd {
    public String pw_age;

    public String pw_comment;

    public String pw_gecos;

    public String pw_dir;

    public String pw_shell;


    @Override
    public String getPwGecos() {
        return pw_gecos;
    }

    @Override
    public String getPwDir() {
        return pw_dir;
    }

    @Override
    public String getPwShell() {
        return pw_shell;
    }

    @Override
    protected List getFieldOrder() {
        List fieldOrder = new ArrayList(super.getFieldOrder());
        fieldOrder.addAll(Arrays.asList("pw_age", "pw_comment", "pw_gecos",
                "pw_dir", "pw_shell"));
        return fieldOrder;
    }

}
