/*
 * $Id: FeatureRule.java,v 1.1 2005/09/20 21:11:33 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.rules;


import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.DisplayNameBean;
import com.sun.faces.config.beans.FeatureBean;
import com.sun.faces.config.beans.IconBean;


/**
 * <p>Base Digester rule for elements whose configuration bean
 * extends {@link FeatureBean}.</p>
 */

public abstract class FeatureRule extends Rule {


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeDescription(DescriptionBean top, DescriptionBean old) {

        if (top.getDescription() != null) {
            old.setDescription(top.getDescription());
        }

    }


    // Merge "top" into "old"
    static void mergeDisplayName(DisplayNameBean top, DisplayNameBean old) {

        if (top.getDisplayName() != null) {
            old.setDisplayName(top.getDisplayName());
        }

    }


    // Merge "top" into "old"
    static void mergeFeatures(FeatureBean top, FeatureBean old) {

        DescriptionBean db[] = top.getDescriptions();
        for (int i = 0; i < db.length; i++) {
            DescriptionBean dbo = old.getDescription(db[i].getLang());
            if (dbo == null) {
                old.addDescription(db[i]);
            } else {
                mergeDescription(db[i], dbo);
            }
        }

        DisplayNameBean dnb[] = top.getDisplayNames();
        for (int i = 0; i < dnb.length; i++) {
            DisplayNameBean dnbo = old.getDisplayName(dnb[i].getLang());
            if (dnbo == null) {
                old.addDisplayName(dnb[i]);
            } else {
                mergeDisplayName(dnb[i], dnbo);
            }
        }

        IconBean ib[] = top.getIcons();
        for (int i = 0; i < ib.length; i++) {
            IconBean ibo = old.getIcon(ib[i].getLang());
            if (ibo == null) {
                old.addIcon(ib[i]);
            } else {
                mergeIcon(ib[i], ibo);
            }
        }

    }


    // Merge "top" into "old"
    static void mergeIcon(IconBean top, IconBean old) {

        if (top.getLargeIcon() != null) {
            old.setLargeIcon(top.getLargeIcon());
        }
        if (top.getSmallIcon() != null) {
            old.setSmallIcon(top.getSmallIcon());
        }

    }



}
