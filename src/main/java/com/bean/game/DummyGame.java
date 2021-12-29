package com.bean.game;

import com.bean.engine.mesh.GameItem;
import com.bean.engine.GameLogic;
import com.bean.engine.MouseInput;
import com.bean.engine.Window;
import com.bean.engine.graph.Camera;
import com.bean.engine.mesh.CubeMeshFactory;
import com.bean.engine.mesh.Mesh;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements GameLogic {

    private int direction = 0;
    private float color = 0.0f;

    private final Renderer renderer;
    private List<GameItem> gameItems = new ArrayList<>();

    private final Vector3f cameraInc;
    private final Camera camera;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        Mesh cubeMesh = CubeMeshFactory.getCubeMesh();
        for (int i = 0; i < 100; i++) {
            GameItem grassCube = new GameItem(cubeMesh);
            grassCube.setPosition(Math.sin(Math.toRadians((i + 100) * 10)), (float) (i * .2), Math.sin(Math.toRadians((i + 100) * 10)));
            grassCube.setRotation(3 * i, 3 * i, 3 * i);
            gameItems.add(grassCube);
        }
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera position
        float CAMERA_POS_STEP = 0.1f;
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP,
                cameraInc.y * CAMERA_POS_STEP,
                cameraInc.z * CAMERA_POS_STEP);

        // Update camera based on mouse
        float MOUSE_SENSITIVITY = 0.4f;

        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplacementVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        gameItems.forEach(item -> item.setRotation(item.getRotation().add(new Vector3f(1.5f))));

//        float rotation = gameItems.get(0).getRotation().x + 1.5f;
//        if (rotation > 360) {
//            rotation = 0;
//        }
//        gameItems.get(0).setRotation(rotation, rotation, rotation);
    }

    @Override
    public void render(Window window) {
        window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, camera, gameItems);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
    }
}
