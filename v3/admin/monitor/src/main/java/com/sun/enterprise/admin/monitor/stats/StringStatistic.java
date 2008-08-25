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
 * $Id: StringStatistic.java,v 1.2 2005/12/25 03:52:26 tcfujii Exp $
 * $Date: 2005/12/25 03:52:26 $
 * $Revision: 1.2 $
 */

package com.sun.enterprise.admin.monitor.stats;
import org.glassfish.j2ee.statistics.Statistic;

/**
 * Custom statistic type created for the Sun ONE Application Server.
 * The goal is to be able to represent changing attribute values that are strings
 * in the form of Statistics. Semantically, it is analogous to a {@link CountStatistic},
 * the only difference being in the value that is returned. Unlike a CountStatistic
 * (which always is unidirectional), this Statistic type is not having any
 * specified direction, simply because there is no natural order. An example
 * of the values that an instance of this statistic type can assume is: A State
 * Statistic which can have "CONNECTED, CLOSED, DISCONNECTED" as the permissible
 * values and the current value can be any one of them (and them only). 
 * The permissible values
 * are upto a particular implementation.
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface StringStatistic extends Statistic {
    
    /**
     * Returns the String value of the statistic.
     */
    public String getCurrent(); 
}
