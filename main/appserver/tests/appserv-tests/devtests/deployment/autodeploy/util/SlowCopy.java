package autodeploy.slowtest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
/*
 * SlowCopy.java
 *
 * Created on November 23, 2004, 12:39 PM
 */

/**
 *Provides a way to test autodeployer's behavior when an autodeployed file is
 *copied into the autodeploy directory slowly. 
 *
 *Usage:
 *
 *  java -classpath ... SlowCopy timed existing-file target-file-spec [delay-in-ms]
 *
 *where
 *<ul>
 *<le>"timed" is a keyword
 *<le>existing-file is the existing archive to be copied
 *<le>target-file-spec is the file spec of the copy to be created
 *<le>delay-in-ms is the optional time delay (in ms) to wait between writes
 *</ul>
 *
 * @author  tjquinn
 */
public class SlowCopy {
    
    /** Creates a new instance of SlowCopy */
    public SlowCopy() {
    }
  
    /** default delay between successive writes of data to the output file */
    private static final long DEFAULT_DELAY = 200; // milliseconds
    
    /** size of read and write buffer */
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        try {
            new SlowCopy().run(args);
            System.exit(0);
        } catch (Throwable thr) {
            thr.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    private void run(String [] args) throws FileNotFoundException, IOException, InterruptedException {
        System.out.println(Arrays.toString(args));
        if (args.length < 3) {
            throw new IllegalArgumentException("Command line args must specify prompt/timed, the original file, and the target file");
        }
        
        /*
         *Get delay from default and override the default if the delay is 
         *specified on the command line.
         */
        long delay = DEFAULT_DELAY;
        if (args.length > 3) {
            delay = Integer.decode(args[3]);
        }
        
        /*
         *For interactive use, the tool would like to let the user press a
         *key each time a read/write is to occur.  This doesn't yet work and
         *is not used in the devtests anyway.
         */
        boolean usePromptDelay = args[0].equalsIgnoreCase("prompt");
        File oldF = new File(args[1]);
        File newF = new File(args[2]);
        
        System.out.println("Slow copy starting\n" +
                "  mode: " + args[0] + "\n" +
                "  input file: " + oldF.getAbsolutePath() + "\n" +
                "  output file: " + newF.getAbsolutePath() + "\n" +
                "  delay (if mode=timed): " + delay + "\n" +
                "  starting time: " + new Date().toString()
                );

        FileOutputStream fos = new FileOutputStream(newF);
        FileInputStream fis = new FileInputStream(oldF);
        byte [] data = new byte [BUFFER_SIZE];
        int bytesRead;
        
        Scanner scanner = null;
        boolean continuePrompting = usePromptDelay;
        String lineSep = System.getProperty("line.separator");
        
        if (usePromptDelay) {
            scanner = new Scanner(System.in);
        }
        
        int totalBytesRead = 0;
        /*
         *Repeat the cycle of reading from the input file and writing to the
         *output file, stalling for either a key press or the timed delay
         *between successive cycles.
         */
        try {
            while ((bytesRead = fis.read(data))  != -1) {
                totalBytesRead += bytesRead;
                fos.write(data, 0, bytesRead);
                if (usePromptDelay) {
                    if (scanner.hasNext(".")) {
                        String input = scanner.next(".");
                        continuePrompting = ! input.equals("q"); // just like Unix utilities - q ends
                        System.out.println("Continue prompting is now set to " + continuePrompting);
                    }
                } else {
                    /*
                     *The user wants to use timed delays, so wait for the
                     *delay period before continuing the loop.
                     */
                    Thread.currentThread().sleep(delay);
                }
            }
            System.out.println("Finished copying " + totalBytesRead + " bytes at " + new Date().toString());
            
        } finally {
            if (fos != null) {
                fos.close();
                fos = null;
            }
            if (fis != null) { 
                fis.close();
                fis = null;
            }
        }
    }
}
