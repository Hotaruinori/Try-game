package com.hotaruinori.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelUpMenu {
    private Stage stage;
    private boolean visible = false;
    private Skin skin;
    private BitmapFont font;
    private Table table;
    private Projectiles projectiles;

    private static final float DAMAGE_INCREMENT = 5.0f;
    private static final float SPEED_INCREMENT = 1.0f;
    private static final float SIZE_INCREMENT = 0.1f;
    private static final float RATE_INCREMENT = 0.05f;

    private final List<String> allUpgrades = Arrays.asList(
        "Increase Damage", "Increase Attack Speed", "Increase Projectile Speed", "Increase Projectile Size", "Increase Projectile Number"
    );

    /**
     * 建構子，必須傳入 Projectiles 實例。
     */
    public LevelUpMenu(Projectiles projectiles) {
        this.projectiles = projectiles;

        stage = new Stage(new ScreenViewport());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/myfont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        font = generator.generateFont(parameter);
        generator.dispose();

        skin = new Skin();

        // === 建立半透明背景 Drawable ===
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.5f); // 半透明黑色
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose(); // 釋放 Pixmap

        NinePatch ninePatch = new NinePatch(texture, 0, 0, 0, 0);
        Drawable background = new NinePatchDrawable(ninePatch);

        // === 建立按鈕樣式 ===
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = background; // 設定背景樣式
        skin.add("default", buttonStyle);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
    }

    /**
     * 顯示升級選單
     */
    public void show() {
        if (projectiles == null) {
            System.err.println("請先設定 Projectiles 物件！");
            return;
        }

        visible = true;
        Gdx.input.setInputProcessor(stage);
        table.clear();

        Collections.shuffle(allUpgrades);
        for (int i = 0; i < 3; i++) {
            String upgrade = allUpgrades.get(i);
            TextButton button = new TextButton(upgrade, skin);
            button.pad(20);

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    applyUpgrade(upgrade);
                    hide();
                }
            });

            table.add(button).pad(20).row();
        }
    }

    private void applyUpgrade(String upgrade) {
        switch (upgrade) {
            case "Increase Damage":
                float newDamage = projectiles.getProjectileDamage() + DAMAGE_INCREMENT;
                projectiles.setProjectileDamage(newDamage);
                break;
            case "Increase Attack Speed":
                float newRate = projectiles.getSpawnInterval() - RATE_INCREMENT;
                projectiles.setSpawnInterval(newRate);
                break;
            case "Increase Projectile Speed":
                float newSpeed = projectiles.getProjectileSpeed() + SPEED_INCREMENT;
                projectiles.setProjectileSpeed(newSpeed);
                break;
            case "Increase Projectile Size":
                float newSize = projectiles.getProjectileWidth() + SIZE_INCREMENT;
                projectiles.setProjectileSize(newSize);
                break;
            case "Increase Projectile Number":
                int newCount = projectiles.getProjectileCount() + 1;
                projectiles.setProjectileCount(newCount);
                break;
        }
    }

    public void hide() {
        visible = false;
        Gdx.input.setInputProcessor(null);
    }

    public void render() {
        if (!visible) return;
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        font.dispose();
        skin.dispose();
    }

    public boolean isVisible() {
        return visible;
    }
}
