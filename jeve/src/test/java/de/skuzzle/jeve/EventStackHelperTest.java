package de.skuzzle.jeve;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.skuzzle.jeve.EventStackHelper.SuppressedEventImpl;

@RunWith(MockitoJUnitRunner.class)
public class EventStackHelperTest {

    private interface SampleListener extends Listener {

    }
    @Mock
    private ExceptionCallback exceptionCallback;
    @Mock
    private BiConsumer<SampleListener, Event<?, SampleListener>> biConsumer;
    @Mock
    private Event<?, SampleListener> eventMock;
    @Mock
    private EventStack eventStack;

    @Before
    public void setUp() throws Exception {
        Mockito.when(this.eventMock.getListenerClass()).thenReturn(SampleListener.class);
    }

    @Test
    public void testCheckDoNotPrevent() throws Exception {
        Mockito.when(this.eventStack.preventDispatch(SampleListener.class)).thenReturn(
                Optional.empty());

        final boolean result = EventStackHelper.checkPrevent(this.eventStack,
                this.eventMock, this.biConsumer, this.exceptionCallback);
        Assert.assertFalse(result);
        Mockito.verify(this.eventMock, Mockito.never()).setPrevented(true);
    }

    @Test
    public void testCheckPrevent() {
        final SynchronousEvent<?, ?> cause = Mockito.mock(SynchronousEvent.class);
        Mockito.when(this.eventStack.preventDispatch(SampleListener.class)).thenReturn(
                Optional.of(cause));

        final boolean result = EventStackHelper.checkPrevent(this.eventStack,
                this.eventMock, this.biConsumer, this.exceptionCallback);
        Assert.assertTrue(result);
        Mockito.verify(this.eventMock).setPrevented(true);
        Mockito.verify(cause).addSuppressedEvent(new SuppressedEventImpl<>(
                this.eventMock, this.exceptionCallback, this.biConsumer));
    }
}
