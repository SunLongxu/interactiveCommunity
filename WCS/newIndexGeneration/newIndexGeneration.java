package newIndexGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

class NodeNeighbour {
    int nodeIndex;
    double weight;
}


public class newIndexGeneration {

    private int maxDegree = -1;
    private int VertexMax = -1;
    private int[] BinInitialPos = null;
    private int[] degreeTable = null;//1,...,VertexMax
    private ArrayList<String> coreIndex;
    
    
    //degreeTable: index is vertex number, value is degree
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
    
    
    int getMaxCore(HashMap<Integer, ArrayList<NodeNeighbour>> hm){

        coreIndex = new ArrayList<String>();

    
        int[] degreeTable = degreeTable(hm);
        
        ArrayList<int[]> result = binSort(degreeTable);
        int[] vertTable = result.get(0);
        int[] posTable =  result.get(1);

        int maxCore = -1;
        
        for(int start = 1; start < vertTable.length; start++){
            
            StringBuilder sb = new StringBuilder();
            sb.append(degreeTable[vertTable[start]]).append(",").append(vertTable[start]);
            coreIndex.add(sb.toString());
            
            if(degreeTable[vertTable[start]] > maxCore)
                maxCore = degreeTable[vertTable[start]];
            
            ArrayList<NodeNeighbour> neighbors = new ArrayList<NodeNeighbour>();
            neighbors = hm.get(vertTable[start]);
            
            if(neighbors!=null){
                for(NodeNeighbour nodeNeighbor : neighbors){
                    int neighborInt = nodeNeighbor.nodeIndex;
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

    public static ArrayList<String> readFile(String FilePath){

        ArrayList<String> lines = new ArrayList<String>();
        int count = 0;

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(FilePath), "UTF-8");

            BufferedReader br = new BufferedReader(isr);
            
            try {

                String line = br.readLine();
                
                while (line != null) {

                    count++;
                    
                    lines.add(line);

                    if (count % 10000000 == 0) {
                        System.out.println(count);
                        
                    }

                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException ex)  
        {
            System.out.println("FileNotFoundException");
            // insert code to run when exception occurs
        } catch (Exception e) {
            System.out.println("Reading **.txt as bufferedReaderhappens");
            System.out.println(e);
        }
        System.out.println("Number of Edges:"+ count);

        return lines;
    }

        int partition(ArrayList<NodeNeighbour> al, int low, int high) {
            double pivot = al.get(high).weight;
            int i = (low - 1); // index of smaller element
            for (int j = low; j < high; j++) {
                // If current element is smaller than the pivot
                if (al.get(j).weight < pivot) {
                    i++;

                    // swap arr[i] and arr[j]
                    Collections.swap(al, i, j); 
                    
                }
            }

            // swap arr[i+1] and arr[high] (or pivot)
            Collections.swap(al, i + 1, high); 
            
            return i + 1;
        }

        /*
         * The main function that implements QuickSort() arr[] --> Array to be sorted,
         * low --> Starting index, high --> Ending index
         */
        void sort(ArrayList<NodeNeighbour> al, int low, int high) {
            if (low < high) {
                /*
                 * pi is partitioning index, arr[pi] is now at right place
                 */
                int pi = partition(al, low, high);

                // Recursively sort elements before
                // partition and after partition
                sort(al, low, pi - 1);
                sort(al, pi + 1, high);
            }
        }

        /* A utility function to print array of size n */
        static void printArrli(ArrayList<NodeNeighbour> al) {
            int n = al.size();
            for (int i = 0; i < n; ++i){
                //System.out.print(al.get(i).nodeIndex+","+al.get(i).weight + " ");
            }

        }

        static ArrayList<NodeNeighbour> sortArrli (ArrayList<NodeNeighbour> al) {
            
            int n = al.size();

            newIndexGeneration ob = new newIndexGeneration();
            ob.sort(al, 0, n - 1);

            return al;
        }

    public static void main(String[] args) {

        
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object

        String ds = myObj.nextLine();  // Read user input
        
        newIndexGeneration run = new newIndexGeneration();
        HashMap<Integer, ArrayList<NodeNeighbour>> hm = new HashMap<Integer, ArrayList<NodeNeighbour>>();
        String FilePath = "../data/"+ds+"/"+ds+"_wcs_weight.txt";
        System.out.println("File Path is: " + FilePath);  // Output user input
        
        ArrayList<String> lines = new ArrayList<String>(readFile(FilePath));
        System.out.println("read file done;");
        
        int lines_siz = lines.size();
        String[] a = new String[3];
        Integer[] b = new Integer[2];
        for(int j = 0; j < lines_siz; j++){
                    
            a = lines.get(j).split(",");
            b[0] = Integer.parseInt(a[0])+1;
            b[1] = Integer.parseInt(a[1])+1;

            for(int i = 0; i < 2; i++){
                if( Integer.parseInt(a[i])+1 > run.VertexMax){
                    run.VertexMax = Integer.parseInt(a[i])+1;}

                if(hm.containsKey(b[i])){
                    NodeNeighbour nb = new NodeNeighbour();
                    nb.nodeIndex = b[1-i];
                    nb.weight = Double.parseDouble(a[2]);

                    hm.get(b[i]).add(nb);
                    nb = null;

                }else{
                    ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>();
                    NodeNeighbour nb = new NodeNeighbour();
                    nb.nodeIndex = b[1-i];
                    nb.weight = Double.parseDouble(a[2]);
				    al.add(nb);
                    hm.put(b[i], al);
                    nb = null;
                    al = null;
                }
			        
            }
            

            
            if(j % 10000000 == 0){
                System.runFinalization();
            }

        }

        System.out.println("Finish construct the edges hashmap");
        System.out.println("Number of Vertices: " + hm.size());
        System.out.println("Max Vertex ID: " + run.VertexMax);
        // we could get the edges from the input txt file which is super easier
        System.out.println("========================");

        double time1 = System.currentTimeMillis();
        
        int resultCore = run.getMaxCore(hm);
        System.out.println("Max Coreness: " + resultCore);

        Iterator hmIterator = hm.entrySet().iterator();
        while (hmIterator.hasNext()) { 
            Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
            sortArrli((ArrayList<NodeNeighbour>)mapElement.getValue());
            
        }
        
        double time2 = System.currentTimeMillis() - time1;
        System.out.println("Running Time:" + time2);
        
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter("../index/"+ds+"/"+ds+"_wcs_index.txt"));
           
            try {

                for (int index = run.VertexMax - 1; index >= run.VertexMax - hm.size(); index--) {

                    String res = run.coreIndex.get(index);
                    String[] corIdx = run.coreIndex.get(index).split(",");
                    ArrayList<NodeNeighbour> al = new ArrayList<NodeNeighbour>(hm.get(Integer.parseInt(corIdx[1])));
                    for (int i = 0; i < al.size(); ++i){
                        res = res + "," + al.get(i).nodeIndex + "," + al.get(i).weight;
                    }
                    bw.write(res);
                    bw.newLine();

                }

            } finally {

                bw.close();

            }
        } catch (Exception e) {
            System.out.println("writing **.txt as bufferedReader break");
        }

        System.out.println("WC index file path:"+ "../index/"+ds+"/"+ds+"_wcs_index.txt");
        System.out.println("========================");
    
    }
}
