/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

function getSelectedValue(field) {
    var theForm = document.forms[0];
    var selectedValue;
    for(i = 0; i < theForm.elements.length; i++) {
        var value = theForm.elements[i].name;
	if(value == null) {
            continue;
	}
	var extnsn = value.lastIndexOf(".");
	var name = value.substr(extnsn+1);
	var fieldName = theForm.elements[i];
	if(name == field && fieldName.checked) {
            selectedValue = fieldName.value;
	    break;
	}
    }
    return selectedValue;
}

function checkForSelectedValue(fieldId) { 
    var field = document.getElementById(fieldId);  
    if (field.value == '' || isWhitespace(field.value)) { 
        return false; 
    } 
    return true; 
}

function isWhitespace(s) { 
    var i; 
    var whitespace = " \t\n\r"; 
    // Search through string's characters one by one 
    // until we find a non-whitespace character. 
    // When we do, return false; if we don't, return true. 
    
    for (i = 0; i < s.length; i++) { 
        // Check that current character isn't whitespace. 
        var c = s.charAt(i); 
        if (whitespace.indexOf(c) == -1) return false; 
    } 

    // All characters are whitespace. 
    return true; 
}
