package hku.prep.dblp;

import hku.prep.PrepUser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fangyixiang
 * @date Jul 22, 2015
 */
public class Finder {

	public static void main(String[] args) {
		try{
			String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
			BufferedReader stdin = new BufferedReader(new FileReader(path));
			
			int count = 0;
			String line = null;
			String title = null;
			while((line =  stdin.readLine()) != null){
				count ++;
				
				line = line.toLowerCase(); //consider lower case only
				if(count % 2 == 1){
					title = line;
					if(line.charAt(line.length() - 1) == '.'){
						title = line.substring(0, line.length() - 1);
					}
				}else{
					String authors[] = line.trim().split("\t");
					
					//(1) assign ids to authors and initialize all the users
					List<Integer> list = new ArrayList<Integer>();
					for(int i = 0;i < authors.length;i ++){
						String author = authors[i];
//						if(author.equals("juan comesaña")){
//							System.out.println(title);
//						}
//						if(author.equals("jim gray")){
						if(author.equals("jiawei han")){
							System.out.println(title);
						}
					}
				}
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
