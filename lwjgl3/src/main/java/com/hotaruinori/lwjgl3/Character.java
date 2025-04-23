package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Character {
    // 角色狀態枚舉
    public enum State {
        STANDING, WALKING
    }
    // 角色面對方向枚舉
    public enum FacingDirection {
        UP, DOWN, LEFT, RIGHT
    }

    // 角色屬性
    private Sprite sprite;
    private State state = State.STANDING;
    private FacingDirection facing = FacingDirection.DOWN;
    private float stateTime = 0;

    // 鍵盤移動動畫資源
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private TextureRegion[] standingFrames;

    // 觸碰 or 滑鼠移動用
    private Vector2 targetPosition = null;
    private float moveSpeed = 4f;
    // 添加移動到目標位置的方法
    public void moveTo(float x, float y) {
        if (targetPosition == null) {
            targetPosition = new Vector2();
        }
        targetPosition.set(x, y);
    }
    // 更新移動邏輯
    public void updateMovement(float delta) {
        if (targetPosition != null) {
            // 計算移動方向
            Vector2 direction = new Vector2(
                targetPosition.x - sprite.getX() - sprite.getWidth()/2,
                targetPosition.y - sprite.getY() - sprite.getHeight()/2
            );

            // 如果已經到達目標位置
            if (direction.len() < 0.1f) {
                targetPosition = null;
                return;
            }

            // 標準化方向向量並計算移動量
            direction.nor();
            float moveX = direction.x * moveSpeed * delta;
            float moveY = direction.y * moveSpeed * delta;

            // 移動角色
            sprite.translateX(moveX);
            sprite.translateY(moveY);

            // 根據移動方向設置面向方向
            if (Math.abs(direction.x) > Math.abs(direction.y)) {
                setFacing(direction.x > 0 ? FacingDirection.RIGHT : FacingDirection.LEFT);
            } else {
                setFacing(direction.y > 0 ? FacingDirection.UP : FacingDirection.DOWN);
            }
        }
    }

    public Character() {
        initAnimations();
        // 使用站立動畫的第一幀初始化精靈
        sprite = new Sprite(standingFrames[FacingDirection.DOWN.ordinal()]);
        sprite.setSize(1, 1);
    }

    private void initAnimations() {
        // 加載站立幀紋理
        standingFrames = new TextureRegion[4];
        standingFrames[FacingDirection.UP.ordinal()] = new TextureRegion(new Texture("character_img/dora_walk_back1.png"));
        standingFrames[FacingDirection.DOWN.ordinal()] = new TextureRegion(new Texture("character_img/dora_standing.png"));
        standingFrames[FacingDirection.LEFT.ordinal()] = new TextureRegion(new Texture("character_img/dora_walk_left_1.png"));
        standingFrames[FacingDirection.RIGHT.ordinal()] = new TextureRegion(new Texture("character_img/dora_walk_right_1.png"));

        // 初始化走路動畫
        walkUpAnimation = new Animation<>(0.15f,
            new TextureRegion(new Texture("character_img/dora_walk_back1.png")),
            new TextureRegion(new Texture("character_img/dora_walk_back2.png"))
        );
        walkDownAnimation = new Animation<>(0.15f,
            new TextureRegion(new Texture("character_img/dora_standing.png")),
            new TextureRegion(new Texture("character_img/dora_standing.png"))
        );
        walkLeftAnimation = new Animation<>(0.15f,
            new TextureRegion(new Texture("character_img/dora_walk_left_1.png")),
            new TextureRegion(new Texture("character_img/dora_walk_left_2.png"))
        );
        walkRightAnimation = new Animation<>(0.15f,
            new TextureRegion(new Texture("character_img/dora_walk_right_1.png")),
            new TextureRegion(new Texture("character_img/dora_walk_right_2.png"))
        );
    }

    public void update(float delta, boolean isMoving) {
        if (isMoving) {
            state = State.WALKING;
            stateTime += delta;
            sprite.setRegion(getCurrentAnimationFrame());
        } else {
            state = State.STANDING;
            stateTime = 0;
            sprite.setRegion(standingFrames[facing.ordinal()]);
        }
    }

    private TextureRegion getCurrentAnimationFrame() {
        switch (facing) {
            case UP: return walkUpAnimation.getKeyFrame(stateTime, true);
            case DOWN: return walkDownAnimation.getKeyFrame(stateTime, true);
            case LEFT: return walkLeftAnimation.getKeyFrame(stateTime, true);
            case RIGHT: return walkRightAnimation.getKeyFrame(stateTime, true);
            default: return standingFrames[facing.ordinal()];
        }
    }

    // 移動方法
    public void move(float deltaX, float deltaY) {
        sprite.translateX(deltaX);
        sprite.translateY(deltaY);
    }

    // 設置面向方向
    public void setFacing(FacingDirection direction) {
        this.facing = direction;
    }

    // 獲取精靈用於繪製
    public Sprite getSprite() {
        return sprite;
    }

    // 獲取位置和大小用於碰撞檢測
    public float getX() {
        return sprite.getX();
    }

    public float getY() {
        return sprite.getY();
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

    public void dispose() {
        // 釋放所有紋理資源
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
