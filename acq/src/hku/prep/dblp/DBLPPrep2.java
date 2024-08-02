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
 * For each user, we collect all the keywords and choose the most frequent words
 */
public class DBLPPrep2 {
	private int n = 1000000;
	private PrepUser users[] = null;
	private Map<String, Integer> userMap = null;
	private String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
	
	public DBLPPrep2(){
		users = new PrepUser[n + 1];
		userMap = new HashMap<String, Integer>();
	}
	
	public void prep(){
		//step 1: read and group information by userID
		read();
		n = userMap.size();//the acutual number of users
		
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
		
		System.out.println("userMap.size:" + userMap.size());
	}

	//step 2: select 10% frequent keywords and extract each user's keywords
	private void selectTopKw(){
		STEMExt stem = new STEMExt();
		StopFilter stop = new StopFilter();
		
		//step 1: count the frequency
		for(int i = 1;i <= n;i ++){
			PrepUser user = users[i];
			
			Iterator<String> iter = user.getKeySet().iterator();
			Map<String, Integer> kwMap = new HashMap<String, Integer>();
		    while(iter.hasNext()){
		    	String title = iter.next();
		    	String s[] = title.trim().split(" ");
				for(int j = 0;j < s.length;j ++){
					String word = s[j].trim();
					word = word.toLowerCase();
					word = filerSpec(word); //filter special symbols
					word = stem.extSTEM(word); //extract its stem
					
					if(word != null && word.length() > 0){
						//only consider words that are stems and not stop words
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
		    }
		    
		    Set<String> set = new HashSet<String>();
		    int tmp = 0;
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
		    		tmp += 1;
		    		if(user.getUserID() == 15237){
		    			System.out.println("idx:" + tmp + " word:" + word);
		    		}
		    	}
		    }
		    user.setKeySet(set);//the keyword set has been updated from titles to words
		}
	}
	
	//step 3: output a graph and a list of user profiles
	private void output(){
		//step 1: output the information about nodes
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.dblpNode));
			
			for(int i = 1;i <= n;i ++){
				PrepUser user = users[i];
				String line = user.getUserID() + "\t" + user.getUsername() + "\t";
				
				Iterator<String> iter = user.getKeySet().iterator();
			    while(iter.hasNext()){
			    	String word = iter.next();
			    	line += word + " ";
			    }
			    
			    if(user.getUsername().equals("jiawei han")){
			    	System.out.println("jiawei han's id:" + user.getUserID());
			    	System.out.println("jiawei han's keywords:" + line);
			    }
			    
			    if(user.getUserID() == 15237){
			    	System.out.println("15237.keywords:" + user.getKeySet().size());
			    }
			    
			    stdout.write(line);
			    stdout.newLine();
			}
			stdout.flush();
			stdout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//step 2: output the graph (nodeId, its neighbors)
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.dblpGraph));
			
			for(int i = 1;i <= n;i ++){
				PrepUser user = users[i];
				String line = user.getUserID() + "";
				
//				if(user.getEdgeSet().size() == 0){
//					System.out.println("warn: a node has 0 neighbors.");
//				}
				
				Iterator<Integer> iter = user.getEdgeSet().iterator();
			    while(iter.hasNext()){
			    	int neighbor = iter.next();
			    	line += " " + neighbor;
			    }
			    
			    stdout.write(line);
			    stdout.newLine();
			}
			stdout.flush();
			stdout.close();
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
		DBLPPrep2 prep = new DBLPPrep2();
		prep.prep();
	}

}
