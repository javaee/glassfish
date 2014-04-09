package org.glassfish.hk2.tests.locator.qualifiers;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class BlackInjectee {

    private final Color black;

    @Inject
    public BlackInjectee(@Black Color black) {
        this.black = black;
    }

    public Color getBlack() {
        return black;
    }
}
