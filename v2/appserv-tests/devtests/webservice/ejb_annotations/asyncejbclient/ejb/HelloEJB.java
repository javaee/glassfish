package ejb;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import endpoint.SayHelloResponse;

@Stateless 
public class HelloEJB implements Hello {


   @WebServiceRef
   WebServiceEJBService webService;
 
    public String invokeSync(String string) {
	WebServiceEJB ejb = webService.getWebServiceEJBPort();
	return ejb.sayHello("SYNC CALL" + string);
   }

   public String invokeAsyncPoll(String msg) {
       try {
	    WebServiceEJB ejb = webService.getWebServiceEJBPort();
            Response<SayHelloResponse> resp = ejb.sayHelloAsync("ASYNC POLL CALL" + msg);
	    Thread.sleep (2000);
	    SayHelloResponse out = resp.get();
            return(out.getReturn());
       } catch(Throwable t) {
            return(t.getMessage());
       }
   }

   public String invokeAsyncCallBack(String msg) {
	try {
System.out.println("VIJ - invoking async call back");
	    WebServiceEJB ejb = webService.getWebServiceEJBPort();
            MyCallBackHandler cbh = new MyCallBackHandler();
            Future<?> response =
                ejb.sayHelloAsync("ASYNC CALL BACK CALL" + msg, cbh);
            Thread.sleep (2000);
            SayHelloResponse out = cbh.getResponse ();
            return(out.getReturn());
        } catch(Throwable t) {
            return(t.getMessage());
        }
   }

   // The actual call back handler
   private class MyCallBackHandler implements
                    AsyncHandler<SayHelloResponse> {
        private SayHelloResponse output;
        public void handleResponse (Response<SayHelloResponse> response) {
            try {
                output = response.get ();
            } catch (ExecutionException e) {
                e.printStackTrace ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }

        SayHelloResponse getResponse (){
            return output;
        }
    }
}
