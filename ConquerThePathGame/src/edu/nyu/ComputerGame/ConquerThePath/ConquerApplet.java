package edu.nyu.ComputerGame.ConquerThePath;

import java.awt.*;
import simpleGamePlatform.*;

public class ConquerApplet extends GamePlatform
{
    ConquerThePathGame game;

    int dimX, dimY, sqX, sqY;
    int mouseOverX, mouseOverY;

    int attackerX, attackerY;
    int attackeeX, attackeeY;

    static final int XMARGIN = 50;
    static final int YMARGIN = 50;

    // interface modes:
    int selectMode;

    static final int ATTACKER_MODE = 1; // player selects a territory that attacks
    static final int ATTACKEE_MODE = 2; // player selects a territory to attack
    static final int BATTLE_MODE = 3; // battle animation

    public void init() {
        dimX = 10;
        dimY = dimX;

        mouseOverX = mouseOverY = -1;

        // bring up the backend
        game = new ConquerThePathGame(dimX);
        game.initialize();

        selectMode = ATTACKER_MODE;
    }

    public void overlay(Graphics g) {
        sqX = (getWidth()-(XMARGIN*2))/dimX;
        sqY = (getHeight()-(YMARGIN*2))/dimY;

        // make sure it's square
        if (sqX < sqY) sqY = sqX;
        else if (sqY < sqX) sqX = sqY;

        // fill squares
        for (int y = 0; y < dimY; y++) {
            for (int x = 0; x < dimX; x++) {
                // check if this territory is on the path
                if (game.getTerritory(y,x).isPath()) {
                    g.setColor(Color.yellow);
                } else {
                    g.setColor(Color.green);
                }

                g.fillRect(pXmin(x),pYmin(y), sqX,sqY);
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

        if (selectMode == ATTACKEE_MODE) {
            // draw attacker
            g.setColor(Color.red);

            drawThickRect(g, pXmin(attackerX), pYmin(attackerY), sqX, sqY, 4);
        }
        else if (selectMode == BATTLE_MODE) {
        }

        // draw mouseover cursor
        if (mouseOverX != -1 && mouseOverY != -1) {
            g.setColor(Color.black);

            g.drawOval(pXmin(mouseOverX), pYmin(mouseOverY), sqX, sqY);
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
                attackerX = gx;
                attackerY = gy;

                selectMode = ATTACKEE_MODE;
                break;
            case ATTACKEE_MODE:
                if (game.validateAttack(attackerX, attackerY, gx, gy)) {
                    attackeeX = gx;
                    attackeeY = gy;

                    // do the attack
                    game.attack(attackerX, attackerY, attackeeX, attackeeY);

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
