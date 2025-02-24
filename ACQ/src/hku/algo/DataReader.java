package hku.algo;

import hku.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fangyixiang
 * @date Jul 22, 2015
 * (1) read the node information
 * (2) read the node and edges (Nodes are named starting from 1, 2, 3, ...)
 */
public class DataReader {
	private String graphFile = null;
	private String nodeFile = null;
	private int userNum = -1;
	private int edgeNum = -1;
	
	public DataReader(String graphFile, String nodeFile){
		this.graphFile = graphFile;
		this.nodeFile = nodeFile;
		
		try{
			File test= new File(nodeFile);
			long fileLength = test.length(); 
			LineNumberReader rf = new LineNumberReader(new FileReader(test));
			if (rf != null) {
				rf.skip(fileLength);
				userNum = rf.getLineNumber();//obtain the number of nodes
			}
			rf.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		// System.out.print(nodeFile);
		System.out.println(" the # of nodes in G:" + userNum);
	}
	public static ArrayList<String> readFile(String FilePath){

        ArrayList<String> lines = new ArrayList<String>();
        int count = 0;

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(FilePath), "UTF-8");

            BufferedReader br = new BufferedReader(isr);
            
            try {

                String line = br.readLine();
                
                while (line != null) {

                    count++;
                    
                    lines.add(line);

                    // if (count % 1000000 == 0) {
                        // System.out.println(count);
                    // }

                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (IOException ex)  
        {
            System.out.println("FileNotFoundException");
            // insert code to run when exception occurs
        } catch (Exception e) {
            System.out.println("Reading **.txt as bufferedReaderhappens");
            System.out.println(e);
        }
        System.out.println(count);

        return lines;
    }
	//return the user's keyword information
	public String[][] readNode(){
		//NOTICE: users[i][0] is the i-th user's name
		double len = 0.0;//count the number of keywords
		String users[][] = new String[userNum + 1][];
		String line = null;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(nodeFile));
			
			while((line = stdin.readLine()) != null){
				line = line.trim();//2015-10-21 bug
				String userIdString = line.substring(0, line.indexOf('\t'));
				int userId = Integer.parseInt(userIdString);
						
				line = line.substring(line.indexOf('\t') + 1);
				String username = line;
				if(line.indexOf('\t') >= 0){ //have keywords
					username = line.substring(0, line.indexOf('\t'));
				}
				line = line.substring(line.indexOf('\t') + 1);

				String kw[] = line.trim().split(" ");
				len += kw.length;
				
				users[userId] = new String[kw.length + 1];
				users[userId][0] = username;
				for(int i = 0;i < kw.length;i ++){
					users[userId][i + 1] = kw[i];
				}
			}
			stdin.close();
		}catch(Exception e){
			System.out.println("line:" + line);
			e.printStackTrace();
		}
		System.out.println("the avg # of keywords in each node:" + (len / userNum));
		return users;
	}
	public String[][] readDBLPNode(){
		//NOTICE: users[i][0] is the i-th user's name
		double len = 0.0;//count the number of keywords
		String users[][] = new String[userNum + 1][];
		Pattern pattern = Pattern.compile("#(.*?)#");
		String[] s = new String[3];
        String line = null;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(nodeFile));
			
			while((line = stdin.readLine()) != null){
				int count = 0;
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					s[count] = matcher.group(1);
					count++;
				}
				
				int userId = Integer.parseInt(s[1]);
				String username = s[0];
				
				String[] kw = {};
				if (!s[2].isEmpty()) {
					kw = s[2].split(";");
				} 
				
				len += kw.length;
				
				users[userId] = new String[kw.length + 1];
				users[userId][0] = username;
				for(int i = 0;i < kw.length;i ++){
					users[userId][i + 1] = kw[i];
				}
			}
			stdin.close();
		}catch(Exception e){
			System.out.println("line:" + line);
			e.printStackTrace();
		}
		System.out.println("the avg # of keywords in each node:" + (len / userNum));
		return users;
	}

	//return the graph edge information
	public int[][] readGraph(){
		int edges = 0;
		int graph[][] = new int[userNum + 1][];
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(graphFile));
						
			String line = null;
			while((line = stdin.readLine()) != null){
				String s[] = line.split(" ");
				int userId = Integer.parseInt(s[0]);
				graph[userId] = new int[s.length - 1];
				for(int i = 1;i < s.length;i ++){
					graph[userId][i - 1] = Integer.parseInt(s[i]);
				}
				edges += graph[userId].length;
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.print(graphFile);
		System.out.println(" the # of edges in G:" + edges);
		System.out.println("the average degree:" + (edges * 1.0 / userNum));
		
		edgeNum = edges / 2;
		
		return graph;
	}
	public int[][] readDBLPGraph(){
		int edges = 0;
		int graph[][] = new int[userNum + 1][];
		HashMap<Integer, ArrayList<Integer>> graphMap = new HashMap<Integer, ArrayList<Integer>>();
		Pattern pattern = Pattern.compile("\\d+");
		int count = 0;
		Integer[] s = new Integer[2];
		ArrayList<String> lines = new ArrayList<String>(readFile(graphFile));
		edgeNum = lines.size();

		for(String line : lines){
			Matcher matcher = pattern.matcher(line);
			count = 0;
        	while (matcher.find()) {
        		s[count] = Integer.parseInt(matcher.group());
				count++;
        	}
			for (int i = 0; i < 2; i++) {
				if (graphMap.containsKey(s[i])) {
					graphMap.get(s[i]).add(s[1-i]);
				} else {
					ArrayList<Integer> edgeSet = new ArrayList();
					edgeSet.add(s[1-i]);
					graphMap.put(s[i], edgeSet);
					edgeSet = null;
				}
			}
		}

		for (int i = 1; i < userNum+1; i++) {
			if (graphMap.containsKey(i)) {
				ArrayList<Integer> edgeset = new ArrayList(graphMap.get(i));
				graph[i] = new int[edgeset.size()];
				count = 0;
				for(int vertex : edgeset){
					graph[i][count] = vertex;
					count++;
				}
				edges += graph[i].length;
			} else {
				graph[i] = new int[0];
			}
		}
			
		System.out.print(graphFile);
		System.out.println(" the # of edges in G:" + edges);
		System.out.println("the average degree:" + (edges * 1.0 / userNum));
		
		
		return graph;
	}
	
	public int getUserNum() {
		return userNum;
	}
	
	public int getEdgeNum(){
		return edgeNum;
	}

	public static void main(String[] args) {
		DataReader dataReader = new DataReader(Config.dataFilePath, Config.dataFilePath);
		String users[][] = dataReader.readNode();
		int graph[][] = dataReader.readGraph();
//		
//		int nodeId = 7786;
//		String kw[] = users[nodeId];
//		for(int i = 0;i < users[nodeId].length;i ++){
//			int neighbor = graph[nodeId][i];
//			String tmpKw[] = users[neighbor];
//			
//			String out = "";
//			for(int j = 0;j < kw.length;j ++){
//				for(int k = 0;k < tmpKw.length;k ++){
//					if(kw[j].equals(tmpKw[k])){
//						out += kw[j] + " ";
//					}
//				}
//			}
//			
//			System.out.println(users[nodeId][0] + " * " + users[neighbor][0] + ": " + out);
//		}
//		System.out.println();
//		
//		System.out.println(graph[nodeId].length);
	}

}
