package com.elighthouse.ndtnet2plan.services;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UtilsService {

	public double[][] convertListToDoubleArray(List<List<Integer>> list) {
	    if (list == null || list.isEmpty()) return new double[0][0];

	    int outerSize = list.size();
	    double[][] result = new double[outerSize][];

	    for (int i = 0; i < outerSize; i++) {
	        List<Integer> innerList = list.get(i);
	        int innerSize = innerList.size();
	        result[i] = new double[innerSize];
	        for (int j = 0; j < innerSize; j++) {
	            result[i][j] = innerList.get(j); // Autoboxing handles conversion from Integer to double
	        }
	    }

	    return result;
	}
}
