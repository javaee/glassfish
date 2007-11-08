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

package com.sun.org.apache.jdo.model;

import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * This exception indicates a problem during model validation.
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class ModelValidationException 
    extends ModelException
{
	/** Constant representing an error. */
	public static final int ERROR = 0;

	/** Constant representing a warning. */
	public static final int WARNING = 1;

	/** 
     * This field holds the type -- one of {@link #ERROR} or {@link #WARNING}
	 */
	private int type;

	/** 
     * This field holds the offending object -- the one being validated 
	 * when the problem occurred
	 */
	private Object offendingObject;

    /** I18N support */
    private static I18NHelper msg = 
        I18NHelper.getInstance(ModelValidationException.class);

    /**
     * Creates new <code>ModelValidationException</code> of type 
     * {@link #ERROR} with <code>null</code> as the offending object and
     * no detail message.
     */
    public ModelValidationException() 
    {
       this(ERROR, null, null); 
    }
    
    /**
     * Constructs a <code>ModelValidationException</code> of type 
     * {@link #ERROR} with <code>null</code> as the offending object and
     * with the specified detail message. 
     * @param message the detail message.
     */
    public ModelValidationException(String message)
    {
        this(ERROR, null, message);
    }

	/**
	 * Constructs a <code>ModelValidationException</code> of type 
	 * {@link #ERROR} with the specified offending object and no 
	 * detail message.
	 * @param offendingObject the offending object.
	 */
	public ModelValidationException (Object offendingObject)
	{
		this(ERROR, offendingObject, null);
	}

	/**
	 * Constructs a <code>ModelValidationException</code> of type 
	 * {@link #ERROR} with the specified offending object and detail
     * message .
	 * @param offendingObject the offending object.
	 * @param message the detail message.
	 */
	public ModelValidationException (Object offendingObject, String message)
	{
		this(ERROR, offendingObject, message);
	}

	/**
	 * Constructs a <code>ModelValidationException</code> of the specified 
	 * type with the specified detail message and offending object. 
	 * @param errorType the type -- one of {@link #ERROR} or 
     * {@link #WARNING}. 
	 * @param offendingObject the offending object.
	 * @param message the detail message.
	 */
	public ModelValidationException(int errorType, Object offendingObject,
                                    String message)
	{
		super(message);
		this.type = errorType;
		this.offendingObject = offendingObject;
	}

	/**
	 * Get the offending object -- the one being validated when the problem 
	 * occurred.
	 */
	public Object getOffendingObject () 
    { 
        return offendingObject; 
    }

	/**
	 * Get the type -- one of {@link #ERROR} or {@link #WARNING}.
	 */
	public int getType() 
    { 
        return type; 
    }

	/**
	* Returns the error message string of this throwable object.
	* @return the error message string of this 
	* <code>ModelValidationException</code>, prepended with the warning string 
	* if the type is {@link #WARNING}
	*
	*/
	public String getMessage ()
	{
		String message = super.getMessage();
		if ((WARNING == getType()) && 
            (message != null) && (message.length() > 0)) {
			message	= msg.msg("MSG_OffendingObject") + message; //NOI18N
		}
		return message;
	}
    /** 
     * The <code>String</code> representation includes the name of the class,
     * the descriptive comment (if any),
     * and the <code>String</code> representation of the cause (if any).
     * @return the <code>String</code>.
     */
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        // include offending object information
        if (offendingObject != null) {
            sb.append("\n");  //NOI18N
            sb.append(msg.msg("MSG_OffendingObject"));  //NOI18N
            sb.append(offendingObject.toString());
        }
        return sb.toString();
    }
  
}
