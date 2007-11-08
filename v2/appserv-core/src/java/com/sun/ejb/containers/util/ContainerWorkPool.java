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

/*
 * Filename: ContainerWorkPool.java
 *
 */

/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/containers/util/ContainerWorkPool.java,v $</I>
 * @author     $Author: mk111283 $
 * @version    $Revision: 1.4 $ $Date: 2006/12/23 13:48:56 $
 */

package com.sun.ejb.containers.util;

import com.sun.corba.ee.spi.orbutil.threadpool.Work;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPool;
import com.sun.enterprise.util.S1ASThreadPoolManager;
import com.sun.enterprise.util.ORBManager;

import com.sun.enterprise.util.threadpool.Servicable;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This class implements a singleton instance which
 * is used by the container for its housekeeping tasks
 * using the default threadpool of the appserver
 */

public class ContainerWorkPool {

    public static final Logger _logger =
        LogDomains.getLogger(LogDomains.UTIL_LOGGER);

    public static void addLast(Servicable ser) {
        addLast(new WorkAdapter(ser));
    }

    public static void addFirst(Servicable ser) {
        addLast(new WorkAdapter(ser));
    }

    public static void addFirst(Work work) {
        addLast(work);
    }

    public static void addLast(Work work) {
        ThreadPoolManager threadpoolMgr =
            S1ASThreadPoolManager.getThreadPoolManager();
        ThreadPool threadpool = threadpoolMgr.getDefaultThreadPool();
        threadpool.getAnyWorkQueue().addWork(work);
    }

}

