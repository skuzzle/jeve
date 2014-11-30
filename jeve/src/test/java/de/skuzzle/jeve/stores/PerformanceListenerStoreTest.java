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
