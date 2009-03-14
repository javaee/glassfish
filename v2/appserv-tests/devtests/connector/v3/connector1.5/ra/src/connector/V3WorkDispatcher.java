package connector;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;

public class V3WorkDispatcher extends WorkDispatcher {
    public V3WorkDispatcher(String id, BootstrapContext ctx, MessageEndpointFactory factory, ActivationSpec spec) {
        super(id, ctx, factory, spec);
    }

    public void run() {

        try {
            synchronized (Controls.readyLock) {
                debug("WAIT...");
                Controls.readyLock.wait();


                if (stop) {
                    return;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
            MessageEndpoint ep = factory.createEndpoint(new FakeXAResource());
            int numOfMessages = 1;
            debug("V3WorkDispatcher sleeping");
            //Thread.sleep(10000);
            debug("V3WorkDispatcher woke up");
            //importing transaction

            //write/commit
            ExecutionContext ec = startTx();
            debug("Start TX - " + ec.getXid());

            debug("V3WorkDispatcher about to submit work");
            DeliveryWork w =
                    new DeliveryWork(ep, numOfMessages, "WRITE");
            wm.doWork(w, 0, null, null);
                      xa.commit(ec.getXid(), true);

            debug("V3WorkDispatcher done work");
            debug("DONE WRITE TO DB");
            Controls.expectedResults = numOfMessages;
            notifyAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            debug("V3WorkDispatcher calling DONE()");
            done();
            debug("V3WorkDispatcher finished calling DONE()");
            
        }
    }
}
