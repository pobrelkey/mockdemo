#!/usr/bin/env ruby

require 'find'

sections = {}

dir = File.dirname(__FILE__)
Find.find(dir) do |path|
  next if path !~ /\.java$/
  filename = path[dir.length, path.length - dir.length].gsub(Regexp.quote(File::SEPARATOR), '/').sub(/^\/+/,'')

  source = File.read(path)
  source = source.gsub(/^(package|import)\s+(\w+\.)*\w+;\s*$/,'')
  source = source.gsub(/^\s*@SuppressWarnings\("unchecked"\)\s*$(?:\r\n?|\n)?/,'')
  source = source.strip
  sections[filename] = source
  source = source.sub(/^(.*)\}[\s\r\n]+$/m,'\1')

  key = "#{filename}##start"
  lines = []
  source.each_line do |line|
    lines << line
    if line =~ /^\s*(?:(?:public|private|protected|abstract|final|@\w+)\s+)*(?:<.*?>\s+)?(?:\w+(?:\s*<.*?>)?\s+)?(\w+)\s*\((?:[^\s,\)]+\s+[^\s,\)]+(?:,\s+|(?=\))))*\)\s*\{\s*$/
      key = "#{filename}##{$1}"
    elsif line =~ /^\s*(?:\}\s*)?$/
      (sections[key] || (sections[key] = '')) << lines.join('')
      lines = []
    end
  end
  (sections[key] || (sections[key] = '')) << lines.join('') if !lines.empty?
end

print DATA.read.gsub(/^#include\s+(\S+)\s*$/) {
  s = sections[$1]
  if s
    unindent = s.split(/[\r\n]/).grep(/^(\s*)[^\s\}]/){$1}.sort{|a,b| a.length <=> b.length }.first || ''
    s.gsub!(Regexp.compile('(^|\r\n?|\n)'+Regexp.quote(unindent)), '\1')
    s.gsub!(/(\r\n?|\n)[\r\n]+\}/,'\1}')
  end
  s.strip || $&
}
__END__

<wiki:toc max_depth="2" />

= Introduction =

This is a brief comparison of several major [http://www.mockobjects.com/ mock object] libraries available for Java today.

The examples presented don't necessarily illustrate the sole correct way of writing tests using these libraries.  They illustrate the way I've usually seen tests written with each library - your mileage may vary.  The examples should be sufficient to give a flavor of differences in mocking syntax, and other relative strengths and weaknesses.

You can download the example tests in this article as a Maven project from Moxie's [http://code.google.com/p/moxiemocks/downloads/list download page], or get them from Moxie's Google Code [http://moxiemocks.googlecode.com/svn/mockdemo/ SVN repository].

This article was inspired by Jean Tessier's [http://jeantessier.com/SoftwareEngineering/Mocking.html "Mocking in Java: jMock vs. EasyMock"].


= Disclaimer =

I happen to be the author of [http://www.moxiemocks.org/ Moxie].  I wrote Moxie after I spent nearly three years on a project that used all four other libraries in this comparison, and got annoyed with all of them one reason or another.  So I'm somewhat biased.

= The Libraries =

=== JMock 2 ===

[http://www.jmock.org/ JMock 2] has had the most attention of the current crop of Java mocking libraries - it has had a large user base, and has been mentioned in numerous books (including _[http://www.growing-object-oriented-software.com/ Growing Object-Oriented Software Guided by Tests]_, written by JMock's authors).  It was notable for having a mocking syntax designed to make sense as a domain-specific language (DSL).  Proponents say its DSL is the most expressive API of any mocking library; critics say its mocking syntax as commonly used, involving the creation of an anonymous inner class per test, is excessively verbose.

JMock 2 should be considered a different library from its predecessor, JMock 1; its mocking syntax is almost entirely different.

The example code in this article was tested using JMock 2.5.1.

=== JMock 1 ===

[http://www.jmock.org/jmock1.html JMock 1] was my introduction to mocking around 2003.  JMock 1 is generally considered a legacy library; new projects should use one of the other libraries in this article.

Compared to more modern mocking libraries, a major drawback of JMock 1 is that the code dates back to the JDK 1.3 days, and as such makes no attempt to use generics to provide a refactorable mocking syntax - i.e. one where using a refactoring tool to change method names/signatures will result in appropriate changes to mocking code.  JMock 1's syntax, while quite nice for its time and more concise than that of JMock 2, is reflection-heavy - names of mocked methods are specified as strings, for example.

The example code in this article was tested using JMock 1.2.0.

=== !EasyMock ===

Along with JMock 2, [http://www.easymock.org/ EasyMock] is the other established mocking library for Java 1.5; its syntax is somewhat more concise than that of the two JMocks (especially JMock 2).

Uniquely among the major mocking libraries, !EasyMock mocks are modal.  That is to say, one programs the mocks with the expected behavior, then tells the mocks to "replay" their behavior before passing them to the code under test - so your mocks can either be in "record" mode or "replay" mode.  Proponents say this makes it impossible to muddle together expectation-setting code and code that actually calls the code under test.  Critics say that the required call to {{{EasyMock.replay()}}} before calling the code under test is at best code clutter, at worst an abhorrence.

The example code in this article was tested using !EasyMock 3.0, though it should also be valid for most of the !EasyMock 2.x series.  !EasyMock 1.x had a different API.

=== Mockito ===

[http://www.mockito.org/ Mockito] is similar in API style to !EasyMock (it started out as a fork of !EasyMock), but takes a different approach to mocking: rather than setting up an exhaustive set of expectations ahead of time, you instead stub out any necessary behavior on your mocks, then after calling the code under test verify that the relevant methods on your mock objects were called.

Proponents of this "verification-driven" style say it makes tests less brittle and cuts down on the amount of meaningless boilerplate mock expectations in each test.  Critics say it makes it more difficult to write tests that are as strict as traditional "expectation-driven" tests.

This article uses all expectation-driven tests - a testing style Mockito is ill-suited to writing - which makes it a not entirely fair comparison with respect to Mockito.  The Mockito authors have an [http://code.google.com/p/mockito/wiki/MockitoVSEasyMock article] on their wiki comparing verification-driven tests in Mockito with expectation-driven tests in !EasyMock, which should give a flavor of the difference between the two styles.

The example code in this article was tested using Mockito 1.8.5.

=== JMockit ===

[http://code.google.com/p/jmockit JMockit] is a lesser-known mocking framework for Java.  It has its own syntax, most similar in appearance to JMock 2.  It's capable of both expectation-driven and verification-driven testing.

Unlike the other five mocking frameworks in this comparison, I've never used JMockit in a real project; I wrote the example code you see below in about half an hour having just downloaded JMockit.  I had previously found its syntax off-putting - with its use of anonymous inner classes, direct access to protected fields, and limited set of parameter matchers - and found little in my brief interactions with JMockit to dispel these notions.  Also, it insists on being before JUnit in the classpath, which isn't a good sign.

The example code in this article was tested using JMockit 0.999.10.

=== Moxie ===

Moxie was written from scratch after years of ~~annoyance~~ experience with the four major frameworks, taking lessons from each.  It aims to have a consistent, concise mocking syntax "nicer" than that of the competition.

The example code in this article was tested using Moxie 0.9.

----

= The Class Under Test =

Our example code will use mocks to test {{{ROT13List}}} - a simple class that implements {{{List&lt;String&gt;}}}, transforming the strings using the [http://en.wikipedia.org/wiki/ROT13 ROT13] algorithm when storing/retrieving them from an underlying {{{List&lt;String&gt;}}}.

{{{
#include src/main/java/mockdemo/ROT13List.java
}}}

----

= Boilerplate Code =

Tests using the different mocking libraries typically have different amounts of boilerplate code to enable mocking in the test.  In the examples below, import statements are omitted for brevity.

=== JMock 2 ===

This shows JMock 2.5.1 as used with JUnit 4.  The JMock web site also has [http://www.jmock.org/getting-started.html examples] for using JMock 2 stand-alone and with JUnit 3.

{{{
#include src/test/java/mockdemo/JMock2Test.java##start
}}}

=== JMock 1 ===

JMock 1 requires that you write your tests as JUnit 3 tests extending the JMock test base class.

{{{
#include src/test/java/mockdemo/JMock1Test.java##start
}}}

=== !EasyMock ===

!EasyMock has no out-of-the-box integration with JUnit, so !EasyMock tests don't need any boilerplate code.

{{{
#include src/test/java/mockdemo/EasyMockTest.java##start
}}}

Note that some people prefer to write their tests using an {{{IMocksControl}}} instance - partly to give them finer-grained control over !EasyMock's behavior, partly out of an aversion to calling static methods on the {{{EasyMock}}} class.  The most common pattern is to create this object in the setup method.  For more about {{{IMocksControl}}}, see the [http://www.easymock.org/EasyMock3_0_Documentation.html EasyMock documentation].

=== Mockito ===

Mockito offers a {{{MockitoJUnitRunner}}} for use with JUnit 4, which automatically puts a fresh mock object into each field annotated with {{{@Mock}}} before each test.  You can opt not to use this integration, in which case mock creation will look quite similar to the !EasyMock examples.

{{{
#include src/test/java/mockdemo/MockitoTest.java##start
}}}

=== JMockit ===

When used as directed with JUnit 4 (including putting the JMockit jar ahead of the JUnit jar on the classpath), JMockit will automatically put a mock object into each field annotated with {{{@Mocked}}} before each test. 

{{{
#include src/test/java/mockdemo/JMockitTest.java##start
}}}

=== Moxie ===

Moxie has {{{MoxieRule}}}, usable under JUnit 4.7 or later.  Like Mockito's JUnit integration, it automatically creates mocks before each test; unlike Mockito, it also verifies them afterward.  Without {{{MoxieRule}}}, manual mock creation/verification looks very similar to !EasyMock.

An explanatory note: {{{MoxieRule}}} also populates fields of type {{{Group}}}; these are used to check that calls arrive in a sequence across mocks, and generally aren't needed otherwise.

{{{
#include src/test/java/mockdemo/MoxieTest.java##start
}}}

----

= A Simple Test =

This test performs a few simple operations on {{{ROT13List}}}, verifying that they are passed through appropriately to the underlying {{{List}}}.

=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#simpleScenario
}}}

In JMock 2, mock expectations tend to be set on an anonymous subclass of JMock's {{{Expectations}}} class.  {{{Expectations}}} contains most of the methods of JMock's domain-specific language.  Note that these methods are all {{{public}}} rather than {{{protected}}}, so there's nothing preventing you from instantiating a run-of-the-mill (i.e. not an anonymous subclass) instance of {{{Expectations}}} and calling methods on that to set up your mocks; the syntax seen here is usually used for brevity.

For those unfamiliar with this Java syntax, the code within the double braces is a Java [http://java.sun.com/docs/books/jls/third_edition/html/classes.html#246032 instance initializer] block - essentially a no-arg constructor for the anonymous subclass created in this method.

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testSimpleScenario
}}}

Note that nowhere in JMock 1's syntax does a call take place to the actual method being mocked; the method name is passed as a string.  This means that refactoring tools will fail to update mock tests when method names or signatures change, resulting in broken tests.

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#simpleScenario
}}}

Note the distinctive feature of !EasyMock's syntax: First calls are made to the mock to set the expectations. Then the call to {{{EasyMock.replay()}}} signals that setup is finished, and that further calls on the mock are to be checked against the expectations.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#simpleScenario
}}}

You can probably see from this example why Mockito is ill-suited to writing exhaustive tests.  To perform the equivalent of an expectation-driven test, every method called on the mock must be separately verified.  (This is on top of any stubbing you need to perform ahead of time, leading to potentially two calls per method instead of one.)  Furthermore, calls to unstubbed methods don't throw exceptions, making it difficult to track down places where unexpected calls occur.

Again, these examples aren't entirely fair on Mockito, which comes from a different philosophy of testing.  Mockito fans would argue that trying to verify too many things as in this example makes your tests too broad and brittle, and your tests should instead take Mockito's lighter-weight approach of only verifying what you must.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#simpleScenario
}}}

JMockit's syntax looks similar to that of JMock 2, with an anonymous inner class extending JMockit's {{{Expectations}}} class used to specify the expected calls.

Note that you specify the value that each mock call will return by assigning a value to the magical {{{result}}} protected variable after the call (there is also a {{{returns()}}} method you can call instead).  Other protected fields are used to set invocation counts (i.e. number of times a method will be called) and for certain argument matchers, as will be seen later.

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#simpleScenario
}}}

Mock expectations in Moxie begin by referencing the mock object, set up any details in the middle, and end with a call to the method concerned.  The methods in the middle often have a few similarly-named equivalents which do exactly the same thing.  (For example, the first statement uses {{{will()}}}, while the rest use {{{on()}}}.)  The alternatives are provided to allow one to choose the method that "sounds" better depending on context or personal tastes.

Some people (especially !EasyMock/Mockito fans) find it slightly stilted to have the method at the end of the mocking syntax.  This was done to keep the syntax consistent for void and non-void methods - probably the biggest problem with !EasyMock/Mockito's syntax.


----

= Fuzzy Parameter Matching =

Each mocking library has a mechanism by which conditional expressions can be evaluated against parameters to mocked methods.  For example, we may want an expectation to match only if the value of a parameter is greater than zero, or if it is a string containing a certain substring.  Alternately, we may not care what parameter gets passed to a method, and want the expectation to match regardless of the value passed.


=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#fuzzyParameterMatching
}}}

JMock 2's fuzzy match syntax largely revolves around [http://code.google.com/p/hamcrest/ Hamcrest] matchers; the {{{with()}}} method can be used to match parameters against an arbitrary {{{Matcher}}}.

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testFuzzyParameterMatching
}}}

JMock 1 had a {{{Constraint}}} interface that fulfilled a similar function to Hamcrest's {{{Matcher}}} interface. Unlike in JMock 2 and other mocking libraries, each parameter had to be specified as a {{{Constraint}}} - you couldn't specify a bare value and presume that only parameters equal to that value would be accepted.

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#fuzzyParameterMatching
}}}

The {{{EasyMock}}} class has many static methods that can be used to build match conditions - {{{eq()}}}, {{{lt()}}}, {{{gt()}}}, {{{isNull()}}}, {{{and()}}}, {{{or()}}}, {{{not()}}}, etc.  You can also specify custom match conditions by implementing !EasyMock's {{{IArgumentMatcher}}} interface.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#fuzzyParameterMatching
}}}

Mockito's fuzzy-match syntax is quite similar to !EasyMock's, as Mockito started life as an !EasyMock derivative.  You can specify a custom Hamcrest {{{Matcher}}} using Mockito's {{{argThat()}}} method.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#fuzzyParameterMatching
}}}

JMockit has a limited set of parameter matchers that can be used directly; it seems the accepted practice for all but the simplest cases is to use the {{{with()}}} method to specify a Hamcrest matcher.  Note that there are two forms of {{{with()}}} - the two-argument syntax (the first argument is a throwaway value) must be specified when matching a parameter of a primitive type.

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#fuzzyParameterMatching
}}}

Moxie's fuzzy-match syntax is similar to that of Mockito, including Hamcrest {{{Matcher}}} support.


----

= Calls In Sequence Across Mocks =

Most of the mocking libraries (except JMock 1) let you verify that method calls are received in a certain sequence across mocks.  For example, you might want to verify that {{{mock2.getBar()}}} is called only after a call to {{{mock1.getFoo()}}} is received, and that {{{mock3.getBaz()}}} is only called after the call to {{{mock2.getBar()}}}.

=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#callsInSequence
}}}

Sequences across mocks are tied together using a {{{Sequence}}} object.

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testCallsInSequence
}}}

JMock 1 doesn't support call sequencing across mocks, but calls within a mock can be tied together if you give them ID's.

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#callsInSequence
}}}

The !EasyMock {{{createStrictControl()}}} method gives you an {{{IMocksControl}}} that verifies the sequencing of all calls across all mocks; there appears to be no way in !EasyMock to verify the ordering of some calls and not others.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#callsInSequence
}}}

In Mockito, the {{{InOrder}}} object behaves similarly to a strict mock control in !EasyMock.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#callsInSequence
}}}

In JMockit, all calls set up within an individual instance of {{{Expectations}}} are taken to be a sequence of calls across objects that must occur in the order specified.

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#callsInSequence
}}}

Moxie uses {{{Group}}}s to tie together sequences of calls across mocks, similarly to JMock 2.  ({{{sequence}}} is a field of type {{{Group}}} automatically populated by {{{MoxieRule}}}, Moxie's JUnit 4 integration class.)


----

= Throwing Exceptions From A Mock =

Confiuring a mocked method to throw an exception rather than return a value is straightforward in most of the mocking libraries.

=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#throwExceptions
}}}

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testThrowExceptions
}}}

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#throwExceptions
}}}

Note that !EasyMock's syntax for throwing an exception from a mocked method returning void is inconsistent with that for methods that return a value.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#throwExceptions
}}}

As in !EasyMock, Mockito's void-method syntax for throwing exceptions is also inconsistent with non-void methods - though it's slightly improved.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#throwExceptions
}}}

In JMockit, you make a mock method throw an exception by assigning the desired exception to the protected {{{result}}} field immediately after the setup call.  (The {{{returns()}}} method can be used where a method actually returns an exception; no analogous {{{throws()}}} method exists for those averse to directly setting fields.)

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#throwExceptions
}}}


----

= Ignoring Calls To A Mock =

Sometimes we want calls to a certain method to have a particular behavior, while ignoring them when it comes to determining whether the test passes or fails.  The following test combines one checked mock expectation with one unchecked mock expectation.

=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#ignoreInvocations
}}}

Ignored mock calls are set up using the {{{ignoring()}}} method.

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testIgnoreInvocations
}}}

"Stub" mock methods are created in JMock 1 using {{{stubs()}}}.

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#ignoreInvocations
}}}

In !EasyMock you can effectively "ignore" a method using the {{{anyTimes()}}} modifier.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#ignoreInvocations
}}}

There's no special way of setting up an "ignored" method in Mockito - you just don't verify it at the end of the test.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#ignoreInvocations
}}}

The {{{NonStrictExpectations}}} class is used to set "ignored" expectations which won't fail the test if they aren't fulfilled.

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#ignoreInvocations
}}}

Moxie uses {{{stub()}}} to set up "ignored" mock method behavior.


----

= Responding Differently On Consecutive Calls =

Once in a great while we'll want a mocked method to exhibit one behavior on one call, and another behavior on a subsequent call.  The first mock expectation in the example returns a series of values across different calls, similar to an iterator method; the second expectation returns a value on the first call, then throws an exception on the second call.

=== JMock 2 ===

{{{
#include src/test/java/mockdemo/JMock2Test.java#consecutiveCalls
}}}

JMock 2 uses the {{{onConsecutiveCalls()}}} method to specify consecutive-calls behavior.

=== JMock 1 ===

{{{
#include src/test/java/mockdemo/JMock1Test.java#testConsecutiveCalls
}}}

JMock 1 has its own similar version of the {{{onConsecutiveCalls()}}} method.

=== !EasyMock ===

{{{
#include src/test/java/mockdemo/EasyMockTest.java#consecutiveCalls
}}}

In !EasyMock, consecutive-calls behavior is specified by calling multiple behavior-specifying methods in a row.

=== Mockito ===

{{{
#include src/test/java/mockdemo/MockitoTest.java#consecutiveCalls
}}}

Mockito specifies consecutive-calls behavior similarly to !EasyMock, but also offers a shorthand method to specify different values to be returned from a method in succession.

=== JMockit ===

{{{
#include src/test/java/mockdemo/JMockitTest.java#consecutiveCalls
}}}

JMockit offers a varargs version of the {{{returns()}}} method to specify a series of values to be consecutively returned.  Expectations that throw a series of exceptions, or exceptions after successfully returned values, have to be written out longhand.

=== Moxie ===

{{{
#include src/test/java/mockdemo/MoxieTest.java#consecutiveCalls
}}}

Moxie's consecutive-calls syntax is similar to that of Mockito.  Note that the total number of calls does not need to be explicitly specified, but if it is it will be validated against the number of consecutive-call behaviors.


