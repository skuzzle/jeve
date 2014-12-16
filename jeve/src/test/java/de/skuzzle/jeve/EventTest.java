package de.skuzzle.jeve;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventTest {

    private interface SampleListener extends Listener {

    }

    private Event<Object, SampleListener> subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new Event<>(new Object(), SampleListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNull() throws Exception {
        new Event<>(null, SampleListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListenerClassNull() throws Exception {
        new Event<>(new Object(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCauseIsNull() throws Exception {
        new Event<Object, SampleListener>(new Object(), SampleListener.class,
                (Optional<Event<?, ?>>) null);
    }

    @Test
    public void testSetListenerStoreOnce() {
        final ListenerStore store1 = Mockito.mock(ListenerStore.class);
        final ListenerStore store2 = Mockito.mock(ListenerStore.class);

        this.subject.setListenerStore(store1);
        this.subject.setListenerStore(store2);
        Assert.assertSame(store1, this.subject.getListenerStore());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetListenerStoreNoDispatch() {
        this.subject.getListenerStore();
    }

    @Test
    public void testStopNotifying() {
        final ListenerStore store = Mockito.mock(ListenerStore.class);
        final SampleListener listener = Mockito.mock(SampleListener.class);
        this.subject.setListenerStore(store);
        this.subject.stopNotifying(listener);
        Mockito.verify(store).remove(SampleListener.class, listener);
    }
}
