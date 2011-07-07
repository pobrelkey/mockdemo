package mockdemo;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class EasyMockTest {

    @Test
    public void simpleScenario() {
        @SuppressWarnings("unchecked")
        List<String> mock = EasyMock.createMock(List.class);

        mock.clear();  // set expectation that the "clear" method will be called
        EasyMock.expect(mock.add("Uryyb Jbeyq")).andReturn(true);
        EasyMock.expect(mock.size()).andReturn(1);
        EasyMock.expect(mock.get(0)).andReturn("Uryyb Jbeyq");
        EasyMock.replay(mock);

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));

        EasyMock.verify(mock);
    }

    @Test
    public void fuzzyParameterMatching() {
        @SuppressWarnings("unchecked")
        List<String> mock = EasyMock.createMock(List.class);

        EasyMock.expect(mock.get(0)).andReturn("Tbbqolr Whcvgre");
        EasyMock.expect(mock.get(EasyMock.gt(0))).andReturn("Uryyb Jbeyq").atLeastOnce();
        EasyMock.expect(mock.add(EasyMock.endsWith("Natryf"))).andReturn(true);
        EasyMock.expect(mock.add(EasyMock.endsWith("Qrivyf"))).andReturn(false);
        EasyMock.expect(mock.set(EasyMock.anyInt(), EasyMock.<String>anyObject())).andReturn("Obawbhe Zrephel");
        EasyMock.replay(mock);

        ROT13List underTest = new ROT13List(mock);

        Assert.assertEquals("Goodbye Jupiter", underTest.get(0));
        Assert.assertEquals("Hello World", underTest.get(1));
        Assert.assertEquals("Hello World", underTest.get(19));
        Assert.assertEquals("Hello World", underTest.get(90210));
        Assert.assertTrue(underTest.add("California Angels"));
        Assert.assertFalse(underTest.add("New Jersey Devils"));
        Assert.assertEquals("Bonjour Mercury", underTest.set(0, "something"));

        EasyMock.verify(mock);
    }

    @Test
    public void callsInSequence() {
        IMocksControl control = EasyMock.createStrictControl();
        @SuppressWarnings("unchecked")
        List<String> mock = control.createMock(List.class);
        @SuppressWarnings("unchecked")
        List<String> otherMock = control.createMock(List.class);

        EasyMock.expect(mock.add("svefg pnyy")).andReturn(true);
        EasyMock.expect(otherMock.add("frpbaq pnyy")).andReturn(true);
        EasyMock.expect(mock.add("guveq pnyy")).andReturn(true);
        control.replay();

        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");

        control.verify();
    }


    @Test
    public void throwExceptions() {
        @SuppressWarnings("unchecked")
        List<String> mock = EasyMock.createMock(List.class);

        mock.clear();  // create expectation that clear() will be called
        EasyMock.expectLastCall().andThrow(new RuntimeException("hello"));
        EasyMock.expect(mock.add(EasyMock.contains("Fdhveery"))).andThrow(new RuntimeException("allergic to squirrels"));
        EasyMock.replay(mock);

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

        EasyMock.verify(mock);
    }

    @Test
    public void ignoreInvocations() {
        @SuppressWarnings("unchecked")
        List<String> mock = EasyMock.createMock(List.class);

        EasyMock.expect(mock.get(0)).andReturn("Uryyb Jbeyq");
        EasyMock.expect(mock.add(EasyMock.<String>anyObject())).andReturn(true).anyTimes();
        EasyMock.replay(mock);

        ROT13List underTest = new ROT13List(mock);

        Assert.assertTrue(underTest.add("doesn't"));
        Assert.assertTrue(underTest.add("matter"));
        Assert.assertTrue(underTest.add("it's all"));
        Assert.assertTrue(underTest.add("ignored"));
        Assert.assertTrue(underTest.add("anyway"));
        Assert.assertEquals("Hello World", underTest.get(0));

        EasyMock.verify(mock);
    }

    @Test
    public void consecutiveCalls() {
        @SuppressWarnings("unchecked")
        List<String> mock = EasyMock.createMock(List.class);

        EasyMock.expect(mock.get(0)).andReturn("bar").andReturn("gjb").andReturn("guerr");
        EasyMock.expect(mock.get(1234)).andReturn("nqvbf").andThrow(new RuntimeException("blah blah"));
        EasyMock.replay(mock);

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

        EasyMock.verify(mock);
    }


}
