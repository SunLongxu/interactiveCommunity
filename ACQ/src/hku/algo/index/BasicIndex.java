package hku.algo.index;

import hku.algo.DataReader;
import hku.algo.KCore;
import hku.algo.TNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Aug 14, 2015
 * build the index in a recursive manner
 */
public class BasicIndex {
	private TNode root = null; //the root node of the cck-core tree
	private String nodes[][] = null;
	private int graph[][] = null;
	private int core[] = null;
	private int maxCore = -1;
	
	public BasicIndex(int graph[][], String nodes[][]){
		this.graph = graph;
		this.nodes = nodes;
	}
	
	public BasicIndex(String graphFile, String nodeFile){
		DataReader dataReader = new DataReader(graphFile, nodeFile);
		graph = dataReader.readGraph();
		nodes = dataReader.readNode();
	}
	
	public TNode build(){
		//step 1: compute k-core
		KCore kcore = new KCore(graph);
		core = kcore.decompose();
		maxCore = kcore.obtainMaxCore();
		System.out.println("k-core decomposition finished (maxCore= " + maxCore +  ").");
		
		//step 2: build the root node
		TNode root = new TNode(0);
		Set<Integer> restNodeSet = new HashSet<Integer>();
		for(int i = 1;i < core.length;i ++){
			if(core[i] == 0){
				root.getNodeSet().add(i);
			}else{
				restNodeSet.add(i);
			}
		}
		
		//step 3: build the tree recursively
		buildChild(root, restNodeSet, 1);
		
		//step 4: delete empty nodes
		deleteEmpty(root);
		
		//step 5: attach keywords
		AttachKw attacher = new AttachKw(nodes);
		root = attacher.attach(root);
		
		return root;
	}
	
	//the input is the previous root, allNodes and K
	private void buildChild(TNode root, Set<Integer> restNodeSet, int k){
		List<Set<Integer>> rsList = findCC(restNodeSet, k);
		
		for(Set<Integer> set:rsList){
			TNode tnode = new TNode(k);
			Set<Integer> newRestNodeSet = new HashSet<Integer>();
			Iterator<Integer> iter = set.iterator();
			while(iter.hasNext()){
				int nodeId = iter.next();
				if(core[nodeId] == k){
					tnode.getNodeSet().add(nodeId);
				}else{
					newRestNodeSet.add(nodeId);
				}
			}
			root.getChildList().add(tnode);
			
			if(newRestNodeSet.size() > 1 && k < maxCore){
				buildChild(tnode, newRestNodeSet, k + 1);
			}
		}
	}
	
	//find connected components in k-core
	private List<Set<Integer>> findCC(Set<Integer> allNodeSet, int k){
		List<Set<Integer>> rsList = new ArrayList<Set<Integer>>();
		
		//labling visited nodes
		int arrNum = graph.length; //arrNum = nodeNum + 1; 
		boolean visit[] = new boolean[arrNum];
		
		while(allNodeSet.size() > 0){
			Set<Integer> ccSet = new HashSet<Integer>(); //this is a connected component
			Queue<Integer> queue = new LinkedList<Integer>(); 
			
			Iterator<Integer> iter = allNodeSet.iterator();
			int seedID = iter.next();
			
			//initialize
			queue.add(seedID);
			allNodeSet.remove(seedID);
			ccSet.add(seedID);
			visit[seedID] = true;
			
			//search
			while(queue.size() > 0){
				int current = queue.poll();
				for(int i = 0;i < graph[current].length;i ++){
					int neighbor = graph[current][i];
					if(visit[neighbor] == false && core[neighbor] >= k && allNodeSet.contains(neighbor)){
						queue.add(neighbor);
						allNodeSet.remove(neighbor);
						ccSet.add(neighbor);
						visit[neighbor] = true;
					}
				}
			}
			
			rsList.add(ccSet);
		}
		
		return rsList;
	}
	
	//delete a node if it is empty
	private void deleteEmpty(TNode root){
		for(TNode child:root.getChildList()){
			deleteEmpty(child);
		}
		
		if(root.getChildList().size() > 0){
			List<TNode> newChildList = new ArrayList<TNode>();
			for(TNode child:root.getChildList()){
				if(child.getNodeSet().size() == 0){//this child is empty
					for(TNode grandChild:child.getChildList()){
						newChildList.add(grandChild);
					}
				}else{
					newChildList.add(child);
				}				
			}
			root.setChildList(newChildList);
		}
	}
	
	public int[] getCore() {
		return core;
	}
	
	//traverse the tree
	public void traverse(TNode root){
		Iterator<Integer> iter = root.getNodeSet().iterator();
		System.out.print("k=" + root.getCore() + "\t:");
		while(iter.hasNext()){
			System.out.print(iter.next() + " ");
		}
		System.out.println();
		if(root.getKwMap() != null){
			for(Entry<String, int[]> entry:root.getKwMap().entrySet()){
				System.out.print("KEYWORD-" + entry.getKey() + ":");
//				int idx = entry.getValue();
//				int invert[] = root.getInvert()[idx];
				int invert[] = entry.getValue();
				for(int i = 0;i < invert.length;i ++){
					System.out.print(" " + invert[i]);
				}
				System.out.println();
			}
		}
		System.out.println();
		
		for(int i = 0;i < root.getChildList().size();i ++){
			TNode tnode = root.getChildList().get(i);
			traverse(tnode);
		}
	}

}
