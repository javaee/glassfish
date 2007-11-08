package com.sun.s1asdev.ejb.sfsb.passivateactivate.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

public class SFSBEJB
    implements SessionBean 
{

    private static final String LOCAL_CHILD_SUFFIX = "_childLocal";

    private Context envCtx;
    private Context envSubCtx;
    private Context javaCtx;
    private Context javaCompCtx;

    private transient String message;

    private SessionContext              sessionCtx;
    private Context                     initialCtx;
    private String                      sfsbName;
    private String                      envEntryTagValue;
    private SimpleEntityHome            entityHome;
    private SimpleEntityRemote          entityRemote;
    private SimpleEntityLocalHome       entityLocalHome;
    private SimpleEntityLocal           entityLocal;
    private SFSBHome                    sfsbHome;
    private SFSB                        sfsbRemote;    
    private SFSBLocalHome               sfsbLocalHome;
    private SFSBLocal                   sfsbLocal;

    private HomeHandle                  homeHandle;
    private Handle                      handle;
    private UserTransaction             userTransaction1;
    private UserTransaction             userTransaction2;

    private int				activationCount;
    private int				passivationCount;
    private Object			nonSerializableState;

    public void ejbCreate(String sfsbName) {
        System.out.println ("In SFSB.ejbCreate() for name -> " + sfsbName);
        this.sfsbName = sfsbName;

        try {
            entityRemote = entityHome.create(sfsbName, sfsbName);

            entityLocal = entityLocalHome.findByPrimaryKey(sfsbName);

            sfsbHome = (SFSBHome) sessionCtx.getEJBHome();
            sfsbLocalHome = (SFSBLocalHome) sessionCtx.getEJBLocalHome();
            
            homeHandle = entityHome.getHomeHandle();
            handle = entityRemote.getHandle();
            
            userTransaction1 = sessionCtx.getUserTransaction();
            userTransaction2 = (UserTransaction) new InitialContext().
                lookup("java:comp/UserTransaction");
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO
        }
    }

    public String getName() {
        System.out.println("In getName() for " + sfsbName);
        return this.sfsbName;
    }

    public void createSFSBChild() {
        try {
            sfsbRemote = sfsbHome.create(sfsbName + " _child");
            sfsbLocal = sfsbLocalHome.create(sfsbName + LOCAL_CHILD_SUFFIX);
        } catch(Exception e) {
	    EJBException ex = new EJBException(e.getMessage());
	    ex.initCause(e);
            throw ex;
        }
    }

    public boolean checkSFSBChild() {
        String childName = sfsbLocal.getName();
        boolean status = childName.equals(sfsbName + LOCAL_CHILD_SUFFIX);       
        return status;
    }

    public boolean checkSessionContext() {
        boolean status = sessionCtx != null;
        status = status && (sessionCtx.getEJBObject() != null);
        return status;
    }

    public boolean checkInitialContext() {
        boolean status = (initialCtx != null);
        //status = status && lookupEntityHome();
        return status;
    }

    public boolean checkEntityHome() {
        boolean status = entityHome != null;
        try {
            status = status && (entityHome.findByPrimaryKey(sfsbName) != null);
        } catch (Exception ex) {
            status = false;
        }

        return status;
    }

    public boolean checkEntityLocalHome() {
        boolean status = entityHome != null;
        try {
            status = status && (entityLocalHome.findByPrimaryKey(sfsbName) != null);
        } catch (Exception ex) {
            status = false;
        }

        return status;
    }

    public boolean checkEntityRemoteRef() {
        boolean status = entityHome != null;
        try {
            status = status && (entityHome.findByPrimaryKey(sfsbName) != null);
        } catch (Exception ex) {
            status = false;
        }

        return status;
    }

    public boolean checkEntityLocalRef() {
        boolean status = entityLocalHome != null;
        try {
            status = status
                && (entityLocalHome.findByPrimaryKey(sfsbName) != null);
        } catch (Exception ex) {
            status = false;
        }

        return status && checkSFSBChild();
    }

    public boolean checkHomeHandle() {
        boolean status = homeHandle != null;
        try {
            if (status) {
                Object homeRef = homeHandle.getEJBHome();
                SimpleEntityHome h = (SimpleEntityHome)
                    PortableRemoteObject.narrow(homeRef, SimpleEntityHome.class);
                EJBMetaData metaData2 = h.getEJBMetaData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }

        return status;
    }

    public boolean checkHandle() {
        boolean status = handle != null;
        try {
            if (status) {
                Object ref = handle.getEJBObject();
                SimpleEntityRemote ejbRef = (SimpleEntityRemote)
                    PortableRemoteObject.narrow(ref, SimpleEntityRemote.class);
                status = ejbRef.getPrimaryKey().equals(sfsbName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    public boolean checkUserTransaction() {
        boolean status =
            ((userTransaction1 != null) && (userTransaction2 != null));
        
        try {
            if( status ) {
                userTransaction1.begin();
                userTransaction1.commit();

                userTransaction2.begin();
                userTransaction2.commit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }

        return status;
    }

    public boolean isOK(String name) {
        String fieldName = "Name";
        boolean ok = name.equals(sfsbName);

        try {
            if (ok) {
                fieldName = "SessionContext";
                ok = sessionCtx != null;
            }
            if (ok) {
                fieldName = "InitialContext";
                ok = initialCtx != null;
            }
            if (ok) {
                fieldName = "java:";
                ok = ( (javaCtx != null) && 
                       javaCtx.getNameInNamespace().equals(fieldName) );
            }
            if (ok) {
                fieldName = "java:comp";
                ok = ( (javaCompCtx != null) &&
                       javaCompCtx.getNameInNamespace().equals(fieldName) );
            }
            if (ok) {
                fieldName = "java:comp/env";
                ok = ( (envCtx != null) &&
                       envCtx.getNameInNamespace().equals(fieldName) );
                
            }
            if (ok) {
                fieldName = "java:comp/env/ejb";
                ok = ( (envSubCtx != null) &&
                       envSubCtx.getNameInNamespace().equals(fieldName) );
            }
            if (ok) {
                fieldName = "env-entry";
          
                String value1 = (String)
                    initialCtx.lookup("java:comp/env/TagValue");
                String value2 = (String) envCtx.lookup("TagValue");
                ok = (value1 != null)
                    && (value1.equals(value2))
                    && (value1.equals(envEntryTagValue));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }
        
        this.message = (ok) ? null : (fieldName + " not restored properly");
        
        return ok;
    }

    public String getMessage() {
        return this.message;
    }

    public int getActivationCount() {
	return this.activationCount;
    }

    public int getPassivationCount() {
	return this.passivationCount;
    }

    public void makeStateNonSerializable() {
	nonSerializableState = new Object();
    }

    public void sleepForSeconds(int sec) {
	try {
	    Thread.currentThread().sleep(sec * 1000);
	} catch (Exception ex) {
	}
    }

    public void unusedMethod() {
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
        try {
            this.initialCtx = new InitialContext();
            this.javaCtx = (Context) initialCtx.lookup("java:");
            this.javaCompCtx = (Context) initialCtx.lookup("java:comp");
            this.envCtx = (Context) initialCtx.lookup("java:comp/env");
            this.envSubCtx = (Context) initialCtx.lookup("java:comp/env/ejb");
            this.envEntryTagValue = (String)
                envCtx.lookup("TagValue");
            lookupEntityHome();
            lookupEntityLocalHome();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        activationCount++;
    }

    public void ejbPassivate() {
        passivationCount++;
    }

    private boolean lookupEntityHome() {
        boolean status = false;
        try {
            Object homeRef = initialCtx.lookup("java:comp/env/ejb/SimpleEntityHome");
            this.entityHome = (SimpleEntityHome)
                PortableRemoteObject.narrow(homeRef, SimpleEntityHome.class);

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    private boolean lookupEntityLocalHome() {
        boolean status = false;
        try {
            Object homeRef = envSubCtx.lookup("SimpleEntityLocalHome");
            this.entityLocalHome = (SimpleEntityLocalHome) homeRef;

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }
}
