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

package com.sun.enterprise.config.serverbeans.validation.tests;

import java.util.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.validation.StringManagerHelper;
import com.sun.enterprise.config.serverbeans.validation.Result;

import com.sun.enterprise.admin.util.QuotedStringTokenizer;

public class JvmOptionsTest {

    private static LocalStringManagerImpl smh = 
        StringManagerHelper.getLocalStringsManager();

    private static final JvmOptionsTest instance = new JvmOptionsTest();

    private JvmOptionsTest() {
    }

    public static void validateJvmOptions(String[] jvmOptions, Result result) {
        try {
            instance.checkForNullOptions(jvmOptions);
            Set optionsSet = instance.tokenizeJvmOptions(jvmOptions);
            instance.checkBeginWithDash(optionsSet);
            instance.checkQuotes(optionsSet);
        }
        catch (Exception e) {
            result.failed(e.getMessage());
        }
    }

    private void checkForNullOptions(String[] options) 
        throws InvalidJvmOptionsException
    {
        if (null == options) {
            throw new InvalidJvmOptionsException(getNullJvmOptionMsg());
        }
        for (int i = 0; i < options.length; i++) {
            if (null == options[i]) {
                throw new InvalidJvmOptionsException(getNullJvmOptionMsg());
            }
        }
    }

    private void checkBeginWithDash(Set options) 
        throws InvalidJvmOptionsException
    {
        List invalidOptions = new ArrayList();
        Iterator it = options.iterator();
        while (it.hasNext()) {
            String option = it.next().toString();
            if (!option.startsWith("-") &&
                !(option.startsWith("\"-") && option.endsWith("\""))  ) 
            {
                invalidOptions.add(option);
            }
        }
        if (invalidOptions.size() > 0) {
            throw new InvalidJvmOptionsException(
                getInvalidJvmOptionMsg(invalidOptions.toString()));
        }
    }

    private void checkQuotes(Set options) throws InvalidJvmOptionsException
    {
        List invalidOptions = new ArrayList();
        final Iterator it = options.iterator();
        while (it.hasNext()) {
            String option = it.next().toString();
            if (!checkQuotes(option)) {
                invalidOptions.add(option);
            }
        }
        if (invalidOptions.size() > 0) {
            throw new InvalidJvmOptionsException(
                getIncorrectQuotesMsg(invalidOptions.toString()));
        }
    }

    private boolean checkQuotes(String option)
    {
        int length      = option.length();
        int numQuotes   = 0;
        int index       = 0;

        while (index < length && 
                (index = option.indexOf('\"', index)) != -1)
        {
            numQuotes++;
            index++;
        }
        return ((numQuotes % 2) == 0);
    }

    private Set tokenizeJvmOptions(String[] options) {
        final Set optionsSet = new LinkedHashSet();
        final String DELIM = " \t";
        for (int i = 0; i < options.length; i++) {
            QuotedStringTokenizer strTok = new QuotedStringTokenizer(
                                                options[i], DELIM);
            while (strTok.hasMoreTokens()) {
                optionsSet.add(strTok.nextToken());
            }
        }
        return Collections.unmodifiableSet(optionsSet);
    }

    private String getNullJvmOptionMsg() {
        return smh.getLocalString(getClass().getName() + ".nullJvmOption", 
            "Null Jvm option", new Object[0]);
    }

    private String getInvalidJvmOptionMsg(String invalidOptions) {
        return smh.getLocalString(getClass().getName() + ".invalidJvmOption", 
            "{0} - Invalid Jvm option. Option must begin with -", 
            new Object[] {invalidOptions});
    }

    private String getIncorrectQuotesMsg(String invalidOptions) {
        return smh.getLocalString(getClass().getName() + ".incorrectQuotesInJvmOption", 
            "{0} - Invalid Jvm option. Check quotes", 
            new Object[] {invalidOptions});
    }

    private static final class InvalidJvmOptionsException extends Exception {
        InvalidJvmOptionsException(String msg) {
            super(msg);
        }
    }
}

