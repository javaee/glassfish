package com.sun.enterprise.tools.classmodel.test.external;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * A service that should be filtered out from apt-test completely
 */
@RunLevel(2)
@Service(name="corba")
public class IIOPService extends AbstractServerService {

}
