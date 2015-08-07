package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import de.skuzzle.jeve.DefaultDispatchable;

public abstract class AbstractExecutorAwareEventProviderTest<E extends AbstractEventProvider>
        extends AbstractEventProviderTest<E> {

    @Mock
    protected ExecutorService executor;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assert.assertTrue(this.subject instanceof ExecutorAware);
        Mockito.when(this.executor.isShutdown()).thenReturn(false);
        Mockito.when(this.executor.isTerminated()).thenReturn(false);
    }

    private ExecutorAware subject() {
        return (ExecutorAware) this.subject;
    }

    @Test
    public void testCanNotDefaultDispatch() throws Exception {
        Mockito.when(this.executor.isShutdown()).thenReturn(true);
        final DefaultDispatchable event = Mockito.mock(DefaultDispatchable.class);
        this.subject.dispatch(event);
        Mockito.verify(this.executor, Mockito.never()).execute(Mockito.any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExecutorNull() throws Exception {
        subject().setExecutorService(null);
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
    public void testSetExecutorService() {
        final ExecutorService es = Mockito.mock(ExecutorService.class);
        subject().setExecutorService(es);
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
