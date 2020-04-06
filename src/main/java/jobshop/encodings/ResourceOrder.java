package jobshop.encodings;

import java.util.Arrays;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;


//représentation par ordre de passage sur ressources
public class ResourceOrder extends Encoding {	
	public Task[][] resource;
	public int[] nextToSet;
	
	public ResourceOrder(Instance instance) {
		super(instance);
		resource = new Task[instance.numMachines][instance.numJobs];
		nextToSet = new int[instance.numMachines];
    }

	@Override
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
	
	public ResourceOrder fromSchedule(Schedule s) {
		ResourceOrder res = new ResourceOrder(s.pb);
		int machine;
		Task best[] = new Task[res.instance.numMachines];
		while(res.nextToSet[0]< res.instance.numJobs) {
			Arrays.fill(best,null);
			for(int i=0;i<res.instance.numJobs;i++) {
				for(int j=0;j<res.instance.numTasks;j++) {
					machine=res.instance.machine(i,j);
					if(res.nextToSet[machine]==0) {
						best[machine] = new Task(i, j);
					}
				}
				for(int i1=0;i1<res.instance.numMachines;i1++) {
					res.resource[i1][res.nextToSet[i1]++]=best[i1];
				}
			}
		}
		return res;
	}
}
