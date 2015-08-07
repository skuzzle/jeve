package de.skuzzle.jeve.providers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.ListenerStore;

public class AWTEventProviderInvokeNowTest extends
        AbstractEventProviderTest<AWTEventProvider> {

    @Override
    protected AWTEventProvider createSubject(ListenerStore store) {
        return new AWTEventProvider(store, true);
    }

    @Test
    public void testIsSequential() throws Exception {
        Mockito.when(this.store.isSequential()).thenReturn(true);
        Assert.assertTrue(this.subject.isSequential());
    }

    @Test
    public void testCanDispatch() throws Exception {
        Assert.assertTrue(this.subject.canDispatch());
    }

    @Test
    public void testisInvokeNow() throws Exception {
        Assert.assertTrue(this.subject.isInvokeNow());
    }
}
