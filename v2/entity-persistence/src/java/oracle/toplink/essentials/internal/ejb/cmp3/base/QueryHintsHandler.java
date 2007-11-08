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
package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.toplink.essentials.config.*;

import oracle.toplink.essentials.queryframework.DatabaseQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.logging.SessionLog;

/**
 * 
 * The class processes query hints.
 * 
 * TopLink query hints and their values defined in oracle.toplink.essentials.config package.
 * 
 * To add a new query hint:
 *   Define a new hint in TopLinkQueryHints;
 *   Add a class containing hint's values if required to config package (like CacheUsage);
 *      Alternatively values defined in HintValues may be used - Refresh and BindParameters hints do that.
 *   Add an inner class to this class extending Hint corresponding to the new hint (like CacheUsageHint);
 *      The first constructor parameter is hint name; the second is default value;
 *      In constructor 
 *          provide 2-dimensional value array in case the values should be translated (currently all Hint classes do that);
 *              in case translation is not required provide a single-dimension array (no such examples yet).
 *   In inner class Hint static initializer addHint an instance of the new hint class (like addHint(new CacheUsageHint())).
 * 
 * @see TopLinkQueryHints
 * @see HintValues
 * @see CacheUsage
 * @see PessimisticLock
 * 
 */
public class QueryHintsHandler {
    
    /**
     * INTERNAL:
     * Verifies the hints.
     * 
     * If session != null then logs a FINEST message for each hint.
     * queryName parameter used only for identifying the query in messages,
     * if it's null then "null" will be used.
     * Throws IllegalArgumentException in case the hint value is illegal.
     */
    public static void verify(Map hints, String queryName, AbstractSession session) {
        if(hints == null) {
            return;
        }
        Iterator it = hints.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String hintName = (String)entry.getKey();
            verify(hintName, entry.getValue(), queryName, session);
        }
    }
    
    /**
     * INTERNAL:
     * Verifies the hint.
     * 
     * If session != null then logs a FINEST message.
     * queryName parameter used only for identifying the query in messages,
     * if it's null then "null" will be used.
     * Throws IllegalArgumentException in case the hint value is illegal.
     */
    public static void verify(String hintName, Object hintValue, String queryName, AbstractSession session) {
        Hint.verify(hintName, shouldUseDefault(hintValue), hintValue, queryName, session);
    }
    
    /**
     * INTERNAL:
     * Applies the hints to the query.
     * Throws IllegalArgumentException in case the hint value is illegal.
     */
    public static void apply(Map hints, DatabaseQuery query) {
        if(hints == null) {
            return;
        }
        Iterator it = hints.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String hintName = (String)entry.getKey();
            apply(hintName, entry.getValue(), query);
        }
    }
    
    /**
     * INTERNAL:
     * Applies the hint to the query.
     * Throws IllegalArgumentException in case the hint value is illegal.
     */
    public static void apply(String hintName, Object hintValue, DatabaseQuery query) {
        Hint.apply(hintName, shouldUseDefault(hintValue), hintValue, query);
    }
    
    /**
     * INTERNAL:
     * Empty String hintValue indicates that the default hint value
     * should be used.
     */
    protected static boolean shouldUseDefault(Object hintValue) {
        return (hintValue != null) &&  (hintValue instanceof String) && (((String)hintValue).length() == 0);
    }
    
    protected static abstract class Hint {
        static HashMap mainMap = new HashMap();
        Object[] valueArray;
        HashMap valueMap;
        String name;
        String defaultValue;
        Object defaultValueToApply;
        boolean valueToApplyMayBeNull;
        
        static {
            addHint(new BindParametersHint());
            addHint(new CacheUsageHint());
            addHint(new PessimisticLockHint());
            addHint(new RefreshHint());
            addHint(new CascadePolicyHint());
        }
        
        Hint(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        abstract void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query);
                
        static void verify(String hintName, boolean shouldUseDefault, Object hintValue, String queryName, AbstractSession session) {
            Hint hint = (Hint)mainMap.get(hintName);
            if(hint == null) {
                if(session != null) {
                    session.log(SessionLog.FINEST, SessionLog.QUERY, "unknown_query_hint", new Object[]{getPrintValue(queryName), hintName});
                }
                return;
            }
                                    
            hint.verify(hintValue, shouldUseDefault, queryName, session);
        }
        
        void verify(Object hintValue, boolean shouldUseDefault, String queryName, AbstractSession session) {
            if(shouldUseDefault) {
                hintValue = defaultValue;
            }
            if(session != null) {
                session.log(SessionLog.FINEST, SessionLog.QUERY, "query_hint", new Object[]{getPrintValue(queryName), name, getPrintValue(hintValue)});
            }
            if(!shouldUseDefault && valueMap != null && !valueMap.containsKey(getUpperCaseString(hintValue))) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-wrong-query-hint-value",new Object[]{getPrintValue(queryName), name, getPrintValue(hintValue)}));
            }
        }
        
        static void apply(String hintName, boolean shouldUseDefault, Object hintValue, DatabaseQuery query) {
            Hint hint = (Hint)mainMap.get(hintName);
            if(hint == null) {
                // unknown hint name - silently ignored.
                return;
            }
            
            hint.apply(hintValue, shouldUseDefault, query);
        }
        
        void apply(Object hintValue, boolean shouldUseDefault, DatabaseQuery query) {
            Object valueToApply = hintValue;
            if(shouldUseDefault) {
                valueToApply = defaultValueToApply;
            } else {
                if(valueMap != null) {
                    String key = getUpperCaseString(hintValue);
                    valueToApply = valueMap.get(key);
                    if(valueToApply == null) {
                        boolean wrongKey = true;
                        if(valueToApplyMayBeNull) {
                            wrongKey = !valueMap.containsKey(key);
                        }
                        if(wrongKey) {
                            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-wrong-query-hint-value",new Object[]{getQueryId(query), name, getPrintValue(hintValue)}));
                        }
                    }
                }
            }
            applyToDatabaseQuery(valueToApply, query);
        }

        static String getQueryId(DatabaseQuery query) {
            String queryId = query.getName();
            if(queryId == null) {
                queryId = query.getEJBQLString();
            }
            return getPrintValue(queryId);
        }
        
        static String getPrintValue(Object hintValue) {
            return hintValue != null ? hintValue.toString() : "null";
        }
    
        static String getUpperCaseString(Object hintValue) {
            return hintValue != null ? hintValue.toString().toUpperCase() : null;
        }

        void initialize() {
            if(valueArray != null) {
                valueMap = new HashMap(valueArray.length);
                if(valueArray instanceof Object[][]) {
                    Object[][] valueArray2 = (Object[][])valueArray;
                    for(int i=0; i<valueArray2.length; i++) {
                        valueMap.put(getUpperCaseString(valueArray2[i][0]), valueArray2[i][1]);
                        if(valueArray2[i][1] == null) {
                            valueToApplyMayBeNull = true;
                        }
                    }
                } else {
                    for(int i=0; i<valueArray.length; i++) {
                        valueMap.put(getUpperCaseString(valueArray[i]), valueArray[i]);
                        if(valueArray[i] == null) {
                            valueToApplyMayBeNull = true;
                        }
                    }
                }
                defaultValueToApply = valueMap.get(defaultValue.toUpperCase());
            }
        }
        static void addHint(Hint hint) {
            hint.initialize();
            mainMap.put(hint.name, hint);
        }
    }

    protected static class BindParametersHint extends Hint {
        BindParametersHint() {
            super(TopLinkQueryHints.BIND_PARAMETERS, HintValues.PERSISTENCE_UNIT_DEFAULT);
            valueArray = new Object[][] { 
                {HintValues.PERSISTENCE_UNIT_DEFAULT, null},
                {HintValues.TRUE, Boolean.TRUE},
                {HintValues.FALSE, Boolean.FALSE}
            };
        }
    
        void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query) {
            if(valueToApply == null) {
                query.ignoreBindAllParameters();
            } else {
                query.setShouldBindAllParameters(((Boolean)valueToApply).booleanValue());
            }
        }
    }

    protected static class CacheUsageHint extends Hint {
        CacheUsageHint() {
            super(TopLinkQueryHints.CACHE_USAGE, CacheUsage.DEFAULT);
            valueArray = new Object[][] {
                {CacheUsage.UseEntityDefault, ObjectLevelReadQuery.UseDescriptorSetting},
                {CacheUsage.DoNotCheckCache, ObjectLevelReadQuery.DoNotCheckCache},
                {CacheUsage.CheckCacheByExactPrimaryKey, ObjectLevelReadQuery.CheckCacheByExactPrimaryKey},
                {CacheUsage.CheckCacheByPrimaryKey, ObjectLevelReadQuery.CheckCacheByPrimaryKey},
                {CacheUsage.CheckCacheThenDatabase, ObjectLevelReadQuery.CheckCacheThenDatabase},
                {CacheUsage.CheckCacheOnly, ObjectLevelReadQuery.CheckCacheOnly},
                {CacheUsage.ConformResultsInUnitOfWork, ObjectLevelReadQuery.ConformResultsInUnitOfWork}
            };
        }
    
        void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query) {
            if (query.isObjectLevelReadQuery()) {
                ((ObjectLevelReadQuery)query).setCacheUsage(((Integer)valueToApply).intValue());
            }
        }
    }

    protected static class CascadePolicyHint extends Hint {
        CascadePolicyHint() {
            super(TopLinkQueryHints.REFRESH_CASCADE, CascadePolicy.DEFAULT);
            valueArray = new Object[][] {
                {CascadePolicy.NoCascading, DatabaseQuery.NoCascading},
                {CascadePolicy.CascadePrivateParts, DatabaseQuery.CascadePrivateParts},
                {CascadePolicy.CascadeAllParts, DatabaseQuery.CascadeAllParts},
                {CascadePolicy.CascadeByMapping, DatabaseQuery.CascadeByMapping}
            };
        }
    
        void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query) {
            // this time cascade policy make sense only for read query with refresh option
            // However cascade policy is generic property for DatabaseQuery, 
            // therefore can have a meaning for other types of query in the future. 
            if (query.isObjectLevelReadQuery()) {
                query.setCascadePolicy((Integer)valueToApply);
            }
        }
    }

    protected static class PessimisticLockHint extends Hint {
        PessimisticLockHint() {
            super(TopLinkQueryHints.PESSIMISTIC_LOCK, PessimisticLock.DEFAULT);
            valueArray = new Object[][] {
                {PessimisticLock.NoLock, ObjectLevelReadQuery.NO_LOCK},
                {PessimisticLock.Lock, ObjectLevelReadQuery.LOCK},
                {PessimisticLock.LockNoWait, ObjectLevelReadQuery.LOCK_NOWAIT}
            };
        }
    
        void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query) {
            if (query.isObjectLevelReadQuery()) {
                ((ObjectLevelReadQuery)query).setLockMode(((Short)valueToApply).shortValue());
            }
        }
    }

    protected static class RefreshHint extends Hint {
        RefreshHint() {
            super(TopLinkQueryHints.REFRESH, HintValues.FALSE);
            valueArray = new Object[][] { 
                {HintValues.FALSE, Boolean.FALSE},
                {HintValues.TRUE, Boolean.TRUE}
            };
        }
    
        void applyToDatabaseQuery(Object valueToApply, DatabaseQuery query) {
            if (query.isObjectLevelReadQuery()) {
                ((ObjectLevelReadQuery)query).setShouldRefreshIdentityMapResult(((Boolean)valueToApply).booleanValue());
            }
        }
    }

}
