package hku.prep.dblp;

import hku.Config;
import hku.prep.PrepUser;
import hku.util.STEMExt;
import hku.util.StopFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Jul 20, 2015
 * We sample some researchers
 * For each user, we collect all the keywords and choose the most frequent words
 */
public class CASEPrep {
	private int n = 2000000;
	private PrepUser users[] = null;
	private Map<String, Integer> userMap = null;
	private String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
	
	public CASEPrep(){
		users = new PrepUser[n];
		userMap = new HashMap<String, Integer>();
	}
	
	public void prep(){
		//step 1: read and group information by userID
		read();
		
		//step 2: select 10% frequent keywords and extract each user's keywords
		selectTopKw();
		
		//step 3: output a graph and a list of user profiles
		output();
	}
	
	//step 1: read and group information by userID
	private void read(){
		try{
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
						if(userMap.containsKey(author) == false){
							int userId = userMap.size() + 1; //starting from 1
							userMap.put(author, userId);
							users[userId] = new PrepUser(userId, author);
							list.add(userId);
						}else{
							list.add(userMap.get(author));
						}
					}
					
					//(2) update each user's own information
					for(int i = 0;i < list.size();i ++){
						int userId = list.get(i);
						users[userId].getKeySet().add(title);
						for(int j = 0;j < list.size();j ++){
							if(list.get(j) != userId){//skip itself
								users[userId].getEdgeSet().add(list.get(j));
							}
						}
					}
				}
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//statistic information
		int userNum = userMap.size();
		int edgeNum = 0;
		for(int i = 0;i < users.length;i ++){
			if(users[i] != null){
				edgeNum += users[i].getEdgeSet().size();
			}
		}
		
		//In original dataset, userNum:977288  edgeNum:7841834  Reynold Cheng:15856
		System.out.println("In original dataset, userNum:" + userNum + "  edgeNum:" + edgeNum);
		System.out.println("Reynold Cheng:" + userMap.get("reynold cheng"));
	}

	//step 2: select 10% frequent keywords and extract each user's keywords
	private void selectTopKw(){
		STEMExt stem = new STEMExt();
		StopFilter stop = new StopFilter();
		
		//step 0: sample some users 10000 active users
		int selectedUser = 0;
		for(int i = 0;i < n;i ++){
			PrepUser user = users[i];
			if(user != null){
				if(user.getKeySet().size() < 5){//publish more than 50 papers
					users[i] = null;
				}else{
					selectedUser += 1;
				}
			}
		}
		System.out.println("selectedUser: " + selectedUser);
		
		//step 1: count the frequency
		for(int i = 0;i < n;i ++){
			PrepUser user = users[i];
			
			if(user != null){
				Iterator<String> iter = user.getKeySet().iterator();
				Map<String, Integer> kwMap = new HashMap<String, Integer>();
			    while(iter.hasNext()){
			    	String title = iter.next();
			    	String s[] = title.split(" ");
					for(int j = 0;j < s.length;j ++){
						String word = s[j];
						word = filerSpec(word); //filter special symbols
						
						//only consider words that are stems and not stop words
						word = stem.extSTEM(word);
						if(stop.contains(word) == false){
							if(kwMap.containsKey(word)){
								int freq = kwMap.get(word);
								kwMap.put(word, freq + 1);
							}else{
								kwMap.put(word, 1);
							}
						}
					}
					
			    }
			    
			    Set<String> set = new HashSet<String>();
			    for(int j = 0;j < Config.topKw;j ++){
			    	int max = -1;
			    	String word = "";
			    	for(Map.Entry<String, Integer> entry:kwMap.entrySet()){
			    		if(entry.getValue() > max){
			    			max = entry.getValue();
			    			word = entry.getKey();
			    		}
			    	}
			    	
			    	if(max >= 0){
			    		set.add(word);
			    		kwMap.remove(word);
			    	}
			    }
			    user.setKeySet(set);//the keyword set has been updated from titles to words
			}
		}
	}
	
	//step 3: output a graph and a list of user profiles
	private void output(){
		//step 1: output the information about nodes
		Map<Integer, Integer> userIDMap = new HashMap<Integer, Integer>(); //map the seleted userIDs into a consecutive space
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.caseNode));
			
			int newUserID = 1;
			for(int i = 0;i < n;i ++){
				PrepUser user = users[i];
				if(user != null){
					String line = newUserID + "\t" + user.getUsername() + "\t";
					userIDMap.put(user.getUserID(), newUserID);//map the original userID to new userID
					newUserID += 1;
					
					Iterator<String> iter = user.getKeySet().iterator();
				    while(iter.hasNext()){
				    	String word = iter.next();
				    	line += word + " ";
				    }
				    
				    stdout.write(line);
				    stdout.newLine();
				}
			}
			stdout.flush();
			stdout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//step 2: output the graph (nodeId, its neighbors)
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.caseGraph));
			
			int edge = 0;
			for(int i = 0;i < n;i ++){
				PrepUser user = users[i];
				if(user != null && userIDMap.containsKey(user.getUserID())){
					String line = userIDMap.get(user.getUserID()) + "";
					
					Iterator<Integer> iter = user.getEdgeSet().iterator();
				    while(iter.hasNext()){
				    	int neighbor = iter.next();
				    	if(users[neighbor] != null){//She/He appears in the original file
				    		if(userIDMap.containsKey(neighbor)){
				    			line += " " + userIDMap.get(neighbor);
					    		edge += 1;
				    		}
				    	}
				    }
				    
				    stdout.write(line);
				    stdout.newLine();
				}
			}
			stdout.flush();
			stdout.close();
			System.out.println("edges: " + edge);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	//filter special characters
	private String filerSpec(String word){
		String rs = "";
		for(int i = 0;i < word.length();i ++){
			char c = word.charAt(i);
			if(c >= 'a' && c <= 'z'){
				rs += c;
			}
			if(c >= 0 && c <= 9){
				rs += c;
			}
		}
		return rs;
	}
	
	public static void main(String[] args) {
		CASEPrep prep = new CASEPrep();
		prep.prep();
	}

}
