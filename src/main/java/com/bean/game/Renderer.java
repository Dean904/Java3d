package com.bean.game;

import com.bean.engine.mesh.GameItem;
import com.bean.engine.graph.Camera;
import com.bean.engine.graph.ShaderProgram;
import com.bean.engine.Window;
import com.bean.engine.graph.Transformation;
import com.bean.engine.mesh.Mesh;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    ShaderProgram shaderProgram;
    Transformation transformation;

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(loadResource("src/main/resources/shaders/vertex.shader"));
        shaderProgram.createFragmentShader(loadResource("src/main/resources/shaders/fragment.shader"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void render(Window window, Camera camera, List<GameItem> gameItems) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        shaderProgram.setUniform("texture_sampler", 0);


        Matrix4f viewMatrix = transformation.getViewMatrix(camera); // Update view Matrix

        for (GameItem gameItem : gameItems) {
            Mesh mesh = gameItem.getMesh();
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            gameItem.getMesh().render();
        }

        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private String loadResource(String sourcePath) {
        try {
            return Files.readString(Path.of(sourcePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not read shader source file '" + sourcePath + "'");
        }
    }

}
