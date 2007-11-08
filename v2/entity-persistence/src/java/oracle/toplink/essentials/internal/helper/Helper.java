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

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.Timestamp;

import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.essentials.internal.security.PrivilegedGetField;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;
import oracle.toplink.essentials.exceptions.*;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>: Define any usefull methods that are missing from the base Java.
 */
public class Helper implements Serializable {

    /** Used to configure JDBC level date optimization. */
    protected static boolean shouldOptimizeDates = false;

    /** Used to store null values in hashtables, is helper because need to be serializable. */
    protected static final Object nullWrapper = new Helper();

    /** PERF: Used to cache a set of calendars for conversion/printing purposes. */
    protected static Vector calendarCache = new Vector(10);

    /** PERF: Cache default timezone for calendar conversion. */
    protected static TimeZone defaultTimeZone = TimeZone.getDefault();

    // Changed static initialization to lazy initialization for bug 2756643

    /** Store CR string, for some reason \n is not platform independent. */
    protected static String CR = null;

    /** Prime the platform-dependent path separator */
    protected static String PATH_SEPARATOR = null;

    /** Prime the platform-dependent file separator */
    protected static String FILE_SEPARATOR = null;

    /** Prime the platform-dependent current working directory */
    protected static String CURRENT_WORKING_DIRECTORY = null;

    /** Prime the platform-dependent temporary directory */
    protected static String TEMP_DIRECTORY = null;

    /**
     * Return if JDBC date access should be optimized.
     */
    public static boolean shouldOptimizeDates() {
        return shouldOptimizeDates;
    }

    /**
     * Return if JDBC date access should be optimized.
     */
    public static void setShouldOptimizeDates(boolean value) {
        shouldOptimizeDates = value;
    }

    /**
     * PERF: This is used to optimize Calendar conversion/printing.
     * This should only be used when a calendar is temporarily required,
     * when finished it must be released back.
     */
    public static Calendar allocateCalendar() {
        Calendar calendar = null;
        synchronized (calendarCache) {
            if (calendarCache.size() > 0) {
                calendar = (Calendar)calendarCache.remove(calendarCache.size() - 1);
            }
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        return calendar;
    }

    /**
     * PERF: Return the cached default platform.
     * Used for ensuring Calendar are in the local timezone.
     * The JDK method clones the timezone, so cache it locally.
     */
    public static TimeZone getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * PERF: This is used to optimize Calendar conversion/printing.
     * This should only be used when a calendar is temporarily required,
     * when finished it must be released back.
     */
    public static void releaseCalendar(Calendar calendar) {
        if (calendarCache.size() < 10) {
            calendarCache.add(calendar);
        }
    }

    public static void addAllToVector(Vector theVector, Vector elementsToAdd) {
        for (Enumeration stream = elementsToAdd.elements(); stream.hasMoreElements();) {
            theVector.addElement(stream.nextElement());
        }
    }

    public static void addAllToVector(Vector theVector, List elementsToAdd) {
        theVector.addAll(elementsToAdd);
    }

    public static Vector addAllUniqueToVector(Vector theVector, Vector elementsToAdd) {
        for (Enumeration stream = elementsToAdd.elements(); stream.hasMoreElements();) {
            Object element = stream.nextElement();
            if (!theVector.contains(element)) {
                theVector.addElement(element);
            }
        }

        return theVector;
    }

    /**
    * Convert the specified vector into an array.
    */
    public static Object[] arrayFromVector(Vector vector) {
        Object[] result = new Object[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.elementAt(i);
        }
        return result;
    }

    /**
     * Convert the HEX string to a byte array.
     * HEX allows for binary data to be printed.
     */
    public static byte[] buildBytesFromHexString(String hex) {
        String tmpString = (String)hex;
        if ((tmpString.length() % 2) != 0) {
            throw ConversionException.couldNotConvertToByteArray(hex);
        }
        byte[] bytes = new byte[tmpString.length() / 2];
        int byteIndex;
        int strIndex;
        byte digit1;
        byte digit2;
        for (byteIndex = bytes.length - 1, strIndex = tmpString.length() - 2; byteIndex >= 0;
                 byteIndex--, strIndex -= 2) {
            digit1 = (byte)Character.digit(tmpString.charAt(strIndex), 16);
            digit2 = (byte)Character.digit(tmpString.charAt(strIndex + 1), 16);
            if ((digit1 == -1) || (digit2 == -1)) {
                throw ConversionException.couldNotBeConverted(hex, ClassConstants.APBYTE);
            }
            bytes[byteIndex] = (byte)((digit1 * 16) + digit2);
        }
        return bytes;
    }

    /**
     * Convert the passed Vector to a Hashtable
     * Return the Hashtable
     */
    public static Hashtable buildHashtableFromVector(Vector theVector) {
        Hashtable toReturn = new Hashtable(theVector.size());

        Iterator iter = theVector.iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            toReturn.put(next, next);
        }
        return toReturn;
    }

    /**
     * Convert the byte array to a HEX string.
     * HEX allows for binary data to be printed.
     */
    public static String buildHexStringFromBytes(byte[] bytes) {
        char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuffer stringBuffer = new StringBuffer();
        int tempByte;
        for (int byteIndex = 0; byteIndex < ((byte[])bytes).length; byteIndex++) {
            tempByte = ((byte[])bytes)[byteIndex];
            if (tempByte < 0) {
                tempByte = tempByte + 256;//compensate for the fact that byte is signed in Java
            }
            tempByte = (byte)(tempByte / 16);//get the first digit
            if (tempByte > 16) {
                throw ConversionException.couldNotBeConverted(bytes, ClassConstants.STRING);
            }
            stringBuffer.append(hexArray[tempByte]);

            tempByte = ((byte[])bytes)[byteIndex];
            if (tempByte < 0) {
                tempByte = tempByte + 256;
            }
            tempByte = (byte)(tempByte % 16);//get the second digit
            if (tempByte > 16) {
                throw ConversionException.couldNotBeConverted(bytes, ClassConstants.STRING);
            }
            stringBuffer.append(hexArray[tempByte]);
        }
        return stringBuffer.toString();
    }

    /**
      * Create a new Vector containing all of the hashtable elements
      *
      */
    public static Vector buildVectorFromHashtableElements(Hashtable hashtable) {
        Vector vector = new Vector(hashtable.size());
        Enumeration enumeration = hashtable.elements();

        while (enumeration.hasMoreElements()) {
            vector.addElement(enumeration.nextElement());
        }

        return vector;
    }

    /**
      * Create a new Vector containing all of the map elements.
      */
    public static Vector buildVectorFromMapElements(Map map) {
        Vector vector = new Vector(map.size());
        Iterator iterator = map.values().iterator();

        while (iterator.hasNext()) {
            vector.addElement(iterator.next());
        }

        return vector;
    }

    /**
      * Create a new Vector containing all of the hashtable elements
      *
      */
    public static Vector buildVectorFromHashtableElements(IdentityHashtable hashtable) {
        Vector vector = new Vector(hashtable.size());
        Enumeration enumeration = hashtable.elements();

        while (enumeration.hasMoreElements()) {
            vector.addElement(enumeration.nextElement());
        }

        return vector;
    }

    /**
     * Answer a Calendar from a date.
     */
    public static Calendar calendarFromUtilDate(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //In jdk1.3, millisecond is missing
        if (date instanceof Timestamp) {
            calendar.set(Calendar.MILLISECOND, ((Timestamp)date).getNanos() / 1000000);
        }
        return calendar;
    }

    /**
     * INTERNAL:
     * Return whether a Class implements a specific interface, either directly or indirectly
     * (through interface or implementation inheritance).
     * @return boolean
     */
    public static boolean classImplementsInterface(Class aClass, Class anInterface) {
        // quick check
        if (aClass == anInterface) {
            return true;
        }

        Class[] interfaces = aClass.getInterfaces();

        // loop through the "directly declared" interfaces
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] == anInterface) {
                return true;
            }
        }

        // recurse through the interfaces
        for (int i = 0; i < interfaces.length; i++) {
            if (classImplementsInterface(interfaces[i], anInterface)) {
                return true;
            }
        }

        // finally, recurse up through the superclasses to Object
        Class superClass = aClass.getSuperclass();
        if (superClass == null) {
            return false;
        }
        return classImplementsInterface(superClass, anInterface);
    }

    /**
     * INTERNAL:
     * Return whether a Class is a subclass of, or the same as, another Class.
     * @return boolean
     */
    public static boolean classIsSubclass(Class subClass, Class superClass) {
        Class temp = subClass;

        if (superClass == null) {
            return false;
        }

        while (temp != null) {
            if (temp == superClass) {
                return true;
            }
            temp = temp.getSuperclass();
        }
        return false;
    }

    public static boolean compareArrays(Object[] array1, Object[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int index = 0; index < array1.length; index++) {
            //Related to Bug#3128838 fix.  ! is added to correct the logic.
            if (!array1[index].equals(array2[index])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two BigDecimals.
     * This is required because the .equals method of java.math.BigDecimal ensures that
     * the scale of the two numbers are equal. Therefore 0.0 != 0.00.
     * @see java.math.BigDecimal#equals(Object)
     */
    public static boolean compareBigDecimals(java.math.BigDecimal one, java.math.BigDecimal two) {
        if (one.scale() != two.scale()) {
            double doubleOne = ((java.math.BigDecimal)one).doubleValue();
            double doubleTwo = ((java.math.BigDecimal)two).doubleValue();
            if ((doubleOne != Double.POSITIVE_INFINITY) && (doubleOne != Double.NEGATIVE_INFINITY) && (doubleTwo != Double.POSITIVE_INFINITY) && (doubleTwo != Double.NEGATIVE_INFINITY)) {
                return doubleOne == doubleTwo;
            }
        }
        return one.equals(two);
    }

    public static boolean compareByteArrays(byte[] array1, byte[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int index = 0; index < array1.length; index++) {
            if (array1[index] != array2[index]) {
                return false;
            }
        }
        return true;
    }

    public static boolean compareCharArrays(char[] array1, char[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int index = 0; index < array1.length; index++) {
            if (array1[index] != array2[index]) {
                return false;
            }
        }
        return true;
    }

    /**
    * PUBLIC:
    *
    * Compare two vectors of types. Return true if the size of the vectors is the
    * same and each of the types in the first Vector are assignable from the types
    * in the corresponding objects in the second Vector.
    */
    public static boolean areTypesAssignable(Vector types1, Vector types2) {
        if ((types1 == null) || (types2 == null)) {
            return false;
        }

        if (types1.size() == types2.size()) {
            for (int i = 0; i < types1.size(); i++) {
                Class type1 = (Class)types1.elementAt(i);
                Class type2 = (Class)types2.elementAt(i);

                // if either are null then we assume assignability.
                if ((type1 != null) && (type2 != null)) {
                    if (!type1.isAssignableFrom(type2)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
      * PUBLIC:
      * Compare the elements in 2 hashtables to see if they are equal
      *
      * Added Nov 9, 2000 JED Patch 2.5.1.8
      */
    public static boolean compareHashtables(Hashtable hashtable1, Hashtable hashtable2) {
        Enumeration enumtr;
        Object element;
        Hashtable clonedHashtable;

        if (hashtable1.size() != hashtable2.size()) {
            return false;
        }

        clonedHashtable = (Hashtable)hashtable2.clone();

        enumtr = hashtable1.elements();
        while (enumtr.hasMoreElements()) {
            element = enumtr.nextElement();
            if (clonedHashtable.remove(element) == null) {
                return false;
            }
        }

        return clonedHashtable.isEmpty();
    }

    /**
     * Compare the elements in two <code>Vector</code>s to see if they are equal.
     * The order of the elements is significant.
     * @return whether the two vectors are equal
     */
    public static boolean compareOrderedVectors(Vector vector1, Vector vector2) {
        if (vector1 == vector2) {
            return true;
        }
        if (vector1.size() != vector2.size()) {
            return false;
        }
        for (int index = 0; index < vector1.size(); index++) {
            Object element1 = vector1.elementAt(index);
            Object element2 = vector2.elementAt(index);
            if (element1 == null) {// avoid null pointer exception
                if (element2 != null) {
                    return false;
                }
            } else {
                if (!element1.equals(element2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare the elements in two <code>Vector</code>s to see if they are equal.
     * The order of the elements is ignored.
     * @param  v1  a vector
     * @param  v2  a vector
     * @return whether the two vectors contain the same elements
     */
    public static boolean compareUnorderedVectors(Vector v1, Vector v2) {
        if (v1 == v2) {
            return true;
        }
        if (v1.size() != v2.size()) {
            return false;
        }

        // One of the Vectors must be cloned so we don't miscompare
        // vectors with the same elements but in different quantities.
        // e.g. [fred, sam, sam] != [fred, sam, fred]
        Vector v3 = (Vector)v2.clone();
        for (int i = 0; i < v1.size(); i++) {
            Object e1 = v1.elementAt(i);
            if (e1 == null) {// avoid null pointer exception
                // Helper.removeNullElement() will return false if the element was not present to begin with
                if (!removeNullElement(v3)) {
                    return false;
                }
            } else {
                // Vector.removeElement() will return false if the element was not present to begin with
                if (!v3.removeElement(e1)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Hashtable concatenateHashtables(Hashtable first, Hashtable second) {
        Hashtable concatenation;
        Object key;
        Object value;

        concatenation = new Hashtable(first.size() + second.size() + 4);

        for (Enumeration keys = first.keys(); keys.hasMoreElements();) {
            key = keys.nextElement();
            value = first.get(key);
            concatenation.put(key, value);
        }

        for (Enumeration keys = second.keys(); keys.hasMoreElements();) {
            key = keys.nextElement();
            value = second.get(key);
            concatenation.put(key, value);
        }

        return concatenation;
    }
    
    /**
     * Merge the two Maps into a new HashMap.
     */
    public static Map concatenateMaps(Map first, Map second) {
        Map concatenation = new HashMap(first.size() + second.size() + 4);

        for (Iterator keys = first.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            Object value = first.get(key);
            concatenation.put(key, value);
        }

        for (Iterator keys = second.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            Object value = second.get(key);
            concatenation.put(key, value);
        }

        return concatenation;
    }

    /**
      * Return a new vector with no duplicated values
      *
      */
    public static Vector concatenateUniqueVectors(Vector first, Vector second) {
        Vector concatenation;
        Object element;

        concatenation = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();

        for (Enumeration stream = first.elements(); stream.hasMoreElements();) {
            concatenation.addElement(stream.nextElement());
        }

        for (Enumeration stream = second.elements(); stream.hasMoreElements();) {
            element = stream.nextElement();
            if (!concatenation.contains(element)) {
                concatenation.addElement(element);
            }
        }

        return concatenation;

    }

    public static Vector concatenateVectors(Vector first, Vector second) {
        Vector concatenation;

        concatenation = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();

        for (Enumeration stream = first.elements(); stream.hasMoreElements();) {
            concatenation.addElement(stream.nextElement());
        }

        for (Enumeration stream = second.elements(); stream.hasMoreElements();) {
            concatenation.addElement(stream.nextElement());
        }

        return concatenation;

    }

    /**
     * Returns whether the given <code>Vector</code> contains a <code>null</code> element
     * Return <code>true</code> if the Vector contains a null element
     * Return <code>false</code> otherwise.
     * This is needed in jdk1.1, where <code>Vector.contains(Object)</code>
     * for a <code>null</code> element will result in a <code>NullPointerException</code>....
     */
    public static boolean containsNull(Vector v, int index) {
        return indexOfNullElement(v, 0) != -1;
    }

    /** Return a copy of the vector containing a subset starting at startIndex
     *  and ending at stopIndex.
     *  @param vector - original vector
     *  @param startIndex - starting position in vector
     *  @param stopIndex - ending position in vector
     *  @exception TopLinkException
     */
    public static Vector copyVector(Vector originalVector, int startIndex, int stopIndex) throws ValidationException {
        Vector newVector;

        if (stopIndex < startIndex) {
            return oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        }

        if ((startIndex < 0) || (startIndex > originalVector.size())) {
            throw ValidationException.startIndexOutOfRange();
        }

        if ((stopIndex < 0) || (stopIndex > originalVector.size())) {
            throw ValidationException.stopIndexOutOfRange();
        }

        newVector = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();

        for (int index = startIndex; index < stopIndex; index++) {
            newVector.addElement(originalVector.elementAt(index));
        }

        return newVector;
    }

    /**
     * Return a string containing the platform-appropriate
     * characters for carriage return.
     */
    public static String cr() {
        // bug 2756643
        if (CR == null) {
            CR = System.getProperty("line.separator");
        }
        return CR;
    }

    /**
     * Return the name of the "current working directory".
     */
    public static String currentWorkingDirectory() {
        // bug 2756643
        if (CURRENT_WORKING_DIRECTORY == null) {
            CURRENT_WORKING_DIRECTORY = System.getProperty("user.dir");
        }
        return CURRENT_WORKING_DIRECTORY;
    }

    /**
     * Return the name of the "temporary directory".
     */
    public static String tempDirectory() {
        // Bug 2756643
        if (TEMP_DIRECTORY == null) {
            TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        }
        return TEMP_DIRECTORY;
    }

    /**
     * Answer a Date from a long
     *
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     * @param longObject - milliseconds from the epoch (00:00:00 GMT
     * Jan 1, 1970).  Negative values represent dates prior to the epoch.
     */
    public static java.sql.Date dateFromLong(Long longObject) {
        return new java.sql.Date(longObject.longValue());
    }
    
    /**
     * Answer a Date with the year, month, date.
     * This builds a date avoiding the deprecated, inefficient and concurrency bottleneck date constructors.
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     * The year, month, day are the values calendar uses,
     * i.e. year is from 0, month is 0-11, date is 1-31.
     */
    public static java.sql.Date dateFromYearMonthDate(int year, int month, int day) {
        // Use a calendar to compute the correct millis for the date.
        Calendar localCalendar = allocateCalendar();
        localCalendar.clear();
        localCalendar.set(year, month, day, 0, 0, 0);
        long millis = JavaPlatform.getTimeInMillis(localCalendar);
        java.sql.Date date = new java.sql.Date(millis);
        releaseCalendar(localCalendar);
        return date;
    }

    /**
     * Answer a Date from a string representation.
     * The string MUST be a valid date and in one of the following
     * formats: YYYY/MM/DD, YYYY-MM-DD, YY/MM/DD, YY-MM-DD.
     *
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     *
     * The Date class contains  some minor gotchas that you have to watch out for.
     * @param dateString - string representation of date
     * @return  - date representation of string
     */
    public static java.sql.Date dateFromString(String dateString) throws ConversionException {
        int year;
        int month;
        int day;
        StringTokenizer dateStringTokenizer;

        if (dateString.indexOf('/') != -1) {
            dateStringTokenizer = new StringTokenizer(dateString, "/");
        } else if (dateString.indexOf('-') != -1) {
            dateStringTokenizer = new StringTokenizer(dateString, "- ");
        } else {
            throw ConversionException.incorrectDateFormat(dateString);
        }

        try {
            year = Integer.parseInt(dateStringTokenizer.nextToken());
            month = Integer.parseInt(dateStringTokenizer.nextToken());
            day = Integer.parseInt(dateStringTokenizer.nextToken());
        } catch (NumberFormatException exception) {
            throw ConversionException.incorrectDateFormat(dateString);
        }

        // Java returns the month in terms of 0 - 11 instead of 1 - 12. 
        month = month - 1;

        return dateFromYearMonthDate(year, month, day);
    }

    /**
     * Answer a Date from a timestamp
     *
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     * @param timestampObject - timestamp representation of date
     * @return  - date representation of timestampObject
     */
    public static java.sql.Date dateFromTimestamp(java.sql.Timestamp timestamp) {
        return sqlDateFromUtilDate(timestamp);
    }

    /**
     * Returns true if the file of this name does indeed exist
     */
    public static boolean doesFileExist(String fileName) {
        try {
            new FileReader(fileName);
        } catch (FileNotFoundException fnfException) {
            return false;
        }

        return true;

    }

    /**
     * Double up \ to allow printing of directories for source code generation.
     */
    public static String doubleSlashes(String path) {
        StringBuffer buffer = new StringBuffer(path.length() + 5);
        for (int index = 0; index < path.length(); index++) {
            char charater = path.charAt(index);
            buffer.append(charater);
            if (charater == '\\') {
                buffer.append('\\');
            }
        }

        return buffer.toString();
    }

    /**
     * Extracts the actual path to the jar file.
     */
    public static String extractJarNameFromURL(java.net.URL url) {
        String tempName = url.getFile();
        int start = tempName.indexOf("file:") + 5;
        int end = tempName.indexOf("!/");
        return tempName.substring(start, end);
    }

    /**
     * Return a string containing the platform-appropriate
     * characters for separating directory and file names.
     */
    public static String fileSeparator() {
        //Bug 2756643
        if (FILE_SEPARATOR == null) {
            FILE_SEPARATOR = System.getProperty("file.separator");
        }
        return FILE_SEPARATOR;
    }

    /**
     * INTERNAL:
     * Returns a Field for the specified Class and field name.
     * Uses Class.getDeclaredField(String) to find the field.
     * If the field is not found on the specified class
     * the superclass is checked, and so on, recursively.
     * Set accessible to true, so we can access private/package/protected fields.
     */
    public static Field getField(Class javaClass, String fieldName) throws NoSuchFieldException {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return (Field)AccessController.doPrivileged(new PrivilegedGetField(javaClass, fieldName, true));
            } catch (PrivilegedActionException exception) {
                if (exception.getCause() instanceof NoSuchMethodException) {
                   throw (NoSuchFieldException)exception.getCause();
               }
               else {
                   // really shouldn't happen
                   throw (RuntimeException)exception.getCause();
               } 
            }
        } else {
            return PrivilegedAccessHelper.getField(javaClass, fieldName, true);
        }
    }

    /**
     * INTERNAL:
     * Returns a Method for the specified Class, method name,
     * and formal parameter types.
     * Uses Class.getDeclaredMethod(String Class[]) to find the method.
     * If the method is not found on the specified class
     * the superclass is checked, and so on, recursively.
     * Set accessible to true, so we can access private/package/protected methods.
     */
    public static Method getDeclaredMethod(Class javaClass, String methodName, Class[] methodParameterTypes) throws NoSuchMethodException {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return (Method)AccessController.doPrivileged(new PrivilegedGetMethod(javaClass, methodName, methodParameterTypes, true));
            } catch (PrivilegedActionException exception) {
                if (exception.getCause() instanceof NoSuchMethodException) {
                   throw (NoSuchMethodException)exception.getCause();
               }
               else {
                   // really shouldn't happen
                   throw (RuntimeException)exception.getCause();
               } 
            }
        } else {
            return PrivilegedAccessHelper.getMethod(javaClass, methodName, methodParameterTypes, true);
        }
    }

    /**
     * Return the class instance from the class
     */
    public static Object getInstanceFromClass(Class classFullName) {
        if (classFullName == null) {
            return null;
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(classFullName));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof InstantiationException) {
                        ValidationException exc = new ValidationException();
                        exc.setInternalException(throwableException);
                        throw exc;
                    } else {
                        ValidationException exc = new ValidationException();
                        exc.setInternalException(throwableException);
                        throw exc;
                    }
                }
            } else {
                return PrivilegedAccessHelper.newInstanceFromClass(classFullName);
            }
        } catch (InstantiationException notInstantiatedException) {
            ValidationException exception = new ValidationException();
            exception.setInternalException(notInstantiatedException);
            throw exception;
        } catch (IllegalAccessException notAccessedException) {
            ValidationException exception = new ValidationException();
            exception.setInternalException(notAccessedException);
            throw exception;
        }
    }

    /**
     * Used to store null values in hashtables, is helper because need to be serializable.
     */
    public static Object getNullWrapper() {
        return nullWrapper;
    }

    /**
     *    Returns the object class. If a class is primitive return its non primitive class
     */
    public static Class getObjectClass(Class javaClass) {
        return ConversionManager.getObjectClass(javaClass);
    }

    /**
     *    Answers the unqualified class name for the provided class.
     */
    public static String getShortClassName(Class javaClass) {
        return getShortClassName(javaClass.getName());
    }

    /**
     *    Answers the unqualified class name from the specified String.
     */
    public static String getShortClassName(String javaClassName) {
        return javaClassName.substring(javaClassName.lastIndexOf('.') + 1);
    }

    /**
     *    Answers the unqualified class name for the specified object.
     */
    public static String getShortClassName(Object object) {
        return getShortClassName(object.getClass());
    }

    /**
     *    return a package name for the specified class.
     */
    public static String getPackageName(Class javaClass) {
        String className = Helper.getShortClassName(javaClass);
        return javaClass.getName().substring(0, (javaClass.getName().length() - (className.length() + 1)));
    }

    /**
     * Return a string containing the specified number of tabs.
     */
    public static String getTabs(int noOfTabs) {
        StringWriter writer = new StringWriter();
        for (int index = 0; index < noOfTabs; index++) {
            writer.write("\t");
        }
        return writer.toString();
    }

    /**
     * Returns the index of the the first <code>null</code> element found in the specified
     * <code>Vector</code> starting the search at the starting index specified.
     * Return  an int >= 0 and less than size if a <code>null</code> element was found.
     * Return -1 if a <code>null</code> element was not found.
     * This is needed in jdk1.1, where <code>Vector.contains(Object)</code>
     * for a <code>null</code> element will result in a <code>NullPointerException</code>....
     */
    public static int indexOfNullElement(Vector v, int index) {
        for (int i = index; i < v.size(); i++) {
            if (v.elementAt(i) == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return true if the object implements the Collection interface
     * Creation date: (9/7/00 1:59:51 PM)
     * @return boolean
     * @param testObject java.lang.Object
     */
    public static boolean isCollection(Object testObject) {
        // Does it implement the Collection interface
        if (testObject instanceof Collection) {
            return true;
        }

        // It's not a collection
        return false;
    }

    /**
     * ADVANCED
     * returns true if the class in question is a primitive wrapper
     */
    public static boolean isPrimitiveWrapper(Class classInQuestion) {
        return classInQuestion.equals(Character.class) || classInQuestion.equals(Boolean.class) || classInQuestion.equals(Byte.class) || classInQuestion.equals(Short.class) || classInQuestion.equals(Integer.class) || classInQuestion.equals(Long.class) || classInQuestion.equals(Float.class) || classInQuestion.equals(Double.class);
    }

    /**
     * Returns true if the string given is an all upper case string
     */
    public static boolean isUpperCaseString(String s) {
        char[] c = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(c[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the character given is a vowel. I.e. one of a,e,i,o,u,A,E,I,O,U.
     */
    public static boolean isVowel(char c) {
        return (c == 'A') || (c == 'a') || (c == 'e') || (c == 'E') || (c == 'i') || (c == 'I') || (c == 'o') || (c == 'O') || (c == 'u') || (c == 'U');
    }

    /**
     * Return an array of the files in the specified directory.
     * This allows us to simplify jdk1.1 code a bit.
     */
    public static File[] listFilesIn(File directory) {
        if (directory.isDirectory()) {
            return directory.listFiles();
        } else {
            return new File[0];
        }
    }

    /**
     * Make a Vector from the passed object.
     * If it's a Collection, iterate over the collection and add each item to the Vector.
     * If it's not a collection create a Vector and add the object to it.
     */
    public static Vector makeVectorFromObject(Object theObject) {
        if (theObject instanceof Vector) {
            return ((Vector)theObject);
        }
        if (theObject instanceof Collection) {
            Vector returnVector = new Vector(((Collection)theObject).size());
            Iterator iterator = ((Collection)theObject).iterator();
            while (iterator.hasNext()) {
                returnVector.add(iterator.next());
            }
            return returnVector;
        }

        Vector returnVector = new Vector();
        returnVector.addElement(theObject);
        return returnVector;
    }

    /**
     * Return a string containing the platform-appropriate
     * characters for separating entries in a path (e.g. the classpath)
     */
    public static String pathSeparator() {
        // Bug 2756643
        if (PATH_SEPARATOR == null) {
            PATH_SEPARATOR = System.getProperty("path.separator");
        }
        return PATH_SEPARATOR;
    }

    /**
     * Return a String containing the printed stacktrace of an exception.
     */
    public static String printStackTraceToString(Throwable aThrowable) {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter, true);
        aThrowable.printStackTrace(writer);
        writer.close();
        return swriter.toString();
    }

    /* Return a string representation of a number of milliseconds in terms of seconds, minutes, or
     * milliseconds, whichever is most appropriate.
     */
    public static String printTimeFromMilliseconds(long milliseconds) {
        if ((milliseconds > 1000) && (milliseconds < 60000)) {
            return (milliseconds / 1000) + "s";
        }
        if (milliseconds > 60000) {
            return (milliseconds / 60000) + "min " + printTimeFromMilliseconds(milliseconds % 60000);
        }
        return milliseconds + "ms";
    }

    /**
     * Given a Vector, print it, even if there is a null in it
     */
    public static String printVector(Vector vector) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write("[");
        Enumeration enumtr = vector.elements();
        stringWriter.write(String.valueOf(enumtr.nextElement()));
        while (enumtr.hasMoreElements()) {
            stringWriter.write(" ");
            stringWriter.write(String.valueOf(enumtr.nextElement()));
        }
        stringWriter.write("]");
        return stringWriter.toString();

    }

    public static Hashtable rehashHashtable(Hashtable table) {
        Hashtable rehashedTable = new Hashtable(table.size() + 2);

        Enumeration values = table.elements();
        for (Enumeration keys = table.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = values.nextElement();
            rehashedTable.put(key, value);
        }

        return rehashedTable;
    }
    
    public static Map rehashMap(Map table) {
        HashMap rehashedTable = new HashMap(table.size() + 2);

        Iterator values = table.values().iterator();
        for (Iterator keys = table.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            Object value = values.next();
            rehashedTable.put(key, value);
        }

        return rehashedTable;
    }

    /**
     * Returns a String which has had enough non-alphanumeric characters removed to be equal to
     * the maximumStringLength.
     */
    public static String removeAllButAlphaNumericToFit(String s1, int maximumStringLength) {
        int s1Size = s1.length();
        if (s1Size <= maximumStringLength) {
            return s1;
        }

        // Remove the necessary number of characters	
        StringBuffer buf = new StringBuffer();
        int numberOfCharsToBeRemoved = s1.length() - maximumStringLength;
        int s1Index = 0;
        while ((numberOfCharsToBeRemoved > 0) && (s1Index < s1Size)) {
            char currentChar = s1.charAt(s1Index);
            if (Character.isLetterOrDigit(currentChar)) {
                buf.append(currentChar);
            } else {
                numberOfCharsToBeRemoved--;
            }
            s1Index++;
        }

        // Append the rest of the character that were not parsed through.
        // Is it quicker to build a substring and append that?
        while (s1Index < s1Size) {
            buf.append(s1.charAt(s1Index));
            s1Index++;
        }

        //
        return buf.toString();
    }

    /**
     * Returns a String which has had enough of the specified character removed to be equal to
     * the maximumStringLength.
     */
    public static String removeCharacterToFit(String s1, char aChar, int maximumStringLength) {
        int s1Size = s1.length();
        if (s1Size <= maximumStringLength) {
            return s1;
        }

        // Remove the necessary number of characters	
        StringBuffer buf = new StringBuffer();
        int numberOfCharsToBeRemoved = s1.length() - maximumStringLength;
        int s1Index = 0;
        while ((numberOfCharsToBeRemoved > 0) && (s1Index < s1Size)) {
            char currentChar = s1.charAt(s1Index);
            if (currentChar == aChar) {
                numberOfCharsToBeRemoved--;
            } else {
                buf.append(currentChar);
            }
            s1Index++;
        }

        // Append the rest of the character that were not parsed through.
        // Is it quicker to build a substring and append that?
        while (s1Index < s1Size) {
            buf.append(s1.charAt(s1Index));
            s1Index++;
        }

        //
        return buf.toString();
    }

    /**
     * Remove the first <code>null</code> element found in the specified <code>Vector</code>.
     * Return <code>true</code> if a <code>null</code> element was found and removed.
     * Return <code>false</code> if a <code>null</code> element was not found.
     * This is needed in jdk1.1, where <code>Vector.removeElement(Object)</code>
     * for a <code>null</code> element will result in a <code>NullPointerException</code>....
     */
    public static boolean removeNullElement(Vector v) {
        int indexOfNull = indexOfNullElement(v, 0);
        if (indexOfNull != -1) {
            v.removeElementAt(indexOfNull);
            return true;
        }
        return false;
    }

    /**
     * Returns a String which has had enough of the specified character removed to be equal to
     * the maximumStringLength.
     */
    public static String removeVowels(String s1) {
        // Remove the vowels
        StringBuffer buf = new StringBuffer();
        int s1Size = s1.length();
        int s1Index = 0;
        while (s1Index < s1Size) {
            char currentChar = s1.charAt(s1Index);
            if (!isVowel(currentChar)) {
                buf.append(currentChar);
            }
            s1Index++;
        }

        //
        return buf.toString();
    }

    /**
     * Replaces the first subString of the source with the replacement.
     */
    public static String replaceFirstSubString(String source, String subString, String replacement) {
        int index = source.indexOf(subString);

        if (index >= 0) {
            return source.substring(0, index) + replacement + source.substring(index + subString.length());
        }
        return null;
    }

    public static Vector reverseVector(Vector theVector) {
        Vector tempVector = new Vector(theVector.size());
        Object currentElement;

        for (int i = theVector.size() - 1; i > -1; i--) {
            currentElement = theVector.elementAt(i);
            tempVector.addElement(currentElement);
        }

        return tempVector;
    }

    /**
     * Returns a new string with all space characters removed from the right
     *
     * @param originalString - timestamp representation of date
     * @return  - String
     */
    public static String rightTrimString(String originalString) {
        int len = originalString.length();
        while ((len > 0) && (originalString.charAt(len - 1) <= ' ')) {
            len--;
        }
        return originalString.substring(0, len);
    }

    /**
     * Returns a String which is a concatenation of two string which have had enough
     * vowels removed from them so that the sum of the sized of the two strings is less than
     * or equal to the specified size.
     */
    public static String shortenStringsByRemovingVowelsToFit(String s1, String s2, int maximumStringLength) {
        int size = s1.length() + s2.length();
        if (size <= maximumStringLength) {
            return s1 + s2;
        }

        // Remove the necessary number of characters
        int s1Size = s1.length();
        int s2Size = s2.length();
        StringBuffer buf1 = new StringBuffer();
        StringBuffer buf2 = new StringBuffer();
        int numberOfCharsToBeRemoved = size - maximumStringLength;
        int s1Index = 0;
        int s2Index = 0;
        int modulo2 = 0;

        // While we still want to remove characters, and not both string are done.
        while ((numberOfCharsToBeRemoved > 0) && !((s1Index >= s1Size) && (s2Index >= s2Size))) {
            if ((modulo2 % 2) == 0) {
                // Remove from s1
                if (s1Index < s1Size) {
                    if (isVowel(s1.charAt(s1Index))) {
                        numberOfCharsToBeRemoved--;
                    } else {
                        buf1.append(s1.charAt(s1Index));
                    }
                    s1Index++;
                }
            } else {
                // Remove from s2
                if (s2Index < s2Size) {
                    if (isVowel(s2.charAt(s2Index))) {
                        numberOfCharsToBeRemoved--;
                    } else {
                        buf2.append(s2.charAt(s2Index));
                    }
                    s2Index++;
                }
            }
            modulo2++;
        }

        // Append the rest of the character that were not parsed through.
        // Is it quicker to build a substring and append that?
        while (s1Index < s1Size) {
            buf1.append(s1.charAt(s1Index));
            s1Index++;
        }
        while (s2Index < s2Size) {
            buf2.append(s2.charAt(s2Index));
            s2Index++;
        }

        //
        return buf1.toString() + buf2.toString();
    }

    /**
     * Answer a sql.Date from a timestamp.
     */
    public static java.sql.Date sqlDateFromUtilDate(java.util.Date utilDate) {
        // PERF: Avoid deprecated get methods, that are now very inefficient.
        Calendar calendar = allocateCalendar();
        calendar.setTime(utilDate);
        java.sql.Date date = dateFromCalendar(calendar);
        releaseCalendar(calendar);
        return date;
    }

    /**
     * Print the sql.Date.
     */
    public static String printDate(java.sql.Date date) {
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = allocateCalendar();
        calendar.setTime(date);
        String string = printDate(calendar);
        releaseCalendar(calendar);
        return string;
    }

    /**
     * Print the date part of the calendar.
     */
    public static String printDate(Calendar calendar) {
        return printDate(calendar, true);
    }

    /**
     * Print the date part of the calendar.
     * Normally the calendar must be printed in the local time, but if the timezone is printed,
     * it must be printing in its timezone.
     */
    public static String printDate(Calendar calendar, boolean useLocalTime) {
        int year;
        int month;
        int day;
        if (useLocalTime && (!defaultTimeZone.equals(calendar.getTimeZone()))) {
            // Must convert the calendar to the local timezone if different, as dates have no timezone (always local).
            Calendar localCalendar = allocateCalendar();
            JavaPlatform.setTimeInMillis(localCalendar, JavaPlatform.getTimeInMillis(calendar));
            year = localCalendar.get(Calendar.YEAR);
            month = localCalendar.get(Calendar.MONTH) + 1;
            day = localCalendar.get(Calendar.DATE);
            releaseCalendar(localCalendar);
        } else {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DATE);
        }

        char[] buf = "2000-00-00".toCharArray();
        buf[0] = Character.forDigit(year / 1000, 10);
        buf[1] = Character.forDigit((year / 100) % 10, 10);
        buf[2] = Character.forDigit((year / 10) % 10, 10);
        buf[3] = Character.forDigit(year % 10, 10);
        buf[5] = Character.forDigit(month / 10, 10);
        buf[6] = Character.forDigit(month % 10, 10);
        buf[8] = Character.forDigit(day / 10, 10);
        buf[9] = Character.forDigit(day % 10, 10);

        return new String(buf);
    }

    /**
     * Print the sql.Time.
     */
    public static String printTime(java.sql.Time time) {
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = allocateCalendar();
        calendar.setTime(time);
        String string = printTime(calendar);
        releaseCalendar(calendar);
        return string;
    }

    /**
     * Print the time part of the calendar.
     */
    public static String printTime(Calendar calendar) {
        return printTime(calendar, true);
    }

    /**
     * Print the time part of the calendar.
     * Normally the calendar must be printed in the local time, but if the timezone is printed,
     * it must be printing in its timezone.
     */
    public static String printTime(Calendar calendar, boolean useLocalTime) {
        int hour;
        int minute;
        int second;
        if (useLocalTime && (!defaultTimeZone.equals(calendar.getTimeZone()))) {
            // Must convert the calendar to the local timezone if different, as dates have no timezone (always local).
            Calendar localCalendar = allocateCalendar();
            JavaPlatform.setTimeInMillis(localCalendar, JavaPlatform.getTimeInMillis(calendar));
            hour = localCalendar.get(Calendar.HOUR_OF_DAY);
            minute = localCalendar.get(Calendar.MINUTE);
            second = localCalendar.get(Calendar.SECOND);
            releaseCalendar(localCalendar);
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
        }
        String hourString;
        String minuteString;
        String secondString;
        if (hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = Integer.toString(hour);
        }
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = Integer.toString(minute);
        }
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = Integer.toString(second);
        }
        return (hourString + ":" + minuteString + ":" + secondString);
    }

    /**
     * Print the Calendar.
     */
    public static String printCalendar(Calendar calendar) {
        return printCalendar(calendar, true);
    }

    /**
     * Print the Calendar.
     * Normally the calendar must be printed in the local time, but if the timezone is printed,
     * it must be printing in its timezone.
     */
    public static String printCalendar(Calendar calendar, boolean useLocalTime) {
        String millisString;

        //	String zeros = "000000000";
        if (calendar.get(Calendar.MILLISECOND) == 0) {
            millisString = "0";
        } else {
            millisString = buildZeroPrefixAndTruncTrailZeros(calendar.get(Calendar.MILLISECOND), 3);
        }

        StringBuffer timestampBuf = new StringBuffer();
        timestampBuf.append(printDate(calendar, useLocalTime));
        timestampBuf.append(" ");
        timestampBuf.append(printTime(calendar, useLocalTime));
        timestampBuf.append(".");
        timestampBuf.append(millisString);

        return timestampBuf.toString();
    }

    /**
     * Print the sql.Timestamp.
     */
    public static String printTimestamp(java.sql.Timestamp timestamp) {
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = allocateCalendar();
        calendar.setTime(timestamp);

        String nanosString;

        //	String zeros = "000000000";
        String yearZeros = "0000";

        if (timestamp.getNanos() == 0) {
            nanosString = "0";
        } else {
            nanosString = buildZeroPrefixAndTruncTrailZeros(timestamp.getNanos(), 9);
        }

        StringBuffer timestampBuf = new StringBuffer();
        timestampBuf.append(printDate(calendar));
        timestampBuf.append(" ");
        timestampBuf.append(printTime(calendar));
        timestampBuf.append(".");
        timestampBuf.append(nanosString);

        releaseCalendar(calendar);

        return (timestampBuf.toString());
    }

    /**
     * Build a numerical string with leading 0s.  number is an existing number that 
     * the new string will be built on.  totalDigits is the number of the required 
     * digits of the string.
     */
    public static String buildZeroPrefix(int number, int totalDigits) {
        String zeros = "000000000";
        int absValue = (number < 0) ? (-number) : number;
        String numbString = Integer.toString(absValue);

        // Add leading zeros
        numbString = zeros.substring(0, (totalDigits - numbString.length())) + numbString;

        if (number < 0) {
            numbString = "-" + numbString;
        } else {
            numbString = "+" + numbString;
        }            
        return numbString;
    }
 
    /**
     * Build a numerical string with leading 0s and truncate trailing zeros.  number is
     * an existing number that the new string will be built on.  totalDigits is the number
     * of the required digits of the string.
     */
    public static String buildZeroPrefixAndTruncTrailZeros(int number, int totalDigits) {
        String zeros = "000000000";
        String numbString = Integer.toString(number);

        // Add leading zeros
        numbString = zeros.substring(0, (totalDigits - numbString.length())) + numbString;
        // Truncate trailing zeros
        char[] numbChar = new char[numbString.length()];
        numbString.getChars(0, numbString.length(), numbChar, 0);
        int truncIndex = totalDigits - 1;
        while (numbChar[truncIndex] == '0') {
            truncIndex--;
        }
        return new String(numbChar, 0, truncIndex + 1);
    }

    /**
     * Print the sql.Timestamp without the nanos portion.
     */
    public static String printTimestampWithoutNanos(java.sql.Timestamp timestamp) {
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = allocateCalendar();
        calendar.setTime(timestamp);
        String string = printCalendarWithoutNanos(calendar);
        releaseCalendar(calendar);
        return string;
    }

    /**
     * Print the Calendar without the nanos portion.
     */
    public static String printCalendarWithoutNanos(Calendar calendar) {
        StringBuffer timestampBuf = new StringBuffer();
        timestampBuf.append(printDate(calendar));
        timestampBuf.append(" ");
        timestampBuf.append(printTime(calendar));
        return timestampBuf.toString();
    }

    /**
     * Answer a sql.Date from a Calendar.
     */
    public static java.sql.Date dateFromCalendar(Calendar calendar) {
        if (!defaultTimeZone.equals(calendar.getTimeZone())) {
            // Must convert the calendar to the local timezone if different, as dates have no timezone (always local).
            Calendar localCalendar = allocateCalendar();
            JavaPlatform.setTimeInMillis(localCalendar, JavaPlatform.getTimeInMillis(calendar));
            java.sql.Date date = dateFromYearMonthDate(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DATE));
            releaseCalendar(localCalendar);
            return date;
        }
        return dateFromYearMonthDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    /**
     * Can be used to mark code if a workaround is added for a JDBC driver or other bug.
     */
    public static void systemBug(String description) {
        // Use sender to find what is needy.
    }

    /**
     * Answer a Time from a Date
     *
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     * @param timestampObject - time representation of date
     * @return  - time representation of dateObject
     */
    public static java.sql.Time timeFromDate(java.util.Date date) {
        // PERF: Avoid deprecated get methods, that are now very inefficient.
        Calendar calendar = allocateCalendar();
        calendar.setTime(date);
        java.sql.Time time = timeFromCalendar(calendar);
        releaseCalendar(calendar);
        return time;
    }

    /**
     * Answer a Time from a long
     *
     * @param longObject - milliseconds from the epoch (00:00:00 GMT
     * Jan 1, 1970).  Negative values represent dates prior to the epoch.
     */
    public static java.sql.Time timeFromLong(Long longObject) {
        return new java.sql.Time(longObject.longValue());
    }
        
    /**
     * Answer a Time with the hour, minute, second.
     * This builds a time avoiding the deprecated, inefficient and concurrency bottleneck date constructors.
     * The hour, minute, second are the values calendar uses,
     * i.e. year is from 0, month is 0-11, date is 1-31.
     */
    public static java.sql.Time timeFromHourMinuteSecond(int hour, int minute, int second) {
        // Use a calendar to compute the correct millis for the date.
        Calendar localCalendar = allocateCalendar();
        localCalendar.clear();
        localCalendar.set(1970, 0, 1, hour, minute, second);
        long millis = JavaPlatform.getTimeInMillis(localCalendar);
        java.sql.Time time = new java.sql.Time(millis);
        releaseCalendar(localCalendar);
        return time;
    }

    /**
     * Answer a Time from a string representation.
     * This method will accept times in the following
     * formats: HH-MM-SS, HH:MM:SS
     *
     * @param timeString - string representation of time
     * @return  - time representation of string
     */
    public static java.sql.Time timeFromString(String timeString) throws ConversionException {
        int hour;
        int minute;
        int second;
        String timePortion = timeString;

        if (timeString.length() > 12) {
            // Longer strings are Timestamp format (ie. Sybase & Oracle)
            timePortion = timeString.substring(11, 19);
        }

        if ((timePortion.indexOf('-') == -1) && (timePortion.indexOf('/') == -1) && (timePortion.indexOf('.') == -1) && (timePortion.indexOf(':') == -1)) {
            throw ConversionException.incorrectTimeFormat(timePortion);
        }
        StringTokenizer timeStringTokenizer = new StringTokenizer(timePortion, " /:.-");

        try {
            hour = Integer.parseInt(timeStringTokenizer.nextToken());
            minute = Integer.parseInt(timeStringTokenizer.nextToken());
            second = Integer.parseInt(timeStringTokenizer.nextToken());
        } catch (NumberFormatException exception) {
            throw ConversionException.incorrectTimeFormat(timeString);
        }

        return timeFromHourMinuteSecond(hour, minute, second);
    }

    /**
     * Answer a Time from a Timestamp
     * Usus the Hours, Minutes, Seconds instead of getTime() ms value.
     */
    public static java.sql.Time timeFromTimestamp(java.sql.Timestamp timestamp) {
        return timeFromDate(timestamp);
    }

    /**
     * Answer a sql.Time from a Calendar.
     */
    public static java.sql.Time timeFromCalendar(Calendar calendar) {
        if (!defaultTimeZone.equals(calendar.getTimeZone())) {
            // Must convert the calendar to the local timezone if different, as dates have no timezone (always local).
            Calendar localCalendar = allocateCalendar();
            JavaPlatform.setTimeInMillis(localCalendar, JavaPlatform.getTimeInMillis(calendar));
            java.sql.Time date = timeFromHourMinuteSecond(localCalendar.get(Calendar.HOUR_OF_DAY), localCalendar.get(Calendar.MINUTE), localCalendar.get(Calendar.SECOND));
            releaseCalendar(localCalendar);
            return date;
        }
        return timeFromHourMinuteSecond(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    /**
     * Answer a Timestamp from a Calendar.
     */
    public static java.sql.Timestamp timestampFromCalendar(Calendar calendar) {
        return timestampFromLong(JavaPlatform.getTimeInMillis(calendar));
    }

    /**
     * Answer a Timestamp from a java.util.Date.
     */
    public static java.sql.Timestamp timestampFromDate(java.util.Date date) {
        return timestampFromLong(date.getTime());
    }

    /**
     * Answer a Time from a long
     *
     * @param longObject - milliseconds from the epoch (00:00:00 GMT
     * Jan 1, 1970).  Negative values represent dates prior to the epoch.
     */
    public static java.sql.Timestamp timestampFromLong(Long millis) {
        return timestampFromLong(millis.longValue());
    }

    /**
     * Answer a Time from a long
     *
     * @param longObject - milliseconds from the epoch (00:00:00 GMT
     * Jan 1, 1970).  Negative values represent dates prior to the epoch.
     */
    public static java.sql.Timestamp timestampFromLong(long millis) {
        java.sql.Timestamp timestamp = new java.sql.Timestamp(millis);

        // P2.0.1.3: Didn't account for negative millis < 1970
        // Must account for the jdk millis bug where it does not set the nanos.
        if ((millis % 1000) > 0) {
            timestamp.setNanos((int)(millis % 1000) * 1000000);
        } else if ((millis % 1000) < 0) {
            timestamp.setNanos((int)(1000000000 - (Math.abs((millis % 1000) * 1000000))));
        }
        return timestamp;
    }

    /**
     * Answer a Timestamp from a string representation.
     * This method will accept strings in the following
     * formats: YYYY/MM/DD HH:MM:SS, YY/MM/DD HH:MM:SS, YYYY-MM-DD HH:MM:SS, YY-MM-DD HH:MM:SS
     *
     * @param timestampString - string representation of timestamp
     * @return  - timestamp representation of string
     */
    public static java.sql.Timestamp timestampFromString(String timestampString) throws ConversionException {
        if ((timestampString.indexOf('-') == -1) && (timestampString.indexOf('/') == -1) && (timestampString.indexOf('.') == -1) && (timestampString.indexOf(':') == -1)) {
            throw ConversionException.incorrectTimestampFormat(timestampString);
        }
        StringTokenizer timestampStringTokenizer = new StringTokenizer(timestampString, " /:.-");

        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int nanos;
        try {
            year = Integer.parseInt(timestampStringTokenizer.nextToken());
            month = Integer.parseInt(timestampStringTokenizer.nextToken());
            day = Integer.parseInt(timestampStringTokenizer.nextToken());
            try {
                hour = Integer.parseInt(timestampStringTokenizer.nextToken());
                minute = Integer.parseInt(timestampStringTokenizer.nextToken());
                second = Integer.parseInt(timestampStringTokenizer.nextToken());
            } catch (java.util.NoSuchElementException endOfStringException) {
                // May be only a date string desired to be used as a timestamp.
                hour = 0;
                minute = 0;
                second = 0;
            }
        } catch (NumberFormatException exception) {
            throw ConversionException.incorrectTimestampFormat(timestampString);
        }

        try {
            String nanoToken = timestampStringTokenizer.nextToken();
            nanos = Integer.parseInt(nanoToken);
            for (int times = 0; times < (9 - nanoToken.length()); times++) {
                nanos = nanos * 10;
            }
        } catch (java.util.NoSuchElementException endOfStringException) {
            nanos = 0;
        } catch (NumberFormatException exception) {
            throw ConversionException.incorrectTimestampFormat(timestampString);
        }

        // Java dates are based on year after 1900 so I need to delete it.
        year = year - 1900;

        // Java returns the month in terms of 0 - 11 instead of 1 - 12. 
        month = month - 1;

        java.sql.Timestamp timestamp;
        // This was not converted to use Calendar for the conversion because calendars do not take nanos.
        // but it should be, and then just call setNanos.
        timestamp = new java.sql.Timestamp(year, month, day, hour, minute, second, nanos);
        return timestamp;
    }
    
    /**
     * Answer a Timestamp with the year, month, day, hour, minute, second.
     * The hour, minute, second are the values calendar uses,
     * i.e. year is from 0, month is 0-11, date is 1-31, time is 0-23/59.
     */
    public static java.sql.Timestamp timestampFromYearMonthDateHourMinuteSecondNanos(int year, int month, int date, int hour, int minute, int second, int nanos) {
        // This was not converted to use Calendar for the conversion because calendars do not take nanos.
        // but it should be, and then just call setNanos.
        return new java.sql.Timestamp(year - 1900, month, date, hour, minute, second, nanos);
    }

    /**
     * Can be used to mark code as need if something strange is seen.
     */
    public static void toDo(String description) {
        // Use sender to find what is needy.
    }

    /**
     * If the size of the original string is larger than the passed in size,
     * this method will remove the vowels from the original string.
     *
     * The removal starts backward from the end of original string, and stops if the
     * resulting string size is equal to the passed in size.
     *
     * If the resulting string is still larger than the passed in size after
     * removing all vowels, the end of the resulting string will be truncated.
     */
    public static String truncate(String originalString, int size) {
        if (originalString.length() <= size) {
            //no removal and truncation needed
            return originalString;
        }
        String vowels = "AaEeIiOoUu";
        StringBuffer newStringBufferTmp = new StringBuffer(originalString.length());

        //need to remove the extra characters
        int counter = originalString.length() - size;
        for (int index = (originalString.length() - 1); index >= 0; index--) {
            //search from the back to the front, if vowel found, do not append it to the resulting (temp) string! 
            //i.e. if vowel not found, append the chararcter to the new string buffer.
            if (vowels.indexOf(originalString.charAt(index)) == -1) {
                newStringBufferTmp.append(originalString.charAt(index));
            } else {
                //vowel found! do NOT append it to the temp buffer, and decrease the counter
                counter--;
                if (counter == 0) {
                    //if the exceeded characters (counter) of vowel haven been removed, the total 
                    //string size should be equal to the limits, so append the reversed remaining string
                    //to the new string, break the loop and return the shrunk string.
                    StringBuffer newStringBuffer = new StringBuffer(size);
                    newStringBuffer.append(originalString.substring(0, index));
                    //need to reverse the string
                    //bug fix: 3016423. append(BunfferString) is jdk1.4 version api. Use append(String) instead
                    //in order to support jdk1.3.
                    newStringBuffer.append(newStringBufferTmp.reverse().toString());
                    return newStringBuffer.toString();
                }
            }
        }

        //the shrunk string still too long, revrese the order back and truncate it!
        return newStringBufferTmp.reverse().toString().substring(0, size);
    }

    /**
     * Answer a Date from a long
     *
     * This implementation is based on the java.sql.Date class, not java.util.Date.
     * @param longObject - milliseconds from the epoch (00:00:00 GMT
     * Jan 1, 1970).  Negative values represent dates prior to the epoch.
     */
    public static java.util.Date utilDateFromLong(Long longObject) {
        return new java.util.Date(longObject.longValue());
    }

    /**
     * Answer a java.util.Date from a sql.date
     *
     * @param sqlDate - sql.date representation of date
     * @return  - java.util.Date representation of the sql.date
     */
    public static java.util.Date utilDateFromSQLDate(java.sql.Date sqlDate) {
        return new java.util.Date(sqlDate.getTime());
    }

    /**
     * Answer a java.util.Date from a sql.Time
     *
     * @param time - time representation of util date
     * @return  - java.util.Date representation of the time
     */
    public static java.util.Date utilDateFromTime(java.sql.Time time) {
        return new java.util.Date(time.getTime());
    }

    /**
     * Answer a java.util.Date from a timestamp
     *
     * @param timestampObject - timestamp representation of date
     * @return  - java.util.Date representation of timestampObject
     */
    public static java.util.Date utilDateFromTimestamp(java.sql.Timestamp timestampObject) {
        // Bug 2719624 - Conditionally remove workaround for java bug which truncated
        // nanoseconds from timestamp.getTime().  We will now only recalculate the nanoseconds
        // When timestamp.getTime() results in nanoseconds == 0;
        long time = timestampObject.getTime();
        boolean appendNanos = ((time % 1000) == 0);
        if (appendNanos) {
            return new java.util.Date(time + (timestampObject.getNanos() / 1000000));
        } else {
            return new java.util.Date(time);
        }
    }

    /**
    * Convert the specified array into a vector.
    */
    public static Vector vectorFromArray(Object[] array) {
        Vector result = new Vector(array.length);
        for (int i = 0; i < array.length; i++) {
            result.addElement(array[i]);
        }
        return result;
    }

    /**
     * Convert the byte array to a HEX string.
     * HEX allows for binary data to be printed.
     */
    public static void writeHexString(byte[] bytes, Writer writer) throws IOException {
        writer.write(buildHexStringFromBytes(bytes));
    }
}
