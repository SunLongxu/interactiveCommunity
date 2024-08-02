package hku.algo.recommend;

import hku.algo.TNode;
import hku.util.Log;
import java.util.*;

class PPRGainComparator implements Comparator<PPRVertexGain> {
    public int compare(PPRVertexGain vg1, PPRVertexGain vg2) {
        if (vg1.gain > vg2.gain) {
            return -1;
        } else if (vg1.gain < vg2.gain){
            return 1;
        } else {
            return 0;
        }
    }
}

class PPRVertexGain {
    public final Integer vid;
    public double gain;
    public Integer tag;

    public PPRVertexGain(Integer vid, Double gain, Integer tag) {
        this.vid = vid;
        this.gain = gain;
        this.tag = tag;
    }
}

public class Rec {
    private int graph[][];// graph structure
	private String nodes[][];// the keywords of each node
	private TNode root;// the built index
	private int core[];
    private Set<Integer> posNodes;
    private Set<Integer> negNodes;
    private int queryId;
    private Set<Integer> allResSet;

    public Rec(int graph[][], String nodes[][], TNode root, int core[], Set<Integer> posNodes, Set<Integer> negNodes, int queryId, Set<Integer> allResSet){
        this.graph = graph;
        this.nodes = nodes;
        this.root = root;
        this.core = core;
        this.posNodes = posNodes;
        this.negNodes = negNodes;
        this.queryId = queryId;
        this.allResSet = allResSet;
    }

    public List<Integer> recPPR(Integer[] rootNodes, HashSet<Integer> allNodes, int recTag){
        
        int[] level = new int[allNodes.size()];
        Queue<Integer> queue = new ArrayDeque<Integer>();
        // HashMap<Integer, ArrayList<Integer>> inGraph = new HashMap<>();
        int[] nodeMapper = new int[allNodes.size()];
        Map<Integer, Integer> noderMapper = new HashMap<>();
        List<Integer>[] adj = new ArrayList[allNodes.size()];
        for (int i = 0; i < allNodes.size(); i++){
            adj[i] = new ArrayList<>();}

        // damping factor: \alpha, persentage of random walk; min delta: end of the iteration
        double dampingFactor = 0.85;
        double minDelta = 0.0001;
        Set<String> attrQ = new HashSet<>(Arrays.asList(Arrays.copyOfRange(nodes[queryId], 1, nodes[queryId].length)));
        long time1 = System.nanoTime();
        // Mark the source node as visited and enqueue it
        for (int v = 0; v < rootNodes.length; v++) {
            // visited.add(rootNodes[v]);
            level[v] = 0;
            queue.add(v);
            noderMapper.put(rootNodes[v], v);
            nodeMapper[v] = rootNodes[v];
        }
        int cntNodeId = rootNodes.length;
        int levelMax = 0;
        // Form directed graph
        while (!queue.isEmpty()) {
            int v = queue.poll();
            int levelNow = level[v] + 1;

            for(int nb : graph[nodeMapper[v]]){
                if (allNodes.contains(nb) && !noderMapper.containsKey(nb)) {
                        nodeMapper[cntNodeId] = nb;
                        noderMapper.put(nb, cntNodeId);
                        // visited.add(nb);
                        level[cntNodeId] = levelNow;
                        queue.add(cntNodeId);
                        cntNodeId++;
                        if (levelNow>levelMax) {
                            levelMax = levelNow;
                        }
                }
            }
        }
        int maxIterations = levelMax+1;

        for (int i = 0; i < cntNodeId; i++) {
            for(int nb : graph[nodeMapper[i]]){
                if (allNodes.contains(nb)) {
                    adj[i].add(noderMapper.get(nb));
                    // adj[noderMapper.get(nb)].add(i);
                }
            }
        }
        // Log.log("size compare: "+ (allNodes.size() - visited.size()));
        int countOutNb;
        // double[] outNbSiz = new double[cntNodeId+1];
        // double[] pageRank = new double[cntNodeId+1];
        double[] outNbSiz = new double[cntNodeId];
        double[] pageRank = new double[cntNodeId];
        double dampingValue = (1.0 - dampingFactor) / (cntNodeId+1-rootNodes.length);
        // List<Integer>[] inGraph = new ArrayList[cntNodeId+1];
        List<Integer>[] inGraph = new ArrayList[cntNodeId];
        
        for (int i = 0; i < cntNodeId; i++) {
            inGraph[i] = new ArrayList<>(adj[i].size());
        }
        double sumRootOutNbSiz = 0.0;
        for (int v = 0; v < cntNodeId; v++) {
            // ArrayList<Integer> inNb = new ArrayList<>();
            // inGraph[v] = new ArrayList<>();
            countOutNb = 0;
            for (int nb  : adj[v]) {
                if (level[v] > level[nb]) {
                    inGraph[v].add(nb);
                } else {
                    if (level[nb] > 0) {
                        countOutNb++;
                        if (level[v] == level[nb]) {
                            inGraph[v].add(nb);
                        }
                    }
                }
            }
            // inGraph.put(v, inNb);
            outNbSiz[v] = countOutNb;
            if(v < rootNodes.length){
                sumRootOutNbSiz+=countOutNb;
                pageRank[v] = outNbSiz[v];
            //     if(countOutNb>0){
            //         inGraph[v].add(v);
            //     }
            } else {
                pageRank[v] = dampingValue;
            }
        }
        //Initialize root visual node 
        for (int i = 0; i < rootNodes.length; i++) {
            if (outNbSiz[i]>0) {
                pageRank[i] = dampingValue + dampingFactor*outNbSiz[i]/sumRootOutNbSiz;
            }
        }
        // pageRank[cntNodeId] = dampingValue+dampingFactor;
        // inGraph[cntNodeId] = new ArrayList<>(1);
        // inGraph[cntNodeId].add(cntNodeId);
        // outNbSiz[cntNodeId] = sumRootOutNbSiz;

        long time2 = System.nanoTime();

        double rank;
        double change;
        
        for (int pagerankIter = 0; pagerankIter < maxIterations; pagerankIter++) {
            change = 0.0;
            double[] oldpageRank = Arrays.copyOf(pageRank, pageRank.length);
            // for (Integer vInteger = rootNodes.length; vInteger < cntNodeId+1; vInteger++) {
            for (Integer vInteger = rootNodes.length; vInteger < cntNodeId; vInteger++) {
                rank = dampingValue;
                if (!inGraph[vInteger].isEmpty()) {
                    for (Integer inNb : inGraph[vInteger]) {
                        rank += (dampingFactor * oldpageRank[inNb]) / outNbSiz[inNb];
                    }
                }
                change += Math.abs(oldpageRank[vInteger] - rank);
                pageRank[vInteger] = rank;
                // System.out.println(vInteger+"PageRank:"+rank);
            }
            // Log.log("change: "+change);
            if (change < minDelta) {
                break;
            }

        }

        
        long time3 = System.nanoTime();
        // Recommendation process
        // Log.log("1st pageRank");
        // For Running Example Log Only
        // for (Integer vInteger = 0; vInteger < cntNodeId; vInteger++) {
        //     System.out.println(nodeMapper[vInteger]+":"+pageRank[vInteger]);
        // }
        // Form a HashMap to store the cover vertices for each vertex
        HashMap<Integer, HashSet<Integer>> coverVMap = new HashMap<Integer, HashSet<Integer>>();
        HashMap<Integer, HashSet<String>> coverKMap = new HashMap<Integer, HashSet<String>>();
        // Form a HashSet to store the covered vertices
        HashSet<Integer> coveredVSet = new HashSet<>();
        HashSet<String> coveredKSet = new HashSet<>();
        for (int v = 0; v < rootNodes.length; v++){
            pageRank[v] = Double.valueOf(1.0);
        
        }
        if (recTag == 0) {
            for (int vInteger = 0; vInteger < cntNodeId; vInteger++) {
                pageRank[vInteger] =  1.0 / pageRank[vInteger];
            }
        }
        // Log.log("2nd pageRank");
        // Recommendation Plan B:
        // Initialize a priority queue to store the vertices and their gain
        Queue<PPRVertexGain> dirQueue = new PriorityQueue<>(new PPRGainComparator());
        for (int v = rootNodes.length; v < cntNodeId; v++) {
            HashSet<Integer> coverVSet = new HashSet<>();
            HashSet<String> coverKSet = new HashSet<>();
            coverVSet.add(v);
            double rankV = pageRank[v];
            for (int nb : adj[v]){
                if (nb>=rootNodes.length) {
                    coverVSet.add(nb);
                    rankV += pageRank[nb];
                }
            }
            for(int i = 1; i < nodes[nodeMapper[v]].length; i ++){
                if(recTag == 1 && attrQ.contains(nodes[nodeMapper[v]][i])){
                    coverKSet.add(nodes[nodeMapper[v]][i]);
                } else if(recTag ==0){
                    coverKSet.add(nodes[nodeMapper[v]][i]);
                }
            }
            rank = (rankV * coverKSet.size())/(rankV + coverKSet.size());
            // For Running Example Log Only
            // System.out.println(nodeMapper[v]+":"+rankV+":"+coverKSet.size()+":"+rank);
            dirQueue.add(new PPRVertexGain(v, rank, 0));
            coverVMap.put(v, coverVSet);
            coverKMap.put(v, coverKSet);
        }
        
        long time4 = System.nanoTime();
        // Recommend r vertices to users
        // Integer recBudget :  number of recommend budget number
        // Integer recNum: number of recommended number
        int recBudget = 10;
        int recNum = 0;
        int canSiz = cntNodeId - rootNodes.length;
        if(recBudget > canSiz){
            recBudget = canSiz;
        }
        int recCount = recBudget;
        List<Integer> posList = new ArrayList<>();
        
        //New for store no keyword sharing Set
        Queue<PPRVertexGain> dirNoKQueue = new PriorityQueue<>(new PPRGainComparator());
        

        //Interactive process
        // Log.log("rec:");
        while (recNum < recCount && !dirQueue.isEmpty()){
            PPRVertexGain vg = dirQueue.poll();
            // For Running Example Log Only
            // System.out.println(nodeMapper[vg.vid]+":"+vg.gain);
            if (vg.tag < recNum) {
                HashSet<String> coverKSet = new HashSet<>(coverKMap.get(vg.vid));
                HashSet<String> removeKSet = new HashSet<>(coverKMap.get(vg.vid));
                removeKSet.retainAll(coveredKSet);
                HashSet<Integer> coverVSet = new HashSet<>(coverVMap.get(vg.vid));
                HashSet<Integer> removeVSet = new HashSet<>(coverVMap.get(vg.vid));
                removeVSet.retainAll(coveredVSet);
                double rankV = 0d;
                if (!removeKSet.isEmpty()) {
                    coverKSet.removeAll(removeKSet);
                    coverKMap.put(vg.vid, coverKSet);
                }
                if(!removeVSet.isEmpty()){
                    coverVSet.removeAll(removeVSet);
                    coverVMap.put(vg.vid, coverVSet);
                }
                for(Integer coverV : coverVSet){
                    rankV += pageRank[coverV];
                }
                rank = (rankV * coverKSet.size())/(rankV + coverKSet.size());
                if (coverKSet.isEmpty()) {
                    dirNoKQueue.add(new PPRVertexGain(vg.vid, rankV, 0));
                    // System.out.println(nodeMapper[vg.vid]+"newScore"+rankV);
                } else {
                    dirQueue.add(new PPRVertexGain(vg.vid, rank, recNum));
                }
            } else {
                posList.add(nodeMapper[vg.vid]);
                coveredVSet.addAll(coverVMap.remove(vg.vid));
                coveredKSet.addAll(coverKMap.remove(vg.vid));
                // System.out.println("Rec NodeId and Score:"+nodeMapper[vg.vid]+":"+vg.gain);
                // Log.log(recNum+":"+nodeMapper[vg.vid]);
                recNum++;
                // System.out.println("covered vertex set size:"+ coveredVSet.size() +"covered keyword set size:"+ coveredKSet.size() + "recommend vertex set size:"+ posList.size());
            }
        }
        while (recNum < recCount && !dirNoKQueue.isEmpty()){
            PPRVertexGain vg = dirNoKQueue.poll();
            // System.out.println(nodeMapper[vg.vid]+":"+vg.gain);
            if (vg.tag < recNum) {
                HashSet<Integer> coverVSet = new HashSet<>(coverVMap.get(vg.vid));
                HashSet<Integer> removeVSet = new HashSet<>(coverVMap.get(vg.vid));
                removeVSet.retainAll(coveredVSet);
                double rankV = 0d;
                if(!removeVSet.isEmpty()){
                    coverVSet.removeAll(removeVSet);
                    coverVMap.put(vg.vid, coverVSet);
                }
                for(Integer coverV : coverVSet){
                    rankV += pageRank[coverV];
                }
                dirNoKQueue.add(new PPRVertexGain(vg.vid, rankV, recNum));
            } else {
                posList.add(nodeMapper[vg.vid]);
                coveredVSet.addAll(coverVMap.remove(vg.vid));
                // System.out.println("Rec NodeId and Score:"+nodeMapper[vg.vid]+":"+vg.gain);
                // Log.log(recNum+":"+nodeMapper[vg.vid]);
                recNum++;
                // System.out.println("covered vertex set size:"+ coveredVSet.size() +"covered keyword set size:"+ coveredKSet.size() + "recommend vertex set size:"+ posList.size());
            }
        }
        long time5 = System.nanoTime();
        // Output the recommended vertices
        // System.out.println("time2:"+ (time2-time1)/1000000 + "time3:"+ (time3-time2)/1000000 + "time4:"+ (time4-time3)/1000000 + "time5:"+ (time5-time4)/1000000);
        return posList;
    }
    
    public List<Integer> recRnd(Set<Integer> posSet, Set<Integer> canSet, int recTag){
        HashSet<Integer> recSet = new HashSet<>(canSet);
        recSet.removeAll(posSet);
        List<Integer> recList= new ArrayList<>(recSet);
        Collections.shuffle(recList);
        int recBudget = 10;
        if (recList.size() < recBudget) {
            recBudget = recList.size();
        }
        recList = recList.subList(0, recBudget);
        return recList;
    }

    public HashSet<Integer> canPosBFS(HashSet<Integer> resSet){
        HashSet<Integer> canPosSet = new HashSet<Integer>();
        Queue<Integer> queue = new LinkedList<Integer>();
        // Queue<Integer> queue2 = new LinkedList<Integer>();
        canPosSet.add(queryId);
        int sizebound = 1000;
        for (int nb : graph[queryId]) {
            if (canPosSet.add(nb)) { 
                queue.add(nb);
                if (!resSet.contains(nb)) {
                    sizebound--;
                    if (sizebound == 0) {
                        return canPosSet;
                    }
                }
            }
        }
        
        while (!queue.isEmpty() && sizebound > 0) {
            int v = queue.poll();
            for (int nb : graph[v]) {
                if (canPosSet.add(nb)) { 
                    queue.add(nb);
                    if (!resSet.contains(nb)) {
                        sizebound--;
                        if (sizebound == 0) {
                            return canPosSet;
                        }
                    }
                }
            }
        }
        // for(int nb : graph[queryId]){
        //     queue.add(nb);
        //     canPosSet.add(nb);
        //     if (!resSet.contains(nb) ) {
        //         sizebound--;
        //         if (sizebound == 0) {
        //             break;
        //         }
        //     }
        // }
        
        // while (!queue.isEmpty() && sizebound > 0) {
        //     int v = queue.poll();
        //     for(int nb : graph[v]){
        //         if (!canPosSet.contains(nb)) {
        //             queue2.add(nb);
        //             canPosSet.add(nb);
        //             if (!resSet.contains(nb) ) {
        //                 sizebound--;
        //                 if (sizebound == 0) {
        //                     break;
        //                 }
        //             }
        //         }
        //     }
        // }
        // while (!queue2.isEmpty() && sizebound > 0) {
        //     int v = queue2.poll();
        //     for(int nb : graph[v]){
        //         if (!canPosSet.contains(nb)) {
        //             queue2.add(nb);
        //             canPosSet.add(nb);
        //             if (!resSet.contains(nb) ) {
        //                 sizebound--;
        //                 if (sizebound == 0) {
        //                     break;
        //                 }
        //             }
        //         }
        //     }
        // }
        // for(int v : resSet){
        //     if (!canPosSet.contains(v)) {
        //         canPosSet.add(v);
        //     }
        // }
        return canPosSet;
    }

    public List<Integer> recMain(int recTag, int recMethod) {
        List<Integer> recList = new ArrayList<>();
        HashSet<Integer> rootNodes = new HashSet<>(posNodes);
        HashSet<Integer> allNodes = new HashSet<>();

           
        if (recTag == 1) {
            rootNodes.addAll(allResSet);
            //bug fix
            rootNodes.removeAll(negNodes);
            long time0 = System.nanoTime();
            allNodes = canPosBFS(rootNodes);
            allNodes.addAll(rootNodes);
            long time1 = System.nanoTime();
            // System.out.println("time candidate:"+ (time1 - time0));
        } else {
            rootNodes.add(queryId);
            allNodes.addAll(rootNodes);
            allNodes.addAll(allResSet);
        }
        allNodes.removeAll(negNodes);
        
        Integer[] rootNodesArr = new Integer[rootNodes.size()];
        rootNodes.toArray(rootNodesArr);
        if (recMethod == 1) {
            recList = recRnd(rootNodes, allNodes, recTag);
        } else {
            recList = recPPR(rootNodesArr, allNodes, recTag);
        }
        return recList;
    }
}