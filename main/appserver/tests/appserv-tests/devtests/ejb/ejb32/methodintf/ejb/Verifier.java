package ejb32.methodintf;

import javax.naming.InitialContext;

public class Verifier {
    static boolean verify_tx(boolean op) {
        boolean valid = true;
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new javax.naming.InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("========> TX Status for " + op + " : " + r.getTransactionStatus());
            if (op && r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: NON-Active transaction");
                valid = false;
            } else if (!op && r.getTransactionStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: Active transaction");
                valid = false;
            }
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
            valid = false;
        }

        return valid;
    }

}
