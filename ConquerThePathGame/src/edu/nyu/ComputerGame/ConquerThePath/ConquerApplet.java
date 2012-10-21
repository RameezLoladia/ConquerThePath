package edu.nyu.ComputerGame.ConquerThePath;

import java.awt.*;
import simpleGamePlatform.*;

public class ConquerApplet extends GamePlatform
{
    int dimX, dimY, sqX, sqY;

    static final int XMARGIN = 100;
    static final int YMARGIN = 100;

    public void init() {
        dimX = 10;
        dimY = 10;
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
                g.setColor(new Color(x*1.0f/dimX, y*1.0f/dimY, 0.5f));
                g.fillRect(XMARGIN+x*sqX,YMARGIN+y*sqY, sqX,sqY);
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
}
