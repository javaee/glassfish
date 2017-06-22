/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import javax.transaction.xa.*;

/**
 * This is class is used for debugging. It prints out
 * trace information on TM calls to XAResource before
 * directing the call to the actual XAResource object
 */
public class FakeXAResource implements XAResource {

    public FakeXAResource() {}

    public void commit(Xid xid, boolean onePhase) throws XAException {
        print("FakeXAResource.commit: " + xidToString(xid) + "," + onePhase);
    }

    public void end(Xid xid, int flags) throws XAException {
        print("FakeXAResource.end: " + xidToString(xid) + "," +
              flagToString(flags));
    }

    
    public void forget(Xid xid) throws XAException {
        print("FakeXAResource.forget: " + xidToString(xid));
    }

    public int getTransactionTimeout() throws XAException {
        return 60*1000;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        print("FakeXAResource.prepare: " + xidToString(xid));
        return XAResource.XA_OK;
    }
    
    public Xid[] recover(int flag) throws XAException {
        print("FakeXAResource.recover: " + flagToString(flag));
        return null;
    }

    public void rollback(Xid xid) throws XAException {
        print("FakeXAResource.rollback: " + xidToString(xid));
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }
    
    public void start(Xid xid, int flags) throws XAException {
        print("FakeXAResource.start: " + xidToString(xid) + "," +
                flagToString(flags));
        throw new XAException();
    }

    private void print(String s) {
        System.out.println(s);
    }

    static public String xidToString(Xid xid) {
        return String.valueOf((new String(xid.getGlobalTransactionId()) +
                               new String(xid.getBranchQualifier())).hashCode());
    }

    static public String flagToString(int flag) {
        switch (flag) {
        case TMFAIL:
            return "TMFAIL";
        case TMJOIN:
            return "TMJOIN";
        case TMNOFLAGS:
            return "TMNOFLAGS";
        case TMONEPHASE:
            return "TMONEPHASE";
        case TMRESUME:
            return "TMRESUME";
        case TMSTARTRSCAN:
            return "TMSTARTRSCAN";
        case TMENDRSCAN:
            return "TMENDRSCAN";
        case TMSUCCESS:
            return "TMSUCCESS";
        case TMSUSPEND:
            return "TMSUSPEND";
        case XA_RDONLY:
            return "XA_RDONLY";
        default:
            return "" + Integer.toHexString(flag);
        }
    }

    public boolean equals(Object obj) {
        return false;
    }

    public int hashCode() {
        return 1;
    }
}
