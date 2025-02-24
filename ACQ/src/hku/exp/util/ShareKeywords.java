package hku.exp.util;

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

public class ShareKeywords {
	private int graph[][];// graph structure
	private String nodes[][];// the keywords of each node
	private TNode root;// the built index
	private int core[];
	private TNode invert[];
	private String ccsFile = null;
	private CCSSaver saver = null;
	private int queryId = -1;
	private AprioriPruner apruner = null;
	private Set<String> seedKwSet = null;
	private long startT = 0;

	public ShareKeywords(int graph[][], String nodes[][], TNode root, int core[], String ccsFile) {
		this.graph = graph;
		this.nodes = nodes;
		this.root = root;
		this.core = core;
		this.ccsFile = ccsFile;
	}

	public int[] query(int queryId){
//		System.out.println("NghQuery-queryId:" + queryId);
		this.queryId = queryId;
		if (core[queryId] < Config.k)   return null;
		this.seedKwSet = new HashSet<String>();
		for(int i = 1;i < nodes[queryId].length;i ++){
			String word = nodes[queryId][i];
			this.seedKwSet.add(word);
		}
		
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

		int rs[] = new int[1000];
		for(int i = 1;i < allLenList.size();i ++){
			List<Integer> list = allLenList.get(i);
			rs[i] = list.size();
		}
		return rs;
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

}
