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
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractEventProviderTest<T extends AbstractEventProvider<ListenerStore>> {

    protected interface SampleListener extends Listener {
        public void onEvent(Event<?, SampleListener> e);
    }

    @Mock
    protected ListenerStore store;

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
        this.subject.dispatch((Event<?, ?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchEventIsNull3() throws Exception {
        this.subject.dispatch((Event<?, SampleListener>) null, SampleListener::onEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchBCIsNull() throws Exception {
        final Event<?, SampleListener> event = Mockito.mock(Event.class);
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        this.subject.dispatch(event, null, ec);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchBCisNull2() throws Exception {
        final Event<?, SampleListener> event = Mockito.mock(Event.class);
        this.subject.dispatch(event, (BiConsumer<SampleListener, Event<?, SampleListener>>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDispatchECIsNull() throws Exception {
        final Event<?, SampleListener> event = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc = SampleListener::onEvent;
        this.subject.dispatch(event, bc, null);
    }

    @Test
    public void testGetStore() throws Exception {
        Assert.assertSame(this.store, this.subject.listeners());
    }

    @Test
    public void testNotifySingleSuccess() throws Exception {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final SampleListener listener = Mockito.mock(SampleListener.class);
        this.subject.notifySingle(listener, e, bc, ec);
        Mockito.verify(listener).onEvent(e);
        Mockito.verifyZeroInteractions(ec);
    }

    @Test
    public void testNotifySingleDelegateAbortionException() {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final SampleListener listener = Mockito.mock(SampleListener.class);
        Mockito.doThrow(AbortionException.class).when(listener).onEvent(e);

        try {
            this.subject.notifySingle(listener, e, bc, ec);
            Assert.fail("Expected AbortionException");
        } catch (AbortionException ex) {
            Mockito.verifyZeroInteractions(ec);
        }
    }

    @Test
    public void testNotifySingleHandleException() {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final SampleListener listener = Mockito.mock(SampleListener.class);
        final RuntimeException ex = new RuntimeException();
        Mockito.doThrow(ex).when(listener).onEvent(e);

        this.subject.notifySingle(listener, e, bc, ec);
        Mockito.verify(ec).exception(ex, listener, e);
    }

    @Test
    public void testHandleExceptionSuccess() throws Exception {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final RuntimeException ex = new RuntimeException();
        final SampleListener listener = Mockito.mock(SampleListener.class);

        this.subject.handleException(ec, ex, listener, e);
        Mockito.verify(ec).exception(ex, listener, e);
    }

    @Test
    public void testHandleExceptionSwallow() throws Exception {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final RuntimeException ex = new RuntimeException();
        final SampleListener listener = Mockito.mock(SampleListener.class);
        Mockito.doThrow(new RuntimeException()).when(ec).exception(ex, listener, e);

        this.subject.handleException(ec, ex, listener, e);
        Mockito.verify(ec).exception(ex, listener, e);
    }

    @Test
    public void testHandleExceptionDelegateAbortionException() throws Exception {
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final RuntimeException ex = new RuntimeException();
        final SampleListener listener = Mockito.mock(SampleListener.class);
        Mockito.doThrow(new AbortionException()).when(ec).exception(ex, listener, e);

        try {
            this.subject.handleException(ec, ex, listener, e);
            Assert.fail("Expected AbortionException");
        } catch (AbortionException ex2) {
            Mockito.verify(ec).exception(ex, listener, e);
        }
    }

    @Test
    public void testNotifyListeners() throws Exception {
        final AbstractEventProvider<ListenerStore> spy = Mockito.spy(this.subject);
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;

        Mockito.when(e.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(listener1, listener2).stream());

        spy.notifyListeners(e, SampleListener::onEvent, ec);
        InOrder inOrder = Mockito.inOrder(e, listener1, listener2);
        inOrder.verify(e).setListenerStore(this.store);
        inOrder.verify(listener1).onEvent(e);
        inOrder.verify(listener2).onEvent(e);
    }

    @Test
    public void testNotifyListenersHandledEvent() throws Exception {
        final AbstractEventProvider<ListenerStore> spy = Mockito.spy(this.subject);
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final Event<?, SampleListener> e = Mockito.mock(Event.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;

        Mockito.when(e.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(e.isHandled()).thenReturn(true);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(listener1, listener2).stream());

        spy.notifyListeners(e, SampleListener::onEvent, ec);
        InOrder inOrder = Mockito.inOrder(e, listener1, listener2);
        inOrder.verify(e).setListenerStore(this.store);
        inOrder.verify(listener1, Mockito.never()).onEvent(Mockito.any());
        inOrder.verify(listener2, Mockito.never()).onEvent(Mockito.any());
    }

    @Test
    public void testIsNotSequentialIfStoreIsNot() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(false);
        Assert.assertFalse(this.subject.isSequential());
    }

    @Test
    public void testClose() throws Exception {
        this.subject.close();
        Mockito.verify(this.store).close();
    }

    @Test
    public void testSetExceptionCallback() throws Exception {
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        this.subject.setExceptionCallback(ec);
        Assert.assertSame(ec, this.subject.exceptionHandler);
    }

    @Test
    public void testResetExceptionCallback() throws Exception {
        this.subject.setExceptionCallback(null);
        Assert.assertSame(this.subject.defaultHandler, this.subject.exceptionHandler);
    }

    @Test
    public void testDefaultDispatch() throws Exception {
        final Event<?, SampleListener> event = Mockito.mock(Event.class);
        this.subject.dispatch(event);
        Mockito.verify(event).defaultDispatch(this.subject, this.subject.exceptionHandler);
    }
}
