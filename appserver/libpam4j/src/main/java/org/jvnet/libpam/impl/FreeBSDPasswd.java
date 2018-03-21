/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.libpam.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jvnet.libpam.impl.CLibrary.Passwd;

/**
 * FreeeBSD
 *
 * struct passwd {
 * 	char	*pw_name;
 * 	char	*pw_passwd;
 * 	uid_t	pw_uid;
 * 	gid_t	pw_gid;
 * 	time_t	pw_change;
 * 	char	*pw_class;
 * 	char	*pw_gecos;
 * 	char	*pw_dir;
 * 	char	*pw_shell;
 * 	time_t	pw_expire;
 * 	int	pw_fields;
 * };
 *
 * @author R. Tyler Croy
 */

public class FreeBSDPasswd extends Passwd {
    /* password change time */
    public long pw_change;

    /* user access class */
    public String pw_class;

    /* Honeywell login info */
    public String pw_gecos;

    /* home directory */
    public String pw_dir;

    /* default shell */
    public String pw_shell;

    /* account expiration */
    public long pw_expire;

    /* internal on FreeBSD? */
    public int pw_fields;

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
        fieldOrder.addAll(Arrays.asList("pw_change", "pw_class", "pw_gecos",
                "pw_dir", "pw_shell", "pw_expire", "pw_fields"));
        return fieldOrder;
    }

}
