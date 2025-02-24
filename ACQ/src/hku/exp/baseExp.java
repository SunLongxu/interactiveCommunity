package hku.exp;

import hku.Config;
import hku.algo.DataReader;
import hku.algo.TNode;
import hku.algo.index.AdvancedIndex;
import hku.algo.query1.IncS;
import hku.algo.query1.IncT;
import hku.algo.query2.*;
import hku.exp.util.*;
import hku.algo.online.BasicG;
import hku.algo.online.BasicW;
import hku.algo.*;
import hku.algo.FindCC;
import hku.algo.FindCCS;
import hku.algo.FindKCore;
import hku.algo.recommend.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import hku.util.*;
import java.util.Scanner;

public class baseExp {

	public void expIndex(String graphFile, String nodeFile, String queryFile, String groundFile){

		
		singleIndex(graphFile, nodeFile, queryFile, groundFile);
		
	}
	public static int[] removeElements(int[] arr, int key)
    {
        // Move all other elements to beginning
        int index = 0;
        for (int i=0; i<arr.length; i++)
            if (arr[i] != key)
                arr[index++] = arr[i];
 
        // Create a copy of arr[]
        return Arrays.copyOf(arr, index);
    }
	//new util
	public static Set<Integer> findCommonIntegers(List<Set<Integer>> sets, Set<Integer> posNodes, Set<Integer> negNodes) {
        Set<Integer> result = new HashSet<>();
        int countMax = 0;
		result = sets.get(0);
		HashMap<Integer, Set<Integer>> hmres = new HashMap<>();
            
        for (Set<Integer> set : sets) {
			// int countCommonTotal = negNodes.size();
			int countCommonTotal = 0;
			for (int num : set) {
                if (posNodes.contains(num)) {
					countCommonTotal++;
				}
            }
			// for (int num : negNodes) {
			// 	if (set.contains(num)) {
			// 		countCommonTotal--;
			// 	}
			// }
			if (countCommonTotal>countMax) {
				countMax = countCommonTotal;
			}
			hmres.put(countCommonTotal, set);

        }
        for (Map.Entry<Integer, Set<Integer>> set : hmres.entrySet()) {
			if (set.getKey()==countMax) {
				result.addAll(set.getValue());
			}
            // System.out.println(set.getKey() + " = "
                            //    + set.getValue());
        }
        return result;
    }
	private void outputSubgraph(int[][] graph, String[][] nodes, int qid, Set<Integer>targetVertices, String resLabel){
		Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
		Set<Integer> resultVertices = new HashSet<>();
        Set<String> printVertices = new HashSet<>();
        Set<String> resultEdges = new HashSet<>();
        
        queue.offer(qid);
        visited.add(qid);
        
        // while (!queue.isEmpty()) {
        //     int currentNode = queue.poll();
        //     resultVertices.add(currentNode);
        //     printVertices.add(currentNode + " " + nodes[currentNode][0]);
            
        //     if (targetVertices.contains(currentNode)) {
        //         targetVertices.remove(currentNode);
        //     }
		// 	if (targetVertices.isEmpty()) {
		// 		break;
		// 	}
        //     for (int neighbor : graph[currentNode]) {
        //         if (!visited.contains(neighbor)) {
        //             queue.offer(neighbor);
        //             visited.add(neighbor);
        //         }
        //     }
        // }
        // for (int vertex : resultVertices) {
		for (int vertex : targetVertices) {
			printVertices.add(vertex + "," + nodes[vertex][0]);
            for (int neighbor : graph[vertex]) {
                if (targetVertices.contains(neighbor)) {
					if (vertex>neighbor) {
						int tmp = vertex;
						vertex = neighbor;
						neighbor = tmp;
					}
                    resultEdges.add(vertex + "," + neighbor);
                }
            }
        }
		// writeToFile("case_vertices.txt", printVertices);
        writeToFile("_case_subvertices_"+nodes[qid][0]+".csv", resLabel, printVertices);
        writeToFile("_case_subedges_"+nodes[qid][0]+".csv", resLabel, resultEdges);
    }

    public static void writeToFile(String fileName, String resLabel, Set<String> data) {
		
		Date date = new Date();
		SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

		try { 
  
            BufferedWriter fw = new BufferedWriter(new FileWriter("./info/"+timeStamp.format(date)+"_"+resLabel+fileName, true));
			if (fileName=="case_subvertices.csv") {
				fw.write("Id,Label"); 
				fw.newLine();
			} else if (fileName=="case_subedges.csv") {
				fw.write("Source,Target"); 
				fw.newLine();
			}
            for (String item : data) {
                fw.write(item); 
				fw.newLine();
			}
			fw.flush();
            fw.close(); 
        }catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	
	private void singleIndex(String graphFile, String nodeFile, String queryFile, String gtFile){
		DataReader dataReader = new DataReader(graphFile, nodeFile);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
		AdvancedIndex index = new AdvancedIndex(graph, nodes);
		TNode root = index.build();
		int core[] = index.getCore();
		System.out.println("index construction finished !");
		
		QueryIdReader qReader = new QueryIdReader();
		List<QueryObj> queryIdList = qReader.read(queryFile);

		GroundtruthReader gtReader = new GroundtruthReader();
		ArrayList<HashSet<Integer>> gtCmtyList = gtReader.readGT(gtFile);

		double[][] q1 = new double[12][3];
		double[][] recPosPre = new double[12][4];
		double[][] sumavgtres = new double[12][3];
		for(double[] dbl: q1){
			Arrays.fill(dbl, 0.0);
		}
		for (double[] flt : recPosPre) {
			Arrays.fill(flt, 0.0);
		}
		for (double[] flt : sumavgtres) {
			Arrays.fill(flt, 0.0);
		}

		int count = 0;
		
		for(QueryObj qC:queryIdList){
			// long time1 = System.nanoTime();
			// IncS query1 = new IncS(graph, nodes, root, core, null);
			// int size1 = query1.query(queryId);
			// long time2 = System.nanoTime();
			// q1 += time2 - time1;
			
			// long time3 = System.nanoTime();
			// IncT query2 = new IncT(graph, nodes, root, core, null);
			// int size2 = query2.query(queryId);
			// long time4 = System.nanoTime();
			// q2 += time4 - time3;
			Set<Integer> posNodes = new HashSet<>();
			Set<Integer> negNodes = new HashSet<>();
			Integer posId = null, negId = null;
			Config.k = qC.getqCore();
			Config.kwLength = -1;
			count += 1;
			String newnodes[][] = nodes.clone();
			int newgraph[][] = graph.clone();
			int newcore[] = core.clone();
			
			for (int recRound = 0; recRound < 11; recRound++) {
			
				long time1 = System.nanoTime();
				Dec query3 = new Dec(newgraph, newnodes, root, newcore, null);
				List<Set<Integer>> resList = query3.query(qC.getqid());
				long time2 = System.nanoTime();
				q1[recRound][0] += time2 - time1;
				double[] avgtres = {0.0, 0.0, 0.0};
				

				if(resList == null || resList.isEmpty()) {
					StringBuilder sb = new StringBuilder ();
					sb.append(qC.getqid()).append(" Config.k=" + Config.k).append(" ").append(0);
					// Log.log(sb.toString());
					System.out.println(sb.toString());
				} else {
					for (int tcmtyid : qC.getqCmty()){
						System.out.println("tcmtyid: " + tcmtyid);
						System.out.println("resList size: " + resList.size());
						double[] tres = {-1.0, -1.0, -1.0};
						int maxcmtyid = -1;
						for (int cmtyid = 0; cmtyid < resList.size(); cmtyid++) {
							F1ScoreCalculator f1 = new F1ScoreCalculator();
							// fix pos neg vertices hard
							Set<Integer> ressetManage = new HashSet<Integer>(resList.get(cmtyid));
							ressetManage.removeAll(negNodes);
							ressetManage.addAll(posNodes);
							double[] res = f1.f1score(gtCmtyList.get(tcmtyid-1), ressetManage);
							if (res[2] > tres[2]) {

								maxcmtyid = cmtyid;
								tres = Arrays.copyOf(res, 3);
								System.out.println("pr: " + res[0] + ", re: " + res[1] + ", f1: " + res[2] );
							
							}
						}
						StringBuilder sb = new StringBuilder ();
						sb.append(qC.getqid()).append(" Config.k=" + Config.k).append(" ").append(maxcmtyid);
					
						for (Integer cmtyV : resList.get(maxcmtyid)){
							sb.append(" ").append(cmtyV);
						}
						// Log.log(sb.toString());
						System.out.println(sb.toString());
						// Log.log("pr: "+tres[0] + " re: " + tres[1] + " f1: " + tres[2]);
						for(int i = 0; i < 3; i++) {
							avgtres[i] += tres[i];
						}
					}
					for(int i = 0; i < 3; i++) {
						avgtres[i] = avgtres[i]/qC.getqCmty().length;
						sumavgtres[recRound][i] += avgtres[i];
					}
					// Log.log("avg pr: "+avgtres[0] + " avg re: " + avgtres[1] + " avg f1: " + avgtres[2]);
					System.out.println("avg pr: "+avgtres[0] + " avg re: " + avgtres[1] + " avg f1: " + avgtres[2]);
 
				}

				
				// if (avgtres[1] == 1.0) {
					
				// }

				System.out.println("Rec Round:" + recRound 
				+ "k: " + Config.k
				+ "kwLength: " + Config.kwLength
				+ "Avg Pre:" + sumavgtres[recRound][0] / count
				+ " Rec:" + sumavgtres[recRound][1] / count
				+ " F1:" + sumavgtres[recRound][2] / count);
				if(count == 1){
					Log.log(graphFile);
					Log.log("count" + ","
							+ " rec round"+ "," 
							+ " posnode rec time" + ","
							+ " negnode rec time" + ","
							+ " posnode rec precision" + ","
							+ " posnode rec target" + ","
							+ " negnode rec precision" + ","
							+ " negnode rec target" + ","
							// + " Inc-S:" + q1 / 1000000 / count
							// + " Inc-T:" + q2 / 1000000 / count
							+ " Dec:" + ","
							+ " Pre:" + ","
							+ " Rec:" + ","
							+ " F1:" + ",");
				}else if(count % 100 == 0){
					Log.log(count + ","
							+ recRound + ","
							+ q1[recRound][1] / 1000000 /count + ","
							+ q1[recRound][2] / 1000000 /count + ","
							+ recPosPre[recRound][0] /count + ","
							+ recPosPre[recRound][1] /count + ","
							+ recPosPre[recRound][2] /count + ","
							+ recPosPre[recRound][3] /count + ","
							// + " Inc-S:" + q1 / 1000000 / count
							// + " Inc-T:" + q2 / 1000000 / count
							+ q1[recRound][0] / 1000000 /count + ","
							+ sumavgtres[recRound][0] / count + ","
							+ sumavgtres[recRound][1] / count + ","
							+ sumavgtres[recRound][2] / count + ",");
							// Log.log("count:" + count
							// + " rec round:" + recRound
							// + " posnode rec time:" + q1[recRound][1] / 1000000 /count
							// + " negnode rec time:" + q1[recRound][2] / 1000000 /count
							// + " posnode rec precision:" + recPosPre[recRound][0] /count
							// + " posnode rec target:" + recPosPre[recRound][1] /count
							// + " negnode rec precision:" + recPosPre[recRound][2] /count
							// + " negnode rec target:" + recPosPre[recRound][3] /count
							// + " Dec:" + q1[recRound][0] / 1000000 /count
							// + " Pre:" + sumavgtres[recRound][0] / count
							// + " Rec:" + sumavgtres[recRound][1] / count
							// + " F1:" + sumavgtres[recRound][2] / count);
					if(count == queryIdList.size())   Log.log("\n");
				}
					//recommendation procedure
					Set<Integer> allResSet = new HashSet<Integer>();
        
        			if(resList == null || resList.isEmpty()) {
        			    allResSet.add(qC.getqid());
        			} else {
        			    for(Set<Integer> resSet : resList){
        			        allResSet.addAll(resSet);
        			    }
        			}

					time2 = System.nanoTime();
					Rec rec1 = new Rec(graph, nodes, root, core, posNodes, negNodes, qC.getqid(), allResSet);
					List<Integer> posList = rec1.recMain(1, 1);
					long time3 = System.nanoTime();
					q1[recRound+1][1] += time3 - time2;
					List<Integer> negList = rec1.recMain(0, 1);
					// List<Integer> posList = new ArrayList<>();
					long time4 = System.nanoTime();
					q1[recRound+1][2] += time4 - time3;
					
					Set<Integer> gtSumSet = new HashSet<Integer>();
					for (int tcmtyid : qC.getqCmty()){
						gtSumSet.addAll(gtCmtyList.get(tcmtyid-1));
					}
					Set<Integer> truPosSet = new HashSet<Integer>(gtSumSet);
					truPosSet.removeAll(allResSet);
					int truePosSiz = Math.min(truPosSet.size(), posList.size());
					truPosSet.retainAll(posList);

					Set<Integer> truNegSet = new HashSet<Integer>(allResSet);
					truNegSet.removeAll(gtSumSet);
					int trueNegSiz = Math.min(truNegSet.size(), negList.size());
					truNegSet.retainAll(negList);

					System.out.println("rec true pos size: " + truPosSet.size() + "rec pos id set:"+ posList + "rec true pos id set:"+ truPosSet);
					System.out.println("rec true neg size: " + truNegSet.size() + "rec neg id set:"+ negList + "rec true neg id set:"+ truNegSet);
					
					// int kChangeTag = 0, kwChangeTag = 0;
					// if(resList == null || resList.isEmpty()) {
					// 	kChangeTag = 1;
					// }
					Set<Integer> posNodesCopy = new HashSet<Integer>(posNodes);
					posNodesCopy.removeAll(allResSet);
					// if(posNodesCopy.size() > 0){
					// 	kwChangeTag = 1;
					// }
					// paramUpd param1 = new paramUpd(graph, nodes, core);
					// int changeTag = 0;
					int posTag = 0;
					int negTag = 0;
					if (posList.isEmpty() || truPosSet.isEmpty()) {
						// Log.log("rec pos precision: 0.0 rec pos target: 0.0");
						System.out.println("rec pos precision: 0.0 rec pos target: 0.0");
						if (avgtres[1] == 1.0) {
							recPosPre[recRound+1][0] += recPosPre[recRound+1][0] / count;
							recPosPre[recRound+1][1] += recPosPre[recRound+1][1] / count;
						}
					}
					else {
						recPosPre[recRound+1][0] += (double) truPosSet.size() / (double) truePosSiz;
						recPosPre[recRound+1][1] += 1.0;
						System.out.println("rec pos precision:" + ((double) truPosSet.size() / (double) truePosSiz) + " rec pos target: 1.0");
						posId = truPosSet.iterator().next();
						System.out.println("rec pos id:" + posId);
						posTag = 1;
						// changeTag = param1.nbBasePosParam(qC.getqid(), posId, kChangeTag, kwChangeTag);
						// if(changeTag == 1) {System.out.println("change param successfully");}
						// Config.k = Math.min(Config.k, core[posId]);
					}
					if (negList.isEmpty() || truNegSet.isEmpty()) {
						// Log.log("rec neg precision: 0.0 rec neg target: 0.0");
						System.out.println("rec neg precision: 0.0 rec neg target: 0.0");
						if (avgtres[0] == 1.0) {
							recPosPre[recRound+1][2] += recPosPre[recRound+1][2] / count;
							recPosPre[recRound+1][3] += recPosPre[recRound+1][3] / count;
						}
						
					}
					else {
						recPosPre[recRound+1][2] += (double) truNegSet.size() / (double) trueNegSiz;
						recPosPre[recRound+1][3] += 1.0;
						System.out.println("rec neg precision:" + ((double) truNegSet.size() / (double) trueNegSiz) + " rec neg target: 1.0");
						negId = truNegSet.iterator().next();
						System.out.println("rec neg id:" + negId);
						negTag = 1;
					}
					if (posTag ==1 && negTag ==1) {
						Random rand = new Random();
				        int randAddDelete = rand.nextInt(2);
						if (randAddDelete == 1) {
							negTag = 0;
						} else {
							posTag = 0;
						}
					} 
					if (posTag == 1) {
						posNodes.add(posId);
					} 
					if (negTag == 1){
						negNodes.add(negId);
						
						for(int negNb: newgraph[negId]){
							List<Integer> negNbNbList = new ArrayList<Integer>();
							List<Integer> intList = new ArrayList<Integer>(newgraph[negNb].length-1);
							for (int i : newgraph[negNb])
							{	
								if(i != negId){
									negNbNbList.add(i);
								}
							}
							newgraph[negNb] = negNbNbList.stream().mapToInt(i -> i).toArray();
						}
						newgraph[negId] = new int[0];
						newnodes[negId] = new String[0];
						newcore[negId] = 0;

					}
					// if(changeTag == 0 && kChangeTag == 1) {
					// 	param1.nbBaseNegParam(qC.getqid(), negId, kChangeTag);
					// 	System.out.println("change param successfully");}
					// }
					}
		}
	}
	
	
	public static void main(String[] args) {
		
		baseExp exp = new baseExp();
		String graphFile, nodeFile, queryFile, groundFile;

		//test all
		String filePath = Config.dataFilePath;
		// String[] dataset = {"ama", "ytb", "dblp", "lj"};
		Scanner myObj = new Scanner(System.in);  // Create a Scanner object

        String ds = myObj.nextLine();  // Read user input
        graphFile = filePath + ds + "/" + ds + "-graph.txt";
		nodeFile = filePath + ds + "/" + ds + "-node.txt";
		queryFile = filePath + ds + "/" + ds + "-query.txt"; 
		groundFile = filePath + ds + "/" + ds + "-ground.txt";
		exp.expIndex(graphFile, nodeFile, queryFile, groundFile);
		
		
	}

}
