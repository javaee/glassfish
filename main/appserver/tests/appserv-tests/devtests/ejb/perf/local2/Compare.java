
import java.io.*;
import java.util.StringTokenizer;

public class Compare {


    public static void main(String args[]) {


        String file1Name = args[0];
        String file2Name = args[1];

        File file1 = new File(file1Name);
        File file2 = new File(file2Name);
        
        float allResults = 0;
        int totalResults = 0;

        try {

            LineNumberReader reader1 = new LineNumberReader
                (new FileReader(file1));

            LineNumberReader reader2 = new LineNumberReader
                (new FileReader(file2));
            
            String nextLine1 = reader1.readLine();

            while(nextLine1 != null) {
                String nextLine2 = reader2.readLine();
                
                StringTokenizer tokenizer1 = 
                    new StringTokenizer(nextLine1);
                StringTokenizer tokenizer2 = 
                    new StringTokenizer(nextLine2);                
                StringBuffer category = new StringBuffer();
                boolean tx = true;
                int numResults = 0;
                while(tokenizer1.hasMoreTokens()) {
                    String nextToken1 = tokenizer1.nextToken();
                    String nextToken2 = tokenizer2.nextToken();

                    if( Character.isDigit(nextToken1.charAt(0)) ) {
                        numResults++;
                        float float1 = Float.parseFloat(nextToken1);
                        float float2 = Float.parseFloat(nextToken2);
                        float dif = float1 - float2;
                        float pcg =  dif / float1 * 100;

                        allResults += pcg;
                        totalResults++;

                        System.out.printf("%16s   %9.3f   %9.3f   %9.3f   %6.2f  %s\n",
                                          category.toString(), float1, float2, dif, pcg, tx ? "TX" : "NO_TX");
                        //                        System.out.println(category.toString() + "\t" + float1 + "\t" + float2 + "\t" + dif + "\t" + pcg);
                        tx = false;
                    } else {
                        if( nextToken1.indexOf("[exec]") == -1 ) {
                            category.append(nextToken1);
                        }
                    }
                }
                if( numResults == 0 ) {
                    System.out.println(category);
                }

                nextLine1 = reader1.readLine();
                System.out.println();
            } 

            System.out.println("total results = " + totalResults);
            System.out.println("avg pcg dif = " + allResults / totalResults);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }




}
