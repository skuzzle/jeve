package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DefaultListenerStoreImplTest extends
        AbstractListenerStoreTest<DefaultListenerStoreImpl> {

    @Override
    protected DefaultListenerStoreImpl createStore() {
        return new DefaultListenerStoreImpl();
    }

    @Test
    public void testCreateList() {
        final List<String> list = this.subject.createListenerList();
        Assert.assertTrue(list instanceof ArrayList<?>);
    }

    @Test
    public void testIsSequential() {
        Assert.assertTrue(this.subject.isSequential());
    }
}
