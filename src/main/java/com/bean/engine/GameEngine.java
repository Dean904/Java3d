package com.bean.engine;

public class GameEngine implements Runnable {

    public static final int TARGET_UPS = 30;

    private final Thread gameLoopThread; // game loop will be run inside a separate thread

    private GameLogic gameLogic;
    private Window window;
    private Timer timer;
    private MouseInput mouseInput;

    public GameEngine(String windowTitle, int width, int height, boolean vsSync, GameLogic gameLogic) throws Exception {
        gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
        window = new Window(windowTitle, width, height, vsSync);
        mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
    }

    /**
     * Since the thread was created with target "this", starting the thread calls the overridden run()
     */
    public void start() {
        System.out.println("Initializing GameEngine!");
        gameLoopThread.start();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        timer = new Timer();
        window.init();
        mouseInput.init(window);
        gameLogic.init(window);
    }

    protected void gameLoop() {
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!window.windowShouldClose()) {
            float delta = timer.getDelta();
            accumulator += delta;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }


            render();
            // Sync is probably needed for target VSync, dont want to hard-code FPS atm though
        }
    }

    protected void input() {
        mouseInput.input(window);
        gameLogic.input(window, mouseInput);
    }

    protected void update(float interval) {
        gameLogic.update(interval, mouseInput);
    }

    protected void render() {
        gameLogic.render(window);
        window.update();
    }

    protected void cleanup() {
        gameLogic.cleanup();
    }

}
