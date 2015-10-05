package de.skuzzle.jeve;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class SequentialEventTest {
    private interface SampleListener extends Listener {

    }

    private interface OtherListener extends Listener {

    }

    private SequentialEvent<Object, SampleListener> subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new SequentialEvent<>(new Object(), SampleListener.class);
    }

    @Test
    public void testGetPreventedEmptyNotNull() {
        Assert.assertNotNull(this.subject.getPrevented());
    }

    @Test(expected = IllegalArgumentException.class)
    public void preventCascadeNull() throws Exception {
        this.subject.preventCascade(null);
    }

    @Test
    public void testPreventCascade() {
        this.subject.preventCascade();
        Assert.assertTrue(this.subject.getPrevented().contains(SampleListener.class));
    }

    @Test
    public void testPreventCascade2() {
        this.subject.preventCascade(SampleListener.class);
        this.subject.preventCascade(OtherListener.class);
        Assert.assertTrue(this.subject.getPrevented().contains(SampleListener.class));
        Assert.assertTrue(this.subject.getPrevented().contains(OtherListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSuppressedNull() throws Exception {
        this.subject.addSuppressedEvent(null);
    }

    @Test
    public void testGetSuppressedNotNull() throws Exception {
        Assert.assertNotNull(this.subject.getSuppressedEvents());
    }

    @Test
    public void testAddSuppressed() {
        final SuppressedEvent sup = Mockito.mock(SuppressedEvent.class);
        this.subject.addSuppressedEvent(sup);
        Assert.assertTrue(this.subject.getSuppressedEvents().contains(sup));
    }
}
