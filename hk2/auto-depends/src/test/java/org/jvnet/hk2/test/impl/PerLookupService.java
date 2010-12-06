package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

@Service
@Scoped(PerLookup.class)
public class PerLookupService {

}
