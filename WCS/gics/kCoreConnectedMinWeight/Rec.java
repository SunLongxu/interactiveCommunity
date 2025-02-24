package kCoreConnectedMinWeight;

import java.util.*;

// class NodeNeighbour {
//     Integer nodeIndex;
//     double weight;
// }

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
    HashMap<Integer, ArrayList<NodeNeighbour>> graph;// graph structure
    private Set<Integer> posNodes;
    private Set<Integer> negNodes;
    private ArrayList<Integer> queryList;
    private Set<Integer> allResSet;

    public Rec(HashMap<Integer, ArrayList<NodeNeighbour>> graph, Set<Integer> posNodes, Set<Integer> negNodes, ArrayList<Integer> queryList, Set<Integer> allResSet){
        this.graph = graph;
        this.posNodes = posNodes;
        this.negNodes = negNodes;
        this.queryList = queryList;
        this.allResSet = allResSet;
    }

    public List<Integer> recPPR(Integer[] rootNodes, Set<Integer> allNodes, int recTag){
        List<Integer> posList = new ArrayList<Integer>();
        
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
        int maxIterations = 20;
        double minDelta = 0.0001;
        // Set<String> attrQ = new HashSet<>(Arrays.asList(Arrays.copyOfRange(nodes[queryId], 1, nodes[queryId].length)));
        // Mark the source node as visited and enqueue it
        for (int v = 0; v < rootNodes.length; v++) {
            // visited.add(rootNodes[v]);
            level[v] = 0;
            queue.add(v);
            noderMapper.put(rootNodes[v], v);
            nodeMapper[v] = rootNodes[v];
        }
        int cntNodeId = rootNodes.length;

        // Form directed graph
        while (!queue.isEmpty()) {
            int v = queue.poll();
            int levelNow = level[v] + 1;

            for(NodeNeighbour nb : graph.get(nodeMapper[v])){
                if (allNodes.contains(nb.nodeIndex) && !noderMapper.containsKey(nb.nodeIndex)) {
                        nodeMapper[cntNodeId] = nb.nodeIndex;
                        noderMapper.put(nb.nodeIndex, cntNodeId);
                        level[cntNodeId] = levelNow;
                        queue.add(cntNodeId);
                        cntNodeId++;
                }
            }
        }
        for (int i = 0; i < cntNodeId; i++) {
            for(NodeNeighbour nb : graph.get(nodeMapper[i])){
                if (allNodes.contains(nb.nodeIndex)) {
                    adj[i].add(noderMapper.get(nb.nodeIndex));
                }
            }
        }
        // Log.log("size compare: "+ (allNodes.size() - visited.size()));
        int countOutNb;
        
        double[] outNbSiz = new double[cntNodeId];
        double[] pageRank = new double[cntNodeId];
        int dirGSize = cntNodeId;
        double dampingValue = (1.0 - dampingFactor) / dirGSize;
        List<Integer>[] inGraph = new ArrayList[cntNodeId];
        for (int i = 0; i < cntNodeId; i++) {
            inGraph[i] = new ArrayList<>(adj[i].size());
        }
        for (int v = 0; v < cntNodeId; v++) {
            // ArrayList<Integer> inNb = new ArrayList<>();
            // inGraph[v] = new ArrayList<>();
            countOutNb = 0;
            for (int nb  : adj[v]) {
                if (level[v] > level[nb]) {
                    inGraph[v].add(nb);
                } else {
                    countOutNb++;
                    if (level[v] == level[nb] && level[v] > 0) {
                        inGraph[v].add(nb);
                    }
                }
            }
            // inGraph.put(v, inNb);
            outNbSiz[v] = countOutNb;
            if(v < rootNodes.length){
                pageRank[v] = outNbSiz[v];
            } else {
                pageRank[v] = dampingValue;
            }
        }
        
        //int dirGSize = cntNodeId;
        //double dampingValue = (1.0 - dampingFactor) / dirGSize;
        double rank;
        double change;
        
        for (int pagerankIter = 0; pagerankIter < maxIterations; pagerankIter++) {
            change = 0.0;
            for (Integer vInteger = rootNodes.length; vInteger < cntNodeId; vInteger++) {
                rank = dampingValue;
                if (!inGraph[vInteger].isEmpty()) {
                    for (Integer inNb : inGraph[vInteger]) {
                        rank += dampingFactor * pageRank[inNb] / outNbSiz[inNb];
                    }
                }
                change += Math.abs(pageRank[vInteger] - rank);
                pageRank[vInteger] = rank;
                
            }
            // Log.log("change: "+change);
            
            if (change < minDelta) {
                break;
            }

        }
        // long time3 = System.nanoTime();
        // Recommendation process
        
        
        // Form a HashMap to store the cover vertices for each vertex
        HashMap<Integer, HashSet<Integer>> coverVMap = new HashMap<Integer, HashSet<Integer>>();
        // HashMap<Integer, HashSet<String>> coverKMap = new HashMap<Integer, HashSet<String>>();
        // Form a HashSet to store the covered vertices
        HashSet<Integer> coveredVSet = new HashSet<>();
        HashSet<String> coveredKSet = new HashSet<>();
        for (int v = 0; v < rootNodes.length; v++){
            pageRank[v] = Double.valueOf(1.0);
        
        }
        if (recTag == 0) {
            for (int vInteger = 0; vInteger < cntNodeId; vInteger++) {
                pageRank[vInteger] =  (-1) * pageRank[vInteger];
            }
        }
        // Recommendation Plan B:
        // Initialize a priority queue to store the vertices and their gain
        Queue<PPRVertexGain> dirQueue = new PriorityQueue<>(new PPRGainComparator());
        for (int v = rootNodes.length; v < cntNodeId; v++) {
            HashSet<Integer> coverVSet = new HashSet<>();
            coverVSet.add(v);
            double rankV = pageRank[v];
            for (int nb : adj[v]){
                    coverVSet.add(nb);
                    rankV += pageRank[nb];
            }
            rank = rankV;
            dirQueue.add(new PPRVertexGain(v, rank, 0));
            coverVMap.put(v, coverVSet);
            // coverKMap.put(v, coverKSet);
        }
        // long time4 = System.nanoTime();
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
        //Interactive process
        while (recNum < recCount && !dirQueue.isEmpty()){
            PPRVertexGain vg = dirQueue.poll();
            // System.out.println(vg.vid+":"+vg.gain);
            if (vg.tag < recNum) {
                // HashSet<String> coverKSet = new HashSet<>(coverKMap.get(vg.vid));
                // HashSet<String> removeKSet = new HashSet<>(coverKMap.get(vg.vid));
                // removeKSet.retainAll(coveredKSet);
                HashSet<Integer> coverVSet = new HashSet<>(coverVMap.get(vg.vid));
                HashSet<Integer> removeVSet = new HashSet<>(coverVMap.get(vg.vid));
                removeVSet.retainAll(coveredVSet);
                double rankV = 0d;
                // if (!removeKSet.isEmpty()) {
                //     coverKSet.removeAll(removeKSet);
                //     coverKMap.put(vg.vid, coverKSet);
                // }
                if(!removeVSet.isEmpty()){
                    coverVSet.removeAll(removeVSet);
                    coverVMap.put(vg.vid, coverVSet);
                }
                for(Integer coverV : coverVSet){
                    rankV += pageRank[coverV];
                }
                // rank = (rankV * coverKSet.size())/(rankV + coverKSet.size());
                // if (coverKSet.isEmpty()) {
                //     rank = rankV;
                // }
                rank = rankV;
                dirQueue.add(new PPRVertexGain(vg.vid, rank, recNum));
            
            } else {
                posList.add(nodeMapper[vg.vid]);
                coveredVSet.addAll(coverVMap.remove(vg.vid));
                // coveredKSet.addAll(coverKMap.remove(vg.vid));
                recNum++;
                // System.out.println("covered vertex set size:"+ coveredVSet.size() +"covered keyword set size:"+ coveredKSet.size() + "recommend vertex set size:"+ posList.size());
            }
        }
        // long time5 = System.nanoTime();
        // Output the recommended vertices
        // posList.forEach(System.out::println);
        // Log.log("time2:"+ (time2-time1)/1000000 + "time3:"+ (time3-time2)/1000000 + "time4:"+ (time4-time3)/1000000 + "time5:"+ (time5-time4)/1000000);
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
        HashSet<Integer> canPosSet = new HashSet<Integer>(queryList);
        Queue<Integer> queue = new LinkedList<Integer>();
        // Queue<Integer> queue2 = new LinkedList<Integer>();
        // canPosSet.add(queryId);
        int sizebound = 1000;
        for (Integer queryv : queryList) {
            for(NodeNeighbour nb : graph.get(queryv)){
                if (canPosSet.add(nb.nodeIndex)){
                queue.add(nb.nodeIndex);
				if (!resSet.contains(nb)) {
                    sizebound--;
                    if (sizebound == 0) {
                        return canPosSet;
                    }
                }
				}
            }
        }
        
        
        while (!queue.isEmpty() && sizebound > 0) {
            //if (canPosSet.size() > sizebound) {
            //    break;
            //}
            int v = queue.poll();
            for(NodeNeighbour nb : graph.get(v)){
                if ( canPosSet.add(nb.nodeIndex)) {
                   
                    queue.add(nb.nodeIndex);
                    if (!resSet.contains(nb.nodeIndex)) {
                        sizebound--;
                        if (sizebound == 0) {
                            return canPosSet;
                        }
                    }
                }
            }
        }
        //while (!queue2.isEmpty()) {
        //    if (canPosSet.size() > sizebound) {
        //        break;
        //    }
        //    int v = queue2.poll();
        //    for(NodeNeighbour nb : graph.get(v)){
        //        if (!canPosSet.contains(nb.nodeIndex)) {
        //            canPosSet.add(nb.nodeIndex);
        //            queue2.add(nb.nodeIndex);
        //        }
        //    }
        //}
        return canPosSet;
    }

    public List<Integer> recMain(int recTag, int recMethod) {
        List<Integer> recList = new ArrayList<>();
        HashSet<Integer> rootNodes = new HashSet<>(posNodes);
        rootNodes.addAll(queryList);
        HashSet<Integer> allNodes = new HashSet<>();

           
        if (recTag == 1) {
            rootNodes.addAll(allResSet);
            //bug fix
            rootNodes.removeAll(negNodes);
            long time0 = System.nanoTime();
            allNodes = canPosBFS(rootNodes);
            allNodes.addAll(rootNodes);
            long time1 = System.nanoTime();
            System.out.println("time candidate:"+ (time1 - time0));
        } else {
            // rootNodes.add(queryList);
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