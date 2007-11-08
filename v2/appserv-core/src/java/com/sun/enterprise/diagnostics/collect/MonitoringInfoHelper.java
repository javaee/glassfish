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

package com.sun.enterprise.diagnostics.collect;


import com.sun.enterprise.admin.util.ClassUtil;
import com.sun.enterprise.admin.util.ArrayConversion;
import com.sun.enterprise.admin.server.core.jmx.AppServerMBeanServerFactory;
import com.sun.enterprise.admin.server.core.jmx.InitException;

import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.logging.LogDomains;

import javax.management.*;


import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * A helper which executes List, Get for Dotted Names AdminCommand
 * @author Jagadish Ramu
 */
public class MonitoringInfoHelper {

    private static final String LIST_COMMAND = "list";
    private static final String LIST_OPERATION = "dottedNameList";
    private static final String LIST_MONITORING_OPERATION =
            "dottedNameMonitoringList";
    private static final String GET_COMMAND = "get";
    private static final String GET_OPERATION = "dottedNameGet";
    private static final String GET_MONITORING_OPERATION =
            "dottedNameMonitoringGet";


    public final static String STRING_ARRAY = (new String[]{}).
            getClass().getName();
    private static final String MONITOR_OPTION = "monitor";
    private static final String OBJECT_NAME =
            "com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
    private static final String PROPERTY_STRING = "property|system-property";

    public final static String SECURE = "secure";
    private ArrayList<String> operands;
    private String name;
    private HashMap options;

    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    private ArrayList<String> output;

    public MonitoringInfoHelper() {
        options = new HashMap();
    }

    /**
     * Operands passed to List/ Get command
     *
     * @param operands
     */
    public void setOperands(ArrayList<String> operands) {
        this.operands = operands;
    }

    /**
     * Operands passed to List/ Get command
     *
     * @return Vector representing the operands
     */
    public ArrayList<String> getOperands() {
        return operands;
    }

    /**
     * set the command name ( list / get )
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get the command name (list / get)
     *
     * @return String
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the list of options for this AdminCommand
     *
     * @param options List of options for this command
     */
    public void setOptions(HashMap options) {
        this.options = options;
    }

    /**
     * Finds the option with the give name
     *
     * @return Option return option if found else return null
     */
    public String getOption(String optionName) {
        if (!optionNameExist(optionName)) {
            return null;
        }
        return (String) options.get(optionName);
    }

    /**
     * returns true if the option name exist in the options list
     *
     * @ true if option name exist
     */
    private boolean optionNameExist(String optionName) {
        return options.containsKey(optionName);
    }


    /**
     * Sets the option value for the give name
     *
     * @param optionName  name of the option
     * @param optionValue value of the option
     */
    public void setOption(String optionName, String optionValue) {
        options.put(optionName, optionValue);
    }


    /**
     * Finds the option with the give name
     *
     * @return boolean return boolean type of the option value
     */
    protected boolean getBooleanOption(String optionName) {
        return Boolean.valueOf(getOption(optionName));
    }


    /**
     * execute the command (list or get)
     *
     * @param result - list inwhich results are stored
     * @throws com.sun.enterprise.diagnostics.DiagnosticException
     */
    public void runCommand(ArrayList<String> result) throws DiagnosticException {
        output = result;
        //use http connector

        final Object[] params = getDottedNamesParam
                (getName().equals(LIST_COMMAND) ?
                false : true);
        final String[] types = new String[]{STRING_ARRAY};
        final String operationName = getOperation();


        try {

            MBeanServer mbs = AppServerMBeanServerFactory.
                    getMBeanServerInstance();
            // we always invoke with an array, and so the
            //  result is always an Object []
            final Object[] returnValues = (Object[]) mbs.
                    invoke(new ObjectName(OBJECT_NAME),
                    operationName, params, types);
            if (operationName.indexOf("List") >= 0) {
                // a list operation, just print the String []
                displayResultFromList((String []) returnValues);
            }

            else {
                final String[]    userArgs = (String []) params[0];

                displayResultFromGetOrSet(userArgs, returnValues);
            }
        }

        catch (InitException ie) {
            logger.log(Level.WARNING, "Initialization" +
                    " exception occurred while getting" +
                    " MBean Server Instance", ie);
            throw new DiagnosticException(ie.getMessage());
        }

        catch (Exception e) {
            final String msg = getExceptionMessage(e);
            if (msg != null) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            throw new DiagnosticException(e.getMessage());
        }
    }

    private static String OUTER_ARRAY_DELIM = System.
            getProperty("line.separator");    // top-level array

    /**
     * figure out the returnValue type and call appropriate print methods
     *
     * @ params returnval
     * @ throws CommandException if could not print AttributeList
     */
    private void
            displayResultFromGetOrSet(final String[] inputs,
                                      final Object [] returnValues)
            throws Exception {
        if (returnValues.length == 1) {
            // there was a single string provided as input
            final Object result = returnValues[0];

            if (result instanceof Exception) {
                throw (Exception) result;
            } else if (result.getClass() == Attribute[].class) {
                // this must have been the result of a wildcard input
                final Attribute[]    attrs = (Attribute[]) result;

                if (attrs.length == 0) {
                    throw new AttributeNotFoundException("NoWildcardMatches");
                }

                printMessage(stringify(attrs, OUTER_ARRAY_DELIM));
            } else {
                printMessage(stringify(result, OUTER_ARRAY_DELIM));
            }
        } else {
            // more than one input String; collect all the resulting Attributes
            // into one big non-duplicate sorted list and print them.
            final Attribute[]    attrs = collectAllAttributes(returnValues);

            //addToResults(  stringify( attrs, OUTER_ARRAY_DELIM ) );

            for (Attribute attr : attrs) {
                addToResults(attr.getName() + " = " + attr.getValue());
            }

            // tell about any failures
            for (int i = 0; i < returnValues.length; ++i) {
                if (returnValues[i] instanceof Exception) {
                    final Exception e = (Exception) returnValues[i];

                    final String msg = "ErrorInGetSet " +
                            inputs[i] + e.getLocalizedMessage();

                    printMessage(msg);
                }
            }
        }
    }


    /*
         Extract all attributes from the results into one
          large array without duplicates.
     */
    private Attribute []
            collectAllAttributes(Object [] results) {
        // use a HashMap to eliminate duplicates; use name as the key
        final HashMap attrs = new HashMap();

        for (final Object result : results) {
            if (result instanceof Attribute) {
                attrs.put(((Attribute) result).getName(), result);
            } else if (result instanceof Attribute[]) {
                final Attribute[]    list = (Attribute[]) result;

                for (int attrIndex = 0; attrIndex < list.length; ++attrIndex) {
                    final Attribute attr = list[attrIndex];

                    attrs.put(attr.getName(), attr);
                }
            } else {
                assert(result instanceof Exception);
            }
        }

        final Attribute[]    attrsArray = new Attribute[ attrs.size() ];
        attrs.values().toArray(attrsArray);
        Arrays.sort(attrsArray, new AttributeComparator());

        return (attrsArray);
    }

/*
		Compare Attributes (for sorting).  Attribute by itself
		doesn't work correctly.
	 */

    private final class AttributeComparator implements Comparator {
        public int
                compare(Object o1, Object o2) {
            final Attribute attr1 = (Attribute) o1;
            final Attribute attr2 = (Attribute) o2;

            return (attr1.getName().compareTo(attr2.getName()));
        }

        public boolean
                equals(Object other) {
            return (other instanceof AttributeComparator);
        }
    }

    /**
     * get the dotted notation from the operand and convert it to a Object[]
     *
     * @return Object[]
     */
    private Object[] getDottedNamesParam(boolean convertUnderscore) {
        final ArrayList<String> dottedNames = getOperands();
        String [] dottedNamesArray = new String[dottedNames.size()];

        for (int ii = 0; ii < dottedNames.size(); ii++) {
            if (convertUnderscore) {
                dottedNamesArray[ii] = convertUnderscoreToHyphen
                        (dottedNames.get(ii));
            } else {
                dottedNamesArray[ii] = dottedNames.get(ii);
            }
        }
        return new Object[]{dottedNamesArray};


    }


    /**
     * get the operation to invoke depending on the command name and option
     * if command name is "set" then the operation is dottedNameSet
     * if command name is "get" then the operation is dottedNameGet
     * if the command name is "get" with --monitor option to true, then the
     * operation is dottedNameMonitoringGet
     * all others return null.
     *
     * @return name of th operation
     */
    private String getOperation() {

        if (getName().equals(GET_COMMAND)) {
            if (getBooleanOption(MONITOR_OPTION)) {
                return GET_MONITORING_OPERATION;
            } else {
                return GET_OPERATION;
            }
        } else if (getName().equals(LIST_COMMAND)) {
            if (getBooleanOption(MONITOR_OPTION)) {
                return LIST_MONITORING_OPERATION;
            } else {
                return LIST_OPERATION;
            }
        }
        return null;

    }

    /**
     * Log the messages
     *
     * @param msg
     */
    private void printMessage(String msg) {
        logger.log(Level.INFO, msg, "INFO");
    }

    /**
     * add the results to output
     *
     * @param msg
     */
    private void addToResults(String msg) {
        if (output != null) {
            output.add(msg);

        }
    }


    private String
            getExceptionMessage(Exception e) {
        String msg = null;

        if (e instanceof RuntimeMBeanException) {
            RuntimeMBeanException rmbe = (RuntimeMBeanException) e;
            msg = rmbe.getTargetException().getLocalizedMessage();
        } else if (e instanceof RuntimeOperationsException) {
            RuntimeOperationsException roe = (RuntimeOperationsException) e;
            msg = roe.getTargetException().getLocalizedMessage();
        } else {
            msg = e.getLocalizedMessage();
        }
        if (msg == null || msg.length() == 0) {
            msg = e.getMessage();
        }

        if (msg == null || msg.length() == 0) {
            msg = e.getClass().getName();
        }

        return (msg);
    }

    private static String INNER_ARRAY_DELIM = ",";
    // when an array is inside another

    private String stringifyArray(final Object [] a, String delim) {
        final StringBuffer buf = new StringBuffer();

        for (int i = 0; i < a.length; ++i) {
            buf.append(stringify(a[i], INNER_ARRAY_DELIM));
            if (i != a.length - 1) {
                buf.append(delim);
            }
        }
        return (buf.toString());
    }


    /*
         Turn the object into a String suitable for display to the user.
      */
    private String stringify(Object o, String delim) {
        String result = null;

        if (o == null) {
            result = "";
        } else if (o instanceof Attribute) {
            final Attribute attr = (Attribute) o;

            result = attr.getName() + " = " + stringify(attr.getValue(),
                    INNER_ARRAY_DELIM);
        } else if (ClassUtil.objectIsPrimitiveArray(o)) {
            final Object [] objectList = ArrayConversion.toAppropriateType(o);

            result = stringifyArray(objectList, delim);
        } else if (ClassUtil.objectIsArray(o)) {
            result = stringifyArray((Object []) o, delim);
        } else if (o instanceof Exception) {
            final Exception e = (Exception) o;

            result = getExceptionMessage(e);
        } else {
            result = o.toString();
        }


        return (result);
    }

    private void displayResultFromList(final String [] result) {
        if (result.length == 0) {
            //need to convert the operands to String for display

            final String displayOperands = stringify(getDottedNamesParam(true),
                    INNER_ARRAY_DELIM);
            printMessage("EmptyList - " + displayOperands);
        } else {
            for (String value : result) {
                addToResults(value);
            }
        }
    }


    /**
     * This method will convert the attribute in the dotted name notation
     * from underscore to hyphen.
     *
     * @param param - the dotted name to convert
     * @return the converted string
     */
    public String convertUnderscoreToHyphen(String param) {
        int endIndex = param.indexOf('=');
        int begIndex = (endIndex > 0) ? param.lastIndexOf('.', endIndex) :
                param.lastIndexOf('.');
        if (begIndex < 1 || checkPropertyToConvert(
                param.substring(0, begIndex))) {
            return param;
        }
        if (endIndex > 0) {
            return param.substring(0, begIndex) +
                    param.substring(begIndex, endIndex).replace('_', '-') +
                    param.substring(endIndex);
        } else {
            return param.substring(0, begIndex) +
                    param.substring(begIndex).replace('_', '-');
        }
    }

    /**
     * This method checks if the element in the dotted name contains "property"
     * or "system-property".  If the element is "property" or "system-property"
     * then return true else return false.
     *
     * @param param - dotted name
     * @return true if dotted name contains "property" or "system-property"
     *         false
     */
    public boolean checkPropertyToConvert(String param) {
        final int index = param.lastIndexOf('.');
        if (index < 0) {
            return false;
        }
        final String elementName = param.substring(index + 1);
        return elementName.matches(PROPERTY_STRING);
    }
}
