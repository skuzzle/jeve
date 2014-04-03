package de.skuzzle.test.jeve;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.OneTimeEventListener;


/**
 * Base class for testing event providers.
 * 
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class EventProviderTestBase {
    
    /**
     * Parameterizes the test instances.
     * @return Collection of parameters for the constructor of 
     *          {@link EventProviderTestBase}.
     */
    @Parameters
    public final static Collection<Object[]> getParameters() {
        return Arrays.asList(
                new EventProviderFactory[] { EventProvider::newDefaultEventProvider },
                new EventProviderFactory[] { EventProvider::newAsynchronousEventProvider }
            );
    }

    
    
    /** The factory to create EventProvider instances for testing */
    protected final EventProviderFactory factory;
    
    /** The provider being used in a single test case */
    private EventProvider subject;
    
    
    
    /**
     * Creates a new Test class instance.
     * @param factory A factory for creating event providers
     */
    public EventProviderTestBase(EventProviderFactory factory) {
        this.factory = factory;
    }
    
    
    
    /**
     * Sets up a single test case by creating a new {@link EventProvider} using the 
     * factory provided by the constructor.
     */
    @Before
    public void setUp() {
        this.subject = this.factory.create();
        
    }
    
    
    
    private String getFailString(String fail) {
        return this.subject.getClass().getSimpleName() + ": " + fail;
    }
    
    
    
    /**
     * Tests whether {@link OneTimeEventListener}s are not notified anymore after
     * returning true in their workDone method.
     * 
     * <p>This test case might not work for asynchronous event providers which use more
     * than one thread.</p>
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testOneTimeListener() throws Exception {
        final int MAX_NOTIFICATIONS = 5;
        final String SUBJECT = "someString";
        
        class TestListener implements StringListener, OneTimeEventListener {

            private int notificationCount = 0;
            
            @Override
            public void onStringEvent(StringEvent e) {
                final String tName = Thread.currentThread().getName();
                System.out.println(tName + " count: " + this.notificationCount);
                Assert.assertEquals(getFailString("Wrong string"), SUBJECT, e.getString());
                Assert.assertTrue(getFailString("Wrong notification count"), 
                        this.notificationCount < MAX_NOTIFICATIONS);
                ++this.notificationCount;
            }
            
            @Override
            public boolean workDone(EventProvider parent) {
                boolean value = this.notificationCount == MAX_NOTIFICATIONS - 1;
                final String tName = Thread.currentThread().getName();
                System.out.println(tName + " 'workDone' going to return " + value);
                return value;
            }
        }
        
        this.subject.addListener(StringListener.class, new TestListener());
        // use <= to perform one additional notification
        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        for (int i = 0; i <= MAX_NOTIFICATIONS; ++i) {
            this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
        }
    }
    
    
    
    /**
     * Tests whether delegation stops after event has been handled.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testHandleEvent() throws Exception {
        final String SUBJECT = "someString";
        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        // first listener sets event to be handled
        final StringListener firstListener = event -> event.setHandled(true);
        final StringListener secondListener = event -> Assert.fail(
                getFailString("Second listener has been notified"));
        
        this.subject.addListener(StringListener.class, firstListener);
        this.subject.addListener(StringListener.class, secondListener);
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
    }
}
