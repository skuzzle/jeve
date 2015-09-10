package de.skuzzle.jeve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class EventTest {

    private interface SampleListener extends Listener {

    }

    private Event<Object, SampleListener> subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new Event<>(new Object(), SampleListener.class);
    }

    @Test
    public void testGetUnknownProperty() throws Exception {
        assertFalse(this.subject.getValue("foo").isPresent());
    }

    @Test
    public void testSetGetProperty() throws Exception {
        this.subject.setValue("foo", "bar");
        assertEquals("bar", this.subject.getValue("foo").get());
    }

    @Test
    public void testSetPropertyThroughMap() throws Exception {
        this.subject.getProperties().put("foo", "bar");
        assertEquals("bar", this.subject.getValue("foo").get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNull() throws Exception {
        new Event<>(null, SampleListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListenerClassNull() throws Exception {
        new Event<>(new Object(), null);
    }
}
