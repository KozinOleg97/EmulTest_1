package com.company;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import sun.font.TrueTypeFont;

import java.awt.*;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Graphics {

    // The mainWindow handle
    private long mainWindow;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the mainWindow callbacks and destroy the mainWindow
        glfwFreeCallbacks(mainWindow);
        glfwDestroyWindow(mainWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current mainWindow hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the mainWindow will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the mainWindow will be resizable

        // Create the mainWindow
        int width = 800;
        int heigth = 600;

        mainWindow = glfwCreateWindow(width, heigth, "Hello World!", NULL, NULL);
        if (mainWindow == NULL)
            throw new RuntimeException("Failed to create the GLFW mainWindow");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(mainWindow, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the mainWindow size passed to glfwCreateWindow
            glfwGetWindowSize(mainWindow, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            System.out.println(vidmode.width() + "x" + vidmode.height());

            // Center the mainWindow
            glfwSetWindowPos(
                    mainWindow,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically


        // Make the OpenGL context current
        glfwMakeContextCurrent(mainWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the mainWindow visible
        glfwShowWindow(mainWindow);
    }


    private void myRectungle(int x, int y, int w, int h, float r, float g, float b) {
        glPushMatrix();  //Make sure our transformations don't affect any other transformations in other code
        glTranslatef(x, y, 0.0f);  //Translate rectangle to its assigned x and y position
        //Put other transformations here
        glBegin(GL_QUADS);   //We want to draw a quad, i.e. shape with four sides
        glColor3f(r, g, b); //Set the colour to red
        glVertex2f(0, 0);            //Draw the four corners of the rectangle
        glVertex2f(0, h);
        glVertex2f(w, h);
        glVertex2f(w, 0);
        glEnd();
        glPopMatrix();
    }


    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        //glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        ////////////////////////////////////////////////////
        int width = 800;
        int heigth = 600;
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);  //Set the cleared screen colour to black
        glViewport(0, 0, width, heigth);   //This sets up the viewport so that the coordinates (0, 0) are at the top left of the window

        //Set up the orthographic projection so that coordinates (0, 0) are in the top left
        //and the minimum and maximum depth is -10 and 10. To enable depth just put in
        //glEnable(GL_DEPTH_TEST)
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, heigth, 0, -10, 10);

        //Back to the modelview so we can draw stuff
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear the screen and depth buffer
/////////////////////////////////////////////////////////////


        // Run the rendering loop until the user has attempted to close
        // the mainWindow or has pressed the ESCAPE key.


        int x = 5, y = 5;
        int h = 20, w = 20;
        int xi = 10, yi = 10;

        int tx = 300, ty = 200;


        double lastTime = glfwGetTime();
        int nbFrames = 0;

        while (!glfwWindowShouldClose(mainWindow)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer


            if (x + w > width | x < 0) {
                xi = -xi;
            }

            if (y + h > heigth | y < 0) {
                yi = -yi;
            }


            x += xi;
            y += yi;
            myRectungle(x, y, w, h, 1, 0, 0);


            glBegin(GL_TRIANGLES);
            glColor3f(0.25f, 0.85f, 0.5f);
            glVertex2f(tx, ty);
            glVertex2f(tx + 50.0f, ty + 50.0f);
            glVertex2f(tx + 70.0f, ty + -70.0f);
            glEnd();
            glPopMatrix();

            glfwSwapBuffers(mainWindow); // swap the color buffers


            // Poll for mainWindow events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();


            // Measure speed(fps)
            double currentTime = glfwGetTime();
            nbFrames++;
            if (currentTime - lastTime >= 1.0) {
                // printf and reset timer
                System.out.println(1000.0 / (nbFrames) + "(" + nbFrames + ")");
                nbFrames = 0;
                lastTime += 1.0;
            }


        }


    }
}