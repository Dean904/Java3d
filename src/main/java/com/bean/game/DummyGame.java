package com.bean.game;

import com.bean.engine.graph.*;
import com.bean.engine.mesh.GameItem;
import com.bean.engine.GameLogic;
import com.bean.engine.MouseInput;
import com.bean.engine.Window;
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

    //temp
    private Vector3f ambientLight;
    private PointLight pointLight;

    int tick = 0;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        Texture grassTexture = new Texture("src/main/resources/textures/grassblock.png");
        Material material = new Material(grassTexture, 1f);

        Mesh cubeMesh = OBJLoader.loadMesh("src/main/resources/models/cube.obj");
        cubeMesh.setMaterial(material);

        for (int i = 0; i < 100; i++) {
            GameItem grassCube = new GameItem(cubeMesh);
            grassCube.setPosition(Math.sin(Math.toRadians((i + 100) * 10)), (float) (i * .2), Math.sin(Math.toRadians((i + 100) * 10)));
            grassCube.setRotation(3 * i, 3 * i, 3 * i);
            grassCube.setScale(0.1f);
            gameItems.add(grassCube);
        }


        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);

        Mesh bunnyMesh = OBJLoader.loadMesh("src/main/resources/models/bunny.obj");
        bunnyMesh.setMaterial(material);
        GameItem bunny = new GameItem(bunnyMesh);
        bunny.setScale(1.5f);
        bunny.setPosition(0, 0, 0);
        gameItems.add(bunny);

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
        if (window.isKeyPressed(GLFW_KEY_TAB)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = 1;
        } else if (window.isKeyPressed(GLFW_KEY_Z)) {
            window.togglePolygonMode();
        }

        //temp
        float lightPos = pointLight.getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.pointLight.getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.pointLight.getPosition().z = lightPos - 0.1f;
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


        //gameItems.forEach(item -> item.setRotation(item.getRotation().add(new Vector3f(1.5f))));

        int tixMax = gameItems.size();
        gameItems.forEach(item -> {
            if (tick == gameItems.indexOf(item)) {
                item.setScale(item.getScale() * 10);
            } else {
                item.setScale(.1f);
            }
        });

        if (tick > tixMax) {
            tick = 0;
        }

        tick++;
    }

    @Override
    public void render(Window window) {
        window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, camera, gameItems, ambientLight, pointLight);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
    }
}
