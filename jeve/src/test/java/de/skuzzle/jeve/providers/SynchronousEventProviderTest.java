package de.skuzzle.jeve.providers;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SynchronousEvent;

public class SynchronousEventProviderTest extends
        AbstractEventProviderTest<SynchronousEventProvider<ListenerStore>> {

    @Override
    protected SynchronousEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new SynchronousEventProvider<ListenerStore>(store);
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
        InOrder inOrder = Mockito.inOrder(event, this.listener, listener2);
        inOrder.verify(event).setListenerStore(this.store);
        inOrder.verify(event).setEventStack(this.subject.getEventStack());
        inOrder.verify(this.listener).onEvent(event);
        inOrder.verify(listener2).onEvent(event);
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }
}
