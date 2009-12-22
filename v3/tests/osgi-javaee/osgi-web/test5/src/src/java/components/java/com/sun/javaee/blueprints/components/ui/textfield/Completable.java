/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/*
 * Completable.java
 *
 * Created on April 29, 2005, 1:39 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.sun.javaee.blueprints.components.ui.textfield;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 * Interface implemented by classes which can provide completion results
 * for a given prefix.
 *
 * @author Tor Norbye
 */
public interface Completable {
    /** This method is called asynchronously from client javascript calls;
     * it should return a short list of possible matches for the given prefix
     * as an array of Strings. These are returned by adding them to the
     * result object.  These items will then be presented to the user in
     * an auto complete popup.
     *
     * @param context The faces context for the JSF application
     * @param prefix A string prefix to select matches from (note that the
     *   prefix may be empty in which case it may be natural to return the
     *   first N matches.
     * @param result A result object; call {@link CompletionResult#add(String)} to
     *   populate the completion result returned to the browser.
     */
    public void complete(FacesContext context, String prefix, CompletionResult result); // throws CompletionException?
}
