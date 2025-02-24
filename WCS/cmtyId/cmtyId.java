package cmtyId;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class cmtyId {

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

        HashMap<String, Integer> vertexSet = new HashMap<String, Integer>();
        HashSet<String> cmtySet = new HashSet<String>();
        ArrayList<ArrayList<String>> cmtyList = new ArrayList<ArrayList<String>>();
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object

        String ds = myObj.nextLine();  // Read user input
        // begin read in communities line by line
        String FilePath = "../data/"+ds+"/"+ds+"_cmty_5000.txt";
        ArrayList<String> lines = new ArrayList<String>(readFile(FilePath));

        System.out.println("read file done;");

        int lines_siz = lines.size();
        String line = "";
        String[] nodes = {};
        int len = 0;
        int maxlength = 0;
        for(int j = 0; j < lines_siz; j++){

            line = lines.get(j);
            nodes = line.split("\\s+");
            len = nodes.length;
            if (len > maxlength) {
                maxlength = len;
            }
            if (!cmtySet.contains(line)) {
                cmtySet.add(line);

                for(int i = 0; i<len; i++){
                    String v = nodes[i];

                    if(vertexSet.containsKey(v)){
                        Integer times = vertexSet.get(v) + 1;
                        vertexSet.replace(v, times);
                    } else {
                        vertexSet.put(v, 1);
                    }

                }

            }
            
            
            int qnum = len;
            ArrayList<String> cmty = new ArrayList<String>(Arrays.asList(nodes));
            cmty.add(Integer.toString(qnum));
            cmtyList.add(cmty);
            
        }

        ArrayList<String> arrli = new ArrayList<String>();
        // generate id community set
        for (int i = 0; i < cmtyList.size(); i++) {

            ArrayList<String> cmty = cmtyList.get(i);
            int cmtylen = cmty.size() - 1;
            String idv = cmty.get(cmtylen);
            for(int j = 0; j < cmtylen ; j++){
                String v = cmty.get(j);
                Integer times = vertexSet.get(v);
                if (times == 1) {
                    idv = idv  + "\t" + v;
                }
            }
            arrli.add(idv);

        }
        // write down community id
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter("../data/"+ds+"/"+ds+"_cmty_id.txt"));
            
            try {
                for (int index = 0; index < arrli.size(); index++) {
                    bw.write(arrli.get(index));
                    bw.newLine();

                }

            } finally {

                bw.close();

            }
        } catch (Exception e) {
            System.out.println("Writing **.txt as bufferedReader break");
        }
    }
}
