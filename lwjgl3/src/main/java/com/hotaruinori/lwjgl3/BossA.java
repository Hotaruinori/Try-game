package com.hotaruinori.lwjgl3;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;

public class BossA {
    //怪物跟玩家
    private Sprite bossSprite;
    private Character character;

    //    private final InputPro inputPro = new InputPro(); //建立輸入方法
    private float posX = 0; //起始位置 可修改
    private float posY = 0; //起始位置 可修改
    private float moveSpeed = 2f; //BOSS速度 可修改

    // 攻擊變數
    private float attackCooldown = 1f; // 攻擊之間的冷卻時間（秒）
    private float timeSinceLastAttack = 1f; // 距離上次攻擊的時間
    private float attackRange = 1.0f; // 怪物可以攻擊的距離範圍
    private float attackDamage = 10.0f; // 怪物造成的傷害值


    public BossA() { // 建構子負責初始化

        bossSprite = new Sprite(new Texture("monsters/boss1.png"));

//      Gdx.input.setInputProcessor(inputPro); //初始輸入方法並將這個輸入方法設置為該物件的輸入處理器
    }

    // 提供一個方法來設定要追蹤的玩家物件
    public void setPlayer(Character character) {
        this.character = character;
    }


    public void render(SpriteBatch batch) {
//        inputPro.updateSpeed(); //實現更新位置 如果想要不同移動速度可以在InputPro那邊最底下新增方法 例如updateSpeed2()
//        posX += inputPro.X_SPD * Gdx.graphics.getDeltaTime(); //X的位置
//        posY += inputPro.Y_SPD * Gdx.graphics.getDeltaTime(); //Y的位置

//        update(Gdx.graphics.getDeltaTime());
        batch.draw(bossSprite, posX, posY,2f,2f);


    }

    public void update(float deltaTime) {
        // 移動向玩家
        if (character != null) {
            // 使用 character.getCenterPosition() 來獲取玩家中心點
            Vector2 playerPosition = character.getCenterPosition();
            float deltaX = playerPosition.x - posX;
            float deltaY = playerPosition.y - posY;

            // 計算怪物中心點到玩家中心點的距離
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // 只有當距離大於一定值時才追蹤移動，避免抖動
            if (distance > 1f) { // 這裡將你原來的 1f 稍微調大，讓怪物不會過度靠近
                float normalizedDirectionX = deltaX / distance;
                float normalizedDirectionY = deltaY / distance;

                // 根據速度和時間差計算移動量
                float velocityX = normalizedDirectionX * moveSpeed * deltaTime;
                float velocityY = normalizedDirectionY * moveSpeed * deltaTime;

                posX += velocityX;
                posY += velocityY;
            }

            // 攻擊邏輯
            timeSinceLastAttack += deltaTime;

            // 判斷怪物與玩家之間的距離是否在攻擊範圍內
            // 這裡的 distance 已經是怪物與玩家中心點的距離
            if (distance <= attackRange && timeSinceLastAttack >= attackCooldown) {
                attack(); // 呼叫攻擊方法
                timeSinceLastAttack = 0; // 重置冷卻時間
            }
        }
    }

    private void attack() {
        if (character != null) {
            System.out.println("怪物攻擊玩家，造成 " + attackDamage + " 點傷害！");
            // 呼叫玩家的 takeDamage 方法來減少生命值
            character.takeDamage(attackDamage);
            // 你也可以在這裡播放攻擊音效或動畫
        }
    }




    public void dispose() { // 釋放資源的方法
        bossSprite.getTexture().dispose();


    }

    public float getX() {   //取得BOSS的 X 座標
        return posX;
    }

    public float getY() {   //取得BOSS的 Y 座標
        return posY;
    }


}
