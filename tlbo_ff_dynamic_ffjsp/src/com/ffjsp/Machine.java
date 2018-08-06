package com.ffjsp;

import java.util.ArrayList;
import java.util.List;

public   class Machine {
    private int machineID;
    private List<Integer> finishTime;
    public Machine(int machineID) {
    	this(machineID,null);
    }
    public Machine(Machine mac) {
    	this(mac.getMachineID(),mac.getFinishTime());
    }
    public Machine(int machineID,List<Integer> finishTime) {
    	this.machineID=machineID;
    	this.finishTime=finishTime;
    	if(finishTime==null) {
    		this.finishTime=initTime();
    	}
    	
    }
    private List<Integer> initTime() {
		// TODO Auto-generated method stub
		List<Integer> time=new ArrayList<>();
		time.add(0);
		time.add(0);
		time.add(0);
		return time;
	}
	public int getMachineID() {
		return machineID;
	}
	public void setMachineID(int machineID) {
		this.machineID = machineID;
	}
	public List<Integer> getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(List<Integer> finishTime) {
		this.finishTime = finishTime;
	}
    
    
}
