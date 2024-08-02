package hku.util;

import hku.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Jul 27, 2015
 */
public class CCSSaver {
	private String path = null;
	private BufferedWriter stdout = null;
	
	public CCSSaver(String path){
		this.path = path;
		try {
			this.stdout = new BufferedWriter(new FileWriter(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void save(int graph[][], String nodes[][], int queryId, Set<Integer> ccsSet, String kws[]){
		try{
			//community information
			stdout.write("The query node: " + nodes[queryId][0] + "[" + queryId + "]");
			stdout.newLine();
			stdout.write("The community size: " + ccsSet.size());
			stdout.newLine();
			stdout.write("The community context: ");
			for(int i = 0;i < kws.length;i ++){
				stdout.write(kws[i] + " ");
			}
			stdout.newLine();
			stdout.write("The community cohesiveness: " + Config.k);
			stdout.newLine();
			
			//member information
			if(ccsSet.size() <= Config.ccsSizeThreshold){
				for(int nodeId:ccsSet){
					stdout.write(nodes[nodeId][0] + "[" + nodeId + "]: ");
					for(int i = 0;i < graph[nodeId].length;i ++){
						int neighbor = graph[nodeId][i];
						if(ccsSet.contains(neighbor)){
							stdout.write(nodes[neighbor][0] + "[" + neighbor + "] ");
						}
					}
					stdout.newLine();
				}
			}else{
				stdout.write("The community is too large to save (save threshold:" + Config.ccsSizeThreshold + ").");
			}
			stdout.newLine();
			
			stdout.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void save(int graph[][], String nodes[][], int queryId, String kws[]){
		try{
			//community information
			stdout.write("The query node: " + nodes[queryId][0] + "[" + queryId + "]");
			stdout.newLine();
			stdout.write("The community size: 0");
			stdout.newLine();
			stdout.write("The community context: ");
			for(int i = 0;i < kws.length;i ++){
				stdout.write(kws[i] + " ");
			}
			stdout.newLine();
			stdout.write("The community cohesiveness: " + Config.k);
			stdout.newLine();
			stdout.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
