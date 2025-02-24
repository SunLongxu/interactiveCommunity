package hku.algo;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * @author fangyixiang
 * @date Aug 25, 2015
 */
public class AprioriPruner {
	private Set<String> concatedKwSet = null;
	
	public AprioriPruner(List<String[]> validKwList){
		this.concatedKwSet = new HashSet<String>();
		
		for(String kws[]:validKwList){
			String concatedKws = kws[0];
			for(int i = 1;i < kws.length;i ++){
				concatedKws += " " + kws[i];
			}
			concatedKws = concatedKws.trim();
			concatedKwSet.add(concatedKws);
			
//			System.out.println("******" + concatedKws);
		}
	}
	
	public boolean isPruned(String kw[]){
		//for a candidate with length 2, no need for pruning
		if(kw.length <= 2){
			return false;
		}
		
		boolean rs = false;
		
		
		int k = kw.length;
		for(int i = 0;i < k - 2;i ++){
			String subConcatedKws = "";
			for(int j = 0;j < k;j ++){
				if(i != j){
					subConcatedKws += kw[j] + " ";
				}
			}
			subConcatedKws = subConcatedKws.trim();
			
//			if(kw.length == 6 && kw[0].equals("approach")){
//				System.out.println("ERROR: " + subConcatedKws + " === " + concatedKwSet.contains(subConcatedKws));
//			}
			
			//this sub-keyword-combination is pruned as it is not frequent!!!
			if(concatedKwSet.contains(subConcatedKws) == false){
				rs = true;
				break;
			}
		}
		
		return rs;
	}
}
