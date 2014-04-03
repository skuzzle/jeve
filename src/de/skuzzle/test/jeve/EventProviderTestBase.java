package de.skuzzle.test.jeve;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.OneTimeEventListener;


/**
 * Base class for testing event providers.
 * 
 * @author Simon Taddiken
 */
@Ignore
public abstract class EventProviderTestBase {
    
    /** The factory to create EventProvider instances for testing */
    protected final EventProviderFactory factory;
    
    /** The provider being used in a single test case */
    protected EventProvider subject;
    
    
    
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
    
    
    
    /**
     * Constructs an error string containing the name of the currently tested 
     * {@link EventProviderTestBase}.
     * 
     * @param fail The reason why a test failed.
     * @return The constructed string.
     */
    protected String getFailString(String fail) {
        return this.subject.getClass().getSimpleName() + ": " + fail;
    }
    
    
    
    /**
     * Tests whether listeners are notified in order they are added.
     * 
     * <p>This test case might not work for asynchronous event providers which use more
     * than one thread.</p>
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testDelegationOrder() throws Exception {
        final int TESTS = 5;
        final int[] counter = new int[1];
        for (int i = 0; i < TESTS; ++i) {
            final int finalCopy = i;
            final StringListener listener = e -> {
                Assert.assertEquals(finalCopy, counter[0]);
                ++counter[0];
            };
            this.subject.addListener(StringListener.class, listener);
        }
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
    }
    
    
    
    /**
     * Tests whether listeners are returned in order they have been added by 
     * {@link EventProvider#getListeners(Class)}.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testListenerOrder() throws Exception {
        final int TESTS = 5;
        for (int i = 0; i < TESTS; ++i) {
            final int finalCopy = i;
            final StringListener listener = new StringListener() {
                @Override
                public void onStringEvent(StringEvent e) {
                    // ignore
                }
                
                @Override
                public String toString() {
                    return "" + finalCopy;
                };
            };
            this.subject.addListener(StringListener.class, listener);
        }
        final Collection<StringListener> listeners = this.subject.getListeners(
                StringListener.class);
        Assert.assertEquals(getFailString("Listener size differ"), TESTS, listeners.size());
        final Iterator<StringListener> it = listeners.iterator();
        for (int i = 0; i < TESTS; ++i) {
            Assert.assertEquals(getFailString("Wrong order"), "" + i, it.next().toString());
        }
    }
    
    
    
    /**
     * Tests whether further listener is notified if first listener throws an exception.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testExceptionHandling() throws Exception {
        final String SUBJECT = "someString";
        final boolean[] container = new boolean[1];
        final StringListener first = e -> { throw new RuntimeException(); };
        final StringListener second = e -> {
            Assert.assertEquals(getFailString("Listener not called"), SUBJECT, e.getString());
            container[0] = true;
        };
                
        
        this.subject.addListener(StringListener.class, first);
        this.subject.addListener(StringListener.class, second);
        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
        // HACK: give async providers some time to execute
        Thread.sleep(1000);
        
        Assert.assertTrue(getFailString("Second listener not notified"), container[0]);
    }
    
    
    
    /**
     * Tests whether only correct listeners are notified if multiple listener classes
     * have been registered.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testMultipleListenerTypes() throws Exception {
        final StringListener string1 = e -> {};
        final StringListener string2 = e -> {};
        final DifferentStringListener diffString1 = e -> Assert.fail(getFailString("Listener should not have been notified"));
        final DifferentStringListener diffString2 = e -> Assert.fail(getFailString("Listener should not have been notified"));
        
        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);
        
        this.subject.addListener(StringListener.class, string2);
        this.subject.addListener(DifferentStringListener.class, diffString2);
        
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
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
    
    
    
    /**
     * Tests whether removal of a listener works.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testRemoveListener() throws Exception {
        final StringListener listener = e -> Assert.fail(getFailString("Listener should not have been notified"));
        this.subject.addListener(StringListener.class, listener);
        this.subject.removeListener(StringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
    }
    
    
    
    /**
     * Registers an object which implements two kinds of listeners for both events,
     * then removes it for one again.
     * 
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void removeMutliListener() throws Exception {
        final boolean[] container = new boolean[1];
        class TestListener implements StringListener, DifferentStringListener {

            @Override
            public void onDifferentStringEvent(StringEvent e) {
                Assert.fail(getFailString("Listener should not have been notified"));
            }

            @Override
            public void onStringEvent(StringEvent e) {
                container[0] = true;
            }
        }
        final TestListener listener = new TestListener();
        this.subject.addListener(StringListener.class, listener);
        this.subject.addListener(DifferentStringListener.class, listener);
        
        this.subject.removeListener(DifferentStringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(StringListener.class, e, StringListener::onStringEvent);
        // HACK: give async providers some time to execute
        Thread.sleep(1000);
        
        Assert.assertTrue(getFailString("Listener method has not been called"), 
                container[0]);
    }
}
