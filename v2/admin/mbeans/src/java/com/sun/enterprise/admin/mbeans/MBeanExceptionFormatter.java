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

/*
 * $Id: MBeanExceptionFormatter.java,v 1.3 2005/12/25 03:42:23 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

import javax.management.MBeanException;

/**
 * Helper class that formats the chained exception messages. 
 * The format of the returned message will be cause1(cause2(...)).
 * It also initializes the cause of the MBeanException which otherwise
 * is not initialized during MBeanException creation.
 */
public final class MBeanExceptionFormatter
{
    /**
     * This method returns an MBeanException instance with the message formatted
     * as msg(msg1(msg2...)). It also inits the cause of the MBeanException.
     * @param e The cause.
     * @param msg
     * @return A new instance of MBeanException.
     */
    public static MBeanException toMBeanException(  final Exception e, 
                                                    final String    msg )
    {
        final String s = getMessage(e, msg);
        final Exception cause = (e != null) ? e : new Exception(s);
        MBeanException mbe = new MBeanException(cause, s);
        mbe.initCause(cause);
        return mbe;
    }

    /**
     * @param t
     * @param cause1
     * @return Returns the formatted message cause1(cause2(...))
     */
    public static String getMessage(final Throwable t, final String cause1)
    {
        if (null == t) { return cause1; }
        final String cause = t.getMessage();
        final Throwable x = (t instanceof MBeanException) ? 
            ((MBeanException)t).getTargetException() : t.getCause();
        final String s = getMessage(x, cause);
        return formatMessage(cause1, s);
    }

    private static String formatMessage(String a, String b)
    {
        final StringBuffer sb = new StringBuffer("");
        boolean isValidA = (null != a) && (a.length() > 0);
        boolean isValidB = (null != b) && (b.length() > 0);
        if (isValidA) { sb.append(a); }
        if (isValidA && isValidB) { sb.append('('); }
        if (isValidB) { sb.append(b); }
        if (isValidA && isValidB) { sb.append(')'); }
        return sb.toString();
    }
}
