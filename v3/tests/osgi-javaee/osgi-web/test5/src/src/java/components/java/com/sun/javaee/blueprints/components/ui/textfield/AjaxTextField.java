/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/**
 * Completion-capable JSF text field.
 */
package com.sun.javaee.blueprints.components.ui.textfield;

import java.io.IOException;
import java.util.ArrayList;

import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;


/**
 * <p>
 * Completion-capable JSF text field.
 * The component has a completionMethod which is called repeatedly (asynchronously)
 * and is in charge of registering completion results (which will be populated
 * in the completion dialog for this field).
 * </p>
 * <p>
 * The component will render CSS and JavaScript into the page. These JavaScript and CSS
 * class names could potentially interact with your own JSP contents if there is a name
 * conflict so if you observe strange results, try changing your method names or style
 * classes.
 * </p>
 *
 * @todo Pick more unique namespace names for the style classes and the javascript
 *    methods - perhaps prefixed by "ajax" or "ajtf" (ajax text field), or maybe even
 *    "blueprints" ?
 * @todo Add property "maxResults", setting the max number of completion items
 *    shown in the completion popup
 * @todo Add property "Delay (milliseconds)" - an optional timer delay before
 *    popup should appear or get updated after keystrokes
 * @todo Add property "showOnFocus", which when set will cause the completion
 *    dialog to be shown showing the first possible matches even before the user
 *    has typed anything into the text field
 *
 * @author Tor Norbye
*/
public class AjaxTextField extends HtmlInputText {
    //private MethodBinding completionBinding;
    private String completionMethod;
    private String onchoose;
    private String ondisplay;

    public AjaxTextField() {
        super();
        setRendererType("AjaxTextField");
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[4];
        values[0] = super.saveState(context);
        values[1] = completionMethod;
        values[2] = onchoose;
        values[3] = ondisplay;

        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[])state;
        super.restoreState(context, values[0]);
        this.completionMethod = (String)values[1];
        this.onchoose = (String)values[2];
        this.ondisplay = (String)values[3];
    }

    public void setOnchoose(String onchoose) {
        this.onchoose = onchoose;
    }

    public String getOnchoose() {
        return onchoose;
    }

    public void setOndisplay(String ondisplay) {
        this.ondisplay = ondisplay;
    }

    public String getOndisplay() {
        return ondisplay;
    }

    public void setCompletionMethod(String completionMethod) {
        this.completionMethod = completionMethod;
    }

    public String getCompletionMethod() {
        return completionMethod;
    }

    /**
     * <p>Render a textual textfield.</p>
     */
    public String getText() {
        //return (String) getValue();
        Object value = getValue();
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }
    
    /** 
     * @see #getText()
     */
    public void setText(String text) {
        setValue((Object) text);
    }     
    
    /** Return the maximum number of results returned from this text field
     * @return A numberf indicating the maximum number of completion matches
     *   that should be returned/displayed
     */
    public int getMaxCount() {
        return AjaxPhaseListener.MAX_RESULTS_RETURNED;
    }
}
