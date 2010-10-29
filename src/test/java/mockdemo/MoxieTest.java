package mockdemo;

import moxie.Group;
import moxie.Mock;
import moxie.Moxie;
import moxie.MoxieRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class MoxieTest {
    @Rule
    public MoxieRule moxie = new MoxieRule();

    @Mock
    private List<String> mock;

    @Mock
    private List<String> otherMock;

    private Group sequence;

    @Test
    public void simpleScenario() {
        Moxie.expect(mock).will().clear();
        Moxie.expect(mock).andReturn(true).on().add("Uryyb Jbeyq");
        Moxie.expect(mock).andReturn(1).on().size();
        Moxie.expect(mock).andReturn("Uryyb Jbeyq").on().get(0);

        ROT13List underTest = new ROT13List(mock);

        underTest.clear();
        Assert.assertTrue(underTest.add("Hello World"));
        Assert.assertEquals(1, underTest.size());
        Assert.assertEquals("Hello World", underTest.get(0));
    }

    @Test
    public void fuzzyParameterMatching() {
        Moxie.expect(mock).andReturn("Tbbqolr Whcvgre").on().get(0);
        Moxie.expect(mock).andReturn("Uryyb Jbeyq").atLeastOnce().on().get(Moxie.gt(0));
        Moxie.expect(mock).andReturn(true).on().add(Moxie.endsWith("Natryf"));
        Moxie.expect(mock).andReturn(false).on().add(Moxie.endsWith("Qrivyf"));
        Moxie.expect(mock).andReturn("Obawbhe Zrephel").on().set(Moxie.anyInt(), Moxie.<String>anything());

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
        Moxie.expect(mock).inGroup(sequence).will().add("svefg pnyy");
        Moxie.expect(otherMock).inGroup(sequence).will().add("frpbaq pnyy");
        Moxie.expect(mock).inGroup(sequence).will().add("guveq pnyy");

        ROT13List underTest = new ROT13List(mock);
        ROT13List otherUnderTest = new ROT13List(otherMock);

        underTest.add("first call");
        otherUnderTest.add("second call");
        underTest.add("third call");
    }

    @Test
    public void throwExceptions() {
        Moxie.expect(mock).andThrow(new RuntimeException("hello")).on().clear();
        Moxie.expect(mock).andThrow(new RuntimeException("allergic to squirrels")).on().add(Moxie.hasSubstring("Fdhveery"));

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
        Moxie.expect(mock).andReturn("Uryyb Jbeyq").on().get(0);
        Moxie.stub(mock).andReturn(true).on().add(Moxie.<String>anything());

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
        Moxie.expect(mock).andConsecutivelyReturn("bar", "gjb", "guerr").times(3).on().get(0);
        Moxie.expect(mock).andReturn("nqvbf").andThrow(new RuntimeException("blah blah")).on().get(1234);

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
