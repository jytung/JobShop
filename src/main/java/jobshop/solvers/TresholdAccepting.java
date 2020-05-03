package jobshop.solvers;

import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.DescentSolver.Block;
import jobshop.solvers.DescentSolver.Swap;

public class TresholdAccepting extends DescentSolver{

	final int tolerance=2;
	//final int tolerance= Integer.MAX_VALUE;
	
	 @Override
	    public Result solve(Instance instance, long deadline) {
	    	//Initialisation the initial solution with  SPT
	    	GreedySolver glutonne= new GreedySolver(GreedySolver.Priority.EST_LRPT);
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
	    				if(duration-bestDuration<tolerance) {
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

}
