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
package oracle.toplink.essentials.expressions;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <p>
 * <b>Purpose</b>: ADVANCED: The expression operator is used internally to define SQL operations and functions.
 * It is possible for an advanced user to define their own operators.
 */
public class ExpressionOperator implements Serializable {

    /** Required for serialization compatibility. */
    static final long serialVersionUID = -7066100204792043980L;
    protected int selector;
    protected String[] databaseStrings;
    protected boolean isPrefix = false;
    protected boolean isRepeating = false;
    protected Class nodeClass;
    protected int type;
    protected int[] argumentIndices = null;
    protected static Hashtable allOperators;
    protected static Hashtable platformOperatorNames;
    protected String[] javaStrings;

    /** Operator types */
    public static final int LogicalOperator = 1;
    public static final int ComparisonOperator = 2;
    public static final int AggregateOperator = 3;
    public static final int OrderOperator = 4;
    public static final int FunctionOperator = 5;

    /** Logical operators */
    public static final int And = 1;
    public static final int Or = 2;
    public static final int Not = 3;

    /** Comparison operators */
    public static final int Equal = 4;
    public static final int NotEqual = 5;
    public static final int EqualOuterJoin = 6;
    public static final int LessThan = 7;
    public static final int LessThanEqual = 8;
    public static final int GreaterThan = 9;
    public static final int GreaterThanEqual = 10;
    public static final int Like = 11;
    public static final int NotLike = 12;
    public static final int In = 13;
    public static final int InSubQuery = 125;
    public static final int NotIn = 14;
    public static final int NotInSubQuery = 126;
    public static final int Between = 15;
    public static final int NotBetween = 16;
    public static final int IsNull = 17;
    public static final int NotNull = 18;
    public static final int Exists = 86;
    public static final int NotExists = 88;
    public static final int LikeEscape = 89;
    public static final int Decode = 105;
    public static final int Case = 117;

    /** Aggregate operators */
    public static final int Count = 19;
    public static final int Sum = 20;
    public static final int Average = 21;
    public static final int Maximum = 22;
    public static final int Minimum = 23;
    public static final int StandardDeviation = 24;
    public static final int Variance = 25;
    public static final int Distinct = 87;

    /** Ordering operators */
    public static final int Ascending = 26;
    public static final int Descending = 27;

    /** Function operators */

    // General
    public static final int ToUpperCase = 28;
    public static final int ToLowerCase = 29;
    public static final int Chr = 30;
    public static final int Concat = 31;
    public static final int HexToRaw = 32;
    public static final int Initcap = 33;
    public static final int Instring = 34;
    public static final int Soundex = 35;
    public static final int LeftPad = 36;
    public static final int LeftTrim = 37;
    public static final int Replace = 38;
    public static final int RightPad = 39;
    public static final int RightTrim = 40;
    public static final int Substring = 41;
    public static final int ToNumber = 42;
    public static final int Translate = 43;
    public static final int Trim = 44;
    public static final int Ascii = 45;
    public static final int Length = 46;
    public static final int CharIndex = 96;
    public static final int CharLength = 97;
    public static final int Difference = 98;
    public static final int Reverse = 99;
    public static final int Replicate = 100;
    public static final int Right = 101;
    public static final int Locate = 112;
    public static final int Locate2 = 113;
    public static final int ToChar = 114;
    public static final int ToCharWithFormat = 115;
    public static final int RightTrim2 = 116;
    public static final int Any = 118;
    public static final int Some = 119;
    public static final int All = 120;
    public static final int Trim2 = 121;
    public static final int LeftTrim2 = 122;

    // Date
    public static final int AddMonths = 47;
    public static final int DateToString = 48;
    public static final int LastDay = 49;
    public static final int MonthsBetween = 50;
    public static final int NextDay = 51;
    public static final int RoundDate = 52;
    public static final int ToDate = 53;
    public static final int Today = 54;
    public static final int AddDate = 90;
    public static final int DateName = 92;
    public static final int DatePart = 93;
    public static final int DateDifference = 94;
    public static final int TruncateDate = 102;
    public static final int NewTime = 103;
    public static final int Nvl = 104;
    public static final int CurrentDate = 123;
    public static final int CurrentTime = 124;

    // Math
    public static final int Ceil = 55;
    public static final int Cos = 56;
    public static final int Cosh = 57;
    public static final int Abs = 58;
    public static final int Acos = 59;
    public static final int Asin = 60;
    public static final int Atan = 61;
    public static final int Exp = 62;
    public static final int Sqrt = 63;
    public static final int Floor = 64;
    public static final int Ln = 65;
    public static final int Log = 66;
    public static final int Mod = 67;
    public static final int Power = 68;
    public static final int Round = 69;
    public static final int Sign = 70;
    public static final int Sin = 71;
    public static final int Sinh = 72;
    public static final int Tan = 73;
    public static final int Tanh = 74;
    public static final int Trunc = 75;
    public static final int Greatest = 76;
    public static final int Least = 77;
    public static final int Add = 78;
    public static final int Subtract = 79;
    public static final int Divide = 80;
    public static final int Multiply = 81;
    public static final int Atan2 = 91;
    public static final int Cot = 95;

    // Object-relational
    public static final int Deref = 82;
    public static final int Ref = 83;
    public static final int RefToHex = 84;
    public static final int Value = 85;

    //XML Specific
    public static final int Extract = 106;
    public static final int ExtractValue = 107;
    public static final int ExistsNode = 108;
    public static final int GetStringVal = 109;
    public static final int GetNumberVal = 110;
    public static final int IsFragment = 111;

    /**
     * ADVANCED:
     * Create a new operator.
     */
    public ExpressionOperator() {
        this.type = FunctionOperator;
        // For bug 2780072 provide default behavior to make this class more useable.
        setNodeClass(ClassConstants.FunctionExpression_Class);
    }

    /**
     * ADVANCED:
     * Create a new operator with the given name(s) and strings to print.
     */
    public ExpressionOperator(int selector, Vector newDatabaseStrings) {
        this.type = FunctionOperator;
        // For bug 2780072 provide default behavior to make this class more useable.
        setNodeClass(ClassConstants.FunctionExpression_Class);
        this.selector = selector;
        this.printsAs(newDatabaseStrings);
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator abs() {
        return simpleFunction(Abs, "ABS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator acos() {
        return simpleFunction(Acos, "ACOS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator addDate() {
        ExpressionOperator exOperator = simpleThreeArgumentFunction(AddDate, "DATEADD");
        int[] indices = new int[3];
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 0;

        exOperator.setArgumentIndices(indices);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator addMonths() {
        return simpleTwoArgumentFunction(AddMonths, "ADD_MONTHS");
    }

    /**
     * ADVANCED:
     * Add an operator to the global list of operators.
     */
    public static void addOperator(ExpressionOperator exOperator) {
        allOperators.put(new Integer(exOperator.getSelector()), exOperator);
    }

    /**
     * INTERNAL:
     * Create the AND operator.
     */
    public static ExpressionOperator and() {
        return simpleLogical(And, "AND", "and");
    }

    /**
     * INTERNAL:
     * Apply this to an object in memory.
     * Throw an error if the function is not supported.
     */
    public Object applyFunction(Object source, Vector arguments) {
        if (source instanceof String) {
            if (getSelector() == ToUpperCase) {
                return ((String)source).toUpperCase();
            } else if (getSelector() == ToLowerCase) {
                return ((String)source).toLowerCase();
            } else if ((getSelector() == Concat) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof String)) {
                return ((String)source).concat((String)arguments.elementAt(0));
            } else if ((getSelector() == Substring) && (arguments.size() == 2) && (arguments.elementAt(0) instanceof Number) && (arguments.elementAt(1) instanceof Number)) {
                // assume the first parameter to be 1-based first index of the substring, the second - substring length.
                int beginIndexInclusive = ((Number)arguments.elementAt(0)).intValue() - 1;
                int endIndexExclusive = beginIndexInclusive +  ((Number)arguments.elementAt(1)).intValue();
                return ((String)source).substring(beginIndexInclusive, endIndexExclusive);
            } else if (getSelector() == ToNumber) {
                return new java.math.BigDecimal((String)source);
            } else if (getSelector() == Trim) {
                return ((String)source).trim();
            } else if (getSelector() == Length) {
                return new Integer(((String)source).length());
            }
        } else if (source instanceof Number) {
            if (getSelector() == Ceil) {
                return new Double(Math.ceil(((Number)source).doubleValue()));
            } else if (getSelector() == Cos) {
                return new Double(Math.cos(((Number)source).doubleValue()));
            } else if (getSelector() == Abs) {
                return new Double(Math.abs(((Number)source).doubleValue()));
            } else if (getSelector() == Acos) {
                return new Double(Math.acos(((Number)source).doubleValue()));
            } else if (getSelector() == Asin) {
                return new Double(Math.asin(((Number)source).doubleValue()));
            } else if (getSelector() == Atan) {
                return new Double(Math.atan(((Number)source).doubleValue()));
            } else if (getSelector() == Exp) {
                return new Double(Math.exp(((Number)source).doubleValue()));
            } else if (getSelector() == Sqrt) {
                return new Double(Math.sqrt(((Number)source).doubleValue()));
            } else if (getSelector() == Floor) {
                return new Double(Math.floor(((Number)source).doubleValue()));
            } else if (getSelector() == Log) {
                return new Double(Math.log(((Number)source).doubleValue()));
            } else if ((getSelector() == Power) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(Math.pow(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if (getSelector() == Round) {
                return new Double(Math.round(((Number)source).doubleValue()));
            } else if (getSelector() == Sin) {
                return new Double(Math.sin(((Number)source).doubleValue()));
            } else if (getSelector() == Tan) {
                return new Double(Math.tan(((Number)source).doubleValue()));
            } else if ((getSelector() == Greatest) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(Math.max(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if ((getSelector() == Least) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(Math.min(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if ((getSelector() == Add) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(((Number)source).doubleValue() + (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((getSelector() == Subtract) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(((Number)source).doubleValue() - (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((getSelector() == Divide) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(((Number)source).doubleValue() / (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((getSelector() == Multiply) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return new Double(((Number)source).doubleValue() * (((Number)arguments.elementAt(0)).doubleValue()));
            }
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Create the ASCENDING operator.
     */
    public static ExpressionOperator ascending() {
        return simpleOrdering(Ascending, "ASC", "ascending");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ascii() {
        return simpleFunction(Ascii, "ASCII");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator asin() {
        return simpleFunction(Asin, "ASIN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator atan() {
        return simpleFunction(Atan, "ATAN");
    }

    /**
     * INTERNAL:
     * Create the AVERAGE operator.
     */
    public static ExpressionOperator average() {
        return simpleAggregate(Average, "AVG", "average");
    }

    /**
     * ADVANCED:
     * Tell the operator to be postfix, i.e. its strings start printing after
     * those of its first argument.
     */
    public void bePostfix() {
        isPrefix = false;
    }

    /**
     * ADVANCED:
     * Tell the operator to be pretfix, i.e. its strings start printing before
     * those of its first argument.
     */
    public void bePrefix() {
        isPrefix = true;
    }

    /**
     * INTERNAL:
     * Make this a repeating argument. Currently unused.
     */
    public void beRepeating() {
        isRepeating = true;
    }

    /**
     * INTERNAL:
     * Create the BETWEEN Operator
     */
    public static ExpressionOperator between() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(Between);
        result.setType(ComparisonOperator);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" BETWEEN ");
        v.addElement(" AND ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#caseStatement(Hashtable, String)
     */
    public static ExpressionOperator caseStatement() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Case);
        exOperator.bePrefix();
        exOperator.setNodeClass(FunctionExpression.class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ceil() {
        return simpleFunction(Ceil, "CEIL");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator charIndex() {
        return simpleTwoArgumentFunction(CharIndex, "CHARINDEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator charLength() {
        return simpleFunction(CharLength, "CHAR_LENGTH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator chr() {
        return simpleFunction(Chr, "CHR");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator concat() {
        return simpleMath(Concat, "+");
    }

    /**
     * INTERNAL:
     * Compare bewteen in memory.
     */
    public boolean conformBetween(Object left, Object right) {
        Object start = ((Vector)right).elementAt(0);
        Object end = ((Vector)right).elementAt(1);
        if ((left == null) || (start == null) || (end == null)) {
            return false;
        }
        if ((left instanceof Number) && (start instanceof Number) && (end instanceof Number)) {
            return ((((Number)left).doubleValue()) >= (((Number)start).doubleValue())) && ((((Number)left).doubleValue()) <= (((Number)end).doubleValue()));
        } else if ((left instanceof String) && (start instanceof String) && (end instanceof String)) {
            return ((((String)left).compareTo(((String)start)) > 0) || (((String)left).compareTo(((String)start)) == 0)) && ((((String)left).compareTo(((String)end)) < 0) || (((String)left).compareTo(((String)end)) == 0));
        } else if ((left instanceof java.util.Date) && (start instanceof java.util.Date) && (end instanceof java.util.Date)) {
            return (((java.util.Date)left).after(((java.util.Date)start)) || ((java.util.Date)left).equals(((java.util.Date)start))) && (((java.util.Date)left).before(((java.util.Date)end)) || ((java.util.Date)left).equals(((java.util.Date)end)));
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Compare like in memory.
     * This only works for % not _.
     * @author Christian Weeks aka ChristianLink
     */
    public boolean conformLike(Object left, Object right) {
        if ((right == null) && (left == null)) {
            return true;
        }
        if (!(right instanceof String) || !(left instanceof String)) {
            throw QueryException.cannotConformExpression();
        }
        String likeString = (String)right;
        if (likeString.indexOf("_") != -1) {
            throw QueryException.cannotConformExpression();
        }
        String value = (String)left;
        if (likeString.indexOf("%") == -1) {
            // No % symbols
            return left.equals(right);
        }
        boolean strictStart = !likeString.startsWith("%");
        boolean strictEnd = !likeString.endsWith("%");
        StringTokenizer tokens = new StringTokenizer(likeString, "%");
        int lastPosition = 0;
        String lastToken = null;
        if (strictStart) {
            lastToken = tokens.nextToken();
            if (!value.startsWith(lastToken)) {
                return false;
            }
        }
        while (tokens.hasMoreTokens()) {
            lastToken = tokens.nextToken();
            lastPosition = value.indexOf(lastToken, lastPosition);
            if (lastPosition < 0) {
                return false;
            }
        }
        if (strictEnd) {
            return value.endsWith(lastToken);
        }
        return true;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cos() {
        return simpleFunction(Cos, "COS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cosh() {
        return simpleFunction(Cosh, "COSH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cot() {
        return simpleFunction(Cot, "COT");
    }

    /**
     * INTERNAL:
     * Create the COUNT operator.
     */
    public static ExpressionOperator count() {
        return simpleAggregate(Count, "COUNT", "count");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateDifference() {
        return simpleThreeArgumentFunction(DateDifference, "DATEDIFF");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateName() {
        return simpleTwoArgumentFunction(DateName, "DATENAME");
    }

    /**
     * INTERNAL:
     * Oracle equivalent to DATENAME is TO_CHAR.
     */
    public static ExpressionOperator oracleDateName() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(DateName);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("TO_CHAR(");
        v.addElement(", '");
        v.addElement("')");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator datePart() {
        return simpleTwoArgumentFunction(DatePart, "DATEPART");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateToString() {
        return simpleFunction(DateToString, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toChar() {
        return simpleFunction(ToChar, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toCharWithFormat() {
        return simpleTwoArgumentFunction(ToCharWithFormat, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#decode(Hashtable, String)
     */
    public static ExpressionOperator decode() {
        ExpressionOperator exOperator = new ExpressionOperator();

        exOperator.setSelector(Decode);

        exOperator.setNodeClass(FunctionExpression.class);
        exOperator.setType(FunctionOperator);
        exOperator.bePrefix();
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator deref() {
        return simpleFunction(Deref, "DEREF");
    }

    /**
     * INTERNAL:
     * Create the DESCENDING operator.
     */
    public static ExpressionOperator descending() {
        return simpleOrdering(Descending, "DESC", "descending");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator difference() {
        return simpleTwoArgumentFunction(Difference, "DIFFERENCE");
    }

    /**
     * INTERNAL:
     * Create the DISTINCT operator.
     */
    public static ExpressionOperator distinct() {
        return simpleFunction(Distinct, "DISTINCT", "distinct");
    }

    /**
     * INTERNAL:
     * Compare the values in memory.
     * Used for in-memory querying, all operators are not support.
     */
    public boolean doesRelationConform(Object left, Object right) {
        // Big case statement follows.
        // Note, compareTo for String returns a number <= -1 if the String is less than.  We assumed that
        // it would return -1.  The same thing for strings that are greater than (ie it returns >= 1). PWK
        // Equals
        if (getSelector() == Equal) {
            if ((left == null) && (right == null)) {
                return true;
            } else if ((left == null) || (right == null)) {
                return false;
            }
            if (((left instanceof Number) && (right instanceof Number)) && (left.getClass() != right.getClass())) {
                return ((Number)left).doubleValue() == ((Number)right).doubleValue();
            }
            return left.equals(right);
        } else if (getSelector() == NotEqual) {
            if ((left == null) && (right == null)) {
                return false;
            } else if ((left == null) || (right == null)) {
                return true;
            }
            return !left.equals(right);
        } else if (getSelector() == IsNull) {
            return (left == null);
        }
        if (getSelector() == NotNull) {
            return (left != null);
        }
        // Less thans, greater thans
        else if (getSelector() == LessThan) {// You have gottan love polymorphism in Java, NOT!!!
            if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) < (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                return ((String)left).compareTo(((String)right)) < 0;
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).before(((java.util.Date)right));
            }
        } else if (getSelector() == LessThanEqual) {
            if ((left == null) && (right == null)) {
                return true;
            } else if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) <= (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue < 0) || (compareValue == 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).equals(((java.util.Date)right)) || ((java.util.Date)left).before(((java.util.Date)right));
            }
        } else if (getSelector() == GreaterThan) {
            if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) > (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue > 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).after(((java.util.Date)right));
            }
        } else if (getSelector() == GreaterThanEqual) {
            if ((left == null) && (right == null)) {
                return true;
            } else if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) >= (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue > 0) || (compareValue == 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).equals(((java.util.Date)right)) || ((java.util.Date)left).after(((java.util.Date)right));
            }
        }
        // Between
        else if ((getSelector() == Between) && (right instanceof Vector) && (((Vector)right).size() == 2)) {
            return conformBetween(left, right);
        } else if ((getSelector() == NotBetween) && (right instanceof Vector) && (((Vector)right).size() == 2)) {
            return !conformBetween(left, right);
        }
        // In
        else if ((getSelector() == In) && (right instanceof Vector)) {
            return ((Vector)right).contains(left);
        } else if ((getSelector() == NotIn) && (right instanceof Vector)) {
            return !((Vector)right).contains(left);
        }
        // Like
        //conformLike(left, right);
        else if ((getSelector() == Like) || (getSelector() == NotLike)) {
            // the regular expression framework we use to conform like is only supported in
            // JDK 1.4 and later.  We will ask our JavaPlatform to do this for us.
            int doesLikeConform = JavaPlatform.conformLike(left, right);
            if (doesLikeConform == JavaPlatform.TRUE) {
                return getSelector() == Like;// Negate for NotLike
            } else if (doesLikeConform == JavaPlatform.FALSE) {
                return getSelector() != Like;// Negate for NotLike
            }
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Initialize the outer join operator
     * Note: This is merely a shell which is incomplete, and
     * so will be replaced by the platform's operator when we
     * go to print. We need to create this here so that the expression
     * class is correct, normally it assumes functions for unknown operators.
     */
    public static ExpressionOperator equalOuterJoin() {
        return simpleRelation(EqualOuterJoin, "=*");
    }

    /**
     * PUBLIC:
     * Test for equality
     */
    public boolean equals(Object object) {
        if (!(object instanceof ExpressionOperator)) {
            return false;
        }
        return getSelector() == ((ExpressionOperator)object).getSelector();
    }

    /**
     * PUBLIC:
     * Return the hash-code based on the unique selector.
     */
    public int hashCode() {
        return getSelector();
    }

    /**
     * INTERNAL:
     * Create the EXISTS operator.
     */
    public static ExpressionOperator exists() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Exists);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("EXISTS" + " ");
        v.addElement(" ");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator exp() {
        return simpleFunction(Exp, "EXP");
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base.
     */
    public Expression expressionFor(Expression base) {
        return expressionForArguments(base, oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(0));
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and a single argument.
     */
    public Expression expressionFor(Expression base, Object value) {
        return newExpressionForArgument(base, value);
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and a single argument.
     * Base is used last in the expression
     */
    public Expression expressionForWithBaseLast(Expression base, Object value) {
        return newExpressionForArgumentWithBaseLast(base, value);
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and arguments.
     */
    public Expression expressionForArguments(Expression base, Vector arguments) {
        return newExpressionForArguments(base, arguments);
    }

    /**
     * INTERNAL:
     * Create the extract expression operator
     */
    public static ExpressionOperator extract() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("extract(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(Extract);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the extractValue expression operator
     */
    public static ExpressionOperator extractValue() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("extractValue(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(ExtractValue);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the existsNode expression operator
     */
    public static ExpressionOperator existsNode() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("existsNode(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(ExistsNode);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator getStringVal() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".getStringVal()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(GetStringVal);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator getNumberVal() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".getNumberVal()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(GetNumberVal);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator isFragment() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".isFragment()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(IsFragment);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator floor() {
        return simpleFunction(Floor, "FLOOR");
    }

    /**
     * ADVANCED:
     * Return the hashtable of all operators.
     */
    public static synchronized Hashtable getAllOperators() {
        if (allOperators == null) {
            initializeOperators();
        }
        return allOperators;
    }

    /**
     * INTERNAL:
     */
    public String[] getDatabaseStrings() {
        return databaseStrings;
    }

    /**
     * INTERNAL:
     */
    public String[] getJavaStrings() {
        return javaStrings;
    }

    /**
     * INTERNAL:
     */
    public Class getNodeClass() {
        return nodeClass;
    }

    /**
     * INTERNAL:
     * Lookup the operator with the given name.
     */
    public static ExpressionOperator getOperator(Integer selector) {
        return (ExpressionOperator)getAllOperators().get(selector);
    }

    /**
     * INTERNAL:
     * Return the selector id.
     */
    public int getSelector() {
        return selector;
    }

    /**
     * ADVANCED:
     * Return the type of function.
     * This must be one of the static function types defined in this class.
     */
    public int getType() {
        return this.type;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator greatest() {
        return simpleTwoArgumentFunction(Greatest, "GREATEST");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator hexToRaw() {
        return simpleFunction(HexToRaw, "HEXTORAW");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ifNull() {
        return simpleTwoArgumentFunction(Nvl, "NVL");
    }

    /**
     * INTERNAL:
     * Create the IN operator.
     */
    public static ExpressionOperator in() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(In);
        Vector v = new Vector(2);
        v.addElement(" IN (");
        v.addElement(")");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the IN operator taking a subquery. 
     * Note, the subquery itself comes with parenethesis, so the IN operator
     * should not add any parenethesis.
     */
    public static ExpressionOperator inSubQuery() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(InSubQuery);
        Vector v = new Vector(1);
        v.addElement(" IN ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator initcap() {
        return simpleFunction(Initcap, "INITCAP");
    }

    /**
     * INTERNAL:
     */
    protected static void initializeAggregateFunctionOperators() {
        addOperator(count());
        addOperator(sum());
        addOperator(average());
        addOperator(minimum());
        addOperator(maximum());
        addOperator(variance());
        addOperator(standardDeviation());
        addOperator(distinct());
    }

    /**
     * INTERNAL:
     */
    protected static void initializeFunctionOperators() {
        addOperator(notOperator());
        addOperator(ascending());
        addOperator(descending());
        addOperator(any());
        addOperator(some());
        addOperator(all());
        addOperator(in());
        addOperator(inSubQuery());
        addOperator(notIn());
        addOperator(notInSubQuery());
    }

    /**
     * INTERNAL:
     */
    protected static void initializeLogicalOperators() {
        addOperator(and());
        addOperator(or());
        addOperator(isNull());
        addOperator(notNull());

    }

    /**
     * INTERNAL:
     */
    public static Hashtable initializeOperators() {
        resetOperators();
        initializeFunctionOperators();
        initializeRelationOperators();
        initializeLogicalOperators();
        initializeAggregateFunctionOperators();
        return allOperators;
    }

    /**
     * INTERNAL:
     * Initialize a mapping to the platform operator names for usage with exceptions.
     */
    public static String getPlatformOperatorName(int operator) {
        String name = (String)getPlatformOperatorNames().get(new Integer(operator));
        if (name == null) {
            name = String.valueOf(operator);
        }
        return name;
    }

    /**
     * INTERNAL:
     * Initialize a mapping to the platform operator names for usage with exceptions.
     */
    public static synchronized Hashtable getPlatformOperatorNames() {
        if (platformOperatorNames == null) {
            platformOperatorNames = new Hashtable();
            platformOperatorNames.put(new Integer(ToUpperCase), "ToUpperCase");
            platformOperatorNames.put(new Integer(ToLowerCase), "ToLowerCase");
            platformOperatorNames.put(new Integer(Chr), "Chr");
            platformOperatorNames.put(new Integer(Concat), "Concat");
            platformOperatorNames.put(new Integer(HexToRaw), "HexToRaw");
            platformOperatorNames.put(new Integer(Initcap), "Initcap");
            platformOperatorNames.put(new Integer(Instring), "Instring");
            platformOperatorNames.put(new Integer(Soundex), "Soundex");
            platformOperatorNames.put(new Integer(LeftPad), "LeftPad");
            platformOperatorNames.put(new Integer(LeftTrim), "LeftTrim");
            platformOperatorNames.put(new Integer(RightPad), "RightPad");
            platformOperatorNames.put(new Integer(RightTrim), "RightTrim");
            platformOperatorNames.put(new Integer(Substring), "Substring");
            platformOperatorNames.put(new Integer(Translate), "Translate");
            platformOperatorNames.put(new Integer(Ascii), "Ascii");
            platformOperatorNames.put(new Integer(Length), "Length");
            platformOperatorNames.put(new Integer(CharIndex), "CharIndex");
            platformOperatorNames.put(new Integer(CharLength), "CharLength");
            platformOperatorNames.put(new Integer(Difference), "Difference");
            platformOperatorNames.put(new Integer(Reverse), "Reverse");
            platformOperatorNames.put(new Integer(Replicate), "Replicate");
            platformOperatorNames.put(new Integer(Right), "Right");
            platformOperatorNames.put(new Integer(Locate), "Locate");
            platformOperatorNames.put(new Integer(Locate2), "Locate");
            platformOperatorNames.put(new Integer(ToNumber), "ToNumber");
            platformOperatorNames.put(new Integer(ToChar), "ToChar");
            platformOperatorNames.put(new Integer(ToCharWithFormat), "ToChar");
            platformOperatorNames.put(new Integer(AddMonths), "AddMonths");
            platformOperatorNames.put(new Integer(DateToString), "DateToString");
            platformOperatorNames.put(new Integer(MonthsBetween), "MonthsBetween");
            platformOperatorNames.put(new Integer(NextDay), "NextDay");
            platformOperatorNames.put(new Integer(RoundDate), "RoundDate");
            platformOperatorNames.put(new Integer(AddDate), "AddDate");
            platformOperatorNames.put(new Integer(DateName), "DateName");
            platformOperatorNames.put(new Integer(DatePart), "DatePart");
            platformOperatorNames.put(new Integer(DateDifference), "DateDifference");
            platformOperatorNames.put(new Integer(TruncateDate), "TruncateDate");
            platformOperatorNames.put(new Integer(NewTime), "NewTime");
            platformOperatorNames.put(new Integer(Nvl), "Nvl");
            platformOperatorNames.put(new Integer(NewTime), "NewTime");
            platformOperatorNames.put(new Integer(Ceil), "Ceil");
            platformOperatorNames.put(new Integer(Cos), "Cos");
            platformOperatorNames.put(new Integer(Cosh), "Cosh");
            platformOperatorNames.put(new Integer(Abs), "Abs");
            platformOperatorNames.put(new Integer(Acos), "Acos");
            platformOperatorNames.put(new Integer(Asin), "Asin");
            platformOperatorNames.put(new Integer(Atan), "Atan");
            platformOperatorNames.put(new Integer(Exp), "Exp");
            platformOperatorNames.put(new Integer(Sqrt), "Sqrt");
            platformOperatorNames.put(new Integer(Floor), "Floor");
            platformOperatorNames.put(new Integer(Ln), "Ln");
            platformOperatorNames.put(new Integer(Log), "Log");
            platformOperatorNames.put(new Integer(Mod), "Mod");
            platformOperatorNames.put(new Integer(Power), "Power");
            platformOperatorNames.put(new Integer(Round), "Round");
            platformOperatorNames.put(new Integer(Sign), "Sign");
            platformOperatorNames.put(new Integer(Sin), "Sin");
            platformOperatorNames.put(new Integer(Sinh), "Sinh");
            platformOperatorNames.put(new Integer(Tan), "Tan");
            platformOperatorNames.put(new Integer(Tanh), "Tanh");
            platformOperatorNames.put(new Integer(Trunc), "Trunc");
            platformOperatorNames.put(new Integer(Greatest), "Greatest");
            platformOperatorNames.put(new Integer(Least), "Least");
            platformOperatorNames.put(new Integer(Add), "Add");
            platformOperatorNames.put(new Integer(Subtract), "Subtract");
            platformOperatorNames.put(new Integer(Divide), "Divide");
            platformOperatorNames.put(new Integer(Multiply), "Multiply");
            platformOperatorNames.put(new Integer(Atan2), "Atan2");
            platformOperatorNames.put(new Integer(Cot), "Cot");
            platformOperatorNames.put(new Integer(Deref), "Deref");
            platformOperatorNames.put(new Integer(Ref), "Ref");
            platformOperatorNames.put(new Integer(RefToHex), "RefToHex");
            platformOperatorNames.put(new Integer(Value), "Value");
            platformOperatorNames.put(new Integer(Extract), "Extract");
            platformOperatorNames.put(new Integer(ExtractValue), "ExtractValue");
            platformOperatorNames.put(new Integer(ExistsNode), "ExistsNode");
            platformOperatorNames.put(new Integer(GetStringVal), "GetStringVal");
            platformOperatorNames.put(new Integer(GetNumberVal), "GetNumberVal");
            platformOperatorNames.put(new Integer(IsFragment), "IsFragment");
        }
        return platformOperatorNames;
    }

    /**
     * INTERNAL:
     */
    protected static void initializeRelationOperators() {
        addOperator(simpleRelation(Equal, "=", "equal"));
        addOperator(simpleRelation(NotEqual, "<>", "notEqual"));
        addOperator(simpleRelation(LessThan, "<", "lessThan"));
        addOperator(simpleRelation(LessThanEqual, "<=", "lessThanEqual"));
        addOperator(simpleRelation(GreaterThan, ">", "greaterThan"));
        addOperator(simpleRelation(GreaterThanEqual, ">=", "greaterThanEqual"));

        addOperator(like());
        addOperator(likeEscape());
        addOperator(notLike());
        addOperator(between());

        addOperator(exists());
        addOperator(notExists());
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator instring() {
        return simpleTwoArgumentFunction(Instring, "INSTR");
    }

    /**
     * Aggregate functions are function in the select such as COUNT.
     */
    public boolean isAggregateOperator() {
        return getType() == AggregateOperator;
    }

    /**
     * Comparison functions are functions such as = and >.
     */
    public boolean isComparisonOperator() {
        return getType() == ComparisonOperator;
    }

    /**
     * INTERNAL:
     * If we have all the required information, this operator is complete
     * and can be used as is. Otherwise we will need to look up a platform-
     * specific operator.
     */
    public boolean isComplete() {
        return (databaseStrings != null) && (databaseStrings.length != 0);
    }

    /**
     * General functions are any normal function such as UPPER.
     */
    public boolean isFunctionOperator() {
        return getType() == FunctionOperator;
    }

    /**
     * Logical functions are functions such as and and or.
     */
    public boolean isLogicalOperator() {
        return getType() == LogicalOperator;
    }

    /**
     * INTERNAL:
     * Create the ISNULL operator.
     */
    public static ExpressionOperator isNull() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ComparisonOperator);
        result.setSelector(IsNull);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" IS NULL)");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".isNull()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * Order functions are used in the order by such as ASC.
     */
    public boolean isOrderOperator() {
        return getType() == OrderOperator;
    }

    /**
     * ADVANCED:
     * Return true if this is a prefix operator.
     */
    public boolean isPrefix() {
        return isPrefix;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator lastDay() {
        return simpleFunction(LastDay, "LAST_DAY");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator least() {
        return simpleTwoArgumentFunction(Least, "LEAST");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator leftPad() {
        return simpleThreeArgumentFunction(LeftPad, "LPAD");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator leftTrim() {
        return simpleFunction(LeftTrim, "LTRIM");
    }

    /**
     * INTERNAL:
     * Build leftTrim operator that takes one parameter.
     */
    public static ExpressionOperator leftTrim2() {
        return simpleTwoArgumentFunction(LeftTrim2, "LTRIM");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator length() {
        return simpleFunction(Length, "LENGTH");
    }

    /**
     * INTERNAL:
     * Create the LIKE operator.
     */
    public static ExpressionOperator like() {
        return simpleRelation(Like, "LIKE", "like");
    }

    /**
     * INTERNAL:
     * Create the LIKE operator.
     */
    public static ExpressionOperator likeEscape() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(LikeEscape);
        result.setType(ComparisonOperator);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" LIKE ");
        v.addElement(" ESCAPE ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ln() {
        return simpleFunction(Ln, "LN");
    }

    /**
     * INTERNAL:
     * Build locate operator i.e. LOCATE("ob", t0.F_NAME)
     */
    public static ExpressionOperator locate() {
        ExpressionOperator expOperator = simpleTwoArgumentFunction(Locate, "LOCATE");
        int[] argumentIndices = new int[2];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        expOperator.setArgumentIndices(argumentIndices);
        return expOperator;
    }

    /**
     * INTERNAL:
     * Build locate operator with 3 params i.e. LOCATE("coffee", t0.DESCRIP, 4).
     * Last parameter is a start at.
     */
    public static ExpressionOperator locate2() {
        ExpressionOperator expOperator = simpleThreeArgumentFunction(Locate2, "LOCATE");
        int[] argumentIndices = new int[3];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        argumentIndices[2] = 2;
        expOperator.setArgumentIndices(argumentIndices);
        return expOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator log() {
        return simpleFunction(Log, "LOG");
    }

    /**
     * INTERNAL:
     * Create the MAXIMUM operator.
     */
    public static ExpressionOperator maximum() {
        return simpleAggregate(Maximum, "MAX", "maximum");
    }

    /**
     * INTERNAL:
     * Create the MINIMUM operator.
     */
    public static ExpressionOperator minimum() {
        return simpleAggregate(Minimum, "MIN", "minimum");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator mod() {
        return simpleTwoArgumentFunction(Mod, "MOD");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator monthsBetween() {
        return simpleTwoArgumentFunction(MonthsBetween, "MONTHS_BETWEEN");
    }

    /**
     * INTERNAL:
     * Create a new expression. Optimized for the single argument case.
     */
    public Expression newExpressionForArgument(Expression base, Object singleArgument) {
        if (representsEqualToNull(singleArgument)) {
            return base.isNull();
        }
        if (representsNotEqualToNull(singleArgument)) {
            return base.notNull();
        }

        try {
            Expression exp = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    exp = (Expression)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getNodeClass()));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                exp = (Expression)PrivilegedAccessHelper.newInstanceFromClass(getNodeClass());
            }
            exp.create(base, singleArgument, this);
            return exp;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException f) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Create a new expression. Optimized for the single argument case with base last
     */
    public Expression newExpressionForArgumentWithBaseLast(Expression base, Object singleArgument) {
        if (representsEqualToNull(singleArgument)) {
            return base.isNull();
        }
        if (representsNotEqualToNull(singleArgument)) {
            return base.notNull();
        }

        try {
            Expression exp = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    exp = (Expression)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getNodeClass()));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                exp = (Expression)PrivilegedAccessHelper.newInstanceFromClass(getNodeClass());
            }
            exp.createWithBaseLast(base, singleArgument, this);
            return exp;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException f) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * The general case.
     */
    public Expression newExpressionForArguments(Expression base, Vector arguments) {
        if (representsEqualToNull(arguments)) {
            return base.isNull();
        }
        if (representsNotEqualToNull(arguments)) {
            return base.notNull();
        }

        try {
            Expression exp = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    exp = (Expression)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getNodeClass()));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                exp = (Expression)PrivilegedAccessHelper.newInstanceFromClass(getNodeClass());
            }
            exp.create(base, arguments, this);
            return exp;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException f) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator newTime() {
        return simpleThreeArgumentFunction(NewTime, "NEW_TIME");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator nextDay() {
        return simpleTwoArgumentFunction(NextDay, "NEXT_DAY");
    }

    /**
     * INTERNAL:
     * Create the NOT EXISTS operator.
     */
    public static ExpressionOperator notExists() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(NotExists);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("NOT EXISTS" + " ");
        v.addElement(" ");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create the NOTIN operator.
     */
    public static ExpressionOperator notIn() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(NotIn);
        Vector v = new Vector(2);
        v.addElement(" NOT IN (");
        v.addElement(")");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOTIN operator taking a subQuery.
     * Note, the subquery itself comes with parenethesis, so the IN operator
     * should not add any parenethesis.
     */
    public static ExpressionOperator notInSubQuery() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(NotInSubQuery);
        Vector v = new Vector(1);
        v.addElement(" NOT IN ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOTLIKE operator.
     */
    public static ExpressionOperator notLike() {
        return simpleRelation(NotLike, "NOT LIKE", "notLike");
    }

    /**
     * INTERNAL:
     * Create the NOTNULL operator.
     */
    public static ExpressionOperator notNull() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ComparisonOperator);
        result.setSelector(NotNull);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" IS NOT NULL)");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".notNull()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOT operator.
     */
    public static ExpressionOperator notOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(Not);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("NOT (");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".not()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the OR operator.
     */
    public static ExpressionOperator or() {
        return simpleLogical(Or, "OR", "or");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator power() {
        return simpleTwoArgumentFunction(Power, "POWER");
    }

    /**
     * INTERNAL: Print the collection onto the SQL stream.
     */
    public void printCollection(Vector items, ExpressionSQLPrinter printer) {
        int dbStringIndex = 0;
        try {
            if (isPrefix()) {
                printer.getWriter().write(getDatabaseStrings()[0]);
                dbStringIndex = 1;
            } else {
                dbStringIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < items.size(); i++) {
            int index = 0;
            if (argumentIndices == null) {
                index = i;
            } else {
                index = argumentIndices[i];
            }
            ;
            Expression item = (Expression)items.elementAt(index);
            if ((getSelector() == Ref) || ((getSelector() == Deref) && (item.isObjectExpression()))) {
                DatabaseTable alias = ((ObjectExpression)item).aliasForTable((DatabaseTable)((ObjectExpression)item).getDescriptor().getTables().firstElement());
                printer.printString(alias.getName());
            } else if ((getSelector() == Count) && (item.isExpressionBuilder())) {
                printer.printString("*");
            } else if (getType() == FunctionOperator) {
                item.printSQLWithoutConversion(printer);
            } else {
                item.printSQL(printer);
            }
            if (dbStringIndex < getDatabaseStrings().length) {
                printer.printString(getDatabaseStrings()[dbStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL: Print the collection onto the SQL stream.
     */
    public void printJavaCollection(Vector items, ExpressionJavaPrinter printer) {
        int javaStringIndex = 0;

        for (int i = 0; i < items.size(); i++) {
            Expression item = (Expression)items.elementAt(i);
            item.printJava(printer);
            if (javaStringIndex < getJavaStrings().length) {
                printer.printString(getJavaStrings()[javaStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL:
     * For performance, special case printing two children, since it's by far the most common
     */
    public void printDuo(Expression first, Expression second, ExpressionSQLPrinter printer) {
        int dbStringIndex;
        if (isPrefix()) {
            printer.printString(getDatabaseStrings()[0]);
            dbStringIndex = 1;
        } else {
            dbStringIndex = 0;
        }

        first.printSQL(printer);
        if (dbStringIndex < getDatabaseStrings().length) {
            printer.printString(getDatabaseStrings()[dbStringIndex++]);
        }
        if (second != null) {
            second.printSQL(printer);
            if (dbStringIndex < getDatabaseStrings().length) {
                printer.printString(getDatabaseStrings()[dbStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL:
     * For performance, special case printing two children, since it's by far the most common
     */
    public void printJavaDuo(Expression first, Expression second, ExpressionJavaPrinter printer) {
        int javaStringIndex = 0;

        first.printJava(printer);
        if (javaStringIndex < getJavaStrings().length) {
            printer.printString(getJavaStrings()[javaStringIndex++]);
        }
        if (second != null) {
            second.printJava(printer);
            if (javaStringIndex < getJavaStrings().length) {
                printer.printString(getJavaStrings()[javaStringIndex]);
            }
        }
    }

    /**
     * ADVANCED:
     * Set the single string for this operator.
     */
    public void printsAs(String s) {
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(s);
        printsAs(v);
    }

    /**
     * ADVANCED:
     * Set the strings for this operator.
     */
    public void printsAs(Vector dbStrings) {
        this.databaseStrings = new String[dbStrings.size()];
        for (int i = 0; i < dbStrings.size(); i++) {
            getDatabaseStrings()[i] = (String)dbStrings.elementAt(i);
        }
    }

    /**
     * ADVANCED:
     * Set the single string for this operator.
     */
    public void printsJavaAs(String s) {
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(s);
        printsJavaAs(v);
    }

    /**
     * ADVANCED:
     * Set the strings for this operator.
     */
    public void printsJavaAs(Vector dbStrings) {
        this.javaStrings = new String[dbStrings.size()];
        for (int i = 0; i < dbStrings.size(); i++) {
            getJavaStrings()[i] = (String)dbStrings.elementAt(i);
        }
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ref() {
        return simpleFunction(Ref, "REF");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator refToHex() {
        return simpleFunction(RefToHex, "REFTOHEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator replace() {
        return simpleThreeArgumentFunction(Replace, "REPLACE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator replicate() {
        return simpleTwoArgumentFunction(Replicate, "REPLICATE");
    }

    /**
     * INTERNAL:
     * Test if this operator instance represents a comparison to null, which
     * we have to print specially.  Also special-cased for performance.
     */
    public boolean representsEqualToNull(Object singleArgument) {
        if (singleArgument instanceof Vector) {
            return representsEqualToNull((Vector)singleArgument);
        }
        return (getSelector() == Equal) && (singleArgument == null);
    }

    /**
     * INTERNAL:
     * Test if this operator instance represents a comparison to null, which
     * we have to print specially.
     */
    public boolean representsEqualToNull(Vector arguments) {
        return (getSelector() == Equal) && (arguments.size() == 1) && (arguments.elementAt(0) == null);
    }

    /**
     * INTERNAL:
     * Test if this operator instance represents a comparison to null, which
     * we have to print specially.  Also special-cased for performance.
     */
    public boolean representsNotEqualToNull(Object singleArgument) {
        if (singleArgument instanceof Vector) {
            return representsNotEqualToNull((Vector)singleArgument);
        }
        return (getSelector() == NotEqual) && (singleArgument == null);
    }

    /**
     * INTERNAL:
     * Test if this operator instance represents a comparison to null, which
     * we have to print specially.
     */
    public boolean representsNotEqualToNull(Vector arguments) {
        return (getSelector() == NotEqual) && (arguments.size() == 1) && (arguments.elementAt(0) == null);
    }

    /**
     * INTERNAL:
     * Reset all the operators.
     */
    public static void resetOperators() {
        allOperators = new Hashtable();
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator reverse() {
        return simpleFunction(Reverse, "REVERSE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator right() {
        return simpleTwoArgumentFunction(Right, "RIGHT");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator rightPad() {
        return simpleThreeArgumentFunction(RightPad, "RPAD");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator rightTrim() {
        return simpleFunction(RightTrim, "RTRIM");
    }

    /**
     * INTERNAL:
     * Build rightTrim operator that takes one parameter.
     * @bug 2916893 rightTrim(substring) broken.
     */
    public static ExpressionOperator rightTrim2() {
        return simpleTwoArgumentFunction(RightTrim2, "RTRIM");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator round() {
        return simpleTwoArgumentFunction(Round, "ROUND");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator roundDate() {
        return simpleTwoArgumentFunction(RoundDate, "ROUND");
    }

    /**
     * ADVANCED:
     * Set the array of indexes to use when building the SQL function.
     */
    public void setArgumentIndices(int[] indices) {
        argumentIndices = indices;
    }

    /**
     * ADVANCED:
     * Set the node class for this operator. For user-defined functions this is
     * set automatically but can be changed.
     * <p>A list of Operator types, an example, and the node class used follows.
     * <p>LogicalOperator     AND        LogicalExpression
     * <p>ComparisonOperator  <>         RelationExpression
     * <p>AggregateOperator   COUNT      FunctionExpression
     * <p>OrderOperator       ASCENDING         "
     * <p>FunctionOperator    RTRIM             "
     * <p>Node classes given belong to oracle.toplink.essentials.internal.expressions.
     */
    public void setNodeClass(Class nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * INTERNAL:
     * Set the selector id.
     */
    public void setSelector(int selector) {
        this.selector = selector;
    }

    /**
     * ADVANCED:
     * Set the type of function.
     * This must be one of the static function types defined in this class.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sign() {
        return simpleFunction(Sign, "SIGN");
    }

    /**
     * INTERNAL:
     * Create an operator for a simple aggregate given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleAggregate(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(AggregateOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement(databaseName + "(");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.printsJavaAs("." + javaName + "()");
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple function given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleFunction(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement(databaseName + "(");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple function call without parentheses
     */
    public static ExpressionOperator simpleFunctionNoParentheses(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(databaseName);
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }


    /**
     * INTERNAL:
     * Create an operator for a simple function given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleFunction(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = simpleFunction(selector, databaseName);
        exOperator.printsJavaAs("." + javaName + "()");
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple logical given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleLogical(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(LogicalOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName + " ");
        exOperator.bePostfix();
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("." + javaName + "(");
        v.addElement(")");
        exOperator.printsJavaAs(v);
        exOperator.setNodeClass(ClassConstants.LogicalExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple math operatin, i.e. +, -, *, /
     */
    public static ExpressionOperator simpleMath(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("(");
        v.addElement(" " + databaseName + " ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple ordering given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleOrdering(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(OrderOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName);
        exOperator.bePostfix();
        exOperator.printsJavaAs("." + javaName + "()");
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple relation given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleRelation(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ComparisonOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName + " ");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.RelationExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple relation given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleRelation(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = simpleRelation(selector, databaseName);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("." + javaName + "(");
        v.addElement(")");
        exOperator.printsJavaAs(v);        
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator simpleThreeArgumentFunction(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(4);
        v.addElement(dbString + "(");
        v.addElement(", ");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator simpleTwoArgumentFunction(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement(dbString + "(");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * e.g.: ... "Bob" CONCAT "Smith" ...
     * Parentheses will not be addded. [RMB - March 5 2000]
     */
    public static ExpressionOperator simpleLogicalNoParens(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("");
        v.addElement(" " + dbString + " ");
        v.addElement("");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sin() {
        return simpleFunction(Sin, "SIN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sinh() {
        return simpleFunction(Sinh, "SINH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator soundex() {
        return simpleFunction(Soundex, "SOUNDEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sqrt() {
        return simpleFunction(Sqrt, "SQRT");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator standardDeviation() {
        return simpleAggregate(StandardDeviation, "STDDEV", "standardDeviation");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator substring() {
        return simpleThreeArgumentFunction(Substring, "SUBSTR");
    }

    /**
     * INTERNAL:
     * Create the SUM operator.
     */
    public static ExpressionOperator sum() {
        return simpleAggregate(Sum, "SUM", "sum");
    }

    /**
     * INTERNAL:
     * Function, to add months to a date.
     */
    public static ExpressionOperator sybaseAddMonthsOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(AddMonths);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("DATEADD(month, ");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;

    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sybaseAtan2Operator() {
        return ExpressionOperator.simpleTwoArgumentFunction(Atan2, "ATN2");
    }

    /**
     * INTERNAL:
     * Build instring operator
     */
    public static ExpressionOperator sybaseInStringOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Instring);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("CHARINDEX(");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;

    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_NUMBER.
     */
    public static ExpressionOperator sybaseToNumberOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToNumber);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(NUMERIC, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToDateToStringOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(DateToString);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_DATE.
     */
    public static ExpressionOperator sybaseToDateOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToDate);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(DATETIME, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToCharOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToChar);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToCharWithFormatOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToCharWithFormat);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(",");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build the Sybase equivalent to Locate
     */
    public static ExpressionOperator sybaseLocateOperator() {
        ExpressionOperator result = simpleTwoArgumentFunction(ExpressionOperator.Locate, "CHARINDEX");
        int[] argumentIndices = new int[2];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        result.setArgumentIndices(argumentIndices);
        return result;
    }


    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator tan() {
        return simpleFunction(Tan, "TAN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator tanh() {
        return simpleFunction(Tanh, "TANH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toDate() {
        return simpleFunction(ToDate, "TO_DATE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator today() {
        return currentTimeStamp();
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentTimeStamp() {
        return simpleFunctionNoParentheses(Today,  "CURRENT_TIMESTAMP");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentDate() {
        return simpleFunctionNoParentheses(CurrentDate,  "CURRENT_DATE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentTime() {
        return simpleFunctionNoParentheses(CurrentTime, "CURRENT_TIME");
    }

    /**
     * INTERNAL:
     * Create the toLowerCase operator.
     */
    public static ExpressionOperator toLowerCase() {
        return simpleFunction(ToLowerCase, "LOWER", "toLowerCase");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toNumber() {
        return simpleFunction(ToNumber, "TO_NUMBER");
    }

    /**
     * Print a debug representation of this operator.
     */
    public String toString() {
        if ((getDatabaseStrings() == null) || (getDatabaseStrings().length == 0)) {
            //CR#... Print a useful name for the missing plaftorm operator.
            return "platform operator - " + getPlatformOperatorName(getSelector());
        } else {
            return "operator " + getDatabaseStrings()[0];
        }
    }

    /**
     * INTERNAL:
     * Create the TOUPPERCASE operator.
     */
    public static ExpressionOperator toUpperCase() {
        return simpleFunction(ToUpperCase, "UPPER", "toUpperCase");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator translate() {
        return simpleThreeArgumentFunction(Translate, "TRANSLATE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator trim() {
        return simpleFunction(Trim, "TRIM");
    }
    
    /**
     * INTERNAL:
     * Build Trim operator.
     */
    public static ExpressionOperator trim2() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Trim2);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("TRIM(");
        v.addElement(" FROM ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator trunc() {
        return simpleTwoArgumentFunction(Trunc, "TRUNC");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator truncateDate() {
        return simpleTwoArgumentFunction(TruncateDate, "TRUNC");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator value() {
        return simpleFunction(Value, "VALUE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator variance() {
        return simpleAggregate(Variance, "VARIANCE", "variance");
    }
    
    /**
     * INTERNAL:
     * Create the ANY operator.
     */
    public static ExpressionOperator any() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Any);
        exOperator.printsAs("ANY");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Create the SOME operator.
     */
    public static ExpressionOperator some() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Some);
        exOperator.printsAs("SOME");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Create the ALL operator.
     */
    public static ExpressionOperator all() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(All);
        exOperator.printsAs("ALL");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Indicates whether operator has selector Any or Some
     */
    public boolean isAny() {
        return  selector == ExpressionOperator.Any ||
                selector == ExpressionOperator.Some;
    }
    /**
     * INTERNAL:
     * Indicates whether operator has selector All
     */
    public boolean isAll() {
        return  selector == ExpressionOperator.All;
    }
    /**
     * INTERNAL:
     * Indicates whether operator has selector Any, Some or All
     */
    public boolean isAnyOrAll() {
        return  isAny() || isAll();
    }
}
