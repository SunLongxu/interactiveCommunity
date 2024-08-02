package hku.prep.tencent;

import hku.prep.PrepUser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
/**
 * @author fangyixiang
 * @date Oct 14, 2015
 */
public class Test {

	public static void main(String[] args) {
		String nodeInFile = "/home/fangyixiang/Desktop/CCS/tencent/user_key_word.txt";
		
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String node[][] = new String[2320900][];
		
		int index = 1;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(nodeInFile));
			
			String line = null;
			while((line = stdin.readLine()) != null){
				String idStr = line;
				
				if(line.indexOf('\t') > 0){
					idStr = line.substring(0, line.indexOf('\t'));
//					System.out.println(idStr);
					
					String rest = line.substring(line.indexOf('\t') + 1);
//					System.out.println(rest);
					
					String pairs[] = rest.trim().split(";");
					int len = pairs.length;
//					node[index] = new String[len + 1];
//					node[index][0] = "qq" + index;
					for(int i = 0;i < len;i ++){
						String pair[] = pairs[i].split(":");
//						node[index][i + 1] = pair[0];
						
						if(index == 680655){
							System.out.println("fuck:" + pair[0]);
						}
					}
					if(index == 680655){
						System.out.println(line);
					}
				}else{
//					node[index] = new String[1];
//					node[index][0] = "qq" + index;
				}

				int id = Integer.parseInt(idStr);
				map.put(id, index);
				index += 1;
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
