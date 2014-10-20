package de.skuzzle.jeve.stores;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

public abstract class AbstractListenerStoreTest<T extends ListenerStore> {

    protected interface SampleListener extends Listener {

    }

    protected interface OtherListener extends Listener {

    }

    protected T subject;

    protected abstract T createStore();

    @Before
    public void setUp() throws Exception {
        this.subject = createStore();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalGet() {
        this.subject.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalAdd1() {
        this.subject.add(null, Mockito.mock(SampleListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalAdd2() {
        this.subject.add(SampleListener.class, null);
    }

    @Test
    public void testRemoveClassNull() {
        this.subject.remove(null, Mockito.mock(SampleListener.class));
    }

    @Test
    public void testRemoveListenerNull() {
        this.subject.remove(SampleListener.class, null);
    }

    @Test
    public void testAdd() {
        final SampleListener listener = Mockito.mock(SampleListener.class);
        this.subject.add(SampleListener.class, listener);
        Mockito.verify(listener).onRegister(Mockito.any());
    }

    @Test
    public void testRemove() {
        final SampleListener listener = Mockito.mock(SampleListener.class);
        this.subject.add(SampleListener.class, listener);
        this.subject.remove(SampleListener.class, listener);
        Mockito.verify(listener).onUnregister(Mockito.any());
    }

    @Test
    public void testClearAll() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final OtherListener listener2 = Mockito.mock(OtherListener.class);
        this.subject.add(SampleListener.class, listener1);
        this.subject.add(OtherListener.class, listener2);
        this.subject.clearAll();
        Mockito.verify(listener1).onUnregister(Mockito.any());
        Mockito.verify(listener2).onUnregister(Mockito.any());
        Assert.assertFalse(this.subject.get(SampleListener.class).iterator().hasNext());
        Assert.assertFalse(this.subject.get(OtherListener.class).iterator().hasNext());
    }

    @Test
    public void testClearAllSingle() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final OtherListener listener2 = Mockito.mock(OtherListener.class);
        this.subject.add(SampleListener.class, listener1);
        this.subject.add(OtherListener.class, listener2);
        this.subject.clearAll(SampleListener.class);
        Mockito.verify(listener1).onUnregister(Mockito.any());
        Mockito.verify(listener2, Mockito.never()).onUnregister(Mockito.any());
        Assert.assertFalse(this.subject.get(SampleListener.class).iterator().hasNext());
        Assert.assertTrue(this.subject.get(OtherListener.class).iterator().hasNext());
    }

    @Test
    public void testGetSequential() {
        Assume.assumeTrue(this.subject.isSequential());

        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        this.subject.add(SampleListener.class, listener1);
        this.subject.add(SampleListener.class, listener2);
        final Iterator<SampleListener> it = this.subject.get(SampleListener.class).iterator();
        Assert.assertSame(listener1, it.next());
        Assert.assertSame(listener2, it.next());
    }

    @Test
    public void testGet() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final SampleListener listener2 = Mockito.mock(SampleListener.class);
        this.subject.add(SampleListener.class, listener1);
        this.subject.add(SampleListener.class, listener2);
        final Set<SampleListener> all = this.subject.get(SampleListener.class).collect(Collectors.toSet());
        final Set<SampleListener> expected = new HashSet<>(Arrays.asList(listener1, listener2));
        Assert.assertEquals(expected, all);
    }

    @Test
    public void testClose() {
        final SampleListener listener1 = Mockito.mock(SampleListener.class);
        final OtherListener listener2 = Mockito.mock(OtherListener.class);
        this.subject.add(SampleListener.class, listener1);
        this.subject.add(OtherListener.class, listener2);
        this.subject.close();
        Mockito.verify(listener1).onUnregister(Mockito.any());
        Mockito.verify(listener2).onUnregister(Mockito.any());
        Assert.assertFalse(this.subject.get(SampleListener.class).iterator().hasNext());
        Assert.assertFalse(this.subject.get(OtherListener.class).iterator().hasNext());
    }
}
