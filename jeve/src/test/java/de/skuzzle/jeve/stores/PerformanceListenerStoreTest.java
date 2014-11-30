package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class PerformanceListenerStoreTest extends
        AbstractListenerStoreTest<PerformanceListenerStore> {

    @Override
    protected PerformanceListenerStore createStore() {
        return new PerformanceListenerStore();
    }

    @Test
    public void testAutoOptimize() throws Exception {
        final PerformanceListenerStore store = new PerformanceListenerStore(true);
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
