package jobshop.solvers;

import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver.*;

public class TabooSolver  implements Solver {

	final int dureeTaboo = 10;
	final int maxIter =1000;
	final int maxIterWithoutImprovement= 1000;

	private static boolean isTaboo(int[][] sTaboo, Swap swap, ResourceOrder order, int k) {
        Task t1 = swap.getTasksfromSwap(order).get(0);
        Task t2 = swap.getTasksfromSwap(order).get(1);
        return k <sTaboo[t1.job * order.instance.numTasks + t1.task][t2.job * order.instance.numTasks + t2.task];
    }

    private void setTaboo(int[][] sTaboo, Swap swap, ResourceOrder order, int k) {
    	Task t1 = swap.getTasksfromSwap(order).get(0);
        Task t2 = swap.getTasksfromSwap(order).get(1);
        sTaboo[t2.job * order.instance.numTasks + t2.task][t1.job * order.instance.numTasks + t1.task]= dureeTaboo + k;
    }

    @Override
    public Result solve(Instance instance, long deadline) {
    	//Initialisation the initial solution with 
    	GreedySolver solver= new GreedySolver(GreedySolver.Priority.EST_LRPT);

    	Schedule s= solver.solve(instance, deadline).schedule;	 

    	ResourceOrder bestOrder = new ResourceOrder(s);
    	int bestDuration=bestOrder.toSchedule().makespan();

    	ResourceOrder currentOrder = bestOrder;
    	int currentDuration= bestDuration;

    	int[][] sTaboo= new int[instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
    	int k=0;
    	int improve=0;

    	while(k<=maxIter && deadline - System.currentTimeMillis() > 1 && improve<=maxIterWithoutImprovement) {
    		k++;
    		improve++;
    		ResourceOrder bestNeighborOrder = null;
    		Swap bestNeighborSwap= null;
    		int bestNeighborDuration = Integer.MAX_VALUE;

    		List<Block> criticalPathBlock = DescentSolver.blocksOfCriticalPath(currentOrder);
    		for (Block block: criticalPathBlock) {
    			List<Swap> neighbor= DescentSolver.neighbors(block);
    			//for each neighbor, apply change on resource order
    			for(Swap swap: neighbor) {
    				if(!isTaboo(sTaboo, swap, currentOrder, k)) {
    					ResourceOrder currentNeighborOrder = currentOrder.copy();
    					swap.applyOn(currentNeighborOrder);
    					int duration= currentNeighborOrder.toSchedule().makespan();
    					//find the best not taboo neighbor
    					if((duration < bestDuration) || ((duration < bestNeighborDuration) &&  !isTaboo(sTaboo,swap, currentOrder, k))){
    						bestNeighborDuration=duration;
    						bestNeighborOrder= currentNeighborOrder; 
    						bestNeighborSwap= swap;
    						if(duration < bestDuration) improve=0;
    					}
    				}
    			}
    		}

    		if(bestNeighborOrder == null) {
    			bestNeighborDuration = currentDuration;
    			bestNeighborOrder = currentOrder;
    		} else {
    			setTaboo(sTaboo, bestNeighborSwap, currentOrder, k);
    			currentOrder=bestNeighborOrder;
    			currentDuration= bestNeighborDuration;

    			if(bestNeighborDuration < bestDuration) {
    				bestOrder = bestNeighborOrder;
    				bestDuration = bestNeighborDuration;
    			}
    		}
    	}


    	Result.ExitCause exitCause = Result.ExitCause.Blocked;
    	if(System.currentTimeMillis() >= deadline) {
    		exitCause = Result.ExitCause.Timeout;
    	}
    	return new Result(instance, bestOrder.toSchedule(), exitCause);
    }
    
//    @Override
//    public Result solve(Instance instance, long deadline) {
//        Solver solver =new GreedySolver(GreedySolver.Priority.EST_LRPT);
//
//        ResourceOrder bestSolution = new ResourceOrder(solver.solve(instance, -1).schedule);
//        int bestMakespan = bestSolution.toSchedule().makespan();
//
//        ResourceOrder currentSolution = bestSolution;
//        int currentMakeSpan = bestMakespan;
//
//        //implicitly filled with zeros
//        int[][] tabooSolutions = new int[instance.numJobs * instance.numTasks][instance.numJobs * instance.numTasks];
//
//        int k = 0;
//
//        while(k < maxIter && System.currentTimeMillis() < deadline) {
//            k++;
//
//            DescentSolver.Swap bestNeighborSwap = null;
//            ResourceOrder bestNeighborSolution = null;
//            int bestNeighborMakeSpan = Integer.MAX_VALUE;
//
//            List<DescentSolver.Block> blocks = DescentSolver.blocksOfCriticalPath(currentSolution);
//            for (DescentSolver.Block currentBlock : blocks) {
//                List<DescentSolver.Swap> blockNeighbors = DescentSolver.neighbors(currentBlock);
//                for (DescentSolver.Swap currentSwap : blockNeighbors) {
//                    if(!isTaboo(tabooSolutions, currentSwap, currentSolution, k)) {
//                        ResourceOrder currentNeighborSolution = currentSolution.copy();
//                        currentSwap.applyOn(currentNeighborSolution);
//                        int currentNeighborMakeSpan = currentNeighborSolution.toSchedule().makespan();
//                        if (
//                                currentNeighborMakeSpan < bestMakespan ||
//                                (currentNeighborMakeSpan < bestNeighborMakeSpan && !isTaboo(tabooSolutions, currentSwap, currentSolution, k))
//                        ) {
//                            bestNeighborSwap = currentSwap;
//                            bestNeighborSolution = currentNeighborSolution;
//                            bestNeighborMakeSpan = currentNeighborMakeSpan;
//                        }
//                    }
//                }
//            }
//
//            if(bestNeighborSolution == null) {
//                bestNeighborSolution = currentSolution;
//                bestNeighborMakeSpan = currentMakeSpan;
//
//            } else {
//                setTaboo(tabooSolutions, bestNeighborSwap, currentSolution, k);
//
//                currentSolution = bestNeighborSolution;
//                currentMakeSpan = bestNeighborMakeSpan;
//
//                if (bestNeighborMakeSpan < bestMakespan) {
//                    bestSolution = bestNeighborSolution;
//                    bestMakespan = bestNeighborMakeSpan;
//                }
//            }
//        }
//
//        Result.ExitCause exitCause = Result.ExitCause.Blocked;
//        if(System.currentTimeMillis() >= deadline) {
//            exitCause = Result.ExitCause.Timeout;
//        }
//        return new Result(instance, bestSolution.toSchedule(), exitCause);
//    }
}