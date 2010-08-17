package connector;

import javax.resource.spi.work.WorkContextProvider;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.endpoint.MessageEndpoint;
import java.util.List;
import java.util.ArrayList;


public class JSR322Work extends DeliveryWork implements WorkContextProvider {

//    private WorkContexts ics = null;
    private List<WorkContext> contextsList = new ArrayList<WorkContext>();


    public JSR322Work(MessageEndpoint ep, int numOfMessages, String op){
        super(ep, numOfMessages, op);
    }

    public JSR322Work(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount){
        super(ep, numOfMessages, op, keepCount);
    }

    public List<WorkContext> getWorkContexts() {
        return contextsList;
    }


    public void addWorkContext(WorkContext ic){
        contextsList.add(ic);
    }

    public void run(){
        super.run();
        
    }
}
