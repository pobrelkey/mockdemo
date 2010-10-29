package mockdemo;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JMock.class)
public class JMock2Test {
    private JUnit4Mockery context = new JUnit4Mockery();

    @Test
    public void simpleScenario() {
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class);

        context.checking(new Expectations() {{
            one(mock).clear();

            one(mock).add("Uryyb Jbeyq");
            will(returnValue(true));

            one(mock).size();
            will(returnValue(1));

            one(mock).get(0);
            will(returnValue("Uryyb Jbeyq"));
        }});

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    @Test
    public void fuzzyParameterMatching() {
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class);

        context.checking(new Expectations() {{
            one(mock).get(0);
            will(returnValue("Tbbqolr Whcvgre"));

            atLeast(1).of(mock).get(with(Matchers.greaterThan(0)));
            will(returnValue("Uryyb Jbeyq"));

            one(mock).add(with(Matchers.endsWith("Natryf")));
            will(returnValue(true));

            one(mock).add(with(Matchers.endsWith("Qrivyf")));
            will(returnValue(false));

            one(mock).set(with(any(Integer.TYPE)), with(Expectations.<String>anything()));
            will(returnValue("Obawbhe Zrephel"));
        }});

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
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class, "mock");
        @SuppressWarnings("unchecked")
        final List<String> otherMock = context.mock(List.class, "otherMock");
        final Sequence sequence = context.sequence("some sequence name");

        context.checking(new Expectations() {{
            one(mock).add("svefg pnyy");
            inSequence(sequence);

            one(otherMock).add("frpbaq pnyy");
            inSequence(sequence);

            one(mock).add("guveq pnyy");
            inSequence(sequence);
        }});

        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");
    }

    @Test
    public void throwExceptions() {
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class);

        context.checking(new Expectations() {{
            one(mock).clear();
            will(throwException(new RuntimeException("hello")));

            one(mock).add(with(Matchers.containsString("Fdhveery")));
            will(throwException(new RuntimeException("allergic to squirrels")));
        }});

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
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class);

        context.checking(new Expectations() {{
            one(mock).get(0);
            will(returnValue("Uryyb Jbeyq"));

            ignoring(mock).add(with(Matchers.<String>anything()));
            will(returnValue(true));
        }});

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
        @SuppressWarnings("unchecked")
        final List<String> mock = context.mock(List.class);

        context.checking(new Expectations() {{
            exactly(3).of(mock).get(0);
            will(onConsecutiveCalls(returnValue("bar"), returnValue("gjb"), returnValue("guerr")));

            exactly(2).of(mock).get(1234);
            will(onConsecutiveCalls(returnValue("nqvbf"), throwException(new RuntimeException("blah blah"))));
        }});

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
