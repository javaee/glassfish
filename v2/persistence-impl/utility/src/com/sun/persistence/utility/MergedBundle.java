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
 * MappingContext.java
 *
 * Created on January 28, 2002, 6:30 PM
 */


package com.sun.persistence.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Special resource bundle which delegates to two others. Ideally could just set
 * the parent on the first, but this is protected, so it might not work.  It's
 * still unclear whether that approach would work in this subclass because it
 * may break the localization fall through mechanism if used. Note: This code is
 * copied from NbBundle in the openide sources with the following modifications:
 * - reformatting - making variables final - renaming variables and some params
 * - removing locale code - creating the merged set of keys using jdk classes
 * and not nb utils
 * @author Rochelle Raccah
 * @version %I%
 */
public class MergedBundle extends ResourceBundle {
    private final ResourceBundle _mainBundle, _parentBundle;

    public MergedBundle(ResourceBundle mainBundle, ResourceBundle parentBundle) {
        _mainBundle = mainBundle;
        _parentBundle = parentBundle;
    }

    public Enumeration getKeys() {
        return mergeKeys();
    }

    private Enumeration mergeKeys() {
        Set noDuplicatesMerge = new HashSet(
                getCollection(_mainBundle.getKeys()));

        noDuplicatesMerge.addAll(getCollection(_parentBundle.getKeys()));

        return Collections.enumeration(noDuplicatesMerge);
    }

    private Collection getCollection(Enumeration enumeration) {
        List returnList = new ArrayList();

        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                returnList.add(enumeration.nextElement());
            }
        }

        return returnList;
    }

    protected Object handleGetObject(String key)
            throws MissingResourceException {
        try {
            return _mainBundle.getObject(key);
        } catch (MissingResourceException mre)	// try the other bundle
        {
            return _parentBundle.getObject(key);
        }
    }
}
