package com.oboenikui.othello.test;

public class L {
    public static void o(Object... o){
        for(int i=0;i<o.length;i++){
            System.out.print(o[i]);
            if(i!=o.length-1){
                System.out.print(",");
            }
        }
        System.out.flush();
        System.out.println();
    }
    
    public static void e(Object... o){
        for(int i=0;i<o.length;i++){
            System.out.print(o[i]);
            if(i!=o.length-1){
                System.out.print(",");
            }
        }
        System.out.flush();
        System.out.println();
    }
}
