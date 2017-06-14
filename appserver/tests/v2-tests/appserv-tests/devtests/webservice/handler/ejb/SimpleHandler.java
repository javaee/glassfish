package ejb;

import java.util.Date;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.*;

public class SimpleHandler extends GenericHandler {
    
    protected HandlerInfo info = null;
    
    public void init(HandlerInfo info) {
        this.info = info;
    }
    
    public boolean handleRequest(MessageContext context) {
        try {
            Date startTime = new Date();
            context.setProperty("startTime", startTime);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean handleResponse(MessageContext context) {
        try {
            Date startTime = (Date) context.getProperty("startTime");
            Date endTime = new Date();
            long elapsed = endTime.getTime() - startTime.getTime();
            System.out.println(" in handler, elapsed " + elapsed);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public QName[] getHeaders() {
        return new QName[0];
    }
}