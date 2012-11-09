package ejb32.intrfaces;

import javax.ejb.*;
import javax.annotation.*;

@Remote
@Singleton
public class SingletonBean1 implements St5, St6{

    public String st5() {
	return "st5";
    }

    public String st6() {
	return "st6";
    }
}
