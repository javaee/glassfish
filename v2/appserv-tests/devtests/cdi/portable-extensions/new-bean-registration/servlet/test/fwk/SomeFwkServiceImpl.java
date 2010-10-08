package test.fwk;

public class SomeFwkServiceImpl implements SomeFwkServiceInterface {
    
    public SomeFwkServiceImpl(String t){
        System.out.println("SomeFrameworkClass:" + t);
    }

    @Override
    public boolean fooMethod() {
        System.out.println("SomeFwkServiceImpl::fooMethod called");
        return true;
    }

}
