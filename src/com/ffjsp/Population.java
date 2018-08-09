package com.ffjsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public  class Population {

	private int populationsize;
	private Individual[] population;
	private List<List<Integer>> populationObj;
	private Case project;
	public Population(int populationSize, Case project) {
		this.populationsize = populationSize;
		this.project = project;
		this.population = new Individual[populationSize];
	}
	
	public Population(int populationSize, Case project,boolean initial) {
		this.populationsize = populationSize;
		this.project = project;
		this.population = new Individual[populationSize];
		if (initial) {
			for (int i = 0; i < populationSize; i++) {
				Individual individual = new Individual(project);
				this.population[i] = individual;
			}
			this.populationObj = populationObjCompute(this.population);
		}
	}
	/*
	 * 计算给定种群的个体目标函数
	 * 得到个体目标函数的集合
	 * @param population 种群
	 * @return populationObj 种群中个体目标函数集合
	 */
	public List<List<Integer>> populationObjCompute(Individual[] population) {
		List<List<Integer>> populationObj = new ArrayList<>();
		for (Individual individual : population) {
			populationObj.add(individual.getObj());
		}
		return populationObj;
	}
	
	/*
	 * 使用果蝇优化算法获得下一代种群
	 * @param project 创建的案例
	 * @return OffSpring 下一代新种群
	 */
	public Population getOffSpring_NSFFA() {
	     //算法求解优化
		Population OffSpring = new Population(NSFFA.NS,project);
		// 基于气味搜索，每个个体生成S个个体，种群大小为NS*S
		//Population p1 = this.smell_BasedSearch();
		Population p1 = this.smell_BasedSearchDivieded();
		// 将两个种群合并
		Population mp1 = merged(this,p1);
		// 从混合种群中选择前populationSize个个体作为新一代父代种群
		Population p2 = mp1.slectPopulation(NSFFA.NS);
		// 基于知识的搜索  基于果蝇指导
		Population Q = p2.knowledge_BasedSearch();
		// 将两个种群合并
		Population mp3 = merged(p2,Q);
		// 从混合种群中选择前populationSize个个体作为新一代父代种群
		OffSpring = mp3.slectPopulation(NSFFA.NS);

		return OffSpring;
	}
	/*
	 * 基于知识指导搜索    搜索策略效果不佳 需要添加更多搜索策略改善算法
	 * 基于气味搜索 基于视觉搜索 基于知识指导  本质上是搜索策略   搜索策略里本质上是个体的改变探索更优解
	 * @return newPopulation 基于知识指导新生成的种群
	 */
	public Population knowledge_BasedSearch() {
		Population newPopulation = new Population(this.getPopulationsize(),project);
		// 选择NE个精英个体
		Population EPop = this.slectPopulation(NSFFA.NE);

		List<Task> tasks = project.getTasks();
		//资源分配概率更新 根据精英个体
		//更新资源分配概率 没有更新
		updateResourceProb(EPop,tasks);
		// 遍历种群的个体，利用最大概率法为每个个体的染色体任务序列重新分配资源
		Population resPopulation=updateAssignResource(this.getPopulation(),tasks);
		// 选择出最优种群
		// 将两个种群合并
		Population resupdateOrigin = merged(this,resPopulation);
		// 从混合种群中选择前populationSize个个体作为新一代父代种群
		Population resUpdate = resupdateOrigin.slectPopulation(NSFFA.NS);
		//任务序列调整 进行交叉算子操作
		Population taskPopulation1=updataAssignTask(resUpdate,EPop,tasks);//两种交叉    后期可以改进交叉算法  更多搜索策略  一种 vs两种 效果都不好
		Population taskPopulation2=updataAssignTask(EPop,resUpdate,tasks);
		Population taskPopulation=merged(taskPopulation1,taskPopulation2);
		Population resupdateTaskupdate= merged(resUpdate,taskPopulation);
		
		//精英相互学习
		Population ePopPopulationT=updataAssignTask(EPop,EPop,tasks);
		Population ePopPopulationR=updateAssignResource(ePopPopulationT.getPopulation(),tasks);
		//epop调整资源选择最早可以执行的
		Population ePopPopulation= merged(ePopPopulationR,ePopPopulationT);
		
		Population epopAndtaskRes= merged(ePopPopulation,resupdateTaskupdate);
		// 从混合种群中选择前populationSize个个体作为新一代父代种群
		newPopulation=epopAndtaskRes.slectPopulation(NSFFA.NS);
		return newPopulation;
	}
	/*
	 * 任务序列调整 进行交叉算子操作 遍历种群resUpdate从精英种群中随机选择一个进行序列调整
	 * 即取resUpdate的一段任务序列插入到EPop中且保持紧前约束 任务的资源随之插入
	 * @param resUpdate 经过资源调整的种群
	 * @param EPop 精英种群
	 * @param tasks 原始任务list
	 * @return taskPopulation 任务序列调整后种群
	 */
	private Population updataAssignTask(Population resUpdate, Population EPop,List<Task> tasks) {
		// TODO Auto-generated method stub
		//创建新种群
		Population taskPopulation = new Population(resUpdate.getPopulationsize(),project);
    	//遍历经过资源调整的每一个个体，为其进行任务序列调整  后期可以考虑调整概率的引入  即精英个体训练加强资源调整过的种群的任务序列
    	for (int i = 0; i <resUpdate.getPopulationsize(); i++) {
    		List<List<Integer>> taskChromosome = new ArrayList<>();
			Individual parent = resUpdate.getPopulation()[i];
			List<List<Integer>> resChromosome = parent.getChromosome();
			// 从NE精英个体群中随机选择一个个体  任务序列调整
			int randIndex = (int) (Math.random() * EPop.getPopulationsize());
			Individual parent_2 = EPop.getPopulation()[randIndex];
			List<List<Integer>> ePopChromosome = parent_2.getChromosome();
			// 随机选择两个位置，p,q,需要满足1<=p<q<=J-1      => 0.5*size<q-p<0.75*size
			int p, q;
			while (true) {
				int size=resChromosome.get(0).size();
				p = (int) (Math.random() * size);
				q = (int) (Math.random() * size);
				if (p < q) {
					break;
				}
			}
			//更改 将经过资源调整的染色体resChromosome的p-q段加入新个体染色体 将ePopChromosome的不属于resChromosome的部分加入新个体 满足紧前紧后关系
			taskChromosome=groupChromosome(resChromosome,ePopChromosome,p,q,tasks);
			//精英调整资源
			// 创建子个体
			Individual offspring = new Individual(taskChromosome,parent.getChromosomeHead(),project);
			taskPopulation.setIndividual(i, offspring);
	}
		return taskPopulation;
	}

	/*
	 * 将经过资源调整的染色体resChromosome的p-q段加入新个体染色体 将ePopChromosome的不属于resChromosome的部分加入新个体 满足紧前紧后关系  类似两点交叉
	 * @param resChromosome 经过资源调整的染色体
	 * @param ePopChromosome精英个体的染色体
	 * @return taskChromosome 经过任务序列重组后的染色体
	 */
    private List<List<Integer>> groupChromosome(List<List<Integer>> resChromosome, List<List<Integer>> ePopChromosome,
			int p, int q,List<Task> tasks) {
		// TODO Auto-generated method stub
    	List<List<Integer>> taskChromosome = new ArrayList<>();
    	/*
    	 *@param midium 存储资源调整的染色体的p-q段
    	 *@param start  存储精英个体的不属于medium段的放入medium前段的染色体段
    	 *@param end 存储精英个体的不属于medium段的放入medium后的染色体段
    	 *@param tID_List 存储中间段的任务编号 
    	 */
    	List<List<Integer>> midium=new ArrayList<>();
		List<List<Integer>> start=new ArrayList<>();
		List<List<Integer>> end=new ArrayList<>();
		// 用来存储中间任务序列
		List<Integer> tID_List = new ArrayList<>();
		//紧前关系的检查
		List<Integer> ta =new ArrayList<>();
		List<Integer> res=new ArrayList<>();
		List<Integer> ta1 =new ArrayList<>();
		List<Integer> res1=new ArrayList<>();
		for (int j = p; j <= q; j++) {
			tID_List.add(resChromosome.get(0).get(j));
			ta.add( resChromosome.get(0).get(j));
			res.add( resChromosome.get(1).get(j));
		}
		midium.add(new ArrayList<>(ta));
		midium.add(new ArrayList<>(res));
		ta.clear();
		res.clear();
		for (int j = 0; j < ePopChromosome.get(0).size(); j++) {
			if (!tID_List.contains(ePopChromosome.get(0).get(j))) { 
				int taskid=ePopChromosome.get(0).get(j);
				List<Integer> preids=tasks.get(taskid-1).getPresecessorIDs();
				if(preids.size()==0) {
					double rand=Math.random();
					//更改过 后面可以改成判断所有的同工作操作
					ta.add(taskid);
					res.add(ePopChromosome.get(1).get(j));
				}
				else {
					int flag=0;
					for(int k=0;k<preids.size();k++) {
						if(tID_List.contains(preids.get(k))) {
							flag=1;
							break;
						}
					}
					if(flag==1) {
						//tID_List包含task紧前 添加到后面
						ta1.add(taskid);
						res1.add(ePopChromosome.get(1).get(j));
					}
					else {
						ta.add(taskid);
						res.add(ePopChromosome.get(1).get(j));
					}
				}
		}
		}
		start.add(new ArrayList<>(ta));
		start.add(new ArrayList<>(res));
		end.add(new ArrayList<>(ta1));
		end.add(new ArrayList<>(res1));
		taskChromosome=groupStartMediumEnd(start,midium,end);
		return taskChromosome;
	}
    /*
     * 将新生成的三段染色体连起来
     * @param start 开始段
     * @param midium 中间段
     * @param end 尾段
     * @return taskChromosome 组合的染色体
     */
    private List<List<Integer>> groupStartMediumEnd(List<List<Integer>> start, List<List<Integer>> midium,
			List<List<Integer>> end) {
		// TODO Auto-generated method stub
    	List<List<Integer>> taskChromosome = new ArrayList<>();
    	List<Integer> task=new ArrayList<>();
		List<Integer> ress=new ArrayList<>();
		for(int e=0;e<start.get(0).size();e++) {
			task.add(start.get(0).get(e));
			ress.add(start.get(1).get(e));
		}
		for(int e=0;e<midium.get(0).size();e++) {
			task.add(midium.get(0).get(e));
			ress.add(midium.get(1).get(e));
		}
		for(int e=0;e<end.get(0).size();e++) {
			task.add(end.get(0).get(e));
			ress.add(end.get(1).get(e));
		}
		taskChromosome.add(task);
		taskChromosome.add(ress);
		return taskChromosome;
	}

	/*
     * 调整种群个体的任务资源 基于精英个体更新的任务到资源的分配概率 概率大的被选中
     * @param tasks 任务表
     * @return resPopulation 原始种群经过资源调整后的种群
     */
	private Population updateAssignResource(Individual[] individuals,List<Task> tasks) {
		// TODO Auto-generated method stub
    	Population resPopulation = new Population(individuals.length,project);
    	//遍历每一个个体
    	for (int i = 0; i <individuals.length; i++) {
    		
			Individual parent =individuals[i];
			List<List<Integer>> originChromosome = parent.getChromosome();
			//复制染色体对象 
			List<List<Integer>> resChromosome = new ArrayList<>();
			for (int m = 0; m < originChromosome.size(); m++) {
				List<Integer> list = new ArrayList<>();
				for (int n = 0; n < originChromosome.get(m).size(); n++) {
					list.add(originChromosome.get(m).get(n));
				}
				resChromosome.add(list);
			}
            //为每一个任务调整资源
			for (int j = 0; j < resChromosome.get(0).size(); j++) {
				// 任务ID
				int tID = resChromosome.get(0).get(j);
				// 任务对象
				Task t = tasks.get(tID - 1);
				//任务t的可执行资源集
				Map<Integer,Double> capapleResource = t.getCapableResource();
				//利用最大概率法
				int reassignResourceID = selectResource(capapleResource);
				resChromosome.get(1).set(j, reassignResourceID);
			}
			Individual offspring = new Individual(resChromosome,parent.getChromosomeHead(),project);
			resPopulation.setIndividual(i, offspring);
	}
    	return resPopulation;
    }

	/*
     * 根据精英个体更新资源分配概率
     * @param EPop 精英个体种群
     * @return rp 更新过后的资源到任务的分配概率  key:资源编号 value:资源到本任务的分配概率  map对应一个任务
     */
	private void updateResourceProb(Population EPop,List<Task> tasks) {
		// TODO Auto-generated method stub
		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			if(EPop.getPopulation()[0].getChromosomeHead().get(0).contains(task.getTaskID())) {
				continue;
			}
			List<Integer> capapleResourceid = task.getResourceIDs();
			//key:资源编号  value:资源分配到任务的概率
			Map<Integer,Double> rp = tasks.get(i).getCapableResource();	
			
			double sumTemp_P = 0;
			for (int j = 0; j < capapleResourceid.size(); j++) {
				double temp_P = (1 - NSFFA.alpha) * rp.get(capapleResourceid.get(j))
						+ NSFFA.alpha * getISum(EPop, task.getTaskID(), capapleResourceid.get(j)) / NSFFA.NE;
				rp.replace(capapleResourceid.get(j), temp_P);
				sumTemp_P += temp_P;
			}
			for (int j = 0; j < capapleResourceid.size(); j++) {
				double P = rp.get(capapleResourceid.get(j)) / sumTemp_P;
				rp.replace(capapleResourceid.get(j), P);
			}
			tasks.get(i).setCapableResource(rp);
		}
		
	}

	/*
	 * 从混合种群中，选择前N个个体
	 * 选中的为优化目标优的精英个体
	 * @param num 要选择的优个体数
	 * @return newPopulation 指定数目的精英种群
	 */
	public Population slectPopulation(int num) {
		// 创建新的种群
		Population newPopulation = new Population(num,project);

		// 混合种群进行快速排序 混合种群内部也进行了排序
		// 返回混合种群根据目标值进行排序的个体 
		List<Individual> indivIndex = Tools.popSort(this, project);
		//保持多样性 先选择独特的
		List<Individual> diffrentIndix=new ArrayList<>();
		List<Individual> alreadyIndix=new ArrayList<>();
		List<List<Integer>> lastSchedule=indivIndex.get(0).getChromosome();
		List<Integer> lastobj=new ArrayList<>();
		diffrentIndix.add(indivIndex.get(0));
		lastobj=indivIndex.get(0).getObj();
		
		for(int k=1;k<indivIndex.size();k++) {
			List<Integer> obj=indivIndex.get(k).getObj();
			//判断值是否相等
			if(Tools.judgeListEqual(obj,lastobj)) {
				//判断schedule是否相等
				List<List<Integer>> schedule=indivIndex.get(k).getChromosome();
//				boolean flag=Tools.judgeSameIndividual(schedule,lastSchedule);//相等true 不相等flase
//				if(flag) {
//					//如果相等则添加到已存在
//					alreadyIndix.add(indivIndex.get(k));
//				}
//				else {
//					//不相等则添加到不同表
//					diffrentIndix.add(indivIndex.get(k));
//					//更新lastobj lastSchedule
//					lastobj=obj;
//					lastSchedule=schedule;
//				}
			     alreadyIndix.add(indivIndex.get(k));
			}
			else {
				diffrentIndix.add(indivIndex.get(k));
				//更新lastobj lastSchedule
				lastobj=obj;
				lastSchedule=indivIndex.get(k).getChromosome();
			}
		}
		//首先在不同表中选择 不够再在相同表中选
		int diffSize=diffrentIndix.size();
		if(diffSize>=num) {
			//满足个数 直接添加
			for(int i=0;i<num;i++) {
				//个体是在变的  
				Individual indiv=new Individual(diffrentIndix.get(i));
				newPopulation.setIndividual(i,indiv);
			}
		}
		else {
			//不满足个数要求 先把全部的不同个体添加 然后依次添加相同表里面的
			for(int i=0;i<diffSize;i++) {
				Individual indiv=new Individual(diffrentIndix.get(i));
				newPopulation.setIndividual(i,indiv);
			   }
			for(int i=0;i<num-diffSize;i++) {
				//个体是在变的  
				Individual indiv=new Individual(alreadyIndix.get(i));
				newPopulation.setIndividual(diffSize+i,indiv);
		       }
		   }
		return newPopulation;
	}
	/*
	 * 将两个算子拆开单独生成种群
	 * 
	 */
	public Population smell_BasedSearchDivieded() {
		// 遍历输入种群中的每个个体，每个个体经过操作，生成S个子个体
		Individual[] individuals = this.getPopulation();
		//进行资源调整
		Population resPopulation=smelll_BasedResourceSearch(individuals);
		Population taskPopulation=smell_BasedTaskOrderSearch(individuals);
		//resPopulation size*s taskPopulationsize*s  pop2:5s
		Population pop1=merged(this,resPopulation);
		Population pop2=merged(pop1,taskPopulation);
		return pop2;
		
	}
	private Population smell_BasedTaskOrderSearch(Individual[] individuals) {
		// TODO Auto-generated method stub
		int size=this.getPopulationsize();
		Population taskPopulation = new Population(size* NSFFA.S,project );
		List<Individual> indivList = new ArrayList<>();
		for (int i = 0; i < size; i++){
			Individual individual = individuals[i];
			
			for (int j = 0; j<NSFFA.S; j++)  {
				List<List<Integer>> offspringChromosome = new ArrayList<>();
				for (int m = 0; m < individual.getChromosome().size(); m++) {
					List<Integer> list = new ArrayList<>();
					for (int n = 0; n < individual.getChromosome().get(m).size(); n++) {
						list.add(individual.getChromosome().get(m).get(n));
					}
					offspringChromosome.add(list);
				}
		        Random rand=new Random();
				double rprob=rand.nextDouble();
				if(rprob<0.5) {
				//选取两个位置 交换 修正违反紧前关系的
				//随机选取两个位置  进行size次/1次
					int index_origin = (int) (Math.random() * offspringChromosome.get(0).size());
					int index_new = (int) (Math.random() * offspringChromosome.get(0).size());
					if(index_origin!=index_new ) {
						int taskID1 = offspringChromosome.get(0).get(index_origin);
						int resourceID1 = offspringChromosome.get(1).get(index_origin);
						offspringChromosome.get(0).remove(index_origin);
						offspringChromosome.get(1).remove(index_origin);
						if(index_origin>index_new) {
							offspringChromosome.get(0).add(index_new, taskID1);
							offspringChromosome.get(1).add(index_new, resourceID1);
							
						}
						else {
							offspringChromosome.get(0).add(index_new-1, taskID1);
							offspringChromosome.get(1).add(index_new-1, resourceID1);
							index_new=index_new-1;
						}
						//检查紧前关系 修正关系 位置排序  ID排序 一一对应   在原始project中taskID比index小1
						Task task1 = project.getTasks().get(taskID1 - 1);
						//List<Integer> preIDs=task1.getPresecessorIDs();//紧前关系紧后关系都要考虑
						List<Integer> preIDs=task1.getSameJobTaskIDS();//紧前关系紧后关系都要考虑 重新排列
						//需要处理 去除头部
						preIDs=processWithHead(individual.getChromosomeHead(),preIDs);
						List<Integer> IDs=new ArrayList<>(preIDs);
						IDs.add(taskID1);
						List<Integer> indexs=new ArrayList<>();
						Map<Integer,Integer> taskRes=new TreeMap<>();
						taskRes.put(taskID1, resourceID1);//存储需要重新排列的任务资源信息
						indexs.add(index_new);//存储位置
						
						for(int m=0;m<preIDs.size();m++) {
							//根据preTaskID查找其在染色体中的位置
							int index=offspringChromosome.get(0).indexOf(preIDs.get(m));
							indexs.add(index);
							taskRes.put(preIDs.get(m),offspringChromosome.get(1).get(index));
						}

						Collections.sort(IDs);
						Collections.sort(indexs);
						for(int x=0;x<IDs.size();x++) {
							offspringChromosome.get(0).set(indexs.get(x),IDs.get(x));
							offspringChromosome.get(1).set(indexs.get(x),taskRes.get(IDs.get(x)));
						}
					}
					
				}
				else {
				// 重复随机选择任务序列中的某个位置，直到两个相邻任务没有紧前任务关系， 做1次任务位置交换
					
				while (true) {
					int index_t_2 = (int) (Math.random() * offspringChromosome.get(0).size());
					if (index_t_2 != (offspringChromosome.get(0).size() - 1)) {
						
						int taskID1 = offspringChromosome.get(0).get(index_t_2);
						int resourceID1 = offspringChromosome.get(1).get(index_t_2);
						int taskID2 = offspringChromosome.get(0).get(index_t_2 + 1);
						int resourceID2 = offspringChromosome.get(1).get(index_t_2 + 1);

						Task task1 = project.getTasks().get(taskID1 - 1);
						Task task2 = project.getTasks().get(taskID2 - 1);

						if (!project.isPredecessor(task1, task2)) {
							// 交换两个位置上的任务编号以及资源编号
							offspringChromosome.get(0).set(index_t_2, taskID2);
							offspringChromosome.get(1).set(index_t_2, resourceID2);
							offspringChromosome.get(0).set(index_t_2 + 1, taskID1);
							offspringChromosome.get(1).set(index_t_2 + 1, resourceID1);
							break;
						} 
					}
				}
				}
				// 创建子代个体对象
				Individual offspring = new Individual(offspringChromosome,individual.getChromosomeHead(),project);
				indivList.add(offspring);
			}
		}
		for (int i = 0; i < indivList.size(); i++) {
			taskPopulation.setIndividual(i, indivList.get(i));
		}
		return taskPopulation;
	}

	public List<Integer> processWithHead(List<List<Integer>> head, List<Integer> preIDs) {
		// TODO Auto-generated method stub
		for(int i=0;i<head.get(0).size();i++) {
			if(preIDs.contains(head.get(0).get(i))) {
				preIDs.remove(head.get(0).get(i));
			}
		}
		return preIDs;
	}

	/*
	 * 气味搜索阶段的资源调整搜索
	 */
	private Population smelll_BasedResourceSearch(Individual[] individuals) {
		// TODO Auto-generated method stub
		int size=this.getPopulationsize();
		Population resPopulation = new Population(size* NSFFA.S,project );
		List<Individual> indivList = new ArrayList<>();
		for (int i = 0; i < size; i++){
			Individual individual = individuals[i];
				for (int j = 0; j<NSFFA.S; j++)  {

					List<List<Integer>> offspringChromosome = new ArrayList<>();
					for (int m = 0; m < individual.getChromosome().size(); m++) {
						List<Integer> list = new ArrayList<>();
						for (int n = 0; n < individual.getChromosome().get(m).size(); n++) {
							list.add(individual.getChromosome().get(m).get(n));
						}
						offspringChromosome.add(list);
					}
					// 随机选择任务序列中的某个位置  对1个任务的资源重新选择  资源调整
					//添加次数 一次太少
					int index_t_1 = (int) (Math.random() * offspringChromosome.get(0).size());
					
					int taskID = offspringChromosome.get(0).get(index_t_1);
					Task task = project.getTasks().get(taskID - 1);
					
					List<Integer> capapleResource = task.getResourceIDs();
					double rd = Math.random() ;
					int index_capaple = (int) (rd * capapleResource.size());
					
					int resourceid = capapleResource.get(index_capaple);
					offspringChromosome.get(1).set(index_t_1, resourceid);
					
				    // 创建子代个体对象
					Individual offspring = new Individual(offspringChromosome,individual.getChromosomeHead(),project);
					indivList.add(offspring);
				}
				
		}
		for (int i = 0; i < indivList.size(); i++) {
			resPopulation.setIndividual(i, indivList.get(i));
		}
		return resPopulation;
	}

	/**
	 * 基于气味搜索方法 遍历输入种群中的每个个体，每个个体经过操作，生成S个子个体
	 * @param population
	 * @return
	 */
	public Population smell_BasedSearch() {
		int size=this.getPopulationsize();
		int numTask=project.getNtask();
		Population newPopulation = new Population(size* NSFFA.S,project );
		List<Individual> indivList = new ArrayList<>();
		// 遍历输入种群中的每个个体，每个个体经过操作，生成S个子个体
		Individual[] individuals = this.getPopulation();
		//resPopulation=resAdjustSearch(individuals);
		for (int i = 0; i < size; i++){
		Individual individual = individuals[i];
			for (int j = 0; j<NSFFA.S; j++)  {

				List<List<Integer>> offspringChromosome = new ArrayList<>();
				for (int m = 0; m < individual.getChromosome().size(); m++) {
					List<Integer> list = new ArrayList<>();
					for (int n = 0; n < individual.getChromosome().get(m).size(); n++) {
						list.add(individual.getChromosome().get(m).get(n));
					}
					offspringChromosome.add(list);
				}
				// 随机选择任务序列中的某个位置  对1个任务的资源重新选择  资源调
                //
				int index_t_1 = (int) (Math.random() * offspringChromosome.get(0).size());
				int taskID = offspringChromosome.get(0).get(index_t_1);
				Task task = project.getTasks().get(taskID - 1);
				List<Integer> capapleResource = task.getResourceIDs();
				double rd = Math.random() ;
				int index_capaple = (int) (rd * capapleResource.size());
				int resourceid = capapleResource.get(index_capaple);
				offspringChromosome.get(1).set(index_t_1, resourceid);

				Random rand=new Random();
				double tprob=rand.nextDouble();
				if(tprob<0.7) {
				//选取两个位置 交换 修正违反紧前关系的
				for(int k=0;k<numTask;k++) {
					int index_origin = (int) (Math.random() * offspringChromosome.get(0).size());
					int index_new = (int) (Math.random() * offspringChromosome.get(0).size());
					if(index_origin!=index_new ) {
						int taskID1 = offspringChromosome.get(0).get(index_origin);
						int resourceID1 = offspringChromosome.get(1).get(index_origin);
						offspringChromosome.get(0).remove(index_origin);
						offspringChromosome.get(1).remove(index_origin);
						if(index_origin>index_new) {
							offspringChromosome.get(0).add(index_new, taskID1);
							offspringChromosome.get(1).add(index_new, resourceID1);
						}
						else {
							offspringChromosome.get(0).add(index_new-1, taskID1);
							offspringChromosome.get(1).add(index_new-1, resourceID1);
							index_new=index_new-1;
						}
						//检查紧前关系 修正关系 位置排序  ID排序 一一对应   在原始project中taskID比index小1
						Task task1 = project.getTasks().get(taskID1 - 1);
						//出错
						List<Integer> preIDs=task1.getSameJobTaskIDS();//紧前关系紧后关系都要考虑 重新排列
						//List<Integer> preIDs=task1.getPresecessorIDs();
						List<Integer> IDs=new ArrayList<>(preIDs);
						IDs.add(taskID1);
						List<Integer> indexs=new ArrayList<>();
						Map<Integer,Integer> taskRes=new TreeMap<>();
						taskRes.put(taskID1, resourceID1);//存储需要重新排列的任务资源信息
						indexs.add(index_new);//存储位置
						
						for(int m=0;m<preIDs.size();m++) {
							//根据preTaskID查找其在染色体中的位置
							 int index=offspringChromosome.get(0).indexOf(preIDs.get(m));
							indexs.add(index);
							taskRes.put(preIDs.get(m),offspringChromosome.get(1).get(index));
						}

						Collections.sort(IDs);
						Collections.sort(indexs);
						for(int x=0;x<IDs.size();x++) {
							offspringChromosome.get(0).set(indexs.get(x),IDs.get(x));
							offspringChromosome.get(1).set(indexs.get(x),taskRes.get(IDs.get(x)));
						}
					}
					
				  }
				}
				else {
				// 重复随机选择任务序列中的某个位置，直到两个相邻任务没有紧前任务关系， 做1次任务位置交换
					while (true) {
						int index_t_2 = (int) (Math.random() * offspringChromosome.get(0).size());
						if (index_t_2 != (offspringChromosome.get(0).size() - 1)) {
							
							int taskID1 = offspringChromosome.get(0).get(index_t_2);
							int resourceID1 = offspringChromosome.get(1).get(index_t_2);
							int taskID2 = offspringChromosome.get(0).get(index_t_2 + 1);
							int resourceID2 = offspringChromosome.get(1).get(index_t_2 + 1);
	
							Task task1 = project.getTasks().get(taskID1 - 1);
							Task task2 = project.getTasks().get(taskID2 - 1);
	
							if (!project.isPredecessor(task1, task2)) {
								// 交换两个位置上的任务编号以及资源编号
								offspringChromosome.get(0).set(index_t_2, taskID2);
								offspringChromosome.get(1).set(index_t_2, resourceID2);
								offspringChromosome.get(0).set(index_t_2 + 1, taskID1);
								offspringChromosome.get(1).set(index_t_2 + 1, resourceID1);
								break;
							} 
						}
				 }
				}
				// 创建子代个体对象
				Individual offspring = new Individual(offspringChromosome,individual.getChromosomeHead(),project);
				indivList.add(offspring);
			}
		}
		for (int i = 0; i < indivList.size(); i++) {
			newPopulation.setIndividual(i, indivList.get(i));
		}
		return newPopulation;
	}


	/*
	 * 合并两个种群
	 */
	public Population merged(Population p1,Population p2){
		List<Individual> mergedList = new ArrayList<>();
		for (int i = 0; i < p1.getPopulationsize(); i++) {
			if (!mergedList.contains(p1.getPopulation()[i])) {
			  mergedList.add(p1.getPopulation()[i]);				
			}
		}
		for (int i = 0; i < p2.getPopulationsize(); i++) {
			if (!mergedList.contains(p2.getPopulation()[i])) {
				  mergedList.add(p2.getPopulation()[i]);				
				}			
		}
		Population mergedPopulation = new Population(mergedList.size(),project);
		for (int i =0; i <mergedList.size();i++){
			mergedPopulation.setIndividual(i, mergedList.get(i));
		}
		return mergedPopulation;
	}
	/**
	 * 计算在NE个精英个体中，有多少个体满足指定任务，被指定资源分配
	 * 
	 * @param ePop
	 * @param taskID
	 * @param resourceID
	 * @return
	 */
	private int getISum(Population ePop, int taskID, int resourceID) {
		int sum = 0;
		for (int i = 0; i < ePop.getPopulationsize(); i++) {
			Individual indiv = ePop.getPopulation()[i];
			// 指定任务在个体染色体中的索引
			int index_t = indiv.getChromosome().get(0).indexOf(taskID);
			int rID = indiv.getChromosome().get(1).get(index_t);
			// 如果与指定资源ID相同，sum加1
			if (rID == resourceID) {
				sum++;
			}
		}
		return sum;
	}

	/*
	 * 根据精英个体资源任务分配概率选择分配任务的资源 最大可能的概率                可以引入一个随机变量 蚁群算法中  大于指定的数 就采用最大概率的资源 否则就随机选择一个 保持多样性  轮盘赌？？
	 * @param capapleResource 任务能够执行的资源及对应分配概率
	 * @return 选中的资源编号
	 */
	public int selectResource(Map<Integer, Double> capapleResource) {
		int Resourceid=0;
		// 选择资源                =>选择最大的
		double rand = Math.random();
		if(rand<1) {
		List<Map.Entry<Integer,Double>> capapleList=new ArrayList<>();
		for(Map.Entry<Integer,Double> entry:capapleResource.entrySet()) {
			capapleList.add(entry);
		}
		Collections.sort(capapleList,new Comparator<Map.Entry<Integer,Double>>(){

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// TODO Auto-generated method stub
				int flag=0;
				//根据分配概率大小进行排序 分配概率大的背选中
				if(o1.getValue()>o2.getValue()) {
					flag=-1;
				}
				if(o1.getValue()<o2.getValue()) {
					flag=1;
				}
				return flag;
			}
			
		});
		Resourceid =capapleList.get(0).getKey();
		}
		else {
		double rouletteWheelPosition = Math.random();
		double spinWheel = 0;
		
		Iterator<Integer> rt = capapleResource.keySet().iterator();
		while(rt.hasNext()){
			Resourceid = rt.next();
			spinWheel += capapleResource.get(Resourceid);
			if (spinWheel >= rouletteWheelPosition) {
				break;
			}		
		}
		}
		return Resourceid;
	}

	// 设置种群中的个体
	public Individual setIndividual(int offset, Individual individual) {
		return population[offset] = individual;
	}
	public int getPopulationsize() {
		return populationsize;
	}

	public void setPopulationsize(int populationsize) {
		this.populationsize = populationsize;
	}

	public Individual[] getPopulation() {
		return population;
	}

	public void setPopulation(Individual[] population) {
		this.population = population;
	}

	public List<List<Integer>> getPopulationObj() {
		return populationObj;
	}

	public void setPopulationObj(List<List<Integer>> populationObj) {
		this.populationObj = populationObj;
	}

	public Case getProject() {
		return project;
	}

	public void setProject(Case project) {
		this.project = project;
	}
	
}
