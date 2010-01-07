/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
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
 * CompletionResult.java
 *
 * Created on June 16, 2005, 4:13 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.sun.javaee.blueprints.components.ui.textfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Class which gathers completion results from event handlers
 *
 * @author Tor Norbye
 */
public class CompletionResult {
    private List results = new ArrayList(AjaxPhaseListener.MAX_RESULTS_RETURNED);

    /** Creates a new instance of CompletionResult */
    CompletionResult() {
    }

    /**
     * Add the given String into the set of completion items returned to the browser
     * @param item The item to be added to the completion result
     */
    public void addItem(String item) {
        results.add(item);
    }

    /**
     * Add all the items in the given String artray into
     * the set of completion items returned to the browser
     * @param items The item array to be added to the completion result
     */
    public void addItems(String[] items) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                results.add(items[i]);
            }
        }
    }

    /**
     * Add all the items in the given collection <b>of Strings</b> into
     * the set of completion items returned to the browser
     * @param items The collection of Strings to be added
     */
    public void addItems(Collection items) {
        results.addAll(items);
    }

    /**
     * Provide the results to other ajax textfield classes such that the results
     * can be encoded and returned to the browser
     * @return A list of completion results. Note that it's not a defensive copy
     *   since it's a package private method. Clients should not muck with this list!
     */
    List getItems() {
        return results;
    }
}
