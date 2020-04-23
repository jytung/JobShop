package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Arrays;

public class GreedySolver implements Solver {
	
	public enum Priority {
        SPT,LPT, SRPT,LRPT,EST_SPT, EST_LRPT
    }
	
	Priority prio;
	
	//constructor
	public GreedySolver(Priority prio) {
		this.prio=prio;
	}
	
	@Override
	public Result solve(Instance instance, long deadline) {
		
		// Initialization -- placer tous les premiers task de tous les jobs dans taskAvailable
		int[] taskAvailable = new int[instance.numJobs];
		Arrays.fill(taskAvailable,0);
		int remainingTask= instance.numJobs*instance.numTasks;
		ResourceOrder sol = new ResourceOrder(instance);
		Arrays.fill(sol.nextToSet,-1);
		
		//iteration
		switch(this.prio) {
		case SPT:
			//SPT (Shortest Processing Time) : donne priorité à la tâche la plus courte ;
			while(remainingTask>0) {
				//recherche de la duree minimal de tous les taskAvailable
				int min =100000;
				int job=-1;
				int task=-1;
				for (int j=0;j<instance.numJobs;j++) {
					if(taskAvailable[j]<instance.numTasks && instance.duration(j,taskAvailable[j])<min) {
						min = instance.duration(j, taskAvailable[j]);
						job=j;
						task=taskAvailable[j];
					}
				}
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
				
				//maj de tache realisable
				taskAvailable[job]++;
			}
			break;
		case LPT:
			// LPT (Longest Processing Time) : donne priorité à la tâche la plus longue 
			while(remainingTask>0) {
				//recherche de la duree maximal de tous les taskAvailable
				int max=-1;
				int job=-1;
				int task=-1;
				for (int j=0;j<instance.numJobs;j++) {
					if(taskAvailable[j]<instance.numTasks && instance.duration(j,taskAvailable[j])>max) {
						max = instance.duration(j, taskAvailable[j]);
						job=j;
						task=taskAvailable[j];
					}
				}
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
				
				//maj de tache realisable
				taskAvailable[job]++;
			}
			break;
		case LRPT:
			//LRPT (Longest Remaining Processing Time) : donne la priorité à la tâche appartenant au job ayant la plus grande durée
			int[] remainingTime= new int[instance.numJobs];
			Arrays.fill(remainingTime, 0);
			//initialisation
			for (int job=0;job<instance.numJobs;job++) {
				for(int task=0;task<instance.numTasks;task++) {
					remainingTime[job]+= instance.duration(job,task);
				}
			}
			/*
			System.out.println("------------remainingTime-----------");
			System.out.println(Arrays.toString(remainingTime));
			*/
			while(remainingTask>0) {
				//recherche de la duree restant maximal de tous les jobs
				int max=-1;
				int job=-1;
				int task=-1;
				for (int j=0;j<instance.numJobs;j++) {
					if(taskAvailable[j]<instance.numTasks && remainingTime[j]>max) {
						max = remainingTime[j];
						job=j;
						task=taskAvailable[j];
					}
				}
				//System.out.println("max: "+ max);
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
				/*
				System.out.println("------------taskAvailable-----------");
				System.out.println(Arrays.toString(taskAvailable));
				System.out.println("------------task choisi-----------");
				System.out.println(job+ " , "+ task);
				System.out.println("------------Resource-----------");
				System.out.println(sol.toString());
				*/
				//maj de tache realisable
				remainingTime[job]-=instance.duration(job,task);
				taskAvailable[job]++;
				/*
				System.out.println("------------remainingTime-----------");
				System.out.println(Arrays.toString(remainingTime));
				*/
			}
			break;
		case SRPT:
			// SRPT (Shortest Remaining Processing Time) : donne la priorité à la tâche appartenant au job ayant la plus petite durée restante
			int[] remainingTime1= new int[instance.numJobs];
			Arrays.fill(remainingTime1, 0);
			//initialisation
			for (int job=0;job<instance.numJobs;job++) {
				for(int task=0;task<instance.numTasks;task++) {
					remainingTime1[job]+= instance.duration(job,task);
				}
			}
			while(remainingTask>0) {
				//recherche de la duree restant minimal de tous les jobs
				int min=10000;
				int job=-1;
				int task=-1;
				for (int j=0;j<instance.numJobs;j++) {
					if(taskAvailable[j]<instance.numTasks && remainingTime1[j]<min) {
						min = remainingTime1[j];
						job=j;
						task=taskAvailable[j];
					}
				}
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
				
				//maj de tache realisable
				remainingTime1[job]-=instance.duration(job,task);
				taskAvailable[job]++;
			}
			break;
		case EST_SPT:
			//la restriction aux tâches pouvant commencer au plus tôt.
			
			// for each available task, its start time
			int nextFreeTimeJob [] = new int[instance.numJobs];
			// time at which each machine is going to be freed
	        int[] nextFreeTimeResource = new int[instance.numMachines];
//	        System.out.println(" ");
			while(remainingTask>0) {
//				System.out.println("------------nextFreeTimeResource-----------");
//				System.out.println(Arrays.toString(nextFreeTimeResource));
//				System.out.println("------------taskAvailable-----------");
//				System.out.println(Arrays.toString(taskAvailable));
//				System.out.println("------------nextFreeTimeJob-----------");
//				System.out.println(Arrays.toString(nextFreeTimeJob));
				//recherche de la duree minimal de tous les taskAvailable
				int min =100000;
				int job=-1;
				int task=-1;
				
				for (int j=0;j<instance.numJobs;j++) {
					//recherche le temps de debut le plus tot
					if(taskAvailable[j]<instance.numTasks && nextFreeTimeJob[j]<min) {
						min = nextFreeTimeJob[j];
						job=j;
						task=taskAvailable[j];
					}
					//si on a deux taches qui a le meme start time on choisit la duree la plus courte(spt)
					else if(taskAvailable[j]<instance.numTasks && nextFreeTimeJob[j]==min) {
						if(instance.duration(j,taskAvailable[j])<instance.duration(job,taskAvailable[job])) {
							min = nextFreeTimeJob[j];
							job=j;
							task=taskAvailable[j];
						}//si les durees sont egaux, on garde la tache qu'on a trouve en premier
					}
					
				}
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
//				System.out.println("------------task choisi-----------");
//				System.out.println(job+ " , "+ task);
//				System.out.println("------------Resource-----------");
//				System.out.println(sol.toString());
				
				//maj de tache realisable
				taskAvailable[job]++;
				nextFreeTimeResource[instance.machine(job,task)] +=  instance.duration(job, task) ;
			
				// earliest start time for this task
				int est = task == 0 ? 0 : nextFreeTimeJob[job] + instance.duration(job,task);
			    est = Math.max(est, nextFreeTimeResource[instance.machine(job,task)]);
				nextFreeTimeJob[job]=est;
			}
			break;
		case EST_LRPT:
			//la restriction aux tâches pouvant commencer au plus tôt.

			// time at which each machine is going to be freed
	        int[] nextFreeTimeResource1 = new int[instance.numMachines];
	        int[] nextFreeTimeJob1 = new int[instance.numJobs];
	        int[] remainingTime11= new int[instance.numJobs];
			Arrays.fill(remainingTime11, 0);
			//initialisation
			for (int job=0;job<instance.numJobs;job++) {
				for(int task=0;task<instance.numTasks;task++) {
					remainingTime11[job]+= instance.duration(job,task);
				}
			}
			while(remainingTask>0) {
				//recherche de la duree minimal de tous les taskAvailable
				int min =100000;
				int job=-1;
				int task=-1;
				
				for (int j=0;j<instance.numJobs;j++) {
					//recherche le temps de debut le plus tot
					if(taskAvailable[j]<instance.numTasks && nextFreeTimeJob1[j]<min) {
						min =nextFreeTimeJob1[j];
						job=j;
						task=taskAvailable[j];
					}
					//si on a deux taches qui a le meme start time on choisit le remaingTime le plus long(lrpt)
					else if(taskAvailable[j]<instance.numTasks && nextFreeTimeJob1[j]==min) {
						if(remainingTime11[j]>remainingTime11[job]) {
							min = nextFreeTimeJob1[j];
							job=j;
							task=taskAvailable[j];
						}//si les durees sont egaux, on garde la tache qu'on a trouve en premier
					}
					
				}
				//placer la tache le plus court dans la representation ResourceOrder
				Task best= new Task(job,task);
				sol.resource[instance.machine(best)][++sol.nextToSet[instance.machine(best)]]=best;
				remainingTask--;
				
				//maj de startTimes 
//				for (int j=0;j<instance.numJobs;j++) {
//					//si le job n'est pas encore termine
//					if(taskAvailable[j]<instance.numTasks){
//						int t = taskAvailable[j];
//						// earliest start time for this task
//						int est = t == 0 ? 0 : startTimes1[j][t-1] + instance.duration(j, t-1);
//					    est = Math.max(est, nextFreeTimeResource1[instance.machine(j,t)]);
//					    startTimes1[j][t] = est;
//					    //nextFreeTimeResource[instance.machine(j,t)] = est+ instance.duration(j, t) ;
//					    nextTask[j] = t + 1;
//					}
//				}
				//maj de tache realisable
				taskAvailable[job]++;
				nextFreeTimeResource1[instance.machine(job,task)] +=  instance.duration(job, task) ;
			
				// earliest start time for this task
				int est = task == 0 ? 0 : nextFreeTimeJob1[job] + instance.duration(job,task);
			    est = Math.max(est, nextFreeTimeResource1[instance.machine(job,task)]);
				nextFreeTimeJob1[job]=est;
			}
			break;	
		default:
			break;
		}
		Schedule best =sol.toSchedule();
		return new Result(instance, best, Result.ExitCause.Timeout);
	}


}
