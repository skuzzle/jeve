package de.skuzzle.jeve.providers;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.SequentialEvent;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.providers.EventStackImpl;

public class EventStackImplTest {

    @ListenerInterface
    private interface SampleListener extends Listener {

    }

    @ListenerInterface
    private interface OtherListener extends Listener {

    }

    private EventStackImpl subject;

    @Before
    public void setup() {
        this.subject = new EventStackImpl();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyPop() {
        final Event<?, ?> event = Mockito.mock(Event.class);
        this.subject.popEvent(event);
    }

    @Test(expected = IllegalStateException.class)
    public void testUnbalancedPop() {
        final Event<?, ?> event1 = Mockito.mock(Event.class);
        final Event<?, ?> event2 = Mockito.mock(Event.class);
        this.subject.pushEvent(event1);
        this.subject.popEvent(event2);
    }

    @Test
    public void testPushPop() {
        final Event<?, ?> event1 = Mockito.mock(Event.class);
        final Event<?, ?> event2 = Mockito.mock(Event.class);
        this.subject.pushEvent(event1);
        this.subject.pushEvent(event2);
        this.subject.popEvent(event2);
        this.subject.popEvent(event1);
    }

    @Test
    public void testPrevented() {
        final SequentialEvent<?, SampleListener> first = new SequentialEvent<>(new Object(), SampleListener.class);
        final SequentialEvent<?, SampleListener> second = new SequentialEvent<>(new Object(), SampleListener.class);

        this.subject.pushEvent(first);
        this.subject.pushEvent(second);

        first.preventCascade(SampleListener.class);
        second.preventCascade(SampleListener.class);

        final Optional<SequentialEvent<?, ?>> opt = this.subject.preventDispatch(first);
        Assert.assertSame(first, opt.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsActive() {
        final Event<?, SampleListener> event1 = Mockito.mock(Event.class);
        final Event<?, SampleListener> event2 = Mockito.mock(Event.class);
        Mockito.when(event1.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(event2.getListenerClass()).thenReturn(SampleListener.class);

        this.subject.pushEvent(event1);

        Assert.assertTrue(this.subject.isActive(event1));
        Assert.assertTrue(this.subject.isActive(event2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIsAnyActive() throws Exception {
        final Event<?, SampleListener> event1 = Mockito.mock(Event.class);
        final Event<?, OtherListener> event2 = Mockito.mock(Event.class);
        Mockito.when(event1.getListenerClass()).thenReturn(SampleListener.class);
        Mockito.when(event2.getListenerClass()).thenReturn(OtherListener.class);

        this.subject.pushEvent(event2);

        Assert.assertTrue(this.subject.isAnyActive(Arrays.asList(SampleListener.class, OtherListener.class)));
        Assert.assertFalse(this.subject.isAnyActive(Arrays.asList(SampleListener.class)));
    }
}
