/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.web.ha.session.management;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.logging.LogDomains;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;
//import com.sun.appserv.ha.util.CompositeMetadata;
//import com.sun.appserv.ha.util.SessionAttributeMetadata;
//import com.sun.appserv.ha.util.Metadata;
//import com.sun.enterprise.ee.web.authenticator.ReplicationSingleSignOn;
//import com.sun.enterprise.security.web.SingleSignOn;
//import com.sun.enterprise.security.web.SingleSignOnEntry;
import org.apache.catalina.*;
import org.apache.catalina.session.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Larry White
 * @author Rajiv Mordani
 */
public class ReplicationAttributeStore extends ReplicationStore {
    

    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = LogDomains.getLogger(ReplicationAttributeStore.class, LogDomains.WEB_LOGGER);    
    

    /** Creates a new instance of ReplicationAttributeStore */
    public ReplicationAttributeStore(ServerConfigLookup serverConfigLookup, JavaEEIOUtils ioUtils) {
        super(serverConfigLookup, ioUtils);
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
//        BackingStore replicator = mgr.getCompositeBackingStore();
        BackingStore replicator = mgr.getBackingStore();
        System.out.println("ReplicationAttributeStore>>save: replicator: " + replicator);                    
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>save: replicator: " + replicator);                    
        }         
        CompositeMetadata compositeMetadata
            = createCompositeMetadata(modAttrSession);
                
        try {        
            System.out.println("Calling save with composite metadata");
            replicator.save(session.getIdInternal(), //id
                    compositeMetadata, !((HASession) session).isPersistent());
            modAttrSession.resetAttributeState();
            postSaveUpdate(modAttrSession);
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
//        BackingStore replicator = mgr.getCompositeBackingStore();
        BackingStore replicator = mgr.getBackingStore();
        System.out.println("in do save");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>doSave: replicator: " + replicator);                    
        }         
        CompositeMetadata compositeMetadata 
            = createCompositeMetadata(modAttrSession);
                
        try {        
            System.out.println("Calling save on backing store");
            replicator.save(session.getIdInternal(), //id
                    compositeMetadata, !((HASession) session).isPersistent());
            modAttrSession.resetAttributeState();
            postSaveUpdate(modAttrSession);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }
    
    private BackingStore getCompositeBackingStore() {
        ReplicationManagerBase mgr
                = (ReplicationManagerBase) this.getManager();
//        BackingStore backingStore = mgr.getCompositeBackingStore();
        BackingStore backingStore = mgr.getBackingStore();
        return backingStore;
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
    public Session load(String id) throws ClassNotFoundException, IOException {
        return load(id, null);
    }

    public Session load(String id, String version)
            throws ClassNotFoundException, IOException {
        try {
            CompositeMetadata metaData =
                    (CompositeMetadata) getCompositeBackingStore().load(id, version);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationAttributeStore>>load:id=" + id + ", metaData=" + metaData);
            }
            Session session = getSession(metaData);
            validateAndSave(session);
            return session;
        } catch (BackingStoreException ex) {
            IOException ex1 =
                    (IOException) new IOException("Error during load: " + ex.getMessage()).initCause(ex);
            throw ex1;
        }
    }

    private void validateAndSave(Session session) throws IOException {
        if (session != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationAttributeStore>>validateAndSave saving " +
                        "the session after loading it. Session=" + session);
            }
            //save session - save will reset dirty to false
            ((HASession) session).setDirty(true);
            valveSave(session); // TODO :: revisit this for third party backing stores;
        }
        if (session != null) {
            ((HASession) session).setDirty(false);
            ((HASession) session).setPersistent(false);
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
/*
    public Metadata __load(String id, String version) throws BackingStoreException {
        CompositeMetadata result = null;
        if(id == null) {
            return result;
        }
        ReplicationManagerBase repMgr =
            (ReplicationManagerBase)this.getManager();
        //ReplicationState localCachedState = repMgr.getFromReplicationCache(id);
        ReplicationState localCachedState = repMgr.transferFromReplicationCache(id);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>__load:localCachedState=" + localCachedState);
        } 
        //check if we got a hit from our own replica cache
        //and if so return it immediately
        if(version != null && localCachedState != null) {
            long versionLong = ReplicationUtil.parseLong(version);
            if(localCachedState.getVersion() == versionLong) {
                return ReplicationState.createCompositeMetadataFrom(localCachedState);
            }            
        }        
        ReplicationState broadcastResultState = findSessionViaBroadcast(id, version);
        ReplicationState bestState
            = ReplicationState.getBestResult(localCachedState, broadcastResultState);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>__load:broadcastResultState from broadcast=" +
                    broadcastResultState + ", bestState = " + bestState);
        }
        result = ReplicationState.createCompositeMetadataFrom(bestState);
        return result;
    } 
*/

/*
    private ReplicationState findSessionViaBroadcast(String id, String version)
            throws BackingStoreException{
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("ReplicationAttributeStore",
                             "findSessionViaBroadcast",
                             new Object[]{id, version});                       
        }          
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
        ReplicationState queryResult = jxtaReplicator != null ?
                jxtaReplicator.__load(id, version) : null;
        return queryResult;
    }
*/

    /**
    * Given a byte[] containing session data, return a session
    * object
    * note we use the trunkState to get the basic session
    * this over-rides the inherited getSession method behavior
    *
    * @param replicationState
    *   The byte[] with the session data
    *
    * @return
    *   A newly created session for the given session data, and associated
    *   with this Manager
    */
/*
    @Override
    public Session getSession(ReplicationState replicationState) throws IOException {
        if (replicationState == null || replicationState.getState() == null) {
            return null;
        } else {
            return getSession(ReplicationState.createCompositeMetadataFrom(replicationState));
        }
    }
*/

    public Session getSession(CompositeMetadata metadata)
        throws IOException 
    {
        if (metadata == null || metadata.getState() == null) {
            return null;
        }
        byte[] state = metadata.getState();
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
//        IOUtilsCaller utilsCaller = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            //Get the username, ssoId from metadata
            //ssoId = metadata.getSsoId();
            ssoId = metadata.getStringExtraParam();
            version = metadata.getVersion();
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

                try {
                    ois = ioUtils.createObjectInputStream(bis, true, classLoader);
                } catch (Exception ex) {}

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
            IOException ex1 = (IOException) new IOException(
                    "Error during deserialization: " + e.getMessage()).initCause(e);
            throw ex1;
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
/*
        ((BaseHASession)_session).setSsoId(ssoId);
        if((ssoId !=null) && (!ssoId.equals("")))
            associate(ssoId, _session);
*/
        ((HASession)_session).setVersion(version);
        ((HASession)_session).setDirty(false);
        
        //now load entries from deserialized entries collection
        ((ModifiedAttributeHASession)_session).clearAttributeStates();
        byte[] entriesState = metadata.getState();
        Collection entries = null;
        if(entriesState != null) {
            entries = this.deserializeStatesCollection(entriesState);
            loadAttributes((ModifiedAttributeHASession)_session, entries);
        }
        loadAttributes((ModifiedAttributeHASession)_session, metadata.getEntries());
        return _session;
    }
    
    //FIXME might not be needed - can make a no-ope
/*
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
*/

    //metadata related
    
    private void postSaveUpdate(ModifiedAttributeHASession modAttrSession) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>postSaveUpdate");                       
        }
        ArrayList addedAttrs = modAttrSession.getAddedAttributes();
        ArrayList modifiedAttrs = modAttrSession.getModifiedAttributes();
        ArrayList deletedAttrs = modAttrSession.getDeletedAttributes();
        printAttrList("ADDED", addedAttrs);
        printAttrList("MODIFIED", modifiedAttrs);
        printAttrList("DELETED", deletedAttrs);

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
        if (modAttrSession.isNew()) {
            try {
                trunkState = this.getByteArray(modAttrSession);
            } catch(IOException ex) {
                //no op
            }
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationAttributeStore>>createCompositeMetadata:trunkState=" + trunkState);                       
        }         
       
        ArrayList entries = new ArrayList();
        ArrayList addedAttrs = modAttrSession.getAddedAttributes();
        ArrayList modifiedAttrs = modAttrSession.getModifiedAttributes();
        ArrayList deletedAttrs = modAttrSession.getDeletedAttributes();
        printAttrList("ADDED", addedAttrs);
        printAttrList("MODIFIED", modifiedAttrs);
        printAttrList("DELETED", deletedAttrs);
        
        addToEntries(modAttrSession, entries, 
                SessionAttributeMetadata.Operation.ADD, addedAttrs);
        addToEntries(modAttrSession, entries, 
                SessionAttributeMetadata.Operation.UPDATE, modifiedAttrs);
        entries = addToEntries(modAttrSession, entries, 
                SessionAttributeMetadata.Operation.DELETE, deletedAttrs);

        CompositeMetadata result 
            = new CompositeMetadata(modAttrSession.getVersion(),
                modAttrSession.getLastAccessedTimeInternal(),
                modAttrSession.getMaxInactiveInterval(),
                entries, trunkState, null);
                //,
                //trunkState);
//                ,modAttrSession.getSsoId()); //ssoId is the extraParam here
        return result;
    }
    
    private void printAttrList(String attrListType, ArrayList attrList) {
        if (_logger.isLoggable(Level.INFO)) {
            _logger.info("AttributeType = " + attrListType);
            String nextAttrName = null;
            for(int i=0; i<attrList.size(); i++) {
                nextAttrName = (String)attrList.get(i);
                _logger.info("attribute[" + i + "]=" + nextAttrName);
            }
        }
    }
    
    private ArrayList addToEntries(ModifiedAttributeHASession modAttrSession,
            ArrayList entries, SessionAttributeMetadata.Operation op, ArrayList attrList) {
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
            SessionAttributeMetadata nextAttrMetadata
                = new SessionAttributeMetadata(nextAttrName, op, nextValue);
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
            

            try {
                oos = ioUtils.createObjectOutputStream(new BufferedOutputStream(bos), true);
            } catch (Exception ex) {}

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

                try {
                    ois = ioUtils.createObjectInputStream(bis, true, classLoader);
                } catch (Exception ex) {}

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
            // FIXME Evaluate logging level for exception.
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "ClassNotFoundException occurred in getAttributeValue", e);
            }
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
    * @param entries
    *   The Collection of entries we are serializing
    *
    */
    protected byte[] getByteArrayFromCollection(Collection entries)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        
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
            

            try {
                oos = ioUtils.createObjectOutputStream(new BufferedOutputStream(bos), true);
            } catch (Exception ex) {}

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

                try {
                    ois = ioUtils.createObjectInputStream(bis, true, classLoader);
                } catch (Exception ex) {}

            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            if(ois != null) {
                try {
                    //first get List size
                    Object whatIsIt = ois.readObject();
                    int entriesSize = 0;
                    if(whatIsIt instanceof Integer) {
                        entriesSize = ((Integer)whatIsIt).intValue();
                    }
                    //int entriesSize = ((Integer) ois.readObject()).intValue();
                    //attributeValueList = new ArrayList(entriesSize);
                    //if (_logger.isLoggable(Level.FINE)) {
                    //    _logger.fine("first obj: " + whatIsIt + " entriesSize=" + entriesSize);
                    //}
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
            // FIXME Evaluate logging level for exception.
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "ClassNotFoundException occurred in getAttributeValueCollection", e);
            }
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
    * @param modifiedAttributeSession
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
        SessionAttributeMetadata.Operation thisAttrOp = null;
        Object thisAttrVal = null;
        Iterator it = attributeList.iterator();
        while (it.hasNext()) { 
            SessionAttributeMetadata nextAttrMetadata = (SessionAttributeMetadata)it.next();
            thisAttrName = nextAttrMetadata.getAttributeName();
            thisAttrOp = nextAttrMetadata.getOperation();
            byte[] nextAttrState = nextAttrMetadata.getState();
            thisAttrVal = null;
            try { 
                thisAttrVal = getAttributeValue(nextAttrState);
            } catch (ClassNotFoundException ex1) {
                //FIXME log?
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
/*
    private ReplicationState createReplicationStateFrom(String id, CompositeMetadata compositeMetadata, String command) {
        Object containerExtraParams
            //FIXED: Was   = compositeMetadata.getContainerExtraParam();
            = compositeMetadata.getExtraParam();
        byte[] containerExtraParamState = null;
        if(containerExtraParams != null) {
            try {
                containerExtraParamState 
                    = getByteArray(containerExtraParams);
            } catch (IOException ex) {
                ;   //deliberate no-op
            }
        }
        String extraParamString 
            = ReplicationState.extractExtraParamStringFrom(MODE_WEB, containerExtraParams);
        
        ReplicationState result 
            = new ReplicationState(MODE_WEB,    //mode
                id,                             //id
                this.getApplicationId(),        //appid
                compositeMetadata.getVersion(), //version
                compositeMetadata.getLastAccessTime(), //lastAccess
                compositeMetadata.getMaxInactiveInterval(), //maxInactive
                //compositeMetadata.getExtraParam(), //extraParam
                extraParamString,   //extraParam
                null,                               //queryResult
                null,                               //instanceName
                command,                               //command
                this.serializeStatesCollection(compositeMetadata.getEntries()), //data 
                compositeMetadata.getState(),          //trunkData
                containerExtraParamState);          //containerExtraParamsData
        return result;
    }
*/

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
/*
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
*/

    private Collection deserializeStatesCollection(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getAttributeValueCollection(entriesState);
        } catch (ClassNotFoundException ex1) {
            // FIXME log?
        } catch (IOException ex2) {}        
        return result;
    } 
    
    private Collection deserializeStatesCollectionPrevious(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getAttributeValue(entriesState);
        } catch (ClassNotFoundException ex1) {
              // FIXME log?
        } catch (IOException ex2) {}        
        return result;
    }     
    
}
