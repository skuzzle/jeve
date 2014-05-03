package de.skuzzle.jeve.util;

import org.junit.After;
import org.junit.Before;

import de.skuzzle.jeve.EventProvider;

/**
 * Base class for setting up an EventProvider testing area.
 * 
 * @author Simon Taddiken
 * @since 1.1.0
 */
public class AbstractEventProviderTest {
    
    /** Time to wait to give threaded handlers some time to finish one dispatch action */
    protected final static long THREAD_WAIT_TIME = 150; // ms
    
    /** The factory to create EventProvider instances for testing */
    protected final EventProviderFactory factory;
    
    /** The provider being used in a single test case */
    protected EventProvider subject;
    
    
    
    
    /**
     * Creates a new Test class instance.
     * @param factory A factory for creating event providers
     */
    public AbstractEventProviderTest(EventProviderFactory factory) {
        this.factory = factory;
    }
    
    
    
    /**
     * Sets up a single test case by creating a new {@link EventProvider} using the 
     * factory provided by the constructor.
     */
    @Before
    public void setUp() {
        this.subject = this.factory.create();
    }
    
    
    
    /**
     * Closes the current test subject
     */
    @After
    public void tearDown() {
        this.subject.close();
    }
    
    
    
    /**
     * Constructs an error string containing the name of the currently tested 
     * {@link EventProviderTestBase}.
     * 
     * @param fail The reason why a test failed.
     * @return The constructed string.
     */
    protected String getFailString(String fail) {
        return this.subject.getClass().getSimpleName() + ": " + fail;
    }
    
    
    
    /**
     * Checks whether a test case should be skipped because it does not work for 
     * non-sequential EventProviders.
     * 
     * <p>A warning is printed to the error console if the test case is skipped.</p>
     * 
     * @return Whether to skip the test case.
     * @since 1.1.0
     */
    protected boolean checkSkipNonSequential() {
        if (!this.subject.isSequential()) {
            System.err.println("Skipping test case because '" + 
                this.subject.getClass().getSimpleName() + "' is not sequential");
            return true;
        }
        return false;
    }
}
