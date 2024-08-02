package hku.algo.index.unionFind;

/**
 * @author fangyixiang
 * @date Sep 16, 2015
 */
public class UnionFind {
	
	public void makeSet(UNode x){
		x.parent = x;
		x.rank = 0;
		x.represent = x.value; //this is initialized as itself for our tree index
	}
	
	public UNode find(UNode x){
		if(x.parent != x){
			x.parent = find(x.parent);
		}
		return x.parent;
	}
	
	public void union(UNode x, UNode y){
		UNode xRoot = find(x);
		UNode yRoot = find(y);
		
		if(xRoot == yRoot){
			return ;
		}
		
		//x and y are not already in same set. Merge them.
		if(xRoot.rank < yRoot.rank){
			xRoot.parent = yRoot;
		}else if(xRoot.rank > yRoot.rank){
			yRoot.parent = xRoot;
		}else{
			yRoot.parent = xRoot;
			xRoot.rank = xRoot.rank + 1;
		}
	}
}
