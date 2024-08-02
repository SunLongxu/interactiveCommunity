package hku.algo.query2;

import hku.Config;
import hku.algo.AprioriPruner;
import hku.algo.DataReader;
import hku.algo.TNode;
import hku.algo.index.BasicIndex;
import hku.algo.query2.fpMine.AlgoFPGrowth;
import hku.algo.query2.fpMine.Itemset;
import hku.algo.query2.fpMine.Itemsets;
import hku.util.Tool;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * @author fangyixiang
 * @date Aug 23, 2015
 * The neighbor information is used in the query
 */
public class FPGrowthLocalFilter {
	private int graph[][];
	private String nodes[][];
	private int core[];
	private int queryId = -1;
	private Set<String> seedKwSet = null;
	
	public FPGrowthLocalFilter(int graph[][], String nodes[][], int core[], int queryId, List<Integer> nghList){
		this.graph = graph;
		this.nodes = nodes;
		this.core = core;
		this.queryId = queryId;
		this.seedKwSet = new HashSet<String>();
	}
	
	//mine all the candidate patterns using neighbor infomation
	public List<List<String[]>> mine(){
		List<List<String[]>> candList = new ArrayList<List<String[]>>();
		candList.add(null);//it takes space for length-0 keyword combination
		
		//step 1: find queryId's keyword set
		Set<String> kwSet = mineWord(queryId, null);//mine keywords
		if(kwSet == null || kwSet.size() == 0)   return candList;
		
		//step 2: obtain the transactions
		int itemIndex = 0;
		Map<String, Integer> wordIntMap = new HashMap<String, Integer>();//word -> int
		Map<Integer, String> intWordMap = new HashMap<Integer, String>();//int -> word
		List<List<Integer>> transList = new ArrayList<List<Integer>>();
		for(int i = 0;i < graph[queryId].length;i ++){
			int neighbor = graph[queryId][i];
			Set<String> nkwSet = mineWord(neighbor, kwSet);//mine keywords
			if(nkwSet == null)   continue;
			
			List<Integer> list = new ArrayList<Integer>();
			for(String word:nkwSet){
				if(kwSet.contains(word)){
					if(wordIntMap.containsKey(word)){
						int item = wordIntMap.get(word);
						list.add(item);
					}else{
						wordIntMap.put(word, itemIndex);
						intWordMap.put(itemIndex, word);//add
						
						list.add(itemIndex);
						itemIndex += 1;
					}
				}
			}
			if(list.size() > 0)   transList.add(list);
		}
		
//		System.out.println("LocalFilter:" + nodes[69969].length + " transList.size:" + transList.size());
//		for(List<Integer> list:transList){
//			System.out.println("transaction.size:" + list.size());
//		}
		
//		for(List<Integer> list:transList){
//			for(int item:list){
//				System.out.print(item + " ");
//			}
//			System.out.println();
//		}
		
		//step 3: mine fp
		int minsupp = Config.k;
		AlgoFPGrowth algo = new AlgoFPGrowth();
//		algo.printStats();
		try{
			Itemsets itemsets = algo.runAlgorithm(transList, minsupp);
			List<List<Itemset>> rsList = itemsets.getLevels();
			
			List<List<Itemset>> patternList = itemsets.getLevels();
			for(int i = 1;i < rsList.size();i ++){
				List<Itemset> list = patternList.get(i);
//				System.out.println("-----------level:" + i + " size:" + list.size() + "--------------");
				
				List<String[]> levelPatternList = new ArrayList<String[]>();
				for(int j = 0;j < list.size();j ++){
					Itemset itemset = list.get(j);
					int arr[] = itemset.itemset;
					
					//this tmpList is used for ranking
					List<String> tmpList = new ArrayList<String>();
					for(int k = 0;k < arr.length;k ++){
						String word = intWordMap.get(arr[k]);
						tmpList.add(word);
					}
					Collections.sort(tmpList, new Comparator<String>() {
						public int compare(String item1, String item2) {
							return item1.compareTo(item2);
						}
					});
					
					//obatin a keyword combination
					String kws[] = new String[arr.length];
					for(int k = 0;k < tmpList.size();k ++){
						kws[k] = tmpList.get(k);
					}
					
					
					levelPatternList.add(kws);
					
//					for(String word:kws)   System.out.print(word + " ");
//					System.out.println();
				}
				candList.add(levelPatternList);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
//		System.out.println("FP finished!");
		
		//update seed by length one pattern
		if(candList.size() >= 2){
			for(String kws[]:candList.get(1)){
				seedKwSet.add(kws[0]);
			}
		}
		
		return candList;
	}
	
	public Set<String> getSeedKwSet() {
		return seedKwSet;
	}

	//mine keywords
	public Set<String> mineWord(int nodeId, Set<String> kwSet){
		//each neighbor should have core number being at least k
		if(core[nodeId] < Config.k)   return null;
		
		//nodeId's keyword set
		Set<String> curSet = new HashSet<String>();
		if(kwSet == null){//for the query vertex
			for(int i = 1;i < nodes[nodeId].length;i ++){
				String word = nodes[nodeId][i];
				curSet.add(word);
			}
		}else{//for the neighbors of the query vertex
			for(int i = 1;i < nodes[nodeId].length;i ++){
				String word = nodes[nodeId][i];
				if(kwSet.contains(word)){
					curSet.add(word);
				}
			}
		}
		if(curSet.size() == 0)   return null;
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0;i < graph[nodeId].length;i ++){
			int neighbor = graph[nodeId][i];
			if(core[neighbor] >= Config.k){
				for(int j = 1;j < nodes[neighbor].length;j ++){
					String word = nodes[neighbor][j];
					if(curSet.contains(word)){
						if(map.containsKey(word)){
							map.put(word, map.get(word) + 1);
						}else{
							map.put(word, 1);
						}
					}
				}
			}
		}
		
		//frequency >= Config.k
		Set<String> rsSet = new HashSet<String>();
		for(Map.Entry<String, Integer> entry:map.entrySet()){
			if(entry.getValue() >= Config.k){
				rsSet.add(entry.getKey());
			}
		}
		return rsSet;
	}
	
	public static void main(String args[]){
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		BasicIndex index = new BasicIndex(graph, nodes);
		TNode root = index.build();
		int core[] = index.getCore();
		
//		int queryId = 15238;
		int queryId = 805308;
		List<Integer> nghList = new ArrayList<Integer>();
		for(int i = 0;i < graph[queryId].length;i ++){
			int nghId = graph[queryId][i];
			if(core[nghId] >= Config.k){
				nghList.add(nghId);
			}
		}
		System.out.println("nghList.size=" + nghList.size());
		
		long t1 = System.currentTimeMillis();
		FPGrowthLocalFilter localFilter = new FPGrowthLocalFilter(graph, nodes, core, queryId, nghList);
		localFilter.mine();
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}
}
