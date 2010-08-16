package connector;

import javax.resource.spi.work.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.ArrayList;

public class NestedWork_Parent extends DeliveryWork implements WorkContextProvider {
    //    private WorkContexts ics = null;
    private List<WorkContext> contextsList = new ArrayList<WorkContext>();
    private MessageEndpoint ep;
    private int numOfMessages;
    private int workCount = 1;
    private String op = null;
    private WorkManager wm;
    private XATerminator xa;
    private boolean transactedChild;
    private boolean transacted;
    private boolean successfulPVForChild;
    private boolean translationRequired;

    public NestedWork_Parent(MessageEndpoint ep, int numOfMessages, String op, int workCount,
                             WorkManager wm, XATerminator xa, boolean transacted, boolean transactedChild,
                             boolean successfulPVForChild, boolean translationRequired) {
        super(ep, numOfMessages, op);
        this.workCount = workCount;
        this.ep = ep;
        this.numOfMessages = numOfMessages;
        this.op = op;
        this.wm = wm;
        this.xa = xa;
        this.transacted =  transacted;
        this.transactedChild = transactedChild;
        this.successfulPVForChild = successfulPVForChild;
        this.translationRequired =  translationRequired;
    }

    public NestedWork_Parent(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount,
                             int workCount, WorkManager wm, XATerminator xa, boolean transacted, boolean transactedChild,
                             boolean successfulPVForChild, boolean translationRequired) {
        super(ep, numOfMessages, op, keepCount);
        this.workCount = workCount;
        this.ep = ep;
        this.numOfMessages = numOfMessages;
        this.op = op;
        this.wm = wm;
        this.xa = xa;
        this.transacted =  transacted;
        this.transactedChild = transactedChild;
        this.successfulPVForChild = successfulPVForChild;
        this.translationRequired = translationRequired;
    }

    public List<WorkContext> getWorkContexts() {
        return contextsList;
    }


    public void addWorkContext(WorkContext ic) {
        contextsList.add(ic);
    }

    public void run() {
        for (int i = 0; i < workCount; i++) {

            ExecutionContext ec1 = null;
            try {
                if(transacted){
                    super.run();
                }

                NestedWork_Child w1 = new NestedWork_Child(ep, numOfMessages, op, transactedChild, translationRequired);

                if (transactedChild) {
                    ec1 = startTx();
                    TransactionContext tic = new TransactionContext();
                    tic.setXid(ec1.getXid());
                    w1.addWorkContext(tic);
                }

                if(successfulPVForChild){
                    MySecurityContext sic =
                        new MySecurityContextWithListener("prasath", "jagadish", "jagadish",  translationRequired,  true, true);
                    w1.addWorkContext(sic);
                }else{
                    MySecurityContext sic =
                            new MySecurityContextWithListener("abc", "xyz", "jagadish",  translationRequired,  true, false);
                    w1.addWorkContext(sic);
                }

                debug("executing nested work parent instance [ " + i + " ] ");
                wm.doWork(w1, 0, null, null);
                if (transactedChild) {
                    xa.commit(ec1.getXid(), true);
                    debug("commiting nested work parent instance [ " + i + " ] ");
                }
                debug("completed nested work parent instance [ " + i + " ] ");

            } catch (Exception we) {
                debug(we.toString());
            }
        }
    }

    private ExecutionContext startTx() {
        ExecutionContext ec = new ExecutionContext();
        try {
            Xid xid = new XID();
            ec.setXid(xid);
            ec.setTransactionTimeout(50 * 1000); //50 seconds
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ec;
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [Nested Work - Parent]: " + message);
    }

}
