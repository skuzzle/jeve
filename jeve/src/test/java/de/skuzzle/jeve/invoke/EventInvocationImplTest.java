package de.skuzzle.jeve.invoke;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.skuzzle.jeve.AbortionException;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

@RunWith(MockitoJUnitRunner.class)
public class EventInvocationImplTest {

    protected static interface SampleListener extends Listener {
        public void onEvent(Event<?, SampleListener> e);
    }

    @Mock
    protected ListenerStore store;
    @Mock
    protected Event<Object, SampleListener> event;
    @Mock
    protected ExceptionCallback ec;
    @Mock
    protected SampleListener listener;

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testHandleExceptionSuccess() throws Exception {
        final RuntimeException ex = new RuntimeException();
        doThrow(ex).when(this.listener).onEvent(this.event);

        final EventInvocation inv = EventInvocation
                .of(this.listener, this.event, SampleListener::onEvent, this.ec);

        inv.notifyListener();
        final FailedEventInvocation failed = inv.toFailedInvocation(ex);
        verify(this.ec).exception(failed);
    }

    @Test
    public void testHandleExceptionSwallow() throws Exception {
        final RuntimeException ex = new RuntimeException();
        final EventInvocation inv = EventInvocation
                .of(this.listener, this.event, SampleListener::onEvent, this.ec);

        doThrow(ex).when(this.listener).onEvent(this.event);
        doThrow(new RuntimeException()).when(this.ec).exception(inv.toFailedInvocation(ex));

        inv.notifyListener();

        verify(this.ec).exception(inv.toFailedInvocation(ex));
    }

    @Test
    public void testHandleExceptionDelegateAbortionException() throws Exception {
        final RuntimeException ex = new RuntimeException();
        final EventInvocation inv = EventInvocation
                .of(this.listener, this.event, SampleListener::onEvent, this.ec);
        doThrow(new AbortionException()).when(this.ec).exception(inv.toFailedInvocation(ex));
        doThrow(ex).when(this.listener).onEvent(this.event);

        try {
            inv.notifyListener();
            Assert.fail("Expected AbortionException");
        } catch (AbortionException ex2) {
            Mockito.verify(this.ec).exception(inv.toFailedInvocation(ex));
        }
    }

    @Test
    public void testDoNotHandleAbortionException() throws Exception {
        final EventInvocation inv = EventInvocation
                .of(this.listener, this.event, SampleListener::onEvent, this.ec);
        doThrow(new AbortionException()).when(this.listener).onEvent(this.event);

        try {
            inv.notifyListener();
            Assert.fail("Expected AbortionException");
        } catch (AbortionException ex2) {
            Mockito.verifyZeroInteractions(this.ec);
        }
    }
}
