package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import de.skuzzle.jeve.DefaultDispatchEvent;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousEventProviderTest extends
        AbstractEventProviderTest<AsynchronousEventProvider<ListenerStore>> {

    @Mock
    private ExecutorService executor;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Mockito.when(this.executor.isShutdown()).thenReturn(false);
        Mockito.when(this.executor.isTerminated()).thenReturn(false);
    }

    @Override
    protected AsynchronousEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new AsynchronousEventProvider<ListenerStore>(store, this.executor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExecutorNull() throws Exception {
        this.subject.setExecutorService(null);
    }

    @Test
    public void testCanDispatch() throws Exception {
        Mockito.when(this.executor.isShutdown()).thenReturn(false);
        Mockito.when(this.executor.isTerminated()).thenReturn(false);
        Assert.assertTrue(this.subject.canDispatch());
    }

    @Test
    public void testCanDispatchShutdown() throws Exception {
        Mockito.when(this.executor.isShutdown()).thenReturn(true);
        Mockito.when(this.executor.isTerminated()).thenReturn(false);
        Assert.assertFalse(this.subject.canDispatch());
    }

    @Test
    public void testCanDispatchTermianted() throws Exception {
        Mockito.when(this.executor.isShutdown()).thenReturn(false);
        Mockito.when(this.executor.isTerminated()).thenReturn(true);
        Assert.assertFalse(this.subject.canDispatch());
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }

    @Test
    public void testCanNotDispatch() throws Exception {
        Mockito.when(this.executor.isShutdown()).thenReturn(true);
        final DefaultDispatchEvent<?, SampleListener> event = Mockito.mock(DefaultDispatchEvent.class);
        this.subject.dispatch(event);
        Mockito.verify(this.executor, Mockito.never()).execute(Mockito.any());
    }

    @Test
    public void testDispatch() {
        final AsynchronousEventProvider<ListenerStore> spy = Mockito.spy(this.subject);
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);

        final Event<?, SampleListener> event = Mockito.mock(Event.class);
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;

        Mockito.when(event.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(listener1, listener2).stream());
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        }).when(this.executor).execute(Mockito.any());

        Mockito.doReturn(true).when(spy).notifyListeners(event, bc, ec);
        spy.dispatch(event, bc, ec);
        Mockito.verify(spy).notifyListeners(event, bc, ec);
    }

    @Test
    public void testClose2() throws Exception {
        this.subject.close();
        Mockito.verify(this.executor).shutdownNow();
        Mockito.verify(this.executor).awaitTermination(2000L, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testCloseAwaitTerminationFails() throws Exception {
        Mockito.doThrow(InterruptedException.class).when(this.executor)
                .awaitTermination(Mockito.anyLong(), Mockito.any());
        this.subject.close();
        Mockito.verify(this.executor).shutdownNow();
    }
}
