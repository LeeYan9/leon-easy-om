package com.lyon.easy.common.utils;

import cn.hutool.core.util.RandomUtil;

import java.util.Arrays;

/**
 * @author Lyon
 */
public class ArrayUtil {

    public static void main(String[] args) {
        final int[] arr = randomArr(20, 80);
        final int[] arr1 = bubbleSort(arr);

        final int[] standardArr = Arrays.copyOf(arr, arr.length);
        Arrays.sort(standardArr);
        System.out.println(Arrays.toString(standardArr));
        System.out.println(Arrays.toString(arr1));
        System.out.println("isEquals:"+isEquals(arr1,standardArr));

    }

    public static int[] selectSort(int[] originArr) {
        final int[] arr = Arrays.copyOf(originArr, originArr.length);
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] > arr[j]) {
                    swap(arr, i, j);
                }
            }
        }
        return arr;
    }

    private static void swap(int[] arr, int i, int j) {
        arr[i] = arr[i] ^ arr[j];
        arr[j] = arr[i] ^ arr[j];
        arr[i] = arr[i] ^ arr[j];
    }

    ;

    public static int[] bubbleSort(int[] arr) {

        for (int i = 0; i < arr.length -1 ; i++) {
            for (int j = 0; j < arr.length -1; j++) {
                if (arr[j] > arr[j+1]){
                    swap(arr,j,j+1);
                }
            }
        }

        return arr;
    }

    public static int[] shellSort(int[] arr) {
        return arr;
    }

    public static int binarySearch(int[] arr, int x) {



        return -1;
    }

    public  static int[] randomArr(int arrMax, int eleMax) {
        final int[] arr = new int[RandomUtil.randomInt(0, arrMax)];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = RandomUtil.randomInt(0, eleMax);
        }
        return arr;
    }

    public static boolean isEquals(int[] arr1, int[] arr2){
        return Arrays.equals(arr1,arr2);
    }

}
