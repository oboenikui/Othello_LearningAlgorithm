package com.oboenikui.othello;

public class OthelloData {
    public String pattern;
    public long win_count;
    public long lose_count;
    public int next_x;
    public int next_y;
    public OthelloData(String pattern, long win_count, long lose_count, int next_x, int next_y) {
        this.pattern = pattern;
        this.win_count = win_count;
        this.lose_count = lose_count;
        this.next_x = next_x;
        this.next_y = next_y;
    }
    
    public static String[] getOthelloPatterns(String pattern){
        String[] patterns = new String[8];
        char[] orig = pattern.toCharArray();
        patterns[0] = pattern;
        for(int i=1;i<8;i++){
            patterns[i] = "";
        }
        for(int y=0;y<8;y++){
            for(int x=0;x<8;x++){
                patterns[1] += orig[8*(7-x)+y];
                patterns[2] += orig[7-x+8*(7-y)];
                patterns[3] += orig[8*x+7-y];
                patterns[4] += orig[7-x+8*y];
                patterns[5] += orig[8*(7-x)+7-y];
                patterns[6] += orig[x+8*(7-y)];
                patterns[7] += orig[8*x+y];
            }
        }
        return patterns;
    }
    
    public static int[] getXs(int x, int y){
        int[] xs = new int[8];
        xs[0] = x;
        xs[1] = 7-y;
        xs[2] = 7-x;
        xs[3] = y;
        xs[4] = 7-x;
        xs[5] = 7-y;
        xs[6] = x;
        xs[7] = y;
        return xs;
    }

    public static int[] getYs(int x, int y){
        int[] ys = new int[8];
        ys[0] = y;
        ys[1] = x;
        ys[2] = 7-y;
        ys[3] = 7-x;
        ys[4] = y;
        ys[5] = 7-x;
        ys[6] = 7-y;
        ys[7] = x;
        return ys;
    }
    
    public static int[] restoreCoordinate(int x, int y, int pattern){
        int[] coordinate = new int[2];
        switch (pattern) {
        case 0:
            coordinate[0] = x;
            coordinate[1] = y;
            break;
        case 1:
            coordinate[0] = y;
            coordinate[1] = 7-x;
            break;
        case 2:
            coordinate[0] = 7-x;
            coordinate[1] = 7-y;
            break;
        case 3:
            coordinate[0] = 7-y;
            coordinate[1] = x;
            break;
        case 4:
            coordinate[0] = 7-x;
            coordinate[1] = y;
            break;
        case 5:
            coordinate[0] = 7-y;
            coordinate[1] = 7-x;
            break;
        case 6:
            coordinate[0] = x;
            coordinate[1] = 7-y;
            break;
        case 7:
            coordinate[0] = y;
            coordinate[1] = x;
            break;
        }
        return coordinate;
    }
}
