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

/* StatisticWorkaround.java
 * $Id: StatisticWorkaround.java,v 1.4 2007/02/15 20:35:41 sirajg Exp $
 * $Revision: 1.4 $
 * $Date: 2007/02/15 20:35:41 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;
import javax.management.j2ee.statistics.Statistic;
import com.sun.enterprise.admin.monitor.stats.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.logging.Logger;
import java.util.logging.Level;

/** This class is a workaround for returning the statistic descriptions. Please see bug 5045413. 
	This is a best case effort and in general not expected to be accurate.
	We should revisit this in AS 8.2.
	@since AS 8.1
	@author Kedar.Mhaswade@Sun.Com
*/

final class StatisticWorkaround {
	private static final StatsDescriptionHelper helper = new StatsDescriptionHelper(); 
	private static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	static final Statistic[] populateDescriptions(final Statistic[] from) {
		if (from == null) {
			throw new IllegalArgumentException("null arg");	
		}
		final int length = from.length;
		final Statistic[] to = new Statistic[length];
		for (int i = 0 ; i < length ; i++) {
			final Statistic as = from[i];
			final String name = as.getName();
			final String desc = helper.getDescription(name);
			if (StatsDescriptionHelper.NO_DESCRIPTION_AVAILABLE.equals(desc)) {
				//Localized Description was not available, do nothing
				to[i] = from[i];
			}
			else {
                        	attemptSettingDescription(as, desc);
				to[i] = as;
			}
		}
		return ( to );
	}
	private static void attemptSettingDescription(final Statistic s, final String nd) {
		if (s instanceof StatisticImpl) {
			((StatisticImpl)s).setDescription(nd);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("New Description was set: " + nd);
                        }
		}
		else if (s instanceof AverageRangeStatisticImpl) {
			((AverageRangeStatisticImpl)s).setDescription(nd);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("New Description was set: " + nd);
                        }
		}
		else if (s instanceof MutableBoundedRangeStatisticImpl) {
			((MutableBoundedRangeStatisticImpl)s).setDescription(nd);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("New Description was set: " + nd);
                        }
		}
		else if (s instanceof MutableCountStatisticImpl) {
			((MutableCountStatisticImpl)s).setDescription(nd);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("New Description was set: " + nd);
                        }
		}
		else if (s instanceof MutableTimeStatisticImpl) {
			((MutableTimeStatisticImpl)s).setDescription(nd);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("New Description was set: " + nd);
                        }
		}
	}
}
