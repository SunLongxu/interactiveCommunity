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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Jul 20, 2015
 * We collect all the keywords and choose the most frequent words
 */
public class DBLPPrep {
	private int n = 979134;
	private PrepUser users[] = null;
	private Map<String, Integer> userMap = null;
	private Map<String, Integer> kwMap = null;
//	private String path = "/home/fangyixiang/Desktop/CCS/dblp/tmp.txt";
	private String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
	
	public DBLPPrep(){
		users = new PrepUser[n];
		userMap = new HashMap<String, Integer>();
		kwMap = new HashMap<String, Integer>();
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
							int userId = userMap.size();
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
							users[userId].getEdgeSet().add(list.get(j));
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
//		for(Map.Entry<String, Integer> entry:userMap.entrySet()){   
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
//		}
	}

	//step 2: select 10% frequent keywords and extract each user's keywords
	private void selectTopKw(){
		STEMExt stem = new STEMExt();
		StopFilter stop = new StopFilter();
		
		//step 1: count the frequency
		for(int i = 0;i < n;i ++){
			PrepUser user = users[i];
			
			Set<String> set = new HashSet<String>();
			Iterator<String> iter = user.getKeySet().iterator();
		    while(iter.hasNext()){
		    	String title = iter.next();
		    	String s[] = title.split(" ");
				for(int j = 0;j < s.length;j ++){
					String word = s[j];
					word = word.toLowerCase();
					word = filerSpec(word); //filter special symbols
					
					//only consider words that are stems and not stop words
					if(stop.contains(word) == false){
						word = stem.extSTEM(word);
						if(kwMap.containsKey(word)){
							int freq = kwMap.get(word);
							kwMap.put(word, freq + 1);
						}else{
							kwMap.put(word, 1);
						}
						set.add(word);
					}
				}
		    }
		    user.setKeySet(set);//the keyword set has been updated from titles to words
		}
		
		//step 2: extract the most frequent keywords
		Comparator<Integer> OrderIsdn =  new Comparator<Integer>(){  
            public int compare(Integer o1, Integer o2) {  
                if(o1.intValue() > o2.intValue()){
                	return 1;
                }else if(o1.intValue() < o2.intValue()){
                	return -1;
                }else{
                	return 0;
                }
            }
        };  
        
        //we select words, whose frequences are higher than top
        int top = (int)(kwMap.size() * Config.kwFreq);
        System.out.println("Config.kwFreq:" + Config.kwFreq + "   top:" + top);
        Queue<Integer> queue =  new PriorityQueue<Integer>(top,OrderIsdn);
        for(Map.Entry<String, Integer> entry:kwMap.entrySet()){
			queue.add(entry.getValue());
			if(queue.size() > top){
				queue.poll();
			}
		}
        
        int freqThreshold = queue.peek();//the frequency of top-th word
		Map<String, Integer> mp = new HashMap<String, Integer>();
		for(Map.Entry<String, Integer> entry:kwMap.entrySet()){
			if(entry.getValue() > freqThreshold){
				mp.put(entry.getKey(), entry.getValue());
			}
		}
		kwMap = mp;//the key-value map has been updated
		
		
		//step 3: extract the keywords for each user
		for(int i = 0;i < n;i ++){
			PrepUser user = users[i];
			
			Set<String> set = new HashSet<String>();
			Iterator<String> iter = user.getKeySet().iterator();
		    while(iter.hasNext()){
		    	String word = iter.next();
		    	if(kwMap.containsKey(word)){
		    		set.add(word);
		    	}
		    }
		    user.setKeySet(set);
		}
	}
	
	//step 3: output a graph and a list of user profiles
	private void output(){
		//step 1: output the information about nodes
		try{
			BufferedWriter stdout = new BufferedWriter(new FileWriter(Config.dblpNode));
			
			for(int i = 0;i < n;i ++){
				PrepUser user = users[i];
				String line = user.getUserID() + "\t" + user.getUsername();
				
				Iterator<String> iter = user.getKeySet().iterator();
			    while(iter.hasNext()){
			    	String word = iter.next();
			    	line += "\t" + word;
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
			
			for(int i = 0;i < n;i ++){
				PrepUser user = users[i];
				String line = user.getUserID() + "";
				
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
		DBLPPrep prep = new DBLPPrep();
		prep.prep();
	}

}
