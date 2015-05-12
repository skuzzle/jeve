package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ListenerStore;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousEventProviderTest extends
        AbstractExecutorAwareEventProviderTest<AsynchronousEventProvider<ListenerStore>> {

    @Override
    protected AsynchronousEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new AsynchronousEventProvider<ListenerStore>(store, this.executor);
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }

    @Override
    @Test
    public void testDispatch() {
        final AsynchronousEventProvider<ListenerStore> spy = Mockito.spy(this.subject);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);

        final BiConsumer<SampleListener, Event<?, SampleListener>> bc =
                SampleListener::onEvent;

        Mockito.when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(this.store.get(SampleListener.class)).thenReturn(
                Arrays.asList(this.listener, listener2).stream());
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        }).when(this.executor).execute(Mockito.any());

        spy.dispatch(this.event, bc, this.ec);
        Mockito.verify(spy).notifyListeners(this.event, bc, this.ec);
    }

}
