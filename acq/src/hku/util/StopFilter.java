package hku.util;

import hku.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Jul 20, 2015
 */
public class StopFilter {
	private Set<String> set = null;
	
	public StopFilter(){
		set = new HashSet<String>();
		
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(Config.stopFile));
			
			String line = null;
			while((line = stdin.readLine()) != null){
				set.add(line.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean contains(String word){
		return set.contains(word);		
	}
	
	public static void main(String[] args) {
		StopFilter ext = new StopFilter();
		System.out.println(ext.contains("on"));
	}

}
