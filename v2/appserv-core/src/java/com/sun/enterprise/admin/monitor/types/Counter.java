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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.monitor.types;

/**
 * Monitored attribute type counter. An attribute of Counter type has following
 * properties -
 * <ul>
 * <li>Attribute value is Byte, Short, Integer or Long</li>
 * <li>Attribute value is always greater than or equal to zero</li>
 * <li>Attribute value can only be incremented.</li>
 * <li>Attribute value can roll over and in that case a modulus value is
 * defined.</li>
 * </ul>
 */
public class Counter extends MonitoredAttributeType {

    /**
     * Useful object to denote a monitored attribute whose values are of
     * type java.lang.Byte and maximum value is Byte.MAX_VALUE
     */
    public static final Counter BYTE = new Counter(Byte.TYPE,
            new Byte(Byte.MAX_VALUE));

    /**
     * Useful object to denote a monitored attribute whose values are of
     * type java.lang.Short and maximum value is Short.MAX_VALUE
     */
    public static final Counter SHORT = new Counter(Short.TYPE,
            new Short(Short.MAX_VALUE));

    /**
     * Useful object to denote a monitored attribute whose values are of
     * type java.lang.Integer and maximum value is Integer.MAX_VALUE
     */
    public static final Counter INTEGER = new Counter(Integer.TYPE,
            new Integer(Integer.MAX_VALUE));

    /**
     * Useful object to denote a monitored attribute whose values are of
     * type java.lang.Long and maximum value is Long.MAX_VALUE
     */
    public static final Counter LONG = new Counter(Long.TYPE,
            new Long(Long.MAX_VALUE));

    /**
     * Maximum value for this type
     */
    private Number maxValue;

    /**
     * Create a Type "Counter Monitored Attribute" with specified java type
     * (of the monitored attribute value) and specified maximum value.
     * @param type class of object returned as value for the monitored attribute
     * @param maxVal maximum possible value for this type of monitored attribute
     */
    protected Counter(Class type, Number maxVal) {
        super(type);
        maxValue = maxVal;
    }

    /**
     * Get maximum possible value for this counter. If the attribute value is
     * higher than this number then it will be rolled over.
     */
    public Number getMaxValue() {
        return maxValue;
    }
}
