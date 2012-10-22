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

    static final int XMARGIN = 50;
    static final int YMARGIN = 50;

    // interface modes
    int selectMode;

    static final int ATTACKER_MODE = 1; // player selects a territory that attacks
    static final int ATTACKEE_MODE = 2; // player selects a territory to attack
    static final int BATTLE_MODE = 3; // battle animation

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
        pathImg = toolkit.getImage("32x32pathH.gif");
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
        selectMode = ATTACKER_MODE;
    }

    public void overlay(Graphics g) {
        // fill squares
        for (int y = 0; y < dimY; y++) {
            for (int x = 0; x < dimX; x++) {
                Territory t = game.getTerritory(y,x);

                g.drawImage(grass, pXmin(x), pYmin(y), pXmax(x), pYmax(y),
                                   (x%2)*32,(y%2)*32,(x%2+1)*32,(y%2+1)*32,this);

                // check if this territory is on the path
                if (t.isPath()) {
                    g.drawImage(pathImg, pXmin(x), pYmin(y), 32, 32, this);
                }

                //g.fillRect(pXmin(x),pYmin(y), sqX, sqY);

                // draw territory ownership & dice info

                switch (t.getOwner()) {
                    case GamePlayer:
                        g.drawImage(playerFlag, pXmin(x), pYmin(y), 32, 32, this);
                        break;
                    case OtherPlayer:
                        // neutral, draw nothing
                        break;
                    case ComputerPlayer:
                        g.drawImage(computerFlag, pXmin(x), pYmin(y), 32, 32, this);
                        break;
                }

                g.setColor(Color.darkGray);
                g.setFont(infoFont);
                g.drawString(""+t.getNoOfDies(), pXmin(x)+2, pYmin(y)+15);
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

        switch (selectMode) {
            case ATTACKER_MODE:
                // draw mouseover cursor for attacker
                if (mouseOverX != -1 && mouseOverY != -1 && game.getTerritory(mouseOverY,mouseOverX).getOwner() == Player.GamePlayer) {
                    g.setColor(Color.red);

                    drawThickRect(g, pXmin(mouseOverX), pYmin(mouseOverY), sqX, sqY, 3);
                }
                break;
            case ATTACKEE_MODE:
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
                // immediately end
                selectMode = ATTACKER_MODE;
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

                    // do the attack
                    game.attack(attackerY, attackerX, attackeeY, attackeeX);

                    selectMode = BATTLE_MODE;
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
