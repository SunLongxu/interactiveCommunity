package hku.algo.query2.fpMine;

import java.util.ArrayList;
import java.util.List;



/**
 * @author yxfang
 *
 * @date 2015-10-11
 */
public class Main {

	public static void main(String[] args) {
		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(1);	list1.add(2);	list1.add(3);	list1.add(4);
		
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(1);	list2.add(2);	list2.add(5);	list2.add(6);
		
		List<Integer> list3 = new ArrayList<Integer>();
		list3.add(1);	list3.add(2);	list3.add(7);	list3.add(3);
		
		List<List<Integer>> transList = new ArrayList<List<Integer>>();
		transList.add(list1);
		transList.add(list2);
		transList.add(list3);
		
		int minsupp = 2;
		AlgoFPGrowth algo = new AlgoFPGrowth();
		algo.printStats();
		try{
			Itemsets itemsets = algo.runAlgorithm(transList, minsupp);
			List<List<Itemset>> rsList = itemsets.getLevels();
			for(int i = 0;i < rsList.size();i ++){
				List<Itemset> list = rsList.get(i);
				System.out.println("Level:" + i);
				for(int j = 0;j < list.size();j ++){
					Itemset itemset = list.get(j);
					int arr[] = itemset.itemset;
					for(int id:arr){
						System.out.print(id + " ");
					}
					System.out.println("   " + itemset.getAbsoluteSupport());
				}
				System.out.println();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
