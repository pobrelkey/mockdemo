package mockdemo;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


// Quick and dirty performance benchmarking test comparing different mocking libraries.
// For a large data set over which to run analysis, leave something like the following running over an evening/weekend:
// while true ; do for i in 1 5 10 25 50 100 250 500 1000 2500 5000 10000 ; do ( date ; mvn exec:exec -Dexec.executable=java -Dexec.classpathScope=test -Dexec.args="-Xmx256m -cp %classpath mockdemo.PerformanceBenchmarker ${i}" ) | grep -Ev '^\[INFO\] ' | tee -a tests.out ; done ; done
public class Benchmocker {

    static private Class[] TEST_CLASSES = {
            EasyMockTest.class,
            JMock1Test.class,
            JMock2Test.class,
            JMockitTest.class,
            MockitoTest.class,
            MoxieTest.class
    };


    private static List<Class> permutation(int i) {
        ArrayList<Class> result = new ArrayList<Class>(TEST_CLASSES.length);
        ArrayList<Class> candidates = new ArrayList<Class>(Arrays.asList(TEST_CLASSES));
        while (!candidates.isEmpty()) {
            result.add(candidates.remove(i % candidates.size()));
            i /= (candidates.size()+1);
        }
        return result;
    }

    public static void main(String[] args) {
        // how many loops to run?
        int cyclesPerTest = 1000;
        try {
            cyclesPerTest = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // just do 1000 runs per test then...
        }

        //  compute f = factorial of TEST_CLASSES.length
        int f = 1;
        for (int i = 2; i <= TEST_CLASSES.length; i++) {
            f *= i;
        }

        // generate list of integers 0 through f
        ArrayList<Integer> integers = new ArrayList<Integer>();
        for (int i = 0; i < f; i++) {
            integers.add(f);
        }

        // generate running order of test classes
        ArrayList<Integer> permutationNumbers = new ArrayList<Integer>();
        ArrayList<Class> runningOrder = new ArrayList<Class>();
        while (runningOrder.size() < TEST_CLASSES.length * cyclesPerTest) {
            if (permutationNumbers.isEmpty()) {
                permutationNumbers.addAll(integers);
                Collections.shuffle(permutationNumbers);
            }
            runningOrder.addAll(permutation(permutationNumbers.remove(0)));
        }

        // now run the tests, tabulating total run times by class
        Map<Class, Long> runTimes = new HashMap<Class, Long>();
        for (Class testClass : TEST_CLASSES) {
            runTimes.put(testClass, 0L);
        }
        for (Class testClass : runningOrder) {
            Result testResult = JUnitCore.runClasses(testClass);
            runTimes.put(testClass, runTimes.get(testClass) + testResult.getRunTime());
        }

        // print results
        System.out.println(cyclesPerTest + " cycles per test:");
        TreeMap<String, Class> sortedClasses = new TreeMap<String, Class>();
        for (Class clazz : TEST_CLASSES) {
            sortedClasses.put(clazz.getSimpleName(), clazz);
        }
        for (Class clazz : sortedClasses.values()) {
            System.out.println(clazz.getSimpleName() + " = " + runTimes.get(clazz));
        }
    }

}
