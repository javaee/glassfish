package connector;

import javax.resource.spi.work.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.ArrayList;

public class NestedWork_Child extends DeliveryWork implements WorkContextProvider {
//    private WorkContexts ics = null;
private List<WorkContext> contextsList = new ArrayList<WorkContext>();
private MessageEndpoint ep;
private int numOfMessages ;
private int workCount = 1;
private String op = null;
private WorkManager wm;
private XATerminator xa;
private boolean transacted;
private boolean translationRequired;

public NestedWork_Child(MessageEndpoint ep, int numOfMessages, String op, boolean transacted
                        , boolean translationRequired){
    super(ep, numOfMessages, op);
    this.ep = ep;
    this.numOfMessages = numOfMessages;
    this.op = op;
    this.transacted = transacted;
    this.translationRequired = translationRequired;
}

public NestedWork_Child(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount,
                        int workCount, WorkManager wm, XATerminator xa, boolean transacted
                        , boolean translationRequired){
    super(ep, numOfMessages, op, keepCount);
    this.workCount = workCount;
    this.ep = ep;
    this.numOfMessages = numOfMessages;
    this.op = op;
    this.wm = wm;
    this.xa = xa;
    this.transacted = transacted;
    this.translationRequired = translationRequired;
}

    public List<WorkContext> getWorkContexts() {
    return contextsList;
}


public void addWorkContext(WorkContext ic){
    contextsList.add(ic);
}

    public void run() {
        debug("executing nested work - child");
        if(transacted){
            super.run();
        }
        debug("completed executing nested work - child");
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [NestedWork - Child]: " + message);
    }

}
