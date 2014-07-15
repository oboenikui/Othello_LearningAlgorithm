package com.oboenikui.othello.test;

import java.io.IOException;

import com.oboenikui.othello.Edax;

public class EdaxTest {
    public static void main(String[] args) throws IOException {
        Edax edax = new Edax();
        L.o("a");
        int[] a = edax.getCoordinate("--O-------OXXXX---OXXXXO--OXXXXO--OXXOXO--OXOXXO--OOOOXO--OOXXX-");
        L.o(a[0]+","+a[1]);
        edax.close();
    }
}
