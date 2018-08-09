package com.ffjsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public  class Tools {
    /*
     * @param startTime 任务的开始执行时间
     * @param processTime 任务的执行时间
     * @return completedTime任务的完成时间
     */
	public static List<Integer> computeCompletedTime(List<Integer>startTime,List<Integer> processTime){
		List<Integer> completedTime=new ArrayList<>();
		for(int i=0;i<startTime.size();i++) {
			completedTime.add(startTime.get(i)+processTime.get(i));
			//System.out.println("completedTime "+completedTime);
		}
		return completedTime;
	}
	public static List<Integer> addFuzzyTime(List<Integer>time1,List<Integer> time2){
		if(time1.size()!=time2.size()) {
			System.out.println("模糊数出错");
		}
		List<Integer> addfuzzyTime=new ArrayList<>();
		for(int i=0;i<time1.size();i++) {
			addfuzzyTime.add(time1.get(i)+time2.get(i));
			//System.out.println("completedTime "+completedTime);
		}
		return addfuzzyTime;
	}
	/*
	 * @param finishTime1 分配的机器的上一个任务的完成时间 x1,x2,x3
	 * @param finishTime2 紧前任务的最大完成时间 y1,y2,y3
	 * @return startTime 当前任务的开始执行时间
	 */
	public static List<Integer> computeStartTime(List<Integer>finishTime1,List<Integer> finishTime2){
		List<Integer> startTime=new ArrayList<>();
	
		//x1>=y3 return x
		if(finishTime1.get(0)>=finishTime2.get(2)) {
			startTime=finishTime1;
		}
		//y1>=x3 return y
		else if(finishTime2.get(0)>=finishTime1.get(2)) {
			startTime=finishTime2;
		}
		//y1<=x1 x3<=y3
		else if(finishTime2.get(0)<=finishTime1.get(0)&&finishTime2.get(2)>=finishTime1.get(2)) {
			startTime.add(finishTime1.get(0));
			startTime.add(finishTime1.get(1));
			startTime.add(finishTime2.get(2));
		}
		//x1<=y1 y3<=x3
		else if(finishTime2.get(0)>=finishTime1.get(0)&&finishTime2.get(2)<=finishTime1.get(2)) {
			startTime.add(finishTime2.get(0));
			startTime.add(finishTime2.get(1));
			startTime.add(finishTime1.get(2));
		}
		//x1<=y1 y3>=x3 y1<=x3
		else if(finishTime2.get(0)>=finishTime1.get(0)&&finishTime2.get(2)>=finishTime1.get(2)&&finishTime2.get(0)<=finishTime1.get(2)) {
			startTime=finishTime2;
		}
		//y1<=x1 x3>=y3 x1<=y3
		else if(finishTime1.get(0)>=finishTime2.get(0)&&finishTime1.get(2)>=finishTime2.get(2)&&finishTime1.get(0)<=finishTime2.get(2)) {
			startTime=finishTime1;
		}
//		int flag=compareTime(finishTime1,finishTime2);
//		if(flag==-1) {
//			startTime=finishTime1;
//		}
//		else {
//			startTime=finishTime2;
//		}
		return startTime;
	}
	/*
	 * 计算最大完成时间 三种计算方法依次比对
	 * @param finishTimes所有机器执行完最后一个任务的完成时间
	 * @return maxTime 即最大的完成时间 
	 */
	public static List<Integer> computeMaxTime(List<List<Integer>>finishTimes){
		List<Integer> maxTime=new ArrayList<Integer>();
		//降序排列
		Collections.sort(finishTimes,new Comparator<List<Integer>>() {
			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				// TODO Auto-generated method stub
				int flag=compareTime(o1,o2);
				return flag;
			}
			
		});
		maxTime=finishTimes.get(0);
		return maxTime;
	}
    //比较两个模糊执行时间的大小 tim1>time2 -1 time1<time2 1
	public static int compareTime(List<Integer> time1, List<Integer> time2) {
		// TODO Auto-generated method stub
		int flag=0;
		if(((time1.get(0)+time1.get(1)+time1.get(2))/4.0)>((time2.get(0)+time2.get(1)+time2.get(2))/4.0)) {
			flag=-1;
		}
		else if(((time1.get(0)+time1.get(1)+time1.get(2))/4.0)<((time2.get(0)+time2.get(1)+time2.get(2))/4.0)) {
			flag=1;
		}
		else {
			if(time1.get(1)>time2.get(2)) {
				flag=-1;
			}
			else if(time1.get(1)<time2.get(2)) {
				flag=1;
			}
			else {
				if((time1.get(2)-time1.get(0))>time2.get(2)-time2.get(0)) {
					flag=-1;
				}
				else if((time1.get(2)-time1.get(0))<time2.get(2)-time2.get(0)) {
						flag=1;
					
				}
			}
		}
		return flag;
	}
	/*
	 * 判断两个个体的schedule是否相等
	 * @return boolean 相等-true 不相等false
	 */
    public static boolean  judgeSameIndividual(List<List<Integer>> schedule,List<List<Integer>>lastSchedule) {
    
		//先判断task list 再判断resource list
		if(judgeListEqual(schedule.get(0),lastSchedule.get(0))){
			if(judgeListEqual(schedule.get(1),lastSchedule.get(1))){
				return true;
			}
		}
		return false;
    	
    }
    /*
     * 判断模糊时间是否相等
     * @return boolean 相等-true 不相等-false
     */
    public static boolean judgeListEqual(List<Integer> list1,List<Integer> list2) {
    	if(list1.size()!=list2.size()) {
    		System.out.println("the two list size not equal");
    		return false;
    	}
    	else {
    		for(int i=0;i<list1.size();i++) {
    			if(!list1.get(i).equals(list2.get(i))) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
	/*
	 * 根据目标值优劣选择出种群
	 * @ return 目标值最优的个体组
	 */
	public static List<Individual> popSort(Population population,Case project){
		Individual[] individuals = population.getPopulation();//thefirst 排序出错
		List<Individual> indivList=Arrays.asList(individuals);
		Collections.sort(indivList,new Comparator<Individual>() {
            
			@Override
			public int compare(Individual indiv1,Individual indiv2) {
				// TODO Auto-generated method stub
				//需要升序排列
				int flag=0;
				List<Integer> o1=indiv1.getObj();
				List<Integer> o2=indiv2.getObj();
				if(((o1.get(0)+o1.get(1)+o1.get(2))/4.0)>((o2.get(0)+o2.get(1)+o2.get(2))/4.0)) {
					flag= 1;
				}
				else if(((o1.get(0)+o1.get(1)+o1.get(2))/4.0)<((o2.get(0)+o2.get(1)+o2.get(2))/4.0)) {
					flag=-1;
				}
				else {
					if(o1.get(1)>o2.get(1)) {
						flag=1;
					}
					else if(o1.get(1)<o2.get(1)) {
						flag=-1;
					}
					else {
						if((o1.get(2)-o1.get(0))>(o2.get(2)-o2.get(0))) {
							flag=1;
						}
						else if((o1.get(2)-o1.get(0))<(o2.get(2)-o2.get(0))) {
								flag=-1;
						}
						
						}
					}
				
				return flag;
			}
			
		});
		return indivList;
	}
	public static List<Individual> popSortPrint(Population population,Case project){
		Individual[] individuals = population.getPopulation();//thefirst 排序出错
		List<Individual> indivList=Arrays.asList(individuals);
		Collections.sort(indivList,new Comparator<Individual>() {
            
			@Override
			public int compare(Individual indiv1, Individual indiv2) {
				// TODO Auto-generated method stub
				//需要升序排列
				int flag=0;
				List<Integer> o1=indiv1.getObj();
				List<Integer> o2=indiv2.getObj();
				
				if(o1.get(0)>o2.get(0)) {
					flag=1;
				}
				if(o1.get(0)<o2.get(0)) {
					flag=-1;
				}
				return flag;
			}
			
		});
		return indivList;
	}
	/**
	 * 选择种群中个体指定目标函数的最大值
	 * 
	 * @param index_objList
	 * @param m
	 * @return
	 */
	public static void printsolutions(Population solutions,long startTime) {
		if (solutions.getPopulationsize()>0){
//			List<Individual> listindiv=popSortPrint(solutions,solutions.getProject());
//			Individual[] bestIndividuals =(Individual[]) listindiv.toArray();
			Individual[] bestIndividuals = solutions.getPopulation();
			// 存储个体的目标函数
			List<List<Integer>> betterObjs = new ArrayList<>();
			// 遍历输出每个个体,并格式化输出染色体结构以及目标函数
			for (int i = 0; i < bestIndividuals.length; i++) {
				
				List<Integer> obj = bestIndividuals[i].getObj();
				betterObjs.add(obj);
				System.out.println("项目工期为：" + obj.get(0)+ " " + obj.get(1)+" " +obj.get(2));
			    if(i<7) {
			    	//合并 打印
			    	List<Integer> alltask=new ArrayList<>(bestIndividuals[i].getChromosome().get(0));
			    	List<Integer> allRes=new ArrayList<>(bestIndividuals[i].getChromosome().get(1));
			    	List<List<Integer>> head=bestIndividuals[i].getChromosomeHead();
			    	for(int l=head.get(0).size()-1;l>=0;l--) {
			    		alltask.add(0,head.get(0).get(l));
			    		allRes.add(0,head.get(1).get(l));
			    	}
			    	/*List<Integer>task=bestIndividuals[i].getChromosome().get(0);
			    	List<Integer>res=bestIndividuals[i].getChromosome().get(1);*/
			    	for(int k=0;k<alltask.size();k++) {
			    		System.out.print(alltask.get(k)+"---"+allRes.get(k)+"  ");
			    		if((k+1)%20==0) {
			    			System.out.println("");
			    		}
			    	}
				}
			    System.out.println("");
			}
		}
		// 如果没有
		else {
			System.out.println("该算法无法求得最优解");
		}
		long endTime = System.currentTimeMillis();
		System.out.println("共计用时：" + (endTime - startTime) / 1000 + "秒");
	}
	/**
	 * 选择种群中个体指定目标函数的最大值
	 * 
	 * @param index_objList
	 * @param m
	 * @return
	 */
	public static Population getbestsolution(Population p,Case project) {
		Population solutions;
		// P种群进行排序
//		List<Individual> indiv= popSort(p, project);
//		solutions = new Population(p.getPopulationsize(),project);
//		for(int i=0;i<indiv.size();i++) {
//		solutions.setIndividual(0, indiv.get(i));
//		}
		solutions=p;
		return solutions;	
	}
	/*
	 * 计算指定任务的紧前任务的最大完成时间
	 */
	public static List<Integer> computeMaxPreEndTime(Task curtask, List<ITask> taskslist) {
		// TODO Auto-generated method stub
		List<Integer> preEndtime =new ArrayList<>();
		for(int k=0;k<3;k++) {
			preEndtime.add(0);
		}
		List<Integer>  pretaskids = curtask.getPresecessorIDs();
		for (int j = 0; j < pretaskids.size();j++){
			List<Integer> finishTimeTemp=taskslist.get(pretaskids.get(j)-1).getFinishTime();//有问题
			preEndtime=Tools.computeStartTime(finishTimeTemp,preEndtime);
		}
		return preEndtime;
	}
	public static int minCompletedTimeResource(List<Machine> reslist,Task curtask ) {
		// TODO Auto-generated method stub
		//机器ID=index-1
		int size=reslist.size();
		List<Integer> IDsort=new ArrayList<>();
		for(int i=0;i<size;i++) {
			IDsort.add(i+1);//资源ID
		}
		Collections.sort(IDsort, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				//升序排列 最小的排前面  o1 o2 是资源ID
				int flag=0;
				List<Integer> o1finishTime=reslist.get(o1-1).getFinishTime();
				List<Integer> taskwitho1finishtime=computeCompletedTime(o1finishTime,curtask.getProcessTime().get(o1-1));
				List<Integer> o2finishTime=reslist.get(o2-1).getFinishTime();
				List<Integer> taskwitho2finishtime=computeCompletedTime(o2finishTime,curtask.getProcessTime().get(o2-1));
				//比较两个模糊执行时间的大小 tim1>time2 -1 time1<time2 1
				flag=compareTime(taskwitho1finishtime,taskwitho2finishtime)==1?-1:1;
				return flag;
			}
			
		});
		return IDsort.get(0);
	}
	public static List<Integer> computeCompletedTimeSimple(List<ITask> taskslist, List<Machine> reslist, Task curtask,
			int resourceid) {
		// TODO Auto-generated method stub
		List<Integer> preEndTime=Tools.computeMaxPreEndTime(curtask,taskslist);
		List<Integer> resfinishTime=reslist.get(resourceid-1).getFinishTime();
		List<Integer> startTime=Tools.computeStartTime(resfinishTime,preEndTime);
		//此任务完成时间
		List<Integer> endEndTime=Tools.computeCompletedTime(startTime, curtask.getProcessTime().get(resourceid-1));
		
		return endEndTime;
	}
	
}
