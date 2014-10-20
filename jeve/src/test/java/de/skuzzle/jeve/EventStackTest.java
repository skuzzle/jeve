package de.skuzzle.jeve;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.annotation.ListenerInterface;

public class EventStackTest {

    @ListenerInterface
    private interface SampleListener extends Listener {

    }

    private EventStack subject;

    @Before
    public void setup() {
        this.subject = new EventStack();
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
        final Event<?, SampleListener> first = new Event<>(null, SampleListener.class);
        final Event<?, SampleListener> second = new Event<>(null, SampleListener.class);

        this.subject.pushEvent(first);
        this.subject.pushEvent(second);

        first.preventCascade(SampleListener.class);
        second.preventCascade(SampleListener.class);

        final Optional<Event<?, ?>> opt = this.subject.preventDispatch(first);
        Assert.assertSame(first, opt.get());
    }
}
