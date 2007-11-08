/*
 * DO NOT USE THIS CODE FOR TOMCAT!!!
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

class SlowClient extends Thread {

    Socket s = null;
    static WebTest test;
    public SlowClient(String host,int port,WebTest test) {
        this.test = test;
        setDaemon(true);
        try {
            s = new Socket(host, port);
            start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                s.getOutputStream().write(0);
                try {
                    System.out.println(getName() + " waiting");
                    Thread.sleep(30000);
                } catch (InterruptedException i) {
                    i.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            test.count++;
        }
    }
}
