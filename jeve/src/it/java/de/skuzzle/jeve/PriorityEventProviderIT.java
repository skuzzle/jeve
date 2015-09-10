package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.stores.PriorityListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class PriorityEventProviderIT extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { (Function<ListenerStore, ? extends EventProvider>) SynchronousEventProvider::new, (Supplier<ListenerStore>) PriorityListenerStore::create },
                new Object[] {(Function<ListenerStore, ? extends EventProvider>) SynchronousEventProvider::new, (Supplier<ListenerStore>) () -> PriorityListenerStore.create(0) }
                );
    }

    public PriorityEventProviderIT(
            Function<ListenerSource, ? extends EventProvider> factory,
            Supplier<? extends ListenerStore> sourceFactory) {
        super(factory, sourceFactory);
    }

    private PriorityListenerStore getStore() {
        return (PriorityListenerStore) this.store;
    }

    @Test
    public void testPrioritization() {
        final StringListener l1 = Mockito.mock(StringListener.class);
        final StringListener l2 = Mockito.mock(StringListener.class);

        // Add l1 before l2, but l2 with lower precedence
        getStore().add(StringListener.class, l1, 2);
        getStore().add(StringListener.class, l2, 1);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        final InOrder inOrder = Mockito.inOrder(l2, l1);
        inOrder.verify(l2).onStringEvent(e);
        inOrder.verify(l1).onStringEvent(e);
    }

    @Test
    public void testDefaultPrioritization() {
        final StringListener l1 = Mockito.mock(StringListener.class);
        final StringListener l2 = Mockito.mock(StringListener.class);

        // Add l1 before l2, but l2 with lower precedence
        getStore().add(StringListener.class, l1);
        getStore().add(StringListener.class, l2, -1);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        final InOrder inOrder = Mockito.inOrder(l2, l1);
        inOrder.verify(l2).onStringEvent(e);
        inOrder.verify(l1).onStringEvent(e);
    }
}
