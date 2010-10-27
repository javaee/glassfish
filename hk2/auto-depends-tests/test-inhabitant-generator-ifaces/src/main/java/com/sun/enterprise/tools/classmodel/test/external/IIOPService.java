package com.sun.enterprise.tools.classmodel.test.external;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

@RunLevel(2)
@Service(name="corba")
public class IIOPService extends AbstractServerService {

}
