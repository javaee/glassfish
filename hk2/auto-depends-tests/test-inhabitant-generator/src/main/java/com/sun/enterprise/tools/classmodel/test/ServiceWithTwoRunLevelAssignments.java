package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

@Admin  // this is not a mistake, we want to have two run level assignments for testing purposes
@RunLevel(55)
@Service
public class ServiceWithTwoRunLevelAssignments {

}
