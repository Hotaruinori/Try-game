package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;



public class BossA {
    float posX = 0.0f;
    float posY = 0.0f;
    private final Texture boss_image;
    private final Sprite sprite;


    public BossA() { // 建構子負責初始化 SpriteBatch 和 Texture
        this.boss_image = new Texture("monsters/boss1.png");
        this.sprite = new Sprite(boss_image);
    }

    public void render(SpriteBatch batch) {
        batch.draw(sprite, posX, posY, 1f, 1f);
    }

    public void dispose() { // 釋放資源的方法
        boss_image.dispose();

    }
}
