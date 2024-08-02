package hku.prep.dblp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yxfang
 *
 * @date 2015-7-19
 */
public class LineReader {

	public void work(){
		try{
			String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp.xml";
//			String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-ext.xml";
			BufferedReader stdin = new BufferedReader(new FileReader(path));
			
			String line = null;
			long count = 0;
			while((line =  stdin.readLine()) != null){
				count ++;
//				if(count > 17305951){
//				if(count > 43314425){
//					System.out.println(line);
//				}
				if(line.startsWith("<proceedings")){
					System.out.println(line);
					for(int i = 0;i < 40;i ++){
						System.out.println(stdin.readLine());
					}
				}
			}
			stdin.close();
			
			System.out.println("count:" + count);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void countUser(){
		try{
			String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
			BufferedReader stdin = new BufferedReader(new FileReader(path));
			
			int count = 0;
			String line = null;
			Set<String> set = new HashSet<String>();
			int edge = 0;
			while((line =  stdin.readLine()) != null){
				count ++;
				if(count % 2 == 0){
					String s[] = line.trim().split("\t");
					for(int i = 0;i < s.length;i ++){
						set.add(s[i]);
					}
					
					int authorNum = s.length;
					System.out.println("authorNum:" + authorNum);
					edge += authorNum * (authorNum - 1) / 2;
				}
			}
			stdin.close();
			
			System.out.println("node:" + set.size());
			System.out.println("edge:" + edge);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void count(){
		try{
			String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-node";
			BufferedReader stdin = new BufferedReader(new FileReader(path));
			
			int count = 0;
			String line = null;
			while((line =  stdin.readLine()) != null){
				
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LineReader reader = new LineReader();
		reader.work();

	}

}

//node:979134
//edge:4969328
