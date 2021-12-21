/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package com.bean.tutorials;

import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.*;

/**
 * In Intro2 we learnt how to allocate an off-heap memory buffer using MemoryUtil. This was done by first calling one of
 * the memAlloc*() methods which return a Java NIO Buffer instance representing the allocated memory region. Once we
 * were done with the buffer, we called the memFree() method to deallocate/free the off-heap memory represented by the
 * Java NIO Buffer.
 * <p>
 * This manual memory management is necessary when a buffer needs to live for an extended amount of time in our
 * application, meaning that the time between allocation and deallocation spans beyond one method.
 * <p>
 * In most scenarios however, the memory will be very short-living. One example was the allocation of the memory to fill
 * the VBO in Intro2. Memory was allocated, filled with data, given to OpenGL and then freed again.
 * <p>
 * LWJGL 3 provides a better way to handle such situations, which is by using the MemoryStack class. This class allows
 * to retrieve a small chunk of memory from a pre-allocated thread-local memory region of a fixed size. By default the
 * maximum size allocatable from the MemoryStack is 8 kilobytes.
 * <p>
 * By the way: It is called a stack because allocations/deallocations must be issued in LIFO order, in that allocations
 * cannot be freed randomly bust must be freed in the reverse allocation order. This allows to avoid any heap allocation
 * and compaction strategies.
 * <p>
 * Also note that the pre-allocated memory of the MemoryStack is per thread. That means, every thread will get its own
 * memory region and MemoryStack instances should not be shared among different threads.
 *
 * @author Kai Burjack
 */
public class Intro3 {

    public static void main(String[] args) {
        glfwInit();
        long window = createWindow();

        /*
         * Wrap the code that is using the MemoryStack in a Java 7 try-with-resources statement. The nice thing here is
         * that the MemoryStack class itself implements AutoCloseable, so it is applicable to being the resource in the
         * try-with-resources statement.
         *
         * What is also new here is that a call to stackPush() actually returns something, namely an instance of the
         * MemoryStack class. This instance represents the thread-local MemoryStack instance, which would otherwise be
         * accessed whenever we call stackPush(), stackPop() or one of the static stackMalloc* methods.
         *
         * So, the code below calls the static method stackPush() on the class MemoryStack, which returns the
         * MemoryStack instance of the current thread. At the end of the try-with-resources statement, a call to
         * MemoryStack.pop() will be done automatically to undo the allocation.
         */
        try (MemoryStack stack = stackPush()) {
            /*
             * The following code is identical to Intro3.
             */
            FloatBuffer buffer = stackMallocFloat(3 * 2);
            buffer.put(-0.5f).put(-0.5f);
            buffer.put(+0.5f).put(-0.5f);
            buffer.put(+0.0f).put(+0.5f);
            buffer.flip();
            int vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

            /*
             * Notice that we do not need a stackPop() here! It will be done automatically at the end of the
             * try-with-resources statement, even in the event of an exception, which was the sole purpose of doing it
             * this way, in the first place.
             */
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 0, 0L);
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glfwSwapBuffers(window);
        }
        glfwTerminate();
        System.out.println("Fin.");
    }

    private static long createWindow() {
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        long window = glfwCreateWindow(800, 600, "Intro3", NULL, NULL);
        glfwMakeContextCurrent(window);
        createCapabilities();
        return window;
    }

}
