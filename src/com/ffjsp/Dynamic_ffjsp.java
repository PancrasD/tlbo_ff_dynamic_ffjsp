package com.ffjsp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Dynamic_ffjsp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length==1){
            File ff = new File("case_def");
            String[] fl = ff.list(); 
            if (fl==null){
            	System.out.print("没有在case_def目录下找到算例文件");
            	return;           	
            }
    		if (args[0].trim().toLowerCase().equals("f")){
    			PrintStream out = System.out;
                for(int i = 0; i<fl.length; i++){
                	 String _fn =  "case_def/" + fl[i];
                	 String _fo = "datalei/tlbo_ff_dyna_ffjsp_"+fl[i]+".txt";
                	 String _fh="case_head/" + fl[i];
                	 String _foconverce = "datalei/converce_"+fl[i]+".txt";
                	 NSFFA_algorithm(_fn,_fo,_fh,_foconverce);
                }
                System.setOut(out);
                System.out.println(fl.length +"个案例算法计算完成"); 
                return;
                
    		}
    		
        }else{
        	System.out.print("请输入参数：'f'、自学习算法");
        	return;
        }
	}

	private static void NSFFA_algorithm(String casefile,String datafile,String datahead, String _foconverce) {
		// TODO Auto-generated method stub
		//记录开始计算的时间，用于统计本算法的总时间
		long startTime = System.currentTimeMillis();
		List<Double> result=new ArrayList<>();
		// 创建案例类对象
		Case project = new Case(casefile,datahead);
        
		// 初始化种群
		Population P = new Population(NSFFA.NS,project,true);
		
		int generationCount = 0;
        //循环迭代 算法指定的次数
		while (generationCount < NSFFA.maxGenerations ) {
			

			P = P.getOffSpring_NSFFA();
			List<Integer> resultlist=P.getPopulation()[0].getObj();
            result.add((resultlist.get(0)+2*resultlist.get(1)+resultlist.get(2))/4.0);
			generationCount++;
		}
		//从最后得到种群中获取最优解集
		Population solutions = Tools.getbestsolution(P, project);
		
         
		 File f = new File(datafile);
		 PrintStream ps = null;
		 try {
		   if (f.exists()) f.delete();
		   f.createNewFile();
		   FileOutputStream fos = new FileOutputStream(f);
		   ps = new PrintStream(fos);
		   System.setOut(ps);
		   //输出最优解集
		   Tools.printsolutions(solutions,startTime);			   
		 } catch (IOException e) {
			e.printStackTrace();
		 }  finally {
	        if(ps != null) 	ps.close();
	     }
		 //输出收敛结果
		 f = new File(_foconverce);
		 ps = null;
		 try {
		   if (f.exists()) f.delete();
		   f.createNewFile();
		   FileOutputStream fos = new FileOutputStream(f);
		   ps = new PrintStream(fos);
		   System.setOut(ps);
		   //
		   //Tools.printConvrece(result);			   
		 } catch (IOException e) {
			e.printStackTrace();
		 }  finally {
	        if(ps != null) 	ps.close();
	     }
     }
	

}
