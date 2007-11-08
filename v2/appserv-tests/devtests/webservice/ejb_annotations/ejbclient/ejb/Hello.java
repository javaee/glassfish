package ejb;
import javax.ejb.Remote;

@Remote
public interface Hello {
	public String invoke(String msg);
}
