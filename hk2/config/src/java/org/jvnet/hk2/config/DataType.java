/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hk2.config;

/**
 * Denotes the <code>type</code> of the data a particular config
 * element (attribute, element) should have. This class should be 
 * subclassed whenever a need arises to check if
 * an abstract data type can be represented as a given <code> String </code>.
 * The implementations of a DataType are mapped by their <code> names </code> elsewhere.
 * Subclasses should provide functional implementation of the #validate method
 * and must have a public parameterless constructor (except possibly for primitives).
 * 
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see PrimitiveDataType
 * @see WriteableView
 * @since hk2 0.3.10
 */
public abstract class DataType {

    /** Checks if given value can be had by the abstract data type represented
     *  by this class.
     * @param value String representing the value for this DataType
     * @throws org.jvnet.hk2.config.ValidationException if given String does
     * not represent given data type.
     */
    public abstract void validate(String value) throws ValidationException;
}