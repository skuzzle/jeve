package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.stores.PriorityListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class SynchronousEventProviderIT extends
        EventProviderTestBase<ListenerStore> {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { EventProvider.configure()
                        .defaultStore()
                        .useSynchronousProvider().and()
                        .exceptionCallBack(SampleExceptionCallback::new)
                        .createSupplier() },
                new Object[] { EventProvider.configure()
                        .store(PriorityListenerStore.create())
                        .useSynchronousProvider().and()
                        .synchronizeStore().and()
                        .statistics()
                        .createSupplier() }
                );
    }

    public SynchronousEventProviderIT(
            Supplier<? extends EventProvider<ListenerStore>> factory) {
        super(factory);
    }

    /**
     * Tests whether {@link AbortionException} is delegated to the caller of
     * dispatch. This behavior is only defined for synchronous providers
     */
    @Test(expected = AbortionException.class)
    public void testAbortionExceptionInCallback() {
        final ExceptionCallback ec = new ExceptionCallback() {
            @Override
            public void exception(EventProvider<?> provider, Exception e,
                    Listener source, Event<?, ?> cause) {
                throw new AbortionException();
            }
        };
        final StringListener l = event -> {
            throw new RuntimeException();
        };
        this.subject.setExceptionCallback(ec);
        this.subject.listeners().add(StringListener.class, l);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }
}
