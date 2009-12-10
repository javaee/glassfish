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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * StatsToString.java
 * $Id: StatsToString.java,v 1.2 2005/12/25 03:52:33 tcfujii Exp $
 * $Date: 2005/12/25 03:52:33 $
 * $Revision: 1.2 $
 */


package com.sun.enterprise.admin.monitor.util;
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.Statistic;
/**
 * Returns String implementation of all the statistics within the {@link Stats}
 * instance passed in with one statistic per line.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since __PRODUCT__
 * @version $Revision: 1.2 $
 */
public class StatsToString {
	private final Stats stats;
	private final String NL = System.getProperty("line.separator");
	/** Creates a new instance of StatsToString */
	StatsToString(Stats stats) {
		this.stats = stats;
	}
	public String toString() {
		final StringBuffer s = new StringBuffer(stats.getClass().getName()).append(NL);
		final Statistic[] ss = stats.getStatistics();
		for(int i = 0 ; i < ss.length ; i++) {
			s.append(ss[i].toString()).append(NL);
		}
		return s.toString();
	}
}
