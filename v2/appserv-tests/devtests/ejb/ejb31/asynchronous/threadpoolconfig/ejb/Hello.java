package test;
import javax.ejb.Remote;
import java.util.concurrent.*;

@Remote
public interface Hello {
    Future<String> getThreadNameId();
}
