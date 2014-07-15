package com.oboenikui.othello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.oboenikui.othello.test.L;

public class Edax {
    OutputStreamWriter mOutputStreamWriter;
    BufferedReader mBufferedReader;
    BufferedReader mErrorReader;
    Process mProcess;
    public Edax() throws IOException {
        Runtime r = Runtime.getRuntime();
        mProcess = r.exec("cmd");
        OutputStream outputStream = mProcess.getOutputStream();
        mOutputStreamWriter = new OutputStreamWriter(outputStream);
        mOutputStreamWriter.write("cd edax\\bin\n");
        mOutputStreamWriter.write("wEdax-x64.exe -cassio\n\n");
        mOutputStreamWriter.flush();
        mBufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
        mErrorReader = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
        while(true){
            if("ready.".equals(mBufferedReader.readLine())){
                break;
            }
        }
    }

    public int[] getCoordinate(String pattern) throws IOException, RuntimeException{
        L.o("ENGINE-PROTOCOL midgame-search "+pattern+"O 0 1 1 100");
        mOutputStreamWriter.write("ENGINE-PROTOCOL midgame-search "+pattern+"O 0 1 1 100\n");
        mOutputStreamWriter.flush();
        for(int i=0;!mBufferedReader.ready()&&i<50;i++){
            if(mErrorReader.ready()){
                L.o(mErrorReader.readLine());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!mBufferedReader.ready()){
            throw new RuntimeException();
        }
        String result = mBufferedReader.readLine();
        L.o(result);
        L.o(mBufferedReader.readLine());
        char[] tmp = result.split(" ")[2].toCharArray();
        int[] xy = new int[2];
        if(tmp[0]=='-'){
            xy[0] = -1;
            xy[1] = -1;
        } else {
            if(tmp[0]>0x60){
                xy[0] = (int)tmp[0]-0x61;
            } else {
                xy[0] = (int)tmp[0]-0x41;
            }
            xy[1] = tmp[1]-0x31;
        }
        return xy;
    }
    
    public void close(){
        try {
            mOutputStreamWriter.close();
            mBufferedReader.close();
            mProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
