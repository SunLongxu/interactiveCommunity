package hku.exp.util;
import hku.Config;
import java.io.*;
import java.util.*;
import java.util.Scanner;


public class GroundtruthReader {

    private static HashSet<Integer> convert(String[] array)
    {
        // Hash Set Initialisation
        HashSet<Integer> cmtySet = new HashSet<>();
                    
        // Iteration using enhanced for loop
        for (String element : array) {
            cmtySet.add(Integer.parseInt(element));
        }
        // returning the set
        return cmtySet;
    }
    private ArrayList<String> readCore(String fileName){

            ArrayList<String> coreList = new ArrayList<>();

            // begin read into hashmap line by line
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));

                try {

                    String lines = br.readLine();

                    while (lines != null) {

                        coreList.add(lines);
                        lines = br.readLine();
                    }


                } finally {

                    br.close();

                }
            } catch (Exception e) {
                System.out.println("Reading **.txt as bufferedReader break");
            }
            return coreList;
    }


    public ArrayList<HashSet<Integer>> readGT(String fileName){
        //read in communities
        ArrayList<HashSet<Integer>> gtCmtyList = new ArrayList<>();
        // begin read in communities line by line
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            try {

                String lines = br.readLine();
                

                while (lines != null) {
                    
                    String[] array = lines.split("\\s+");
                    HashSet<Integer> cmtySet = convert(array);
                    gtCmtyList.add(cmtySet);
                    lines = br.readLine();
                    
                }

            } finally {

                br.close();

            }
        } catch (Exception e) {
            System.out.println("Reading ama_cmty_5000.txt as bufferedReader break");
        }
        return gtCmtyList;
    }


    private ArrayList<String> comQuery(ArrayList<String> coreList1, ArrayList<HashSet<Integer>> gtCmtyList1){
        ArrayList<String> queryList = new ArrayList<>();
        HashMap<Integer, String> vCmtyMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        Random ran = new Random();
        for (int i = 0; i < gtCmtyList1.size(); i++) {
            HashSet<Integer> cmtyVSet = gtCmtyList1.get(i);
            for (Integer cmtyV : cmtyVSet) {
                if (vCmtyMap.containsKey(cmtyV)) {

                    String str = vCmtyMap.get(cmtyV);
                    sb.append(str).append(" ").append(i+1);
                    vCmtyMap.put(cmtyV, sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(" ").append(i+1);
                    vCmtyMap.put(cmtyV, sb.toString());
                    sb.setLength(0);
                }
            }
        }
        for (int i = 0; i < coreList1.size(); i++) {
            String[] line = coreList1.get(i).split(",");
            if (vCmtyMap.containsKey(Integer.parseInt(line[1])) && Integer.parseInt(line[0]) > 1) {
                int randK = (ran.nextInt(Integer.parseInt(line[0]))) + 1;
                sb.append((line[0])).append(" ").append(randK).append(" ").
                    append(line[1]).append(vCmtyMap.get(Integer.parseInt(line[1])));
                queryList.add(sb.toString());
                sb.setLength(0);
            }
        }
        
        return queryList;
    }
    private void printQuery(ArrayList<String> queryList1, String queryFile){
        Collections.shuffle(queryList1);
        int querySiz = 100;
        if (queryList1.size() < querySiz) {
            querySiz = queryList1.size();
        }
        
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(queryFile));

            try {

                for (int i = 0; i<querySiz; i++) {

                    bw.write(queryList1.get(i));
                    bw.newLine();
                    
                }

            } finally {

                bw.close();

            }
        } catch (Exception e) {
            System.out.println("write lj_weight.txt as bufferedReader break");
        }
    }
    public static void main(String[] args) {
        GroundtruthReader gtReader = new GroundtruthReader();
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object

        String ds = myObj.nextLine();  // Read user input
        String coreFile = "../../index/" + ds + "/" + ds + "_wcs_index.txt";
        String gtFile = "../../data/" + ds + "/" + ds + "-ground.txt";
        String queryFile = "../../data/" + ds + "/" + ds + "-query.txt";
        ArrayList<String> coreList1 = gtReader.readCore(coreFile);
        ArrayList<HashSet<Integer>> gtCmtyList1 = gtReader.readGT(gtFile);
        ArrayList<String> queryList1 = gtReader.comQuery(coreList1, gtCmtyList1);
        gtReader.printQuery(queryList1, queryFile);
        
    }
}