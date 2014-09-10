package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.util.EventProviderFactory;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class PriorityEventProviderTest extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new EventProviderFactory[] { () -> EventProviders.newPriorityEventProvider(
                        EventProviders.newDefaultEventProvider()) },
                new EventProviderFactory[] { () -> EventProviders.newPriorityEventProvider(
                        EventProviders.newDefaultEventProvider(), 0) });
    }

    public PriorityEventProviderTest(EventProviderFactory factory) {
        super(factory);
    }

    private PriorityEventProvider subject() {
        return (PriorityEventProvider) this.subject;
    }

    @Test
    public void testPrioritization() {
        final StringListener l1 = Mockito.mock(StringListener.class);
        final StringListener l2 = Mockito.mock(StringListener.class);

        // Add l1 before l2, but l2 with lower precedence
        subject().addListener(StringListener.class, l1, 2);
        subject().addListener(StringListener.class, l2, 1);

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
        subject().addListener(StringListener.class, l1);
        subject().addListener(StringListener.class, l2, -1);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        final InOrder inOrder = Mockito.inOrder(l2, l1);
        inOrder.verify(l2).onStringEvent(e);
        inOrder.verify(l1).onStringEvent(e);
    }
}
