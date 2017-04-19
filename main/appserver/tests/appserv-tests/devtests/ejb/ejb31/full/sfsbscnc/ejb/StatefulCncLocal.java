package com.acme;

import javax.ejb.*;
import java.util.concurrent.*;

@Local
public interface StatefulCncLocal extends StatefulCncSuperIntf {}