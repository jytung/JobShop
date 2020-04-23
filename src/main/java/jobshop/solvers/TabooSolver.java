package jobshop.solvers;

import java.util.LinkedList;
import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver.*;

public class TabooSolver  implements Solver {

	//attribute
	final int dureeTaboo = 10;
	final int maxIter = 300;
	int[][] sTaboo; 
	
	 @Override
	    public Result solve(Instance instance, long deadline) {
		 //TODO
		//Initialisation the initial solution with  SPT
	    	GreedySolver glutonne= new GreedySolver(GreedySolver.Priority.EST_LRPT);
	    	Schedule s= glutonne.solve(instance, deadline).schedule;
	    	Schedule best = s; 
	    	ResourceOrder order = new ResourceOrder(s);
	    	List<Block> criticalPathBlock = blocksOfCriticalPath(order);
	    	sTaboo= new int[instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
	    	//iteration counter
	    	int k=0;
	    	int bestDuration= order.toSchedule().makespan();
	    
	    	//for each block of the critical path,find its neighbor
	    	for (Block block: criticalPathBlock) {
	    		List<Swap> neighbor= neighbors(block);
	    		//for each neighbor, apply change on resource order
	    		int min =100000;
	    		int index1=-1;
	    		int index2=-1;
	    		Schedule SbestNeighbour=best;
	    		for(Swap swap: neighbor) {
	    				k++;
	    				ResourceOrder cp= order.copy();
	    				swap.applyOn(cp);
	    				int duration= cp.toSchedule().makespan();
//	    				System.out.println();
//	    				System.out.println("Swap ("+swap.t1 + ","+swap.t2 +") duration ="+duration +"machine "+ swap.machine);
	    				
	    				index1=swap.machine*(order.resource[swap.machine].length)+swap.t1;
    					index2=swap.machine*(order.resource[swap.machine].length)+swap.t2;
//    					System.out.println("index ("+index1+","+index2+")");
//    					System.out.println(sTaboo[index1][index2] +"<" + k +"??");
	    				//find the best not taboo neighbor
	    				if(duration<min && sTaboo[index1][index2]<k) {
//	    					System.out.println("HELLO");
	    					min=duration;
	    					SbestNeighbour=cp.toSchedule();
	    				}
	    		}
	    		if(index1!=-1 && index2!=-1) //add the new best neighbor to sTaboo
	    		sTaboo[index1][index2]=k+dureeTaboo;
	    		
	    		//if the new best neighbor is better than the previous best solution
	    		if(SbestNeighbour.makespan()<bestDuration) {
	    			best= SbestNeighbour;
	    		}
	    		//condition d'arret
	    		if(k>=maxIter)
	    			return new Result(instance, best, Result.ExitCause.Timeout);
	    	}
	    	return new Result(instance, best, Result.ExitCause.Timeout);
	 }
	 
	 /** Returns a list of all blocks of the critical path. */
	 List<Block> blocksOfCriticalPath(ResourceOrder order) {
		 List<Task> criticalPath= order.toSchedule().criticalPath();
		 List<Block> res=new LinkedList<Block>();
		 int currentMachine=order.instance.machine(criticalPath.get(0));
		 int start=order.find(currentMachine,criticalPath.get(0));
		 int end=start;
		 for(int i=1;i<criticalPath.size();i++) {
			 if(order.instance.machine(criticalPath.get(i))==currentMachine) {
				 end++;
			 }
			 else {
				 if(end>start){
					 //before start setting up the next block, add the previous block into the list
					 Block newBlock= new Block(currentMachine,start,end);
					 res.add(newBlock);
				 }
				 //setting up the new task of the block
				 currentMachine=order.instance.machine(criticalPath.get(i));
				 start=order.find(currentMachine,criticalPath.get(i));
				 end=start;
			 }
		 }
		 //the last task of critical path
		 if(order.instance.machine(criticalPath.get(criticalPath.size()-1))==currentMachine) {
			 Block newBlock= new Block(currentMachine,start,end);
			 res.add(newBlock);
		 }
		 return res;
	 }

	 /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
	 List<Swap> neighbors(Block block) {
		 List<Swap> res= new LinkedList<Swap>();
		 int size= block.lastTask-block.firstTask;
		 if(size==1) {
			 Swap s=new Swap(block.machine,block.firstTask,block.lastTask);
			 res.add(s);
		 }else if(size==0) {
			 //unable to swap
		 }
		 else {
			 Swap s=new Swap(block.machine,block.firstTask,block.firstTask+1);
			 res.add(s);
			 Swap s1=new Swap(block.machine,block.lastTask,block.lastTask-1);
			 res.add(s1);
		 }
		 return res;
	 }
}
