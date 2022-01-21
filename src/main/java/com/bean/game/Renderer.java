package com.bean.game;

import com.bean.engine.graph.PointLight;
import com.bean.engine.mesh.GameItem;
import com.bean.engine.graph.Camera;
import com.bean.engine.graph.ShaderProgram;
import com.bean.engine.Window;
import com.bean.engine.graph.Transformation;
import com.bean.engine.mesh.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    private float specularPower = 10f;

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
//        shaderProgram.createUniform("colour");
//        shaderProgram.createUniform("useColour");
        // Create uniform for material
        shaderProgram.createMaterialUniform("material");
        // Create lighting related uniforms
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightUniform("pointLight");

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void render(Window window, Camera camera, List<GameItem> gameItems, Vector3f ambientLight,
                       PointLight pointLight) {
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


        // Update Light Uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);
        // Get a copy of the light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        shaderProgram.setUniform("pointLight", currPointLight);


        for (GameItem gameItem : gameItems) {
            Mesh mesh = gameItem.getMesh();
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
//            shaderProgram.setUniform("colour", mesh.getColour());
//            shaderProgram.setUniform("useColour", mesh.isTextured() ? 0 : 1);
            shaderProgram.setUniform("material", mesh.getMaterial());
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
