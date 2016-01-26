package com.kemalbayindir.homeworks.common;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Kemal BAYINDIR on 1/5/2016.
 */
public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (image != null) {
            g2.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public void refresh(BufferedImage image) {
        this.image = image;
        this.repaint();
    }
}

