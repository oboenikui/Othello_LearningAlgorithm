package com.oboenikui.othello;

import java.util.List;

public class LearningDatabase {

    public static int[] getCoordinate(String pattern, List<OthelloData> data, List<Integer> candidates){
        long[] dist = getDistribution(pattern, data, candidates);
        long sum = 0;
        for(long t:dist){
            sum += t;
        }
        long value = (long)Math.floor(Math.random()*sum);
        for(int i=0;i<candidates.size();i++){
            value -= dist[i];
            if(value<=0){
                int[] coordinate = new int[2];
                int index = candidates.get(i);
                coordinate[0] = index%8;
                coordinate[1] = index/8;
                return coordinate;
            }
        }
        return null;
    }

    private static long[] getDistribution(String pattern, List<OthelloData> data, List<Integer> candidates){
        long[] counts = new long[candidates.size()];
        // TODO Please uncomment if you want to consider lose count.
        /*if(data.size()==0){
            for(int i=0;i<candidates.size();i++){
                counts[i]=1;
            }
        } else {
            for(OthelloData child:data){
                int index = candidates.indexOf(child.next_x+child.next_y*8);
                counts[index] += candidates.size()*child.win_count;
                for(int i=0;i<candidates.size();i++){
                    if(i!=index){
                        counts[i] += child.lose_count;
                    }
                }
            }
        }*/
        for(int i=0;i<candidates.size();i++){
            counts[i]=1;
        }
        for(OthelloData child:data){
            int index = candidates.indexOf(child.next_x+child.next_y*8);
            counts[index] += child.win_count;
        }
        return counts;
    }
}
