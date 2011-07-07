package mockdemo;

import org.hamcrest.Matchers;
import org.hamcrest.number.IsGreaterThan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MockitoTest {
    @Mock
    List<String> mock;

    @Mock
    List<String> otherMock;

    @Test
    public void simpleScenario() {
        Mockito.stub(mock.add("Uryyb Jbeyq")).toReturn(true);
        Mockito.stub(mock.size()).toReturn(1);
        Mockito.stub(mock.get(0)).toReturn("Uryyb Jbeyq");

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));

        Mockito.verify(mock).clear();
        Mockito.verify(mock).add("Uryyb Jbeyq");
        Mockito.verify(mock).size();
        Mockito.verify(mock).get(0);
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fuzzyParameterMatching() {
        Mockito.stub(mock.get(0)).toReturn("Tbbqolr Whcvgre");
        Mockito.stub(mock.get(Mockito.intThat(Matchers.greaterThan(0)))).toReturn("Uryyb Jbeyq");
        Mockito.stub(mock.add(Mockito.argThat(Matchers.endsWith("Natryf")))).toReturn(true);
        Mockito.stub(mock.add(Mockito.argThat(Matchers.endsWith("Qrivyf")))).toReturn(false);
        Mockito.stub(mock.set(Mockito.anyInt(), Mockito.<String>any())).toReturn("Obawbhe Zrephel");

        ROT13List underTest = new ROT13List(mock);

        Assert.assertEquals("Goodbye Jupiter", underTest.get(0));
        Assert.assertEquals("Hello World", underTest.get(1));
        Assert.assertEquals("Hello World", underTest.get(19));
        Assert.assertEquals("Hello World", underTest.get(90210));
        Assert.assertTrue(underTest.add("California Angels"));
        Assert.assertFalse(underTest.add("New Jersey Devils"));
        Assert.assertEquals("Bonjour Mercury", underTest.set(0, "something"));

        Mockito.verify(mock).get(0);
        Mockito.verify(mock, Mockito.atLeastOnce()).get(Mockito.intThat(new IsGreaterThan(0)));
        Mockito.verify(mock).add(Mockito.argThat(Matchers.endsWith("Natryf")));
        Mockito.verify(mock).add(Mockito.argThat(Matchers.endsWith("Qrivyf")));
        Mockito.verify(mock).set(Mockito.anyInt(), Mockito.<String>any());
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    public void callsInSequence() {
        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");

        InOrder ordering = Mockito.inOrder(mock, otherMock);
        ordering.verify(mock).add("svefg pnyy");
        ordering.verify(otherMock).add("frpbaq pnyy");
        ordering.verify(mock).add("guveq pnyy");
    }


    @Test
    public void throwExceptions() {
        Mockito.doThrow(new RuntimeException("hello")).when(mock).clear();
        Mockito.stub(mock.add(Mockito.contains("Fdhveery"))).toThrow(new RuntimeException("allergic to squirrels"));

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

        Mockito.verify(mock).clear();
        Mockito.verify(mock).add(Mockito.contains("Fdhveery"));
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    public void ignoreInvocations() {
        Mockito.stub(mock.get(0)).toReturn("Uryyb Jbeyq");
        Mockito.stub(mock.add(Mockito.<String>any())).toReturn(true);

        ROT13List underTest = new ROT13List(mock);

        Assert.assertTrue(underTest.add("doesn't"));
        Assert.assertTrue(underTest.add("matter"));
        Assert.assertTrue(underTest.add("it's all"));
        Assert.assertTrue(underTest.add("ignored"));
        Assert.assertTrue(underTest.add("anyway"));
        Assert.assertEquals("Hello World", underTest.get(0));

        Mockito.verify(mock).get(0);
    }

    @Test
    public void consecutiveCalls() {
        Mockito.when(mock.get(0)).thenReturn("bar", "gjb", "guerr");
        Mockito.when(mock.get(1234)).thenReturn("nqvbf").thenThrow(new RuntimeException("blah blah"));

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

        Mockito.verify(mock, Mockito.times(3)).get(0);
        Mockito.verify(mock, Mockito.times(2)).get(1234);
        Mockito.verifyNoMoreInteractions(mock);
    }


}
