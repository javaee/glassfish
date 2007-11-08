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
package oracle.toplink.essentials.internal.expressions;

import java.io.*;
import java.math.*;
import java.util.Calendar;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.helper.ConversionManager;

/**
 * <p><b>Purpose</b>: Expression Java printer.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print an expression in Java format.
 * <li> Used in project class generation.
 * </ul>
 *    @since TOPLink10.1.3
 */
public class ExpressionJavaPrinter {

    // What we write on
    protected StringWriter writer;

    // ExpressionBuilder string, e.g. expBuilder
    protected String builderString;

    // Database platform for the project
    protected DatabasePlatform platform;

    public ExpressionJavaPrinter(String builderString, StringWriter writer, DatabasePlatform platform) {
        this.builderString = builderString;
        this.writer = writer;
        this.platform = platform;
    }

    public String getBuilderString() {
        return builderString;
    }

    public DatabasePlatform getPlatform() {
        return platform;
    }

    public StringWriter getWriter() {
        return writer;
    }

    public void printString(String value) {
        getWriter().write(value);
   }

    //Used to convert a java object to java print format
    public void printJava(Object object) {
        if (object == null) {
            printString("null");
        } else if (object.getClass() == ClassConstants.STRING) {
            printString("\"");
            printString((String)object);
            printString("\"");
        } else if (object instanceof Calendar) {
            printString("new java.util.Date(");
            printString(String.valueOf(((Calendar)object).getTimeInMillis()));
            printString("L)");
        } else if (object.getClass() == ClassConstants.TIMESTAMP) {
            printString("new java.sql.Timestamp(");
            printString(String.valueOf(((java.sql.Timestamp)object).getTime()));
            printString("L)");
        } else if (object.getClass() == ClassConstants.SQLDATE) {
            printString("new java.sql.Date(");
            printString(String.valueOf(((java.sql.Date)object).getTime()));
            printString("L)");
        } else if (object.getClass() == ClassConstants.TIME) {
            printString("new java.sql.Time(");
            printString(String.valueOf(((java.sql.Time)object).getTime()));
            printString("L)");
        } else if (object.getClass() == ClassConstants.UTILDATE) {
            printString("new java.util.Date(");
            printString(String.valueOf(((java.util.Date)object).getTime()));
            printString("L)");
        } else if (object.getClass() == ClassConstants.BYTE) {
            printByte((Byte)object);
        } else if (object.getClass() == ClassConstants.APBYTE) {
            printString("new byte[] {");
            byte[] bytes = (byte[])object;
            if (bytes.length > 0) {
                printString(String.valueOf((int)bytes[0]));                
                for (int index = 1; index < bytes.length; index++) {
                    printString(",");                
                    printString(String.valueOf((int)bytes[index]));                
                }
            }
            printString("}");
        } else if (object.getClass() == ClassConstants.ABYTE) {
            printString("new Byte[] {");
            Byte[] bytes = (Byte[])object;
            if (bytes.length > 0) {
                printByte(bytes[0]);                
                for (int index = 1; index < bytes.length; index++) {
                    printString(",");                
                    printByte(bytes[index]);                
                }
            }
            printString("}");
        } else if (object.getClass() == ClassConstants.CHAR) {
            printCharacter((Character)object);
        } else if (object.getClass() == ClassConstants.APCHAR) {
            printString("new char[] {");
            char[] chars = (char[])object;
            if (chars.length > 0) {
                printString("'");                                
                printString(String.valueOf(chars[0]));                
                printString("'");                                
                for (int index = 1; index < chars.length; index++) {
                    printString(",");                
                    printString("'");                                
                    printString(String.valueOf(chars[index]));                
                    printString("'");                                
                }
            }
            printString("}");
        } else if (object.getClass() == ClassConstants.ACHAR) {
            printString("new Character[] {");
            Character[] chars = (Character[])object;
            if (chars.length > 0) {
                printCharacter(chars[0]);                
                for (int index = 1; index < chars.length; index++) {
                    printString(",");                
                    printCharacter(chars[index]);                
                }
            }
            printString("}");
        } else if (object.getClass() == ClassConstants.BIGDECIMAL) {
            printString("new java.math.BigDecimal(\"");
            printString(((BigDecimal)object).toString());
            printString("\")");                
        } else if (object.getClass() == ClassConstants.BIGINTEGER) {
            printString("new java.math.BigInteger(\"");
            printString(((BigInteger)object).toString());
            printString("\")");                
        } else {
            printString((String)ConversionManager.getDefaultManager().convertObject(object, String.class));
        }
        
    }
    
    public void printByte(Byte aByte) {
            printString("new Byte((byte)");
            printString((aByte).toString());
            printString(")");        
    }

    public void printCharacter(Character aCharacter) {
            printString("new Character('");
            printString((aCharacter).toString());
            printString("')");
    }
}
