/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
