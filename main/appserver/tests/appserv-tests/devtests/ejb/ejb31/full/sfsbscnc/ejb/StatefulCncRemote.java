package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Remote
public interface StatefulCncRemote extends StatefulCncSuperIntf {}
