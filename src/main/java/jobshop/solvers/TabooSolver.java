package jobshop.solvers;

import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.DescentSolver.*;

public class TabooSolver  implements Solver {

	final int dureeTaboo = 2;
	final int maxIter = 1000;

	@Override
	public Result solve(Instance instance, long deadline) {
		//Initialisation the initial solution with 
		GreedySolver solver= new GreedySolver(GreedySolver.Priority.EST_LRPT);
		Schedule s= solver.solve(instance, deadline).schedule;	    	
		ResourceOrder currentOrder = new ResourceOrder(s);
		int currentDuration= currentOrder.toSchedule().makespan();

		ResourceOrder bestOrder = currentOrder.copy();
		int bestDuration=currentDuration;

		int[][] sTaboo= new int[instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
		int k=0;

		while(k<=maxIter && deadline - System.currentTimeMillis() > 1) {
			k++;
			ResourceOrder bestNeighborOrder = null;
			int bestNeighborDuration = Integer.MAX_VALUE;
			int index1 =-1;
			int index2 =-1;
			List<Block> criticalPathBlock = DescentSolver.blocksOfCriticalPath(currentOrder);
			for (Block block: criticalPathBlock) {
				List<Swap> neighbor= DescentSolver.neighbors(block);
				//for each neighbor, apply change on resource order
				for(Swap swap: neighbor) {
					index1=swap.machine*(currentOrder.resource[swap.machine].length)+swap.t1;
					index2=swap.machine*(currentOrder.resource[swap.machine].length)+swap.t2;
//					int index1= swap.getTasksfromSwap(currentOrder).get(0).job *instance.numTasks+swap.getTasksfromSwap(currentOrder).get(0).task;
//					int index2=swap.getTasksfromSwap(currentOrder).get(1).job *instance.numTasks+swap.getTasksfromSwap(currentOrder).get(1).task;
					ResourceOrder currentNeighborOrder = currentOrder.copy();
					swap.applyOn(currentNeighborOrder);
					int duration= currentOrder.toSchedule().makespan();
					//find the best not taboo neighbor
					if((duration <= bestDuration) || ((duration <=bestNeighborDuration) && (sTaboo[index1][index2] <= k))){
						bestNeighborDuration=duration;
						bestNeighborOrder= currentNeighborOrder; 
					}
				}
			}
			if(bestNeighborOrder == null) {
				bestNeighborDuration = currentDuration;
				bestNeighborOrder = currentOrder;
			} else {
//				int index1= bestNeighborSwap.getTasksfromSwap(currentOrder).get(0).job *instance.numTasks+bestNeighborSwap.getTasksfromSwap(currentOrder).get(0).task;
//				int index2=bestNeighborSwap.getTasksfromSwap(currentOrder).get(1).job *instance.numTasks+bestNeighborSwap.getTasksfromSwap(currentOrder).get(1).task;
				sTaboo[index1][index2]=k+dureeTaboo;
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
}
