/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgijpa;

import org.osgi.framework.*;
import org.glassfish.osgiweb.Extender;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An extender that listens for Persistence bundle's life cycle events
 * and takes appropriate actions.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JPAExtender implements Extender, SynchronousBundleListener
{
    private Logger logger = Logger.getLogger(JPAExtender.class.getPackage().getName());
    private BundleContext context;

    public JPAExtender(BundleContext context)
    {
        this.context = context;
    }

    public void start()
    {
        context.addBundleListener(this);
        logger.logp(Level.INFO, "JPAExtender", "start", " JPAExtender started", new Object[]{});
    }

    public void stop()
    {
        context.removeBundleListener(this);
        logger.logp(Level.INFO, "JPAExtender", "stop", " JPAExtender stopped", new Object[]{});
    }

    public void bundleChanged(BundleEvent event)
    {
        Bundle bundle = event.getBundle();
        switch (event.getType())
        {
            case BundleEvent.INSTALLED :
            case BundleEvent.UPDATED :
                JPABundleProcessor bi = new JPABundleProcessor(bundle);
                if (!bi.isEnhanced(bundle) && bi.isJPABundle()) {
                    logger.logp(Level.INFO, "JPAExtender", "bundleChanged", "Bundle having id {0} is a JPA bundle", new Object[]{bundle.getBundleId()});
                    try {
                        bi.enhance();
                    } catch (Exception e) {
                        logger.logp(Level.WARNING, "JPAExtender", "bundleChanged", "Failed to enhance bundle having id " + bundle.getBundleId(), e);
                    }
                }
                break;
            default:
                break;
        }
    }

}
