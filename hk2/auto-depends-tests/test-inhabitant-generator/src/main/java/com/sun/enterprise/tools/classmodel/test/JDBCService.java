package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.tools.classmodel.test.external.StandBy;

@StandBy
@Service(name="jdbc")
final public class JDBCService extends JDBCBaseService {
  
}
