package hku.algo.online;

import hku.Config;
import hku.algo.AprioriPruner;
import hku.algo.DataReader;
import hku.algo.FindCCS;
import hku.algo.FindCKCore;
import hku.algo.KCore;
import hku.util.CCSSaver;
import hku.util.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fangyixiang
 * @date Jul 21, 2015
 * steps: 1) K-core; 2) Keywords filtering; and 3) K-core again
 */
public class BasicG {
	private String nodes[][] = null;
	private int graph[][] = null;
	private int core[] = null;
	private int queryId = -1;
	private String ccsFile = null;
	private CCSSaver saver = null;
	private AprioriPruner apruner = null;
	
	public BasicG(int graph[][], String nodes[][]){
		this.graph = graph;
		this.nodes = nodes;
		
		KCore kcore = new KCore(graph);
		core = kcore.decompose();
	}
	
	public BasicG(String graphFile, String nodeFile, String ccsFile) {
		DataReader dataReader = new DataReader(graphFile, nodeFile);
		nodes = dataReader.readNode();
		graph = dataReader.readGraph();
		this.ccsFile = ccsFile;
		
		//compute k-core
		KCore kcore = new KCore(graph);
		core = kcore.decompose();
//		System.out.println("k-core decomposition finished.");
	}

	public void query(int queryId) {
		this.queryId = queryId;
		
		// step 1: find the connected k-core containing nodeId
		if(core[queryId] < Config.k){
//			System.out.println("No answer! Cannot find k-core");
			return ;
		}
		FindCKCore finder = new FindCKCore();
		int cKCoreNode[] = finder.findCKCore(graph, core, queryId);
		
		// step 2: filter with keywords and re-find k-core
		if(cKCoreNode.length > 0){
//			System.out.println("ck-core search has been finished, size=" + cKCoreNode.length);
			kwFilter(cKCoreNode);
		}
	}

	

	private void kwFilter(int cKCoreNode[]) {
		//initialize the candidate list
		List<String[]> candList = new ArrayList<String[]>();
		for(int j = 1;j < nodes[queryId].length;j ++){
			String kws[] = {nodes[queryId][j]};
			candList.add(kws);
		}
		
		//consider keyword combination with length k
		for(int iterK = 1;;iterK ++){
			//step 1: consider all the keyword combinations
			List<String[]> validKwList = new ArrayList<String[]>();
			for(String kws[]:candList){
//				System.out.print("We are considering: ");
//				for(int i = 0;i < kws.length;i ++)   System.out.print(kws[i] + " ");
//				System.out.println();
				
				//step 1.1: keywords filtering and subgraph copy
				List<Integer> curList = new ArrayList<Integer>();//this list serves as a map (newID -> original ID)
				curList.add(-1);//for consuming space purpose
				for(int i = 0;i < cKCoreNode.length; i ++){
					int curId = cKCoreNode[i];
						
					boolean isContained = true;
					for(int x = 0; x < kws.length;x ++){
						boolean isSingleContained = false;
						for(int y = 1;y < nodes[curId].length;y ++){
							if(nodes[curId][y].equals(kws[x])){
								isSingleContained = true;
								break;
							}
						}
						
						//if this single keyword is not contained, then skip
						if(isSingleContained == false){
							isContained = false;
							break;
						}
					}
					
					//if this node's keywords are contained, then we choose it
					if(isContained){
						curList.add(curId);
					}
				}
				
				if(curList.size() <= 1){
//					saver.save(graph, nodes, queryId, kws);
					continue;
				}else{
//					System.out.println("After keywords filtering, the number of nodes: " + curList.size());
				}
				
				//step 2: find a context community from the subgraph
				FindCCS finder = new FindCCS(graph, curList, queryId);
				Set<Integer> ccsSet = finder.findRobustCCS();
				if(ccsSet.size() > 1){
					validKwList.add(kws);
//					System.out.println("We find a community with size = " + ccsSet.size());
				}
			}
			
			if(validKwList.size() == 0){
				break;
			}
			
			//step 2: generate candidates
			Tool tool = new Tool();
			apruner = new AprioriPruner(validKwList);
			candList = new ArrayList<String[]>();
			for(int i = 0;i < validKwList.size() - 1;i ++){
				for(int j = i + 1;j < validKwList.size();j ++){
					if(iterK == 1){
						String kws1 = validKwList.get(i)[0];
						String kws2 = validKwList.get(j)[0];
						String newKws[] = {kws1, kws2};
						if(kws1.compareTo(kws2) > 0){
							newKws[0] = kws2;   newKws[1] = kws1;
						}
						if(!apruner.isPruned(newKws))   candList.add(newKws);
					}else{
						boolean isCand = true;
						String kws1[] = validKwList.get(i);
						String kws2[] = validKwList.get(j);
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
							if(!apruner.isPruned(newKws))   candList.add(newKws);
						}
					}
				}
			}
		}
	}
}
