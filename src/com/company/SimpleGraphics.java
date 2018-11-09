package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public enum SimpleGraphics {
    INSTANCE;

    JFrame frame;
    JPanel panel;
    public Palette palette0;


    public Color[] buffer = new Color[256 * 240];

    SimpleGraphics() {



        frame = new JFrame("Emul");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setTitle("Emul");
        frame.setResizable(false);
        //setUndecorated(true);

        panel = new GPanel();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);


    }


    void addPixel(Integer curPixel, Integer curLine, Integer color, Palette curPalette) {
        buffer[(256 * curLine) + curPixel] = curPalette.getColor(color);

    }





    class GPanel extends JPanel implements ActionListener {
        Timer timer = new Timer(16, this);
        int x = 5, y = 5;
        int h = 20, w = 20;
        int xi = 10, yi = 10;


        int width = 640;
        int height = 480;

        public GPanel() {
            super();

            setDoubleBuffered(true);

            setBackground(Color.black);
            setPreferredSize(new Dimension(640, 480));


            timer.start();
        }

        public void paint(Graphics g) {
            super.paint(g);

            for (int i = 0; i < 256 * 240; i++) {
                g.setColor(buffer[i]);

                int x = i % 256;
                int y = i / 256;
                g.drawLine(x, y, x, y);
            }

            //g.drawRect(x, y, w, h);



        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (x + w > width | x < 0) {
                xi = -xi;
            }

            if (y + h > height | y < 0) {
                yi = -yi;
            }


            x += xi;
            y += yi;

            repaint();


        }


    }

    void init() {

    }


}