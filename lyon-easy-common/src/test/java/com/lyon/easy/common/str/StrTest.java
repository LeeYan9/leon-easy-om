package com.lyon.easy.common.str;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author Lyon
 */
public class StrTest {

    static int num = 10;

    static {
        num = 20;
    }

    public static void main(String[] args) {
        String prefix = "user-info-";
        String regex = "(r[-])?(w[-])?";
//        String regex = "[(r[-]){0,1}|(w[-]){0,1}]";
        System.out.println(ReUtil.replaceAll("user-info-r-20102020",prefix+regex,""));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-r-20102020"));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-w-20102020"));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-r20102020"));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-20102020"));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-20102"));
        System.out.println(ReUtil.delFirst(prefix+regex,"user-info-ass"));

    }
//
//    private static void test0() {
//        final String str1 = "JAVA";
//        String str0 = "JAVA";
//        System.out.println(str0 == str1);
//    }
//
//    private static void test1() {
//        String str0 = "JAVA";
//        String str1 = "JAVA";
//        System.out.println(str0 == str1);
//    }
//
//    private static void test2() {
//        String str0 = "JAVA";
//        String str1 = "JA";
//        String str2 = "VA";
//        String str3 = str1+str2;
//        System.out.println(str0 == str3);
//    }
//
//    private static void test3(){
//        String str2 = new String("str")+new String("01");
////        str2.intern();
//        String str1 = "str01";
//        System.out.println(str2==str1);
//    }
}
