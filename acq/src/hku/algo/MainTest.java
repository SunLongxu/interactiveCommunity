package hku.algo;

import java.util.List;
import java.util.Set;
import hku.Config;
import hku.algo.index.*;
import hku.algo.online.BasicG;
import hku.algo.online.BasicW;
import hku.algo.query1.IncS;
import hku.algo.query1.IncT;
import hku.algo.query2.*;
import hku.algo.query2.*;

import java.util.Random;

import hku.util.*;
/**
 * @author fangyixiang
 * @date Aug 17, 2015
 */
public class MainTest {

	public static void main(String[] args) {
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
		
		BasicIndex idx = new BasicIndex(graph, nodes);
		TNode root = idx.build();
		int core[] = idx.getCore();
		idx.traverse(root);

		int queryId = 1;
		long time1 = System.currentTimeMillis();
		Dec query1 = new Dec(graph, nodes, root, core, null);
		query1.query(queryId);
		long time2 = System.currentTimeMillis();		
//		GlobalQuery1 query1 = new GlobalQuery1(graph, nodes, root, core, Config.dblpCCS);
//		query1.query(1);
		
//		LocalQuery1 query1 = new LocalQuery1(graph, nodes, root, core, Config.caseCCS);
//		query1.query(1);
	}
	
	//dblp
	public static void main2(String[] args) {
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
		AdvancedIndex index = new AdvancedIndex(graph, nodes);
		TNode root = index.build();
		int core[] = index.getCore();
		
		Config.k = 4;
		
//		long start1 = System.currentTimeMillis();
//		BasicG base1 = new BasicG(Config.dblpGraph, Config.dblpNode, Config.dblpCCS);
//		base1.query(15238);
//		System.out.println("time cost:" + (System.currentTimeMillis() - start1));
//		
//		long start2 = System.currentTimeMillis();
//		BasicW base2 = new BasicW(Config.dblpGraph, Config.dblpNode, Config.dblpCCS);
//		base2.query(15238);
//		System.out.println("time cost:" + (System.currentTimeMillis() - start2));
		
		int queryId = 15238;
		long time1 = System.currentTimeMillis();
		IncS query1 = new IncS(graph, nodes, root, core, null);
		int size1 = query1.query(queryId);
		long time2 = System.currentTimeMillis();
//		
//		long time3 = System.currentTimeMillis();
//		GlobalQuery2 query2 = new GlobalQuery2(graph, nodes, root, core, null);
//		int size2 = query2.query(queryId);
//		long time4 = System.currentTimeMillis();
//		
//		long time5 = System.currentTimeMillis();
//		NghQuery query3 = new NghQuery(graph, nodes, root, core, null);
//		int size3 = query3.query(queryId);
//		long time6 = System.currentTimeMillis();
	}
}
