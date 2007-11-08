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

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.sun.enterprise.ee.web.authenticator.*;

import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.security.web.SingleSignOnEntry;

import org.apache.catalina.*;
import org.apache.catalina.session.*;

import com.sun.appserv.util.cache.BaseCache;
import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class ReplicationAttributeStore extends ReplicationStore implements HAStorePoolElement {
    
    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);    
    
    protected final static String MODE_WEB 
        = ReplicationState.MODE_WEB;
    protected final static String MESSAGE_QUERY_RESULT 
        = ReplicationState.MESSAGE_QUERY_RESULT;
    
    /** Creates a new instance of ReplicationAttributeStore */
    public ReplicationAttributeStore() {
        setLogLevel();
    }
    
    // HAStorePoolElement methods begin
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void valveSave(Session session) throws IOException {
        HASession haSess = (HASession)session;
        if( haSess.isPersistent() && !haSess.isDirty() ) {
            this.updateLastAccessTime(session);
        } else {
            this.doValveSave(session);
            haSess.setPersistent(true);
        }
        haSess.setDirty(false);        
    } 
    
    // Store method begin
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void save(Session session) throws IOException {        
        HASession haSess = (HASession)session;
        if( haSess.isPersistent() && !haSess.isDirty() ) {
            this.updateLastAccessTime(session);
        } else {
            this.doSave(session);
            haSess.setPersistent(true);
        }
        haSess.setDirty(false);        
    }
    
    //methods copied from ReplicationStore
    //must be modified for modified-attribute usage
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void doValveSave(Session session) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>doValveSave:valid =" + ((StandardSession)session).getIsValid());
            _logger.fine("ReplicationAttributeStore>>valveSave:ssoId=" + ((HASession)session).getSsoId());                    
        }         
        //System.out.println("ReplicationAttributeStore>>doValveSave:valid =" + ((StandardSession)session).getIsValid());
        //System.out.println("ReplicationAttributeStore>>valveSave:ssoId=" + ((HASession)session).getSsoId());        
        // begin 6470831 do not save if session is not valid
        if( !((StandardSession)session).getIsValid() ) {
            return;
        }
        // end 6470831        
        ModifiedAttributeHASession modAttrSession
                = (ModifiedAttributeHASession)session;
        String userName = "";
        if(session.getPrincipal() !=null){
            userName = session.getPrincipal().getName();
            ((BaseHASession)session).setUserName(userName);
        }        
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getCompositeBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>save: replicator: " + replicator);                    
        }         
        //System.out.println("ReplicationAttributeStore>>save: replicator: " + replicator);
        CompositeMetadata compositeMetadata 
            = createCompositeMetadata(modAttrSession);
                
        try {        
            replicator.save(session.getIdInternal(), //id
                    compositeMetadata);  //CompositeMetadata 
            modAttrSession.resetAttributeState();
            //postSaveUpdate(modAttrSession);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }    
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void doSave(Session session) throws IOException {
        // begin 6470831 do not save if session is not valid
        if( !((StandardSession)session).getIsValid() ) {
            return;
        }
        // end 6470831        
        ModifiedAttributeHASession modAttrSession
                = (ModifiedAttributeHASession)session;
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getCompositeBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>doSave: replicator: " + replicator);                    
        }         
        //System.out.println("ReplicationAttributeStore>>doSave: replicator: " + replicator);
        CompositeMetadata compositeMetadata 
            = createCompositeMetadata(modAttrSession);
                
        try {        
            replicator.save(session.getIdInternal(), //id
                    compositeMetadata);  //CompositeMetadata 
            modAttrSession.resetAttributeState();
            //postSaveUpdate(modAttrSession);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    } 
    
    /**
    * Load and return the Session associated with the specified session
    * identifier from this Store, without removing it.  If there is no
    * such stored Session, return <code>null</code>.
    *
    * @param id Session identifier of the session to load
    *
    * @exception ClassNotFoundException if a deserialization error occurs
    * @exception IOException if an input/output error occurs
    */
    public synchronized Session load(String id) throws ClassNotFoundException, IOException {
        return load(id, null);
    }     
    
    /**
    * Load and return the Session associated with the specified session
    * identifier from this Store, without removing it.  If there is no
    * such stored Session, return <code>null</code>.
    *
    * @param id Session identifier of the session to load
    *
    * @exception ClassNotFoundException if a deserialization error occurs
    * @exception IOException if an input/output error occurs
    */
    public synchronized Session load(String id, String version) throws ClassNotFoundException, IOException {
        //System.out.println("ReplicationAttributeStore>>load called:dumping stack:");
        //Thread.dumpStack();
        Session result = null;
        if(id == null) {
            return result;
        }
        ReplicationManagerBase repMgr =
            (ReplicationManagerBase)this.getManager();
        //ReplicationState localCachedState = repMgr.getFromReplicationCache(id);
        ReplicationState localCachedState = repMgr.transferFromReplicationCache(id);         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>load:localCachedState=" + localCachedState);                      
        } 
        //check if we got a hit from our own replica cache
        //and if so return it immediately
        if(version != null && localCachedState != null) {
            long versionLong = parseLong(version);
            if(localCachedState.getVersion() == versionLong) {
                return (this.getSession(localCachedState));
            }            
        }        
        //System.out.println("ReplicationAttributeStore>>load:localCachedState=" + localCachedState);               
        ReplicationState broadcastResultState = findSessionViaBroadcast(id, version);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>load:broadcastResultState from broadcast=" + broadcastResultState);                       
        }         
        //System.out.println("ReplicationAttributeStore>>load:broadcastResultState from broadcast=" + broadcastResultState);
        if(broadcastResultState != null) {
            //System.out.println("ReplicationAttributeStore>>load:broadcastResultState VERSION=" + broadcastResultState.getVersion());
            //System.out.println("ReplicationAttributeStore>>load:broadcastResultState STATE=" + broadcastResultState.getState());
            //System.out.println("ReplicationAttributeStore>>load:broadcastResultState TRUNK_STATE=" + broadcastResultState.getTrunkState());
        }
        ReplicationState bestState 
            = getBestResult(localCachedState, broadcastResultState);
        //FIXME uncomment out only for diagnostic
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>load:bestState=" + bestState);                       
        }         
        //System.out.println("ReplicationAttributeStore>>load:bestState=" + bestState);
        if(bestState != null) {
            //System.out.println("ReplicationAttributeStore>>load:bestStateVERSION=" + bestState.getVersion());
            //System.out.println("ReplicationAttributeStore>>load:bestStateSTATE=" + bestState.getState());
            //System.out.println("ReplicationAttributeStore>>load:bestStateTRUNK_STATE=" + bestState.getTrunkState());            
        }
        //FIXME end uncomment out only for diagnostic

        if(bestState != null && bestState.getState() != null && bestState.getTrunkState() != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationAttributeStore>>load:before deserializing bestState:ver=" + bestState.getVersion());                       
            }             
            //System.out.println("ReplicationAttributeStore>>load:before deserializing bestState:ver=" + bestState.getVersion());
            result = this.getSession(bestState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationAttributeStore>>load:after deserializing session:ver=" + ((HASession)result).getVersion());                       
            }             
            //System.out.println("ReplicationAttributeStore>>load:after deserializing session:ver=" + ((HASession)result).getVersion());
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>load:id " + id + " session: " + result);                       
        }         
        //System.out.println("ReplicationAttributeStore>>load:id " + id + " session: " + result);
        if(result != null) {
            ((HASession)result).setDirty(false);
            ((HASession)result).setPersistent(false);
        }
        return result;
    } 
    
    private ReplicationState findSessionViaBroadcast(String id, String version) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>findSessionViaBroadcast");                       
        }          
        //System.out.println("ReplicationAttributeStore>>findSessionViaBroadcast");
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getCompositeBackingStore();
        JxtaBackingStoreImpl jxtaReplicator = null;
        if(replicator instanceof JxtaBackingStoreImpl) {
            jxtaReplicator = (JxtaBackingStoreImpl)replicator;
        }        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>findSessionViaBroadcast: replicator: " + replicator);                       
        }        
        //System.out.println("ReplicationAttributeStore>>findSessionViaBroadcast: replicator: " + replicator);        
        
        CompositeMetadata queryResult = null;
        try {
            //queryResult = (CompositeMetadata)replicator.load(id);
            //use version aware load if possible
            if(jxtaReplicator != null && version != null) {
                queryResult = (CompositeMetadata)jxtaReplicator.load(id, version);
            } else {
                queryResult = (CompositeMetadata)replicator.load(id);
            }            
        } catch (BackingStoreException ex) {
            //FIXME log this
        }
        if(queryResult == null) {
            return null;
        }
        //queryResult.getExtraParam() here is ssoid
        ReplicationState result = createReplicationStateFrom(id, queryResult, MESSAGE_QUERY_RESULT);
        return result;
    }
    
    /**
    * Given a byte[] containing session data, return a session
    * object
    * note we use the trunkState to get the basic session
    * this over-rides the inherited getSession method behavior
    *
    * @param state
    *   The byte[] with the session data
    *
    * @return
    *   A newly created session for the given session data, and associated
    *   with this Manager
    */
    protected Session getSession(ReplicationState replicationState) 
        throws IOException, ClassNotFoundException 
    {
        byte[] state = replicationState.getTrunkState();
        Session _session = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        Loader loader = null;    
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();
        java.security.Principal pal=null; //MERGE chg added
        String ssoId = null;
        long version = 0L;
        IOUtilsCaller utilsCaller = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            //Get the username, ssoId from replicationState
            String id = (String)replicationState.getId();
            //ssoId = replicationState.getSsoId();
            ssoId = replicationState.getExtraParam();
            version = replicationState.getVersion();
            //debug("ReplicationStore.getSession()  id="+id+"  username ="+username+";");    

            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("loaded session from replicationstore, length = "+state.length);
            }
            if (container != null) {
                loader = container.getLoader();
            }

            if (loader != null) {
                classLoader = loader.getClassLoader();
            }
            
            //Bug 4832603 : EJB Reference Failover
            //HERCULES FIXME: for now reverting back
            //need to look at new EJBUtils and related serialization code
            /*
          if (classLoader != null) {
            ois = new CustomObjectInputStream(bis, classLoader);
          }
          else {
            ois = new ObjectInputStream(bis);
          }
          
            //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
            //end - Bug 4832603  
             */          
            if (classLoader != null) {
                if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                    try {
                        ois = utilsCaller.createObjectInputStream(bis, true, classLoader);
                    } catch (Exception ex) {}
                }
            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            
            if(ois != null) {
                try {
                    _session = readSession(manager, ois);
                } 
                finally {
                    if (ois != null) {
                        try {
                            ois.close();
                            bis = null;
                        }
                        catch (IOException e) {
                        }
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("getSession :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getSession IOException :"+e.getMessage());
            throw e;
        }
        String username = ((HASession)_session).getUserName(); 
        if((username !=null) && (!username.equals("")) && _session.getPrincipal() == null) {
            if (_debug > 0) {
                debug("Username retrieved is "+username);
            }
            pal = ((com.sun.web.security.RealmAdapter)container.getRealm()).createFailOveredPrincipal(username);
            if (_debug > 0) {
                debug("principal created using username  "+pal);
            }
            if(pal != null) {
                _session.setPrincipal(pal);
                if (_debug > 0) {
                    debug("getSession principal="+pal+" was added to session="+_session); 
                }                
            }            
        }
        //--SRI        
        
        _session.setNew(false);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>ssoId=" + ssoId);                       
        }        
        //System.out.println("ReplicationAttributeStore>>ssoId=" + ssoId);
        ((BaseHASession)_session).setSsoId(ssoId);        
        if((ssoId !=null) && (!ssoId.equals("")))
            associate(ssoId, _session);
        ((HASession)_session).setVersion(version);
        ((HASession)_session).setDirty(false);
        
        //now load entries from deserialized entries collection
        ((ModifiedAttributeHASession)_session).clearAttributeStates();
        byte[] entriesState = replicationState.getState();
        Collection entries = null;
        if(entriesState != null) {
            entries = this.deserializeStatesCollection(entriesState);
            loadAttributes((ModifiedAttributeHASession)_session, entries);
        }
        return _session;
    }
    
    //FIXME might not be needed - can make a no-ope
    protected void associate(String ssoId, Session _session) {
        if (_debug > 0) {
            debug("Inside associate() -- HAStore");
        }
        Container parent = manager.getContainer();
        SingleSignOn sso = null;
        while ((sso == null) && (parent != null)) {
            if (_debug > 0) {
                 debug("Inside associate()  while loop -- HAStore");
            }
        if (!(parent instanceof Pipeline)) {
            if (_debug > 0) {
                 debug("Inside associate()  parent instanceof Pipeline -- HAStore");
            }
            parent = parent.getParent();
            continue;
        }
        Valve valves[] = ((Pipeline) parent).getValves();
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof SingleSignOn) {
                 if (_debug > 0) {
                    debug("Inside associate()  valves[i] instanceof SingleSignOn -- HAStore");
                 }
                 sso = (SingleSignOn) valves[i];
                 break;
             }
        }
        if (sso == null)
            parent = parent.getParent();
        }
        if (sso != null) {
            if (_debug > 0) {
                debug("Inside associate() sso != null");
            }
            //SingleSignOnEntry ssoEntry = ((ReplicationSingleSignOn)sso).lookup(ssoId);
            SingleSignOnEntry ssoEntry = ((ReplicationSingleSignOn)sso).lookupEntry(ssoId);
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Inside associate() ssoEntry = "+ssoEntry);
                }
                if(ssoEntry!=null)
                    ssoEntry.addSession(sso, _session);
        }

    }   
    
    //metadata related
    
    private void postSaveUpdate(ModifiedAttributeHASession modAttrSession) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>postSaveUpdate");                       
        }
        ArrayList addedAttrs = modAttrSession.getAddedAttributes();
        ArrayList modifiedAttrs = modAttrSession.getModifiedAttributes();
        ArrayList deletedAttrs = modAttrSession.getDeletedAttributes();
        //printAttrList("ADDED", addedAttrs);
        //printAttrList("MODIFIED", modifiedAttrs);
        //printAttrList("DELETED", deletedAttrs);

        postProcessSetAttrStates(modAttrSession, addedAttrs);
        postProcessSetAttrStates(modAttrSession, modifiedAttrs);
        
    }
    
    private void postProcessSetAttrStates(ModifiedAttributeHASession modAttrSession, ArrayList attrsList) {
        for(int i=0; i<attrsList.size(); i++) {
            String nextStateName = (String)attrsList.get(i);
            modAttrSession.setAttributeStatePersistent(nextStateName, true);
            modAttrSession.setAttributeStateDirty(nextStateName, false);
        }
    }
    
    private CompositeMetadata createCompositeMetadata(ModifiedAttributeHASession modAttrSession) {
        
        byte[] trunkState = null;
        try {
            trunkState = this.getByteArray(modAttrSession);
        } catch(IOException ex) {
            //no op
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>createCompositeMetadata:trunkState=" + trunkState);                       
        }         
        //System.out.println("ReplicationAttributeStore>>createCompositeMetadata:trunkState=" + trunkState);
        
        ArrayList entries = new ArrayList();
        ArrayList addedAttrs = modAttrSession.getAddedAttributes();
        ArrayList modifiedAttrs = modAttrSession.getModifiedAttributes();
        ArrayList deletedAttrs = modAttrSession.getDeletedAttributes();
        //printAttrList("ADDED", addedAttrs);
        //printAttrList("MODIFIED", modifiedAttrs);
        //printAttrList("DELETED", deletedAttrs);
        
        addToEntries(modAttrSession, entries, 
                AttributeMetadata.Operation.ADD, addedAttrs);  
        addToEntries(modAttrSession, entries, 
                AttributeMetadata.Operation.UPDATE, modifiedAttrs);
        entries = addToEntries(modAttrSession, entries, 
                AttributeMetadata.Operation.DELETE, deletedAttrs);

        CompositeMetadata result 
            = new CompositeMetadata(modAttrSession.getVersion(),
                modAttrSession.getLastAccessedTimeInternal(),
                modAttrSession.getMaxInactiveInterval(),
                entries,
                trunkState,
                modAttrSession.getSsoId()); //ssoId is the extraParam here
        return result;
    }
    
    private void printAttrList(String attrListType, ArrayList attrList) {
        System.out.println("AttributeType = " + attrListType + "\n");
        String nextAttrName = null;
        for(int i=0; i<attrList.size(); i++) {
            nextAttrName = (String)attrList.get(i);
            System.out.println("attribute[" + i + "]=" + nextAttrName + "\n");
        }
    }
    
    private ArrayList addToEntries(ModifiedAttributeHASession modAttrSession,
            ArrayList entries, AttributeMetadata.Operation op, ArrayList attrList) {
        String nextAttrName = null;
        Object nextAttrValue = null;
        byte[] nextValue = null;
        for(int i=0; i<attrList.size(); i++) {
            nextAttrName = (String)attrList.get(i);
            nextAttrValue = ((StandardSession) modAttrSession).getAttribute(nextAttrName);
            nextValue = null;
            try {
                nextValue = getByteArray(nextAttrValue);
            } catch (IOException ex) {}
            AttributeMetadata nextAttrMetadata 
                = new AttributeMetadata(nextAttrName, op, nextValue);
            entries.add(nextAttrMetadata);
        } 
        return entries;
    }
    
    /**
    * Create an byte[] for the session that we can then pass to
    * the HA Store.
    *
    * @param attributeValue
    *   The attribute value we are serializing
    *
    */
    protected byte[] getByteArray(Object attributeValue)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        IOUtilsCaller utilsCaller = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
              //oos = EJBUtils.getOutputStream(new BufferedOutputStream(bos), true);	
            //end - Bug 4832603 
            
            if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                try {
                    oos = utilsCaller.createObjectOutputStream(new BufferedOutputStream(bos), true);
                } catch (Exception ex) {}
            }
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }            
            oos.writeObject(attributeValue);
            oos.close();
            oos = null;

            obs = bos.toByteArray();
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    }
    
    /**
    * Given a byte[] containing session data, return a session
    * object
    *
    * @param state
    *   The byte[] with the session attribute data
    *
    * @return
    *   A newly created object for the given session attribute data
    */
    protected Object getAttributeValue(byte[] state) 
        throws IOException, ClassNotFoundException 
    {
        Object attributeValue = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        Loader loader = null;    
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();
        IOUtilsCaller utilsCaller = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            if (container != null) {
                loader = container.getLoader();
            }

            if (loader != null) {
                classLoader = loader.getClassLoader();
            }
            
            //Bug 4832603 : EJB Reference Failover
            //HERCULES FIXME: for now reverting back
            //need to look at new EJBUtils and related serialization code
            /*
          if (classLoader != null) {
            ois = new CustomObjectInputStream(bis, classLoader);
          }
          else {
            ois = new ObjectInputStream(bis);
          }
          
            //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
            //end - Bug 4832603  
             */          
            if (classLoader != null) {
                if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                    try {
                        ois = utilsCaller.createObjectInputStream(bis, true, classLoader);
                    } catch (Exception ex) {}
                }
            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            
            if(ois != null) {
                try {
                    attributeValue = ois.readObject();
                } 
                finally {
                    if (ois != null) {
                        try {
                            ois.close();
                            bis = null;
                        }
                        catch (IOException e) {
                        }
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("getAttributeValue :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getAttributeValue IOException :"+e.getMessage());
            throw e;
        }      

        return attributeValue;
    }
    
    //new serialization code for Collection
    /**
    * Create an byte[] for the session that we can then pass to
    * the HA Store.
    *
    * @param attributeValue
    *   The attribute value we are serializing
    *
    */
    protected byte[] getByteArrayFromCollection(Collection entries)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        IOUtilsCaller utilsCaller = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
              //oos = EJBUtils.getOutputStream(new BufferedOutputStream(bos), true);	
            //end - Bug 4832603 
            
            if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                try {
                    oos = utilsCaller.createObjectOutputStream(new BufferedOutputStream(bos), true);
                } catch (Exception ex) {}
            }
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }
            //first write out the entriesSize
            int entriesSize = entries.size();
            oos.writeObject(Integer.valueOf(entriesSize));
            //then write out the entries
            Iterator it = entries.iterator();
            while(it.hasNext()) {
                oos.writeObject(it.next());
            }
            oos.close();
            oos = null;

            obs = bos.toByteArray();
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    }
    
    /**
    * Given a byte[] containing session data, return a session
    * object
    *
    * @param state
    *   The byte[] with the session attribute data
    *
    * @return
    *   A newly created object for the given session attribute data
    */
    protected Object getAttributeValueCollection(byte[] state) 
        throws IOException, ClassNotFoundException 
    {
        Collection attributeValueList = new ArrayList();
        Object attributeValue = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        Loader loader = null;    
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();
        IOUtilsCaller utilsCaller = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            if (container != null) {
                loader = container.getLoader();
            }

            if (loader != null) {
                classLoader = loader.getClassLoader();
            }
            
            //Bug 4832603 : EJB Reference Failover
            //HERCULES FIXME: for now reverting back
            //need to look at new EJBUtils and related serialization code
            /*
          if (classLoader != null) {
            ois = new CustomObjectInputStream(bis, classLoader);
          }
          else {
            ois = new ObjectInputStream(bis);
          }
          
            //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
            //end - Bug 4832603  
             */          
            if (classLoader != null) {
                if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                    try {
                        ois = utilsCaller.createObjectInputStream(bis, true, classLoader);
                    } catch (Exception ex) {}
                }
            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            if(ois != null) {
                try {
                    //first get List size
                    Object whatIsIt = ois.readObject();
                    //System.out.println("first obj: " + whatIsIt);
                    int entriesSize = 0;
                    if(whatIsIt instanceof Integer) {
                        entriesSize = ((Integer)whatIsIt).intValue();
                    }
                    //int entriesSize = ((Integer) ois.readObject()).intValue();
                    //System.out.println("entriesSize = " + entriesSize);
                    //attributeValueList = new ArrayList(entriesSize);
                    for (int i = 0; i < entriesSize; i++) {
                        Object nextAttributeValue = ois.readObject();
                        attributeValueList.add(nextAttributeValue);
                    }
                } 
                finally {
                    if (ois != null) {
                        try {
                            ois.close();
                            bis = null;
                        }
                        catch (IOException e) {
                        }
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("getAttributeValue :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getAttributeValue IOException :"+e.getMessage());
            throw e;
        }      

        return attributeValueList;
    }    
    
    //end new serialization code for Collection

    /**
    * Given a session, load its attributes
    *
    * @param session
    *   The session (header info only) having its attributes loaded
    *
    * @param attributeList
    *   The List<AttributeMetadata> list of loaded attributes
    *
    * @return
    *   A newly created object for the given session attribute data
    */    
    protected void loadAttributes(ModifiedAttributeHASession modifiedAttributeSession, 
            Collection attributeList) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in loadAttributes -- ReplicationAttributeStore : session id=" + modifiedAttributeSession.getIdInternal());
        }

        String thisAttrName = null;
        AttributeMetadata.Operation thisAttrOp = null;
        Object thisAttrVal = null;
        Iterator it = attributeList.iterator();
        while (it.hasNext()) { 
            AttributeMetadata nextAttrMetadata = (AttributeMetadata)it.next();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN loadAttributes:while loop");
            }
            
            thisAttrName = nextAttrMetadata.getAttributeName();
            thisAttrOp = nextAttrMetadata.getOperation();
            byte[] nextAttrState = nextAttrMetadata.getState();
            thisAttrVal = null;
            try { 
                thisAttrVal = getAttributeValue(nextAttrState);
            } catch (ClassNotFoundException ex1) {
            } catch (IOException ex2) {}
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Attr retrieved======" + thisAttrName);
            }

            if(thisAttrVal != null) { //start if
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Setting Attribute: " + thisAttrName);
                }
                modifiedAttributeSession.setAttribute(thisAttrName, thisAttrVal);
                modifiedAttributeSession.setAttributeStatePersistent(thisAttrName, false);
                modifiedAttributeSession.setAttributeStateDirty(thisAttrName, false);
            } //end if
        } //end while 
    } 
    
    //FIXME remove not used
    ModifiedAttributeHASession createSession(String id, CompositeMetadata compositeMetadata) {
        ModifiedAttributeHASession theSession 
            = (ModifiedAttributeHASession) ((ReplicationManagerBase)manager).createSession();
        
        theSession.setId(id);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSession -- ReplicationAttributeStore : id =" + id);
        }
        theSession.setValid(true);
        theSession.setVersion(compositeMetadata.getVersion());
        theSession.setMaxInactiveInterval((int)compositeMetadata.getMaxInactiveInterval());
        theSession.setLastAccessedTime(compositeMetadata.getLastAccessTime());
        theSession.setSsoId(compositeMetadata.getExtraParam());  //extraParam is ssoId
        loadAttributes(theSession, compositeMetadata.getEntries());
        return theSession;
    }
    
    /**
    * create a ReplicationState representing compositeMetadata
    *
    * @param id
    *   The session id
    *
    * @param compositeMetadata
    *   The CompositeMetadata
    *
    * @return
    *   A newly created ReplicationState object for the given compositeMetadata
    */ 
    private ReplicationState createReplicationStateFrom(String id, CompositeMetadata compositeMetadata, String command) {
        ReplicationState result 
            = new ReplicationState(MODE_WEB,    //mode
                id,                             //id
                this.getApplicationId(),        //appid
                compositeMetadata.getVersion(), //version
                compositeMetadata.getLastAccessTime(), //lastAccess
                compositeMetadata.getMaxInactiveInterval(), //maxInactive
                compositeMetadata.getExtraParam(), //extraParam
                null,                               //queryResult
                null,                               //instanceName
                command,                               //command
                this.serializeStatesCollection(compositeMetadata.getEntries()), //data 
                compositeMetadata.getState());          //trunkData
        return result;
    }
    
    private byte[] serializeStatesCollection(Collection entries) {
        byte[] result = null;
        try {
            result = getByteArrayFromCollection(entries);
        } catch (IOException ex) {} 
        return result;
    }
    
    private byte[] serializeStatesCollectionPrevious(Collection entries) {
        byte[] result = null;
        try {
            result = getByteArray(entries);
        } catch (IOException ex) {} 
        return result;
    }    
    
    /**
    * create a CompositeMetadata representing ReplicationState state
    *
    * @param state
    *   The ReplicationState
    *
    * @return
    *   A newly created CompositeMetadata object for the given state
    */ 
    private CompositeMetadata createCompositeMetadataFrom(ReplicationState state) {
        Collection entries = this.deserializeStatesCollection(state.getState());
        CompositeMetadata result
            = new CompositeMetadata(
                state.getVersion(),  //version
                state.getLastAccess(), //lastAccess
                state.getMaxInactiveInterval(), //maxInactive
                entries,                        //entries
                state.getTrunkState(),          //trunkState
                state.getExtraParam());     //extraParam      
        return result;
    } 
    
    private Collection deserializeStatesCollection(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getAttributeValueCollection(entriesState);
        } catch (ClassNotFoundException ex1) {
        } catch (IOException ex2) {}        
        return result;
    } 
    
    private Collection deserializeStatesCollectionPrevious(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getAttributeValue(entriesState);
        } catch (ClassNotFoundException ex1) {
        } catch (IOException ex2) {}        
        return result;
    }     
    
}
