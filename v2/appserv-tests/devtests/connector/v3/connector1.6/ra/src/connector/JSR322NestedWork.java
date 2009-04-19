package connector;

import javax.resource.spi.work.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.ArrayList;

public class JSR322NestedWork extends DeliveryWork implements WorkContextProvider {
//    private WorkContexts ics = null;
private List<WorkContext> contextsList = new ArrayList<WorkContext>();
private MessageEndpoint ep;
private int numOfMessages ;
private int workCount = 1;
private String op = null;
private WorkManager wm;
private XATerminator xa;

public JSR322NestedWork(MessageEndpoint ep, int numOfMessages, String op, int workCount, WorkManager wm, XATerminator xa){
    super(ep, numOfMessages, op);
    this.workCount = workCount;
    this.ep = ep;
    this.numOfMessages = numOfMessages;
    this.op = op;
    this.wm = wm;
    this.xa = xa;
}

public JSR322NestedWork(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount, int workCount, WorkManager wm, XATerminator xa){
    super(ep, numOfMessages, op, keepCount);
    this.workCount = workCount;
    this.ep = ep;
    this.numOfMessages = numOfMessages;
    this.op = op;
    this.wm = wm;
    this.xa = xa;
}

public List<WorkContext> getWorkContexts() {
    return contextsList;
}


public void addWorkContext(WorkContext ic){
    contextsList.add(ic);
}

    public void run() {
        for(int i=0; i<workCount; i++){

            try{
            JSR322Work w1 = new JSR322Work(ep, numOfMessages, op);
            ExecutionContext ec1 = startTx();
            debug("executing nested work instance [ "+ i + " ] " );
            TransactionContext tic = new TransactionContext();
            tic.setXid(ec1.getXid());
            w1.addWorkContext(tic);

            wm.doWork(w1, 0, null, null);
            xa.commit(ec1.getXid(), true);
            debug("commiting nested work instance [ "+ i + " ] "  );
            }catch(Exception we){
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

    public void debug(String message){
        System.out.println("JSR-322 [RA] [JSR322NestedWork]: " + message);
    }

}
