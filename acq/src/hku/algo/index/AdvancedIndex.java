package hku.algo.index;

import hku.algo.DataReader;
import hku.algo.KCore;
import hku.algo.TNode;
import hku.algo.index.unionFind.UNode;
import hku.algo.index.unionFind.UnionFind;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author fangyixiang
 * @date Sep 17, 2015
 * build the index using union-find data structure: correct, union later
 */
public class AdvancedIndex {
	private String nodes[][] = null;
	private int graph[][] = null;
	private int core[] = null;
	private int n = -1;
	private int coreReverseFang[] = null;
	private UNode unodeArr[] = null;
	private TNode invert[] = null;
	private Set<TNode> restNodeSet = null;
	private UnionFind uf = null;;

	
	public AdvancedIndex(int graph[][], String nodes[][]){
		this.graph = graph;
		this.nodes = nodes;
	}
	
	public AdvancedIndex(String graphFile, String nodeFile){
		DataReader dataReader = new DataReader(graphFile, nodeFile);
		graph = dataReader.readGraph();
		nodes = dataReader.readNode();
	}
	
	public TNode build(){
		//step 1: compute k-core
		this.n = graph.length;//1 + actual node number
		KCore kcore = new KCore(graph);
		core = kcore.decompose();
		int maxCore = kcore.obtainMaxCore();
		coreReverseFang = kcore.obtainReverseCoreArr();
		System.out.println("k-core decomposition finished (maxCore= " + maxCore +  ").");
		
		//step 2: initialize the union-find data structure
		restNodeSet = new HashSet<TNode>();//the remaining nodes without parents
		uf = new UnionFind();
		unodeArr = new UNode[n];
		for(int i = 1;i < n;i ++){
			UNode unode = new UNode(i);
			uf.makeSet(unode);
			unodeArr[i] = unode;
		}
		
		//step 3: build the tree in a bottom-up manner
		int startIdx = 1;
		invert = new TNode[n];//the invert index for graph nodes to their TNodes
		Set<Integer> core0Set = new HashSet<Integer>();//nodes with degree of 0
		for(int idx = 1;idx < n;idx ++){//idx is the index of array:coreReverseFang
			int id = coreReverseFang[idx];//current node,  an actual node ID
			int curCoreNum = core[id];
			
			if(curCoreNum > 0){
				int nextIdx = idx + 1;
				if(nextIdx < n){
					int nextId = coreReverseFang[nextIdx];
					if(core[nextId] < curCoreNum){
						handleALevel(startIdx, idx, curCoreNum); //generate nodes of tree index using nodes in [startIdx, idx]
						
						for(int reIdx = startIdx;reIdx <= idx;reIdx ++){
							int reId = coreReverseFang[reIdx];//current node,  an actual node ID
							UNode x = unodeArr[reId];
							for(int nghId:graph[reId]){//consider all the neighbors of id
								if(core[nghId] >= curCoreNum){
									UNode y = unodeArr[nghId];
									uf.union(x, y);
								}
							}
							UNode xParent = uf.find(x);
							int xRepresent = uf.find(x).represent;
							if(core[xRepresent] > core[reId])   xParent.represent = reId;//update x.parent's represent attribute
						}
						
						startIdx = nextIdx;//update the startIdx
					}
				}else if(nextIdx == n){
					handleALevel(startIdx, idx, curCoreNum); //generate nodes of tree index using nodes in [startIdx, idx]
				}
			}else{
				core0Set.add(id);
			}
		}
		
		//step 4: build the root node
		TNode root = new TNode(0);
		root.setNodeSet(core0Set);
		root.setChildList(new ArrayList<TNode>(restNodeSet));
//		System.out.println("after building the root:" + root.getChildList().size());
		
		//step 5: attach keywords
		AttachKw attacher = new AttachKw(nodes);
		root = attacher.attach(root);
		
		return root;
	}
	
	//old version: generate TNodes in the same level
	private void handleALevel(int startIdx, int endIdx, int curCoreNum){
		//step 1: build another temporary union-find data structure
		Map<Integer, UNode> idUFMap = new HashMap<Integer, UNode>();//id -> union-find node
		for(int idx = startIdx;idx <= endIdx;idx ++){
			int id = coreReverseFang[idx];//a node's actual ID
			if(!idUFMap.containsKey(id)){
				UNode unode = new UNode(id);
				uf.makeSet(unode);
				idUFMap.put(id, unode);
			}
			for(int nghId:graph[id]){
				if(core[nghId] >= core[id]){
					if(core[nghId] > core[id])   nghId = uf.find(unodeArr[nghId]).value;//replaced by parent
					if(!idUFMap.containsKey(nghId)){
						UNode unode = new UNode(nghId);
						uf.makeSet(unode);
						idUFMap.put(nghId, unode);
					}
					uf.union(idUFMap.get(id), idUFMap.get(nghId));
				}
			}
		}
		
		//step 2: group nodes and find child nodes
		Map<UNode, Set<Integer>> ufGNodeMap = new HashMap<UNode, Set<Integer>>();//<parent, nodeSet>
		Map<UNode, Set<TNode>> ufTNodeMap = new HashMap<UNode, Set<TNode>>();//<parent, childNode>
		for(int reId:idUFMap.keySet()){//consider all the nodes, including out nodes
			UNode newParent = uf.find(idUFMap.get(reId));//in the new union-find
			
			//group nodes
			if(core[reId] == curCoreNum){
				if(ufGNodeMap.containsKey(newParent)){
					ufGNodeMap.get(newParent).add(reId);
				}else{
					Set<Integer> set = new HashSet<Integer>();
					set.add(reId);
					ufGNodeMap.put(newParent, set);
				}
			}
			
			//find childList
			if(core[reId] > curCoreNum){
				UNode oldParent = unodeArr[reId];//in the original union-find, reId is already an id of a parent node
				TNode tnode = invert[oldParent.represent];
				if(ufTNodeMap.containsKey(newParent)){
					ufTNodeMap.get(newParent).add(tnode);
				}else{
					Set<TNode> set = new HashSet<TNode>();
					set.add(tnode);
					ufTNodeMap.put(newParent, set);
				}
			}
		}
		
		//step 3: generate TNodes and build the connections
		for(Map.Entry<UNode, Set<Integer>> entry:ufGNodeMap.entrySet()){
			UNode parent = entry.getKey();
			Set<Integer> nodeSet = entry.getValue();
			Set<TNode> childSet = ufTNodeMap.get(parent);
			
			TNode tnode = new TNode(curCoreNum);
			tnode.setNodeSet(nodeSet);
			if(childSet != null)   tnode.setChildList(new ArrayList<TNode>(childSet));
			
			restNodeSet.add(tnode);//record it as it has no parent
			for(int nodeId:tnode.getNodeSet())   invert[nodeId] = tnode;//update invert
			for(TNode subTNode:tnode.getChildList())   restNodeSet.remove(subTNode);//move some nodes
		}
	}
	
	public int[] getCore(){
		return core;
	}
	
	public TNode[] getInvert() {
		return invert;
	}

	//traverse the tree
	public void traverse(TNode root){
		Iterator<Integer> iter = root.getNodeSet().iterator();
		System.out.print("k=" + root.getCore() + " size=" + root.getNodeSet().size() + " nodes:");
		while(iter.hasNext())   System.out.print(iter.next() + " ");
		System.out.println();
		
		for(int i = 0;i < root.getChildList().size();i ++){
			TNode tnode = root.getChildList().get(i);
			traverse(tnode);
		}
	}
}
