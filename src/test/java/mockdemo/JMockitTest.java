package mockdemo;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JMockitTest {

    @Mocked
    private List<String> mock;

    @Mocked
    private List<String> otherMock;

    @Test
    public void simpleScenario() {
        new Expectations() {{
            mock.clear();
            mock.add("Uryyb Jbeyq");  result = true;
            mock.size();              result = 1;
            mock.get(0);              result = "Uryyb Jbeyq";
        }};

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    @Test
    public void fuzzyParameterMatching() {
        new Expectations() {{
            mock.get(0);  result = "Tbbqolr Whcvgre";
            mock.get(with(0, Matchers.greaterThan(0))); result = "Uryyb Jbeyq"; minTimes = 1;
            mock.add(withSuffix("Natryf"));  result = true;
            mock.add(withSuffix("Qrivyf"));  result = false;
            mock.set(anyInt, anyString);  result = "Obawbhe Zrephel";
        }};

        ROT13List underTest = new ROT13List(mock);

        Assert.assertEquals("Goodbye Jupiter", underTest.get(0));
        Assert.assertEquals("Hello World", underTest.get(1));
        Assert.assertEquals("Hello World", underTest.get(19));
        Assert.assertEquals("Hello World", underTest.get(90210));
        Assert.assertTrue(underTest.add("California Angels"));
        Assert.assertFalse(underTest.add("New Jersey Devils"));
        Assert.assertEquals("Bonjour Mercury", underTest.set(0, "something"));
    }

    @Test
    public void callsInSequence() {
        // JMockit automatically expects all calls in an Expectations to occur in order across any referenced mock objects.
        new Expectations() {{
            mock.add("svefg pnyy");
            otherMock.add("frpbaq pnyy");
            mock.add("guveq pnyy");
        }};

        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");
    }

    @Test
    public void throwExceptions() {
        new Expectations() {{
            mock.clear();                         result = new RuntimeException("hello");
            mock.add(withSubstring("Fdhveery"));  result = new RuntimeException("allergic to squirrels");
        }};

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
    }

    @Test
    public void ignoreInvocations() {
        new NonStrictExpectations() {{
            mock.add(anyString);  result = true;
        }};
        new Expectations() {{
            mock.get(0);  result = "Uryyb Jbeyq";
        }};

        ROT13List underTest = new ROT13List(mock);

        Assert.assertTrue(underTest.add("doesn't"));
        Assert.assertTrue(underTest.add("matter"));
        Assert.assertTrue(underTest.add("it's all"));
        Assert.assertTrue(underTest.add("ignored"));
        Assert.assertTrue(underTest.add("anyway"));
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    @Test
    public void consecutiveCalls() {
        new Expectations(){{
            mock.get(0);     returns("bar", "gjb", "guerr");  times = 3;
            mock.get(1234);  result = "nqvbf";
            mock.get(1234);  result = new RuntimeException("blah blah");
        }};

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
    }
}
