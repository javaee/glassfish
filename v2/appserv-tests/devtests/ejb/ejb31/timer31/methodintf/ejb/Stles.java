package ejb31.timer.methodintf;

@javax.ejb.Remote
public interface Stles {

    public boolean verifyTimers() throws Exception;
    public void createTimer() throws Exception;
}
