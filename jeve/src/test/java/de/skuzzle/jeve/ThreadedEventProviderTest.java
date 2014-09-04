package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.util.EventProviderFactory;


/**
 * Runs all basic tests for all default provided event providers.
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class ThreadedEventProviderTest extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     * @return Collection of parameters for the constructor of
     *          {@link EventProviderTestBase}.
     */
    @Parameters
    public final static Collection<Object[]> getParameters() {
        return Arrays.asList(
                new EventProviderFactory[] { EventProviders::newParallelEventProvider },
                new EventProviderFactory[] { EventProviders::newAsynchronousEventProvider }
        );
    }



    /**
     * Creates new BasicEventProviderTests
     *
     * @param factory Factory to create a single provider
     */
    public ThreadedEventProviderTest(EventProviderFactory factory) {
        super(factory);
    }
}
