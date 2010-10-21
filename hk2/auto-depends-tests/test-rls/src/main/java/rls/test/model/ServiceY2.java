package rls.test.model;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

@RunLevel(DefaultRunLevelService.KERNEL_RUNLEVEL)
@Service
public class ServiceY2 implements ContractY {

}
