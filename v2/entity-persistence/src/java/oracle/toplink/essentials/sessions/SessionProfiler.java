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
package oracle.toplink.essentials.sessions;

import oracle.toplink.essentials.queryframework.*;

/**
 * <p><b>Purpose</b>: This interface defines the link between the Session and the PerformanceProfiler tool.
 * It is provide to decouple the session from tools and to allow other profilers to register with the session.
 *
 * @author James Sutherland
 */
public interface SessionProfiler {
    //dms sensor weight constants
    public static final int NONE = 0;
    public static final int NORMAL = 5;
    public static final int HEAVY = 10;
    public static final int ALL = Integer.MAX_VALUE;

    //nouns type display name 
    public static final String TopLinkRootNoun = "/TopLink";
    public static final String SessionNounType = "TopLink Session";
    public static final String TransactionNounType = "TopLink Transaction";
    public static final String QueryNounType = "Toplink Queries";
    public static final String RcmNounType = "Toplink RCM";
    public static final String ConnectionNounType = "TopLink Connections";
    public static final String CacheNounType = "TopLink Cache";
    public static final String MiscellaneousNounType = "TopLink Miscellaneous";

    //dms sensors display name
    public static final String SessionName = "SessionName";
    public static final String LoginTime = "loginTime";
    public static final String ClientSessionCreated = "ClientSession";
    public static final String UowCreated = "UnitOfWork";
    public static final String UowCommit = "UnitOfWorkCommits";
    public static final String UowRollbacks = "UnitOfWorkRollbacks";
    public static final String OptimisticLockException = "OptimisticLocks";
    public static final String ReadQuery = "ReadQueries";
    public static final String WriteQuery = "WriteQueries";
    public static final String DeleteQuery = "DeleteQueries";
    public static final String RcmStatus = "RCMStatus";
    public static final String RcmReceived = "MessagesReceived";
    public static final String RcmSent = "MessagesSent";
    public static final String RemoteChangeSet = "RemoteChangeSets";
    public static final String TlConnects = "ConnectCalls";
    public static final String TlDisconnects = "DisconnectCalls";
    public static final String CachedObjects = "CachedObjects";
    public static final String CacheHits = "CacheHits";
    public static final String CacheMisses = "CacheMisses";
    public static final String ChangeSetsProcessed = "ChangesProcessed";
    public static final String ChangeSetsNotProcessed = "ChangesNotProcessed";
    public static final String DescriptorEvent = "DescriptorEvents";
    public static final String SessionEvent = "SessionEvents";
    public static final String ConnectionInUse = "ConnectionsInUse";
    public static final String QueryPreparation = "QueryPreparation";
    public static final String SqlGeneration = "SqlGeneration";
    public static final String DatabaseExecute = "DatabaseExecute";
    public static final String SqlPrepare = "SqlPrepare";
    public static final String RowFetch = "RowFetch";
    public static final String ObjectBuilding = "ObjectBuilding";
    public static final String MergeTime = "MergeTime";
    public static final String UnitOfWorkRegister = "UnitOfWorkRegister";
    public static final String DistributedMergeDmsDisplayName = "DistributedMerge";
    public static final String Sequencing = "Sequencing";
    public static final String Caching = "Caching";
    public static final String ConnectionManagement = "ConnectionManagement";
    public static final String LoggingDMSDisPlayName = "Logging";
    public static final String JtsBeforeCompletion = "TXBeforeCompletion";
    public static final String JtsAfterCompletion = "TXAfterCompletion";

    //Token used by existed default performance profiler 
    public static final String Register = "register";
    public static final String Merge = "merge";
    public static final String AssignSequence = "assign sequence";
    public static final String DistributedMerge = "distributed merge";
    public static final String DeletedObject = "deleted object";
    public static final String Wrapping = "wrapping";
    public static final String Logging = "logging";
    public static final String OBJECT_BUILDING = "object building";
    public static final String SQL_GENERATION = "sql generation";
    public static final String QUERY_PREPARE = "query prepare";
    public static final String STATEMENT_EXECUTE = "sql execute";
    public static final String ROW_FETCH = "row fetch";
    public static final String SQL_PREPARE = "sql prepare";
    public static final String TRANSACTION = "transactions";
    public static final String CONNECT = "connect";
    public static final String CACHE = "cache";

    /**
     * INTERNAL:
     * End the operation timing.
     */
    public void endOperationProfile(String operationName);

    /**
     * INTERNAL:
     * Finish a profile operation if profiling.
     * This assumes the start operation preceeds on the stack.
     * The session must be passed to allow units of work etc. to share their parents profiler.
     *
     * @return the execution result of the query.
     */
    public Object profileExecutionOfQuery(DatabaseQuery query, oracle.toplink.essentials.internal.sessions.AbstractRecord row, oracle.toplink.essentials.internal.sessions.AbstractSession session);

    /**
     * INTERNAL:
     * Set the sesssion.
     */
    public void setSession(Session session);

    /**
     * INTERNAL:
     * Start the operation timing.
     */
    public void startOperationProfile(String operationName);

    /**
     * INTERNAL:
     * Update the value of the State sensor.(DMS)
     */
    public void update(String operationName, Object value);

    /**
     * INTERNAL:
     * Increase DMS Event sensor occurrence.(DMS)
     */
    public void occurred(String operationName);

    /**
     * INTERNAL:
     * Set DMS sensor weight(DMS)
     */
    public void setProfileWeight(int weight);

    /**
     * INTERNAL:
     * Return DMS sensor weight(DMS)
     */
    public int getProfileWeight();

    /**
     * INTERNAL:
     * Initialize TopLink noun tree(DMS)
     */
    public void initialize();
}
