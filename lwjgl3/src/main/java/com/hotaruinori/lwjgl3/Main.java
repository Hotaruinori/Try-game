package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    // 遊戲資源
    private Music music;
    private InfiniteBackground infiniteBackground;

    //渲染相關
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    //遊戲物件
    private Character character;
    private Vector2 touchPos;
    private Rectangle characterRectangle;
    private Projectiles rainDrops;
    private BossA boss1;

    //暫停選單
    private PauseMenu pauseMenu;


    @Override
    public void create() {
        //初始化基礎資源
        infiniteBackground = new InfiniteBackground("background.png");
        // 初始化隨機背景物件，用來隨機產生背景裝飾物的函式，你可以控制中心點與範圍（這邊用 Vector2(0, 0) 為中心，範圍 20x10，代表覆蓋整個地圖的寬與高）。
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        //初始化渲染系統
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // 初始化角色
        character = new Character();
        character.setBlockingObjects(infiniteBackground.getBlockingObjects());

        // 初始化投射物系統，相關參數後續升級系統做好再放入其中，先在main做呼叫
        rainDrops = new Projectiles("drop.png", "drop.mp3");
        rainDrops.setProjectileCount(5);  //設定投射物數量，後續放進升級系統
        rainDrops.setProjectileSpeed(10.0f);  //設定投射物速度，後續放進升級系統
        rainDrops.setSpawnInterval(0.1f);  //設定投射物發射間隔，後續放進升級系統
        rainDrops.setProjectileSize(1.0f);  //設定投射物發射間隔，後續放進升級系統

        //初始化怪物
        boss1 = new BossA();
        boss1.setPlayer(character);  //初始化追蹤位置並將"玩家的位置"傳給怪物的class

        // 其他物件
        touchPos = new Vector2();
        characterRectangle = new Rectangle();

        //設置音樂
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        //暫停選單
        pauseMenu = new PauseMenu();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // 處理 ESC 鍵：按一下切換暫停/恢復
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (pauseMenu.isVisible()) {
                pauseMenu.hide();
            } else {
                pauseMenu.show(() -> {
                    pauseMenu.hide(); // Resume 遊戲
                }, () -> {
                    Gdx.app.exit(); // Exit 遊戲
                });
            }
        }

        if (!pauseMenu.isVisible()) {
            input();
            logic();
        }

        draw(); // draw 你自己遊戲畫面
        pauseMenu.render(); // 最後畫上 pauseMenu
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
        boolean moving = false;
        Vector2 direction = new Vector2(0, 0);

        // 處理鍵盤輸入（現在可以同時檢測多個按鍵）
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) direction.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) direction.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) direction.y -= 1;

        if (!direction.isZero()) {
            character.moveWithDirection(delta, direction, speed);
            moving = true;
        }

        // 觸控移動保持不變
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            character.moveTo(touchPos.x, touchPos.y);
            moving = true;
        }

        character.update(delta, moving);
        character.updateMovement(delta);
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();


        // 更新角色碰撞框
        characterRectangle.set(
            character.getX(),
            character.getY(),
            character.getWidth(),
            character.getHeight()
        );

        // 更新投射物
        rainDrops.update(Gdx.graphics.getDeltaTime(), characterRectangle, viewport, character);
        //更新怪物
        boss1.update(Gdx.graphics.getDeltaTime());
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        // 更新攝影機位置：讓它跟隨角色
        Vector2 center = character.getCenterPosition();
        viewport.getCamera().position.set(center.x, center.y, 0);
        viewport.getCamera().update();
        // 更新攝影機
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        infiniteBackground.render(spriteBatch, character.getCenterPosition(), worldWidth, worldHeight);
        // 更新怪物、角色與子彈
        boss1.render(spriteBatch); // 將 batch 傳遞給 BossA 的 render 方法
        character.render(spriteBatch);
        //更新角色座標與地圖物件碰撞判定
        infiniteBackground.generateChunksAround(character.getCenterPosition(), 1); //
        character.setBlockingObjects(infiniteBackground.getBlockingObjects());// 或其他範圍大小
        //更新子彈
        rainDrops.render(spriteBatch);

        spriteBatch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        infiniteBackground.dispose();
        music.dispose();
        spriteBatch.dispose();
        character.dispose();
        rainDrops.dispose();
        boss1.dispose();
    }
}
