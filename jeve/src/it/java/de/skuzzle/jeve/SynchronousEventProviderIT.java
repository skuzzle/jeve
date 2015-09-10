package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.invoke.FailedEventInvocation;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;
import de.skuzzle.jeve.stores.PriorityListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class SynchronousEventProviderIT extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) SynchronousEventProvider::new,
                        (Supplier<ListenerStore>) DefaultListenerStore::create
                    },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) SynchronousEventProvider::new,
                        (Supplier<ListenerStore>) PriorityListenerStore::create
                    }
                );
    }

    public SynchronousEventProviderIT(
            Function<ListenerSource, ? extends EventProvider> factory,
            Supplier<? extends ListenerStore> sourceFactory) {
        super(factory, sourceFactory);
    }

    /**
     * Tests whether {@link AbortionException} is delegated to the caller of
     * dispatch. This behavior is only defined for synchronous providers
     */
    @Test(expected = AbortionException.class)
    public void testAbortionExceptionInCallback() {
        final ExceptionCallback ec = new ExceptionCallback() {
            @Override
            public void exception(FailedEventInvocation invocation) {
                throw new AbortionException();
            }
        };
        final StringListener l = event -> {
            throw new RuntimeException();
        };
        this.subject.setExceptionCallback(ec);
        this.store.add(StringListener.class, l);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }
}
