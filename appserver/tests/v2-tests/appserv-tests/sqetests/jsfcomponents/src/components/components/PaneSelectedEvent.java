/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * $Id: PaneSelectedEvent.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;


/**
 * A custom event which indicates the currently selected pane
 * in a tabbed pane control.
 */
public class PaneSelectedEvent extends FacesEvent {


    public PaneSelectedEvent(UIComponent component, String id) {
        super(component);
        this.id = id;
    }


    // The component id of the newly selected child pane
    private String id = null;


    public String getId() {
        return (this.id);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer("PaneSelectedEvent[id=");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
    }


    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof PaneComponent.PaneSelectedListener);
    }


    public void processListener(FacesListener listener) {
        ((PaneComponent.PaneSelectedListener) listener).processPaneSelectedEvent(
            this);
    }

}
