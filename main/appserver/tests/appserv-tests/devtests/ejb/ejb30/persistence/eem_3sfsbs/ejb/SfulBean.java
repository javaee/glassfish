package com.sun.s1asdev.ejb.ejb30.persistence.eem_3sfsbs;



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
        beanInterface=com.sun.s1asdev.ejb.ejb30.persistence.eem_3sfsbs.SfulDelegate.class)

public class SfulBean
    implements Sful {

    private String name;
    
    private @EJB SfulDelegate delegate;
    
    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-eem_3sfsbs-par1.jar#em",
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

        SfulPeer peer = delegate.getSfulPeer();

        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

        map.put("delegateCreated",
                (delegate.create(delegateName, delegateData) != null));

        map.put("gotPeer", (peer != null));

        map.put("iFoundDelegate",
                (extendedEM.find(Person.class, delegateName) != null));

        map.put("peerFoundDelegate",
                (peer.find(delegateName) != null));

        extendedEM.persist(person);

        map.put("delegateFoundMe",
                (delegate.find(name) != null));

        map.put("peerFoundMe",
                (peer.find(name) != null));
        
        map.put("delegateRemovedMe",
                delegate.remove(name));

        map.put("peerCannotFindMe (-ve test)",
                (! peer.remove(name)));

        map.put("iCannotFindMe (-ve test)",
            (find(name) == null));
        
        map.put("removedDelegate",
            removePerson(delegateName));

        map.put("peerCannotfindDelegate (-ve test)",
            (! peer.remove(delegateName)));
        
        String peerName = "_peer_name_" + name;
        String peerData = "_peer_data_" + name;

        map.put("peerCreated",
                (peer.create(peerName, peerData) != null));

        map.put("iFoundPeer",
                (find(peerName) != null));

        map.put("delegateFoundPeer",
                (delegate.find(peerName) != null));

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
