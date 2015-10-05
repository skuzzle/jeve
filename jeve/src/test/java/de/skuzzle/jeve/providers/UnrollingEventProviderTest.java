package de.skuzzle.jeve.providers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Test;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SequentialEvent;

public class UnrollingEventProviderTest extends
        AbstractEventProviderTest<UnrollingEventProvider<ListenerStore>> {

    @Override
    protected UnrollingEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new UnrollingEventProvider<ListenerStore>(store);
    }

    @Test
    public void testNestedDispatch() {
        final SequentialEvent<?, SampleListener2> e2 =
                new SequentialEvent<>(this, SampleListener2.class);
        final SampleListener l = new SampleListener() {

            @Override
            public void onEvent(Event<?, SampleListener> e) {
                UnrollingEventProviderTest.this.subject.dispatch(e2, SampleListener2::onEvent);
            }

        };
        final SampleListener2 l2 = mock(SampleListener2.class);
        when(this.event.getListenerClass()).thenReturn(SampleListener.class);
        when(this.store.get(SampleListener.class)).thenReturn(Stream.of(l));
        when(this.store.get(SampleListener2.class)).thenReturn(Stream.of(l2));
        this.subject.dispatch(this.event, SampleListener::onEvent);

        verify(l2).onEvent(e2);
    }
}
