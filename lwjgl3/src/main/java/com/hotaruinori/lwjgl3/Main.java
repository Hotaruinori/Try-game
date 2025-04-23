package com.hotaruinori.lwjgl3;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.badlogic.gdx.graphics.g2d.TextureRegion; //角色移動用
import com.badlogic.gdx.graphics.g2d.Animation; //角色移動用



public class Main implements ApplicationListener {
    // 遊戲資源
    Texture backgroundTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;

    //渲染相關
    SpriteBatch spriteBatch;
    FitViewport viewport;

    //遊戲物件
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    // 角色動畫系統
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private TextureRegion[] standingFrames; // 站立幀
    private float stateTime = 0; // 動畫狀態時間

    //角色狀態
    private CharacterState state = CharacterState.STANDING; // 角色狀態
    private FacingDirection facing = FacingDirection.DOWN; // 面向方向

    //枚舉類型
    private enum CharacterState {
        STANDING, WALKING
    }

    private enum FacingDirection {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void create() {
        //初始化基礎資源
        backgroundTexture = new Texture("background.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        //初始化渲染系統
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        // 新增角色動畫初始化
        initCharacterAnimations();
        // 修改bucketSprite初始化，使用動畫的第一幀
        bucketSprite = new Sprite(standingFrames[FacingDirection.DOWN.ordinal()]);
        bucketSprite.setSize(1, 1);
        // 其他物件
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        //設置音樂
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

    }
    private void initCharacterAnimations() {
        // 加載站立幀紋理
        standingFrames = new TextureRegion[4];
        standingFrames[FacingDirection.UP.ordinal()] = new TextureRegion(new Texture("character/dora_walk_back1.png"));
        standingFrames[FacingDirection.DOWN.ordinal()] = new TextureRegion(new Texture("character/dora_standing.png"));
        standingFrames[FacingDirection.LEFT.ordinal()] = new TextureRegion(new Texture("character/dora_walk_left_1.png"));
        standingFrames[FacingDirection.RIGHT.ordinal()] = new TextureRegion(new Texture("character/dora_walk_right_1.png"));

        // 初始化走路動畫 (每方向2幀)
        walkUpAnimation = new Animation<TextureRegion>(0.15f,
            new TextureRegion(new Texture("character/dora_walk_back1.png")),
            new TextureRegion(new Texture("character/dora_walk_back2.png"))
        );
        walkDownAnimation = new Animation<TextureRegion>(0.15f,
            new TextureRegion(new Texture("character/dora_standing.png")),
            new TextureRegion(new Texture("character/dora_standing.png"))
        );
        walkLeftAnimation = new Animation<TextureRegion>(0.15f,
            new TextureRegion(new Texture("character/dora_walk_left_1.png")),
            new TextureRegion(new Texture("character/dora_walk_left_2.png"))
        );
        walkRightAnimation = new Animation<TextureRegion>(0.15f,
            new TextureRegion(new Texture("character/dora_walk_right_1.png")),
            new TextureRegion(new Texture("character/dora_walk_right_2.png"))
        );
        // 其他方向類似...
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

        // 上下左右鍵盤移動
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
            facing = FacingDirection.RIGHT;
            moving = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
            facing = FacingDirection.LEFT;
            moving = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            bucketSprite.translateY(speed * delta);
            facing = FacingDirection.UP;
            moving = true;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            bucketSprite.translateY(-speed * delta);
            facing = FacingDirection.DOWN;
            moving = true;
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
            // 根據觸摸位置決定面向方向
            facing = (touchPos.x > bucketSprite.getX()) ? FacingDirection.RIGHT : FacingDirection.LEFT;
            moving = true;
        }
        //呼叫動畫狀態更新
        updateAnimationState(moving, delta);
    }
    //動畫狀態更新
    private void updateAnimationState(boolean moving, float delta) {
        if (moving) {
            state = CharacterState.WALKING;
            stateTime += delta;
            bucketSprite.setRegion(getCurrentAnimationFrame());
        } else {
            state = CharacterState.STANDING;
            stateTime = 0;
            bucketSprite.setRegion(standingFrames[facing.ordinal()]);
        }
    }
    //動畫狀態更新
    private TextureRegion getCurrentAnimationFrame() {
        switch (facing) {
            case UP: return walkUpAnimation.getKeyFrame(stateTime, true);
            case DOWN: return walkDownAnimation.getKeyFrame(stateTime, true);
            case LEFT: return walkLeftAnimation.getKeyFrame(stateTime, true);
            case RIGHT: return walkRightAnimation.getKeyFrame(stateTime, true);
            default: return standingFrames[facing.ordinal()];
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        // 原有釋放代碼...
        backgroundTexture.dispose();
        dropTexture.dispose();
        dropSound.dispose();
        music.dispose();
        spriteBatch.dispose();

        // 新增釋放動畫資源
        for (TextureRegion frame : standingFrames) {
            if (frame != null) frame.getTexture().dispose();
        }
        disposeAnimation(walkUpAnimation);
        disposeAnimation(walkDownAnimation);
        disposeAnimation(walkLeftAnimation);
        disposeAnimation(walkRightAnimation);
    }

    private void disposeAnimation(Animation<TextureRegion> animation) {
        if (animation != null) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                if (frame != null && frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }
    }


}

