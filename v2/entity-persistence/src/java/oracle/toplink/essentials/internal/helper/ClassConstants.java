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
import java.math.*;
import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 */
public class ClassConstants {
    // Java classes
    public static final Class Collection_Class = Collection.class;
    public static final Class Hashtable_Class = Hashtable.class;
    public static final Class Enumeration_Class = Enumeration.class;
    public static final Class JavaSqlTime_Class = java.sql.Time.class;
    public static final Class JavaSqlDate_Class = java.sql.Date.class;
    public static final Class JavaSqlTimestamp_Class = java.sql.Timestamp.class;
    public static final Class List_Class = List.class;
    public static final Class Map_Class = Map.class;
    public static final Class Object_Class = Object.class;
    public static final Class SortedSet_Class = SortedSet.class;
    public static final Class Vector_class = Vector.class;
    public static final Class Void_Class = void.class;
    public static final Class PropertyChangeEvent_Class = java.beans.PropertyChangeEvent.class;

    // Toplink Classes
    public static final Class Accessor_Class = oracle.toplink.essentials.internal.databaseaccess.Accessor.class;
    public static final Class ConversionManager_Class = oracle.toplink.essentials.internal.helper.ConversionManager.class;
    public static final Class DatabaseQuery_Class = oracle.toplink.essentials.queryframework.DatabaseQuery.class;
    public static final Class DatabaseRow_Class = oracle.toplink.essentials.internal.sessions.AbstractRecord.class;
    public static final Class DescriptorEvent_Class = oracle.toplink.essentials.descriptors.DescriptorEvent.class;
    public static final Class DirectConnector_Class = oracle.toplink.essentials.sessions.DirectConnector.class;
    public static final Class Expression_Class = oracle.toplink.essentials.expressions.Expression.class;
    public static final Class FunctionExpression_Class = oracle.toplink.essentials.internal.expressions.FunctionExpression.class;
    public static final Class IndirectContainer_Class = oracle.toplink.essentials.indirection.IndirectContainer.class;
    public static final Class IndirectList_Class = oracle.toplink.essentials.indirection.IndirectList.class;
    public static final Class IndirectMap_Class = oracle.toplink.essentials.indirection.IndirectMap.class;
    public static final Class LogicalExpression_Class = oracle.toplink.essentials.internal.expressions.LogicalExpression.class;
    public static final Class PublicInterfaceDatabaseSession_Class = DatabaseSessionImpl.class;
    public static final Class PublicInterfaceSession_Class = AbstractSession.class;
    public static final Class QueryKey_Class = oracle.toplink.essentials.querykeys.QueryKey.class;
    public static final Class RelationExpression_Class = oracle.toplink.essentials.internal.expressions.RelationExpression.class;
    public static final Class Record_Class = oracle.toplink.essentials.sessions.Record.class;
    public static final Class ServerSession_Class = oracle.toplink.essentials.threetier.ServerSession.class;
    public static final Class SessionsSession_Class = oracle.toplink.essentials.sessions.Session.class;
    public static final Class ValueHolderInterface_Class = oracle.toplink.essentials.indirection.ValueHolderInterface.class;
    public static final Class WeavedAttributeValueHolderInterface_Class = oracle.toplink.essentials.indirection.WeavedAttributeValueHolderInterface.class;
   
    // Identity map classes
    public static final Class CacheIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.CacheIdentityMap.class;
    public static final Class FullIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.FullIdentityMap.class;
    public static final Class HardCacheWeakIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.HardCacheWeakIdentityMap.class;
    public static final Class NoIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.NoIdentityMap.class;
    public static final Class SoftCacheWeakIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.SoftCacheWeakIdentityMap.class;
    public static final Class WeakIdentityMap_Class = oracle.toplink.essentials.internal.identitymaps.WeakIdentityMap.class;

    //fetch group class
    public static final Class FetchGroupTracker_class = oracle.toplink.essentials.queryframework.FetchGroupTracker.class;

    // Moved from ConversionManager
    public static final Class ABYTE = Byte[].class;
    public static final Class ACHAR = Character[].class;
    public static final Class APBYTE = byte[].class;
    public static final Class APCHAR = char[].class;
    public static final Class BIGDECIMAL = BigDecimal.class;
    public static final Class BIGINTEGER = BigInteger.class;
    public static final Class BOOLEAN = Boolean.class;
    public static final Class BYTE = Byte.class;
    public static final Class CLASS = Class.class;
    public static final Class CHAR = Character.class;
    public static final Class CALENDAR = Calendar.class;
    public static final Class DOUBLE = Double.class;
    public static final Class FLOAT = Float.class;
    public static final Class GREGORIAN_CALENDAR = GregorianCalendar.class;
    public static final Class INTEGER = Integer.class;
    public static final Class LONG = Long.class;
    public static final Class NUMBER = Number.class;
    public static final Class OBJECT = Object.class;
    public static final Class PBOOLEAN = boolean.class;
    public static final Class PBYTE = byte.class;
    public static final Class PCHAR = char.class;
    public static final Class PDOUBLE = double.class;
    public static final Class PFLOAT = float.class;
    public static final Class PINT = int.class;
    public static final Class PLONG = long.class;
    public static final Class PSHORT = short.class;
    public static final Class SHORT = Short.class;
    public static final Class SQLDATE = java.sql.Date.class;
    public static final Class STRING = String.class;
    public static final Class TIME = java.sql.Time.class;
    public static final Class TIMESTAMP = java.sql.Timestamp.class;
    public static final Class UTILDATE = java.util.Date.class;

    //LOB support types
    public static final Class BLOB = java.sql.Blob.class;
    public static final Class CLOB = java.sql.Clob.class;

    //Indication to ConversionManager not to convert classes implementing this interface
    public static final Class NOCONVERSION = NoConversion.class;

    public ClassConstants() {
    }
}
