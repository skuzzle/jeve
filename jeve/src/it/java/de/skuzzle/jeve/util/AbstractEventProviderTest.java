package de.skuzzle.jeve.util;

import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.EventProviderTestBase;

/**
 * Base class for setting up an EventProvider testing area.
 *
 * @author Simon Taddiken
 * @since 1.1.0
 * @version 2.0.0
 */
public class AbstractEventProviderTest {

    /**
     * Time to wait to give threaded handlers some time to finish one dispatch
     * action
     */
    private static final long THREAD_WAIT_TIME = 150; // ms

    /** The factory to create EventProvider instances for testing */
    protected final Supplier<? extends EventProvider> factory;

    /** The provider being used in a single test case */
    protected EventProvider subject;

    /**
     * Creates a new Test class instance.
     *
     * @param factory A factory for creating event providers
     */
    public AbstractEventProviderTest(Supplier<? extends EventProvider> factory) {
        this.factory = factory;
    }

    /**
     * Sets up a single test case by creating a new {@link EventProvider} using
     * the factory provided by the constructor.
     */
    @Before
    public void setUp() {
        this.subject = this.factory.get();
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
     * <p>
     * A warning is printed to the error console if the test case is skipped.
     * </p>
     *
     * @return Whether to skip the test case.
     * @since 1.1.0
     */
    protected boolean checkSkipNonSequential() {
        Assume.assumeTrue(this.subject.isSequential());
        if (!this.subject.isSequential()) {
            System.err.println("Skipping test case because '" +
                    this.subject.getClass().getSimpleName() + "' is not sequential");
            return true;
        }
        return false;
    }

    /**
     * Waits a certain amount of milliseconds to let threaded providers finish
     * before performing assertions.
     *
     * @since 2.0.0
     */
    protected void sleep() {
        try {
            Thread.sleep(THREAD_WAIT_TIME);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);

        }
    }
}
