/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
