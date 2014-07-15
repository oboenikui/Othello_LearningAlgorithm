package com.oboenikui.othello;
import java.awt.BorderLayout;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JFrame;

public class Main {
    public Main() throws IOException, SQLException {
        JFrame jf = new JFrame("Othello");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new BorderLayout());
        jf.add(new AutoOthelloPanel(), BorderLayout.CENTER);
        jf.pack();
        jf.setVisible(true);
        jf.setResizable(false);
    }
    public static void main(String[] args) throws IOException, SQLException {
        new Main();
    }
}