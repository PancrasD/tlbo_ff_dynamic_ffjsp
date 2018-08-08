package com.ffjsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public  class Task {
    //任务/操作ID
	private int taskID;
	//紧前
	private List<Integer> presecessorIDs=new ArrayList<>();
	//紧前任务数
	private int pretasknum;
	//紧后任务IDS
	private List<Integer> successorTaskIDS = new  ArrayList<>();
	//任务执行时间 index0-9 机器ID1-10 执行模糊时间
	private List<List<Integer>> processTime=new ArrayList<>();
	//任务可执行资源 key:资源编号 value:资源到任务的概率
	private Map<Integer,Double> capableResource=new TreeMap<>();
	//任务的开始时间
	private List<Integer> startTime;
	//任务的结束时间
	private List<Integer> finishTime;
	//任务可用的资源
	private List<Integer> resourceIDs = new ArrayList<>();
	//紧后任务IDS
	private List<Integer> sameJobTaskIDS = new  ArrayList<>();
	
	public List<Integer> getSameJobTaskIDS() {
		return sameJobTaskIDS;
	}
	public void setSameJobTaskIDS(List<Integer> sameJobTaskIDS) {
		this.sameJobTaskIDS = sameJobTaskIDS;
	}
	private int minProcessTimeResource;
	public Task(int id,List<Integer> preIDs,List<List<Integer>> processTime) {
		
		this(id,null,null,preIDs,processTime);
	}
	public Task(int id,List<Integer> startTime,List<Integer> finishTime,List<Integer> preIDs,List<List<Integer>> processTime) {
		this.taskID=id;
		this.presecessorIDs=preIDs;
		this.processTime=processTime;
		this.pretasknum=preIDs.size();
		this.startTime=startTime;
		this.finishTime=finishTime;
		for(int i=1;i<=10;i++) {
			this.resourceIDs.add(i);
		}
		if(startTime==null) {
			this.startTime=initTime();
		}
		if(finishTime==null) {
			this.finishTime=initTime();
		}
		this.minProcessTimeResource=computeMinProcessTimeResource();
	}
	/*
	 * 获得任务最小执行时间可执行资源的ID
	 */
	public Integer computeMinProcessTimeResource() {
		int minIndex=0;//最小资源的索引
		List<Integer> minProcess=processTime.get(0);
		for(int i=1;i< processTime.size();i++) {
			int flag=compareProcessTime(minProcess,processTime.get(i));
			if(flag>0) {
				minIndex=i;
				minProcess=processTime.get(i);
			}
		}
		return (minIndex+1);
	}
	/*
	 * @return flag 1:minProcess>processTime -1:minProcess<processTime
	 */
	private int compareProcessTime(List<Integer> minProcess, List<Integer> processTime) {
		// TODO Auto-generated method stub
		List<Integer> o1=minProcess;
		List<Integer> o2=processTime;
		
		int flag=0;
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
	private List<Integer> initTime() {
		// TODO Auto-generated method stub
		List<Integer> time=new ArrayList<>();
		time.add(0);
		time.add(0);
		time.add(0);
		return time;
	}
	public void setsuccessorTaskIDS(List<Integer> taskids) {
		this.successorTaskIDS = taskids;
	}
	public int getTaskID() {
		return taskID;
	}
	public List<Integer> getPresecessorIDs() {
		return presecessorIDs;
	}
	public int getPretasknum() {
		return pretasknum;
	}
	public List<Integer> getSuccessorTaskIDS() {
		return successorTaskIDS;
	}
	public List<List<Integer>> getProcessTime() {
		return processTime;
	}
	public List<Integer> getStartTime() {
		return startTime;
	}
	public List<Integer> getFinishTime() {
		return finishTime;
	}
	public void setPretasknum(int pretasknum) {
		this.pretasknum = pretasknum;
	}
	public void setStartTime(List<Integer> startTime) {
		this.startTime = startTime;
	}
	public void setFinishTime(List<Integer> finishTime) {
		this.finishTime = finishTime;
	}
	public List<Integer> getResourceIDs() {
		return resourceIDs;
	}
	public Map<Integer, Double> getCapableResource() {
		return capableResource;
	}
	public void setCapableResource(Map<Integer, Double> capableResource) {
		this.capableResource = capableResource;
	}
	public int getMinProcessTimeResource() {
		return minProcessTimeResource;
	}
	
	
}