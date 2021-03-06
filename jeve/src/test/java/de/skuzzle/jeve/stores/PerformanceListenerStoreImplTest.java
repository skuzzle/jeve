package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class PerformanceListenerStoreImplTest extends
        AbstractListenerStoreTest<PerformanceListenerStoreImpl> {

    @Override
    protected PerformanceListenerStoreImpl createStore() {
        return new PerformanceListenerStoreImpl();
    }

    @Test
    public void testAutoOptimize() throws Exception {
        final PerformanceListenerStore store = new PerformanceListenerStoreImpl(true);
        Assert.assertTrue(store.isAutoOptimize());
        store.get(SampleListener.class);
        Assert.assertTrue(store.isOptimized());
    }

    @Test
    public void testNoAutoOptimize() throws Exception {
        Assert.assertFalse(this.subject.isAutoOptimize());
    }

    @Test
    public void testIsSequential() {
        Assert.assertTrue(this.subject.isSequential());
    }

    @Test
    public void testIsNotOptimized() throws Exception {
        Assert.assertFalse(this.subject.isOptimized());
    }

    @Test
    public void testCreateList() {
        final List<String> list = this.subject.createListenerList();
        Assert.assertTrue(list instanceof ArrayList<?>);
    }
}
