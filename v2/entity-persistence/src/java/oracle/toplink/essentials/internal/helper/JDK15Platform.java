/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.helper;
import java.util.regex.Pattern;
import java.util.Hashtable;


/**
 *  INTERNAL:
 *  Implements operations specific to JDK 1.5
 */
public class JDK15Platform implements JDKPlatform {

    /**
     * INTERNAL
     * Get the Map to store the query cache in
     */
    public java.util.Map getQueryCacheMap() {
        return new java.util.concurrent.ConcurrentHashMap();
    }
    
    /**
     * PERF: The regular expression compiled Pattern objects are cached
     * to avoid recompilation on every usage.
     */
    protected static Hashtable patternCache = new Hashtable();

    /**
     * INTERNAL:
     * An implementation of in memory queries with Like which uses the JDK 1.4
     * regular expression framework.
     */
    public int conformLike(Object left, Object right) {
        if ((left == null) && (right == null)) {
            return JavaPlatform.TRUE;
        } else if ((left == null) || (right == null)) {
            return JavaPlatform.FALSE;
        }
        if (left instanceof String && right instanceof String) {
            // PERF: First check the pattern cache for the pattern.
            // Note that the original string is the key, to avoid having to translate it first.
            Pattern pattern = (Pattern)patternCache.get(right);
            if (pattern == null) {
                // Bug 3936427 - Replace regular expression reserved characters with escaped version of those characters
                // For instance replace ? with \?
                String convertedRight = ((String)right).replaceAll("\\?", "\\\\?");
                convertedRight = convertedRight.replaceAll("\\*", "\\\\*");
                convertedRight = convertedRight.replaceAll("\\.", "\\\\.");
                convertedRight = convertedRight.replaceAll("\\[", "\\\\[");
                convertedRight = convertedRight.replaceAll("\\)", "\\\\)");
                convertedRight = convertedRight.replaceAll("\\(", "\\\\(");
                convertedRight = convertedRight.replaceAll("\\{", "\\\\{");
                convertedRight = convertedRight.replaceAll("\\+", "\\\\+");
                convertedRight = convertedRight.replaceAll("\\^", "\\\\^");
                convertedRight = convertedRight.replaceAll("\\|", "\\\\|");

                // regular expressions to substitute SQL wildcards with regex wildcards
                // replace "%" which is not preceded by "\" with ".*"
                convertedRight = convertedRight.replaceAll("([^\\\\])%", "$1.*");

                // replace "_" which is not preceded by "\" with ".
                convertedRight = convertedRight.replaceAll("([^\\\\])_", "$1.");

                // deal with wildcards at the beginning of a line.
                convertedRight = convertedRight.replaceAll("^%", ".*");
                convertedRight = convertedRight.replaceAll("^_", ".");

                // replace "\%" with "%"
                convertedRight = convertedRight.replaceAll("\\\\%", "%");

                // replace "\_" with "_"
                convertedRight = convertedRight.replaceAll("\\\\_", "_");

                pattern = Pattern.compile(convertedRight);
                // Ensure cache does not grow beyond 100.
                if (patternCache.size() > 100) {
                    patternCache.remove(patternCache.keySet().iterator().next());
                }
                patternCache.put(right, pattern);
            }
            boolean match = pattern.matcher((String)left).matches();
            if (match) {
                return JavaPlatform.TRUE;
            } else {
                return JavaPlatform.FALSE;
            }
        }
        return JavaPlatform.UNDEFINED;
    }

    /**
     * INTERNAL:
     * Get the milliseconds from a Calendar.  In JDK 1.4, this can be accessed directly from the calendar.
     */
    public long getTimeInMillis(java.util.Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    /**
     * INTERNAL:
     * Set the milliseconds for a Calendar.  In JDK 1.4, this can be set directly in the calendar.
     */
    public void setTimeInMillis(java.util.Calendar calendar, long millis) {
        calendar.setTimeInMillis(millis);
    }

    /**
     * INTERNAL:
     * Use API first available in JDK 1.4 to set the cause of an exception.
     */
    public void setExceptionCause(Throwable exception, Throwable cause) {
        if (exception.getCause() == null) {
            exception.initCause(cause);
        }
    }

    /**
     * INTERNAL
     * return a boolean which determines where TopLink should include the TopLink-stored
     * Internal exception in it's stack trace.  For JDK 1.4 VMs with exception chaining
     * the Internal exception can be redundant and confusing.
     * @return boolean will return false since JDK 1.4 does supports exception chaining
     */
    public boolean shouldPrintInternalException() {
        return false;
    }
}
