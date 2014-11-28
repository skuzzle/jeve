package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventStack;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SynchronousEvent;

public class SynchronousEventProviderTest extends AbstractEventProviderTest {

    @Override
    protected AbstractEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new SynchronousEventProvider<ListenerStore>(store);
    }

    @Test
    public void testNotifyListenersSynchronousEvent() throws Exception {
        final AbstractEventProvider<ListenerStore> spy = Mockito.spy(this.subject);
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final SynchronousEvent<?, SampleListener> e = Mockito.mock(SynchronousEvent.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;

        Mockito.when(e.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(listener1, listener2).stream());

        final EventStack stack = ((SynchronousEventProvider) this.subject).getEventStack();

        spy.notifyListeners(e, SampleListener::onEvent, ec);
        InOrder inOrder = Mockito.inOrder(e, listener1, listener2);
        inOrder.verify(e).setListenerStore(this.store);
        inOrder.verify(e).setEventStack(stack);
        inOrder.verify(listener1).onEvent(e);
        inOrder.verify(listener2).onEvent(e);
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }
}
