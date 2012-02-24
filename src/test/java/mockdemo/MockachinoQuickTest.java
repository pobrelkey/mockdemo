package mockdemo;

import com.googlecode.gentyref.TypeToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mockachino.Mockachino;
import se.mockachino.Settings;
import se.mockachino.annotations.Mock;
import se.mockachino.matchers.Matchers;
import se.mockachino.matchers.matcher.Matcher;
import se.mockachino.order.OrderingContext;

import java.util.List;

// Test for benchmarking purposes - Kristofer Karlsson (author of Mockachino) points out that
// Mockachino's performance can be improved if mocks are specified using Settings.quick().
// This gives about a 3x speedup, but "slow" Mockachino still performs fine (better than Mockito),
// so this will probably only matter to pedants running mock benchmark suites.  :-)
public class MockachinoQuickTest {
    List<String> mock = Mockachino.mock(new TypeToken<List<String>>(){}, Settings.quick());

    List<String> otherMock = Mockachino.mock(new TypeToken<List<String>>(){}, Settings.quick());

    @Before
    public void setUp() throws Exception {
        Mockachino.setupMocks(this);
    }

    private static Matcher<Integer> greaterThan(final int value) {
        return new Matcher<Integer>() {
            @Override
            public boolean matches(Integer integer) {
                return integer.intValue() > value;
            }

            @Override
            public Class<Integer> getType() {
                return Integer.class;
            }
        };
    }

    @Test
    public void simpleScenario() {
        Mockachino.when(mock.add(("Uryyb Jbeyq"))).thenReturn(true);
        Mockachino.when(mock.size()).thenReturn(1);
        Mockachino.when(mock.get(0)).thenReturn("Uryyb Jbeyq");

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));

        Mockachino.verifyOnce().on(mock).clear();
        Mockachino.verifyOnce().on(mock).add("Uryyb Jbeyq");
        Mockachino.verifyOnce().on(mock).size();
        Mockachino.verifyOnce().on(mock).get(0);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void fuzzyParameterMatching() {
        Mockachino.when(mock.get(0)).thenReturn("Tbbqolr Whcvgre");
        Mockachino.when(mock.get(Matchers.m(greaterThan(0)))).thenReturn("Uryyb Jbeyq");
        Mockachino.when(mock.add(Matchers.endsWith("Natryf"))).thenReturn(true);
        Mockachino.when(mock.add(Matchers.endsWith("Qrivyf"))).thenReturn(false);
        Mockachino.when(mock.set(Matchers.anyInt(), Matchers.any(String.class))).thenReturn("Obawbhe Zrephel");

        ROT13List underTest = new ROT13List(mock);

        Assert.assertEquals("Goodbye Jupiter", underTest.get(0));
        Assert.assertEquals("Hello World", underTest.get(1));
        Assert.assertEquals("Hello World", underTest.get(19));
        Assert.assertEquals("Hello World", underTest.get(90210));
        Assert.assertTrue(underTest.add("California Angels"));
        Assert.assertFalse(underTest.add("New Jersey Devils"));
        Assert.assertEquals("Bonjour Mercury", underTest.set(0, "something"));

        Mockachino.verifyOnce().on(mock).get(0);
        Mockachino.verifyAtLeast(1).on(mock).get(Matchers.m(greaterThan(0)));
        Mockachino.verifyOnce().on(mock).add(Matchers.endsWith("Natryf"));
        Mockachino.verifyOnce().on(mock).add(Matchers.endsWith("Qrivyf"));
        Mockachino.verifyOnce().on(mock).set(Matchers.anyInt(), Matchers.any(String.class));

    }

    @Test
    public void callsInSequence() {
        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");

        OrderingContext ordering = Mockachino.newOrdering();
        ordering.verify().on(mock).add("svefg pnyy");
        ordering.verify().on(otherMock).add("frpbaq pnyy");
        ordering.verify().on(mock).add("guveq pnyy");

    }


    @Test
    public void throwExceptions() {
        Mockachino.stubThrow(new RuntimeException("hello")).on(mock).clear();
        Mockachino.when(mock.add(Matchers.contains("Fdhveery"))).thenThrow(new RuntimeException("allergic to squirrels"));

        ROT13List underTest = new ROT13List(mock);

        try {
            underTest.clear();
            Assert.fail("should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertEquals("hello", e.getMessage());
        }
        try {
            underTest.add("Magic Squirrel Juice");
            Assert.fail("should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertEquals("allergic to squirrels", e.getMessage());
        }

        Mockachino.verifyOnce().on(mock).clear();
        Mockachino.verifyOnce().on(mock).add(Matchers.contains("Fdhveery"));

    }

    @Test
    public void ignoreInvocations() {
        Mockachino.when(mock.get(0)).thenReturn("Uryyb Jbeyq");
        Mockachino.when(mock.add(Matchers.any(String.class))).thenReturn(true);

        ROT13List underTest = new ROT13List(mock);

        Assert.assertTrue(underTest.add("doesn't"));
        Assert.assertTrue(underTest.add("matter"));
        Assert.assertTrue(underTest.add("it's all"));
        Assert.assertTrue(underTest.add("ignored"));
        Assert.assertTrue(underTest.add("anyway"));
        Assert.assertEquals("Hello World", underTest.get(0));

        Mockachino.verifyOnce().on(mock).get(0);

    }

    @Test
    public void consecutiveCalls() {
        Mockachino.when(mock.get(0)).thenReturn("bar", "gjb", "guerr");
        Mockachino.when(mock.get(1234)).thenReturn("nqvbf").thenThrow(new RuntimeException("blah blah"));

        ROT13List underTest = new ROT13List(mock);

        Assert.assertEquals("one", underTest.get(0));
        Assert.assertEquals("two", underTest.get(0));
        Assert.assertEquals("three", underTest.get(0));
        Assert.assertEquals("adios", underTest.get(1234));
        try {
            underTest.get(1234);
            Assert.fail("should have thrown an exception");
        } catch (RuntimeException e) {
            Assert.assertEquals("blah blah", e.getMessage());
        }

        Mockachino.verifyExactly(3).on(mock).get(0);
        Mockachino.verifyExactly(2).on(mock).get(1234);

    }

}