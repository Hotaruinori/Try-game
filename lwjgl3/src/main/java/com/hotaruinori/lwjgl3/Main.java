package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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

    @Override
    public void create() {
        //初始化基礎資源
        infiniteBackground = new InfiniteBackground("background.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        //初始化渲染系統
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // 初始化角色
        character = new Character();

        // 初始化投射物系統
        rainDrops = new Projectiles("drop.png", "drop.mp3", 0.5f, 4f);

        //初始化怪物
        boss1 = new BossA();

        // 其他物件
        touchPos = new Vector2();
        characterRectangle = new Rectangle();

        //設置音樂
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
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

        // 限制角色移動範圍
//        character.getSprite().setX(MathUtils.clamp(
//            character.getX(),
//            0,
//            worldWidth - character.getWidth()
//        ));

        // 更新角色碰撞框
        characterRectangle.set(
            character.getX(),
            character.getY(),
            character.getWidth(),
            character.getHeight()
        );


        // 更新投射物
        rainDrops.update(Gdx.graphics.getDeltaTime(), characterRectangle, viewport, character);
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
        // 更新怪物
        boss1.render(spriteBatch); // 將 batch 傳遞給 BossA 的 render 方法
        character.getSprite().draw(spriteBatch);
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
