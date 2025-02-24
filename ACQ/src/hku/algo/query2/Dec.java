package hku.algo.query2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import hku.Config;
import hku.algo.AprioriPruner;
import hku.algo.FindCCS;
import hku.algo.TNode;
import hku.util.CCSSaver;
import hku.algo.*;

import java.util.*;

public class Dec {
	private int graph[][];// graph structure
	private String nodes[][];// the keywords of each node
	private TNode root;// the built index
	private int core[];
	private TNode invert[];
	private String ccsFile = null;
//	private CCSSaver saver = null;
	private int queryId = -1;
	private AprioriPruner apruner = null;
	private Set<String> seedKwSet = null;
	private long startT = 0;

	public Dec(int graph[][], String nodes[][], TNode root, int core[], String ccsFile) {
		this.graph = graph;
		this.nodes = nodes;
		this.root = root;
		this.core = core;
		this.ccsFile = ccsFile;
	}

	public List<Set<Integer>> query(int queryId){
//		System.out.println("Dec queryId:" + queryId);
//		this.startT = System.currentTimeMillis();
		this.queryId = queryId;
		int qualifiedCC = 0;
		List<Set<Integer>> ccsList = new ArrayList<>();
		if (core[queryId] < Config.k)   return null;
		
		// step 1: mine neighbor information
		List<Integer> nghList = new ArrayList<Integer>();
		for(int nghId:graph[queryId])   if(core[nghId] >= Config.k)   nghList.add(nghId);
		FPGrowthLocalFilter localFilter = new FPGrowthLocalFilter(graph, nodes, core, queryId, nghList);
		List<List<String[]>> candKwList = localFilter.mine();
		if (Config.kwLength == -1) {
			Config.kwLength = candKwList.size();
		}
		this.seedKwSet = localFilter.getSeedKwSet();
		if(this.seedKwSet.size() == 0)   return null;
//		System.out.println("candKwList.size=" + candKwList.size() + " seedKwSet.size=" + seedKwSet.size()
//				+ " time:" + (System.currentTimeMillis() - startT));
		
		
		
		
		
		
		// step 2: locate the node in the cck-core tree
		TNode subRoot = locateAllCK(root);

		// step 3: build the length->nodeList
		Map<Integer, List<String>> idShareWordMap = new HashMap<Integer, List<String>>();// id->wordList
		fillLenMap(subRoot, idShareWordMap);
		List<List<Integer>> allLenList = new ArrayList<List<Integer>>();//len: idList
		for(int i = 0;i <= seedKwSet.size();i ++){
			List<Integer> list = new ArrayList<Integer>();
			allLenList.add(list);
		}
		for(Map.Entry<Integer, List<String>> entry:idShareWordMap.entrySet()){
			int len = entry.getValue().size();
			allLenList.get(len).add(entry.getKey());
		}
//		System.out.println("we have filled the allLenList:" + (System.currentTimeMillis() - startT));
		
		// step 4: initialize candNodeSet, which contains all the nodes sharing at least XX keywords
		Map<String, List<Integer>> invMap = new HashMap<String, List<Integer>>();//2015-10-15
		for (int len = Config.kwLength; len < allLenList.size(); len++) {
			List<Integer> nodeList = allLenList.get(len);
			for(int id:nodeList){
				List<String> shareWordList = idShareWordMap.get(id);
				for(String word:shareWordList){
					if(invMap.containsKey(word)){
						invMap.get(word).add(id);
					}else{
						List<Integer> list = new ArrayList<Integer>();
						list.add(id);
						invMap.put(word, list);
					}
				}
			}
		}
		
		// step 5: search starting from longer keyword combinations
		for (int len = Config.kwLength - 1; len >= 1; len--) {
//			System.out.println("Dec queryId:" + queryId + " kws.len:" + len + " size:" + candKwList.get(len).size());
			
			List<Integer> nodeList = allLenList.get(len);
			for(int id:nodeList){
				List<String> shareWordList = idShareWordMap.get(id);
				for(String word:shareWordList){
					if(invMap.containsKey(word)){
						invMap.get(word).add(id);
					}else{
						List<Integer> list = new ArrayList<Integer>();
						list.add(id);
						invMap.put(word, list);
					}
				}
			}
			
			List<String[]> kwList = candKwList.get(len);
			ccsList = findCCS(invMap, kwList);
			if(ccsList.size() > 0){
				qualifiedCC = ccsList.size();
				// System.out.println("we have found " + ccsList.size() + " acs len:" + len);
				Config.kwLength = len + 1;
				break;
			}
		}
		return ccsList;
	}

	// locate a list of tnodes, each of which has (1)coreness>=Config.k and (2) contains queryId
	private TNode locateAllCK(TNode root) {
		// step 1: find nodes with coreNumber=Config.k using BFS
		List<TNode> candRootList = new ArrayList<TNode>();
		Queue<TNode> queue = new LinkedList<TNode>();
		queue.add(root);

		while (queue.size() > 0) {
			TNode curNode = queue.poll();
			for (TNode tnode : curNode.getChildList()) {
				if (tnode.getCore() < Config.k) {
					queue.add(tnode);
				} else {// the candidate root node must has coreness at least Config.k
					candRootList.add(tnode);
				}
			}
		}
//		System.out.println("candRootList.size:" + candRootList.size());

		// step 2: locate a list of ck-cores
		for (TNode tnode : candRootList) {
			if (findCK(tnode)) {
				return tnode;
			}
		}

		return null;
	}
	// check whether a subtree rooted at "root" contains queryId or not
	private boolean findCK(TNode root) {
		if (root.getCore() <= core[queryId]) {
			boolean rs = false;
			if (root.getNodeSet().contains(queryId)) {
				rs = true;
			} else {
				for (TNode tnode : root.getChildList()) {
					if (findCK(tnode)) {
						rs = true;
						break;
					}
				}
			}
			return rs;
		} else {
			return false;
		}
	}

	private void fillLenMap(TNode root, Map<Integer, List<String>> idShareWordMap) {
		// step 1: compute the number of shared keywords
		Map<String, int[]> kwMap = root.getKwMap();
		
		for (String kw : seedKwSet) {// consider each candidate
			if (kwMap.containsKey(kw)) {
				// the number of shared keywords
				for (int id : kwMap.get(kw)) {
					if (idShareWordMap.containsKey(id)) {
						idShareWordMap.get(id).add(kw);
					} else {
						List<String> list = new ArrayList<String>();
						list.add(kw);
						idShareWordMap.put(id, list);
					}
				}
			}
		}

		// step 3: traverse the sub-tree
		for (int i = 0; i < root.getChildList().size(); i++) {
			TNode tnode = root.getChildList().get(i);
			fillLenMap(tnode, idShareWordMap);
		}
	}
	
	private List<Set<Integer>> findCCS(Map<String, List<Integer>> invMap, List<String[]> kwList) {
		List<Set<Integer>> rsList = new ArrayList<>();
				
		// step 1: verify each keyword combination
		for (String kws[] : kwList) {
//			System.out.print("Dec we are considering: [" + kws[0]);
//			for (int ii = 1; ii < kws.length; ii++)   System.out.print(" " + kws[ii]);
//			System.out.println("] time:" + (System.currentTimeMillis() - startT));

			//select nodes satisfying the keyword constraint
			Set<Integer> nodeSet = new HashSet<Integer>(invMap.get(kws[0]));
			for(int i = 1;i < kws.length;i ++){
				Set<Integer> tmpSet = new HashSet<Integer>();
				for(int id:invMap.get(kws[i])){
					if(nodeSet.contains(id)){
						tmpSet.add(id);
					}
				}
				nodeSet = tmpSet;
			}
			
			// count the number of nodes and edges
			FindCC findCC = new FindCC(graph, nodeSet, queryId);
			Set<Integer> ccNodeSet = findCC.findCC();
			int nodeNum = ccNodeSet.size();
			int edgeNum = findCC.getEdge();
			
			//find the ccs
			Set<Integer> ccsSet = new HashSet<>();
			if(edgeNum - nodeNum >= (Config.k * Config.k - Config.k) / 2 - 1){
				List<Integer> curList = new ArrayList<Integer>();//this list serves as a map (newID -> original ID)
				curList.add(-1);//for consuming space purpose
				curList.addAll(ccNodeSet);
				
				FindCCS finder = new FindCCS(graph, curList, queryId);
				ccsSet = finder.findRobustCCS();
				if(ccsSet.size() > 1){
					rsList.add(ccsSet);
					// System.out.println("A community with size = " + ccsSet.size() + "   Time cost:" + (System.currentTimeMillis() - startT));
				}
			}
		}
		
		return rsList;
	}

}
