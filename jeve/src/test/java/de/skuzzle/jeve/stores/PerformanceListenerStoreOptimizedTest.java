package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class PerformanceListenerStoreOptimizedTest extends
        AbstractListenerStoreTest<PerformanceListenerStore> {

    @Override
    protected PerformanceListenerStore createStore() {
        final PerformanceListenerStore result = new PerformanceListenerStore();
        result.optimizeGet();
        return result;
    }

    @Test
    public void testOptimizeAgain() {
        this.subject.optimizeGet();
    }

    @Test
    public void testOptimizeOnFilledStore() throws Exception {
        final PerformanceListenerStore store = new PerformanceListenerStore();
        store.add(SampleListener.class, Mockito.mock(SampleListener.class));
        store.add(OtherListener.class, Mockito.mock(OtherListener.class));

        store.listenerMap.values().forEach(
                list -> Assert.assertTrue(list instanceof ArrayList<?>));

        store.optimizeGet();

        store.listenerMap.values().forEach(
                list -> Assert.assertTrue(list instanceof CopyOnWriteArrayList<?>));
    }

    @Test
    public void testIsSequential() {
        Assert.assertTrue(this.subject.isSequential());
    }

    @Test
    public void testIsOptimized() throws Exception {
        Assert.assertTrue(this.subject.isOptimized());
    }

    @Test
    public void testCreateList() {
        final List<String> list = this.subject.createListenerList();
        Assert.assertTrue(list instanceof CopyOnWriteArrayList<?>);
    }
}
