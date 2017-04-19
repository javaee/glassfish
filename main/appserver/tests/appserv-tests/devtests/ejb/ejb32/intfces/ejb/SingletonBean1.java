package ejb32.intrfaces;

import javax.ejb.*;
import javax.annotation.*;

/*
    SingletonBean1 exposes local interface St6. St5 isn't business interface
 */

@Remote
@Singleton
public class SingletonBean1 implements St5, St6 {

    public String st5() {
        return "SingletonBean1.st5";
    }

    public String st6() {
        return "SingletonBean1.st6";
    }
}
