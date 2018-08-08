package com.ffjsp;

import java.util.List;

public  class ITask {
	private int taskid;
	public int pretasknum;
	private List<Integer> startTime;
	private List<Integer> finishTime;
	//设置finishTime
	public ITask(Task task){
		taskid = task.getTaskID();
		pretasknum=task.getPretasknum();
		startTime=task.getStartTime();
		finishTime=task.getFinishTime();
	}
	
	public void setstarttime(List<Integer> starttime){
		startTime = starttime;
	}

	public List<Integer> getFinishTime() {
		return finishTime;
	}
	

	public List<Integer> getStartTime() {
		return startTime;
	}

	public void setStartTime(List<Integer> startTime) {
		this.startTime = startTime;
	}

	public void setFinishTime(List<Integer> finishTime) {
		this.finishTime = finishTime;
	}

	public int getTaskid() {
		return taskid;
	}

	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}
	
    
}