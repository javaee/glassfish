/**
 * 
 */
package org.glassfish.hk2.utilities;

/**
 * The mode of the duplicate post processor
 * 
 * @author jwells
 *
 */
public enum DuplicatePostProcessorMode {
	/** Uses the equals method of {@link DescriptorImpl} which includes most of the fields of the descriptor */
	STRICT,
	
	/** Uses only the {@link org.glassfish.hk2.api.Descriptor#getImplementation()} method to compare descriptors */
	IMPLEMENTATION_ONLY

}
