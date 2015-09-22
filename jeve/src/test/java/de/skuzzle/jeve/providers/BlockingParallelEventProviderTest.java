package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.ListenerStore;


public class BlockingParallelEventProviderTest extends AbstractExecutorAwareEventProviderTest<BlockingParallelEventProvider>{

    @Override
    protected BlockingParallelEventProvider createSubject(ListenerStore store) {
        return new BlockingParallelEventProvider(store, this.executor);
    }

    @Override
    @Test
    public void testDispatch() throws Exception {
        final ExecutorService exec = Executors.newCachedThreadPool();
        this.subject.setExecutorService(exec);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        Mockito.when(this.store.get(this.event)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());
        Mockito.when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        this.subject.dispatch(this.event, SampleListener::onEvent);

        Mockito.verify(this.listener).onEvent(this.event);
        Mockito.verify(listener2).onEvent(this.event);
    }
}
