package hku.util;

import hku.algo.TNode;

import java.util.List;
import java.util.*;
/**
 * @author fangyixiang
 * @date Aug 11, 2015
 */
public class Tool {
	
	//convert an ArrayList variable to an array
	public int[] arrayListToArray(List<Integer> list){
		int rs[] = new int[list.size()];
		for(int i = 0;i < list.size();i ++){
			rs[i] = list.get(i);
		}
		return rs;
	}
	
	//convert an ArrayList variable to a sorted array
	public int[] arrayListToSortedArray(List<Integer> list){
		int rs[] = new int[list.size()];
		for(int i = 0;i < list.size();i ++){
			rs[i] = list.get(i);
		}
		
		//sort
		for(int i = 0;i < rs.length;i ++){
			int k = i;
			for(int j = i + 1;j < rs.length;j ++){
				if(rs[j] < rs[k]){
					k = j;
				}
			}
			
			int tmp = rs[i];
			rs[i] = rs[k];
			rs[k] = tmp;
		}
		
		return rs;
	}
	
	//sort the keywords
	public String[] sortKw(String kws[]){
		for(int i = 0;i < kws.length - 1;i ++){
			int k = i;
			for(int j = i + 1;j < kws.length;j ++){
				if(kws[j].compareTo(kws[k]) < 0){
					k = j;
				}
			}
			String tmp = kws[i];
			kws[i] = kws[k];
			kws[k] = tmp;
		}
		return kws;
	}
	
	private static void print(TNode tnode){
		System.out.print("TNode:");
		for(int d:tnode.getNodeSet())   System.out.print(d + " ");
		System.out.println();
	}
	
	
	public int count(TNode root){
		int c = root.getNodeSet().size();
		for(TNode cTNode:root.getChildList()){
			c += count(cTNode);
		}
		return c;
	}
	
	public void countNodeFreq(TNode root, Map<Integer, Integer> map){
		for(int id:root.getNodeSet()){
			if(map.containsKey(id)){
				map.put(id, map.get(id) + 1);
			}else{
				map.put(id, 1);
			}
		}
		
		for(TNode cTNode:root.getChildList()){
			countNodeFreq(cTNode, map);
		}
	}

	public static void main(String args[]){
		String kws[] = {"a", "b", "d", "c"};
		Tool tool = new Tool();
		String rs[] = tool.sortKw(kws);
		for(int i = 0;i < rs.length;i ++){
			System.out.println(rs[i]);
		}
	}
}
