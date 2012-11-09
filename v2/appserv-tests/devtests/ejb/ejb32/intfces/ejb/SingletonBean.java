package ejb32.intrfaces;

import javax.ejb.*;
import javax.annotation.*;

@Remote
@Singleton
public class SingletonBean implements St1, St2{

    @EJB St3 st3;
    @EJB St4 st4;

    public String st1() {
	return st3.st3() + "st1";
    }

    public String st2() {
	return st4.st4() + "st2";
    }
}
