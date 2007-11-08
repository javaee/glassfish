/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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

package oracle.toplink.essentials.internal.sequencing;

import java.util.*;
import oracle.toplink.essentials.sequencing.*;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.threetier.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.helper.ConcurrencyManager;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.exceptions.ConcurrencyException;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * SequencingManager is private to TopLink.
 * It provides most of sequencing functionality.
 * It's accessed by DatabaseSession through getSequencingHome() method.
 *
 * Here's the lifecycle of SequencingManager.
 * InitialState: SequencingManager doesn't exist.
 *   Action: SequencingManager created -> Not connected State.
 * State: Not connected.
 *        isConnected() returns false;
 *        getSequencingControl() could be used;
 *        getSequencing() == getSequencingServer() == getSequencingCallback() == null;
 *   Action: onConnect is called -> Connected State.
 * State: Connected.
 *        isConnected() returns true;
 *        getSequencingControl() could be used;
 *        getSequencing() could be used;
 *        in case ownwerSession is a ServerSession getSequencingServer() could be used;
 *   Action: onDisconnect is called -> Not connected State.
 *
 * Here's a sketch of SequencingManager architecture.
 * The main 4 objects comprising SessionManager are:
 *      valueGenarationPolicy;
 *      preallocationHandler;
 *      connectionHandler;
 *      state;
 *
 * That's how they evolve during lifetime of SequencingManager object:
 * Not connected State:
 *      preallocationHandler doesn't have any preallocated sequencing values.
 *      connectionHandler == null;
 *      state == null;
 *
 * Connected State:
 *      preallocationHandler may contain preallocated sequencing values.
 *      valueGenarationPolicy != null;
 *      state != null;
 *
 * The most important method of the class is onConnect():
 * that's where, using values of the attributes'(accessible through SequencingControl):
 *      shouldUseSeparateConnection;
 *      login;
 *      minPoolSize;
 *      maxPoolSize;
 * as well as boolean flags returned by valueGenerationPolicy methods:
 *      shouldAcquireValueAfterInsert();
 *      shouldUsePreallocation();
 *      shouldUseSeparateConnection();
 *      shouldUseTransaction();
 * one of implementors of inner interface State is created.
 *
 * Once in Connected State, neither changes to attributes, nor to returns of valueGenerationPolicy's
 * four should... methods can change the state object.
 * To change the state object, onDisconnect(), than onConnect() should be called.
 * There is no need to do it directly: each of the following methods
 * available through SequencingControl does that:
 *      setValueGenerationPolicy;
 *      setShouldUseNativeSequencing;
 *      setShouldUseTableSequencing;
 *      resetSequencing;
 */
class SequencingManager implements SequencingHome, SequencingServer, SequencingControl {
    private DatabaseSessionImpl ownerSession;
    private SequencingConnectionHandler connectionHandler;
    private PreallocationHandler preallocationHandler;
    private int whenShouldAcquireValueForAll;
    private Vector connectedSequences;
    boolean atLeastOneSequenceShouldUseTransaction;
    boolean atLeastOneSequenceShouldUsePreallocation;

    // state ids
    private static final int NOPREALLOCATION = 0;
    private static final int PREALLOCATION_NOTRANSACTION = 1;
    private static final int PREALLOCATION_TRANSACTION_NOACCESSOR = 2;
    private static final int PREALLOCATION_TRANSACTION_ACCESSOR = 3;
    private static final int NUMBER_OF_STATES = 4;
    private State[] states;
    private Hashtable locks;
    private SequencingCallback callback;
    private SequencingServer server;
    private Sequencing seq;
    private boolean shouldUseSeparateConnection;
    private Login login;
    private int minPoolSize;
    private int maxPoolSize;

    public SequencingManager(DatabaseSessionImpl ownerSession) {
        this.ownerSession = ownerSession;
    }

    protected DatabaseSessionImpl getOwnerSession() {
        return ownerSession;
    }

    protected void createConnectionHandler() {
        boolean isServerSession = getOwnerSession().isServerSession();

        if (getLogin() == null) {
            Login login;
            if (isServerSession) {
                login = ((ServerSession)getOwnerSession()).getReadConnectionPool().getLogin();
            } else {
                login = getOwnerSession().getDatasourceLogin();
            }
            setLogin((Login)login.clone());
        }

        if (getLogin() != null) {
            if (getLogin().shouldUseExternalTransactionController()) {
                throw ValidationException.invalidSequencingLogin();
            }
        }

        if (isServerSession) {
            ConnectionPool pool = null;
            if (getLogin().shouldUseExternalConnectionPooling()) {
                pool = new ExternalConnectionPool("sequencing", getLogin(), (ServerSession)getOwnerSession());
            } else {
                if ((getMinPoolSize() == 0) && (getMaxPoolSize() == 0)) {
                    setMinPoolSize(2);
                    setMaxPoolSize(2);
                }
                pool = new ConnectionPool("sequencing", getLogin(), getMinPoolSize(), getMaxPoolSize(), (ServerSession)getOwnerSession());
            }

            setConnectionHandler(new ServerSessionConnectionHandler(pool));

        } else {
            setConnectionHandler(new DatabaseSessionConnectionHandler(getOwnerSession(), getLogin()));

        }
    }

    public SequencingControl getSequencingControl() {
        return this;
    }

    protected void setSequencing(Sequencing sequencing) {
        this.seq = sequencing;
    }

    public Sequencing getSequencing() {
        return seq;
    }

    protected void setSequencingServer(SequencingServer server) {
        this.server = server;
    }

    public SequencingServer getSequencingServer() {
        return server;
    }

    protected void setSequencingCallback(SequencingCallback callback) {
        this.callback = callback;
    }

    public SequencingCallback getSequencingCallback() {
        return callback;
    }

    public boolean shouldUseSeparateConnection() {
        return shouldUseSeparateConnection;
    }

    public void setShouldUseSeparateConnection(boolean shouldUseSeparateConnection) {
        this.shouldUseSeparateConnection = shouldUseSeparateConnection;
    }

    public boolean isConnectedUsingSeparateConnection() {
        return isConnected() && (getConnectionHandler() != null);
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int size) {
        this.minPoolSize = size;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int size) {
        this.maxPoolSize = size;
    }

    public boolean isConnected() {
        return states != null;
    }

    // SequencingSetup
    protected SequencingConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    protected void setConnectionHandler(SequencingConnectionHandler handler) {
        this.connectionHandler = handler;
    }

    public Object getNextValue(Class cls) {
        return getNextValue(getOwnerSession(), cls);
    }

    public void initializePreallocated() {
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().initializePreallocated();
        }
    }

    public void initializePreallocated(String seqName) {
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().initializePreallocated(seqName);
        }
    }

    protected void setLocks(Hashtable locks) {
        this.locks = locks;
    }

    protected Hashtable getLocks() {
        return locks;
    }

    protected void acquireLock(String seqName) {
        ConcurrencyManager manager;
        synchronized (getLocks()) {
            manager = (ConcurrencyManager)getLocks().get(seqName);
            if (manager == null) {
                manager = new ConcurrencyManager();
                getLocks().put(seqName, manager);
            }
        }
        manager.acquire();
    }

    protected void releaseLock(String seqName) {
        ConcurrencyManager manager = (ConcurrencyManager)locks.get(seqName);
        manager.release();
    }

    protected Sequence getSequence(Class cls) {
        //** should check here that sequencing is used?
        String seqName = getOwnerSession().getDescriptor(cls).getSequenceNumberName();
        return getSequence(seqName);
    }

    protected void logDebugPreallocation(String seqName, Vector sequences) {
        if (getOwnerSession().shouldLog(SessionLog.FINEST, SessionLog.SEQUENCING)) {
            Object[] args = { seqName, new Integer(sequences.size()), sequences.firstElement(), sequences.lastElement() };
            getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_preallocation", args);
        }
    }

    protected void logDebugLocalPreallocation(AbstractSession writeSession, String seqName, Vector sequences, Accessor accessor) {
        if (writeSession.shouldLog(SessionLog.FINEST, SessionLog.SEQUENCING)) {
            Object[] args = { seqName, new Integer(sequences.size()), sequences.firstElement(), sequences.lastElement() };
            writeSession.log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_localPreallocation", args, accessor);
        }
    }

    static abstract class State {
        abstract Object getNextValue(Sequence sequence, AbstractSession writeSession);

        SequencingCallback getSequencingCallback() {
            return null;
        }

        public String toString() {
            String name = getClass().getName();
            return name.substring(name.lastIndexOf('$') + 1);
        }
    }

    // uses preallocation, uses Transaction, no separate connection.
    class Preallocation_Transaction_NoAccessor_State extends State implements SequencingCallback {
        protected Hashtable accessorToPreallocated = new Hashtable(20);

        SequencingCallback getSequencingCallback() {
            return this;
        }

        public void afterTransaction(Accessor accessor, boolean committed) {
            Hashtable localSequences = (Hashtable)accessorToPreallocated.get(accessor);
            if (localSequences != null) {
                if (committed) {
                    for (Enumeration enumtr = localSequences.keys(); enumtr.hasMoreElements();) {
                        String seqName = (String)enumtr.nextElement();
                        Vector localSequenceForName = (Vector)localSequences.get(seqName);
                        if (!localSequenceForName.isEmpty()) {
                            acquireLock(seqName);
                            getPreallocationHandler().setPreallocated(seqName, localSequenceForName);
                            // clear all localSequencesForName
                            localSequenceForName.clear();
                            releaseLock(seqName);
                        }
                    }
                }

                // remove localSequencing corresponding to the accessor from accessorToPreallocated
                accessorToPreallocated.remove(accessor);

                if (committed) {
                    getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_afterTransactionCommitted", null, accessor);
                } else {
                    getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_afterTransactionRolledBack", null, accessor);
                }
            }
        }

        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            Object sequenceValue;
            String seqName = sequence.getName();
            acquireLock(seqName);

            // keepLocked indicates whether the sequence lock should be kept for the whole duration of this method.
            // Of course the lock should be released in any case when the method returns or throws an exception.
            boolean keepLocked = false;

            Vector sequencesForName = (Vector)getPreallocationHandler().getPreallocated(seqName);
            try {
                if (!sequencesForName.isEmpty()) {
                    sequenceValue = sequencesForName.firstElement();
                    sequencesForName.removeElementAt(0);
                    return sequenceValue;
                } else {
                    if (!getOwnerSession().getDatasourceLogin().shouldUseExternalTransactionController() && (sequence.getPreallocationSize() > 1) && !writeSession.isInTransaction()) {
                        // To prevent several threads from sumultaneously allocating a separate bunch of
                        // sequencing numbers each. With keepLocked==true the first thread locks out others
                        // until it copies the obtained sequence numbers to the global storage.
                        // Note that this optimization possible only in non-jts case when there is no transaction,
                        // and makes sense only in case preallocation size > 1
                        writeSession.beginTransaction();//write accessor is set in begin
                        keepLocked = true;
                    }
                }
            } finally {
                if (!keepLocked) {
                    releaseLock(seqName);
                }
            }

            Accessor accessor;
            Vector localSequencesForName;
            if (!keepLocked) {
                writeSession.beginTransaction();//write accessor is set in begin
            }
            try {
                accessor = writeSession.getAccessor();
                Hashtable localSequences = (Hashtable)accessorToPreallocated.get(accessor);
                if (localSequences == null) {
                    localSequences = new Hashtable(20);
                    accessorToPreallocated.put(accessor, localSequences);
                }
                localSequencesForName = (Vector)localSequences.get(seqName);
                if ((localSequencesForName == null) || localSequencesForName.isEmpty()) {
                    localSequencesForName = sequence.getGeneratedVector(null, writeSession);
                    localSequences.put(seqName, localSequencesForName);
                    logDebugLocalPreallocation(writeSession, seqName, localSequencesForName, accessor);
                }
            } catch (RuntimeException ex) {
                if (keepLocked) {
                    releaseLock(seqName);
                }
                try {
                    // make sure to rollback the transaction we've begun
                    writeSession.rollbackTransaction();
                } catch (Exception rollbackException) {
                    // ignore rollback exception
                }

                // don't eat the original exception
                throw ex;
            }

            if (!keepLocked) {
                acquireLock(seqName);
            }
            try {
                try {
                    // commitTransaction may copy preallocated sequence numbers 
                    // from localSequences to preallocationHandler: that happens
                    // if it isn't a nested transaction, and afterTransaction method 
                    // is called.
                    // In this case:
                    // 1. localSequences corresponding to the accessor
                    //    has been removed from accessorToPreallocated;
                    // 2. All its members are empty (therefore localSequenceForName is empty).
                    writeSession.commitTransaction();
                } catch (DatabaseException ex) {
                    try {
                        // make sure to rollback the transaction we've begun
                        writeSession.rollbackTransaction();
                    } catch (Exception rollbackException) {
                        // ignore rollback exception
                    }

                    // don't eat the original exception
                    throw ex;
                }

                if (!localSequencesForName.isEmpty()) {
                    // localSeqencesForName is not empty, that means
                    // afterTransaction() has not been called.
                    sequenceValue = localSequencesForName.firstElement();
                    localSequencesForName.removeElementAt(0);
                    return sequenceValue;
                } else {
                    // localSeqencesForName is empty, that means
                    // afterTransaction() has been called.
                    // Note the lock before writeSession.commitTransaction() call:
                    // it insures that no other thread could have used seq. values copied
                    // from localSequencesForName to sequencesForName - so it shouldn't be empty.
                    // The only possibility for sequencesForName to be empty is a case of several threads
                    // using the same accessor simultaneously: while sequenceForName is empty,
                    // thread 1 uses localSequencesForName, thread 2 therefore comes here empty sequencesForName.
                    // There should be no concurrent writing through the same DatabaseSession
                    // or ClientSession.
                    try {
                        sequenceValue = sequencesForName.firstElement();
                    } catch (java.util.NoSuchElementException ex) {
                        throw ConcurrencyException.sequencingMultithreadThruConnection(String.valueOf(System.identityHashCode(accessor)));
                    }
                    sequencesForName.removeElementAt(0);
                    return sequenceValue;
                }
            } finally {
                releaseLock(seqName);
            }
        }
    }

    // Preallocation vs NoPreallocation; Transaction vs NoTransaction; Accessor vs NoAccessor
    class Preallocation_Transaction_Accessor_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            Object sequenceValue = null;
            String seqName = sequence.getName();
            acquireLock(seqName);
            try {
                Vector sequencesForName = (Vector)getPreallocationHandler().getPreallocated(seqName);
                if (sequencesForName.isEmpty()) {
                    Accessor accessor = null;
                    try {
                        // note that accessor.getLogin().shouldUseExternalTransactionController()
                        // should be set to false
                        accessor = getConnectionHandler().acquireAccessor();
                        accessor.beginTransaction(writeSession);
                        Vector sequences = sequence.getGeneratedVector(accessor, writeSession);
                        getPreallocationHandler().setPreallocated(seqName, sequences);
                        accessor.commitTransaction(writeSession);
                        logDebugPreallocation(seqName, sequences);
                    } catch (RuntimeException ex) {
                        sequencesForName.clear();
                        try {
                            // make sure to rollback the transaction we've begun
                            accessor.rollbackTransaction(writeSession);
                        } catch (Exception rollbackException) {
                            // ignore rollback exception
                        }

                        // don't eat the original exception
                        throw ex;
                    } finally {
                        getConnectionHandler().releaseAccessor(accessor);
                    }
                }
                sequenceValue = sequencesForName.firstElement();
                sequencesForName.removeElementAt(0);
            } finally {
                releaseLock(seqName);
            }
            return sequenceValue;
        }
    }

    // Preallocation vs NoPreallocation; Transaction vs NoTransaction; Accessor vs NoAccessor
    class Preallocation_NoTransaction_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            Object sequenceValue;
            String seqName = sequence.getName();
            acquireLock(seqName);
            try {
                Vector sequencesForName = (Vector)getPreallocationHandler().getPreallocated(seqName);
                if (sequencesForName.isEmpty()) {
                    Vector sequences = sequence.getGeneratedVector(null, writeSession);
                    getPreallocationHandler().setPreallocated(seqName, sequences);
                    logDebugPreallocation(seqName, sequences);
                }
                sequenceValue = sequencesForName.firstElement();
                sequencesForName.removeElementAt(0);
            } finally {
                releaseLock(seqName);
            }
            return sequenceValue;
        }
    }

    // Preallocation vs NoPreallocation; Transaction vs NoTransaction; Accessor vs NoAccessor
    class NoPreallocation_State extends State {
        public Object getNextValue(Sequence sequence, AbstractSession writeSession) {
            return sequence.getGeneratedValue(null, writeSession);
        }
    }

    public void resetSequencing() {
        if (isConnected()) {
            onDisconnect();
            onConnect();
        }
    }

    public void onConnect() {
        if (isConnected()) {
            return;
        }

        if (!getOwnerSession().getProject().usesSequencing()) {
            return;
        }

        onConnectAllSequences();

        boolean onExceptionDisconnectPreallocationHandler = false;
        boolean onExceptionDisconnectConnectionHandler = false;

        try {
            if (!shouldUseSeparateConnection()) {
                setConnectionHandler(null);
            } else if (atLeastOneSequenceShouldUseTransaction) {
                if (getConnectionHandler() == null) {
                    createConnectionHandler();
                }
                if (getConnectionHandler() != null) {
                    getConnectionHandler().onConnect();
                    onExceptionDisconnectConnectionHandler = true;
                }
            }

            if (atLeastOneSequenceShouldUsePreallocation) {
                if (getPreallocationHandler() == null) {
                    createPreallocationHandler();
                }
                getPreallocationHandler().onConnect();
                onExceptionDisconnectPreallocationHandler = true;
            }

            initializeStates();

        } catch (RuntimeException ex) {
            onDisconnectAllSequences();
            if (getConnectionHandler() != null) {
                if (onExceptionDisconnectConnectionHandler) {
                    getConnectionHandler().onDisconnect();
                }
                setConnectionHandler(null);
            }
            if (getPreallocationHandler() != null) {
                if (onExceptionDisconnectPreallocationHandler) {
                    getPreallocationHandler().onDisconnect();
                }
                clearPreallocationHandler();
            }
            throw ex;
        }
        if (atLeastOneSequenceShouldUsePreallocation) {
            setLocks(new Hashtable(20));
        }
        createSequencingCallback();
        if (getOwnerSession().isServerSession()) {
            setSequencingServer(this);
        }
        setSequencing(this);
        logDebugSequencingConnected();
    }

    public void onDisconnect() {
        if (!isConnected()) {
            return;
        }

        setSequencing(null);
        setSequencingServer(null);
        setSequencingCallback(null);
        setLocks(null);
        clearStates();

        if (getConnectionHandler() != null) {
            getConnectionHandler().onDisconnect();
            setConnectionHandler(null);
        }
        if (getPreallocationHandler() != null) {
            getPreallocationHandler().onDisconnect();
            clearPreallocationHandler();
        }
        onDisconnectAllSequences();
        getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_disconnected");
    }

    protected PreallocationHandler getPreallocationHandler() {
        return preallocationHandler;
    }

    protected void createPreallocationHandler() {
        preallocationHandler = new PreallocationHandler();
    }

    protected void clearPreallocationHandler() {
        preallocationHandler = null;
    }

    protected void onConnectAllSequences() {
        connectedSequences = new Vector();
        boolean shouldUseTransaction = false;
        boolean shouldUsePreallocation = false;
        boolean shouldAcquireValueAfterInsert = false;
        Iterator descriptors = getOwnerSession().getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            if (!descriptor.usesSequenceNumbers()) {
                continue;
            }
            String seqName = descriptor.getSequenceNumberName();
            Sequence sequence = getSequence(seqName);
            if (sequence == null) {
                sequence = new DefaultSequence(seqName);
                getOwnerSession().getDatasourcePlatform().addSequence(sequence);
            }
            if (connectedSequences.contains(sequence)) {
                continue;
            }
            try {
                if (sequence instanceof DefaultSequence && !connectedSequences.contains(getDefaultSequence())) {
                    getDefaultSequence().onConnect(getOwnerSession().getDatasourcePlatform());
                    connectedSequences.add(0, getDefaultSequence());
                    shouldUseTransaction |= getDefaultSequence().shouldUseTransaction();
                    shouldUsePreallocation |= getDefaultSequence().shouldUsePreallocation();
                    shouldAcquireValueAfterInsert |= getDefaultSequence().shouldAcquireValueAfterInsert();
                }
                sequence.onConnect(getOwnerSession().getDatasourcePlatform());
                connectedSequences.addElement(sequence);
                shouldUseTransaction |= sequence.shouldUseTransaction();
                shouldUsePreallocation |= sequence.shouldUsePreallocation();
                shouldAcquireValueAfterInsert |= sequence.shouldAcquireValueAfterInsert();
            } catch (RuntimeException ex) {
                // defaultSequence has to disconnect the last
                for (int i = connectedSequences.size() - 1; i >= 0; i--) {
                    try {
                        Sequence sequenceToDisconnect = (Sequence)connectedSequences.elementAt(i);
                        sequenceToDisconnect.onDisconnect(getOwnerSession().getDatasourcePlatform());
                    } catch (RuntimeException ex2) {
                        //ignore
                    }
                }
                connectedSequences = null;
                throw ex;
            }
        }

        if (shouldAcquireValueAfterInsert && !shouldUsePreallocation) {
            whenShouldAcquireValueForAll = AFTER_INSERT;
        } else if (!shouldAcquireValueAfterInsert && shouldUsePreallocation) {
            whenShouldAcquireValueForAll = BEFORE_INSERT;
        }
        atLeastOneSequenceShouldUseTransaction = shouldUseTransaction;
        atLeastOneSequenceShouldUsePreallocation = shouldUsePreallocation;
    }

    protected void onDisconnectAllSequences() {
        RuntimeException exception = null;

        // defaultSequence has to disconnect the last
        for (int i = connectedSequences.size() - 1; i >= 0; i--) {
            try {
                Sequence sequenceToDisconnect = (Sequence)connectedSequences.elementAt(i);
                sequenceToDisconnect.onDisconnect(getOwnerSession().getDatasourcePlatform());
            } catch (RuntimeException ex) {
                if (exception == null) {
                    exception = ex;
                }
            }
        }
        connectedSequences = null;
        whenShouldAcquireValueForAll = UNDEFINED;
        atLeastOneSequenceShouldUseTransaction = false;
        atLeastOneSequenceShouldUsePreallocation = false;
        if (exception != null) {
            throw exception;
        }
    }

    protected void initializeStates() {
        states = new State[NUMBER_OF_STATES];

        Iterator itConnectedSequences = connectedSequences.iterator();
        while (itConnectedSequences.hasNext()) {
            Sequence sequence = (Sequence)itConnectedSequences.next();
            State state = getState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            if (state == null) {
                createState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            }
        }
    }

    protected void clearStates() {
        states = null;
    }

    protected int getStateId(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        if (!shouldUsePreallocation) {
            // Non-Oracle native sequencing uses this state
            return NOPREALLOCATION;
        } else if (!shouldUseTransaction) {
            // Oracle native sequencing uses this state
            return PREALLOCATION_NOTRANSACTION;
        } else if (getConnectionHandler() == null) {
            // TableSequence and UnaryTableSequence in case there is no separate connection(s) available use this state
            return PREALLOCATION_TRANSACTION_NOACCESSOR;
        } else/*if(getConnectionHandler()!=null)*/
         {
            // TableSequence and UnaryTableSequence in case there is separate connection(s) available use this state
            return PREALLOCATION_TRANSACTION_ACCESSOR;
        }
    }

    protected State getState(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        return states[getStateId(shouldUsePreallocation, shouldUseTransaction)];
    }

    protected void createState(boolean shouldUsePreallocation, boolean shouldUseTransaction) {
        if (!shouldUsePreallocation) {
            // Non-Oracle native sequencing uses this state
            states[NOPREALLOCATION] = new NoPreallocation_State();
        } else if (!shouldUseTransaction) {
            // Oracle native sequencing uses this state
            states[PREALLOCATION_NOTRANSACTION] = new Preallocation_NoTransaction_State();
        } else if (getConnectionHandler() == null) {
            // TableSequence and UnaryTableSequence in case there is no separate connection(s) available use this state
            states[PREALLOCATION_TRANSACTION_NOACCESSOR] = new Preallocation_Transaction_NoAccessor_State();
        } else/*if(getConnectionHandler()!=null)*/
         {
            // TableSequence and UnaryTableSequence in case there is separate connection(s) available use this state
            states[PREALLOCATION_TRANSACTION_ACCESSOR] = new Preallocation_Transaction_Accessor_State();
        }
    }

    protected void createSequencingCallback() {
        Vector callbackVector = new Vector();
        for (int i = 0; i < NUMBER_OF_STATES; i++) {
            if (states[i] != null) {
                SequencingCallback callback = states[i].getSequencingCallback();
                if (callback != null) {
                    callbackVector.addElement(callback);
                }
            }
        }
        if (callbackVector.isEmpty()) {
            setSequencingCallback(null);
        } else if (callbackVector.size() == 1) {
            setSequencingCallback((SequencingCallback)callbackVector.firstElement());
        } else {
            setSequencingCallback(new SequencingCallbackContainer(callbackVector));
        }
    }

    static class SequencingCallbackContainer implements SequencingCallback {
        SequencingCallback[] callbackArray;

        SequencingCallbackContainer(Vector callbackVector) {
            callbackArray = new SequencingCallback[callbackVector.size()];
            callbackVector.copyInto(callbackArray);
        }

        public void afterTransaction(Accessor accessor, boolean committed) {
            for (int i = 0; i < callbackArray.length; i++) {
                callbackArray[i].afterTransaction(accessor, committed);
            }
        }
    }

    public Object getNextValue(AbstractSession writeSession, Class cls) {
        Sequence sequence = getSequence(cls);
        State state = getState(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
        return state.getNextValue(sequence, writeSession);
    }

    protected void logDebugSequencingConnected() {
        Vector[] sequenceVectors = new Vector[NUMBER_OF_STATES];
        Iterator itConnectedSequences = connectedSequences.iterator();
        while (itConnectedSequences.hasNext()) {
            Sequence sequence = (Sequence)itConnectedSequences.next();
            int stateId = getStateId(sequence.shouldUsePreallocation(), sequence.shouldUseTransaction());
            Vector v = sequenceVectors[stateId];
            if (v == null) {
                v = new Vector();
                sequenceVectors[stateId] = v;
            }
            v.addElement(sequence);
        }
        for (int i = 0; i < NUMBER_OF_STATES; i++) {
            Vector v = sequenceVectors[i];
            if (v != null) {
                getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequencing_connected", states[i]);
                for (int j = 0; j < v.size(); j++) {
                    Sequence sequence = (Sequence)v.elementAt(j);
                    Object[] args = { sequence.getName(), Integer.toString(sequence.getPreallocationSize()),
                            Integer.toString(sequence.getInitialValue())};
                    getOwnerSession().log(SessionLog.FINEST, SessionLog.SEQUENCING, "sequence_without_state", args);
                }
            }
        }
    }

    public int getPreallocationSize() {
        return getDefaultSequence().getPreallocationSize();
    }

    public int getInitialValue() {
        return getDefaultSequence().getInitialValue();
    }
    
    public boolean shouldAcquireValueAfterInsert(Class cls) {
        return getSequence(cls).shouldAcquireValueAfterInsert();
    }

    public boolean shouldOverrideExistingValue(Class cls, Object existingValue) {
        return getSequence(cls).shouldOverrideExistingValue(existingValue);
    }

    public int whenShouldAcquireValueForAll() {
        return whenShouldAcquireValueForAll;
    }

    protected Sequence getDefaultSequence() {
        return getOwnerSession().getDatasourcePlatform().getDefaultSequence();
    }

    protected Sequence getSequence(String seqName) {
        return getOwnerSession().getDatasourcePlatform().getSequence(seqName);
    }
}
