package hku.algo;

import hku.Config;

import java.util.*;

/**
 * @author fangyixiang
 * @date Aug 17, 2015
 */
public class FindCKCore {
	
	public int[] findCKCore(int graph[][], int core[], int queryId) {
		if(core[queryId] < Config.k){
			int rsNode[] = {queryId};
			return rsNode;
		}

		Set<Integer> visitSet = new HashSet<Integer>();
		Queue<Integer> queue = new LinkedList<Integer>(); 
		
		//step 1: initialize
		queue.add(queryId);
		visitSet.add(queryId);
		
		//step 2: search
		while(queue.size() > 0){
			int current = queue.poll();
			for(int i = 0;i < graph[current].length;i ++){
				int neighbor = graph[current][i];
				if(visitSet.contains(neighbor) == false && core[neighbor] >= Config.k){
					queue.add(neighbor);
					visitSet.add(neighbor);
				}
			}
		}
		
		//count the number of nodes in the k-core
		int count = visitSet.size();
		
		//put all the nodes in an array
		int index = 0;
		int rsNode[] = new int[count];
		Iterator<Integer> iter = visitSet.iterator();
		for(int id:visitSet){
			rsNode[index] = id;
			index += 1;
		}
		
		return rsNode;
	}
}
