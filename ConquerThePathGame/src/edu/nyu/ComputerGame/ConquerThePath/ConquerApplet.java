package edu.nyu.ComputerGame.ConquerThePath;

import java.awt.*;
import simpleGamePlatform.*;

public class ConquerApplet extends GamePlatform
{
    ConquerThePathGame game;

    Font infoFont;
    Image grass, playerFlag, computerFlag, pathImg;

    int dimX, dimY, sqX, sqY;
    int mouseOverX, mouseOverY;

    int attackerX, attackerY;
    int attackeeX, attackeeY;
    Territory attackerT, attackeeT;

    static final int XMARGIN = 50;
    static final int YMARGIN = 50;

    // interface modes
    int selectMode;

    static final int ATTACKER_MODE = 1; // player selects a territory that attacks
    static final int ATTACKEE_MODE = 2; // player selects a territory to attack
    static final int BATTLE_MODE = 3; // battle animation

    double animT;   // battle animation timer
    int [][] battleResults;

    //
    public void init() {
        dimX = 10;
        dimY = dimX;
        sqX = 32;
        sqY = 32;

        // make sure it's square
        if (sqX < sqY) sqY = sqX;
        else if (sqY < sqX) sqX = sqY;

        mouseOverX = mouseOverY = -1;

        // bring up the backend
        game = new ConquerThePathGame(dimX);
        game.initialize();

        infoFont = new Font("Serif", Font.PLAIN, 12);

        // load images
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        MediaTracker tracker = new MediaTracker(this);
        grass = toolkit.getImage("64x64grass.gif");
        playerFlag = toolkit.getImage("32x32redflag.gif");
        computerFlag = toolkit.getImage("32x32yellowflag.gif");
        pathImg = toolkit.getImage("32x32path.gif");
        tracker.addImage(grass, 0);
        tracker.addImage(playerFlag, 1);
        tracker.addImage(computerFlag, 2);
        tracker.addImage(pathImg, 3);
        try {
            tracker.waitForAll();
        }
        catch (InterruptedException e) {
            System.err.println("Load Interrupted: "+e);
        }

        //
        battleResults = null;
        selectMode = ATTACKER_MODE;
    }

    public void overlay(Graphics g) {
        switch (selectMode) {
            case ATTACKER_MODE:
                drawBoard(g);

                // draw mouseover cursor for attacker
                if (mouseOverX != -1 && mouseOverY != -1 && game.getTerritory(mouseOverY,mouseOverX).getOwner() == Player.GamePlayer) {
                    g.setColor(Color.red);

                    drawThickRect(g, pXmin(mouseOverX), pYmin(mouseOverY), sqX, sqY, 3);
                }
                break;
            case ATTACKEE_MODE:
                drawBoard(g);

                // draw attacker
                g.setColor(Color.red);

                drawThickRect(g, pXmin(attackerX), pYmin(attackerY), sqX, sqY, 4);

                // draw mouseover cursor for attackee
                if (mouseOverX != -1 && mouseOverY != -1) {
                    Territory t = game.getTerritory(mouseOverY,mouseOverX);
                    
                    if (t.getOwner() != Player.GamePlayer && game.validateAttack(attackerY,attackerX,mouseOverY,mouseOverX)) {
                        g.setColor(Color.black);
                        g.drawOval(pXmin(mouseOverX), pYmin(mouseOverY), sqX, sqY);
                    }
                }
                break;
            case BATTLE_MODE:
                if (animT <= 2) {
                    double localT = animT/2;

                    // zoom into the contested territories
                    drawBoard(g);

                    {
                        int trans[] = setupZoom(localT, 6.5);

                        g.translate(trans[0], trans[1]);
                        drawTerritory(g, attackerX, attackerY, 1+localT*5.5);
                        g.translate(-trans[0], -trans[1]);

                        g.translate(trans[2], trans[3]);
                        drawTerritory(g, attackeeX, attackeeY, 1+localT*5.5);
                        g.translate(-trans[2], -trans[3]);
                    }

                } else if (animT >= 2 && animT < 4) {
                    double localT = (animT-2)/2.0;

                    // really do the battle
                    if (battleResults == null) {
                    game.attack(attackerY, attackerX, attackeeY, attackeeX);

                        battleResults = new int[2][3];
                        battleResults[0][0] = 1;
                        battleResults[0][1] = 2;
                        battleResults[0][2] = 3;
                        battleResults[1][0] = 4;
                        battleResults[1][1] = 5;
                        battleResults[1][2] = 6;
                    }

                    //
                    drawBoard(g);

                    {
                        int trans[] = setupZoom(1.0, 6.5);

                        // show dice
                        for (int i = 0; i < battleResults[0].length; i++) {
                            drawDie(g, XMARGIN+i*32, getHeight()-YMARGIN/2, battleResults[0][i]);
                        }
                        for (int i = 0; i < battleResults[1].length; i++) {
                            drawDie(g, getWidth()-(XMARGIN+i*32), getHeight()-YMARGIN/2, battleResults[1][i]);
                        }

                        if (animT < 3) {
                            g.translate(trans[0], trans[1]);
                            drawTerritory(g, attackerT, attackerX, attackerY, 6.5);
                            g.translate(-trans[0], -trans[1]);

                            g.translate(trans[2], trans[3]);
                            drawTerritory(g, attackeeT, attackeeX, attackeeY, 6.5);
                            g.translate(-trans[2], -trans[3]);
                        }
                        else {
                            g.translate(trans[0], trans[1]);
                            drawTerritory(g, attackerX, attackerY, 6.5);
                            g.translate(-trans[0], -trans[1]);

                            g.translate(trans[2], trans[3]);
                            drawTerritory(g, attackeeX, attackeeY, 6.5);
                            g.translate(-trans[2], -trans[3]);
                        }

                    }

                }
                else if (animT >= 4 && animT < 6) {
                    double localT = (animT-4)/2;

                    // zoom out
                    drawBoard(g);

                    {
                        int trans[] = setupZoom(1-localT, 6.5);

                        g.translate(trans[0], trans[1]);
                        drawTerritory(g, attackerX, attackerY, 1+(1-localT)*5.5);
                        g.translate(-trans[0], -trans[1]);

                        g.translate(trans[2], trans[3]);
                        drawTerritory(g, attackeeX, attackeeY, 1+(1-localT)*5.5);
                        g.translate(-trans[2], -trans[3]);
                    }

                }
                else if (animT >= 6)
                {
                    battleResults = null;
                    attackerT = null;
                    attackeeT = null;
                    selectMode = ATTACKER_MODE;
                }

                animT += 0.1;
                break;
        }

    }

    // **** pixel/grid coordinate conversion
    // convert grid coordinate to minimum pixel X
    public int pXmin(int gridX) {
        return XMARGIN+gridX*sqX;
    }

    // convert grid coordinate to maximum pixel X (noninclusive)
    public int pXmax(int gridX) {
        return XMARGIN+(gridX+1)*sqX;
    }

    // convert grid coordinate to minimum pixel Y
    public int pYmin(int gridY) {
        return YMARGIN+gridY*sqY;
    }

    // convert grid coordinate to maximum pixel Y (noninclusive)
    public int pYmax(int gridY) {
        return YMARGIN+(gridY+1)*sqY;
    }

    // convert pixel X to grid X
    public int p2gX(int pX) {
        int result = (int)Math.floor((double)(pX - XMARGIN)/sqX);
        if (result >= 0 && result < dimX) {
            return result;
        } else {
            return -1;
        }
    }

    // convert pixel Y to grid Y
    public int p2gY(int pY) {
        int result = (int)Math.floor((double)(pY - YMARGIN)/sqY);
        if (result >= 0 && result < dimY) {
            return result;
        } else {
            return -1;
        }
    }

    // **** graphics subroutines

    // a rectangle that grows outward
    public void drawThickRect(Graphics g, int x, int y, int width, int height, int thickness) {
        for (int i = 0; i < thickness; i++) {
            g.drawRect(x-i,y-i,width+2*i,height+2*i);
        }
    }

    public int lerpInt(int startx, int endx, double t) {
        return (int)((endx-startx)*t+startx);
    }

    // z from 0 to 1
    // {attacker X, attacker Y, attackee X, attackee Y}
    public int[] setupZoom(double z, double finalScale)
    {
        int erXmin = pXmin(attackerX);
        int erYmin = pYmin(attackerY);
        int erXmax = pXmax(attackerX);
        int erYmax = pYmax(attackerY);

        int eeXmin = pXmin(attackeeX);
        int eeYmin = pYmin(attackeeY);
        int eeXmax = pXmax(attackeeX);
        int eeYmax = pYmax(attackeeY);

        int minX = Math.min(erXmin, eeXmin);
        int minY = Math.min(erYmin, eeYmin);
        int maxX = Math.max(erXmax, eeXmax);
        int maxY = Math.max(erYmax, eeYmax);


        int[] res = new int[4];

        // attacker X translation
        res[0] = lerpInt(erXmin, (int)(finalScale*(erXmin-minX)+XMARGIN), z) - erXmin;
        // attacker Y translation
        res[1] = lerpInt(erYmin, (int)(finalScale*(erYmin-minY)+YMARGIN), z) - erYmin;

        // attackee X translation
        res[2] = lerpInt(eeXmin, (int)(finalScale*(eeXmin-minX)+XMARGIN), z) - eeXmin;
        // attackee Y translation
        res[3] = lerpInt(eeYmin, (int)(finalScale*(eeYmin-minY)+YMARGIN), z) - eeYmin;

        if (erXmin == eeXmin) {
            // vertical layout, fudge to make it expand outwards
            res[0] = 0;
            res[2] = 0;
        }

        return res;
    }

    public void drawTerritory(Graphics g, int x, int y) {
        Territory t = game.getTerritory(y,x);
        drawTerritory(g, t, x, y, 1.0);
    }

    public void drawTerritory(Graphics g, int x, int y, double scale) {
        Territory t = game.getTerritory(y,x);
        drawTerritory(g, t, x, y, scale);
    }

    public void drawTerritory(Graphics g, Territory t, int x, int y, double scale) {

        int xmin = pXmin(x);
        int ymin = pYmin(y);
        int xmax = (int)((pXmax(x)-xmin)*scale+xmin);
        int ymax = (int)((pYmax(y)-ymin)*scale+ymin);

        g.drawImage(grass, xmin, ymin, xmax, ymax,
                           (x%2)*32,(y%2)*32,(x%2+1)*32,(y%2+1)*32,this);

        // check if this territory is on the path
        if (t.isPath()) {
            Territory above = (y==0?null: game.getTerritory(y-1,x));
            Territory below = (y==dimY-1?null: game.getTerritory(y+1,x));
            Territory left  = (x==0?null: game.getTerritory(y,x-1));
            Territory right = (x==dimX-1?null: game.getTerritory(y,x+1));

            int idx = ((above!=null && above.isPath())?1 : 0) |
                      ((below!=null && below.isPath())?2 : 0) |
                      ((left !=null && left .isPath())?4 : 0) |
                      ((right!=null && right.isPath())?8 : 0);

            int sx=0, sy=0;
            // decide how to draw path
            if ((idx & 3) == 3 || idx == 1 || idx == 2) {
                // vertical
                sx = 1;
            } else if ((idx & 12) == 12 || idx == 4 || idx == 8) {
                // horizontal
                sx = 0;
            } else if ((idx & 5) == 5) {
                // above&left: lower right
                sx = 4;
            } else if ((idx & 9) == 9) {
                // above&right: lower left
                sx = 5;
            } else if ((idx & 6) == 6) {
                // below&left: upper right
                sx = 3;
            } else if ((idx & 10) == 10) {
                // below&rigt: upper left
                sx = 2;
            }

            sx *= 32;
            g.drawImage(pathImg, xmin, ymin, xmax, ymax, sx, sy, sx+32, sy+32, this);
        }

        // draw territory ownership & dice info

        switch (t.getOwner()) {
            case GamePlayer:
                g.drawImage(playerFlag, xmin, ymin, (int)(32*scale), (int)(32*scale), this);
                break;
            case OtherPlayer:
                // neutral, draw nothing
                break;
            case ComputerPlayer:
                g.drawImage(computerFlag, xmin, ymin, (int)(32*scale), (int)(32*scale), this);
                break;
        }

        g.setColor(Color.darkGray);
        g.setFont(infoFont);
        g.drawString(""+t.getNoOfDies(), xmin+2, ymin+15);
    }

    public void drawBoard(Graphics g) {
        // fill squares
        for (int y = 0; y < dimY; y++) {
            for (int x = 0; x < dimX; x++) {
                drawTerritory(g, x, y);
            }
        }

        // draw grid
        g.setColor(Color.black);

        for (int y = 0; y <= dimY; y++) {
            int yc = YMARGIN + y * sqY;
            g.drawLine(XMARGIN, yc, XMARGIN + dimX*sqX, yc);
        }

        for (int x = 0; x <= dimX; x++) {
            int xc = XMARGIN + x * sqX;
            g.drawLine(xc, YMARGIN, xc, YMARGIN + dimY*sqY);
        }
    }

    static final double[][][] diePatterns = {
        {{0,0}},                            // 1
        {{-0.5,-0.5}, {0.5,0.5}},           // 2
        {{-0.5,-0.5},{0,0},{0.5,0.5}},  // 3
        {{-0.5,-0.5}, {-0.5,0.5}, {0.5,-0.5}, {0.5,0.5}},   // 4
        {{-0.5,-0.5}, {-0.5,0.5}, {0.5,-0.5}, {0.5,0.5}, {0,0}},   // 5
        {{-0.4,-0.5},{-0.4,0},{-0.4,0.5},{0.4,-0.5},{0.4,0},{0.4,0.5}}    // 6
    };

    // die at (x,y), numbered n
    public void drawDie(Graphics g, int x, int y, int n) {
        g.setColor(Color.white);
        g.fillRoundRect(x, y, 16, 16, 5, 5);
        g.setColor(Color.black);
        g.drawRoundRect(x, y, 16, 16, 5, 5);
        
        if (n > 0) {
            for (double[] p : diePatterns[n-1]) {
                g.fillRect(x+7+(int)(8*p[0]), y+7+(int)(8*p[1]), 3, 3);
            }
        }
    }

    // **** high level mouse interface
    public void selectAt(int gx, int gy) {
        switch (selectMode) {
            case ATTACKER_MODE:
                if (game.getTerritory(gy,gx).getOwner() == Player.GamePlayer) {
                    attackerX = gx;
                    attackerY = gy;
                    selectMode = ATTACKEE_MODE;
                }

                break;
            case ATTACKEE_MODE:
                if (game.validateAttack(attackerX, attackerY, gx, gy)) {
                    attackeeX = gx;
                    attackeeY = gy;

                    // begin the attack
                    selectMode = BATTLE_MODE;
                    animT = 0;
                    // save territory status to only redraw after battle
                    {
                        Territory t0 = game.getTerritory(attackerY, attackerX);
                        Territory t1 = game.getTerritory(attackeeY, attackeeX);
                        attackerT = new Territory(t0.isPath(), t0.getOwner(), t0.getNoOfDies());
                        attackeeT = new Territory(t1.isPath(), t1.getOwner(), t1.getNoOfDies());
                    }
                }
                break;
            case BATTLE_MODE:
                // ignore clicks

                break;
        }
    }

    // **** mouse interface
    public void mouseAt(int x, int y) {
        mouseOverX = p2gX(x);
        mouseOverY = p2gY(y);
    }

    public boolean mouseMove(Event e, int x, int y) {
        mouseAt(x,y);
        return true;
    }

    public boolean mouseDrag(Event e, int x, int y) {
        mouseAt(x,y);
        return true;
    }

    // make sure we know where the mouse is if it starts on the grid
    public boolean mouseEnter(Event e, int x, int y) {
        mouseAt(x,y);
        return true;
    }

    public boolean mouseDown(Event e, int x, int y) {
        int gx = p2gX(x);
        int gy = p2gY(y);

        if (gx == -1 || gy == -1) {
            // not on the grid, ignore
            return true;
        }

        selectAt(gx,gy);

        return true;
    }

}
