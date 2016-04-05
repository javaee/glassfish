/**
 * 
 */
package org.glassfish.hk2.tests.locator.lambda;

import javax.inject.Singleton;

/**
 * @author jwells
 *
 */
@Singleton
public class AAndB {
	public int getA() {
		return 0;
	}
	
	public int getB() {
		return 1;
	}

}
