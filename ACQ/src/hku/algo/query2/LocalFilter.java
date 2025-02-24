package hku.algo.query2;

import hku.Config;
import hku.algo.AprioriPruner;
import hku.algo.DataReader;
import hku.algo.TNode;
import hku.algo.index.BasicIndex;
import hku.util.Tool;

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
public class LocalFilter {
	private int graph[][];
	private String nodes[][];
	private int queryId = -1;
	private List<Integer> nghList;
	private Map<String, Set<Integer>> invertMap = null;
	private Set<String> seedKwSet = null;
	private AprioriPruner apruner = null;
	
	public LocalFilter(int graph[][], String nodes[][], int queryId, List<Integer> nghList){
		this.graph = graph;
		this.nodes = nodes;
		this.nghList = nghList;
		this.invertMap = new HashMap<String, Set<Integer>>();
		this.seedKwSet = new HashSet<String>();
		
		//queryId's keyword set
		Set<String> kwSet = new HashSet<String>();
		for(int i = 1;i < nodes[queryId].length;i ++){
			kwSet.add(nodes[queryId][i]);
		}
		
		//build the inverted list
		for(int nghId:nghList){
			for(int i = 1;i < nodes[nghId].length;i ++){
				String kw = nodes[nghId][i];
				if(kwSet.contains(kw)){
					if(invertMap.containsKey(kw) == false){
						Set<Integer> set = new HashSet<Integer>();
						set.add(nghId);
						invertMap.put(kw, set);
					}else{
						Set<Integer> set = invertMap.get(kw);
						set.add(nghId);
					}
				}
			}
		}
	}
	
	//mine all the candidate patterns using neighbor infomation
	public List<List<String[]>> mine(){
		List<List<String[]>> rsList = new ArrayList<List<String[]>>();
		rsList.add(null);//it takes space for length-0 keyword combination
		
		List<String[]> validKwList = new ArrayList<String[]>();
		List<Set<Integer>> ccsList = new ArrayList<Set<Integer>>();
		for(Map.Entry<String, Set<Integer>> entry:invertMap.entrySet()){
			if(entry.getValue().size() >= Config.k){
				String kws[] = {entry.getKey()};
				validKwList.add(kws);
				ccsList.add(entry.getValue());
				seedKwSet.add(entry.getKey());
			}
		}
		rsList.add(validKwList);
		System.out.println("length-1" + "  validKwList.size=" + validKwList.size() + "  ccsList.size=" + ccsList.size());
		
		for(int iterK = 1;;iterK ++){
			List<String[]> newValidKwList = new ArrayList<String[]>();
			List<Set<Integer>> newCcsList = new ArrayList<Set<Integer>>();
			
			//step 1: generate candidates
			Tool tool = new Tool();
			apruner = new AprioriPruner(validKwList);
			for(int i = 0;i < validKwList.size();i ++){
				for(int j = i + 1;j < validKwList.size();j ++){
					String kws1[] = validKwList.get(i);   Set<Integer> set1 = ccsList.get(i);
					String kws2[] = validKwList.get(j);   Set<Integer> set2 = ccsList.get(j);
					if(iterK == 1){
						String newKws[] = {kws1[0], kws2[0]};
						if(kws1[0].compareTo(kws2[0]) > 0){
							newKws[0] = kws2[0];
							newKws[1] = kws1[0];
						}
						
						//verfication
						Set<Integer> newSet = new HashSet<Integer>();
						for(int id:set1)   if(set2.contains(id))   newSet.add(id);
						if(newSet.size() >= Config.k){
							newValidKwList.add(newKws);
							newCcsList.add(newSet);
						}
					}else{
						boolean isCand = true;
						for(int ij = 0;ij < iterK - 1;ij ++){
							if(kws1[ij].equals(kws2[ij]) == false){
								isCand = false;
								break;
							}
						}

						if(isCand){
							String newKws[] = new String[iterK + 1];
							for(int ij = 0;ij < iterK;ij ++)   newKws[ij] = kws1[ij];
							newKws[iterK] = kws2[iterK - 1];
							newKws = tool.sortKw(newKws); //sort the keywords
							
							//verfication
							Set<Integer> newSet = new HashSet<Integer>();
							for(int id:set1)   if(set2.contains(id))   newSet.add(id);
							if(newSet.size() >= Config.k){
								newValidKwList.add(newKws);
								newCcsList.add(newSet);
							}
						}
					}
				}
			}
			
			if(newValidKwList.size() > 0){
				System.out.println("iterK:" + iterK + " size:" + newValidKwList.size());
				rsList.add(newValidKwList);
				validKwList = newValidKwList;
				ccsList = newCcsList;
//				System.out.println("length=" + (iterK + 1) + "  validKwList.size=" + validKwList.size() + "  ccsList.size=" + ccsList.size());
			}else{
				break;
			}
		}
		return rsList;
	}
	
	public Set<String> getSeedKwSet() {
		return seedKwSet;
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
		LocalFilter localFilter = new LocalFilter(graph, nodes, queryId, nghList);
		localFilter.mine();
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}
}
