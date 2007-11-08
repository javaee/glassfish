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

package com.sun.org.apache.jdo.impl.enhancer.jdo.impl;


/**
 * All constant definitions by the JDO specification.
 */
public interface JDOConstants
    extends JDO_ClassConstants,
            JDO_PC_MemberConstants,
            JDO_IC_MemberConstants,
            JDO_OIFC_MemberConstants,
            JDO_OIFS_MemberConstants,
            JDO_SM_MemberConstants,
            JDO_IH_MemberConstants,
            JDO_FIE_MemberConstants
{
    
    public static final String ORIG_GETTER_PREFIX = "_orig_get";
    
    public static final String ORIG_SETTER_PREFIX = "_orig_set";
    
}
