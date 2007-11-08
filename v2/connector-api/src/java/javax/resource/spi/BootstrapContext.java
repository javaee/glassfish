/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package javax.resource.spi;

import java.util.Timer;
import javax.resource.spi.work.WorkManager;

/**
 * This provides a mechanism to pass a bootstrap context to a resource adapter
 * instance when it is bootstrapped. That is, when 
 * (<code>start(BootstrapContext)</code>) method on the 
 * <code>ResourceAdapter</code> class is invoked. The bootstrap
 * context contains references to useful facilities that could be used by the
 * resource adapter instance.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface BootstrapContext {
    /**
     * Provides a handle to a <code>WorkManager</code> instance. The
     * <code>WorkManager</code> instance could be used by a resource adapter to
     * do its work by submitting <code>Work</code> instances for execution. 
     *
     * @return a <code>WorkManager</code> instance.
     */
    WorkManager getWorkManager();

    /**
     * Provides a handle to a <code>XATerminator</code> instance. The
     * <code>XATerminator</code> instance could be used by a resource adapter 
     * to flow-in transaction completion and crash recovery calls from an EIS.
     *
     * @return a <code>XATerminator</code> instance.
     */
    XATerminator getXATerminator();

    /**
     * Creates a new <code>java.util.Timer</code> instance. The
     * <code>Timer</code> instance could be used to perform periodic 
     * <code>Work</code> executions or other tasks.
     *
     * @throws UnavailableException indicates that a 
     * <code>Timer</code> instance is not available. The 
     * request may be retried later.
     *
     * @return a new <code>Timer</code> instance.
     */
    Timer createTimer() throws UnavailableException;
}
