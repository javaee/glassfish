package test;

import javax.ejb.*;
import javax.naming.*;
import java.util.Properties;

@Singleton @Startup
public class TestBean {
    @EJB private TestBean testBean;
    
    public String hello() {
        return "Hello from " + this;
    }

    public <T> T lookupWithWLInitialContextFactory(String name, Class<T> clazz) throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        InitialContext ic = new InitialContext(props);
        return (T) ic.lookup(name);
    }
    
    public NamingEnumeration<NameClassPair> listEmptyString() throws NamingException {
        Context context = new InitialContext();
        return context.list("");
    }

    public NamingEnumeration<Binding> listBindingsEmptyString() throws NamingException {
        Context context = new InitialContext();
        return context.listBindings("");
    }

    public NamingEnumeration<NameClassPair> listGlobal() throws NamingException {
        Context context = new InitialContext();
        return context.list("java:global");
    }

    public NamingEnumeration<Binding> listBindingsGlobal() throws NamingException {
        Context context = new InitialContext();
        return context.listBindings("java:global");
    }
    
    public NamingEnumeration<NameClassPair> listJavaComp() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:comp/");
        return context.list("env");
    }

    public NamingEnumeration<Binding> listBindingsJavaComp() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:comp/");
        return context.listBindings("env");
    }
    
    public NamingEnumeration<NameClassPair> listJavaModule() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:module/");
        return context.list("");
    }

    public NamingEnumeration<Binding> listBindingsJavaModule() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:module/");
        return context.listBindings("");
    }
    
    public NamingEnumeration<NameClassPair> listJavaApp() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:app/");
        return context.list("");
    }

    public NamingEnumeration<Binding> listBindingsJavaApp() throws NamingException {
        Context context = (Context) new InitialContext().lookup("java:app/");
        return context.listBindings("");
    }
    
    public void closeNamingEnumerations() throws NamingException {
        listEmptyString().close();
        listBindingsEmptyString().close();
        
        listJavaComp().close();
        listBindingsJavaComp().close();
        
        listJavaModule().close();
        listBindingsJavaModule().close();
       
        listJavaApp().close();
        listBindingsJavaApp().close();
    }
}
