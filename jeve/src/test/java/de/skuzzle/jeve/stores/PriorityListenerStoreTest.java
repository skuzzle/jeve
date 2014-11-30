package de.skuzzle.jeve.stores;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PriorityListenerStoreTest extends
        AbstractListenerStoreTest<PriorityListenerStore> {

    @Override
    protected PriorityListenerStore createStore() {
        return new PriorityListenerStore();
    }

    @Test
    public void testIsNotSequential() {
        Assert.assertFalse(this.subject.isSequential());
    }

    @Test
    public void testCreateListenerList() throws Exception {
        final List<String> list = this.subject.createListenerList();
        Assert.assertTrue(list instanceof LinkedList<?>);
    }

    @Test
    public void testGetPrioritized() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);

        this.subject.add(SampleListener.class, listener1, 2);
        this.subject.add(SampleListener.class, listener2, 1);

        final Iterator<SampleListener> it = this.subject.get(SampleListener.class).iterator();
        Assert.assertSame(listener2, it.next());
        Assert.assertSame(listener1, it.next());
    }

    @Test
    public void testGetPrioritizedSamePriority() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);

        this.subject.add(SampleListener.class, listener1, 2);
        this.subject.add(SampleListener.class, listener2, 2);

        final Iterator<SampleListener> it = this.subject.get(SampleListener.class).iterator();
        Assert.assertSame(listener1, it.next());
        Assert.assertSame(listener2, it.next());
    }

    @Test
    public void testGetPrioritizedDefault() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);

        this.subject.add(SampleListener.class, listener1, 2);
        // assume default is 0
        this.subject.add(SampleListener.class, listener2);

        final Iterator<SampleListener> it = this.subject.get(SampleListener.class).iterator();
        Assert.assertSame(listener2, it.next());
        Assert.assertSame(listener1, it.next());
    }
}
