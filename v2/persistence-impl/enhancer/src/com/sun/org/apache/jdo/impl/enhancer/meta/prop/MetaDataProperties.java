/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.meta.prop;

import java.lang.reflect.Modifier;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import java.text.MessageFormat;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;



/**
 * This class parses the properties containing meta data information
 * about classes. The syntax of the properties is the following:
 * <ul>
 *  <li> the keys in the properties file are fully qualified classnames or
 *       fully qualified fieldnames </li>
 *  <li> a fields is separated by a classname with a hash mark ('#')
 *       (e.g. "test.Test#test1") </li>
 *  <li> all classnames are given in a natural form (e.g.
 *       "java.lang.Integer", "java.lang.Integer[][]", "int",
 *       "test.Test$Test1") </li>
 *  <li> property keys are classnames and fieldnames
 *       (e.g. "test.Test=...", "test.Test#field1=...") <br> </li>
 *  <li> Classnames can have the following attributes:
 *    <ul>
 *      <li> jdo:{persistent|transactional} </li>
 *      <li> super: &#60;classname&#62; </li>
 *      <li> oid: &#60;classname&#62; </li>
 *      <li> access: {public|protected|package|private} </li>
 *    </ul> </li>
 *  <li> Fieldnames can have the following attributes:
 *    <ul>
 *      <li> type:&#60;type&#62; </li>
 *      <li> access: {public|protected|package|private} </li>
 *      <li> jdo:{persistent|transactional|transient|property} </li>
 *      <li> annotation:{key|dfg|mediated} </li>
 *    </ul> </li>
 *  <li> the names of the attributes can be ommitted: you can say <br>
 *       test.Test1#field1=jdo:persistent,type:java.lang.String,key,... <br>
 *       or <br>
 *       test.Test1#field1=persistent,java.lang.String,key,... <br>
 *       or <br>
 *       test.Test1#field1=jdo:persistent,java.lang.String,key,... <br> </li>
 *  <li> in order to find fields of a class, a line for the class has to be
 *       specified in the properties: To find the field
 *       <code>test.Test1#field</code>, the keys <code>test.Test1</code> and
 *       <code>test.Test1#Field</code> have to be present. </li>
 * </ul>
 * This class is not thread safe.
 */
final class MetaDataProperties
{
    /**
     * The delimiter of a property key between the class- and fieldname.
     */
    static final char FIELD_DELIMITER = '#';

    /**
     * A string of delimiter characters between attributes.
     */
    static final String PROPERTY_DELIMITERS = " \t,;";

    /**
     * A delimiter character between attribute name and attribute value
     */
    static final char PROPERTY_ASSIGNER = ':';

    // attribute names for classes and fields
    static final String PROPERTY_ACCESS_MODIFIER = "access";
    static final String PROPERTY_JDO_MODIFIER    = "jdo";
    static final String PROPERTY_SUPER_CLASSNAME = "super";
    static final String PROPERTY_OID_CLASSNAME   = "oid";
    static final String PROPERTY_TYPE            = "type";
    static final String PROPERTY_ANNOTATION      = "annotation";

    // values of the access attribute of classes and fields.
    static final String ACCESS_PRIVATE       = "private";
    static final String ACCESS_PACKAGE_LOCAL = "package";
    static final String ACCESS_PROTECTED     = "protected";
    static final String ACCESS_PUBLIC        = "public";

    // values of the jdo attribute of classes and fields.
    // extended for property-based persistent field
    static final String JDO_TRANSIENT     = "transient";
    static final String JDO_PERSISTENT    = "persistent";
    static final String JDO_TRANSACTIONAL = "transactional";
    static final String JDO_PROPERTY      = "property";

    // values of the annotation type attribute of fields.
    static final String ANNOTATION_KEY      = "key";
    static final String ANNOTATION_DFG      = "dfg";
    static final String ANNOTATION_MEDIATED = "mediated";

    /**
     * The properties to parse.
     */
    private Properties properties;

    /**
     * A map of already read class properties. The keys are the
     * classnames, the values are the appropriate
     * <code>JDOClass</code>-object.
     */
    private final Map cachedJDOClasses = new HashMap();

    /**
     * A constant for the cache indicating that a given classname
     * if not specified in the properties.
     */
    static private final JDOClass NULL = new JDOClass(null);

    /**
     * A temporary vector (this is the reason why the implementation is not
     * thread safe).
     */
    private final List tmpTokens = new ArrayList();

    /**
     * Creates a new object with the given properties.
     *
     * @param  props  The properties.
     */
    public MetaDataProperties(Properties props)
    {
        this.properties = props;
    }

    /**
     * Get the information about the class with the given name.
     *
     * @param  classname  The classname.
     * @return  The information about the class or <code>null</code> if no
     *          information is given.
     * @throws  EnhancerMetaDataUserException  If something went wrong parsing
     *                                    the properties.
     */
    public final JDOClass getJDOClass(String classname)
        throws EnhancerMetaDataUserException
    {
        classname = NameHelper.toCanonicalClassName(classname);
        JDOClass clazz = (JDOClass)cachedJDOClasses.get(classname);
        if (clazz == NULL) { //already searched but not found
            return null;
        }
        if (clazz != null) {
            return clazz;
        }

        //load it from the properties file
        String s = properties.getProperty(classname);
        if (s == null) { //class not defined
            cachedJDOClasses.put(classname, NULL);
            return null;
        }

        //the class could be found in the properties
        clazz = parseJDOClass(classname, s);  //parse the class attributes
        parseJDOFields(clazz);  //parse all fields
        validateDependencies(clazz);  //check dependencies
        cachedJDOClasses.put(clazz.getName(), clazz);

        return clazz;
    }

    /**
     * Gets the information about the specified field.
     *
     * @param  classname  The name of the class.
     * @param  fieldname  The name of the field of the class.
     * @return  The information about the field or <code>null</code> if
     *          no information could be found.
     * @throws  EnhancerMetaDataUserException  If something went wrong parsing
     *                                    the properties.
     */
    public final JDOField getJDOField(String fieldname,
                                      String classname)
        throws EnhancerMetaDataUserException
    {
        JDOClass clazz = getJDOClass(classname);
        return (clazz != null ? clazz.getField(fieldname) : null);
    }

    /**
     * Gets all classnames in the properties.
     *
     * @return  All classnames in the properties.
     */
    public final String[] getKnownClassNames()
    {
        Collection classnames = new HashSet();
        for (Enumeration names = properties.propertyNames();
             names.hasMoreElements();) {
            String name = (String)names.nextElement();
            if (name.indexOf(FIELD_DELIMITER) < 0) {
                classnames.add(NameHelper.fromCanonicalClassName(name));
            }
        }

        return (String[])classnames.toArray(new String[classnames.size()]);
    }

    /**
     * Parses the attributes-string of a class and puts them into a
     * <code>JDOClass</code>-object.
     *
     * @param  classname  The name of the class.
     * @param  attributes  The attribute-string as specified in the properties.
     * @return  The create <code>JDOClass</code>-object.
     * @throws  EnhancerMetaDataUserException  If something went wrong parsing
     *                                    the attributes.
     */
    private final JDOClass parseJDOClass(String classname,
                                         String attributes)
        throws EnhancerMetaDataUserException
    {
        List props = parseProperties(attributes);

        // check each property
        for (int i = 0; i < props.size(); i++) {
            final Property prop = (Property)props.get(i);
            validateClassProperty(prop, classname);
        }

        // check dependencies of all properties
        checkForDuplicateProperties(props, classname);

        // properties are OK - assign them to the JDOClass object
        JDOClass clazz = new JDOClass(classname);
        for (int i = 0; i < props.size(); i++) {
            Property prop = (Property)props.get(i);
            if (prop.name.equals(PROPERTY_ACCESS_MODIFIER)) {
                clazz.setAccessModifiers(getAccessModifiers(prop.value));
            } else if (prop.name.equals(PROPERTY_JDO_MODIFIER)) {
                clazz.setPersistent(prop.value.equals(JDO_PERSISTENT));
            } else if (prop.name.equals(PROPERTY_SUPER_CLASSNAME)) {
                clazz.setSuperClassName(prop.value);
            } else if (prop.name.equals(PROPERTY_OID_CLASSNAME)) {
                clazz.setOidClassName(prop.value);
            }
        }

        return clazz;
    }

    /**
     * Checks if the given attribute-property of a class is valid.
     *
     * @param  prop       The attribute-property.
     * @param  classname  The classname.
     * @throws  EnhancerMetaDataUserException  If the validation failed.
     */
    static private void validateClassProperty(Property prop,
                                              String classname)
        throws EnhancerMetaDataUserException
    {
        String value = prop.value;
        if (prop.name == null) {
            // try to guess the property name
            if (value.equals(ACCESS_PUBLIC)
                || value.equals(ACCESS_PROTECTED)
                || value.equals(ACCESS_PACKAGE_LOCAL)
                || value.equals(ACCESS_PRIVATE)) {
                // assume access modifier
                prop.name = PROPERTY_ACCESS_MODIFIER;
            } else if (value.equals(JDO_PERSISTENT)
                       || value.equals(JDO_TRANSIENT)) {
                // assume persistence modifier
                prop.name = PROPERTY_JDO_MODIFIER;
            }
            //@olsen: not unique anymore, could also be oid class name
            // else {
            //     //assume the the given value is the superclassname
            //     prop.name = PROPERTY_SUPER_CLASSNAME;
            // }
        } else {
            // do we have a valid property name?
            String name = prop.name;
            checkPropertyName(prop.name,
                              new String[]{
                                  PROPERTY_ACCESS_MODIFIER,
                                  PROPERTY_JDO_MODIFIER,
                                  PROPERTY_SUPER_CLASSNAME,
                                  PROPERTY_OID_CLASSNAME
                              },
                              classname);

            // do we have a valid property value?
            checkPropertyValue(prop,
                               new String[]{
                                   ACCESS_PUBLIC,
                                   ACCESS_PROTECTED,
                                   ACCESS_PACKAGE_LOCAL,
                                   ACCESS_PRIVATE
                               },
                               PROPERTY_ACCESS_MODIFIER,
                               classname);
            checkPropertyValue(prop,
                               new String[]{
                                   JDO_TRANSIENT,
                                   JDO_PERSISTENT
                               },
                               PROPERTY_JDO_MODIFIER,
                               classname);
        }
    }

    /**
     * Parses all fields of a given class.
     *
     * @param  clazz  the representation of the class
     * @throws EnhancerMetaDataUserException on parse errors
     */
    private final void parseJDOFields(JDOClass clazz)
        throws EnhancerMetaDataUserException
    {
        //search for fields of the class
        for (Enumeration names = properties.propertyNames();
             names.hasMoreElements();) {
            String name = (String)names.nextElement();
            if (name.startsWith(clazz.getName() + FIELD_DELIMITER)) {
                //field found
                String fieldname
                    = name.substring(name.indexOf(FIELD_DELIMITER) + 1,
                                     name.length());
                validateFieldName(fieldname, clazz.getName());
                clazz.addField(parseJDOField(properties.getProperty(name),
                                             fieldname, clazz));
            }
        }
        clazz.sortFields();
    }

    /**
     * Parses the attribute-string of a field.
     *
     * @param  attributes  The attribute-string.
     * @param  fieldname   The fieldname.
     * @param  clazz       The class to field belongs to.
     * @throws  EnhancerMetaDataUserException  on parse errors
     */
    private final JDOField parseJDOField(String attributes,
                                         String fieldname,
                                         JDOClass clazz)
        throws EnhancerMetaDataUserException
    {
        List props = parseProperties(attributes);

        //check each property
        for (int i = 0; i < props.size(); i++) {
            Property prop = (Property)props.get(i);
            validateFieldProperty(prop, fieldname, clazz.getName());
        }

        //check dependencies of all properties
        checkForDuplicateProperties(props,
                                    clazz.getName() + FIELD_DELIMITER
                                    + fieldname);

        //properties are OK - assign them to the JDOField object
        JDOField field = new JDOField(fieldname);
        for (int i = 0; i < props.size(); i++) {
            Property prop = (Property)props.get(i);
            if (prop.name.equals(PROPERTY_ACCESS_MODIFIER)) {
                field.setAccessModifiers(getAccessModifiers(prop.value));
            } else if (prop.name.equals(PROPERTY_JDO_MODIFIER)) {
                field.setJdoModifier(prop.value);
            } else if (prop.name.equals(PROPERTY_TYPE)) {
                field.setType(prop.value);
            } else if (prop.name.equals(PROPERTY_ANNOTATION)) {
                field.setAnnotationType(prop.value);
            }
        }

        return field;
    }

    /**
     * Checks if the given attribute-property if valid for a field.
     *
     * @param  prop       The attribute-property.
     * @param  fieldname  The fieldname.
     * @param  classname  The classname.
     * @throws  EnhancerMetaDataUserException  If the check fails.
     */
    private final void validateFieldProperty(Property prop,
                                             String fieldname,
                                             String classname)
        throws EnhancerMetaDataUserException
    {
        //try to guess the property name
        String value = prop.value;
        if (prop.name == null) {
            if (value.equals(ACCESS_PUBLIC)  ||
                value.equals(ACCESS_PROTECTED)  ||
                value.equals(ACCESS_PACKAGE_LOCAL)  ||
                value.equals(ACCESS_PRIVATE)) {
                // access modifier
                prop.name = PROPERTY_ACCESS_MODIFIER;
            } else if (value.equals(JDO_PERSISTENT)  ||
                     value.equals(JDO_TRANSIENT)  ||
                     value.equals(JDO_TRANSACTIONAL)  ||
                     value.equals(JDO_PROPERTY)) {
                // persistence modifier
                prop.name = PROPERTY_JDO_MODIFIER;
            } else if (value.equals(ANNOTATION_KEY)  ||
                     value.equals(ANNOTATION_DFG)  ||
                     value.equals(ANNOTATION_MEDIATED)) {
                // annotation type
                prop.name = PROPERTY_ANNOTATION;
            } else {
                //assume the the given value is the type
                prop.name = PROPERTY_TYPE;
            }
        } else {
            String entry = classname + FIELD_DELIMITER + fieldname;

            //do we have a valid property name?
            checkPropertyName(prop.name,
                              new String[]{
                                  PROPERTY_ACCESS_MODIFIER,
                                  PROPERTY_JDO_MODIFIER,
                                  PROPERTY_TYPE,
                                  PROPERTY_ANNOTATION
                              },
                              entry);

            //do we have a valid property value
            checkPropertyValue(prop,
                               new String[]{
                                   ACCESS_PUBLIC,
                                   ACCESS_PROTECTED,
                                   ACCESS_PACKAGE_LOCAL,
                                   ACCESS_PRIVATE
                               },
                               PROPERTY_ACCESS_MODIFIER,
                               entry);
            checkPropertyValue(prop,
                               new String[]{
                                   JDO_PERSISTENT,
                                   JDO_TRANSIENT,
                                   JDO_TRANSACTIONAL,
                                   JDO_PROPERTY
                               },
                               PROPERTY_JDO_MODIFIER,
                               entry);
            checkPropertyValue(prop,
                               new String[]{
                                   ANNOTATION_KEY,
                                   ANNOTATION_DFG,
                                   ANNOTATION_MEDIATED
                               },
                               PROPERTY_ANNOTATION,
                               entry);
        }
    }

    /**
     * Validates dependencies between a class and its fields and between.
     *
     * @param clazz the class
     * @throws  EnhancerMetaDataUserException  if the validation fails
     */
    private final void validateDependencies(JDOClass clazz)
        throws EnhancerMetaDataUserException
    {
        final List fields = clazz.getFields();
        for (Iterator i = fields.iterator(); i.hasNext();) {
            JDOField field = (JDOField)i.next();

            // check the jdo field modifier
            if (field.isPersistent() && clazz.isTransient()) {
                // non-persistent classes cannot have persistent fields
                final String msg
                    = getMsg(Msg.ERR_TRANSIENT_CLASS_WITH_PERSISTENT_FIELD,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }
            if (field.isTransactional() && clazz.isTransient()) {
                // non-persistent classes cannot have transactional fields
                final String msg
                    = getMsg(Msg.ERR_TRANSIENT_CLASS_WITH_TRANSACTIONAL_FIELD,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }
            if (!field.isKnownTransient() && !field.isManaged()) {
                // unspecified persistence modifier
                final String msg
                    = getMsg(Msg.ERR_UNSPECIFIED_FIELD_PERSISTENCE_MODIFIER,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }

            // check annotation type
            if (!field.isAnnotated() && field.isManaged()) {
                // unspecified annotation type
                final String msg
                    = getMsg(Msg.ERR_UNSPECIFIED_FIELD_ANNOTATION,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }
            if (field.isAnnotated() && !field.isManaged()) {
                // non managed field with annotation type
                final String msg
                    = getMsg(Msg.ERR_NON_MANAGED_ANNOTATED_FIELD,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }
            if (field.isAnnotated() && clazz.isTransient()) {
                // a non-persistent class cannot have an annotated field
                final String msg
                    = getMsg(Msg.ERR_TRANSIENT_CLASS_WITH_ANNOTATED_FIELD,
                             new String[]{
                                 clazz.getName(),
                                 field.getName() });
                throw new EnhancerMetaDataUserException(msg);
            }
        }
    }

    /**
     * Checks if a given fieldname is a valid Java identifier.
     *
     * @param  fieldname  The fieldname.
     * @param  classname  The corresponding classname.
     * @throws  EnhancerMetaDataUserException  If the check fails.
     */
    static private void validateFieldName(String fieldname,
                                          String classname)
        throws EnhancerMetaDataUserException
    {
        if (fieldname.length() == 0) {
            final String msg
                = getMsg(Msg.ERR_EMPTY_FIELDNAME,
                         new String[]{ classname });
            throw new EnhancerMetaDataUserException(msg);
        }

        if (!Character.isJavaIdentifierStart(fieldname.charAt(0))) {
            final String msg
                = getMsg(Msg.ERR_INVALID_FIELDNAME,
                         new String[]{ classname, fieldname });
            throw new EnhancerMetaDataUserException(msg);
        }

        for (int i = fieldname.length() - 1; i >= 0; i--) {
            final char c = fieldname.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                final String msg
                    = getMsg(Msg.ERR_INVALID_FIELDNAME,
                             new String[]{ classname, fieldname });
                throw new EnhancerMetaDataUserException(msg);
            }
        }
    }

    /**
     * Checks if an attribute-property was entered twice for a class or field.
     *
     * @param  props  The properties.
     * @param  entry  The class- or fieldname.
     * @throws  EnhancerMetaDataUserException  If the check fails.
     */
    static private void checkForDuplicateProperties(List props,
                                                    String entry)
        throws EnhancerMetaDataUserException
    {
        for (int i = 0; i < props.size(); i++) {
            for (int j = i + 1; j < props.size(); j++) {
                Property p1 = (Property)props.get(i);
                Property p2 = (Property)props.get(j);
                if (p1.name.equals(p2.name) && !p1.value.equals(p2.value)) {
                    final String msg
                        = getMsg(Msg.ERR_DUPLICATE_PROPERTY_NAME,
                                 new String[]{
                                     entry,
                                     p1.name,
                                     p1.value,
                                     p2.value });
                    throw new EnhancerMetaDataUserException(msg);
                }
            }
        }
    }

    /**
     * Checks if an attribute name is recognized by the parser.
     *
     * @param  name        The name of the attribute.
     * @param  validnames  A list of valid names(the attribute name has to
     *                     be in this list).
     * @param  entry       The class- or fieldname.
     * @throws  EnhancerMetaDataUserException  If the check fails.
     */
    static private void checkPropertyName(String name,
                                          String[] validnames,
                                          String entry)
        throws EnhancerMetaDataUserException
    {
        for (int i = 0; i < validnames.length; i++) {
            if (name.equals(validnames[i])) {
                return;
            }
        }

        final String msg
            = getMsg(Msg.ERR_INVALID_PROPERTY_NAME,
                     new String[]{ entry, name });
        throw new EnhancerMetaDataUserException(msg);
    }

    /**
     * Checks if the given value of an attribute-property is recognized by
     * by the parser if that value belongs to a given attribute name.
     *
     * @param  prop         The attribute-property(with name and value).
     * @param  validvalues  A list of valid values.
     * @param  name         The name of the attribute-property to check.
     * @param  entry        The class- or fieldname.
     * @throws  EnhancerMetaDataUserException  If the check fails.
     */
    static private void checkPropertyValue(Property prop,
                                           String[] validvalues,
                                           String name,
                                           String entry)
        throws EnhancerMetaDataUserException
    {
        if (!prop.name.equals(name)) {
            return;
        }

        for (int i = 0; i < validvalues.length; i++) {
            if (prop.value.equals(validvalues[i])) {
                return;
            }
        }

        final String msg
            = getMsg(Msg.ERR_INVALID_PROPERTY_VALUE,
                     new String[]{ entry, name, prop.value });
        throw new EnhancerMetaDataUserException(msg);
    }

    /**
     * Formats an error message with the given parameters.
     *
     * @param  msg     The message with format strings.
     * @param  params  The params to format the message with.
     * @return  The formatted error message.
     */
    static final String getMsg(String msg,
                               String[] params)
    {
        return MessageFormat.format(msg, (java.lang.Object[])params);
    }

    /**
     * Parses the attribute-string of a class- or fieldname.
     *
     * @param  attributes  The attribute-string.
     * @return  A list of <code>Propert<</code>-objects for the attributes.
     * @exception  EnhancerMetaDataUserException  If the parsing fails.
     */
    final List parseProperties(String attributes)
        throws EnhancerMetaDataUserException
    {
        tmpTokens.clear();
        for (StringTokenizer t
                 = new StringTokenizer(attributes, PROPERTY_DELIMITERS);
             t.hasMoreTokens();) {
            tmpTokens.add(parseProperty(t.nextToken()));
        }

        return tmpTokens;
    }

    /**
     * Parses the given attribute and splits it into name and value.
     *
     * @param  attribute  The attribute-string.
     * @return  The <code>Propert</code>-object.
     * @exception  EnhancerMetaDataUserException  If the parsing fails.
     */
    private final Property parseProperty(String attribute)
        throws EnhancerMetaDataUserException
    {
        Property prop = new Property();
        int idx = attribute.indexOf(PROPERTY_ASSIGNER);
        if (idx < 0) {
            prop.value = attribute;
        } else {
            prop.name = attribute.substring(0, idx);
            prop.value = attribute.substring(idx + 1, attribute.length());
            if (prop.name.length() == 0 || prop.value.length() == 0) {
                final String msg
                    = getMsg(Msg.ERR_EMPTY_PROPERTY_NAME_OR_VALUE,
                             new String[]{ attribute });
                throw new EnhancerMetaDataUserException(msg);
            }
        }

        return prop;
    }

    /**
     * Returns the access modifier value for a Java modifier name.
     */
    static private int getAccessModifiers(String modifier)
    {
        if (modifier.equals(ACCESS_PUBLIC)) {
            return Modifier.PUBLIC;
        }
        if (modifier.equals(ACCESS_PRIVATE)) {
            return Modifier.PRIVATE;
        }
        if (modifier.equals(ACCESS_PROTECTED)) {
            return Modifier.PROTECTED;
        }
        return 0;
    }

    /**
     * A simple test to run from the command line.
     *
     * @param  argv  The command line arguments.
     */
    public static void main(String[] argv)
    {
        if (argv.length != 1) {
            System.err.println("Error: no property filename specified");
            return;
        }
        final Properties p = new Properties();
        try {
            java.io.InputStream in
                = new java.io.FileInputStream(new java.io.File(argv[0]));
            p.load(in);
            in.close();
            System.out.println("PROPERTIES: " + p);
            System.out.println("############");
            final MetaDataProperties props = new MetaDataProperties(p);
            String[] classnames = props.getKnownClassNames();
            for (int i = 0; i < classnames.length; i++) {
                String classname = classnames[i];
                System.out.println(classname + ": "
                                   + props.getJDOClass(classname));
            }
        } catch(Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * The holder-class for the name and the value of a property.
     */
    static private final class Property
    {
        /**
         * The name of the property.
         */
        String name = null;

        /**
         * The value of the property.
         */
        String value = null;

        /**
         * Creates a string-representation of this object.
         *
         * @return  The string-representation of this object.
         */
        public final String toString()
        {
            return '<' + name + ':' + value + '>';
        }
    }

    //^olsen: -> Bundle.properties

    /**
     * Holds all unformatted error messages.
     */
    static private interface Msg
    {
        // the unformatted error messages
        static final String PREFIX = "Error Parsing meta data properties: ";

        static final String ERR_EMPTY_FIELDNAME =
        PREFIX + "The class ''{0}'' may not have an empty fieldname.";

        static final String ERR_INVALID_FIELDNAME =
        PREFIX + "The field name ''{1}'' of class ''{0}'' is not valid.";

        static final String ERR_EMPTY_PROPERTY_NAME_OR_VALUE  =
        PREFIX + "The property name and value may not be empty if a ''" + 
        PROPERTY_ASSIGNER + "'' is specified: ''{0}''.";

        static final String ERR_INVALID_PROPERTY_NAME =
        PREFIX + "Invalid property name for entry ''{0}'': ''{1}''.";

        static final String ERR_INVALID_PROPERTY_VALUE =
        PREFIX + "Invalid value for property ''{1}'' of entry ''{0}'': ''{2}''.";

        static final String ERR_DUPLICATE_PROPERTY_NAME =
        PREFIX + "The property ''{1}'' for the entry ''{0}'' entered twice with values: ''{2}'' and ''{3}''.";

        static final String ERR_UNSPECIFIED_FIELD_PERSISTENCE_MODIFIER =
        PREFIX + "No persistence modifier specified for field: ''{0}.{1}''.";

        static final String ERR_TRANSIENT_CLASS_WITH_PERSISTENT_FIELD =
        PREFIX + "A non-persistent class cannot have a persistent field(class ''{0}'' with field ''{1})''.";

        static final String ERR_TRANSIENT_CLASS_WITH_TRANSACTIONAL_FIELD =
        PREFIX + "A non-persistent class cannot have a transactional field(class ''{0}'' with field ''{1})''.";

        static final String ERR_UNSPECIFIED_FIELD_ANNOTATION =
        PREFIX + "No annotation type specified for field: ''{0}.{1}''.";

        static final String ERR_TRANSIENT_CLASS_WITH_ANNOTATED_FIELD =
        PREFIX + "A non-persistent class cannot have an annotated field(''{1}'' of class ''{0}'') can''t have a fetch group.";

        static final String ERR_NON_MANAGED_ANNOTATED_FIELD =
        PREFIX + "A non-managed field(''{1}'' of class ''{0}'') can''t be a annotated.";
    }
}
