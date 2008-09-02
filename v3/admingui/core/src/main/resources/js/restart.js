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

/*===========================================================================*/
/* synchronizeRestartRequired */
/*===========================================================================*/
var reasonsHidden = true;

function synchronizeRestartRequired(currentRestartStatus, oldRestartStatus) {
    if (currentRestartStatus != oldRestartStatus) {
        parent.parent.frames["header"].location.reload();
        parent.parent.document.getElementById('outerFrameset').setAttribute('rows', '103,*', 0);
    }
    return true;
}

function showRestartReasons() {
    var el = document.getElementById('restartReasons');
    var toggle = document.getElementById('form:title:restartReasonsToggle');
    if (reasonsHidden) {
        toggle.src = "/admingui/theme/woodstock4_3/suntheme/images/table/grouprow_expanded.gif";
        el.style.visibility = "visible";
    } else {
        toggle.src = "/admingui/theme/woodstock4_3/suntheme/images/table/grouprow_collapsed.gif";
        el.style.visibility = "hidden";
    }
    reasonsHidden = !reasonsHidden;
}