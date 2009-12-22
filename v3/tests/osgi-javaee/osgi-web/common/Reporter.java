    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileReader;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.util.Iterator;
    import java.util.Vector;

    /**
     *
     * @author Mohit
     */
    public class Reporter {

        public static File testResult ; //= new File("/space/v3work/v3/tests/osgi-javaee/testresult.txt");
        public static File testSummaryFile;
        public static String[] statusCode = {"Passed", "BuildFailure", "DeploymentFailure", "RuntimeFailure"};
        public static String lineSepatartor = "***********************";
        public static String lineSepatartor2 = "-----------------------";
        static int PASS = 0;
        static int BUILDFAIL = 1;
        static int DEPLOYMENTFAIL = 2;
        static int RUNTIMEFAIL = 3;

        public Reporter() {
        }

        public Reporter(String testResultFile, String testSummary) {
            testResult = new File(testResultFile);
            testSummaryFile = new File(testSummary);
        }

        public Reporter(String testResultFile) {
            testResult = new File(testResultFile);
        }

        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) {
            Reporter reporter;
            if (args.length == 2) {
                //call is for generating summary.
                //args[0] contain testResultFile path.
                //args[1] contain testSummaryFile path.
                reporter = new Reporter(args[0], args[1]);
                reporter.generateSummary();
            } else if(args.length == 3) {
                //call is for printing test status.
                //args[0] contains testName.
                //args[1] contains testResult Code.
                //args[2] contains testResult Filename.
                reporter = new Reporter(args[2]);
                int code = Integer.parseInt(args[1]);
                reporter.printStatus(args[0], code);
            }
        }

        public void printStatus(String testName, int code) {
            try {
                BufferedWriter out =
                        new BufferedWriter(new FileWriter(testResult, true)); //opening file in append mode.
                out.newLine();
                out.write(lineSepatartor + "\n");
                out.write(testName + " :: " + statusCode[code] + "\n");
                out.write(lineSepatartor + "\n");
                out.newLine();
                out.close();
            } catch (IOException ex) {
                System.out.println("REPORTER: Error Writing TestReport");
            }
        }

        public void generateSummary() {
            Vector[] testSummary = new Vector[4];
            for(int i=0; i< testSummary.length; i++)
                testSummary[i] = new Vector<String>();
            try {
                BufferedReader in = new BufferedReader(new FileReader(testResult));
                BufferedWriter out =
                        new BufferedWriter(new FileWriter(testSummaryFile, true));
                String line = null;
                while((line = in.readLine()) != null) {
                    String testName = (line.split("::"))[0];
                    if(line.contains(statusCode[PASS])) {
                        //Pass.
                        testSummary[0].add(testName);
                    } else if(line.contains(statusCode[BUILDFAIL])) {
                        //Build Failed.
                        testSummary[1].add(testName);
                    } else if(line.contains(statusCode[DEPLOYMENTFAIL])) {
                        //Deployment Failed.
                        testSummary[2].add(testName);
                    } else if(line.contains(statusCode[RUNTIMEFAIL])) {
                        //Runtime Failure.
                        testSummary[3].add(testName);
                    }
                }
                out.write("Test Summary : \n \n");
                out.write(lineSepatartor + "\n");
                Iterator itr = null;
                for(int i=0;i<testSummary.length; i++){
                    out.write(statusCode[i] + " : " + testSummary[i].size() + "\n");
                    out.write(lineSepatartor2 + "\n");
                    itr = testSummary[i].iterator();
                    while(itr.hasNext())
                        out.write(itr.next().toString() + "\n");
                    out.write("\n" + lineSepatartor + "\n");
                }
                in.close();
		out.close();
            } catch (Exception ex) {
                System.out.println("REPORTER: Error generating TestSummary");
            }
        }
    }

