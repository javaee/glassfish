/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.chunk;

import java.io.Serializable;

@javax.inject.Named("SimpleItemReader")
public class SimpleItemReader
    implements javax.batch.api.chunk.ItemReader {

    private int index = 0;
    
    //EMP-ID, MONTH-YEAR, SALARY, TAX%, MEDICARE%, OTHER
    private String[] items = new String[] {
        "120-01, JAN-2013, 8000, 27, 3, 0",
        "120-02, JAN-2013, 8500, 27, 3, 0",
        "120-03, JAN-2013, 9000, 33, 4, 0",
        "120-04, JAN-2013, 8500, 33, 4, 0",
        "120-05, JAN-2013, 10000, 33, 4, 0",
        "120-06, JAN-2013, 10500, 33, 4, 0",
        "120-07, JAN-2013, 11000, 36, 5, 0",
        "120-08, JAN-2013, 11500, 36, 5, 0",
    };
    
    @Override
    public void open(Serializable e) throws Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public Object readItem() throws Exception {
        return index < items.length ? items[index++] : null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
    
}
