package mockdemo;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsGreaterThan;
import org.jmock.core.constraint.StringEndsWith;
import org.junit.Assert;

import java.util.List;

public class JMock1Test extends MockObjectTestCase {

    public void testSimpleScenario() {
        Mock mock = mock(List.class);

        mock.expects(once()).method("clear");
        mock.expects(once()).method("add").with(eq("Uryyb Jbeyq")).will(returnValue(true));
        mock.expects(once()).method("size").withNoArguments().will(returnValue(1));
        mock.expects(once()).method("get").with(eq(0)).will(returnValue("Uryyb Jbeyq"));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    public void testFuzzyParameterMatching() {
        Mock mock = mock(List.class);

        mock.expects(once()).method("get").with(eq(0)).will(returnValue("Tbbqolr Whcvgre"));
        mock.expects(atLeastOnce()).method("get").with(new IsGreaterThan(0)).will(returnValue("Uryyb Jbeyq"));
        mock.expects(once()).method("add").with(new StringEndsWith("Natryf")).will(returnValue(true));
        mock.expects(once()).method("add").with(new StringEndsWith("Qrivyf")).will(returnValue(false));
        mock.expects(once()).method("set").withAnyArguments().will(returnValue("Obawbhe Zrephel"));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

        Assert.assertEquals("Goodbye Jupiter", underTest.get(0));
        Assert.assertEquals("Hello World", underTest.get(1));
        Assert.assertEquals("Hello World", underTest.get(19));
        Assert.assertEquals("Hello World", underTest.get(90210));
        Assert.assertTrue(underTest.add("California Angels"));
        Assert.assertFalse(underTest.add("New Jersey Devils"));
        Assert.assertEquals("Bonjour Mercury", underTest.set(0, "something"));
    }

    public void testCallsInSequence() {
        // You can't have sequences across mocks in JMock 1, so we'll only do this on one mock.
        Mock mock = mock(List.class);

        mock.expects(once()).method("add").with(eq("svefg pnyy")).will(returnValue(true)).id("alpha");
        mock.expects(once()).method("add").with(eq("frpbaq pnyy")).after("alpha").will(returnValue(true)).id("beta");
        mock.expects(once()).method("add").with(eq("guveq pnyy")).after("beta").will(returnValue(true));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

        underTest.add("first call");
        underTest.add("second call");
        underTest.add("third call");
    }


    public void testThrowExceptions() {
        Mock mock = mock(List.class);

        mock.expects(once()).method("clear").withNoArguments().will(throwException(new RuntimeException("hello")));
        mock.expects(once()).method("add").with(contains("Fdhveery")).will(throwException(new RuntimeException("allergic to squirrels")));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

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

    public void testIgnoreInvocations() {
        Mock mock = mock(List.class);

        mock.expects(once()).method("get").with(eq(0)).will(returnValue("Uryyb Jbeyq"));
        mock.stubs().method("add").with(ANYTHING).will(returnValue(true));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

        Assert.assertTrue(underTest.add("doesn't"));
        Assert.assertTrue(underTest.add("matter"));
        Assert.assertTrue(underTest.add("it's all"));
        Assert.assertTrue(underTest.add("ignored"));
        Assert.assertTrue(underTest.add("anyway"));
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    public void testConsecutiveCalls() {
        Mock mock = mock(List.class);

        mock.expects(exactly(3)).method("get").with(eq(0)).will(onConsecutiveCalls(returnValue("bar"), returnValue("gjb"), returnValue("guerr")));
        mock.expects(exactly(2)).method("get").with(eq(1234)).will(onConsecutiveCalls(returnValue("nqvbf"), throwException(new RuntimeException("blah blah"))));

        @SuppressWarnings("unchecked")
        ROT13List underTest = new ROT13List((List<String>) mock.proxy());

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
