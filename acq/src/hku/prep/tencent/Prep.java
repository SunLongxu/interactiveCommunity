package hku.prep.tencent;

import java.io.*;
import java.util.*;

import hku.Config;
import hku.prep.*;
/**
 * @author fangyixiang
 * @date Oct 5, 2015
 * nodes: 2,320,895
 * edges: 50,133,369 (double: 100,266,738)
 * average keyword: 7.009
 */
public class Prep {
	private List<PrepUser> userList = null;
	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	private String node[][] = new String[2320900][];
	
	public void handle(){
		userList = new ArrayList<PrepUser>();
		handleNode();
		handleGraph();
		output();
	}
	
	public void handleNode(){
		String nodeInFile = "/home/fangyixiang/Desktop/CCS/tencent/user_key_word.txt";
		
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
					
					Set<String> set = new HashSet<String>();
					String pairs[] = rest.trim().split(";");
					for(int i = 0;i < pairs.length;i ++){
						String pair[] = pairs[i].split(":");
						set.add(pair[0]);
					}
					
					node[index] = new String[set.size() + 1];
					node[index][0] = "qq" + index;
					int arrIdx = 1;
					Iterator<String> iter = set.iterator();
					while(iter.hasNext()){
						node[index][arrIdx] = iter.next();
						arrIdx += 1;
					}
				}else{
					node[index] = new String[1];
					node[index][0] = "qq" + index;
				}

				PrepUser user = new PrepUser(index, "-");
				userList.add(user);
				
				int id = Integer.parseInt(idStr);
				map.put(id, index);
				index += 1;
				
				
//				break;
			}
			stdin.close();
			System.out.println("The # of nodes:" + index + " userList.size:" + userList.size());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void handleGraph(){
		String graphInFile = "/home/fangyixiang/Desktop/CCS/tencent/user_sns.txt";

		try{
			BufferedReader stdin = new BufferedReader(new FileReader(graphInFile));
			String line = null;
			while((line = stdin.readLine()) != null){
				String nodes[] = line.trim().split("\t");
				int oldNode1 = Integer.parseInt(nodes[0]);
				int oldNode2 = Integer.parseInt(nodes[1]);
				
				int node1 = map.get(oldNode1);
				int node2 = map.get(oldNode2);
				
				userList.get(node1 - 1).getEdgeSet().add(node2);
				userList.get(node2 - 1).getEdgeSet().add(node1);
			}
			stdin.close();
			System.out.println("finish reading");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//step 3: output a graph and a list of user profiles
	private void output(){
		//step 1: output the information about nodes
		int kwLen = 0;
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.tencentNode));
			int n = userList.size();
			for(int index = 1;index <= n;index ++){
				String line = index + "\t" + node[index][0] + "\t";
				stdout.write(line);
				
				if(node[index].length > 1){
					for(int j = 1;j < node[index].length;j ++){
						String tmp = node[index][j] + " ";
						stdout.write(tmp);
					}
					kwLen += node[index].length - 1;
				}
			    
			    stdout.newLine();
			}
			stdout.flush();
			stdout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Average # of keywords:" + (kwLen * 1.0 / userList.size()));
		
		//step 2: output the graph (nodeId, its neighbors)
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.tencentGraph));
			int edge = 0;
			for(int i = 0;i < userList.size();i ++){
				PrepUser user = userList.get(i);
				int userId = user.getUserID();
				if(user != null){
					String line = userId + "";
					stdout.write(line);
					
					Iterator<Integer> iter = user.getEdgeSet().iterator();
				    while(iter.hasNext()){
				    	int neighbor = iter.next();
				    	if(userId != neighbor){//She/He appears in the original file
				    		String tmp = " " + neighbor;
				    		stdout.write(tmp);
				    		
				    		edge += 1;
				    	}
				    }

				    stdout.newLine();
				}
			}
			stdout.flush();
			stdout.close();
			System.out.println("edges: " + edge / 2);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
		Prep p = new Prep();
		p.handle();

	}
}
