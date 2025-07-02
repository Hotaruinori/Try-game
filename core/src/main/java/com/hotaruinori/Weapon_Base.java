package com.hotaruinori;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Weapon 抽象類別：所有武器的基礎類別，提供共通欄位與方法。
 * 每種武器都需實作 update 方法，render 與 dispose 可選擇覆寫。
 */
public abstract class Weapon_Base {
    // 可調整參數，先暫訂初始數值到各個Class去設置，圖片與聲音要到 main 去調整
    protected float projectileWidth;     // 投射物寬度
    protected float projectileHeight;    // 投射物高度
    protected float spawnInterval;       // 發射間隔
    protected float projectileSpeed;     // 投射物飛行速度
    protected int projectileCount;       // 每次發射的投射物數量（可自由調整）
    protected float projectileDamage;    // 投射物傷害
    protected float attackDamage;        // 一般攻擊傷害數值（共通屬性）

    //宣告物件用
    protected Texture projectileTexture;  //儲存投射物使用的圖片材質（Texture 是圖片素材的基本單位）
    protected Sound hitSound;             //發射播放的音效
    protected Array<ProjectileInstance> projectiles; // 用來儲存目前場上所有的投射物實體，每個包含 Sprite 與速度向量
    protected float spawnTimer;           //記錄時間累加，用來決定何時產生下一個投射物。
    protected Rectangle projectileRectangle; //暫存矩形，用於未來可能進行的碰撞偵測


    // 提供給main.java呼叫的投射物create的方法，呼叫要有圖片與聲音路徑，功能請參照上方宣告物件。
    public Weapon_Base(String texturePath, String soundPath) {
        this.projectileTexture = new Texture(texturePath);
        this.hitSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        this.projectiles = new Array<>();
        this.projectileRectangle = new Rectangle();
        this.spawnInterval = spawnInterval;
        this.projectileSpeed = projectileSpeed;
        this.spawnTimer = 0;
        this.projectileCount = projectileCount;
    }

    // 用來記錄每個投射物的 Sprite 和移動速度向量
    protected static class ProjectileInstance {
        Sprite sprite;       // 投射物本體(Sprite)
        Vector2 velocity;    // 速度向量（單位：距離/秒）（Vector2）

        ProjectileInstance(Sprite sprite, Vector2 velocity) {
            this.sprite = sprite;
            this.velocity = velocity;
        }
    }

    // 每幀更新邏輯（必須實作）
    public abstract void update(float delta, Rectangle characterRect, Viewport viewport, Character character, BossA boss1, Monster_Generator monsterGenerator);

    // 渲染圖像
    public void render(SpriteBatch batch) {}




}
