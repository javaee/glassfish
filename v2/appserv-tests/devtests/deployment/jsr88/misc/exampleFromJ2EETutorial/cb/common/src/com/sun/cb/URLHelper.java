/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2003 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */
package com.sun.cb;
import java.util.ResourceBundle;
 
public final class URLHelper {

    public static final String getSaajURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String saajURL = registryBundle.getString("saaj.url");


        return saajURL;

    } // getDate

    public static final String getEndpointURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String endPointURL = registryBundle.getString("endpoint.url");


        return endPointURL;

    } // getDate

    public static final String getQueryURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String queryURL = registryBundle.getString("query.url");


        return queryURL;

    } // getDate

    public static final String getPublishURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String publishURL = registryBundle.getString("publish.url");

        return publishURL;

    } // getDate


} // class
