package com.ffjsp;

public final class NSFFA {
	// 种群数量，即果蝇群体数量
	public static int NS =200 ;//300//200//100
	// 每个个体生成子代种群的大小 基于气味搜索的 一个基于调整操作序列 一个基于调整资源序列
	public static int S =2;
	// 知识库更新的概率
	public static double alpha = 0.15;
	// 提供经验的果蝇数量
	public static int NE = 7;
	
	public static int maxGenerations =500;//250//500//750
	//设计参数  
    public static String[] operator=new String[]{"forwardInsert","backwardInsert","swapNeighbor","swapRandom"};
}
