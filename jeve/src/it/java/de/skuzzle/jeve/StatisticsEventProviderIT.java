package de.skuzzle.jeve;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.skuzzle.jeve.providers.StatisticsEventProvider;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsEventProviderIT {

    @Mock
    private EventProvider eventProvider;

    private StatisticsEventProvider<EventProvider> subject;

    @Before
    public void setup() {
        this.subject = new StatisticsEventProvider<EventProvider>(this.eventProvider);
    }

    @Test
    public void testGetWrapped() {
        Assert.assertSame(this.eventProvider, this.subject.getWrapped());
    }

    @Test
    public void testZeroDispatches() {
        Assert.assertNull(this.subject.getNotificationStatistics().get(StringListener.class));
    }

    @Test
    public void testCountDispatches() {
        this.subject.dispatch(new StringEvent(this.subject, ""),
                StringListener::onStringEvent);
        Assert.assertEquals(Integer.valueOf(1),
                this.subject.getNotificationStatistics().get(StringListener.class));
    }

}
