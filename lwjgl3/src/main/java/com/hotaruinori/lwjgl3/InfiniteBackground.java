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
    private float tileWidth = 10f;
    private float tileHeight = 10f;

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

    // ✅（新增）用來標記每個區塊的座標 key
    private static class ChunkKey {
        int chunkX, chunkY;

        ChunkKey(int chunkX, int chunkY) {
            this.chunkX = chunkX;
            this.chunkY = chunkY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey other = (ChunkKey) o;
            return chunkX == other.chunkX && chunkY == other.chunkY;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkX, chunkY);
        }
    }

    // ✅（新增）記錄每個區塊已生成的物件（Sprite 與 blocking 判定）
    private static class ChunkData {
        List<Sprite> objects = new ArrayList<>();
        List<Rectangle> blockingBounds = new ArrayList<>();
    }

    // ✅（新增）記憶系統，用來記住哪些區塊生成過
    private Map<ChunkKey, ChunkData> generatedChunks = new HashMap<>();

    // ✅（新增）區塊大小設定為 10x10 單位
    private final int CHUNK_SIZE = 10;

    // ✅（新增）生成角色周圍的所有區塊（範圍可設定）
    public void generateChunksAround(Vector2 center, int rangeInChunks) {
        int centerChunkX = (int) Math.floor(center.x / CHUNK_SIZE);
        int centerChunkY = (int) Math.floor(center.y / CHUNK_SIZE);

        backgroundObjects.clear();        // ⚠️ 注意這裡是合併所有 chunk 的物件進背景
        blockingObjectsBounds.clear();

        for (int dx = -rangeInChunks; dx <= rangeInChunks; dx++) {
            for (int dy = -rangeInChunks; dy <= rangeInChunks; dy++) {
                int chunkX = centerChunkX + dx;
                int chunkY = centerChunkY + dy;
                ChunkKey key = new ChunkKey(chunkX, chunkY);

                ChunkData data = generatedChunks.get(key);
                if (data == null) {
                    data = generateChunk(chunkX, chunkY);
                    generatedChunks.put(key, data);
                }

                // 將此 chunk 的物件加入渲染與碰撞列表
                backgroundObjects.addAll(data.objects);
                blockingObjectsBounds.addAll(data.blockingBounds);
            }
        }
    }

    // ✅（新增）實際生成一個區塊的物件
    private ChunkData generateChunk(int chunkX, int chunkY) {
        ChunkData data = new ChunkData();

        int objectCount = 8 + MathUtils.random(5);  // 每個區塊可以自訂密度

        for (int i = 0; i < objectCount; i++) {
            float x = chunkX * CHUNK_SIZE + MathUtils.random(0f, CHUNK_SIZE);
            float y = chunkY * CHUNK_SIZE + MathUtils.random(0f, CHUNK_SIZE);

            BackgroundObjectType type = objectTypes.get(MathUtils.random(objectTypes.size() - 1));
            Sprite obj = new Sprite(type.texture);

            float width = MathUtils.random(0.4f, 0.8f);
            float height = MathUtils.random(0.4f, 0.8f);

            obj.setSize(width, height);
            obj.setOriginCenter();
            obj.setPosition(x - width / 2, y - height / 2);
            obj.setRotation(MathUtils.random(0, 360));

            data.objects.add(obj);

            if (type.isBlocking) {
                data.blockingBounds.add(new Rectangle(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight()));
            }
        }

        return data;
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
