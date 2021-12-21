package com.bean;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

public class Shader {

    public Shader(String vertexSourcePath, String fragmentSourcePath) {

        int shaderProgram = compile(vertexSourcePath, fragmentSourcePath);

        // Data Configuration
        int floatSize = 4;

        int posAttrib = glGetAttribLocation(shaderProgram, "position");
        glEnableVertexAttribArray(posAttrib);
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 6 * floatSize, 0);

        int colAttrib = glGetAttribLocation(shaderProgram, "color");
        glEnableVertexAttribArray(colAttrib);
        glVertexAttribPointer(colAttrib, 3, GL_FLOAT, false, 6 * floatSize, 3 * floatSize);

        // Uniform Configuration
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int uniformModelLoc = glGetUniformLocation(shaderProgram, "model");
            FloatBuffer model = new Matrix4f().get(stack.mallocFloat(16));
            glUniformMatrix4fv(uniformModelLoc, false, model);

            int uniformViewLoc = glGetUniformLocation(shaderProgram, "view");
            FloatBuffer view = new Matrix4f().get(stack.mallocFloat(16));
            glUniformMatrix4fv(uniformViewLoc, false, view);

            int uniformProjectionLoc = glGetUniformLocation(shaderProgram, "projection");
            float ratio = 640f / 480f;
            FloatBuffer projection = new Matrix4f().ortho(ratio, ratio, -1f, 1f, -1f, 1f).get(stack.mallocFloat(16));
            glUniformMatrix4fv(uniformProjectionLoc, false, projection);
        }

    }

    private int compile(String vertexSourcePath, String fragmentSourcePath) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, getSource(vertexSourcePath));
        glCompileShader(vertexShader);
        checkCompilation(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, getSource(fragmentSourcePath));
        glCompileShader(fragmentShader);
        checkCompilation(fragmentShader);

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindFragDataLocation(shaderProgram, 0, "fragColor");
        glLinkProgram(shaderProgram);

        int status = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(shaderProgram));
        }

        glUseProgram(shaderProgram);
        System.out.println("Shader program compiled successfully!");
        return shaderProgram;
    }

    private void checkCompilation(int shader) {
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(shader));
        }
    }

    private CharSequence getSource(String sourcePath) {
        try {
            return Files.readString(Path.of(sourcePath), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new RuntimeException("Could not read shader source file '" + sourcePath + "'");
        }
    }

}
