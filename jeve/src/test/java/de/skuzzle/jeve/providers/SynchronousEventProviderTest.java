package de.skuzzle.jeve.providers;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SynchronousEvent;

public class SynchronousEventProviderTest extends
        AbstractEventProviderTest<SynchronousEventProvider> {

    @Override
    protected SynchronousEventProvider createSubject(ListenerStore store) {
        return new SynchronousEventProvider(store);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNotifyListenersSynchronousEvent() throws Exception {
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        final SynchronousEvent<?, SampleListener> event = Mockito.mock(SynchronousEvent.class);
        Mockito.when(event.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());

        this.subject.notifyListeners(event, SampleListener::onEvent, this.ec);
        final InOrder inOrder = Mockito.inOrder(event, this.listener, listener2);
        inOrder.verify(event).setListenerStore(this.store);
        inOrder.verify(event).setEventStack(this.subject.getEventStack());
        inOrder.verify(this.listener).onEvent(event);
        inOrder.verify(listener2).onEvent(event);
    }

    @Test
    public void testSetCause() throws Exception {
        final Event<?, SampleListener> event = new Event<Object, SampleListener>(this, SampleListener.class);
        final SampleListener listener = new SampleListener() {
            @Override
            public void onEvent(Event<?, SampleListener> e) {
                final SynchronousEvent<Object, SampleListener2> e2 =
                        new SynchronousEvent<Object, SampleListener2>(this, SampleListener2.class);
                SynchronousEventProviderTest.this.subject.dispatch(e2, SampleListener2::onEvent);
            }
        };
        final SampleListener2 listener2 = new SampleListener2() {

            @Override
            public void onEvent(SynchronousEvent<?, SampleListener2> e) {
                e.getEventStack().get().dumpStack(System.out);
                assertTrue(e.getCause().isPresent());
                assertSame(event, e.getCause().get());
            }

        };

        Mockito.when(this.store.get(SampleListener.class)).thenReturn(Arrays.asList(listener).stream());
        Mockito.when(this.store.get(SampleListener2.class)).thenReturn(Arrays.asList(listener2).stream());
        this.subject.dispatch(event, SampleListener::onEvent);
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }
}
