/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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
 * Concurrency.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.concurrency;

import com.sun.jdo.spi.persistence.support.sqlstore.Transaction;
import com.sun.jdo.spi.persistence.support.sqlstore.SQLStateManager;
import com.sun.jdo.spi.persistence.support.sqlstore.UpdateObjectDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ClassDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.UpdateQueryPlan;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.SelectQueryPlan;

/**
 */
public interface Concurrency {
    public static final int CUSTOM = 16;

    public static final int DB_EXPLICIT = 2;

    public static final int DB_NATIVE = 1;

    public static final int NONE = 0;

    public static final int OPT_MASK = 1;

    public static final int OPT_UNIQUE_ID = 4;

    public static final int OPT_VERIFY = 3;

    public Transaction suspend();

    public void resume(Transaction t);

    public void commit(UpdateObjectDesc updateDesc,
                       SQLStateManager beforeImage,
                       SQLStateManager afterImage,
                       int logReason);

    public void configPersistence(ClassDesc config);

    public void select(SelectQueryPlan plan);

    public void update(UpdateQueryPlan plan);

    public Object clone();
}
