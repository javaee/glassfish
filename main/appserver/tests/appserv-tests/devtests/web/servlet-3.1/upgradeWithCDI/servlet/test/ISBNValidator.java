/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.enterprise.context.ApplicationScoped;

/**
 * Validate the ISBN number as described in
 * http://en.wikipedia.org/wiki/International_Standard_Book_Number
 */
@ApplicationScoped
public class ISBNValidator {
    /**
     * For convenience, we will ignore '-', ' '.
     */
    public boolean isValid(String isbnStr) {
        char[] isbnChars = isbnStr.toCharArray();
        if (isbnChars.length < 10) {
            return false;
        }

        boolean valid = true;
        boolean hasX = false;
        int[] is = new int[13];
        int len = 0;
        // read one more if there is
        for (int i = 0; i < isbnChars.length && len < 14; i++) {
            char c = isbnChars[i];
            if ((c >= '0' && c <= '9')) {
                if (len < 13) {
                    is[len] = c - '0';
                }
                len++;
            } else if (c == '-' || c == ' ') {
                //skip
            } else if ((c == 'X' || c == 'x') && len == 9) { // for isbn 10
                is[len++] = 10;
                hasX = true;
            } else {
                valid = false;
                break;
            }
        }

        if (!valid || (len != 10 && len != 13) || (len != 10 && hasX)) {
            return false;
        }

        if (len == 10) {
            return isISBN10(is);
        } else { // len == 13
            return isISBN13(is);
        }
    }

    // only look at the first 10 elements and 'X' has changed to 10
    // 10x_1 + 9x_2 + 8x_3 + ... + 2x_9 + x_10 = 0 (mod 11)
    private boolean isISBN10(int[] is) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (10 - i) * is[i];
        }   
        return (sum % 11 == 0);
    }

    // x_13 = (10 - (x_1 + 3x_2 + x_3 + 3x_4 + ... + x_11 + 3x_12)) mod 10
    private boolean isISBN13(int[] is) {
        int sum = 0;
        for (int i = 0; i < 12; i += 2) {
            sum += is[i] + 3 * is[i + 1];
        }
        sum += is[12];
        return (sum % 10 == 0);
    }
}
