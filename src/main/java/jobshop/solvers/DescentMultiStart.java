package jobshop.solvers;

import java.util.ArrayList;
import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.GreedySolver.Priority;

public class DescentMultiStart extends DescentSolver{

    public DescentMultiStart(Priority prio) {
		super(prio);
	}

	public Result solveRandom(Instance instance, long deadline) {
    	//Initialisation the initial solution with  SPT
    	GreedySolver glutonne= new GreedySolver(GreedySolver.Priority.RANDOM);
    	Schedule s= glutonne.solve(instance, deadline).schedule;
    	Schedule best=s;
    	
    	ResourceOrder currentOrder = new ResourceOrder(s);
    	int currentDuration= currentOrder.toSchedule().makespan();
    	
    	ResourceOrder bestOrder = currentOrder.copy();
    	int bestDuration=currentDuration;
    	
    	int improvement = 1;
    	while (deadline - System.currentTimeMillis() > 1 &&  improvement == 1 ) {
    		List<Block> criticalPathBlock = blocksOfCriticalPath(currentOrder);
    		//for each block of the critical path,find its neighbor
        	for (Block block: criticalPathBlock) {
        		List<Swap> neighbor= neighbors(block);
        		//for each neighbor, apply change on resource order
        		for(Swap swap: neighbor) {
        			ResourceOrder cp= new ResourceOrder(best);
        			swap.applyOn(cp);
        			int duration= cp.toSchedule().makespan();
    				if(duration<bestDuration) {
    					bestDuration=duration;
            			best= cp.toSchedule() ;
            			bestOrder= currentOrder.copy(); 
            			improvement=1;
            		}
            		else improvement=0;
        		}
        	}
        	currentOrder=  bestOrder.copy();
    	}
		return new Result(instance, best, Result.ExitCause.Timeout);
    }
    
    @Override
    public Result solve(Instance instance, long deadline) {
    	List<Result> results= new ArrayList<Result>(4);
    	results.add(solveRandom( instance,  deadline));
    	results.add(solveRandom( instance,  deadline));
    	results.add(solveRandom( instance,  deadline));
    	results.add(solveRandom( instance,  deadline));

    	int min = Integer.MAX_VALUE;
    	Result best=null;
    	for(Result r:results) {
    		if(r.schedule.makespan()<min) {
    			min= r.schedule.makespan();
    			best=r;
    		}
    	}
    	return best;
    }
}
