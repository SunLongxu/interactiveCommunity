package hku.exp.util;
import java.io.*;
import java.util.*;
/**
 * @author cslxsun
 * @date Aug 8, 2022
 * compute f1 score
 */
public class F1ScoreCalculator {
    private double[] res = new double[3];

    public double[] f1score(HashSet<Integer> trueCmty, Set<Integer> resc){
        
        Set<Integer> intersection = new HashSet<>(resc);
        intersection.retainAll(trueCmty);
    
        res[0] = (double) intersection.size() / resc.size();
        res[1] = (double) intersection.size() /  trueCmty.size();
        res[2] = ((double) 2.0 * res[0] * res[1]) / (res[0] + res[1]);
        
        return res;
    }


}
