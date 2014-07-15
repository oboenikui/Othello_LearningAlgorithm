package com.oboenikui.othello.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class RuntimeTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("cmd");
        OutputStream outputStream = p.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write("cd edax\\4.3.2\\bin\n");
        writer.write("wEdax-x64.exe -cassio\n\n");
        writer.flush();

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while(true){
            line = reader.readLine();
            if(line.equals("ready.")){
                break;
            }
        }
        writer.write("ENGINE-PROTOCOL midgame-search ---------------------------OX------XOX-------O------------------X -0.51 2.30 27 90\n");
        writer.flush();
        
        System.out.println(reader.readLine());
        System.out.println(reader.readLine());
        p.destroy();
        
    }

}
