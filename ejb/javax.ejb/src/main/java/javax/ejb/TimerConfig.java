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
package javax.ejb;

import java.io.Serializable;

/**
 * <p>TimerConfig is used to specify additional timer configuration settings during
 * timer creation.</p>   
 *
 * <p>The info object represents a serializable object made available to
 * corresponding timer callbacks.  It is optional and defaults to null.</p>
 *
 * <p>The persistent property determines whether the corresponding timer has
 * a lifetime that spans the JVM in which it was created.  It is optional
 * and defaults to true.</p> 
 */
public class TimerConfig {

    public TimerConfig() {}

    public TimerConfig(Serializable info, boolean persistent) {
	info_ = info;
  	persistent_ = persistent;
    }

    public void setInfo(Serializable i) {
	info_ = i;
    }

    public Serializable getInfo() {
	return info_;
    }

    public void setPersistent(boolean p) {
	persistent_ = p;
    }

    public boolean isPersistent() {
	return persistent_;
    }

    public String toString() {
        return "TimerConfig [persistent=" + persistent_ + ";info=" + info_ + "]";
    }

    private Serializable info_ = null;

    private boolean persistent_ = true;

}

