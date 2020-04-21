package jobshop.encodings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;


//représentation par ordre de passage sur ressources
public class ResourceOrder extends Encoding {
	// for each machine m, resource[m] is an array of tasks to be
    // executed on this machine in the same order
	public Task[][] resource;
	// for each machine, indicate on many tasks have been initialized
	public int[] nextToSet;
	
	public ResourceOrder(Instance instance) {
		super(instance);
		resource = new Task[instance.numMachines][instance.numJobs];
		nextToSet = new int[instance.numMachines];
    }

	 /** Creates a resource order from a schedule. */
    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.resource = new Task[pb.numMachines][];
        this.nextToSet = new int[instance.numMachines];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for this machine, find all tasks that are executed on it and sort them by their start time
            resource[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextToSet[m] = instance.numJobs;
        }
    }	
	
    /*@Override
	public Schedule toSchedule() {
		// for each task, its start time
		int[][] startTimes = new int[instance.numJobs][instance.numTasks];
		int[] nextTask = new int[instance.numJobs];
		int[] nextFreeTimeResource = new int[instance.numMachines];
		int nbNotScheduled=instance.numJobs*instance.numTasks;
		while(nbNotScheduled>0) {
			for(int machine=0;machine <instance.numMachines;machine++) {
				for(Task t:resource[machine]) {
		            // earliest start time for this task
		            int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
		            est = Math.max(est, nextFreeTimeResource[machine]);
		            startTimes[t.job][t.task] = est;
		            nextFreeTimeResource[machine] = est + instance.duration(t.job, t.task);
		            nextTask[t.job] = t.task + 1;
		            nbNotScheduled --;
				}
			}
		}
		return new Schedule(instance, startTimes);
	}
	*/
    
    @Override
    public Schedule toSchedule() {
        // indicate for each task that have been scheduled, its start time
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

        // for each job, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByJob = new int[instance.numJobs];

        // for each machine, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByMachine = new int[instance.numMachines];

        // for each machine, earliest time at which the machine can be used
        int[] releaseTimeOfMachine = new int[instance.numMachines];


        // loop while there remains a job that has unscheduled tasks
        while(IntStream.range(0, instance.numJobs).anyMatch(m -> nextToScheduleByJob[m] < instance.numTasks)) {

            // selects a task that has noun scheduled predecessor on its job and machine :
            //  - it is the next to be schedule on a machine
            //  - it is the next to be scheduled on its job
            // if there is no such task, we have cyclic dependency and the solution is invalid
            Optional<Task> schedulable =
                    IntStream.range(0, instance.numMachines) // all machines ...
                    .filter(m -> nextToScheduleByMachine[m] < instance.numJobs) // ... with unscheduled jobs
                    .mapToObj(m -> this.resource[m][nextToScheduleByMachine[m]]) // tasks that are next to schedule on a machine ...
                    .filter(task -> task.task == nextToScheduleByJob[task.job])  // ... and on their job
                    .findFirst(); // select the first one if any

            if(schedulable.isPresent()) {
                // we found a schedulable task, lets call it t
                Task t = schedulable.get();
                int machine = instance.machine(t.job, t.task);

                // compute the earliest start time (est) of the task
                int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
                est = Math.max(est, releaseTimeOfMachine[instance.machine(t)]);
                startTimes[t.job][t.task] = est;

                // mark the task as scheduled
                nextToScheduleByJob[t.job]++;
                nextToScheduleByMachine[machine]++;
                // increase the release time of the machine
                releaseTimeOfMachine[machine] = est + instance.duration(t.job, t.task);
            } else {
                // no tasks are schedulable, there is no solution for this resource ordering
                return null;
            }
        }
        // we exited the loop : all tasks have been scheduled successfully
        return new Schedule(instance, startTimes);
    }
	
//	public ResourceOrder fromSchedule(Schedule s) {
//		ResourceOrder res = new ResourceOrder(s.pb);
//		int machine;
//		Task best[] = new Task[res.instance.numMachines];
//		while(res.nextToSet[0]< res.instance.numJobs) {
//			Arrays.fill(best,null);
//			for(int i=0;i<res.instance.numJobs;i++) {
//				for(int j=0;j<res.instance.numTasks;j++) {
//					machine=res.instance.machine(i,j);
//					if(res.nextToSet[machine]==0||s.startTime(res.resource[machine][res.nextToSet[machine]-1].job, res.resource[machine][res.nextToSet[machine]-1].task) <s.startTime(i, j)
//							&& s.startTime(best[machine].job, best[machine].task) > s.startTime(i, j)) {
//						best[machine] = new Task(i, j);
//					}
//				}
//				for(int i1=0;i1<res.instance.numMachines;i1++) {
//					res.resource[i1][res.nextToSet[i1]++]=best[i1];
//				}
//			}
//		}
//		return res;
//	}

    /** Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
    }

    //find index of the task with the given machine
    public int find(int machine, Task task) {
    	int index=0;
    	int found=0;
    	while(found==0) {
    		if(this.resource[machine][index].equals(task)) {
    			//System.out.println("FOUND, index ="+index);
    			found=1;
    			return index;
    		}else 
    			index++;
    	}
    	//System.out.println("NOT FOUND");
    	return -1; 
    }
    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for(int m=0; m < instance.numMachines; m++)
        {
            s.append("Machine ").append(m).append(" : ");
            for(int j=0; j<instance.numJobs; j++)
            {
                s.append(resource[m][j]).append(" ; ");
            }
            s.append("\n");
        }

        return s.toString();
    }
}
