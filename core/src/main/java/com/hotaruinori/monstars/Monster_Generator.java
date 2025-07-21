package com.hotaruinori.monstars;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.hotaruinori.Plays.Character;
import com.hotaruinori.monstars.BABY.BabyA;
import com.hotaruinori.monstars.BABY.BabyB;
import com.hotaruinori.monstars.BABY.BabyC;

/**
 * 怪物生成器，用來依據規則在邊緣產生怪物，並統一管理與更新怪物行為。
 * 支援多種怪物，各自擁有獨立的生成間距與出現起始時間。
 */
public class Monster_Generator {

    // 怪物生成規則的內部類別：一種怪物對應一種生成規則
    private static class MonsterSpawnRule {
        public float spawnInterval;      // 該怪物生成間距（秒）
        public float startAtTime;        // 幾秒後才開始產生此怪物
        public float timeSinceLastSpawn; // 此怪物的計時器

        public MonsterSpawnRule(float interval, float startAt) {
            this.spawnInterval = interval;
            this.startAtTime = startAt;
            this.timeSinceLastSpawn = 0f;
        }
    }

    private Character character;                    // 傳入主角方便給怪物AI用
    private Camera camera;                          // 攝影機資訊，用來決定生成邊界
    private float gameTime = 0f;                    // 遊戲總經過時間（秒）

    // 每種怪物的生成規則
    private MonsterSpawnRule babyARule = new MonsterSpawnRule(1.0f, 0f);    // BabyA 每 1 秒，從 0 秒開始
    private MonsterSpawnRule babyBRule = new MonsterSpawnRule(5.0f, 20f);   // BabyB 每 5 秒，從 20 秒開始
    private MonsterSpawnRule babyCRule = new MonsterSpawnRule(8.0f, 40f);   // BabyC 每 8 秒，從 40 秒開始

    // 各怪物類型分開儲存（方便管理與更新）
    private Array<BabyA> babyAList = new Array<>();
    private Array<BabyB> babyBList = new Array<>();
    private Array<BabyC> babyCList = new Array<>();

    public Monster_Generator(Character character, Camera camera) {
        this.character = character;
        this.camera = camera;
    }

    /**
     * 每幀更新所有怪物生成與行為邏輯
     */
    public void update(float deltaTime) {
        gameTime += deltaTime;

        // === BabyA ===
        babyARule.timeSinceLastSpawn += deltaTime;
        if (gameTime >= babyARule.startAtTime && babyARule.timeSinceLastSpawn >= babyARule.spawnInterval) {
            spawnBabyA();
            babyARule.timeSinceLastSpawn = 0f;
        }
        for (BabyA baby : babyAList) {
            if (baby.isAlive()) baby.update(deltaTime);
        }

        // === BabyB ===
        babyBRule.timeSinceLastSpawn += deltaTime;
        if (gameTime >= babyBRule.startAtTime && babyBRule.timeSinceLastSpawn >= babyBRule.spawnInterval) {
            spawnBabyB();
            babyBRule.timeSinceLastSpawn = 0f;
        }
        for (BabyB baby : babyBList) {
            if (baby.isAlive()) baby.update(deltaTime);
        }

        // === BabyC ===
        babyCRule.timeSinceLastSpawn += deltaTime;
        if (gameTime >= babyCRule.startAtTime && babyCRule.timeSinceLastSpawn >= babyCRule.spawnInterval) {
            spawnBabyC();
            babyCRule.timeSinceLastSpawn = 0f;
        }
        for (BabyC baby : babyCList) {
            if (baby.isAlive()) baby.update(deltaTime);
        }
    }

    /**
     * 渲染所有怪物
     */
    public void render(SpriteBatch batch) {
        for (BabyA baby : babyAList) {
            if (baby.isAlive()) baby.render(batch);
        }
        for (BabyB baby : babyBList) {
            if (baby.isAlive()) baby.render(batch);
        }
        for (BabyC baby : babyCList) {
            if (baby.isAlive()) baby.render(batch);
        }
    }

    /**
     * 清除所有已死亡怪物（節省記憶體）
     */
    public void removeDeadMonsters() {
        for (int i = babyAList.size - 1; i >= 0; i--) {
            if (!babyAList.get(i).isAlive()) babyAList.removeIndex(i);
        }
        for (int i = babyBList.size - 1; i >= 0; i--) {
            if (!babyBList.get(i).isAlive()) babyBList.removeIndex(i);
        }
        for (int i = babyCList.size - 1; i >= 0; i--) {
            if (!babyCList.get(i).isAlive()) babyCList.removeIndex(i);
        }
    }

    /**
     * 釋放怪物資源
     */
    public void dispose() {
        for (BabyA baby : babyAList) baby.dispose();
        for (BabyB baby : babyBList) baby.dispose();
        for (BabyC baby : babyCList) baby.dispose();
    }

    // === 以下為各怪物生成方法 ===

    private void spawnBabyA() {
        float[] pos = getRandomEdgePosition();
        BabyA baby = new BabyA();
        baby.setPlayer(character);
        baby.setX(pos[0]);
        baby.setY(pos[1]);
        babyAList.add(baby);
        System.out.println("生成 BabyA 於 (" + pos[0] + ", " + pos[1] + ")");
    }

    private void spawnBabyB() {
        float[] pos = getRandomEdgePosition();
        BabyB baby = new BabyB();
        baby.setPlayer(character);
        baby.setX(pos[0]);
        baby.setY(pos[1]);
        babyBList.add(baby);
        System.out.println("生成 BabyB 於 (" + pos[0] + ", " + pos[1] + ")");
    }

    private void spawnBabyC() {
        float[] pos = getRandomEdgePosition();
        BabyC baby = new BabyC();
        baby.setPlayer(character);
        baby.setX(pos[0]);
        baby.setY(pos[1]);
        babyCList.add(baby);
        System.out.println("生成 BabyC 於 (" + pos[0] + ", " + pos[1] + ")");
    }

    /**
     * 從攝影機畫面邊緣隨機產生一個生成座標
     * @return float[2]，分別為 x 和 y
     */
    private float[] getRandomEdgePosition() {
        float camLeft = camera.position.x - camera.viewportWidth / 2f;
        float camRight = camera.position.x + camera.viewportWidth / 2f;
        float camBottom = camera.position.y - camera.viewportHeight / 2f;
        float camTop = camera.position.y + camera.viewportHeight / 2f;

        int edge = MathUtils.random(3);
        float x = 0, y = 0;

        switch (edge) {
            case 0: x = camLeft;  y = MathUtils.random(camBottom, camTop); break;
            case 1: x = camRight; y = MathUtils.random(camBottom, camTop); break;
            case 2: x = MathUtils.random(camLeft, camRight); y = camTop; break;
            case 3: x = MathUtils.random(camLeft, camRight); y = camBottom; break;
        }

        return new float[] { x, y };
    }
    /**
     * 給Porjectile用的判斷是否擊中怪物
     * return 怪物對應的怪物列表
     */
    public Array<BabyA> getBabyA() {
        return babyAList;
    }
    public Array<BabyB> getBabyB() {
        return babyBList;
    }
    public Array<BabyC> getBabyC() {
        return babyCList;
    }
}
