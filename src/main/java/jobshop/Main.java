package jobshop;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jobshop.solvers.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {

    /** All solvers available in this program */
    private static HashMap<String, Solver> solvers;
    static {
        solvers = new HashMap<>();
        solvers.put("basic", new BasicSolver());
        solvers.put("random", new RandomSolver());
        
        solvers.put("spt", new GreedySolver(GreedySolver.Priority.SPT));
        solvers.put("lpt", new GreedySolver(GreedySolver.Priority.LPT));
        solvers.put("srpt", new GreedySolver(GreedySolver.Priority.SRPT));
        solvers.put("lrpt", new GreedySolver(GreedySolver.Priority.LRPT));
        solvers.put("est-lrpt", new GreedySolver(GreedySolver.Priority.EST_LRPT));
        solvers.put("est-spt", new GreedySolver(GreedySolver.Priority.EST_SPT));
        
        solvers.put("descent-spt", new DescentSolver(GreedySolver.Priority.SPT));
        solvers.put("descent-lpt", new DescentSolver(GreedySolver.Priority.LPT));
        solvers.put("descent-srpt", new DescentSolver(GreedySolver.Priority.SRPT));
        solvers.put("descent-lrpt", new DescentSolver(GreedySolver.Priority.LRPT));
        solvers.put("descent-est-lrpt", new DescentSolver(GreedySolver.Priority.EST_LRPT));
        solvers.put("descent-est-spt", new DescentSolver(GreedySolver.Priority.EST_SPT));
        solvers.put("descent-multi", new  DescentMultiStart(GreedySolver.Priority.EST_LRPT));
        
        solvers.put("taboo-spt", new TabooSolver(GreedySolver.Priority.SPT));
        solvers.put("taboo-lpt", new TabooSolver(GreedySolver.Priority.LPT));
        solvers.put("taboo-srpt", new TabooSolver(GreedySolver.Priority.SRPT));
        solvers.put("taboo-lrpt", new TabooSolver(GreedySolver.Priority.LRPT));
        solvers.put("taboo-est-spt", new TabooSolver(GreedySolver.Priority.EST_SPT));
        solvers.put("taboo-est-lrpt", new TabooSolver(GreedySolver.Priority.EST_LRPT));
        
        solvers.put("taboo1", new TabooSolver(GreedySolver.Priority.EST_LRPT,1));
        solvers.put("taboo5", new TabooSolver(GreedySolver.Priority.EST_LRPT,5));
        solvers.put("taboo10", new TabooSolver(GreedySolver.Priority.EST_LRPT,10));
        solvers.put("taboo20", new TabooSolver(GreedySolver.Priority.EST_LRPT,20));
        solvers.put("taboo100", new TabooSolver(GreedySolver.Priority.EST_LRPT,100));
        solvers.put("taboo1000", new TabooSolver(GreedySolver.Priority.EST_LRPT,1000));

        
        solvers.put("randomGreedy", new  GreedySolver(GreedySolver.Priority.RANDOM));
        
        solvers.put("methodeASeuil", new TresholdAccepting(GreedySolver.Priority.EST_LRPT));
    }


    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
                .defaultHelp(true)
                .description("Solves jobshop problems.");

        parser.addArgument("-t", "--timeout")
                .setDefault(1L)
                .type(Long.class)
                .help("Solver timeout in seconds for each instance");
        parser.addArgument("--solver")
                .nargs("+")
                .required(true)
                .help("Solver(s) to use (space separated if more than one)");

        parser.addArgument("--instance")
                .nargs("+")
                .required(true)
                .help("Instance(s) to solve (space separated if more than one)");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        PrintStream output = System.out;

        long solveTimeMs = ns.getLong("timeout") * 5000;

        List<String> solversToTest = ns.getList("solver");
        for(String solverName : solversToTest) {
            if(!solvers.containsKey(solverName)) {
                System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
                System.err.println("       Available solvers: " + solvers.keySet().toString());
                System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
                System.exit(1);
            }
        }
        List<String> instancePrefixes = ns.getList("instance");
        List<String> instances = new ArrayList<>();
        for(String instancePrefix : instancePrefixes) {
            List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
            if(matches.isEmpty()) {
                System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
                System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
                System.exit(1);
            }
            instances.addAll(matches);
        }

        float[] runtimes = new float[solversToTest.size()];
        float[] distances = new float[solversToTest.size()];

        try {
            output.print(  "                         ");
            for(String s : solversToTest)
                output.printf("%-30s", s);
            output.println();
            output.print("instance size  best      ");
            for(String s : solversToTest) {
                output.print("runtime makespan ecart        ");
            }
            output.println();


	        for(String instanceName : instances) {
	            int bestKnown = BestKnownResult.of(instanceName);
	
	
	            Path path = Paths.get("instances/", instanceName);
	            Instance instance = Instance.fromFile(path);
	
	            output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);
	
	            for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                    String solverName = solversToTest.get(solverId);
                    Solver solver = solvers.get(solverName);
                    long start = System.currentTimeMillis();
                    long deadline = System.currentTimeMillis() + solveTimeMs;
                    Result result = solver.solve(instance, deadline);
                    long runtime = System.currentTimeMillis() - start;
	
                    if(!result.schedule.isValid()) {
                        System.err.println("ERROR: solver returned an invalid schedule");
                        System.exit(1);
                    }
                    assert result.schedule.isValid();
                    int makespan = result.schedule.makespan();
                    float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
                    runtimes[solverId] += (float) runtime / (float) instances.size();
                    distances[solverId] += dist / (float) instances.size();
                    output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
                    output.flush();
                }
                output.println();

            }

	        output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
	        for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
	            output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
	        }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
