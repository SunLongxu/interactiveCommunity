package hku.exp.util;

import java.io.*;
import java.util.Random;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static java.lang.Math.sqrt;

public class transFormat {


    public static String buildStr(StringBuilder sb, Integer vertex, HashSet<Integer> attrSet){
        
    
        sb.append(vertex);
        for (Integer attr: attrSet) {
            sb.append(" ").append(attr);
        }
        String res = sb.toString();

        sb.setLength(0);
        return res;
        
    }

    public static HashSet<Integer> randAttribute(int numAttr, ArrayList<Integer> list) {
        
        Collections.shuffle(list);
        HashSet<Integer> attrSet = new HashSet<Integer>();
        for (int i=0; i<numAttr; i++) attrSet.add(list.get(i));
        return attrSet;
    }
    public static ArrayList<Integer> randCmtyVertex(ArrayList<Integer> list) {
        
        int attrCmtyVNum = (int)Math.ceil(0.8 * list.size());
        Collections.shuffle(list);
        ArrayList<Integer> attrList = new ArrayList<Integer>();
        for (int i=0; i<attrCmtyVNum; i++) attrList.add(list.get(i));
        return attrList;
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

                    if (count % 1000000 == 0) {
                        System.out.println(count);
                        System.runFinalization();
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
        System.out.println(count);

        return lines;
    }


    public static void main(String[] args) {

        // String[] dataset = {"0", "107", "348", "414", "686", "698", "1684", "1912", "3437", "3980"};
        // String[] dataset = {"ama", "ytb", "dblp", "lj"};
        String[] dataset = {"lj"};
        for (String ds : dataset) {
            
        // String edgePath = "../data/facebook/"+ds+".edges";
        // String featPath = "../data/facebook/"+ds+".feat";
        // String cmtyPath = "../data/facebook/"+ds+".circles";
        String edgePath = "../data/"+ds+"/"+ds+"_graph.txt";
        String featPath = "../data/"+ds+"/"+ds+"_keywords.txt";
        String cmtyPath = "../data/"+ds+"/"+ds+"_cmty_5000.txt";

        StringBuilder sb = new StringBuilder();

        ArrayList<String> edgeLines = new ArrayList<String>(readFile(edgePath));

        System.out.println("read edges done;" + ds);

        ArrayList<String> vertexMapList = new ArrayList<>();
        HashMap<Integer, Integer> vertexMap = new HashMap<Integer, Integer>();
        HashMap<Integer, ArrayList<Integer>> vertexAttrMap = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<String> newfeatList = new ArrayList<String>();

        // for(int j = 0; j < featLines.size(); j++){
                    
        //     String[] a = featLines.get(j).split("\\s+");
        //     vertexMap.put(a[0], j+1);
        //     sb.append(a[0]).append(" ").append(j+1);
        //     vertexMapList.add(sb.toString());
        //     sb.setLength(0);
        // }

        HashMap<Integer, HashSet<Integer>> hm = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<String> edgeList = new ArrayList<String>();
        ArrayList<Integer> vertexSet = new ArrayList<>();

        int vertexMax = 0;
        String[] a = {};
        Integer[] b = {};
        for(int j = 0; j < edgeLines.size(); j++){
                    
            a = edgeLines.get(j).split("\\s+");
            b = new Integer[2];
            b[0] = Integer.parseInt(a[0]);
            b[1] = Integer.parseInt(a[1]);

            for(int i = 0; i < 2; i++){
                if( Integer.parseInt(a[i]) > vertexMax){
                    vertexMax = Integer.parseInt(a[i]);}

                if(hm.containsKey(b[i])){
                    hm.get(b[i]).add(b[1-i]);

                }else{
                    vertexSet.add(b[i]);
                    HashSet<Integer> nb = new HashSet<Integer>();
                    nb.add(b[1-i]);
                    hm.put(b[i], nb);
                    nb = null;
                }
			        
            }
            a = null;
            b = null;
            if(j % 10000000 == 0){
                System.runFinalization();
            }
            
        }
        int vertexSetSiz = vertexSet.size();
        System.out.println("vertex max:"+ vertexMax);
        edgeLines = null;

        for (int j = 0; j < vertexSetSiz; j++) {
            vertexMap.put(vertexSet.get(j), j+1);
        }
        for (int j = 0; j < vertexSetSiz; j++) {
            sb.append(vertexSet.get(j)).append(" ").append(j+1).append("\n");
            vertexMapList.add(sb.toString());
            sb.setLength(0);
            if(j % 10000000 == 0){
                System.runFinalization();
            }
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("../data/facebook/"+ds+"-mapper.txt");

            try {
                byte[] intToBytes;
                for (int i = 0; i<vertexMapList.size(); i++) {
                    intToBytes = vertexMapList.get(i).getBytes();
                    outputStream.write(intToBytes);
                    
                }

            } finally {
                outputStream.flush();
                outputStream.close();

            }
        } catch (Exception e) {
            System.out.println("write mapper as bufferedReader break");
        }

        vertexMapList = null;
        Integer vertex;
        for (int j = 0; j < vertexSetSiz; j++) {
        
            vertex = vertexSet.get(j);
            sb.append(vertexMap.get(vertex));
            for(Integer nb : hm.get(vertex)){
                // if (vertexMap.get(vertex) < vertexMap.get(nb)) {
                    sb.append(" ").append(vertexMap.get(nb));
                    
                // }
                
            }
            edgeList.add(sb.append("\n").toString());
            sb.setLength(0);
            vertex = null;
            if(j % 10000000 == 0){
                System.runFinalization();
            }
        }
        hm = null;
        vertexSet = null;

        try {

            outputStream = new FileOutputStream("../data/facebook/"+ds+"-graph.txt");

            try {
                byte[] stringToBytes;
                for (int i = 0; i<edgeList.size(); i++) {
                    stringToBytes = edgeList.get(i).getBytes();
                    outputStream.write(stringToBytes);
                    
                }

            } finally {
                outputStream.flush();
                outputStream.close();

            }
        } catch (Exception e) {
            System.out.println("write graph as bufferedReader break");
        }

        edgeList = null;

        ArrayList<String> featLines = new ArrayList<String>(readFile(featPath));

        System.out.println("read feature done;");

        for(int j = 0; j < featLines.size(); j++){
                    
            a = featLines.get(j).split("\\s+");
            
            if (vertexMap.containsKey(Integer.parseInt(a[0]))) {
                ArrayList<Integer> attrSet = new ArrayList<Integer>();
                // sb.append(vertexMap.get(Integer.parseInt(a[0])));

                for(int i = 1; i < a.length; i++){
                    // this is for facebook feat file which uses matrix keyword
                    // if(Integer.parseInt(a[i])==1){
                    //     attrSet.add(i);
                    // }
                    // this is for datasets use list keyword set
                    attrSet.add(Integer.parseInt(a[i]));
                }

                // newfeatList.add(sb.toString());
                // sb.setLength(0);
                vertexAttrMap.put(vertexMap.get(Integer.parseInt(a[0])), attrSet);
                attrSet = null;
            }
            a = null;
            if(j % 10000000 == 0){
                System.runFinalization();
            }
            
        }
        featLines = null;

        for (int j = 0; j < vertexSetSiz; j++) {
        
            sb.append(j+1).append("\t").append(j+1).append("\t");
            ArrayList<Integer> attrList = vertexAttrMap.get(j+1);
            for(int i = 0; i < attrList.size(); i++ ){
                sb.append(attrList.get(i)).append(" ");
            }
            newfeatList.add(sb.append("\n").toString());
            sb.setLength(0);
            attrList = null;
            if(j % 10000000 == 0){
                System.runFinalization();
            }
        }
        vertexAttrMap = null;
        System.runFinalization();
        try {

            outputStream = new FileOutputStream("../data/facebook/"+ds+"-node.txt");

            try {

                byte[] stringToBytes;               
                for (int i = 0; i<newfeatList.size(); i++) {
                    stringToBytes = newfeatList.get(i).getBytes();
                    outputStream.write(stringToBytes);
                }

            } finally {
                outputStream.flush();
                outputStream.close();

            }
        } catch (Exception e) {
            e.getStackTrace();
            System.out.println(e+"write node as bufferedReader break");
        }
        newfeatList = null;
        
        ArrayList<String> cmtyLines = new ArrayList<String>(readFile(cmtyPath));

        System.out.println("read community done;");
        int cnt = 0;
        String line;
        ArrayList<String> cmtyNewLines = new ArrayList<String>();
        for(int j = 0; j < cmtyLines.size(); j++){
            line = cmtyLines.get(j);
            a = line.split("\\s+");
            for(int i = 0; i < a.length; i++){
            // start from i=1 for facebook datasets only
            // for(int i = 1; i < a.length; i++){
                if (vertexMap.containsKey(Integer.parseInt(a[i]))) {
                    sb.append(vertexMap.get(Integer.parseInt(a[i]))).append(" ");
                    cnt += 1;
                }
            }
            if (cnt > 2) {
                cmtyNewLines.add(sb.append("\n").toString());
            }
            sb.setLength(0);
            line = null;
            a = null;
            cnt = 0;
            
        }
        vertexMap = null;
        cmtyLines = null;
        
        try {

            outputStream = new FileOutputStream("../data/facebook/"+ds+"-ground.txt");

            try {
                byte[] stringToBytes;
                for (int i = 0; i<cmtyNewLines.size(); i++) {
                    stringToBytes = cmtyNewLines.get(i).getBytes();
                    outputStream.write(stringToBytes);
                    
                }

            } finally {
                outputStream.flush();
                outputStream.close();

            }
        } catch (Exception e) {
            e.getStackTrace();
            System.out.println(e+"write ground as bufferedReader break");
        }
    }
    }
}
