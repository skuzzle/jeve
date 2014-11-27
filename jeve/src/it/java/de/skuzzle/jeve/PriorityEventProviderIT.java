package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.stores.PriorityListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class PriorityEventProviderIT extends
        EventProviderTestBase<PriorityListenerStore> {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { EventProvider.configure().store(new PriorityListenerStore()).useSynchronousProvider().createSupplier() },
                new Object[] { EventProvider.configure().store(new PriorityListenerStore(0)).useSynchronousProvider().createSupplier() }
                );
    }

    public PriorityEventProviderIT(
            Supplier<? extends EventProvider<PriorityListenerStore>> factory) {
        super(factory);
    }

    @Test
    public void testPrioritization() {
        final StringListener l1 = Mockito.mock(StringListener.class);
        final StringListener l2 = Mockito.mock(StringListener.class);

        // Add l1 before l2, but l2 with lower precedence
        this.subject.listeners().add(StringListener.class, l1, 2);
        this.subject.listeners().add(StringListener.class, l2, 1);

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
        this.subject.listeners().add(StringListener.class, l1);
        this.subject.listeners().add(StringListener.class, l2, -1);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        final InOrder inOrder = Mockito.inOrder(l2, l1);
        inOrder.verify(l2).onStringEvent(e);
        inOrder.verify(l1).onStringEvent(e);
    }
}
