package com.hotaruinori.lwjgl3;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class BossA {

    private final Texture boss_image;

    //    private final InputPro inputPro = new InputPro(); //建立輸入方法
    private float posX = 0; //起始位置 可修改
    private float posY = 0; //起始位置 可修改
    private float moveSpeed = 2f; //BOSS速度 可修改
    private Character character;


    public BossA() { // 建構子負責初始化
        this.boss_image = new Texture("monsters/boss1.png");

//        Gdx.input.setInputProcessor(inputPro); //初始輸入方法並將這個輸入方法設置為該物件的輸入處理器
    }

    // 提供一個方法來設定要追蹤的玩家物件
    public void setPlayer(Character character) {
        this.character = character;
    }


    public void render(SpriteBatch batch) {
//        inputPro.updateSpeed(); //實現更新位置 如果想要不同移動速度可以在InputPro那邊最底下新增方法 例如updateSpeed2()
//        posX += inputPro.X_SPD * Gdx.graphics.getDeltaTime(); //X的位置
//        posY += inputPro.Y_SPD * Gdx.graphics.getDeltaTime(); //Y的位置
        if (character != null) {
            Vector2 playerPosition = character.getCenterPosition(); //  玩家位置
            float deltaX = playerPosition.x - posX;
            float deltaY = playerPosition.y - posY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > 1f) {    //距離大於1f就會追蹤位置
                float normalizedDirectionX = deltaX / distance;
                float normalizedDirectionY = deltaY / distance;
                float velocityX = normalizedDirectionX * moveSpeed * com.badlogic.gdx.Gdx.graphics.getDeltaTime();
                float velocityY = normalizedDirectionY * moveSpeed * com.badlogic.gdx.Gdx.graphics.getDeltaTime();

                posX += velocityX;
                posY += velocityY;
            }

        }
        batch.draw(boss_image, posX, posY,2f,2f);


    }


    public void dispose() { // 釋放資源的方法
        boss_image.dispose();


    }

    public float getX() {   //取得BOSS的 X 座標
        return posX;
    }

    public float getY() {   //取得BOSS的 Y 座標
        return posY;
    }


}
