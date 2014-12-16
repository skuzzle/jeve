package de.skuzzle.jeve.providers;

import org.junit.Test;

import de.skuzzle.jeve.ListenerStore;

public class UnrollingEventProviderTest extends
        AbstractEventProviderTest<UnrollingEventProvider<ListenerStore>> {

    @Override
    protected UnrollingEventProvider<ListenerStore> createSubject(ListenerStore store) {
        return new UnrollingEventProvider<ListenerStore>(store);
    }

    @Test
    public void testNestedDispatch() {

    }
}
