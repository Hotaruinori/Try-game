package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.*;

public class InfiniteBackground {
    private Texture texture; // 背景地磚材質
    private float tileWidth = 4f;
    private float tileHeight = 4f;

    private List<Sprite> backgroundObjects = new ArrayList<>(); // 所有物件實體
    private List<Rectangle> blockingObjectsBounds = new ArrayList<>(); // 所有阻擋碰撞的邊界框

    // 定義可用的物件類型（含圖片與是否阻擋）
    private static class BackgroundObjectType {
        Texture texture;
        boolean isBlocking;

        BackgroundObjectType(String texturePath, boolean isBlocking) {
            this.texture = new Texture(texturePath);
            this.isBlocking = isBlocking;
        }
    }

    private List<BackgroundObjectType> objectTypes = new ArrayList<>(); // 所有可生成的物件種類

    public InfiniteBackground(String tileTexturePath) {
        texture = new Texture(tileTexturePath); // 載入地磚圖片

        // ➕ 載入各種背景物件圖片，並指定其是否阻擋
        objectTypes.add(new BackgroundObjectType("box.png", true));       // 樹，阻擋
        objectTypes.add(new BackgroundObjectType("box.png", true));       // 石頭，阻擋
        objectTypes.add(new BackgroundObjectType("bucket.png", false));   // 草叢，不阻擋
        objectTypes.add(new BackgroundObjectType("bucket.png", false));   // 招牌，不阻擋
    }

    public void render(SpriteBatch batch, Vector2 characterCenter, float worldWidth, float worldHeight) {
        int tilesX = (int) Math.ceil(worldWidth / tileWidth) + 2;
        int tilesY = (int) Math.ceil(worldHeight / tileHeight) + 2;

        float offsetX = characterCenter.x % tileWidth;
        float offsetY = characterCenter.y % tileHeight;

        float startX = characterCenter.x - worldWidth / 2 - offsetX - tileWidth;
        float startY = characterCenter.y - worldHeight / 2 - offsetY - tileHeight;

        // 🧱 繪製地磚
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                batch.draw(texture,
                    startX + x * tileWidth,
                    startY + y * tileHeight,
                    tileWidth,
                    tileHeight);
            }
        }

        // 🌳 繪製裝飾物件
        for (Sprite obj : backgroundObjects) {
            obj.draw(batch);
        }
    }

    public void generateRandomObjectsAround(Vector2 center, float radius) {
        backgroundObjects.clear();
        blockingObjectsBounds.clear();

        int objectCount = 30 + MathUtils.random(10); // 總共要生成的數量

        for (int i = 0; i < objectCount; i++) {
            float x = center.x + MathUtils.random(-radius, radius);
            float y = center.y + MathUtils.random(-radius, radius);

            // 🎲 隨機選一種物件種類
            BackgroundObjectType type = objectTypes.get(MathUtils.random(objectTypes.size() - 1));
            Sprite obj = new Sprite(type.texture);

            float width = MathUtils.random(0.4f, 0.8f);
            float height = MathUtils.random(0.4f, 0.8f);

            obj.setSize(width, height);
            obj.setOriginCenter();
            obj.setPosition(x - width / 2, y - height / 2);
            obj.setRotation(MathUtils.random(0, 360));

            backgroundObjects.add(obj);

            // 如果此種類是阻擋型，加入碰撞邊界
            if (type.isBlocking) {
                blockingObjectsBounds.add(new Rectangle(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight()));
            }
        }
    }

    public boolean isBlocked(float x, float y) {
        for (Rectangle bounds : blockingObjectsBounds) {
            if (bounds.contains(x, y)) return true;
        }
        return false;
    }

    // ✅ 新增：取得所有阻擋型物件的 Rectangle 陣列，提供給角色做碰撞檢查用
    public Rectangle[] getBlockingObjects() {
        return blockingObjectsBounds.toArray(new Rectangle[0]);
    }

    public void dispose() {
        texture.dispose();
        for (BackgroundObjectType type : objectTypes) {
            type.texture.dispose();
        }
    }
}
