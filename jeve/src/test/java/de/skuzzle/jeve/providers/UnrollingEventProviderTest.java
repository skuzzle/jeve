package de.skuzzle.jeve.providers;

import org.junit.Test;

import de.skuzzle.jeve.ListenerStore;

public class UnrollingEventProviderTest extends
        AbstractEventProviderTest<UnrollingEventProvider> {

    @Override
    protected UnrollingEventProvider createSubject(ListenerStore store) {
        return new UnrollingEventProvider(store);
    }

    @Test
    public void testNestedDispatch() {

    }
}
