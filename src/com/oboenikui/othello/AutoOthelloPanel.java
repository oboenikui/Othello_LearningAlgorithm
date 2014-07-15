package com.oboenikui.othello;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.oboenikui.othello.test.L;

public class AutoOthelloPanel extends JPanel{
    private static final long	serialVersionUID	= 1L;
    private static final int BOX_COUNT = 8;
    private static final boolean SET_ANTIALIASING = true;
    private static final boolean SHOW_HINT = true;
    private static final int BLACK = 0;
    private static final int WHITE = 1;
    private static final int NONE = 2;
    private static final int NEXT = 3;
    private static final int CAN_SET = 4;
    private static final int RIGHT       = 0x01;
    private static final int LEFT        = 0x02;
    private static final int UP          = 0x04;
    private static final int DOWN        = 0x08;
    private static final int UPPER_RIGHT = 0x10;
    private static final int UPPER_LEFT  = 0x20;
    private static final int LOWER_RIGHT = 0x40;
    private static final int LOWER_LEFT  = 0x80;
    private int gameCount = 0;
    private double box_width = 0;
    private double box_height = 0;
    private int nextColor = WHITE;
    private int startColor = WHITE;
    private boolean changed = false;
    private int[] color_count= {0,0};
    private SQLiteDb mSQDb = SQLiteDb.getInstance();
    private List<Integer> candidates = new ArrayList<Integer>();
    private List<OthelloData> enemyData = new ArrayList<OthelloData>();
    private List<OthelloData> playerData = new ArrayList<OthelloData>();

    private Edax mEdax = new Edax();
    private Integer[][] positions = new Integer[BOX_COUNT][BOX_COUNT];

    public AutoOthelloPanel() throws SQLException, IOException {
        super();
        this.setPreferredSize(new Dimension(600, 600));
        reset();
    }

    private void start() throws IOException, SQLException{
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    mSQDb.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                mEdax.close();
            }
        });
        Runnable r = new Runnable() {
            @Override
            public void run() {
                L.o(Runtime.getRuntime());
                while(true){
                    int x,y;
                    String pattern = getPattern();
                    if(nextColor==WHITE){
                        int[] coordinate;
                        try {
                            coordinate = LearningDatabase.getCoordinate(pattern, mSQDb.getData(pattern), candidates);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            break;
                        }
                        if(coordinate!=null){
                            x = coordinate[0];
                            y = coordinate[1];
                            playerData.add(new OthelloData(pattern, 0, 0, x, y));
                        } else {
                            x=-1;
                            y=-1;
                        }
                    } else {
                        int[] coordinate;
                        try {
                            coordinate = mEdax.getCoordinate(pattern);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                        x = coordinate[0];
                        y = coordinate[1];
                        if(x!=-1){
                            enemyData.add(new OthelloData(pattern, 0, 0, x, y));
                        }
                    }
                    if(x==-1||positions[x][y]!=CAN_SET||!judge(x, y, 1, nextColor, RIGHT|LEFT|UP|DOWN|UPPER_LEFT|UPPER_RIGHT|LOWER_LEFT|LOWER_RIGHT, true)){
                        break;
                    }
                    gameCount++;
                    nextColor = nextColor==BLACK?WHITE:BLACK;
                    if(gameCount == BOX_COUNT*BOX_COUNT){
                        showScore();
                        break;
                    } else {
                        addNext(x,y);
                        if(!searchCanSet()){
                            showScore();
                            break;
                        }
                    }
                }
            }
        };
        new Thread(r).start();
        //new Thread(r).start();
    }

    private String getPattern(){
        String result = "";
        for(int y=0;y<8;y++){
            for(int x=0;x<8;x++){
                switch(positions[x][y]){
                case WHITE:
                    result += (nextColor==WHITE?"O":"X");
                    break;
                case BLACK:
                    result += (nextColor==WHITE?"X":"O");
                    break;
                case NONE:
                case NEXT:
                case CAN_SET:
                    result += "-";
                    break;
                }
            }
        }
        return result;
    }

    private void addNext(int x, int y){
        if(x!=0){
            if(y!=0){
                if(positions[x-1][y-1]==NONE){
                    positions[x-1][y-1]=NEXT;
                }
            }
            if(positions[x-1][y]==NONE){
                positions[x-1][y]=NEXT;
            }
            if(y!=BOX_COUNT-1){
                if(positions[x-1][y+1]==NONE){
                    positions[x-1][y+1]=NEXT;
                }
            }
        }
        if(y!=0){
            if(positions[x][y-1]==NONE){
                positions[x][y-1]=NEXT;
            }
        }
        if(y!=BOX_COUNT-1){
            if(positions[x][y+1]==NONE){
                positions[x][y+1]=NEXT;
            }
        }
        if(x!=BOX_COUNT-1){
            if(y!=0){
                if(positions[x+1][y-1]==NONE){
                    positions[x+1][y-1]=NEXT;
                }
            }
            if(positions[x+1][y]==NONE){
                positions[x+1][y]=NEXT;
            }
            if(y!=BOX_COUNT-1){
                if(positions[x+1][y+1]==NONE){
                    positions[x+1][y+1]=NEXT;
                }
            }
        }
    }

    private boolean searchCanSet(){
        int x,y;
        boolean canSet = false;
        candidates.clear();
        for(x=0;x<BOX_COUNT;x++){
            for(y=0;y<BOX_COUNT;y++){
                if(positions[x][y]>=NEXT){
                    boolean b;
                    positions[x][y]=((b=judge(x, y, nextColor, RIGHT|LEFT|UP|DOWN|UPPER_LEFT|UPPER_RIGHT|LOWER_LEFT|LOWER_RIGHT, false))?CAN_SET:NEXT);
                    if(b){
                        candidates.add(x+y*8);
                    }
                    returnPiece(x,y,positions[x][y]);
                    canSet|=b;
                }
            }
        }

        if(!canSet&&!changed){
            nextColor = nextColor==BLACK?WHITE:BLACK;
            changed = true;
            return searchCanSet();
        } else if(!canSet){
            changed = false;
            return false;
        } else if(changed){
            changed = false;
        }
        return true;
    }

    private void showScore(){
        Graphics g2 = getGraphics();
        if(SET_ANTIALIASING){
            ((Graphics2D)g2).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        boolean winnerIsWhite = color_count[WHITE]>color_count[BLACK];
        g2.setColor(Color.red);
        g2.setFont(new Font("Default", Font.PLAIN, 40));
        g2.drawString(winnerIsWhite?"WHITE WIN":"BLACK WIN",
                (int)box_height*BOX_COUNT/3,
                (int)box_width*BOX_COUNT/3);
        for(OthelloData data:playerData){
            try {
                mSQDb.insertData(data.pattern, data.next_x, data.next_y, winnerIsWhite);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        for(OthelloData data:enemyData){
            try {
                mSQDb.insertData(data.pattern, data.next_x, data.next_y, !winnerIsWhite);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        playerData.clear();
        enemyData.clear();
        try {
            Process p = Runtime.getRuntime().exec("cmd /c echo "+(winnerIsWhite?"win,":"lose,")+color_count[WHITE]+","+color_count[BLACK]+" >> log.txt");
            p.waitFor();
            p.destroy();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                removeMouseListener(this);
                reset();
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });*/
        reset();
    }

    private boolean judge(int x, int y, int color, int mode, boolean returnPieces){
        return judge(x, y, 1, color, mode, returnPieces);
    }

    private boolean judge(int x, int y, int n, int color, int mode, boolean returnPieces){
        int next_mode = 0;
        boolean canSet = false;
        int otherColor = color==WHITE?BLACK:WHITE;

        if((mode&RIGHT)!=0){
            if(n<BOX_COUNT-x){
                if (positions[n+x][y]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesX(x, y, n, color);
                        canSet = true;
                    }
                } else if (positions[n+x][y]==otherColor){
                    next_mode|=RIGHT;
                }
            }
        }

        if((mode&LEFT)!=0){
            if(n<=x){
                if (positions[x-n][y]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesX(x-n+1, y, n, color);
                        canSet = true;
                    }
                } else if (positions[x-n][y]==otherColor){
                    next_mode|=LEFT;
                }
            }
        }

        if((mode&UP)!=0){
            if(n<=y){
                if (positions[x][y-n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesY(y-n+1, x, n, color);
                        canSet = true;
                    }
                } else if (positions[x][y-n]==otherColor){
                    next_mode|=UP;
                }
            }
        }

        if((mode&DOWN)!=0){
            if(n<BOX_COUNT-y){
                if (positions[x][y+n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesY(y, x, n, color);
                        canSet = true;
                    }
                } else if (positions[x][y+n]==otherColor){
                    next_mode|=DOWN;
                }
            }
        }

        if((mode&UPPER_RIGHT)!=0){
            if(n<(BOX_COUNT-x>y+1?y+1:BOX_COUNT-x)){
                if (positions[x+n][y-n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesObliquelyL(x+n-1, y-n+1, n, color);
                        canSet = true;
                    }
                } else if (positions[x+n][y-n]==otherColor){
                    next_mode|=UPPER_RIGHT;
                }
            }
        }

        if((mode&UPPER_LEFT)!=0){
            if(n<=(x>y?y:x)){
                if (positions[x-n][y-n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesObliquelyR(x-n+1, y-n+1, n, color);
                        canSet = true;
                    }
                } else if (positions[x-n][y-n]==otherColor){
                    next_mode|=UPPER_LEFT;
                }
            }
        }

        if((mode&LOWER_RIGHT)!=0){
            if(n<((BOX_COUNT-x)>(BOX_COUNT-y)?BOX_COUNT-y:BOX_COUNT-x)){
                if (positions[x+n][y+n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesObliquelyR(x, y, n, color);
                        canSet = true;
                    }
                } else if (positions[x+n][y+n]==otherColor){
                    next_mode|=LOWER_RIGHT;
                }
            }
        }

        if((mode&LOWER_LEFT)!=0){
            if(n<(x+1>BOX_COUNT-y?BOX_COUNT-y:x+1)){
                if (positions[x-n][y+n]==color){
                    if(n!=1){
                        if(returnPieces)
                            returnPiecesObliquelyL(x, y, n, color);
                        canSet = true;
                    }
                } else if (positions[x-n][y+n]==otherColor){
                    next_mode|=LOWER_LEFT;
                }
            }
        }

        if(next_mode==0){
            return canSet;
        } else {
            return (!returnPieces&&canSet)||judge(x, y, n+1, color, next_mode, returnPieces)||canSet;
        }
    }

    private void returnPiecesX(int startX, int y, int length, int color){
        int n;
        for(n=0;n<length;n++){
            returnPiece(n+startX, y, color);
        }
    }

    private void returnPiecesY(int startY, int x, int length, int color){
        int n;
        for(n=0;n<length;n++){
            returnPiece(x, n+startY, color);
        }
    }

    private void returnPiecesObliquelyR(int startX, int startY, int length, int color){
        int n;
        for(n=0;n<length;n++){
            returnPiece(startX+n, startY+n, color);
        }
    }

    private void returnPiecesObliquelyL(final int startX, final int startY, final int length, final int color){
        int n;
        for(n=0;n<length;n++){
            returnPiece(startX-n, startY+n, color);
        }
    }

    private void returnPiece(int x, int y, int color){
        /*if(positions[x][y]==WHITE){
            color_count[WHITE]--;
        } else if(positions[x][y]==BLACK){
            color_count[BLACK]--;
        }
        if(color==WHITE||color==BLACK){
            color_count[color]++;
        }
        positions[x][y]=color;*/
        returnPiece(x, y, color, getGraphics());
    }

    private void returnPiece(int x, int y, int color, Graphics graphics){
        if(SET_ANTIALIASING){
            Graphics2D g2 = (Graphics2D)graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if(x>BOX_COUNT||y>BOX_COUNT||x<0||y<0){
            System.out.println("Index Out Of Bounds");
            return;
        }
        switch (color) {
        case CAN_SET:
            if(SHOW_HINT){
                if(nextColor==WHITE){
                    graphics.setColor(new Color(0x66,0xFF,0x66));
                } else {
                    graphics.setColor(new Color(0x00,0xAA,0x00));
                }
                graphics.fillOval((int)(x*box_width),
                        (int)(y*box_height),
                        (int)(box_width),
                        (int)(box_height));
                break;
            } else {
                return;
            }
        case NEXT:
            if(SHOW_HINT){
                graphics.setColor(Color.green);
                graphics.fillRect(
                        (int)(x*box_width)+1,
                        (int)(y*box_height)+1,
                        ((int)(box_width))-1,
                        ((int)(box_height))-1);
            }
            return;
        case NONE:
            return;
        case WHITE:
            if(positions[x][y]==BLACK){
                color_count[BLACK]--;
            }
            if(positions[x][y]!=WHITE){
                color_count[WHITE]++;
            }
            graphics.setColor(Color.white);
            graphics.fillOval((int)(x*box_width),
                    (int)(y*box_height),
                    (int)(box_width),
                    (int)(box_height));
            break;
        case BLACK:
            if(positions[x][y]==WHITE){
                color_count[WHITE]--;
            }
            if(positions[x][y]!=BLACK){
                color_count[BLACK]++;
            }

            graphics.setColor(Color.black);
            graphics.fillOval((int)(x*box_width),
                    (int)(y*box_height),
                    (int)(box_width),
                    (int)(box_height));
            break;
        }
        graphics.setColor(Color.black);
        graphics.drawOval((int)(x*box_width),
                (int)(y*box_height),
                (int)(box_width),
                (int)(box_height));
        positions[x][y]=color;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(SET_ANTIALIASING){
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        Rectangle r = getBounds();
        g.setColor(Color.green);
        g.fillRect(0, 0, r.width, r.height);
        box_width = ((double)r.width)/BOX_COUNT;
        box_height = ((double)r.height)/BOX_COUNT;
        g.setColor(Color.black);
        for (int i=0; i<BOX_COUNT; i += 1) {
            int x = (int)(i*box_width);
            int y = (int)(i*box_height);
            g.drawLine(x, 0, x, r.height);
            g.drawLine(0, y, r.width, y);
        }
        if(gameCount == 0){
            returnPiece(BOX_COUNT/2-1, BOX_COUNT/2-1, WHITE, g);
            returnPiece(BOX_COUNT/2, BOX_COUNT/2, WHITE, g);
            returnPiece(BOX_COUNT/2-1, BOX_COUNT/2, BLACK, g);
            returnPiece(BOX_COUNT/2, BOX_COUNT/2-1, BLACK, g);
            addNext(BOX_COUNT/2-1, BOX_COUNT/2-1);
            addNext(BOX_COUNT/2, BOX_COUNT/2);
            addNext(BOX_COUNT/2-1, BOX_COUNT/2);
            addNext(BOX_COUNT/2, BOX_COUNT/2-1);
            searchCanSet();
            gameCount += 4;
            try {
                start();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            int x=0,y=0;
            for(x=0;x<BOX_COUNT;x++){
                for(y=0;y<BOX_COUNT;y++){
                    returnPiece(x,y,positions[x][y],g);
                }
            }
        }
    }

    public void resetPositions(){
        int m,n;
        for(m=0;m<BOX_COUNT;m++){
            for(n=0;n<BOX_COUNT;n++){
                positions[m][n] = NONE;
            }
        }
        color_count[WHITE] = 0;
        color_count[BLACK] = 0;
    }

    public void reset(){
        resetPositions();
        gameCount = 0;
        startColor = nextColor = startColor==WHITE?BLACK:WHITE;
        repaint();
    }
}