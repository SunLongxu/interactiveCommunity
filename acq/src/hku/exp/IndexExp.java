package hku.exp;

import hku.Config;
import hku.algo.DataReader;
import hku.algo.TNode;
import hku.algo.index.AdvancedIndex;
import hku.algo.index.BasicIndex;

/**
 * @author fangyixiang
 * @date Oct 13, 2015
 * test the scalability on index construction
 */
public class IndexExp {

	public void exp(String graphFile, String nodeFile){
		singleExp(graphFile + "-20", nodeFile + "-20");
		singleExp(graphFile + "-40", nodeFile + "-40");
		singleExp(graphFile + "-60", nodeFile + "-60");
		singleExp(graphFile + "-80", nodeFile + "-80");
		singleExp(graphFile, nodeFile);
	}
	
	private void singleExp(String graphFile, String nodeFile){
		DataReader dataReader = new DataReader(graphFile, nodeFile);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
		long s1 = System.currentTimeMillis();
		for(int i = 0;i < 3;i ++){
			BasicIndex idx = new BasicIndex(graph, nodes);
			TNode root = idx.build();
			int core[] = idx.getCore();
		}
		long t1 = System.currentTimeMillis();
		
		
		long s2 = System.currentTimeMillis();
		for(int i = 0;i < 3;i ++){
			AdvancedIndex index = new AdvancedIndex(graph, nodes);
			TNode root = index.build();
			int core[] = index.getCore();
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println(graphFile + " basic:" + (t1 - s1) / 3.0 + " advanced:" + (t2 - s2) / 3.0);
	}
	
	public static void main(String[] args) {
		IndexExp exp = new IndexExp();
		exp.exp(Config.flickrGraph, Config.flickrNode);
//		exp.exp(Config.dblpGraph, Config.dblpNode);
//		exp.exp(Config.twitterGraph, Config.twitterNode);
//		exp.exp(Config.tencentGraph, Config.tencentNode);
//		exp.exp(Config.dbpediaGraph, Config.dbpediaNode);
	}

}
