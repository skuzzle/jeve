package de.skuzzle.jeve.stores;

import org.junit.Assert;
import org.junit.Test;

public class DefaultListenerStoreTest extends
        AbstractListenerStoreTest<DefaultListenerStore> {

    @Override
    protected DefaultListenerStore createStore() {
        return new DefaultListenerStore();
    }

    @Test
    public void testIsSequential() {
        Assert.assertTrue(this.subject.isSequential());
    }
}
