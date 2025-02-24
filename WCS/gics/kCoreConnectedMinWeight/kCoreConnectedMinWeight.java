package kCoreConnectedMinWeight;

import java.io.*;
import java.util.*;
import kCoreConnectedMinWeight.*;

class NodeNeighbour {
    Integer nodeIndex;
    double weight;
}

class Vertex implements Comparable<Vertex> {
    public final Integer name;
    public double minDistance = Double.POSITIVE_INFINITY;
    public Vertex previous;

    public Vertex(Integer argName) {
        name = argName;
    }

    Boolean isSpt = false;
    public int level = 0;

    public int compareTo(Vertex other) {
        return Double.compare(minDistance, other.minDistance);
    }

    public void reset() {
        minDistance = Double.POSITIVE_INFINITY;
        previous = null;
        isSpt = false;
        level = 0;
    }
}

class shortPathList {
    public HashMap<Integer, ArrayList<NodeNeighbour>> pwg;
    public HashMap<Integer, ArrayList<Integer>> pwgEdge;
    public ArrayList<Integer> pathNodeList;
    public Integer q;
}

class pathList {
    public ArrayList<Vertex> pathVertex;
    public ArrayList<Integer> pathString;

}

class expandSet {
    public HashSet<Integer> expRes;
    public HashSet<Integer> expLast;
}

// class GainComparator implements Comparator<VertexGain> {
//     public int compare(VertexGain vg1, VertexGain vg2) {
//         return vg2.gain.compareTo(vg1.gain);
//     }
// }


// class VertexGain {
//     public final Integer vid;
//     public Integer gain;
//     public Integer tag;

//     public VertexGain(Integer vid, Integer gain, Integer tag) {
//         this.vid = vid;
//         this.gain = gain;
//         this.tag = tag;
//     }

// }

// class PPRGainComparator implements Comparator<PPRVertexGain> {
//     public int compare(PPRVertexGain vg1, PPRVertexGain vg2) {
//         if (vg1.gain > vg2.gain) {
//             return -1;
//         } else if (vg1.gain < vg2.gain){
//             return 1;
//         } else {
//             return 0;
//         }
//     }
// }

// class PPRVertexGain {
//     public final Integer vid;
//     public double gain;
//     public Integer tag;

//     public PPRVertexGain(Integer vid, Double gain, Integer tag) {
//         this.vid = vid;
//         this.gain = gain;
//         this.tag = tag;
//     }
// }

public class kCoreConnectedMinWeight {

    private int maxDegree = -1;
    private int VertexMax = -1;
    private int[] BinInitialPos = null;
    private int[] coreTable = null;// 1,...,VertexMax
    private int[] degreeTable = null;// 1,...,VertexMax
    private int[] degreeTableCopy = null;// 1,...,VertexMax
    private int[] degreeRemoveTable = null;// 1,...,VertexMax
    private double connectedSubGraphWeightSum = -1;
    private double finalGraphWeightSum = -1;
    private int treeLevel = -1;

    // degreeTable: index is vertex number, value is degree
    int[] degreeTable(HashMap<Integer, ArrayList<NodeNeighbour>> hm){
        
        degreeTable = new int[VertexMax+1];//1...vertexMax, not use 0;
        
        for (Map.Entry<Integer, ArrayList<NodeNeighbour>> entry : hm.entrySet()) {
            
            
            int vertexDegree = entry.getValue().size();
            
            if(vertexDegree > maxDegree)
                maxDegree = vertexDegree;
            
            degreeTable[entry.getKey()] = vertexDegree;
            
        }
        
        return degreeTable;
    }

    ArrayList<int[]> binSort(int[] degreeTable){
        
        int[] AllBinSize = new int[maxDegree+1];//0,....,maxDegree
        
        for(int start = 1; start < degreeTable.length; start++){
            AllBinSize[degreeTable[start]]++;
        }
        
        int[] AllBinPos = new int[maxDegree+1];//0,....,maxDegree
        
        AllBinPos[0] = 1;

        for(int i=1; i < AllBinPos.length;i++){
            AllBinPos[i] = AllBinPos[i-1] + AllBinSize[i-1];
        }
        AllBinSize = null;
        
        BinInitialPos = new int[maxDegree+1];
        System.arraycopy(AllBinPos, 0, BinInitialPos, 0, AllBinPos.length );

        int[] vertTable = new int[VertexMax+1];
        int[] posTable =  new int[VertexMax+1];
        
        for(int vertexId = 1; vertexId < degreeTable.length; vertexId++){
            vertTable[AllBinPos[degreeTable[vertexId]]] = vertexId;// sorted table
            posTable[vertexId] = AllBinPos[degreeTable[vertexId]];
            AllBinPos[degreeTable[vertexId]]++;
        }

        AllBinPos = null;
        System.gc();
        System.runFinalization();

        ArrayList<int[]> result = new ArrayList<int[]>();
        result.add(vertTable);
    
        result.add(posTable);
        
        return result;
    }

    int getMaxCore(HashMap<Integer, ArrayList<NodeNeighbour>> hm) {

        coreTable = new int[VertexMax + 1];

        int[] degreeTable = degreeTable(hm);

        degreeTableCopy = new int[VertexMax + 1];
        System.arraycopy(degreeTable, 0, degreeTableCopy, 0, degreeTable.length);

        ArrayList<int[]> result = binSort(degreeTable);
        int[] vertTable = result.get(0);
        int[] posTable = result.get(1);

        int maxCore = -1;

        for (int start = 1; start < vertTable.length; start++) {

            coreTable[vertTable[start]] = degreeTable[vertTable[start]];

            if (degreeTable[vertTable[start]] > maxCore){
                maxCore = degreeTable[vertTable[start]];}

            ArrayList<NodeNeighbour> neighbors = new ArrayList<NodeNeighbour>();
            neighbors = hm.get(vertTable[start]);

            if(neighbors!=null){
                for(NodeNeighbour nodeNeighbor : neighbors){
                    Integer neighborInt = nodeNeighbor.nodeIndex;
                    if(degreeTable[neighborInt] > degreeTable[vertTable[start]]){
                        int originalDegree = degreeTable[neighborInt];
                        degreeTable[neighborInt]--;
                        //swap in verTable
                        int startBin = BinInitialPos[originalDegree];
                        int neighborIndex = posTable[neighborInt]; 
                        
                        int temp = vertTable[startBin];
                        vertTable[startBin] = neighborInt;
                        vertTable[neighborIndex] = temp;
                        //swap the position
                        posTable[neighborInt] = startBin;
                        posTable[temp]=neighborIndex;                   
                        //update the All BinInitialPos
                        BinInitialPos[originalDegree]++;
                    }
                }
            } 

        }

        return maxCore;
    }

    ArrayList<Integer> getVertexLargerCore(int queryCore) {
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int index = 1; index < coreTable.length; index++) {
            if (coreTable[index] >= queryCore)
                result.add(index);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> getConnectedSubGraph(HashMap<Integer, ArrayList<NodeNeighbour>> hm, Integer[] queryNodes, HashSet<Integer> hsSub) {
        ArrayList<Integer> newConnectedCoreTable = new ArrayList<Integer>();
        HashSet<Integer> hs = new HashSet<Integer>();

        Queue queue = new LinkedList<Integer>();
        queue.add(queryNodes[0]);
        hs.add(queryNodes[0]);

        while (!queue.isEmpty()) {
            Integer node = (Integer) queue.remove();

            ArrayList<NodeNeighbour> al = hm.get(node);
            if (al != null) {
                for (NodeNeighbour nb : al) {
                    Integer neighbourIndex = nb.nodeIndex;
                    if (hsSub.contains(neighbourIndex) && (!hs.contains(neighbourIndex))) {
                        hs.add(neighbourIndex);
                        queue.add(neighbourIndex);
                    }
                }
            }
        }

        for (Integer s : hs) {
            newConnectedCoreTable.add(s);
        }
        hs.clear();
        return newConnectedCoreTable;
    }

    @SuppressWarnings("unchecked")
    boolean checkQueryConnectivity(HashMap<Integer, ArrayList<NodeNeighbour>> hm, Integer[] queryNodes, HashSet<Integer> hsSub) {
        
        HashSet<Integer> hs = new HashSet<Integer>();
        HashSet<Integer> qn = new HashSet<Integer>();
        Queue queue = new LinkedList<Integer>();
        queue.add(queryNodes[0]);
        hs.add(queryNodes[0]);

        for (Integer q : queryNodes){
            qn.add(q);
        }
        qn.remove(queryNodes[0]);

        while (!queue.isEmpty()) {
            Integer node = (Integer) queue.remove();

            ArrayList<NodeNeighbour> al = hm.get(node);
            if (al != null) {
                for (NodeNeighbour nb : al) {
                    Integer neighbourIndex = nb.nodeIndex;
                    if (hsSub.contains(neighbourIndex) && (!hs.contains(neighbourIndex))) {
                        hs.add(neighbourIndex);
                        queue.add(neighbourIndex);
                        if (qn.contains(neighbourIndex)) {
                            qn.remove(neighbourIndex);
                            if (qn.isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }


        return false;
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> removeRecursive(Integer deleteNode, ArrayList<Integer> connectedSubGraph,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm, Integer[] queryNodes, HashSet<Integer> hsSub2, int queryCore) {
        ArrayList<Integer> needRemove = new ArrayList<Integer>();

        HashSet<Integer> hs = new HashSet<Integer>();

        Queue queue = new LinkedList<Integer>();
        queue.add(deleteNode);
        hs.add(deleteNode);

        // shouldn't change degreee table now since may go to the next largest
        degreeRemoveTable = new int[VertexMax + 1];
        // BinInitialPos = AllBinPos;
        System.arraycopy(degreeTableCopy, 0, degreeRemoveTable, 0, degreeTableCopy.length);

        while (!queue.isEmpty()) {
            Integer node = (Integer) queue.remove();

            ArrayList<NodeNeighbour> al = hm.get(node);
            if (al != null) {
                for (NodeNeighbour nb : al) {
                    Integer neighbourIndex = nb.nodeIndex;
                    if (hsSub2.contains(neighbourIndex) && (!hs.contains(neighbourIndex))) {
                        degreeRemoveTable[neighbourIndex]--;
                        if (degreeRemoveTable[neighbourIndex] < queryCore) {
                            hs.add(neighbourIndex);
                            queue.add(neighbourIndex);
                        }
                    }
                }
            }
        }

        
        for (Integer qn : queryNodes)
            if (hs.contains(qn))
                return null;

        for (Integer s : hs) {
            needRemove.add(s);
        }

        // if return means we wont do this step
        // tricky part
        degreeTableCopy = degreeRemoveTable;

        return needRemove;
    }

    double getGraphWeightSum(ArrayList<Integer> subGraph, HashSet<Integer> hs,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm) {
        double result = 0;

        if (subGraph != null) {
            for (int node : subGraph) {
                ArrayList<NodeNeighbour> al = hm.get(node);
                if (al != null) {
                    for (NodeNeighbour nb : al) {
                        if (hs.contains(nb.nodeIndex)) {
                            result += nb.weight;
                        }
                    }
                }
            }
        }

        return result / 2;
    }

    ArrayList<Integer> getSubGraph(ArrayList<Integer> vertexLargerCoreList,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm, Integer[] queryNodes, int queryCore,
            HashSet<Integer> protectNodes, Integer removalN) {

        int initialVertexSize = vertexLargerCoreList.size();
        HashSet<Integer> hsSub = new HashSet<Integer>();
        ArrayList<Integer> connectedSubGraph = new ArrayList<Integer>();

        for (int node : vertexLargerCoreList){
            hsSub.add(node);
            connectedSubGraph.add(node);
        }

        // bug 1 not stop, is set #core = 10 while the max is 6
        for (Integer qn : queryNodes) {
            if (!hsSub.contains(qn)) {
                // finalGraphWeightSum = -1;
                return null;
            }
        }
        // ArrayList<Integer> connectedSubGraph = getConnectedSubGraph(hm, queryNodes, hsSub);
        

        // int initialConnectedVertexSize = connectedSubGraph.size();
        //
        // System.out.println("Finish finding the connected larger than core graph");
        // System.out.println("the connected larger than core graph size: " +
        // initialConnectedVertexSize);
        //
        // System.out.println("========================");

        HashSet<Integer> hsSub2 = new HashSet<Integer>();
        for (int node : connectedSubGraph)
            hsSub2.add(node);

        // connectedSubGraphWeightSum = getGraphWeightSum(connectedSubGraph, hsSub2, hm);

        // upadate new graph degree
        for (Integer nodeConnected : connectedSubGraph) {
            ArrayList<NodeNeighbour> al = hm.get(nodeConnected);
            if (al != null) {
                for (NodeNeighbour nb : al) {
                    Integer neighbourIndex = nb.nodeIndex;
                    if (!hsSub2.contains(neighbourIndex)) {
                        degreeTableCopy[nodeConnected]--;

                    }
                }
            }
        }

        HashSet<Integer> hs = new HashSet<Integer>();
        for (Integer qn : queryNodes)
            hs.add(qn);
        // ******************new to add protect nodes*****************************
        hs.addAll(protectNodes);
        // System.out.println("hssize:"+hs.size()+">"+protectNodes.size());

        // start to iterate delete the nodes
        int flag = 0;
        Integer startSarchNode = queryNodes[0];

        int iterationTimesForOutput = 1;

        boolean transferLinear = false;
        HashSet<Integer> hsCheckedNodesCouldntDelete = new HashSet<Integer>();

        while (flag != -1) {
            flag = -1;
            double maxWeight = -1;
            Integer removeNode = -1;

            int len = connectedSubGraph.size();
            for (Integer inte : hs) {
                if (hsSub2.contains(inte)) {
                    len--;
                }
            }
            // System.out.println("inintial size:"+ connectedSubGraph.size());

            double[][] weightsumSort = new double[len][2];// remember to int for the node index

            int indexSort = 0;

            outer:
            for (Integer node : connectedSubGraph) {
                // System.out.println("node:"+ node);
                if (!hs.contains(node)) {// detect if query nodes or not

                    ArrayList<NodeNeighbour> al = hm.get(node);
                    // double count = 0;//new added
                    if (al != null) {
                        for (NodeNeighbour nb : al) {
                            if (hsSub2.contains(nb.nodeIndex)) {
                                weightsumSort[indexSort][0] = nb.weight;// new added
                                weightsumSort[indexSort][1] = (double) node;
                                indexSort++;
                                continue outer;
                                // count++;// new added
                            }
                        }
                    }

                }

            }

            Arrays.sort(weightsumSort, Comparator.comparing((double[] arr) -> arr[0]).reversed());

            ArrayList<Integer> removeGraph = null;

            boolean haveSthToRemove = false;
            boolean queryDisconnect = false;
            
            // transfer only one need to change
            int batchsize = weightsumSort.length / 2;
            // int batchsize = 1;

            if ((batchsize > 50) && (!transferLinear)) {
                int deleteCountNodes = 0;
                for (int i = 0; i < batchsize; i++) {
                    Integer deleteNode = (Integer) (int) weightsumSort[i][1];
                    if (hsSub2.contains(deleteNode)) {
                        ArrayList<Integer> removeBatchCollectGraph = removeRecursive(deleteNode, connectedSubGraph, hm,
                            queryNodes, hsSub2, queryCore);
                        if (removeBatchCollectGraph != null) {
                            Set<Integer> removalSet = new HashSet<Integer>(removeBatchCollectGraph);
                            for (Integer removeNodeIterate : removeBatchCollectGraph) {
                                connectedSubGraph.remove(removeNodeIterate);
                                hsSub2.remove(removeNodeIterate);
                            }
                            
                            // check if all queries are connected with each other
							boolean checkCon = checkQueryConnectivity(hm, queryNodes, hsSub2);
							if (! checkCon && ! queryDisconnect){
                                if (removalSet.contains(removalN)) {
                                    queryDisconnect = true;
                                    haveSthToRemove = true;
                                    deleteCountNodes++;
                                    continue;
                                }
								for (Integer removeNodeIterate : removeBatchCollectGraph) {
                                    connectedSubGraph.add(removeNodeIterate);
                                    hsSub2.add(removeNodeIterate);
                                }
							} else{
                                haveSthToRemove = true;
                                deleteCountNodes++;
							}
                        }
                    } else {
						haveSthToRemove = true;
                        deleteCountNodes++;
                    }
                }
                if (deleteCountNodes <= batchsize / 2) {
                    transferLinear = true;
                    haveSthToRemove = true;// avoid the case all the first 10% nodes couldnt delete, then we set true
                                           // here to avoid end the delete progress and give it the chance to linear
                                           // delete
                }
            }

            if ((batchsize <= 50) || (transferLinear)) {
                for (int i = 0; i < weightsumSort.length; i++) {
                    Integer deleteNode = (Integer) (int) weightsumSort[i][1];
                    if (!hsCheckedNodesCouldntDelete.contains(deleteNode) && hsSub2.contains(deleteNode)) {
                        removeGraph = removeRecursive(deleteNode, connectedSubGraph, hm, queryNodes, hsSub2, queryCore);
                        if (removeGraph != null) {
                            Set<Integer> removalSet = new HashSet<Integer>(removeGraph);
                            for (Integer removeNodeIterate : removeGraph) {
                                connectedSubGraph.remove(removeNodeIterate);
                                hsSub2.remove(removeNodeIterate);
                            }
							// check if all queries are connected with each other
							boolean checkCon = checkQueryConnectivity(hm, queryNodes, hsSub2);
							if (! checkCon && ! queryDisconnect){
								if (removalSet.contains(removalN)) {
                                    queryDisconnect = true;
                                    haveSthToRemove = true;
                                    System.out.println("query disconnected cause by removal node");
                                    break;
                                }
                                for (Integer removeNodeIterate : removeGraph) {
                                    connectedSubGraph.add(removeNodeIterate);
                                    hsSub2.add(removeNodeIterate);
                                }
                                hsCheckedNodesCouldntDelete.add(deleteNode);
							} else{
                            	haveSthToRemove = true;
                            	break;
							}
                        } else {
                            hsCheckedNodesCouldntDelete.add(deleteNode);
                        }
                    }
                }
            }
            // check if the connected graph contains all the querynodes
            // ArrayList<Integer> newconnectedSubGraph = getConnectedSubGraph(hm, queryNodes, hsSub2);
            // which means the initial connected graph couldnt contain all the query nodes
            
            // if (newconnectedSubGraph == null){
            //     System.out.println("query disconnected");
            // } else if (connectedSubGraph.size() != newconnectedSubGraph.size()) {
            //     ArrayList<Integer> unConnectedSubGraph = new ArrayList<Integer>(connectedSubGraph);
            //     unConnectedSubGraph.removeAll(newconnectedSubGraph);
            //     for (Integer unConnectedNode : unConnectedSubGraph){
            //         connectedSubGraph.remove(unConnectedNode);
            //         hsSub2.remove(unConnectedNode);
            //     }
            //     //System.out.println("newconnectedsubgraph!=connected");
            // }
            // if(removeGraph!=null){
            // if(removeGraph.size()!=0){
            if (haveSthToRemove == true) {
                flag = 0;
                // System.out.println("Finish the " + iterationTimesForOutput + "th time remove
                // iteration");
                // System.out.println("Finish the " + iterationTimesForOutput + "th core graph
                // size: " + connectedSubGraph.size());
                // System.out.println("========================");
                iterationTimesForOutput++;

                // for the second test to get the 10 nodes
                // if(connectedSubGraph.size()<12){
                // return connectedSubGraph;
                // }
            }
            // }

        }

        finalGraphWeightSum = getGraphWeightSum(connectedSubGraph, hsSub2, hm);

        return connectedSubGraph;
    }


    // *************************New to find shortest path********************
    public shortPathList computePaths(Vertex source, List<Integer> list, HashMap<Integer, Vertex> hmv,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm, HashMap<Integer, ArrayList<NodeNeighbour>> pwg,
            HashMap<Integer, ArrayList<Integer>> pwgEdge, int queryCore) {
        source.minDistance = 0.;

        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);
        // hashset is used to store nodes which changed values
        HashSet<Integer> hs = new HashSet<Integer>();
        hs.add(source.name);
        // Initialization return object
        shortPathList spl = new shortPathList();
        // transfer pathweightgraph
        // spl.pwg = new HashMap<Integer, ArrayList<NodeNeighbour>>(pwg);
        // // transfer pathweightgraphedges
        // spl.pwgEdge = new HashMap<Integer, ArrayList<Integer>>(pwgEdge);
        spl.pathNodeList = new ArrayList<Integer>();

        labelA: while (!vertexQueue.isEmpty() && list.size() != 0) {
            Vertex u = vertexQueue.poll();
            u.isSpt = Boolean.TRUE;

            if (list.contains(u.name)) {
                spl.q = u.name;
                // System.out.println("Distance to " + u + ": " + u.minDistance);
                pathList pList = getShortestPathTo(u);
                // System.out.println("Path: " + pList.pathString);

                // // add edges to shortpathlist query node hash map
                // for (int i = pList.pathVertex.size() - 1; i > 0; i--) {
                //     Vertex node1 = pList.pathVertex.get(i);
                //     Vertex node2 = pList.pathVertex.get(i - 1);

                //     NodeNeighbour nb1 = new NodeNeighbour();
                //     nb1.nodeIndex = node2.name;
                //     nb1.weight = node2.minDistance - node1.minDistance;
                //     if (spl.pwg.containsKey(node1.name)) {
                //         // Boolean test1 = spl.pwg.get(node1.name).contains(nb1);
                //         if (!spl.pwgEdge.get(node1.name).contains(node2.name)) {
                //             spl.pwg.get(node1.name).add(nb1);
                //             spl.pwgEdge.get(node1.name).add(node2.name);
                //             // System.out.println(test1);
                //         }
                //     } else {
                //         ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>();
                //         ArrayList<Integer> alEdge = new ArrayList<Integer>();
                //         al.add(nb1);
                //         alEdge.add(node2.name);
                //         spl.pwg.put(node1.name, al);
                //         spl.pwgEdge.put(node1.name, alEdge);
                //     }

                //     NodeNeighbour nb2 = new NodeNeighbour();
                //     nb2.nodeIndex = node1.name;
                //     nb2.weight = node2.minDistance - node1.minDistance;
                //     if (spl.pwg.containsKey(node2.name)) {
                //         // Boolean test2 = spl.pwg.get(node2.name).contains(nb2);
                //         if (!spl.pwgEdge.get(node2.name).contains(node1.name)) {
                //             spl.pwg.get(node2.name).add(nb2);
                //             spl.pwgEdge.get(node2.name).add(node1.name);
                //             // System.out.println(test2);
                //         }

                //     } else {
                //         ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>();
                //         ArrayList<Integer> alEdge = new ArrayList<Integer>();
                //         al.add(nb2);
                //         alEdge.add(node1.name);
                //         spl.pwg.put(node2.name, al);
                //         spl.pwgEdge.put(node2.name, alEdge);
                //     }

                // }

                // add path to path node list
                spl.pathNodeList.addAll(pList.pathString);
                break labelA;

            }

            // Visit each edge exiting u
            ArrayList<NodeNeighbour> neighbours = hm.get(u.name);

            if (neighbours != null) {
                for (NodeNeighbour nb : neighbours) {
                    if (hmv.containsKey(nb.nodeIndex)) {
                        Vertex v = hmv.get(nb.nodeIndex);
                        if (!v.isSpt) {
                            double weight = nb.weight;
                            double distanceThroughU = u.minDistance + weight;
                            if (distanceThroughU < v.minDistance) {
                                vertexQueue.remove(v);

                                v.minDistance = distanceThroughU;
                                v.previous = u;
                                vertexQueue.add(v);
                            }
                        }
                        if (!hs.contains(nb.nodeIndex)) {
                            hs.add(nb.nodeIndex);
                        }
                    }
                }
            }
        }
        for (Integer s : hs) {
            Vertex v = hmv.get(s);
            v.reset();
        }
        if (spl.q != null || list.size() == 0) {
            return spl;
        } else {
            return null;
        }
    }

    public pathList getShortestPathTo(Vertex target) {
        pathList pList = new pathList();
        pList.pathVertex = new ArrayList<Vertex>();
        pList.pathString = new ArrayList<>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous) {
            pList.pathVertex.add(vertex);
            pList.pathString.add(vertex.name);
        }

        return pList;
    }

    // *****************************Find protected nodes********************
    public expandSet protectNodes(int k, Integer[] queryNodes, HashMap<Integer, Integer> coreness,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm, HashMap<Integer, Vertex> hmv) {

        expandSet protectSet = new expandSet();
        protectSet.expLast = new HashSet<>();
        protectSet.expRes = new HashSet<>();

        // for each query node check coreness
        for (Integer qn : queryNodes) {

            if (coreness.get(qn) == k) {

                LinkedList<Integer> queue = new LinkedList<>();
                queue.add(qn);

                while (queue.size() != 0) {

                    Integer q = queue.poll();

                    ArrayList<NodeNeighbour> neighbours = hm.get(q);
                    HashSet<Integer> c_neighbours = new HashSet<Integer>();
                    // for each node check degree
                    for (NodeNeighbour nb : neighbours) {
                        if (hmv.containsKey(nb.nodeIndex)) {
                            c_neighbours.add(nb.nodeIndex);
                        }
                    }

                    if (c_neighbours.size() == k) {

                        if (!(protectSet.expRes.contains(q))) {

                            protectSet.expRes.add(q);
                        } else {
                            protectSet.expLast.remove(q);
                        }

                        for (Integer c_nb : c_neighbours) {

                            if (!(protectSet.expRes.contains(c_nb))) {
                                protectSet.expRes.add(c_nb);
                                protectSet.expLast.add(c_nb);
                                //System.out.println("node protect:" + c_nb);
                                if (coreness.get(c_nb) == k) {
                                    queue.add(c_nb);
                                }
                            }

                        }
                    }

                }
            }
        }

        // System.out.println("protect size:"+protectSet.expRes.size());

        return protectSet;
    }

    
    // ****************************Begin Expand*****************************

    public expandSet mstExpand(HashMap<Integer, ArrayList<NodeNeighbour>> hm, HashMap<Integer, Vertex> hmv,
            List<Integer> mstList, HashSet<Integer> originalSet, int queryCore) {
                // , HashMap<String, Double> candidateExpSet
                // , double maxEW
        expandSet resSet = new expandSet();
        resSet.expLast = new HashSet<Integer>();
        resSet.expRes = new HashSet<Integer>();
        // int nbcount = queryCore;

        // do loop for each node
        for (Integer source : mstList) {

            // Create a queue for BFS
            LinkedList<Vertex> queue = new LinkedList<Vertex>();
            // Mark the current node as visited and enqueue it
            Vertex s = hmv.get(source);
            s.reset();
            // s.isSpt = Boolean.TRUE;
            queue.add(s);
            resSet.expRes.add(s.name);

            lableB: while (queue.size() != 0) {
                // Dequeue a vertex from queue and print it
                Vertex u = queue.poll();
                // Vertex u = s;
                // System.out.print(u+" ");

                // Get all adjacent vertices of the dequeued vertex s
                // If a adjacent has not been visited, then mark it
                // visited and enqueue it
                ArrayList<NodeNeighbour> neighbours = hm.get(u.name);
                int count = 0;
                if (neighbours != null) {
                    // For all adjacent vertex of the extracted vertex V
                    for (NodeNeighbour nb : neighbours) {
    
                            // if (count == nbcount && nb.weight > maxEW ) {
                            //     maxEW = nb.weight;
                            // }
                            

                            if( hmv.containsKey(nb.nodeIndex) && !(originalSet.contains(nb.nodeIndex))) {
								count++;
								if (count > queryCore) {
                                	break;
                            	}
                                
                                    Vertex v = hmv.get(nb.nodeIndex);
                                    
                                // || nb.weight <= maxEW
                                // If V is not visited
                                if (!v.isSpt) {
                                    
                                    v.level = u.level + 1;
									v.isSpt = Boolean.TRUE;
                                    queue.add(v);
                                    resSet.expRes.add(v.name);	
										

                                	if (v.level > treeLevel) {

                                    	break lableB;

                                    } else if (v.level == treeLevel) {
                                        
                                        resSet.expLast.add(v.name);
                                    }
                                    
                                    
                                }

                        }
                    }
                }

            }

        }
        for (Integer integer : resSet.expRes) {
            Vertex v = hmv.get(integer);
            v.reset();
        }

        return resSet;
    }

    public HashMap<Integer, ArrayList<NodeNeighbour>> cMap(HashSet<Integer> expRes,
            HashMap<Integer, ArrayList<NodeNeighbour>> hm) {
        HashMap<Integer, ArrayList<NodeNeighbour>> cmap = new HashMap<Integer, ArrayList<NodeNeighbour>>();

        for (Integer i : expRes) {

            
            ArrayList<NodeNeighbour> neighbours = hm.get(i);

            if (neighbours != null) {
                for (NodeNeighbour nb : neighbours) {

                    Integer neighbourIndex = nb.nodeIndex;
                    if (expRes.contains(neighbourIndex)) {
                        if (cmap.containsKey(i)) {
                            cmap.get(i).add(nb);
                        } else {
                            ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>();
                            al.add(nb);
                            cmap.put(i, al);
                        }
                    }
                }
            }
        }
        // System.out.println(VertexMax+"-vertexmax");

        return cmap;
    }

    public String[] f1score(String[] true_cmty, String resc){
        
        String[] res = new String[5];
        String[] precision = resc.split(",");
        
        Set<String> intersection = new HashSet<String>(Arrays.asList(precision));
        intersection.retainAll(Arrays.asList(true_cmty));
        Set<String> truecmtyset = new HashSet<String>(Arrays.asList(true_cmty));
        truecmtyset.removeAll(intersection);
        // System.out.println(truecmtyset.size());
        Set<String> precisionset = new HashSet<String>(Arrays.asList(precision));
        precisionset.removeAll(intersection);
        // System.out.println(precisionset.size());

        Double pr = ((double) intersection.size()) / (double) precision.length;
        Double re = ((double) intersection.size()) / (double) true_cmty.length;
        Double f1 = ((double) 2.0 * pr * re) / (double) (pr + re);
        res[0] = Double.toString(pr);
        res[1] = Double.toString(re);
        res[2] = Double.toString(f1);
        if (truecmtyset.size() > 0) {
            List<String> truecmtyList = new ArrayList<String>(truecmtyset);
            Collections.shuffle(truecmtyList);
            res[3] = truecmtyList.get(0);
        }
        if (precisionset.size() > 0) {
            List<String> precisionList = new ArrayList<String>(precisionset);
            Collections.shuffle(precisionList);
            res[4] = precisionList.get(0);
        }
        System.out.println(res[0] + "," + res[1] + "," + res[2] + "," + res[3] + "," + res[4]);
        
        return res;
    }

    public HashMap<Integer, String> readGT(String ds){
        //read in communities
        // communities
        HashMap<Integer, String> t_cmty = new HashMap<Integer, String>();
        int cmty_count = 0;
        String datapath = "../..";
        
        // begin read in communities line by line
        try {
            BufferedReader br = new BufferedReader(new FileReader(datapath + "/data/"+ds+"/"+ds+"_cmty_5000.txt"));

            try {

                String lines = br.readLine();

                while (lines != null) {

                    t_cmty.put(cmty_count, lines);

                    lines = br.readLine();
                    cmty_count++;
                }

                System.out.println("num of true cmty:" + cmty_count);

            } finally {

                br.close();

            }
        } catch (Exception e) {
            System.out.println("Reading ama_cmty_5000.txt as bufferedReader break");
        }
        return t_cmty;
    }

    public static void main(String[] args) {

		Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        String ds = myObj.next();  // Read user input
        // String ds = "amazon";
		String datapath = "../..";
        
        kCoreConnectedMinWeight run = new kCoreConnectedMinWeight();
        
        //read in communities
        HashMap<Integer, String> t_cmty = run.readGT(ds);

        HashMap<Integer, ArrayList<NodeNeighbour>> hm = new HashMap<Integer, ArrayList<NodeNeighbour>>();
        
        ArrayList<Integer> arr_index = new ArrayList<Integer>();
        HashMap<Integer, Integer> coreness = new HashMap<Integer, Integer>();
        int maxk = -1;
        int[] tags = null;
        ArrayList<HashMap<Integer, Vertex>> thmv = new ArrayList<HashMap<Integer, Vertex>>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(datapath+"/index/"+ds+"/"+ds+"_wcs_index.txt"));// dblp min vertex = 0
                                                                                      // starts from 0
            try {

                String line = br.readLine();

                String[] firLine = line.split(",");
                maxk = Integer.parseInt(firLine[0]);
                int tag = maxk;
                int count = 0;
                tags = new int[maxk + 1];
                for (int y=0; y<maxk; y++) {
                    HashMap<Integer, Vertex> hmv = new HashMap<Integer, Vertex>();
                    thmv.add(hmv); 
                }
            
                

                while (line != null) {

                    // split ,
                    String[] a = line.split(",");

                    if (Integer.parseInt(a[1])  > run.VertexMax){
                        run.VertexMax = Integer.parseInt(a[1]) ;
                    }


                    ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>();
                                
                    for (int i = 1; i < a.length/2 ; i++) {
                                
                        NodeNeighbour nb = new NodeNeighbour();
                        nb.nodeIndex = Integer.parseInt(a[i*2]);
                        nb.weight = Double.parseDouble(a[i*2 +1]);
                        al.add(nb);
                        
                    }
                    hm.put(Integer.parseInt(a[1]), al);


                    coreness.put(Integer.parseInt(a[1]), Integer.parseInt(a[0]));
                    arr_index.add(Integer.parseInt(a[1]));
                    
                    while (Integer.parseInt(a[0]) != tag) {
                        tags[tag] = count;
                        tag--;

                    }

                    Vertex v = new Vertex(Integer.parseInt(a[1]));
                    thmv.get(maxk - tag).put(Integer.parseInt(a[1]), v);
                
                    line = br.readLine();
                    count++;

                }

                while (tag != 0) {
                    tags[tag] = count;
                    tag--;

                }

                
            } finally {
                br.close();
            }
        } catch (Exception e) {
            System.out.println("Reading ama_weight.txt as bufferedReaderhappens");
            System.out.println("Maybe lack for one dimension such as the weight");
            System.out.println(e);
        }

        

        // for (int i = 3; i < 4; i++) {

            // int query_num = (int) Math.pow(2, i);
            int query_num = 8;
            ArrayList<String> arrli = new ArrayList<String>();
            ArrayList<String> arrli_out = new ArrayList<String>();

            try {

                BufferedReader br = new BufferedReader(new FileReader(datapath + "/data/"+ds+"/"+ds+"_wcs_queries.txt"));

                try {

                    String rline = br.readLine();
                    while (rline != null) {

                        arrli.add(rline);
                        rline = br.readLine();

                    }

                } finally {
                     br.close();
                }
            } catch (IOException except) {
                System.out.println("Reading query.txt as bufferedReaderhappens");
                except.printStackTrace();
            }

            double[] avg_time = new double[11];
            double[] avg_weight = new double[11];
            double[] avg_f1 = new double[11];
            double[] avg_pre = new double[11];
            double[] avg_rec = new double[11];
            double[] avg_insrectime = new double[11];
            double[] avg_delrectime = new double[11];
            double[] avg_insacc = new double[11];
            double[] avg_instarget = new double[11];
            double[] avg_delacc = new double[11];
            double[] avg_deltarget = new double[11];

            int[] countInsRec = new int[11];
            int[] countInsInter = new int[11];
            int[] countDelRec = new int[11];
            int[] countDelInter = new int[11];

            cor0:
            for (int time = 0; time < arrli.size(); time++) {

                run.treeLevel = 1;

                String[] arr = arrli.get(time).split(",");
                String cid = arr[0];
                //new to get the true cmty
                String[] true_cmty = t_cmty.get(Integer.parseInt(cid)).split("\t");

                String tcore = arr[1];
                int queryCore = Integer.parseInt(arr[2]);
          
                String res = "";
                Integer[] queryNodes = new Integer[arr.length-3];
                for (int x = 3; x < arr.length; x++) {
                    queryNodes[x-3] = Integer.parseInt(arr[x]); 
                }
                HashMap<Integer, Vertex> hmv = new HashMap<Integer, Vertex>();
                
                // new for store Vertex
                for (int x = 0; x < maxk; x++) {
                    if (thmv.get(x).size() != 0) {
                        
                        hmv.putAll(thmv.get(x));

                    }
                }
                // System.out.println("begin find short path");

                // *******************Begin Find Short Path*****************
                // hashmap to store path weight graph
                HashMap<Integer, ArrayList<NodeNeighbour>> pwg = new HashMap<Integer, ArrayList<NodeNeighbour>>();
                // hashmap to check duplicate edges
                HashMap<Integer, ArrayList<Integer>> pwgEdge = new HashMap<Integer, ArrayList<Integer>>();
                // nodelist to store path node list
                HashSet<Integer> nodeList = new HashSet<Integer>();

                List<Integer> list = new ArrayList<Integer>(Arrays.asList(queryNodes));
                shortPathList splRes = new shortPathList();
                splRes.pathNodeList = new ArrayList<Integer>();
                // time0 begin
                long time0 = System.nanoTime();
                // find shortest path if node number more than 1
                if (queryNodes.length > 1) {
                    Integer qn = queryNodes[0];
                    while (qn != null) {
                        Vertex v = hmv.get(qn);

                            list.remove(qn);
                            shortPathList spl = run.computePaths(v, list, hmv, hm, pwg, pwgEdge, queryCore); // run Dijkstra

                            while (spl == null) {
                                //System.out.println("Query Nodes are not connected");
                                if (queryCore > 1) {
                                    if(queryCore>10){
                                        queryCore = queryCore/2;
                                    } else {
                                        queryCore = queryCore - 1;
                                    }
                                    // hmv.clear();
                                    // for (int x = 0; x < maxk - queryCore + 1; x++) {
                                    //     if (thmv.get(x)!=null) {
                                    //         HashMap<Integer, Vertex> sub_hmv = new HashMap<Integer, Vertex>(thmv.get(x));
                                    //         hmv.putAll(sub_hmv);
                                    //     }
                                    // }
                                    spl = run.computePaths(v, list, hmv, hm, pwg, pwgEdge, queryCore); // run Dijkstra
                                } else {

                                
                                    String op = cid + "," + tcore + "," + "0";
                                    //System.out.println(op);
                                    arrli_out.add(op);
                                    continue cor0;
                                
                                }

                            }
                            //System.out.println("Query Nodes are connected");

                            // pwg = spl.pwg;
                            // pwgEdge = spl.pwgEdge;
                            nodeList.addAll(spl.pathNodeList);
                            qn = spl.q;
                        }
                        // splRes.pwg = pwg;
                        // splRes.pwgEdge = pwgEdge;
                        splRes.pathNodeList = new ArrayList<Integer>(nodeList);

                } else {

                    splRes.pathNodeList.add(queryNodes[0]);

                }

                // *******************Finish Find shortest path***************
                // *******************Begin Find protected nodes*************

                expandSet protectSet = run.protectNodes(queryCore, queryNodes, coreness, hm, hmv);
                //System.out.println("Finish Find protected nodes");

                // *******************Finish Find protected nodes************
                long time1 = System.nanoTime();
                // ******* ************Begin Expand**************************
                List<Integer> mst = new ArrayList<Integer>();
                expandSet resSet = new expandSet();
                // HashMap<String, Double> candidateExpSet = new HashMap<String, Double>();
                // double maxEW = -1;
                HashMap<Integer, ArrayList<NodeNeighbour>> cmap = new HashMap<Integer, ArrayList<NodeNeighbour>>();

                if (splRes.pathNodeList.size() != 1) {
                    mst = splRes.pathNodeList;
                } else {
                    mst.add(queryNodes[0]);
                }


                for (Integer protectNode : protectSet.expLast) {
                    if (!mst.contains(protectNode)) {
                        mst.add(protectNode);
                        // System.out.println("last protect:"+protectNode);
                    }
                }

                long time2 = 0L;
                ArrayList<Integer> result = null;
                // ArrayList<Integer> g0 = null;
                HashSet<Integer> cNodelist = new HashSet<Integer>();
                HashSet<Integer> originalSet = new HashSet<Integer>();
				originalSet.addAll(mst);
                int levelCount = 0;
                Integer removalN = -1;
                
                //System.out.println("Begin Expand");
                do {
                    levelCount++;
                    if (levelCount < 50) {
                        resSet = run.mstExpand(hm, hmv, mst, originalSet, queryCore);
                        // , maxEW
                        // , candidateExpSet
                        cNodelist.addAll(resSet.expRes);
                        //System.out.println("cNodelist size:"+cNodelist.size()+"mst size:"+mst.size());

                        cmap = run.cMap(cNodelist, hm);

                        time2 = System.nanoTime();

                        int resultCore = run.getMaxCore(cmap);
                        ArrayList<Integer> newvertexLargerCoreList = run.getVertexLargerCore(queryCore);
                        result = run.getSubGraph(newvertexLargerCoreList, cmap, queryNodes, queryCore, protectSet.expRes, removalN);

                        // if result = null
                        //run.treeLevel = levelCount;
                        // if (result != null) {
                        //     g0 = newvertexLargerCoreList;
                        // }


                        mst = new ArrayList<Integer>(resSet.expLast);
                        originalSet = cNodelist;
                        // candidateExpSet = new HashMap<String, Double>(resSet.canMap);
                        // maxEW = resSet.maxEdgeWeight;
                    } else {
                        int resultCore = run.getMaxCore(hm);
			    		ArrayList<Integer> vertexLargerCoreList = new ArrayList<Integer>(arr_index.subList(0, tags[queryCore]));           
                        time2 = System.nanoTime();

                        result = run.getSubGraph(vertexLargerCoreList, hm, queryNodes, queryCore, protectSet.expRes, removalN);
                        if (result == null) {
                            System.out.println("The final grap is null");
                            break;
                        }
                    }

                } while (result == null);

            
                // check protected nodes in result:
                // System.out.println("number in result:"+protectNum);

                long time3 = System.nanoTime();
                double ttime = (time3 - time0) / 1000000000.0;// /10/1000 get seconds
                double insrectime = 0.0;
                double delrectime = 0.0;
                double insacc = 0.0;
                double delacc = 0.0;
                double deltarget = 0.0;
                //System.out.println(queryCore + " : " + ttime);
                // System.out.println("connectedGraphWeightSum : " +
                for (int vertex : result) {
                    res = res  + (vertex - 1)+ ",";
                }
                // run.connectedSubGraphWeightSum);
                double finalWeight = run.finalGraphWeightSum;
                // System.out.println("finalGraphWeightSum : " + finalWeight);

                String[] f1s = run.f1score(true_cmty, res);
                
                // System.out.println("========================");
                // String op = levelCount+","+cid + "," + tcore + "," + queryCore + "," + (time1 - time0) + "," + (time2 - time1)
                // + "," + (time3 - time2) + "," + ttime + "," + finalWeight + "," + avg_time + "," + avg_weight
                //         + res;
                avg_time[0] = (avg_time[0] * time + ttime) / (time + 1);
                avg_weight[0] = (avg_weight[0] * time + finalWeight) / (time + 1);
                
                avg_pre[0] = (avg_pre[0] * time + Double.parseDouble(f1s[0])) / (time + 1);
                avg_rec[0] = (avg_rec[0] * time + Double.parseDouble(f1s[1])) / (time + 1);
                avg_f1[0] = (avg_f1[0] * time + Double.parseDouble(f1s[2])) / (time + 1);
				
                
				
                
                String newres = ttime + "," + finalWeight + "," + f1s[0] + "," +f1s[1] + ","+f1s[2] + ",";
                String newavg = avg_time[0]+ "," + avg_weight[0]+ "," +avg_pre[0]+ ","+avg_rec[0]+ "," + avg_f1[0] +",";

                int tround=1;
                HashSet<Integer> hSet = new HashSet<Integer>(result);
                ArrayList<Integer> newqueryList = new ArrayList<Integer>(Arrays.asList(queryNodes));
                Set<Integer> posNodes = new HashSet<>(newqueryList);
			    Set<Integer> negNodes = new HashSet<>();
			    String posId = null, negId = null;
			            
                    recIterLoop:
                    while (tround < 11) {
                        if (Double.parseDouble(f1s[2]) == 1.0 ) {
                            //avg_time[tround] = (avg_time[tround] * time) / (time + 1);
                            avg_weight[tround] = (avg_weight[tround] * time + finalWeight) / (time + 1);
                        
                            avg_pre[tround] = (avg_pre[tround] * time + Double.parseDouble(f1s[0])) / (time + 1);
                            avg_rec[tround] = (avg_rec[tround] * time + Double.parseDouble(f1s[1])) / (time + 1);
                            avg_f1[tround] = (avg_f1[tround] * time + Double.parseDouble(f1s[2])) / (time + 1);

                            // avg_rectime[tround-1] = (avg_rectime[tround-1] * time) / (time + 1);
                            // avg_acc[tround-1] = (avg_acc[tround-1] * time + acc) / (time + 1); 
                            newavg = newavg  +"\n"+ avg_time[tround]+ "," + avg_weight[tround]+ "," +avg_pre[tround]+ "," +avg_rec[tround]+ "," + avg_f1[tround] +","+ avg_insrectime[tround-1]+ ","+ avg_delrectime[tround-1]+ "," + avg_insacc[tround-1]+ "," + avg_delacc[tround-1]+ ","+ avg_instarget[tround-1]+ ","+ avg_deltarget[tround-1]+ ",";
                        
                            tround++;
                            continue;
                        }

                        //new for recommendations

                        Set<Integer> allResSet = new HashSet<Integer>(result);
                        Integer[] newqueryArr = new Integer[1];
                        Integer[] deleteArr = new Integer[1];
                        
					    Rec rec1 = new Rec(hm, posNodes, negNodes, newqueryList, allResSet);
                        long befinsrec = System.nanoTime();
					    List<Integer> posList = rec1.recMain(1, 2);
                        insrectime = (System.nanoTime() - befinsrec) / 1000000000.0;
					    long befdelrec = System.nanoTime();
					    List<Integer> negList = rec1.recMain(0, 2);
                        delrectime = (System.nanoTime() - befdelrec) / 1000000000.0;
                        //recommendation procedure
					   

                
                        // Output the recommended vertices
                        // recVSet.forEach(System.out::println);
                        Set<String> recPosStringSet = new HashSet<>();
                        Set<String> recNegStringSet = new HashSet<>();
                        // Compute the recommended accuracy and the accurate set
                        for (int vertex : posList) {
                            recPosStringSet.add(Integer.toString(vertex - 1));
                        }
                        for (int vertex : negList) {
                            recNegStringSet.add(Integer.toString(vertex - 1));
                        }
                        

                        // True recommended set
                        Set<String> trueCmtySet = new HashSet<>(Arrays.asList(true_cmty));
                        Set<String> resCmtySet = new HashSet<>(Arrays.asList(res.split(",")));
                        Set<String> truePosSet = new HashSet<>(trueCmtySet);
                        truePosSet.removeAll(resCmtySet);
                        Set<String> trueNegSet = new HashSet<>(resCmtySet);
                        trueNegSet.removeAll(trueCmtySet);
                        
                        int truePosSiz = Math.min(truePosSet.size(), recPosStringSet.size());
                        truePosSet.retainAll(recPosStringSet);
                            
                        int trueNegSiz = Math.min(trueNegSet.size(), recNegStringSet.size());
                        trueNegSet.retainAll(recNegStringSet);

                        //Interactive process
                        int posTag = 0;
					    int negTag = 0;
					    if (posList.isEmpty() || truePosSet.isEmpty()) {
					    	if (Double.parseDouble(f1s[1]) == 1.0) {
                                insrectime = (double)avg_insrectime[tround-1];
                                insacc = avg_insacc[tround-1];
                            } else {
                                countInsRec[tround-1] += 1;
                                insacc = 0;
                            }
					    }
					    else {
                            countInsRec[tround-1] += 1;
                            countInsInter[tround-1] +=1;
					    	insacc = ((double) truePosSet.size()) / (double) truePosSiz;
					    	System.out.println("rec pos precision:" + (insacc) + " rec pos target: 1.0");
					    	posId = truePosSet.iterator().next();
					    	posTag = 1;
					    }
					    if (negList.isEmpty() || trueNegSet.isEmpty()) {
					    	if (Double.parseDouble(f1s[0]) == 1.0) {
                                delrectime = (double)avg_delrectime[tround-1];
                                delacc = avg_delacc[tround-1];
                            } else {
                                countDelRec[tround-1] += 1;
                                delacc = 0;
                            }
					    }
					    else {
					    	delacc = ((double) trueNegSet.size()) / (double) trueNegSiz;
                            countDelRec[tround-1] += 1;
                            countDelInter[tround-1] += 1;
                            System.out.println("rec neg precision:" + (delacc) + " rec neg target: 1.0");
					    	negId = trueNegSet.iterator().next();
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
					    // parameter tune					
                        
                        long timeBefInter = System.nanoTime();

					    if (posTag == 1) {
					    	posNodes.add(Integer.parseInt(posId) +1);
                            newqueryArr[0] = Integer.parseInt(posId) +1;
					    	//Insert Interactive process
                        
                            list = new ArrayList<Integer>(newqueryList);
                            newqueryList.add(newqueryArr[0]);
                            Integer[] newqueryNodes = new Integer[newqueryList.size()];
                            newqueryNodes = newqueryList.toArray(newqueryNodes);

                            
                            //update querycore
                            int insertCore = coreness.get(newqueryArr[0]);
                            if (queryCore > insertCore) {
                                queryCore = insertCore;
                            }

                            //find the path from insert node to exist community
                            Vertex insertV = hmv.get(newqueryArr[0]);
                            shortPathList spl = run.computePaths(insertV, list, hmv, hm, pwg, pwgEdge, queryCore);
                            while (spl == null) {

					    		if (queryCore > 1) {
                                    if(queryCore>10){
                                        queryCore = queryCore/2;
                                    } else {
                                        queryCore = queryCore - 1;
                                    }

                                    spl = run.computePaths(insertV, list, hmv, hm, pwg, pwgEdge, queryCore); // run Dijkstra
                                } else {

                                    System.out.println("disconnected");
                                    break;
                                }

                            }
                            mst = new ArrayList<Integer>(spl.pathNodeList);

                            expandSet newprotectSet = run.protectNodes(queryCore, newqueryArr, coreness, hm, hmv);
                            for (Integer pn : newprotectSet.expLast) {
                                if (!mst.contains(pn)) {
                                    mst.add(pn);
                                }
                            }
                            newprotectSet.expRes.addAll(hSet);

                            do{
                                if (queryCore == 0){
                                    hSet.add(newqueryArr[0]);
                                    result= new ArrayList<Integer>(hSet);
                                    run.finalGraphWeightSum = run.getGraphWeightSum(result, hSet, hm);
                                    System.out.println("querycore is 0 but the node is still not added");
                                    break;
                                }
                                removalN = -1;
                                resSet = run.mstExpand(hm, hmv, mst, originalSet, queryCore);
                                cNodelist.addAll(resSet.expRes);
                                cmap = run.cMap(cNodelist, hm);

                                int resultCore = run.getMaxCore(cmap);
                                ArrayList<Integer> newvertexLargerCoreList = run.getVertexLargerCore(queryCore);
                                result = run.getSubGraph(newvertexLargerCoreList, cmap, newqueryNodes, queryCore, newprotectSet.expRes, removalN);

                                mst = new ArrayList<Integer>(resSet.expLast);
                                originalSet = cNodelist;
                                if (result == null) {
                                    queryCore = queryCore - 1;
                                }

                            } while (result == null);
					    } 
					    if (negTag == 1){
					    	negNodes.add(Integer.parseInt(negId) +1);
                            deleteArr[0] = Integer.parseInt(negId) +1;
                            HashSet<Integer> newhSet = new HashSet<>(hSet);
					    	while (newhSet.contains(deleteArr[0]) || result==null) {
                                if (queryCore == 0){
                                    hSet.remove(deleteArr[0]);
                                    result= new ArrayList<Integer>(hSet);
                                    run.finalGraphWeightSum = run.getGraphWeightSum(result, hSet, hm);
                                    System.out.println("querycore is 0 but the node is still not deleted");
                                    break;
                                }
                                if(result==null){
                                    newhSet = hSet;
                                }
                                protectSet = run.protectNodes(queryCore, queryNodes, coreness, hm, hmv);
                                cmap = run.cMap(newhSet, hm);
                                int resultCore = run.getMaxCore(cmap);
                                ArrayList<Integer> newvertexLargerCoreList = run.getVertexLargerCore(queryCore);
                                result = run.getSubGraph(newvertexLargerCoreList, cmap, queryNodes, queryCore, protectSet.expRes,deleteArr[0]);
                                newhSet = new HashSet<Integer>(result);
                                if(queryCore>10){
                                    queryCore = queryCore/2;
                                } else if (queryCore > 1) {
                                    queryCore = queryCore - 1;
                                } else {
                                    queryCore = 0;
                                    
                                    // break;
                                }
                            }
					    }
                        ttime = (System.nanoTime() - timeBefInter) / 1000000000.0;
                        
                        hSet = new HashSet<Integer>(result);
                        res = "";
                        for (int vertex : result) {
                            res = res  + (vertex - 1) + ",";
                        }
                        finalWeight = run.finalGraphWeightSum;
                        f1s = run.f1score(true_cmty, res);
                        newres = newres + ttime + "," + finalWeight + "," + f1s[0] + "," +f1s[1] + ","+f1s[2] + ",";
                        
                    
                    
                        avg_time[tround] = (avg_time[tround] * time + ttime) / (time + 1);

                        avg_weight[tround] = (avg_weight[tround] * time + finalWeight) / (time + 1);
                        avg_pre[tround] = (avg_pre[tround] * time + Double.parseDouble(f1s[0])) / (time + 1);
                        avg_rec[tround] = (avg_rec[tround] * time + Double.parseDouble(f1s[1])) / (time + 1);
                        avg_f1[tround] = (avg_f1[tround] * time + Double.parseDouble(f1s[2])) / (time + 1);

                        // avg_insrectime[tround-1] = (avg_insrectime[tround-1] * time + insrectime) / (time + 1);
                        // avg_insacc[tround-1] = (avg_insacc[tround-1] * time + insacc) / (time + 1); 
                        // avg_instarget[tround-1] = (avg_instarget[tround-1] * time + instarget) / (time + 1); 
                        // avg_delrectime[tround-1] = (avg_delrectime[tround-1] * time + delrectime) / (time + 1);
                        // avg_delacc[tround-1] = (avg_delacc[tround-1] * time + delacc) / (time + 1); 
                        // avg_deltarget[tround-1] = (avg_deltarget[tround-1] * time + deltarget) / (time + 1); 
                        if (countInsRec[tround-1]!=0) {
                            avg_insrectime[tround-1] = (avg_insrectime[tround-1] * (countInsRec[tround-1]-1) + insrectime) / (double)(countInsRec[tround-1]);
                            avg_insacc[tround-1] = (avg_insacc[tround-1] * (countInsRec[tround-1]-1) + insacc) / (double)(countInsRec[tround-1]); 
                            avg_instarget[tround-1] = (double)countInsInter[tround-1]/(double)countInsRec[tround-1]; 
                        }
                        if (countDelRec[tround-1]!=0) {
                            avg_delrectime[tround-1] = (avg_delrectime[tround-1] * (countDelRec[tround-1]-1) + delrectime) / (double)(countDelRec[tround-1]);
                            avg_delacc[tround-1] = (avg_delacc[tround-1] * (countDelRec[tround-1]-1) + delacc) / (double)(countDelRec[tround-1]); 
                            avg_deltarget[tround-1] = (double)countDelInter[tround-1]/(double)countDelRec[tround-1]; 
                        }
                        newavg = newavg +"\n"+ avg_time[tround]+ "," + avg_weight[tround]+ "," +avg_pre[tround]+ "," +avg_rec[tround]+ "," + avg_f1[tround] +","+ avg_insrectime[tround-1]+ ","+ avg_delrectime[tround-1]+ "," + avg_insacc[tround-1]+ "," + avg_delacc[tround-1]+ ","+ avg_instarget[tround-1]+ ","+ avg_deltarget[tround-1]+ ",";
                        
                        tround++;
                    }

                System.out.println(newavg);

                arrli_out.add(newres);
                if (time == arrli.size()-1) {
                    arrli_out.add("Final average results of 10 rounds:\n");
                    arrli_out.add(newavg);
                }
                // reset hashmap vertex
                
                for (Integer r : cNodelist) {
                    Vertex v = hmv.get(r);
                    v.reset();
                }
                // hmv.clear();
            }

            // write down answers
            try {

                // BufferedWriter bw = new BufferedWriter(new FileWriter("../../data/"+ds+"/"+ds+"_res_" + query_num + "_r100.txt"));
                BufferedWriter bw = new BufferedWriter(new FileWriter(datapath+"/data/"+ds+"/"+ds+"_wcs_res_gics.txt"));

                try {

                    for (int index = 0; index < arrli_out.size(); index++) {
                        bw.write(arrli_out.get(index));
                        bw.newLine();
                    }

                } finally {

                    bw.close();

                }
            } catch (Exception e) {
                System.out.println("Writing **.txt as bufferedReader break");
            }
        // }
    }
}
