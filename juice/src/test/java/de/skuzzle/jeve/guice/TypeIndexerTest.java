package de.skuzzle.jeve.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

import de.skuzzle.jeve.Listener;

public class TypeIndexerTest {

    private interface SampleListener extends Listener {

    }

    private interface SampleListener2 extends Listener {

    }

    static class SampleController implements SampleListener, SampleListener2, Cloneable {

    }

    private TypeIndex typeIndex;
    private TypeIndexer typeIndexer;
    private TypeEncounter<SampleController> encounter;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        this.typeIndex = new TypeIndex();
        this.typeIndexer = new TypeIndexer(this.typeIndex);
        this.encounter = mock(TypeEncounter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexNonSubClass() throws Exception {
        this.typeIndex.addImplementor(SampleListener.class, String.class);
    }

    @Test
    public void testIndexOnlyListeners() throws Exception {
        final TypeLiteral<SampleController> lit = TypeLiteral.get(
                SampleController.class);
        this.typeIndexer.hear(lit, this.encounter);

        final Stream<Class<Cloneable>> s = this.typeIndex
                .findImplementorsOf(Cloneable.class);

        assertFalse(s.iterator().hasNext());
    }

    @Test
    public void testIndex() throws Exception {
        final TypeLiteral<SampleController> lit = TypeLiteral.get(
                SampleController.class);
        this.typeIndexer.hear(lit, this.encounter);

        final Stream<Class<SampleListener>> s = this.typeIndex
                .findImplementorsOf(SampleListener.class);

        assertEquals(SampleController.class, s.iterator().next());

        final Stream<Class<SampleListener2>> s2 = this.typeIndex
                .findImplementorsOf(SampleListener2.class);

        assertEquals(SampleController.class, s2.iterator().next());
    }

}
