package hku.algo;

import hku.Config;

/**
 * @author fangyixiang
 * @date Jul 24, 2015
 * We assume that nodes' IDs are 1, 2, ..., n
 * This class is used to find a specific core with order of k
 */
public class FindKCore {
	private int graph[][] = null;
	private int n = -1;
	private int deg[] = null;
	private int kk = -1;// the target coreness
	
	public FindKCore(int graph[][], int kk){
		this.graph = graph;
		this.n = graph.length - 1;
		this.kk = kk;
	}
	
	public int[] decompose(){
		long startT = System.currentTimeMillis();
//		System.out.println("KCore n:" + n + " time1:" + (System.currentTimeMillis() - startT));
		
		deg = new int[n + 1];
		
		//step 1: obtain the degree and the maximum degree
		int md = -1; // the maximum degree in the graph
		for(int i = 1;i <= n;i ++){
			deg[i] = graph[i].length;
			if(deg[i] > md){
				md = deg[i];
			}
		}
//		System.out.println("KCore time2:" + (System.currentTimeMillis() - startT));
//		System.out.println("md:" + md);
//		for(int i = 1;i <= n;i ++)   System.out.print("deg[" + i + "]=" + deg[i] + " ");
//		System.out.println();
		
		//step 2: fill the bin
		int bin[] = new int[md + 1];
		for(int i = 1;i <= n;i ++){
			bin[deg[i]] += 1;
		}
//		System.out.println("KCore time3:" + (System.currentTimeMillis() - startT));
//		for(int i = 0;i <= md;i ++)   System.out.print("bin[" + i + "]=" + bin[i] + " ");
//		System.out.println();
		
		//step 3: update the bin
		int start = 1;
		for(int d = 0; d <= md;d ++){
			int num = bin[d];
			bin[d] = start;
			start += num;
		}
//		System.out.println("KCore time4:" + (System.currentTimeMillis() - startT));
//		for(int i = 0;i <= md;i ++)   System.out.print("bin[" + i + "]=" + bin[i] + " ");
//		System.out.println();
		
		//step 4: find the position
		int pos[] = new int[n + 1];
		int vert[] = new int[n + 1];
		for(int v = 1; v <= n;v ++){
			pos[v] = bin[deg[v]];
			vert[pos[v]] = v;
			bin[deg[v]] += 1;
		}
//		System.out.println("KCore time5:" + (System.currentTimeMillis() - startT));
//		for(int i = 1;i <= n;i ++)   System.out.print("pos[" + i + "]=" + pos[i] + " ");
//		System.out.println();
//		for(int i = 1;i <= n;i ++)   System.out.print("ver[" + i + "]=" + vert[i] + " ");
//		System.out.println();
		
		for(int d = md; d >= 1; d--){
			bin[d] = bin[d - 1];
		}
		bin[0] = 1;
//		System.out.println("KCore time6:" + (System.currentTimeMillis() - startT));
		
		//step 5: decompose
		int max = 0;
		for(int i = 1;i <= n;i ++){
			int v = vert[i];
			for(int j = 0;j < graph[v].length;j ++){
				int u = graph[v][j];
				if(deg[u] > deg[v]){
					int du = deg[u];   int pu = pos[u];
					int pw = bin[du];  int w = vert[pw];
					if(u != w){
						pos[u] = pw;   vert[pu] = w;
						pos[w] = pu;   vert[pw] = u;
					}
					bin[du] += 1;
					deg[u] -= 1;
				}
			}
//			System.out.println("deg[" + v + "]=" + deg[v]);
			if(deg[v] > kk)   break;//a new line of code added on Oct 27, 2015
		}
//		System.out.println("KCore time7:" + (System.currentTimeMillis() - startT));
		return deg;
	}
	
	public static void main(String[] args) {
		int graph[][] = new int[11][];
		int a1[] = {2, 3, 4, 6}; graph[1] = a1;
		int a2[] = {1, 3, 4, 6}; graph[2] = a2; 
		int a3[] = {1, 2, 4, 5}; graph[3] = a3;
		int a4[] = {1, 2, 3, 5}; graph[4] = a4;
		int a5[] = {3, 4};       graph[5] = a5;
		int a6[] = {1, 2, 7};    graph[6] = a6;
		int a7[] = {6};          graph[7] = a7;
		int a8[] = {9};          graph[8] = a8;
		int a9[] = {8};          graph[9] = a9;
		int a10[] = {};          graph[10] = a10;
		
		FindKCore kcore = new FindKCore(graph, 1);
		int core[] = kcore.decompose();
		for(int i = 1;i < core.length;i ++)   System.out.print("core[" + i + "]=" + core[i] + " ");
		System.out.println();
	}

}
