package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.skuzzle.jeve.AbortionException;
import de.skuzzle.jeve.DefaultDispatchable;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SynchronousEvent;
import de.skuzzle.jeve.invoke.EventInvocation;
import de.skuzzle.jeve.invoke.FailedEventInvocation;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractEventProviderTest<T extends AbstractEventProvider> {

    protected static interface SampleListener extends Listener {
        public void onEvent(Event<?, SampleListener> e);
    }

    protected static interface SampleListener2 extends Listener {
        public void onEvent(SynchronousEvent<?, SampleListener2> e);
    }

    @Mock
    protected ListenerStore store;
    @Mock
    protected Event<Object, SampleListener> event;
    @Mock
    protected ExceptionCallback ec;
    @Mock
    protected SampleListener listener;

    protected T subject;

    protected abstract T createSubject(ListenerStore store);

    @Before
    public void setUp() throws Exception {
        this.subject = createSubject(this.store);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStoreNotNull() throws Exception {
        createSubject(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchEventIsNull() throws Exception {
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc = SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        this.subject.dispatch(null, bc, ec);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchEventIsNull2() throws Exception {
        this.subject.dispatch((DefaultDispatchable) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchEventIsNull3() throws Exception {
        this.subject.dispatch((Event<?, SampleListener>) null, SampleListener::onEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchBCIsNull() throws Exception {
        this.subject.dispatch(this.event, null, this.ec);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchBCisNull2() throws Exception {
        this.subject.dispatch(this.event, (BiConsumer<SampleListener, Event<?, SampleListener>>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchECIsNull() throws Exception {
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc = SampleListener::onEvent;
        this.subject.dispatch(this.event, bc, null);
    }

    @Test
    public void testGetStore() throws Exception {
        Assert.assertSame(this.store, this.subject.getListenerSource());
    }

    @Test
    public void testNotifySingleSuccess() throws Exception {
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        this.subject.notifySingle(this.listener, this.event, bc, this.ec);
        Mockito.verify(this.listener).onEvent(this.event);
        Mockito.verifyZeroInteractions(this.ec);
    }

    @Test
    public void testNotifySingleDelegateAbortionException() {
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        Mockito.doThrow(AbortionException.class).when(this.listener).onEvent(this.event);

        try {
            this.subject.notifySingle(this.listener, this.event, bc, this.ec);
            Assert.fail("Expected AbortionException");
        } catch (final AbortionException ex) {
            Mockito.verifyZeroInteractions(this.ec);
        }
    }

    @Test
    public void testNotifySingleHandleException() {
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final RuntimeException ex = new RuntimeException();
        Mockito.doThrow(ex).when(this.listener).onEvent(this.event);

        this.subject.notifySingle(this.listener, this.event, bc, this.ec);

        final FailedEventInvocation expected = EventInvocation
                .of(this.listener, this.event, SampleListener::onEvent, this.ec)
                .fail(ex);
        Mockito.verify(this.ec).exception(expected);
    }

    @Test
    public void testNotifyListeners() throws Exception {
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        Mockito.when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());

        this.subject.notifyListeners(this.event, SampleListener::onEvent, this.ec);
        final InOrder inOrder = Mockito.inOrder(this.event, this.listener, listener2);
        inOrder.verify(this.listener).onEvent(this.event);
        inOrder.verify(listener2).onEvent(this.event);
    }

    @Test
    public void testNotifyListenersHandledEvent() throws Exception {
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        Mockito.when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.event.isHandled()).thenReturn(true);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());

        this.subject.notifyListeners(this.event, SampleListener::onEvent, this.ec);
        final InOrder inOrder = Mockito.inOrder(this.event, this.listener, listener2);
        inOrder.verify(this.listener, Mockito.never()).onEvent(Mockito.any());
        inOrder.verify(listener2, Mockito.never()).onEvent(Mockito.any());
    }

    @Test
    public void testIsNotSequentialIfStoreIsNot() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(false);
        Assert.assertFalse(this.subject.isSequential());
    }

    @Test
    public void testSetExceptionCallback() throws Exception {
        this.subject.setExceptionCallback(this.ec);
        Assert.assertSame(this.ec, this.subject.exceptionHandler);
    }

    @Test
    public void testResetExceptionCallback() throws Exception {
        this.subject.setExceptionCallback(null);
        Assert.assertSame(this.subject.defaultHandler, this.subject.exceptionHandler);
    }

    @Test
    public void testDefaultDispatch() throws Exception {
        final DefaultDispatchable event = Mockito.mock(DefaultDispatchable.class);
        this.subject.dispatch(event);
        Mockito.verify(event).defaultDispatch(this.subject, this.subject.exceptionHandler);
    }

    @Test
    public void testDispatch() throws Exception {
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());
        Mockito.when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        this.subject.dispatch(this.event, SampleListener::onEvent);

        Mockito.verify(this.listener).onEvent(this.event);
        Mockito.verify(listener2).onEvent(this.event);
    }
}
