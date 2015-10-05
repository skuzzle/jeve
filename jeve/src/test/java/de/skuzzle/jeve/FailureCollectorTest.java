package de.skuzzle.jeve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.skuzzle.jeve.invoke.FailedEventInvocation;

@RunWith(MockitoJUnitRunner.class)
public class FailureCollectorTest {

    @Mock
    private ExceptionCallback delegate;

    private FailureCollector subject;

    @Before
    public void setUp() throws Exception {}

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullDelegate() throws Exception {
        FailureCollector.delegatingTo(null);
    }

    @Test
    public void testInitiallyEmpty() throws Exception {
        assertEquals(0, FailureCollector.create().getFailedInvocations().size());
    }

    @Test
    public void testCollectWithoutDelegate() throws Exception {
        this.subject = FailureCollector.create();
        final FailedEventInvocation failed = mock(FailedEventInvocation.class);
        final FailedEventInvocation failed2 = mock(FailedEventInvocation.class);
        this.subject.exception(failed);
        this.subject.exception(failed2);

        assertEquals(2, this.subject.getFailedInvocations().size());
        assertSame(failed, this.subject.iterator().next());
    }

    @Test
    public void testCollectWithDelegate() throws Exception {
        this.subject = FailureCollector.delegatingTo(this.delegate);
        final FailedEventInvocation failed = mock(FailedEventInvocation.class);
        final FailedEventInvocation failed2 = mock(FailedEventInvocation.class);
        this.subject.exception(failed);
        this.subject.exception(failed2);

        assertEquals(2, this.subject.getFailedInvocations().size());
        assertSame(failed, this.subject.iterator().next());
        verify(this.delegate).exception(failed);
        verify(this.delegate).exception(failed2);
    }
}
