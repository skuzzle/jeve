package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.stores.DefaultListenerStore;

/**
 * Runs all basic tests for all default provided event providers.
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class ThreadedEventProviderTest extends
        EventProviderTestBase<DefaultListenerStore> {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { EventProvider.configure().defaultStore().with().parallelProvider().asSupplier() },
                new Object[] { EventProvider.configure().defaultStore().with().asynchronousProvider().asSupplier() },
                new Object[] { EventProvider.configure().defaultStore().with().parallelProvider().and().statistics().asSupplier() },
                new Object[] { EventProvider.configure().defaultStore().with().asynchronousProvider().and().statistics().asSupplier() }
                );
    }

    /**
     * Creates new BasicEventProviderTests
     *
     * @param factory Factory to create a single provider
     */
    public ThreadedEventProviderTest(
            Supplier<? extends EventProvider<DefaultListenerStore>> factory) {
        super(factory);
    }
}
