package ejb;
import javax.ejb.Remote;

@Remote
public interface Hello {
	public String invokeSync(String msg);
	public String invokeAsyncPoll(String msg);
	public String invokeAsyncCallBack(String msg);
}
