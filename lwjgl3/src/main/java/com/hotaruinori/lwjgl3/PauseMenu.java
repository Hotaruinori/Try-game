package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class PauseMenu {
    private Stage stage;            //Scene2D 的舞台，負責管理所有 UI actor（元件）。
    private Skin skin;              //UI 的樣式檔（例如按鈕的圖樣、字體等）。
    private boolean visible = false;//記錄目前是否顯示選單。
    private Runnable onResume;      //可傳入的回呼函式，當玩家按下 resume 或 exit 時觸發。
    private Runnable onExit;

    public PauseMenu() {
        stage = new Stage(new ScreenViewport());  //使用 ScreenViewport 來確保 UI 元件尺寸不會被地圖鏡頭影響
        Gdx.input.setInputProcessor(stage);       //把輸入焦點交給 stage，這樣 TextButton 才能收到點擊事件。

        // 載入內建的 uiskin.json，這個檔案包含字體、圖片、顏色、樣式等資訊。這是 LibGDX 標準 UI 樣式。
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // 使用 Table 來排版 UI 元件，並填滿整個stage。
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        // 建立 "Paused" 標籤，放大字體並設為白色。
        Label pausedLabel = new Label("Paused", skin);
        pausedLabel.setFontScale(2);
        pausedLabel.setColor(Color.WHITE);
        //建立「Resume」按鈕，點擊後執行 onResume.run()，會在 main.java 呼叫 hide() 並解除暫停。
        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (onResume != null) onResume.run();
            }
        });
        // 建立「Exit Game」按鈕，點擊後執行 onExit.run()，目前是結束遊戲
        TextButton exitButton = new TextButton("Exit Game", skin);
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (onExit != null) onExit.run();
            }
        });
        // 將三個 UI 元件加入表格並垂直排列。row() 代表換行。數字是UI 元件之間的間距設定
        table.add(pausedLabel).padBottom(40).row();
        table.add(resumeButton).pad(40).row();
        table.add(exitButton).pad(40);
    }

    public void render() {
        // 如果暫停選單沒開啟，就什麼都不畫。
        if (!visible) return;
        // 啟用「混合模式」（Blending），可正確顯示透明的圖形（例如半透明按鈕、陰影、特效等）。
        // 在 UI 中的作用：LibGDX 的 UI 元件（例如 TextButton、Label）常常帶有透明背景或陰影。若沒開啟 GL_BLEND，這些透明效果會出錯或完全不顯示
        Gdx.gl.glEnable(GL20.GL_BLEND);
        //stage.act()：處理動畫與事件（例如按鈕滑鼠移入）
        stage.act(Gdx.graphics.getDeltaTime());
        //stage.draw()
        stage.draw();
    }

    public void show(Runnable onResume, Runnable onExit) {
        this.visible = true;
        this.onResume = onResume;
        this.onExit = onExit;
        Gdx.input.setInputProcessor(stage); // 接收輸入事件
    }

    public void hide() {
        this.visible = false;
        Gdx.input.setInputProcessor(null); // 讓遊戲回到主控制
    }

    public boolean isVisible() {
        return visible;
    }
}
