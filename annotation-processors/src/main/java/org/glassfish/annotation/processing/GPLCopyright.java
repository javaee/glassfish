/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.annotation.processing;

import java.util.Calendar;

/*
 * Generates a String representation of the GPL copyright using the 
 * current year.
 */

public class GPLCopyright {

    private static final String YEAR_PATTERN = "XX_YEAR_XX";

    private static final String GPLCOPYRIGHT_UNDATED =
 "#\n" +
 "# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n" +
 "#\n" +
 "# Copyright (c) XX_YEAR_XX Oracle and/or its affiliates. All rights reserved.\n" +
 "#\n" +
 "# The contents of this file are subject to the terms of either the GNU\n" +
 "# General Public License Version 2 only (\"GPL\") or the Common Development\n" +
 "# and Distribution License(\"CDDL\") (collectively, the \"License\").  You\n" +
 "# may not use this file except in compliance with the License.  You can\n" +
 "# obtain a copy of the License at\n" +
 "# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html\n" +
 "# or packager/legal/LICENSE.txt.  See the License for the specific\n" +
 "# language governing permissions and limitations under the License.\n" +
 "#\n" +
 "# When distributing the software, include this License Header Notice in each\n" +
 "# file and include the License file at packager/legal/LICENSE.txt.\n" +
 "#\n" +
 "# GPL Classpath Exception:\n" +
 "# Oracle designates this particular file as subject to the \"Classpath\"\n" +
 "# exception as provided by Oracle in the GPL Version 2 section of the License\n" +
 "# file that accompanied this code.\n" +
 "#\n" +
 "# Modifications:\n" +
 "# If applicable, add the following below the License Header, with the fields\n" +
 "# enclosed by brackets [] replaced by your own identifying information:\n" +
 "# \"Portions Copyright [year] [name of copyright owner]\"\n" +
 "#\n" +
 "# Contributor(s):\n" +
 "# If you wish your version of this file to be governed by only the CDDL or\n" +
 "# only the GPL Version 2, indicate your decision by adding \"[Contributor]\n" +
 "# elects to include this software in this distribution under the [CDDL or GPL\n" +
 "# Version 2] license.\"  If you don't indicate a single choice of license, a\n" +
 "# recipient has the option to distribute your version of this file under\n" +
 "# either the CDDL, the GPL Version 2 or to extend the choice of license to\n" +
 "# its licensees as provided above.  However, if you add GPL Version 2 code\n" +
 "# and therefore, elected the GPL Version 2 license, then the option applies\n" +
 "# only if the new code is made subject to such option by the copyright\n" +
 "# holder.\n" +
 "#\n\n";

    public GPLCopyright() { }

    public static String getCopyright() {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        return GPLCOPYRIGHT_UNDATED.replaceFirst(
                YEAR_PATTERN, String.valueOf(year));
    }
}
