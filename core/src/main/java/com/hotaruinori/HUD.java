package com.hotaruinori;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * HUD 類別：負責顯示畫面上方的資訊（例如經驗條與武器圖示）
 */
public class HUD {

    private ShapeRenderer shapeRenderer; // 畫條形圖用（血條、經驗條）
    private BitmapFont font;             // 用來顯示文字
    private Texture weaponIcon;          // 武器圖示

    private float currentExp;            // 目前經驗值
    private float maxExp;                // 升級所需最大經驗值
    private float currentLevel;          // 目前等級

    private OrthographicCamera hudCamera;  // 專屬 HUD 的攝影機
    private Viewport hudViewport;          // HUD 的 viewport（使用像素座標）

    public HUD() {
        // 初始化 HUD 專用攝影機與 viewport（以螢幕像素為單位）
        hudCamera = new OrthographicCamera();
        hudViewport = new ScreenViewport(hudCamera); // 自動匹配螢幕像素
        hudViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // 初始化 ShapeRenderer 與投影設定
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        // 建立 FreeType 字型（支援 .ttf 向量字型）
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/myfont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;                  // 字型大小
        parameter.color = Color.WHITE;        // 主文字顏色
        parameter.borderColor = Color.BLACK;  // 外框顏色
        parameter.borderWidth = 1f;           // 外框寬度
        font = generator.generateFont(parameter);
        generator.dispose(); // 釋放產生器資源

        // 預設經驗值
        currentExp = 0;
        maxExp = 100;

        // 載入預設武器圖示
        weaponIcon = new Texture(Gdx.files.internal("weapon/Air_Cannon.png"));
    }

    /**
     * 每次視窗縮放時呼叫，更新 HUD 視角大小
     */
    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
    }

    /**
     * 更新經驗值資訊
     */
    public void setExp(int current, int max, int level) {
        this.currentExp = current;
        this.maxExp = max;
        this.currentLevel = level;
    }

    /**
     * 更新武器圖示（切換武器時使用）
     */
    public void setWeaponIcon(Texture newIcon) {
        if (weaponIcon != null) weaponIcon.dispose(); // 釋放舊圖示資源
        weaponIcon = newIcon;
    }

    /**
     * 繪製 HUD（必須在 spriteBatch.end() 之後呼叫）
     */
    public void render(SpriteBatch batch) {
        // 更新攝影機，確保是正確尺寸
        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);

        // --- 畫經驗條背景與前景 ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 背景條（灰色）
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(10, 10, 200, 20);

        // 前景條（根據經驗比率）
        float expRatio = Math.min(currentExp / maxExp, 1f);
        shapeRenderer.setColor(Color.SKY);
        shapeRenderer.rect(10, 10, 200 * expRatio, 20);

        shapeRenderer.end();

        // --- 畫圖示與文字 ---
        batch.begin();
        // 等級文字（顯示在經驗條上方）
        font.draw(batch, "Lv. " + currentLevel, 10, 70); // 經驗條 y=10 + 高20 + 緩衝40 = 70
        // 經驗值文字
        font.draw(batch, (int) currentExp + " / " + (int) maxExp, 10, 45);

        // 武器圖示與說明文字
        batch.draw(weaponIcon, 230, 10, 32, 32);                          // 圖示
        font.draw(batch, "武器", 230, 55, 32, Align.center, false);      // 武器文字（置中）

        batch.end();
    }

    /**
     * 釋放資源，離開遊戲時使用
     */
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        if (weaponIcon != null) weaponIcon.dispose();
    }
}
