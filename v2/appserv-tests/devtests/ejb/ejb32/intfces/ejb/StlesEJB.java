package ejb32.intrfaces;


import javax.ejb.*;

@Local
@Stateless
public class StlesEJB implements St3, St4 {

    public String st3() {
        return "st3";
    }

    public String st4() {
        return "st4";
    }


}
