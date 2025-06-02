package com.hotaruinori.lwjgl3;

import com.hotaruinori.lwjgl3.Attack.Missile;
import com.hotaruinori.lwjgl3.Attack.MissileManager; // <--- 新增引入
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2; // 確保有這個 import
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;  // 碰撞判定用

public class BossA {
    private Sprite bossSprite;
    private Character character;
    private MonsterAI monsterAI;
    private MissileManager missileManager; // <--- 新增 MissileManager 引用

    private float monsterWidth = 2f; //怪物的寬
    private float monsterHeight = 2f; //怪物的高
    private float posX = 0.5f; //怪物的起始位置
    private float posY = 0.5f; //怪物的起始位置
    private float moveSpeed = 2f; //怪物的移動速度 //
    private float attackDistanceThreshold = 1f; // 怪物停止追蹤的距離，可以設小一點讓它更靠近
    private float attackRange = 10f; // 怪物的攻擊範圍不可小於技能範圍
    private float attackDamage = 10f; //怪物的攻擊傷害

    //圖片之後新增
    private String Boss_image ="monsters/boss1.png";


    // --- 新增 HP 相關變數 ---
    private float maxHealth;
    private float currentHealth;
    private boolean isAlive; // 怪物是否存活


    // 可以新增一個引用來管理飛彈列表，飛彈由 BossA 產生
    private Array<Missile> activeMissiles;



    public BossA() {
        bossSprite = new Sprite(new Texture(Boss_image));

        this.maxHealth = 1000f; // 設定 Boss 的最大生命值
        this.currentHealth = maxHealth; // 初始生命值等於最大生命值
        this.isAlive = true; // 初始為存活狀態

    }

    public void setPlayer(Character character) {
        this.character = character;
        this.monsterAI = new MonsterAI(this, character,
            moveSpeed,
            attackDistanceThreshold,
            attackRange,
            attackDamage);
    }



    // 新增方法供 MonsterAI 呼叫
    public void playLaserPrepareAnimation() {
        // 播放attackRange準備動畫或顯示瞄準線
        System.out.println("BossA: 播放雷射準備動畫");
    }

    public void playLaserAttackEffect() {
        // 播放雷射光束效果，並處理雷射的實際視覺呈現
        System.out.println("BossA: 繪製雷射光束");
    }

    // <--- 新增這個方法，讓 Maingame 可以設定 MissileManager
    public void setMissileManager(MissileManager manager) {
        this.missileManager = manager;
    }

    // 新增方法供 MonsterAI 呼叫以生成飛彈
    public void spawnMissile(float speed, float damage) {
        if (missileManager == null) {
            System.err.println("Error: MissileManager not set in BossA!");
            return;
        }
        missileManager.addMissile(getCenterPosition().x, getCenterPosition().y, speed, damage);
        System.out.println("BossA: 生成飛彈，目標玩家 (透過 MissileManager)");
    }

    // 新增一個方法來生成扇形飛彈
    public void spawnSpreadMissiles(int numberOfMissiles, float spreadAngle, float missileSpeed, float missileDamage) {
        if (missileManager == null) {
            System.err.println("Error: MissileManager not set in BossA!");
            return;
        }
        missileManager.addSpreadMissiles(
            getCenterPosition().x,
            getCenterPosition().y,
            numberOfMissiles,
            spreadAngle,
            missileSpeed,
            missileDamage
        );
        System.out.println("BossA: 生成 " + numberOfMissiles + " 枚扇形飛彈 (透過 MissileManager)。");
    }


    public void playChargePrepareAnimation() {
        // 播放衝撞準備動畫
        System.out.println("BossA: 播放衝撞準備動畫");
    }

    public void playChargeImpactEffect() {
        // 播放衝撞擊中效果
        System.out.println("BossA: 播放衝撞擊中效果");
    }

    // 新增這個方法，讓遊戲迴圈可以更新怪物的 AI 行為
    public MonsterAI getMonsterAI() {
        return monsterAI;
    }

    public void render(SpriteBatch batch) {

        if (isAlive) { // 只有活著的 Boss 才繪製
            batch.draw(bossSprite, posX, posY, monsterWidth, monsterHeight);
            // TODO: 可以考慮在這裡繪製 Boss 的血條
        }

    }
    // 怪物的碰撞判定用
    public Rectangle BossA_Rectangle() {
        return new Rectangle(posX+1/4f, posY+1/4f, monsterWidth*3/4f, monsterHeight*3/4f);
    }
    // --- 新增 Boss 受傷方法 ---
    public void takeDamage(float damageAmount) {
        if (!isAlive) { // 如果已經死亡，不再受傷
            return;
        }
        currentHealth -= damageAmount;
        System.out.println("Boss 受到 " + damageAmount + " 點傷害！當前 HP: " + currentHealth);

        if (currentHealth <= 0) {
            currentHealth = 0; // 確保 HP 不會變成負值
            isAlive = false;
            System.out.println("Boss 已被擊敗！");
            // TODO: 播放死亡動畫、掉落物品、遊戲勝利等邏輯
        }
    }
    // --- 新增獲取 HP 和存活狀態的方法 ---
    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return isAlive;
    }



    public void dispose() {
        bossSprite.getTexture().dispose();
    }

    public float getX() {
        return posX;
    }

    public float getY() {
        return posY;
    }

    // 新增這些方法，供 MonsterAI 讀取和設定 BossA 的位置
    public Vector2 getCenterPosition() {
        // 假設 bossSprite 繪製時的寬高是 2f，這裡要與 render 裡的數值一致
        return new Vector2(posX + 2f / 2, posY + 2f / 2);
    }

    public void setX(float x) {
        this.posX = x;
    }

    public void setY(float y) {
        this.posY = y;
    }
}

