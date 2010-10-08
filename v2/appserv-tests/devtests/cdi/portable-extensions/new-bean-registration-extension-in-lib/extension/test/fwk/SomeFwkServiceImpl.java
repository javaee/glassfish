package test.fwk;

public class SomeFwkServiceImpl implements SomeFwkService {
    
    public SomeFwkServiceImpl(){
        System.out.println("SomeFrameworkClass:");
    }

    @Override
    public boolean fooMethod() {
        System.out.println("SomeFwkServiceImpl::fooMethod called");
        return true;
    }

}
