package de.baensch.airsniffer.util;

import android.util.Log;

import java.util.Arrays;

public class LimitedQueue {

    private String TAG = getClass().getSimpleName();

    private final int limit;
    int[] array;
    int index = 0;

    public LimitedQueue(int limit) {
        this.limit = limit;
        this.array = new int[limit];
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.MAX_VALUE;
        }
    }


    public void add (int i){
        array[index] = i;
        index++;
        if (index >= limit) index = 0;
        //Log.d(TAG, "Added a integer, value was: " + i + ". "+toString());
    }


    public int mean(){
        int sum = 0;
        int num = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == Integer.MAX_VALUE){
                //ignore
            }
            else{
                sum += array[i];
                num++;
            }
        }
        Log.d(TAG, "MEAN - Num was: "+num+" & Sum was "+sum);
        if (num == 0) return 0;
        return sum/num;
    }

    @Override
    public String toString() {
        return "LimitedQueue: " + Arrays.toString(array);
    }
}