/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

/**
 * Denotes the <code>type</code> of the data a particular config
 * element (attribute, element) should have. This interface should be 
 * implemented whenever a need arises to check if
 * an abstract data type can be represented as a given <code> String </code>.
 * The implementations of a DataType are mapped by their <code> names </code> elsewhere.
 * Implementations should provide functional implementation of the #validate method
 * and must have a public parameterless constructor (except possibly for primitives).
 * 
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see PrimitiveDataType
 * @see WriteableView
 * @since hk2 0.3.10
 */
@Contract
public interface DataType {

    /** Checks if given value can be had by the abstract data type represented
     *  by this implementation.
     * @param value String representing the value for this DataType
     * @throws org.jvnet.hk2.config.ValidationException if given String does
     * not represent this data type.
     */
    public void validate(String value) throws ValidationException;
}