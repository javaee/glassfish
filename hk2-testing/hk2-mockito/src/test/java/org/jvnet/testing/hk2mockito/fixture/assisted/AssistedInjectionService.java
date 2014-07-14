/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jvnet.testing.hk2mockito.fixture.assisted;

import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author saden
 */
@Service
public class AssistedInjectionService {

    private final CustomService customService;

    @Inject
    AssistedInjectionService(CustomService customService) {
        this.customService = customService;
    }

    public String greet() {
        return customService.greet();
    }

    public CustomService getCustomService() {
        return customService;
    }

}
