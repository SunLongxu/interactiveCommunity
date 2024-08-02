package hku.util;

import hku.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fangyixiang
 * @date Jul 20, 2015
 */
public class STEMExt {
	private Map<String, String> map = null;
	
	public STEMExt(){
		map = new HashMap<String, String>();
		
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(Config.stemFile));
			
			String line = null;
			while((line = stdin.readLine()) != null){
				String s[] = line.trim().split("/");
				for(int i = 1;i < s.length;i ++){
					map.put(s[i], s[0]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String extSTEM(String word){
		String value = map.get(word);
		if(value != null){
			return value;
		}else{
			return word;
		}
	}
	
	public static void main(String[] args) {
		STEMExt ext = new STEMExt();
		System.out.println(ext.extSTEM("scalability"));
	}

}
