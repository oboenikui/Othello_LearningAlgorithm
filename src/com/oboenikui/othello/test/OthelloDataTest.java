package com.oboenikui.othello.test;

import com.oboenikui.othello.OthelloData;

public class OthelloDataTest {
    public static void main(String[] args) {
        String[] patterns = OthelloData.getOthelloPatterns("---------------------------OX-----OOX-------X-------------------");
        int[] xs = OthelloData.getXs(2, 3);
        int[] ys = OthelloData.getYs(2, 3);
        for(int i=0;i<8;i++){
            L.o("pattern='"+patterns[i]+"' or ");
        }
    }
}
