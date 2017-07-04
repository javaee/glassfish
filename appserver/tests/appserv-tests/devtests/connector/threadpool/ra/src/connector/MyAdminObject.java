
package connector;

public class MyAdminObject implements java.io.Serializable {

    Controls control = Controls.getControls();

    public void setup() {
        control.setupInitialWorks();
    }

    public void submit() {
        control.startTestWorks();
    }

    public void triggerWork() {
        control.trigger();
    }

    public void checkResult () {
        control.checkResult();
    }

    public void setNumberOfSetupWorks(int count) {
        control.setNumberOfSetupWorks(count);
    }
                                 
    public int getNumberOfSetupWorks() {
        return control.getNumberOfSetupWorks();
    }
     
    public void setNumberOfActualWorks(int num) {
        control.setNumberOfActualWorks(num);
    }
     
    public int getNumberOfActualWorks() {
        return control.getNumberOfActualWorks();
    }

}

