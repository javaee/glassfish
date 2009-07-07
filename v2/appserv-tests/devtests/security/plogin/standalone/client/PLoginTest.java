import com.sun.appserv.security.ProgrammaticLogin;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;

abstract class MyThreadBase extends Thread {

    public static int LOOP_COUNT = 100;

    private static Random random = new Random();

    protected String username = null;
    protected String password = null;

    private String ejbLookupName = null;
    private int counter = 0;
    private boolean passFail = false;

    protected MySession1Remote my1r = null;

    public MyThreadBase(String username, String password, 
        String ejbLookupName, String threadName) {

        super(threadName);

        this.username = username;
        this.password = password;
        this.ejbLookupName = ejbLookupName;
    }

    public boolean passOrFail() {
        return passFail;
    }

    public void run() {

      try {
          run0();
          // test passed
          passFail = true;
      } catch(Exception e) {
          // test failed
          passFail = false;
          //System.out.println("Thread died: " + Thread.currentThread().getName());
          e.printStackTrace();
      }

    }

    private void run0() throws Exception  {

        counter=0;
        while(counter++<LOOP_COUNT) {

          doLogin();

          try {
              // Give time for other thread to foul up the login of this thread
              Thread.sleep(100);
          } catch(Exception e) {
          }

          InitialContext ctx = new InitialContext();

          Object o = ctx.lookup(ejbLookupName);

          MySession1RemoteHome my1rh = (MySession1RemoteHome)
              PortableRemoteObject.narrow(o, MySession1RemoteHome.class);

          my1r = my1rh.create(); 
          String retval = doBusiness();

          System.out.println(Thread.currentThread().getName() + " - " + retval);
          System.out.flush();

          doLogout();

          try {
              // Give time for other thread to 
              Thread.sleep(random.nextInt(100));
          } catch(Exception e) {
          }
        } 
    }

    public abstract String doBusiness() throws Exception ;
    protected abstract void doLogin();
    protected abstract void doLogout();
}


abstract class LoginBusinessCallerBase extends MyThreadBase {

    ProgrammaticLogin login = null;

    public LoginBusinessCallerBase(String username, String password, 
                                   String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    protected void doLogin() {
        login = new ProgrammaticLogin();
        login.login(username,password);
    }

    protected void doLogout() {
        login.logout();
    }
}

abstract class NoLoginBusinessCallerBase extends MyThreadBase {

    public NoLoginBusinessCallerBase(String username, String password, 
                                     String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    protected void doLogin() {
        //nop
    }

    protected void doLogout() {
        //nop
    }
}

class LoginBusinessCaller extends LoginBusinessCallerBase {

    public LoginBusinessCaller(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod("0th dude");
    }
}

class LoginBusinessCaller2 extends LoginBusinessCallerBase {

    public LoginBusinessCaller2(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod2("2nd dudess");
    }
}

class LoginBusinessCaller3 extends LoginBusinessCallerBase {

    public LoginBusinessCaller3(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod3("3rd fellow");
    }
}

class NoLoginBusinessCaller extends NoLoginBusinessCallerBase {

    public NoLoginBusinessCaller(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod("0th dude");
    }
}

class NoLoginBusinessCaller2 extends NoLoginBusinessCallerBase {

    public NoLoginBusinessCaller2(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod2("2nd dudess");
    }
}

class NoLoginBusinessCaller3 extends NoLoginBusinessCallerBase {

    public NoLoginBusinessCaller3(String username, String password, 
        String ejbLookupName, String threadName) {
        super(username, password, ejbLookupName, threadName);
    }

    public String doBusiness() throws Exception {
        return my1r.businessMethod3("3rd fella");
    }
}

public class PLoginTest {
    
    private static SimpleReporterAdapter stat = 
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) throws Exception {

        Boolean b = Boolean.getBoolean("com.sun.appserv.iiopclient.perthreadauth");
        int numThreads = Integer.valueOf(args[0]);
        MyThreadBase.LOOP_COUNT = Integer.valueOf(args[1]);

        if( b )
            perThreadTest(numThreads);
        else 
            perProcessTest(numThreads);
    }

    private static void perThreadTest(int numThreads) throws Exception {
        
        Set<MyThreadBase> threadSet = new HashSet<MyThreadBase>();

        for(int i=0; i<numThreads; i++) {

            LoginBusinessCaller t1 = 
                new LoginBusinessCaller("testy", "testy", "ejb/MySession1Bean", 
                      "THREAD-"+"1-"+i);
            LoginBusinessCaller2 t2 =
                new LoginBusinessCaller2("testy2", "testy2", "ejb/MySession1Bean", 
                      "THREAD-"+"2-"+i);
            LoginBusinessCaller3 t3 = 
                new LoginBusinessCaller3("testy3", "testy3", "ejb/MySession1Bean", 
                      "THREAD-"+"3-"+i);

            threadSet.add(t1);
            threadSet.add(t2);
            threadSet.add(t3);

            t2.start();   
            t1.start();   
            t3.start();

        }

        System.out.println("Number of threads started: " + threadSet.size());

        for(MyThreadBase t : threadSet ) {
            t.join();
        }

        boolean result = true;
        for(MyThreadBase t : threadSet ) {
            result = result && t.passOrFail();
            if( ! result )
                break;
        }

        stat.addDescription("Programmatic Login per thread test");
        String testId = "Plogin per thread test";
        if( result ) {
            stat.addStatus(testId, stat.PASS);
        } else {
            stat.addStatus(testId, stat.FAIL);
        }
        stat.printSummary(testId);

    }

    private static void perProcessTest(int numThreads) throws Exception {
        
        Set<MyThreadBase> threadSet = new HashSet<MyThreadBase>();

        ProgrammaticLogin login = new ProgrammaticLogin();
        login.login("chief", "chief");

        for(int i=0; i<numThreads; i++) {

            NoLoginBusinessCaller t1 = 
                new NoLoginBusinessCaller("testy", "testy", "ejb/MySession1Bean", 
                      "THREAD-"+"1-"+i);
            NoLoginBusinessCaller2 t2 =
                new NoLoginBusinessCaller2("testy2", "testy2", "ejb/MySession1Bean", 
                      "THREAD-"+"2-"+i);
            NoLoginBusinessCaller3 t3 = 
                new NoLoginBusinessCaller3("testy3", "testy3", "ejb/MySession1Bean", 
                      "THREAD-"+"3-"+i);

            threadSet.add(t1);
            threadSet.add(t2);
            threadSet.add(t3);

            t2.start();   
            t1.start();   
            t3.start();

        }

        for(MyThreadBase t : threadSet ) {
            t.join();
        }

        boolean result = true;
        for(MyThreadBase t : threadSet ) {
            result = result && t.passOrFail();
            if( ! result )
                break;
        }

        stat.addDescription("Programmatic Login per process test");

        String testId = "Plogin per process test";
        if( result ) {
            stat.addStatus(testId, stat.PASS);
        } else {
            stat.addStatus(testId, stat.FAIL);
        }
        stat.printSummary(testId);

    }


}
