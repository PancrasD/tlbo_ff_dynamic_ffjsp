package com.ffjsp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Case {

	private int Ntask;// 案例包含的任务数�?
	private int Nmachine;// 案例包含的资源数�?
   
	private List<Task> tasks = new ArrayList<>();
	private List<Machine> resources = new ArrayList<>();
	public Case(String file) throws IOException {
		readCaseFile(file);
		setTaskCapapleResource();
	}
	private void readCaseFile(String file) throws IOException {
		BufferedReader read=null;
		try {
			read=new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("file not exits");
		}
		String line=null;
		int lastJobFinalTaskID=0;
		int taskid=0;
		while((line=read.readLine())!=null) {
			if(line.startsWith("Job")) {
				lastJobFinalTaskID=taskid;
		    }
			else {
				 String[] str=line.split(" +");
			     int id=Integer.parseInt(str[0])+lastJobFinalTaskID;
			     taskid=id;//
			     List<List<Integer>> processTime=new ArrayList<>();
			     //str 第一个元素是每项工作的操作id 后面是对应于十台机器的模糊执行时�? 执行时间格式�?  3,5,7
			     for(int i=1;i<str.length;i++) {
					List<Integer> processOne=new ArrayList<>();
					String[] process=str[i].split("/");
					for(int k=0;k<process.length;k++) {
						processOne.add(Integer.parseInt(process[k]));
					}
					processTime.add(processOne);
				}
			     //生成紧前任务�?
			     List<Integer> preIDs=new ArrayList<Integer>();
			     for(int j=1+lastJobFinalTaskID;j<id;j++) {
			    	 preIDs.add(j);
			     }
			     Task task=new Task(id,preIDs,processTime);
			     tasks.add(task);
			  }
		}
		read.close();
		Ntask=tasks.size();
		Nmachine=10;//统一都是十台机器
		for(int i=1;i<=Nmachine;i++) {
			 resources.add(new Machine(i));
		}
		countsuccessor();//耗时 �?�?
		countSameJobID();
	}
	/**
	 * 判断任务执行链表中相邻两个任务之间是否存在紧前关系约束�?? 如果task1是task2的紧前任务，则返回true
	 * 分两种情况：1.task2没有紧前任务,返回false; 2.task2有紧前任�?: 紧前任务包含task1; 紧前任务不包含task1
	 * 
	 * @param task1
	 *            任务1
	 * @param task2
	 *            任务2
	 * @return
	 */
	public boolean isPredecessor(Task task1, Task task2) {
		boolean flag = false;

		// task1的ID
		int task1_ID = task1.getTaskID();
		if (task2.getPresecessorIDs().contains(task1_ID)) {
			flag = true;
		}
		return flag;
	}
	/*
	 * 初始化资源的分配任务概率 1/CapableResource.size()
	 */
	public void setTaskCapapleResource() {
		for (int i = 0; i < tasks.size(); i++) {
			Map<Integer, Double> r_possibility = new HashMap<>();
			List<Integer> resurceid = tasks.get(i).getResourceIDs();
			for (int j = 0; j < resurceid.size(); j++) {
				r_possibility.put(resurceid.get(j), ((double) 1) / resurceid.size());
			}
			tasks.get(i).setCapableResource(r_possibility);
		}

	}
	public void countsuccessor() {
		//循环每一列，

		for (int i = 0; i< tasks.size();i++){
			List<Integer> successorIDS = new ArrayList<>();
			for (int j = i+1; j < tasks.size(); j++) {
				int num =  tasks.get(j).getPretasknum();
			   if (num > 0) {
					List<Integer> pre_IDs = tasks.get(j).getPresecessorIDs();
					for (int k=0;k<num;k++) {
						if (pre_IDs.get(k)==i+1){
							successorIDS.add(tasks.get(j).getTaskID());
						}
					}
				}
			}
			tasks.get(i).setsuccessorTaskIDS(successorIDS);
		}
		return;
	}
	public void countSameJobID() {
		for (int i = 0; i< tasks.size();i++){
		List<Integer> pre=tasks.get(i).getPresecessorIDs();
		List<Integer> succ=tasks.get(i).getSuccessorTaskIDS();
		List<Integer> sameJobIDs=new ArrayList<>();
		sameJobIDs.addAll(pre);
		sameJobIDs.addAll(succ);
		tasks.get(i).setSameJobTaskIDS(sameJobIDs);
	}
	}
	public int getNtask() {
		return Ntask;
	}
	public void setNtask(int ntask) {
		Ntask = ntask;
	}
	public int getNmachine() {
		return Nmachine;
	}
	public void setNmachine(int nmachine) {
		Nmachine = nmachine;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public List<Machine> getResources() {
		return resources;
	}
	public void setResources(List<Machine> resources) {
		this.resources = resources;
	}
	

}
