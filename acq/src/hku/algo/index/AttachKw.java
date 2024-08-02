package hku.algo.index;

import hku.algo.TNode;
import hku.util.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Aug 17, 2015
 * Build the inverted index for each node in the index
 */
public class AttachKw {
	private String nodes[][];
	
	public AttachKw(String nodes[][]){
		this.nodes = nodes;
	}
	
	public TNode attach(TNode root){
		//step 1: build the inverted index of this root node
		Set<Integer> nodeSet = root.getNodeSet();
		
		if(nodeSet.size() > 0){
			Iterator<Integer> iter = nodeSet.iterator();
			Map<String, Integer> kwMap = new HashMap<String, Integer>();
			List<List<Integer>> invertList = new ArrayList<List<Integer>>();
			int kwIndex = 0;
			while(iter.hasNext()){
				int nodeId = iter.next();
				String kw[] = nodes[nodeId];
				for(int j = 1;j < kw.length;j ++){//consider all the keywords
					if(kwMap.containsKey(kw[j]) == false){//a new keyword
						kwMap.put(kw[j], kwIndex);
						kwIndex += 1;
						
						List<Integer> list = new ArrayList<Integer>();
						list.add(nodeId);
						invertList.add(list);
					}else{//this keyword has appeared before
						int idx = kwMap.get(kw[j]);
						invertList.get(idx).add(nodeId);
					}
				}
			}
			
			Map<String, int[]> invertMap = new HashMap<String, int[]>();
			for(Entry<String, Integer> entry:kwMap.entrySet()){
				int index = entry.getValue();
				List<Integer> list = invertList.get(index);
				
				int arr[] = new int[list.size()];
				for(int j = 0;j < list.size();j ++){
					arr[j] = list.get(j);
				}
				invertMap.put(entry.getKey(), arr);
			}
			
			root.setKwMap(invertMap);
		}
		
		
		//step 2: build the index for its child nodes
		for(TNode node:root.getChildList()){
			attach(node);
		}
		
		return root;
	}
}
