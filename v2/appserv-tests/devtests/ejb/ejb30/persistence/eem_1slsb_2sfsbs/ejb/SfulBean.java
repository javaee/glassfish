package com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs;



import java.util.Map;
import java.util.LinkedHashMap;

import javax.naming.InitialContext;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityNotFoundException;

import javax.persistence.EntityManager;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.EJBException;

@Stateful
@EJB(name="ejb/SfulBean", 
        beanInterface=com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs.SfulDelegate.class)

public class SfulBean
    implements Sful {

    private String name;
    
    private @EJB SfulDelegate delegate;

    private @EJB Sless sless;
    
    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-eem_1slsb_2sfsbs-par1.jar#em",
                type=PersistenceContextType.EXTENDED) 
            EntityManager extendedEM;
 
    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";
            
            InitialContext initCtx = new InitialContext();
            delegate = (SfulDelegate) initCtx.lookup(lookupName);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests() {
        Person person = new Person(name);
        
        String delegateName = "delgname_" + name;
        String delegateData= "delgdata: " + name;

        SfulPeer peer = sless.getSfulPeer();

        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

        map.put("delegateCreated",
                (delegate.create(delegateName, delegateData) != null));

        map.put("gotPeer", (peer != null));

        map.put("iFoundDelegate",
                (extendedEM.find(Person.class, delegateName) != null));

        boolean peerOpStatus = false;
        try {
            peerOpStatus = false;
            peer.find(delegateName);
        } catch (Exception ex) {
            peerOpStatus = true;
        }
        map.put("peerFoundDelegate (-ve test)", peerOpStatus);

        extendedEM.persist(person);

        map.put("delegateFoundMe",
                (delegate.find(name) != null));

        try {
            peerOpStatus = false;
            peer.find(name);
        } catch (Exception ex) {
            peerOpStatus = true;
        }
        map.put("peerFoundMe (-ve test)", peerOpStatus);
        
        map.put("delegateRemovedMe",
                delegate.remove(name));

        try {
            peerOpStatus = false;
            peer.remove(name);
        } catch (Exception ex) {
            peerOpStatus = true;
        }
        map.put("peerCannotFindMe (-ve test)", peerOpStatus);

        map.put("iCannotFindMe (-ve test)",
            (find(name) == null));
        
        map.put("removedDelegate",
            removePerson(delegateName));

        try {
            peerOpStatus = false;
            peer.remove(delegateName);
        } catch (Exception ex) {
            peerOpStatus = true;
        }
        map.put("peerCannotfindDelegate (-ve test)", peerOpStatus);
        
        String peerName = "_peer_name_" + name;
        String peerData = "_peer_data_" + name;

        try {
            peerOpStatus = false;
            peer.create(peerName, peerData);
        } catch (Exception ex) {
            peerOpStatus = true;
        }
        map.put("peerCreated (-ve test)", peerOpStatus);

        map.put("iFoundPeer (-ve test)",
                (find(peerName) == null));

        map.put("delegateFoundPeer (-ve test)",
               (delegate.find(peerName) == null));

        return map;
    }

    Person find(String name) {

        Person p = extendedEM.find(Person.class, name);
        System.out.println("Found " + p);
        return p;
    }

    boolean removePerson(String personName) {

        Person p = extendedEM.find(Person.class, personName);
        boolean removed = false;
        if (p != null) {
            extendedEM.remove(p);
            removed = true;
        }
        return removed;
    }
    
}
