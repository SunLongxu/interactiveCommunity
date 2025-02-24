package hku.algo;

import hku.Config;

import java.util.*;

/**
 * @author fangyixiang
 * @date Sep 10, 2015
 * Given a subgraph, find a CCS
 */
public class FindCCS {
	private int graph[][];
	private List<Integer> curList;
	private int queryId;
	
	public FindCCS(int graph[][], List<Integer> curList, int queryId){
		this.graph = graph;
		this.curList = curList;
		this.queryId = queryId;
	}
	
	public Set<Integer> findRobustCCS(){
		Set<Integer> ccsSet = new HashSet<Integer>();
		
		//step 1: build a sub-graph on curList
		Set<Integer> fangSet = new HashSet<Integer>();//2016-10-19, specially for checking existence
		fangSet.addAll(curList);
		Map<Integer, Integer> oldToNewMap = new HashMap<Integer, Integer>(curList.size() - 1);
		for(int i = 1;i < curList.size();i ++){
			int old = curList.get(i);
			oldToNewMap.put(old, i);
		}

		int newN = curList.size(); //newN = nodeNum + 1
		int subGraph[][] = new int[newN][];
		for(int i = 1;i < curList.size();i ++){
			int old = curList.get(i);
			int arr[] = graph[old];
			
			int nghCount = 0;
			boolean flag[] = new boolean[arr.length];//consider its original neighbors one by one
			for(int j = 0;j < graph[old].length;j ++){
				int oldNeighbor = graph[old][j];
				
				//2015-10-19, ArrayList is quite slow !!!
				if(fangSet.contains(oldNeighbor)){
					flag[j] = true;
					nghCount += 1;
				}
			}
			
			//convert the neighbor list to an array
			int newNeighborArr[] = new int[nghCount];
			int idx = 0;
			for(int j = 0;j < flag.length;j ++){
				if(flag[j]){
					int newNeighbor = oldToNewMap.get(graph[old][j]);
					newNeighborArr[idx] = newNeighbor;
					idx += 1;
				}
			}
			subGraph[i] = newNeighborArr;
		}
		
		//step 2: find a k-core
//		KCore kc = new KCore(subGraph);
//		int subCore[] = kc.decompose();
		FindKCore fkc = new FindKCore(subGraph, Config.k);
		int subCore[] = fkc.decompose();
		
		FindCKCore finder = new FindCKCore();
		int rsArray[] = finder.findCKCore(subGraph, subCore, oldToNewMap.get(queryId));
		
		if(rsArray.length > 1){
			for(int i = 0;i < rsArray.length;i ++){
				int newID = rsArray[i];
				ccsSet.add(curList.get(newID));
			}
		}
		
		return ccsSet;
	}
}
