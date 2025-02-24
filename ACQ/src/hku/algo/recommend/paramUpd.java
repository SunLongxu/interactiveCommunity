package hku.algo.recommend;

import hku.Config;
import java.util.*;

public class paramUpd {
    private int graph[][];
    private String nodes[][];
    private int core[];

    public paramUpd(int graph[][], String nodes[][], int core[]){
        this.graph = graph;
        this.nodes = nodes;
        this.core = core;
    }

    public int nbBasePosParam(int queryId, int posId, int kChangeTag, int kwChangeTag) {
        int kOri = Config.k;
        int kNow = Math.min(core[queryId], core[posId]);
        Set<String> attrQ = new HashSet<String>(Arrays.asList(Arrays.copyOfRange(nodes[queryId], 1, nodes[queryId].length)));
        Set<String> attrPos = new HashSet<String>(Arrays.asList(Arrays.copyOfRange(nodes[posId], 1, nodes[posId].length)));
        attrPos.retainAll(attrQ);
        ArrayList<String> attrList = new ArrayList<>(attrPos);
        int kwLengthMax = attrList.size();
        System.out.println("Number of common attributes:" + attrList.size());
        String attr = "";
        int tmp = kNow;
        int kMax = 1;
        int[][] countAttr = new int[kNow][attrList.size()];
        for (int j = 0; j < attrList.size(); j++) {
            attr = attrList.get(j);
            kMax = 1;
            for (int nb : graph[posId]) {
                for(int i = 1;i < nodes[nb].length;i ++){
                    if(attr.equals(nodes[nb][i])){
                        if(core[nb] < kNow){
                            tmp = core[nb];
                        }
                        for (int x = 0; x < tmp; x++){
                            countAttr[x][j]+=1;
                        }
                        tmp = kNow;
                        
                    }
                }
            }
        }
        for (int j = 0; j < attrList.size(); j++) {
            for (int x = kNow; x > 0; x--){
                int kFreq = Math.min(x, countAttr[x-1][j]);
                if (kFreq > kMax) {
                    kMax = kFreq;
                }
            }
        }

        System.out.println("kMax: "+ kMax);
        kNow = Math.min(kNow, kMax);
        int kwLengthNow = Config.kwLength;
        kwLengthMax = kwLengthMax + 1;
        if(kNow < kOri){
            System.out.println("k changes to:" + kNow);
            Config.k = kNow;
            Config.kwLength = -1;
        } else if (kChangeTag == 1 && kOri > 1) {
            Config.k = kOri - 1;
            System.out.println("k decrease 1 to" + Config.k);
            Config.kwLength = -1;
        } else if (kwLengthNow > kwLengthMax){
            if (kwLengthMax > 1) {
                Config.kwLength = kwLengthMax;
                System.out.println("kwlength changes to:" + Config.kwLength);
            } else {
                Config.k = kOri - 1;
                System.out.println("k decrease 1 to" + Config.k);
                Config.kwLength = -1;
            }
            
        } else if(kwChangeTag == 1){
            if (kwLengthNow > 2) {
                Config.kwLength = kwLengthNow-1;
            } else {
                Config.k = kOri - 1;
                System.out.println("k decrease 1 to" + Config.k);
                Config.kwLength = -1;
            }
        }
        return 1;
    }
    public int nbBaseNegParam(int queryId, int negId){

        return 1;
    }
}