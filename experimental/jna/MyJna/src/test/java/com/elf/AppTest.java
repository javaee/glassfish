/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf;

import com.sun.jna.Platform;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.sun.jna.Library;
import com.sun.jna.Native;


/**
 *
 * @author bnevins
 */
public class AppTest {
    @Before
    public void setup() {

    }
    @Test
    public void main() {
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZ");
        String[] args = null;
        App.main(args);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
   public interface POSIX extends Library {
	public int chmod(String filename, int mode);
	public int chown(String filename, int user, int group);
	public int rename(String oldpath, String newpath);
	public int kill(int pid, int signal);
	public int link(String oldpath, String newpath);
	public int mkdir(String path, int mode);
	public int rmdir(String path);
	POSIX INSTANCE = (POSIX) Native.loadLibrary("c", POSIX.class);
   }

    public interface Kernel32 extends Library {
       // FREQUENCY is expressed in hertz and ranges from 37 to 32767
       // DURATION is expressed in milliseconds
       public boolean Beep(int FREQUENCY, int DURATION);
       public void Sleep(int DURATION);
       public long GetCurrentProcess();
       public int GetProcessId(long process);
       Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
   }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary(
            (windows ? "msvcrt" : "c"), CLibrary.class);
        void printf(String format, Object... args);
    }

    @Test
    public void beep() {
        Kernel32.INSTANCE.Beep(698, 500);
        Kernel32.INSTANCE.Sleep(500);
        Kernel32.INSTANCE.Beep(698, 500);
   }

    @Test
    public void pid() {
        int pid = Kernel32.INSTANCE.GetProcessId(Kernel32.INSTANCE.GetCurrentProcess());
        System.out.println("************  PID= " + pid + "  **************");
        System.out.println("************  PID= " + pid + "  **************");
        System.out.println("************  PID= " + pid + "  **************");
        System.out.println("************  PID= " + pid + "  **************");
        Kernel32.INSTANCE.Sleep(3000);
    }

    @Test
    public void printf() {
        CLibrary.INSTANCE.printf("Hello, World from NATIVE CODE\n");
        CLibrary.INSTANCE.printf("Hello, World from NATIVE CODE\n");
        CLibrary.INSTANCE.printf("Hello, World from NATIVE CODE\n");
        CLibrary.INSTANCE.printf("Hello, World from NATIVE CODE\n");
        CLibrary.INSTANCE.printf("Hello, World from NATIVE CODE\n");
    }

    @Test
    public void posix() {
        if(windows)
            return;

        System.out.println("Created directory via Native calls: /tmp/foofoogoo ");
        POSIX.INSTANCE.mkdir("/tmp/foofoogoo", 777);
    }

    private final static boolean windows = Platform.isWindows();
}
