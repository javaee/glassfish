package test;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

@Service
public class TestModuleStartup implements ModuleStartup {

	public static boolean wasCalled;
	
	@Override
	public void setStartupContext(StartupContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		System.out.println(">>>>>>>>>>>>>>>>> HERE");
	
		wasCalled=true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
