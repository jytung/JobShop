package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver.Block;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task tmp= order.resource[this.machine][this.t1];
            order.resource[this.machine][this.t1]= order.resource[this.machine][this.t2];
            order.resource[this.machine][this.t2]= tmp;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
    	//TODO
    	//Initialisation the initial solution with  SPT
    	GreedySolver glutonne= new GreedySolver(GreedySolver.Priority.EST_LRPT);
    	Schedule s= glutonne.solve(instance, deadline).schedule;
    	ResourceOrder order = new ResourceOrder(s);
//    	System.out.println();
//    	System.out.println(order.toString());
    	Schedule best=s;
    	//Exploration des vosinages successifs
    	List<Block> criticalPathBlock = blocksOfCriticalPath(order);
    	int bestDuration= order.toSchedule().makespan();
    	//for each block of the critical path,find its neighbor
    	for (int i=0;i<criticalPathBlock.size();i++) {
    		Block block=criticalPathBlock.get(i);
    		List<Swap> neighbor= neighbors(block);
    		//for each neighbor, apply change on resource order
    		for(int n=0;n<neighbor.size();n++) {
    			ResourceOrder cp= order.copy();
    			Swap swap= neighbor.get(n);
    			swap.applyOn(cp);
    			int duration= cp.toSchedule().makespan();
    			if(duration<bestDuration) {
    				bestDuration=duration;
    				best= cp.toSchedule();
    			}
    		}
    	}
		return new Result(instance, best, Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
    	List<Task> criticalPath= order.toSchedule().criticalPath();
//    	System.out.println(criticalPath.toString());
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
//    				System.out.println("------------res-----------");
//    				for(int i1 = 0; i1 < res.size(); i1++) System.out.println("first "+ res.get(i1).firstTask+" ,last "+res.get(i1).lastTask+ ",machine "+res.get(i1).machine );
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
//			System.out.println("------------res-----------");
//			for(int i1 = 0; i1 < res.size(); i1++) System.out.println("first "+ res.get(i1).firstTask+" ,last "+res.get(i1).lastTask+ ",machine "+res.get(i1).machine );
    	}
    	return res;
    }
    
    
    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
    	List<Swap> res= new LinkedList<Swap>();
    	int size= block.lastTask-block.firstTask;
//    	for (int i=0;i<size;i++) {
//    		Swap s=new Swap(block.machine,block.firstTask+i,block.firstTask+i+1);
//    		res.add(s);
//    	}
//    	System.out.println(res.size());
    	if(size==1) {
    		Swap s=new Swap(block.machine,block.firstTask,block.lastTask);
    		res.add(s);
    	}else if(size==0) {
    		//System.out.println("unable to swipe");
    	}
    	else {
    		Swap s=new Swap(block.machine,block.firstTask,block.firstTask+1);
    		res.add(s);
    		Swap s1=new Swap(block.machine,block.lastTask,block.lastTask-1);
    		res.add(s1);
    	}
    	//System.out.println(res.size());
    	return res;
    }

}