package hku.prep.dblp;

import hku.Config;
import hku.algo.DataReader;
import hku.algo.KCore;
import hku.algo.index.BasicIndex;

import java.util.*;
/**
 * @author fangyixiang
 * @date Sep 9, 2015
 */
public class UserFinder {
	private DataReader dataReader = null;
	private int graph[][] = null;
	private String nodes[][] = null;
	private int core[] = null;

	public UserFinder(){
		dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
		graph = dataReader.readGraph();
		nodes = dataReader.readNode();
		
		KCore kcore = new KCore(graph);
		core = kcore.decompose();
		
		for(int i = 1;i < nodes.length;i ++){
//			if(nodes[i][0].contains("jim")){
//			if(nodes[i][0].contains("reynold cheng")){
			if(nodes[i][0].contains("yizhou sun")){
				System.out.println("name:" + nodes[i][0] + " id:" + i);
			}
		}
	}

	public void print(int nodeId){
		System.out.println("username:" + nodes[nodeId][0]);
		System.out.println("keyword number:" + (nodes[nodeId].length - 1));
		for(int i = 1;i < nodes[nodeId].length;i ++){
			System.out.println(i + "-th keyword:" + nodes[nodeId][i]);
		}
		
		System.out.println("neighbor number:" + graph[nodeId].length);

		System.out.println("core number:" + core[nodeId]);
	}
	
	public static void main(String[] args) {
		int nodeId = 15238;//jiawei han
//		int nodeId = 152532;//jim gray
//		int nodeId = 15857;//reynold cheng
		
		UserFinder finder = new UserFinder();
		finder.print(nodeId);
	}
}
