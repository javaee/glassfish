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
 * $Id: RepositoryNameValidatorTest.java,v 1.3 2005/12/25 03:44:11 tcfujii Exp $
 */

package com.sun.enterprise.admin.servermgmt;

import java.util.Random;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

public class RepositoryNameValidatorTest extends TestCase
{
    static final char[] INVALID_CHARS = 
        {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',', '=', 'î', ' ', '&', ';', '[', ']', '{', '}', '(', ')', '%', '$', '^', '!'};

    static final char[] VALID_CHARS = {'a', '0', '-', '_', '.'};

    static final int ITERATIONS = 100;

    Validator   validator;
    Random      random;

    public void testNull()
    {
        testInvalid(null);
    }

    public void testZeroLength()
    {
        testInvalid("");
    }

    public void testInvalidChar()
    {
        for (int i = 0; i < INVALID_CHARS.length; i++)
        {
            testInvalid("" + INVALID_CHARS[i]);
        }
    }

    public void testInvalidStr()
    {
        for (int i = 0; i < ITERATIONS; i++)
        {
            testInvalid("" + 
                INVALID_CHARS[random.nextInt(INVALID_CHARS.length)] + 
                INVALID_CHARS[random.nextInt(INVALID_CHARS.length)]);
        }
    }

    public void testValidChar()
    {
        for (int i = 0; i < VALID_CHARS.length; i++)
        {
            testValid("" + VALID_CHARS[i]);
        }
    }

    public void testValidStr()
    {
        for (int i = 0; i < ITERATIONS; i++)
        {
            testValid("" + 
                VALID_CHARS[random.nextInt(VALID_CHARS.length)] + 
                VALID_CHARS[random.nextInt(VALID_CHARS.length)]);
        }
    }

    public void testCombination()
    {
        for (int i = 0; i < ITERATIONS; i++)
        {
            testInvalid("" + 
                VALID_CHARS[random.nextInt(VALID_CHARS.length)] + 
                INVALID_CHARS[random.nextInt(INVALID_CHARS.length)]);
        }
    }

    public RepositoryNameValidatorTest(String name) throws Exception
    {
        super(name);
    }

    protected void setUp()
    {
        validator   = new RepositoryNameValidator("repository name");
        random      = new Random();
    }

    protected void tearDown()
    {
        validator   = null;
        random      = null;
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(RepositoryNameValidatorTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(
            RepositoryNameValidatorTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    void testInvalid(String invalid)
    {
        try
        {
            validator.validate(invalid);
            System.out.println(invalid);
            Assert.assertTrue(false);
        }
        catch (Exception e)
        {
            //ok
        }
    }

    void testValid(String valid)
    {
        try
        {
            validator.validate(valid);
        }
        catch (Exception e)
        {
            Assert.assertTrue(false);
        }
    }
}
