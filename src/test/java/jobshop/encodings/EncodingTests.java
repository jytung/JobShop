package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {

    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println("test jobNumber 1\n");
        System.out.println(sched.toString());
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        System.out.println("test jobNumber 2\n");
        Schedule sched1 = enc.toSchedule();
        System.out.println(sched1.toString());
        
        sched1 = enc.toSchedule();
        assert sched1.isValid();
        assert sched1.makespan() == 14;
    }
    
    @Test
    public void testResourceOrder() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        ResourceOrder enc = new ResourceOrder(instance);
        enc.resource[0][enc.nextToSet[0]++] = new Task(0,0);
        enc.resource[0][enc.nextToSet[0]++] = new Task(1,1);
        enc.resource[1][enc.nextToSet[1]++] = new Task(1,0);
        enc.resource[1][enc.nextToSet[1]++] = new Task(0,1);
        enc.resource[2][enc.nextToSet[2]++] = new Task(0,2);
        enc.resource[2][enc.nextToSet[2]++] = new Task(1,2);

        Schedule sched = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println("test resource order 1\n");
        System.out.println(sched.toString());
        assert sched.isValid();
        assert sched.makespan() == 12;

        // numéro de jobs : 1 1 2 2 1 2
        enc = new ResourceOrder(instance);
        enc.resource[0][enc.nextToSet[0]++] = new Task(1,1);
        enc.resource[0][enc.nextToSet[0]++] = new Task(0,0);
        enc.resource[1][enc.nextToSet[1]++] = new Task(1,0);
        enc.resource[1][enc.nextToSet[1]++] = new Task(0,1);
        enc.resource[2][enc.nextToSet[2]++] = new Task(0,2);
        enc.resource[2][enc.nextToSet[2]++] = new Task(1,2);

        System.out.println("test resource order 2\n");
        Schedule sched1 = enc.toSchedule();
        System.out.println(sched1.toString());
        sched1 = enc.toSchedule();
        assert sched1.isValid();
        assert sched1.makespan() == 16;
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

}
