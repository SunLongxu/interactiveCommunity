package hku.algo.index;

import hku.Config;
import hku.algo.*;
/**
 * @author fangyixiang
 * @date Sep 17, 2015
 */
public class IndexTest {

	public static void main(String args[]){
//		DataReader dataReader = new DataReader(Config.caseGraph, Config.caseNode);
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
//		long t1 = System.currentTimeMillis();
//		BasicIndex index1 = new BasicIndex(graph, nodes);
//		TNode root1 = index1.build();
//		System.out.println("time cost of basic index:" + (System.currentTimeMillis() -  t1));
		
		long t2 = System.currentTimeMillis();
//		AdvancedIndex index = new AdvancedIndex(graph, nodes);
		AdvancedIndex index = new AdvancedIndex(graph, nodes);
//		AdvancedIndex2 index = new AdvancedIndex2(graph, nodes);
		TNode root = index.build();
		System.out.println("time cost of advanced index:" + (System.currentTimeMillis() -  t2));
	}
	
	public static void main2(String args[]){
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
		AdvancedIndex idx = new AdvancedIndex(graph, nodes);
		TNode root = idx.build();
		idx.traverse(root);
	}
	
	public static void main3(String[] args) {
		int graph[][] = new int[11][];
		int a1[] = {2, 3, 4, 5};graph[1] = a1;
		int a2[] = {1, 3, 4, 5};graph[2] = a2;
		int a3[] = {1, 2, 3};	graph[3] = a3;
		int a4[] = {1, 2, 3, 7};graph[4] = a4;
		int a5[] = {1, 2, 7};	graph[5] = a5;
		int a6[] = {4};			graph[6] = a6;
		int a7[] = {5};			graph[7] = a7;
		int a8[] = {9};			graph[8] = a8;
		int a9[] = {8};			graph[9] = a9;
		int a10[] = {};			graph[10] = a10;
		
		String nodes[][] = new String[11][];
		String k1[] = {"A", "v", "w", "x", "y"};nodes[1] = k1;
		String k2[] = {"B", "x"};				nodes[2] = k2;
		String k3[] = {"C", "x"};				nodes[3] = k3;
		String k4[] = {"D", "x", "y", "z"};		nodes[4] = k4;
		String k5[] = {"E", "w", "y"};			nodes[5] = k5;
		String k6[] = {"F", "y"};				nodes[6] = k6;
		String k7[] = {"G", "y", "z"};			nodes[7] = k7;
		String k8[] = {"H", "z"};				nodes[8] = k8;
		String k9[] = {"I", "x"};				nodes[9] = k9;
		String k10[] = {"J", "x"};				nodes[10] = k10;
		
		AdvancedIndex index = new AdvancedIndex(graph, nodes);
		TNode root = index.build();
		index.traverse(root);
		
		System.out.println("# of root's children:" + root.getChildList().size());
	}

}
/*
the # of nodes in G:977288
the # of edges in G:6864546
k-core decomposition finished (maxCore= 118).
time cost of basic index:32371
k-core decomposition finished (maxCore= 118).
time cost of advanced index:4023


the # of nodes in G:977288
the # of edges in G:6864546
k-core decomposition finished (maxCore= 118).
time cost of basic index:29049
k-core decomposition finished (maxCore= 118).
time cost of advanced index:1963
*/