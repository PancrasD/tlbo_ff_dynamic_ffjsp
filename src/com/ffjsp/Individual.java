package com.ffjsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Individual {

	// 个体染色体的维数
	static final int chromosomeLayer = 2;
	// 每个个体的目标函数个数
	static final int objNum = 1;
	// 个体中任务
	private List<ITask> taskslist = new ArrayList<ITask>(); 
	//
	private List<Machine> reslist = new ArrayList<Machine>(); 
	//染色体头部 初始条件
	private List<List<Integer>> chromosomeHead = new ArrayList<List<Integer>>();
	// 染色体
	private List<List<Integer>> chromosome = new ArrayList<List<Integer>>();
	// 染色体随机数
	private List<List<Double>> chromosomeDNA = new ArrayList<List<Double>>();	
	// 目标函数
	private List<Integer> obj=new ArrayList<>();
    //案例
	private Case project;
	//采用分配资源和安排机器上执行顺序一起的方法
	public Individual(Case project) {
		this.project = project;
		settaskslist(project);
		setResList(project);
		//随机产生DNA及任务序列、资源序列  随机可以更改 可以用其他启发式规则初始化
	    deciphering(project);
		//计算个体的目标函数值，输出计算了起停时间的任务对象list
		objCompute(project);

	}
	public Individual(List<List<Integer>> _chromosome,List<List<Integer>> chromosomeHead, Case project) {
		// 创建个体的染色体
		this.project = project;
		settaskslist(project);	
		setResList(project);
		this.chromosome = _chromosome;
		this.chromosomeHead=chromosomeHead;
		//计算个体的目标函数值，输出计算了起停时间的任务对象list
		objCompute(project);		
	}
	//
	public 	Individual(Individual individual) {
		this.project=individual.project;
		this.taskslist=individual.taskslist;
		this.reslist=individual.reslist;
		this.chromosome=individual.chromosome;
		this.chromosomeHead=individual.chromosomeHead;
		this.obj=individual.obj;
	}
	/*
	 * 十台机器 机器的完成时间初始化为0 
	 */
	private void setResList(Case project2) {
		// TODO Auto-generated method stub
		for (int i = 0; i < project.getResources().size();i++){
			Machine res = new Machine(project.getResources().get(i));
			reslist.add(res);
		}
	}
	/**
	 * 每个chromosome解密之后对应的目标函数值，用一个一维数组表示，数组长度等于目标函数的个数
	 * 
	 * @param chromosome
	 *            解密后的解
	 * @return 计算好起停时间的任务队列
	 */
	public void objCompute(Case project) {
		//计算头部
		List<Task> tasks = project.getTasks();
		List<Integer> maxtime = new ArrayList<>();
		List<List<Integer>> finishTimes=new ArrayList<>();
		List<List<Integer>> chromosomehead=this.getChromosomeHead();
		List<List<Integer>> chromosome=this.getChromosome();
		compute(chromosomehead);
		compute(chromosome);
		/*
		 * 引入左移调度 不改变染色体  可能改变完成时间
		 * machineTask key：machineID value:执行的tasklist
		 */
		//leftSchedule(tasks);//存在问题 不知道如何将改变映射到染色体中 不过可以返回机器上资源顺序及时间
        //当前个体最后的完成时间
		for(int i=0;i<reslist.size();i++) {
			finishTimes.add(reslist.get(i).getFinishTime());
		}
		maxtime=Tools.computeMaxTime(finishTimes);
		this.obj=maxtime;
	}
   
	public void compute(List<List<Integer>> chromosome) {
		// TODO Auto-generated method stub
		List<Task> tasks = this.getProject().getTasks();
		for (int i = 0; i < chromosome.get(0).size(); i++){
			Task curtask = tasks.get(chromosome.get(0).get(i)-1);
			//得到所有前置任务,循环每一前置任务，取最晚结束时间
			List<Integer> preEndtime=Tools.computeMaxPreEndTime(curtask,taskslist);
			//当前任务所对应的资源最晚时间
			List<Integer> resFinish=reslist.get(chromosome.get(1).get(i)-1).getFinishTime();
			//计算开始时间
			List<Integer> startTime=Tools.computeStartTime(resFinish,preEndtime);
			//设置当前任务的开始时间及完成时间
			taskslist.get(chromosome.get(0).get(i)-1).setstarttime(startTime);
			List<Integer> processTime=tasks.get(chromosome.get(0).get(i)-1).getProcessTime().get(chromosome.get(1).get(i)-1);
			List<Integer> completedTime=Tools.computeCompletedTime(startTime, processTime);
			taskslist.get(chromosome.get(0).get(i)-1).setFinishTime(completedTime);
			//更新当前任务资源的最后完工时间
			reslist.get(chromosome.get(1).get(i)-1).setFinishTime(completedTime);
			
		}
	}
	private void leftSchedule(List<Task> tasks) {
		// TODO Auto-generated method stub
		Map<Integer,List<Integer>> machineTask=new TreeMap<>();
	    //遍历机器ID 调整上面的任务序列
		for(int j=0;j<reslist.size();j++) {
			List<Integer> machineTasklist=machineTask.get(reslist.get(j).getMachineID());
			if(machineTasklist==null) {
				machineTasklist=new ArrayList<>();
				machineTasklist.add(taskslist.get(j).getTaskid());
			}
			else {
				machineTasklist.add(taskslist.get(j).getTaskid());
			}
			machineTask.put(reslist.get(j).getMachineID(),machineTasklist);
		}
		//遍历机器找寻空闲间隔
		for(int n=0;n<reslist.size();n++) {
		   List<Integer> machineTasklist=machineTask.get(n+1);//机器ID tasklist:taskID
		   for(int m=0;m<machineTasklist.size()-2;m++) {
			   List<Integer> finishTime=taskslist.get(machineTasklist.get(m)-1).getFinishTime();
			   for(int h=m+1;h<machineTasklist.size()-1;h++) {
				   List<Integer> startTime=taskslist.get(machineTasklist.get(h)-1).getStartTime();
				   //前两个循环找空闲时间间隔 下一个循环找寻task  1 2 3 3的执行时间小于1和2的空白间隔时间 (即1的完成时间+3的执行时间<2的开始时间)
				   //然后普则检查3的紧前关系是否满足 如果3的紧前任务的最大完成时间+3的执行时间<2的开始时间则 3是可以左移的 
				   //可以左移的情况下 3的开始时间=3的紧前任务的最大完成时间和1的结束时间中的最大者
				   //3的完成时间=3的开始时间+3的执行时间 
				   //左移之后对同一机器上的任务的执行时间进行更正
				   for(int k=h+1;k<machineTasklist.size();k++) {
					   int testTaskID=machineTasklist.get(k);
					   List<Integer> processTime=tasks.get(machineTasklist.get(k)-1).getProcessTime().get(n+1);
					   List<Integer> tempfinishTime1=Tools.computeCompletedTime(finishTime, processTime);
					   if(Tools.compareTime(tempfinishTime1, startTime)>0) {
						   //检查紧前关系时间是否满足 //计算紧前任务的最大完成时间
						   List<Integer> preMaxEndTime=Tools.computeMaxPreEndTime(tasks.get(machineTasklist.get(k)-1), taskslist);
						   List<Integer> tempfinishTime2=Tools.computeCompletedTime(preMaxEndTime, processTime);
						   if(Tools.compareTime(tempfinishTime2, startTime)>0) {
							   //紧前关系满足
							   //更新机器上的执行序列 更新task的执行时间   及机器上的执行时间
							   machineTasklist.remove(k);
							   machineTasklist.add(h, testTaskID);
							   machineTask.put(n+1,machineTasklist);
							   List<Integer> newstartTime=Tools.computeStartTime(preMaxEndTime, finishTime);
							   taskslist.get(machineTasklist.get(h)-1).setStartTime(newstartTime);
							   List<Integer> newfinishTime=Tools.computeCompletedTime(newstartTime, processTime);
							   taskslist.get(machineTasklist.get(h)-1).setFinishTime(newfinishTime);
							   //更新机器上的各任务执行时间
							   List<Integer> initialTime=new ArrayList<>();
							   for(int i=0;i<3;i++) {
								   initialTime.add(0);
							   }
							   reslist.get(n).setFinishTime(initialTime);
							   for(int i=0;i< machineTasklist.size();i++) {
								   //更新每一个任务的执行时间
								  List<Integer> resFinishTime=reslist.get(n).getFinishTime();
								  List<Integer>  preMaxTime=Tools.computeMaxPreEndTime(tasks.get(machineTasklist.get(i)-1), taskslist);
								  List<Integer> startTime_1=Tools.computeStartTime(resFinishTime,preMaxTime);
								  taskslist.get(machineTasklist.get(i)-1).setStartTime(startTime_1);
								  List<Integer> finishTime_1=Tools.computeCompletedTime(startTime_1, processTime);
								  taskslist.get(machineTasklist.get(i)-1).setFinishTime(finishTime_1);
								  reslist.get(n).setFinishTime(finishTime_1);
							   }
						   }
						   
					   }
				   }
			   }
		   }
		}
	}

	private void settaskslist(Case project){
		for (int i = 0; i < project.getTasks().size();i++){
			ITask itask = new ITask(project.getTasks().get(i));
			taskslist.add(itask);
		}
	}
	/*
    * deciphering()是一次性将可执行的任务安排完  然后更新可执行表 然后再选择可执行的任务
    * decipheringCapableOneTask()是一次只选择一个task安排 然后更新可执行表
    */
	private void decipheringCapableOneTask(Case project2) {
		// TODO Auto-generated method stub
		List<Integer> taskList = new ArrayList<Integer>();
		List<Integer> resourceList = new ArrayList<Integer>();
		// 可执行任务集合
		List<Integer> executableTaskIDS = new ArrayList<Integer>();	
		List<Task> tasks = project.getTasks();
		List<Double> _list1 = new ArrayList<>();
		List<Double> _list2 = new ArrayList<>();
		// 求taskList任务执行序列和resourceList资源分配序列
		for (int i = 0; i < project.getNtask(); i++) {  
			
			executableTaskIDS.clear();
			double rand1 = Math.random();
			double rand2 = Math.random();
			_list1.add(rand1);
			_list2.add(rand2);
			for (int k = 0; k < tasks.size(); k++) {
				if (taskslist.get(k).pretasknum == 0){
					executableTaskIDS.add(tasks.get(k).getTaskID());
				}
			}
			if (executableTaskIDS.size() == 0){
				break;
			}
			//循环安排当前能执行的任务  可换成两种规则
				int A = (int) ( rand1 * executableTaskIDS.size());
				int currentTaskID = executableTaskIDS.get(A);
				taskList.add(currentTaskID);
				taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
				//处理后续任务
				for (int k = 0; k < tasks.size(); k++) {
					//把所有以任务j为前置任务的前置任务数减1；
					if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
						taskslist.get(k).pretasknum--;	
					}
				}
				// 求对应的资源分配序列resourceList
				// 可执行该任务的资源集合
				List<Integer> list = tasks.get(currentTaskID -1).getResourceIDs();
				int B = (int) (rand2 * list.size());
				int resourceid = list.get(B);
				resourceList.add(resourceid );
		}
		this.chromosomeDNA.add(_list1);
		this.chromosomeDNA.add(_list2);
		this.chromosome.add(taskList);
		this.chromosome.add(resourceList);
		return ;
	}
	/**
	 * 将随机初始化解，解密成整数向量表示任务序列、资源序列的染色体结构
	 * 
	 * @param _chromosome
	 *            随机数组成的二维数组
	 * @return 返回由任务执行序列和资源分配序列组成的集合
	 */
	public void deciphering(Case project) {
		List<Task> tasks = project.getTasks();
		//生成染色体头部
		List<Integer> firstExcuteTask=project.getFirstExcuteTask();
		List<Integer> firstExcuteTaskMachine=project.getFirstExcuteTaskMachine();
		List<List<Integer>> chromosomeHead=new ArrayList<>();
		chromosomeHead.add(firstExcuteTask);
		chromosomeHead.add(firstExcuteTaskMachine);
		this.setChromosomeHead(chromosomeHead);
		//将头部工作集标记为已调度  将头部工作的紧后工作集的紧前数1、
		for(int m=0;m<firstExcuteTask.size();m++) {
			int currentTaskID = firstExcuteTask.get(m);
			taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
			//处理后续任务
			for (int k = 0; k < tasks.size(); k++) {
				//把所有以任务j为前置任务的前置任务数减1；
				if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
					taskslist.get(k).pretasknum--;	
				}
			}
		}
		List<Integer> taskList = new ArrayList<Integer>();
		List<Integer> resourceList = new ArrayList<Integer>();
		// 可执行任务集合
		List<Integer> executableTaskIDS = new ArrayList<Integer>();	
		List<Double> _list1 = new ArrayList<>();
		List<Double> _list2 = new ArrayList<>();
		// 求taskList任务执行序列和resourceList资源分配序列
		for (int i = 0; i < project.getNtask(); i++) {  
			executableTaskIDS.clear();
			double rand1 = Math.random();
			double rand2 = Math.random();
			_list1.add(rand1);
			_list2.add(rand2);
			for (int k = 0; k < tasks.size(); k++) {
				if (taskslist.get(k).pretasknum == 0){
					executableTaskIDS.add(tasks.get(k).getTaskID());
				}
			}
			if (executableTaskIDS.size() == 0){
				break;
			}
			//循环安排当前能执行的任务  可换成两种规则
			//1随机  2最大紧后集   34先选择资源然后选择任务3最大执行时间 4 最大紧后集执行时间和
			double rand3=Math.random();
			if(rand3<0.3) {
			   scheduleTaskByRandomRule(rand1,rand2,executableTaskIDS,taskList,resourceList);
			}else if(rand3<0.5) {
				scheduleTaskByMaxSuccessorsRule(rand2,executableTaskIDS,taskList,resourceList);
			}else if(rand3>0.7) {
				scheduleTaskByMaxProcessTimeRule(executableTaskIDS,taskList,resourceList);
			}else {
				scheduleTaskByMaxSumSuccessorsProcessTimeRule(executableTaskIDS,taskList,resourceList);
			}
		
		}
		this.chromosomeDNA.add(_list1);
		this.chromosomeDNA.add(_list2);
		this.chromosome.add(taskList);
		this.chromosome.add(resourceList);
		return ;
	}
	//随机规则选择任务
	private void scheduleTaskByRandomRule(double rand1, double rand2, List<Integer> executableTaskIDS, List<Integer> taskList,
			List<Integer> resourceList) {
		List<Task> tasks=this.getProject().getTasks();
		while(executableTaskIDS.size()>0) {
			int A = (int) ( rand1 * executableTaskIDS.size());
			int currentTaskID = executableTaskIDS.get(A);
			executableTaskIDS.remove(A);
			taskList.add(currentTaskID);
			taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
			//处理后续任务
			for (int k = 0; k < tasks.size(); k++) {
				//把所有以任务j为前置任务的前置任务数减1；
				if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
					taskslist.get(k).pretasknum--;	
				}
			}
			//Task curtask = tasks.get(currentTaskID-1);
			// 求对应的资源分配序列resourceList
			// 可执行该任务的资源集合
			Random rand=new Random();
			double prob=rand.nextDouble();
			//到底是整个初始化采用统一的规则 还是多种规则组合使用
			if(prob<=0.5) {
				// 采取选择最小执行时间的资源
				selectResWithMinProcessTimeRule(currentTaskID,resourceList);
			}
			else if(prob<0.6){
				//随机选取可执行的资源
				selectResWithRandomRule(rand2,currentTaskID,resourceList);
			}
			else{
				//选取当前任务的完成时间最小的机器  需要设置任务的开始时间 机器的完成时间
				//1选择出最小完成任务时间的资源
				//传入参数reslist获取结束时间   任务ID获取可行资源的执行时间
				selectResWithFirstFinish(currentTaskID,resourceList);
				
			}
			
	   }
		
	}
	
	//最多紧后集规则调度任务
	private void scheduleTaskByMaxSuccessorsRule(double rand2, List<Integer> executableTaskIDS, List<Integer> taskList,
			List<Integer> resourceList) {
		List<Task> tasks=this.getProject().getTasks();
		//按降序排列
		Collections.sort(executableTaskIDS, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				int flag=1;
				if(tasks.get(o1-1).getSuccessorTaskIDS().size()<tasks.get(o2-1).getSuccessorTaskIDS().size()) {
					flag=1;
				}else if(tasks.get(o1-1).getSuccessorTaskIDS().size()>tasks.get(o2-1).getSuccessorTaskIDS().size()){
					flag=-1;
				}else {
					flag=0;
				}
				return flag;
			}
			
		});
		//
		while(executableTaskIDS.size()>0) {
			int A =0;
			int currentTaskID = executableTaskIDS.get(A);
			executableTaskIDS.remove(A);
			taskList.add(currentTaskID);
			taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
			//处理后续任务
			for (int k = 0; k < tasks.size(); k++) {
				//把所有以任务j为前置任务的前置任务数减1；
				if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
					taskslist.get(k).pretasknum--;	
				}
			}
			//Task curtask = tasks.get(currentTaskID-1);
			// 求对应的资源分配序列resourceList
			// 可执行该任务的资源集合
			Random rand=new Random();
			double prob=rand.nextDouble();
			//到底是整个初始化采用统一的规则 还是多种规则组合使用
			if(prob<=0.5) {
				// 采取选择最小执行时间的资源
				selectResWithMinProcessTimeRule(currentTaskID,resourceList);
			}
			else if(prob<0.6){
				//随机选取可执行的资源
				selectResWithRandomRule(rand2,currentTaskID,resourceList);
			}
			else{
				//选取当前任务的完成时间最小的机器  需要设置任务的开始时间 机器的完成时间
				//1选择出最小完成任务时间的资源
				//传入参数reslist获取结束时间   任务ID获取可行资源的执行时间
				selectResWithFirstFinish(currentTaskID,resourceList);
				
			}
			
	   }
	}
	//最大执行时间规则调度任务 先选择资源   然后优先规则调度任务
	private void scheduleTaskByMaxProcessTimeRule(List<Integer> executableTaskIDS, List<Integer> taskList,
			 List<Integer> resourceList) {
		//资源选择 最早结束的资源  随机资源
		List<Task> tasks=this.getProject().getTasks();
		while(executableTaskIDS.size()>0) {
			double rand=Math.random();
			int resId=0;
			if(rand<0.5) {
				int minResIndex=0;
				for(int i=1;i<this.getReslist().size();i++) {
					List<Integer> time1=this.getReslist().get(minResIndex).getFinishTime();
					List<Integer> time2=this.getReslist().get(i).getFinishTime();
					int flag=Tools.compareTime(time1, time2);//tim1>time2 -1 time1<time2 1
					if(flag==-1) {
						minResIndex=i;
					}
				}
				resId=minResIndex+1;
			}
			else {
				resId=(int)Math.random()*this.getReslist().size()+1;
				
			}
		//任务选择 最大执行时间规则
		
		int taskIndex=0;
		for(int k=1;k<executableTaskIDS.size();k++) {
			int taskid1=executableTaskIDS.get(taskIndex);
			int taskid2=executableTaskIDS.get(k);
			List<Integer> time1=tasks.get(taskid1-1).getProcessTime().get(resId-1);
			List<Integer> time2=tasks.get(taskid2-1).getProcessTime().get(resId-1);
			int flag=Tools.compareTime(time1, time2);//tim1>time2 -1 time1<time2 1
			if(flag==1) {
				taskIndex=k;
			}
		}
		int currentTaskID=executableTaskIDS.get(taskIndex);
		Task curtask=tasks.get(currentTaskID-1);
		executableTaskIDS.remove(taskIndex);
		taskList.add(currentTaskID);
		taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
		//处理后续任务
		for (int j = 0; j <tasks.size(); j++) {
			//把所有以任务j为前置任务的前置任务数减1；
			if (tasks.get(j).getPresecessorIDs().contains(currentTaskID)){
				taskslist.get(j).pretasknum--;	
			}
		}
		int resourceid=resId;
		resourceList.add(resourceid );
		List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
	    taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
		reslist.get(resourceid-1).setFinishTime(endEndTime);
		
		}
	}
	//最大紧后执行时间和优先规则调度任务
	private void scheduleTaskByMaxSumSuccessorsProcessTimeRule(List<Integer> executableTaskIDS,
			List<Integer> taskList,  List<Integer> resourceList) {
		//资源选择 最早结束的资源  随机资源
		List<Task> tasks=this.getProject().getTasks();
		while(executableTaskIDS.size()>0) {
			double rand=Math.random();
			int resId=0;
			if(rand<0.5) {
				int minResIndex=0;
				for(int i=1;i<this.getReslist().size();i++) {
					List<Integer> time1=this.getReslist().get(minResIndex).getFinishTime();
					List<Integer> time2=this.getReslist().get(i).getFinishTime();
					int flag=Tools.compareTime(time1, time2);//tim1>time2 -1 time1<time2 1
					if(flag==-1) {
						minResIndex=i;
					}
				}
				resId=minResIndex+1;
			}
			else {
				resId=(int)Math.random()*this.getReslist().size()+1;
				
			}
		//任务选择 最大及紧后执行时间和规则
		int taskIndex=0;
		int taskid1=executableTaskIDS.get(taskIndex);
		List<Integer> MaxSumTimeCurrentAndSucs=computeSumCurrAndSucs(taskid1,resId);
		for(int k=1;k<executableTaskIDS.size();k++) {
			int taskid2=executableTaskIDS.get(k);
			List<Integer> SumTimeCurrentAndSucs1=computeSumCurrAndSucs(taskid2,resId);
			int flag=Tools.compareTime(MaxSumTimeCurrentAndSucs, SumTimeCurrentAndSucs1);//tim1>time2 -1 time1<time2 1
			if(flag==1) {
				taskIndex=k;
				MaxSumTimeCurrentAndSucs=SumTimeCurrentAndSucs1;
			}
		}
		int currentTaskID=executableTaskIDS.get(taskIndex);
		Task curtask=tasks.get(currentTaskID-1);
		executableTaskIDS.remove(taskIndex);
		taskList.add(currentTaskID);
		taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
		//处理后续任务
		for (int j = 0; j <tasks.size(); j++) {
			//把所有以任务j为前置任务的前置任务数减1；
			if (tasks.get(j).getPresecessorIDs().contains(currentTaskID)){
				taskslist.get(j).pretasknum--;	
			}
		}
		int resourceid=resId;
		resourceList.add(resourceid );
		List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
	    taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
		reslist.get(resourceid-1).setFinishTime(endEndTime); 
		}
	}
	
	
	
	private List<Integer> computeSumCurrAndSucs(int taskid, int resId) {
		List<Task>tasks=this.getProject().getTasks();
		List<Integer> suc=tasks.get(taskid-1).getSuccessorTaskIDS();
		List<Integer> sum=new ArrayList<>(tasks.get(taskid-1).getProcessTime().get(resId-1));
		for(int i=0;i<suc.size();i++) {
			List<Integer> sucTime=tasks.get(suc.get(i)-1).getProcessTime().get(resId-1);
			sum=Tools.addFuzzyTime(sucTime, sum);
		}
		
		return sum;
	}
	//根据最小执行时间选择资源
	private void selectResWithMinProcessTimeRule( int currentTaskID, List<Integer> resourceList) {
		List<Task> tasks =this.getProject().getTasks();
		Task curtask = tasks.get(currentTaskID-1);
		int resourceid=tasks.get(currentTaskID -1).getMinProcessTimeResource();
		resourceList.add(resourceid );
		//移除currentTaskID id
		List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
		taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
		reslist.get(resourceid-1).setFinishTime(endEndTime);
		
	}
	//随机规则选择资源
	private void selectResWithRandomRule(double rand2, int currentTaskID, List<Integer> resourceList) {
		List<Task> tasks =this.getProject().getTasks();
		Task curtask = tasks.get(currentTaskID-1);
		List<Integer> list = tasks.get(currentTaskID -1).getResourceIDs();
		int B = (int) (rand2 * list.size());
		int resourceid = list.get(B);
		resourceList.add(resourceid );
		//移除currentTaskID id
		List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
		taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
		reslist.get(resourceid-1).setFinishTime(endEndTime);
		
	}
	//选择最早完成任务的资源
	private void selectResWithFirstFinish(int currentTaskID, List<Integer> resourceList) {
		List<Task> tasks =this.getProject().getTasks();
		Task curtask = tasks.get(currentTaskID-1);
		int resourceid=Tools.minCompletedTimeResource(reslist,curtask);
		resourceList.add(resourceid );
		//2 计算当前任务的开始执行时间 紧前集的最后完成时间 安排资源的结束时间
		//3 更新任务的结束时间  资源的结束时间
		List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
	    taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
		reslist.get(resourceid-1).setFinishTime(endEndTime);
		
	}

	
	private void decipheringFirstProcess(Case project2) {
		// TODO Auto-generated method stub
		List<Integer> taskList = new ArrayList<Integer>();
		List<Integer> resourceList = new ArrayList<Integer>();
		// 可执行任务集合
		List<Integer> executableTaskIDS = new ArrayList<Integer>();	
		List<Task> tasks = project.getTasks();
        
		List<Double> _list1 = new ArrayList<>();
		List<Double> _list2 = new ArrayList<>();

		// 具体解密细节，需要补充

		// 求taskList任务执行序列和resourceList资源分配序列
		for (int i = 0; i < project.getNtask(); i++) {  
			
			executableTaskIDS.clear();
			double rand1 = Math.random();
			double rand2 = Math.random();
			_list1.add(rand1);
			_list2.add(rand2);
			
			for (int k = 0; k < tasks.size(); k++) {
				if (taskslist.get(k).pretasknum == 0){
					executableTaskIDS.add(tasks.get(k).getTaskID());
				}
			}
			if (executableTaskIDS.size() == 0){
				break;
			}
			//循环安排当前能执行的任务  可换成两种规则
			for(int m=0;m<executableTaskIDS.size();m++) {
				int A = (int) ( rand1 * executableTaskIDS.size());
				int currentTaskID = executableTaskIDS.get(A);
				taskList.add(currentTaskID);
				taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
				
				//处理后续任务
				for (int k = 0; k < tasks.size(); k++) {
					//把所有以任务j为前置任务的前置任务数减1；
					if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
						taskslist.get(k).pretasknum--;	
					}
				}
				Task curtask = tasks.get(currentTaskID-1);
				// 求对应的资源分配序列resourceList
				// 可执行该任务的资源集合
				Random rand=new Random();
				double prob=rand.nextDouble();
				//到底是整个初始化采用统一的规则 还是多种规则组合使用
				
				/*
				 * 选取当前任务的完成时间最小的机器  需要设置任务的开始时间 机器的完成时间
				 */
				executableTaskIDS.remove(A);
				//1选择出最小完成任务时间的资源
				//传入参数reslist获取结束时间   任务ID获取可行资源的执行时间
				int resourceid=Tools.minCompletedTimeResource(reslist,curtask);
				resourceList.add(resourceid );
				//2 计算当前任务的开始执行时间 紧前集的最后完成时间 安排资源的结束时间
				//3 更新任务的结束时间  资源的结束时间
				List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
			    taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
				reslist.get(resourceid-1).setFinishTime(endEndTime);
		   }
		
		}
		this.chromosomeDNA.add(_list1);
		this.chromosomeDNA.add(_list2);
		this.chromosome.add(taskList);
		this.chromosome.add(resourceList);
		return ;
	}

	private void decipheringMinProcess(Case project2) {
		// TODO Auto-generated method stub
		List<Integer> taskList = new ArrayList<Integer>();
		List<Integer> resourceList = new ArrayList<Integer>();
		// 可执行任务集合
		List<Integer> executableTaskIDS = new ArrayList<Integer>();	
		List<Task> tasks = project.getTasks();
		List<Double> _list1 = new ArrayList<>();
		List<Double> _list2 = new ArrayList<>();
		// 求taskList任务执行序列和resourceList资源分配序列
		for (int i = 0; i < project.getNtask(); i++) {  
			
			executableTaskIDS.clear();
			double rand1 = Math.random();
			double rand2 = Math.random();
			_list1.add(rand1);
			_list2.add(rand2);
			for (int k = 0; k < tasks.size(); k++) {
				if (taskslist.get(k).pretasknum == 0){
					executableTaskIDS.add(tasks.get(k).getTaskID());
				}
			}
			if (executableTaskIDS.size() == 0){
				break;
			}
			//循环安排当前能执行的任务  可换成两种规则
			for(int m=0;m<executableTaskIDS.size();m++) {
				int A = (int) ( rand1 * executableTaskIDS.size());
				int currentTaskID = executableTaskIDS.get(A);
				taskList.add(currentTaskID);
				taskslist.get(currentTaskID -1).pretasknum = -1;   //当前任务已经被使用，做上标记以防止下次被选用
				//处理后续任务
				for (int k = 0; k < tasks.size(); k++) {
					//把所有以任务j为前置任务的前置任务数减1；
					if (tasks.get(k).getPresecessorIDs().contains(currentTaskID)){
						taskslist.get(k).pretasknum--;	
					}
				}
				Task curtask = tasks.get(currentTaskID-1);
				// 求对应的资源分配序列resourceList
				// 可执行该任务的资源集合
				Random rand=new Random();
				double prob=rand.nextDouble();
				//到底是整个初始化采用统一的规则 还是多种规则组合使用
				/*
				 * 采取选择最小执行时间的资源
				 */
				executableTaskIDS.remove(A);
				int resourceid=tasks.get(currentTaskID -1).getMinProcessTimeResource();
				resourceList.add(resourceid );
				//移除currentTaskID id
				List<Integer> endEndTime=Tools.computeCompletedTimeSimple(taskslist,reslist,curtask,resourceid);
				taskslist.get(currentTaskID-1).setFinishTime(endEndTime);
				reslist.get(resourceid-1).setFinishTime(endEndTime);
			}
		
		}
		this.chromosomeDNA.add(_list1);
		this.chromosomeDNA.add(_list2);
		this.chromosome.add(taskList);
		this.chromosome.add(resourceList);
		return ;
	}
	public List<ITask> getTaskslist() {
		return taskslist;
	}
	public void setTaskslist(List<ITask> taskslist) {
		this.taskslist = taskslist;
	}
	public List<Machine> getReslist() {
		return reslist;
	}
	public void setReslist(List<Machine> reslist) {
		this.reslist = reslist;
	}
	public List<List<Integer>> getChromosome() {
		return chromosome;
	}
	public void setChromosome(List<List<Integer>> chromosome) {
		this.chromosome = chromosome;
	}
	public List<Integer> getObj() {
		return obj;
	}
	public void setObj(List<Integer> obj) {
		this.obj = obj;
	}
	public Case getProject() {
		return project;
	}
	public void setProject(Case project) {
		this.project = project;
	}
	public List<List<Integer>> getChromosomeHead() {
		return chromosomeHead;
	}
	public void setChromosomeHead(List<List<Integer>> chromosomeHead) {
		this.chromosomeHead = chromosomeHead;
	}
	
}
