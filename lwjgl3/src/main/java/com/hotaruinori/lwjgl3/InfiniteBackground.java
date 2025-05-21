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
        boolean allowRandomSize;  // ➕ 是否允許隨機大小
        boolean allowRotation;    // ➕ 是否允許旋轉
        float density;       // ➕ 每個單位面積的物件密度（例如 0.2 表示每 1 單位面積期望 0.2 個）
        float probability;   // ➕ 每次嘗試時的物件生成機率（例如 0.6 表示 60% 機率會放）

        BackgroundObjectType(String texturePath, boolean isBlocking,
                             boolean allowRandomSize, boolean allowRotation, float density, float probability) {
            this.texture = new Texture(texturePath);
            this.isBlocking = isBlocking;
            this.allowRandomSize = allowRandomSize;
            this.allowRotation = allowRotation;
            this.density = density;
            this.probability = probability;
        }
    }

    private List<BackgroundObjectType> objectTypes = new ArrayList<>(); // 所有可生成的物件種類

    public InfiniteBackground(String tileTexturePath) {
        texture = new Texture(tileTexturePath); // 載入地磚圖片

        // ➕ 將各種背景物件加入objectTypes，可指定圖片、是否阻擋人物，是否隨機大小，是否旋轉、物件密度、生成機率
        objectTypes.add(new BackgroundObjectType("box.png", true, true, true,0.1f, 0.7f));       // 樹，阻擋
        objectTypes.add(new BackgroundObjectType("rock_hat.png", true,false, false,0.5f, 0.7f));  // 石頭，阻擋
        objectTypes.add(new BackgroundObjectType("bucket.png", false, true, true,0.05f, 0.5f));   // 草叢，不阻擋
        objectTypes.add(new BackgroundObjectType("bucket.png", false, true, true,0.05f, 0.5f));   // 招牌，不阻擋
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

    // ✅（新增）實際生成一個區塊的地圖物件，關鍵
    private ChunkData generateChunk(int chunkX, int chunkY) {
        //ChunkData 是一個自訂類別，用來儲存這個區塊的所有物件（Sprite）和碰撞阻擋邊界（Rectangle）
        ChunkData data = new ChunkData();
        //chunkOriginX 和 chunkOriginY 計算出這個區塊在世界地圖中的左下角絕對座標。
        //例如：第 (2, 3) 區塊的左下角就是 (20, 30)（如果 CHUNK_SIZE = 10）
        float chunkOriginX = chunkX * CHUNK_SIZE;
        float chunkOriginY = chunkY * CHUNK_SIZE;

        //針對每一種物件類型開始嘗試生成，每種物件都可以有自己的密度、機率、尺寸、旋轉等設定
        for (BackgroundObjectType type : objectTypes) {
            // 密度 density代表「這種類型物件在區塊中的預期密度」，這邊的參數決定該物件類型要嘗試生成幾次。
            //這裡用 *10 代表這種物件類型的嘗試次數上限（根據實測再調整這個數字）。
            //例如：density = 0.3，那就是 0.3 * 10 = 3，最多會嘗試生成 3 個該類型物件。
            int maxCount = MathUtils.ceil(type.density * 10);
            //機率 probability決定是否真正生成該物件
            for (int i = 0; i < maxCount; i++) {
                //如果隨機值random()超過機率probability，跳過這次生成。
                // MathUtils.random() 會生成一個 0~1 之間的亂數。type.probability 是一個 0~1 的機率值。
                if (MathUtils.random() > type.probability) continue;

                // 在整個區塊中亂數產生一個位置。
                // 假設 chunk 是 10x10 單位，那這個位置就會在 (chunkX*10 ~ chunkX*10+10) 和 (chunkY*10 ~ chunkY*10+10) 之間
                float x = chunkOriginX + MathUtils.random(0f, CHUNK_SIZE);
                float y = chunkOriginY + MathUtils.random(0f, CHUNK_SIZE);
                // 產生物件
                Sprite obj = new Sprite(type.texture);

                // 依照是否允許隨機大小設定尺寸，並設定預設值
                float width = type.allowRandomSize ? MathUtils.random(0.4f, 0.8f) : 0.6f;
                float height = type.allowRandomSize ? MathUtils.random(0.4f, 0.8f) : 0.6f;
                // 設定物件的實際大小與位置，並把旋轉中心設在正中央。
                obj.setSize(width, height);
                obj.setOriginCenter();
                obj.setPosition(x - width / 2, y - height / 2);

                // 依照是否允許旋轉決定是否隨機旋轉
                if (type.allowRotation) {
                    obj.setRotation(MathUtils.random(0, 360));
                }

                // 加入物件到data(ChunkData)
                data.objects.add(obj);

                // 如果此物件是阻擋型，加入碰撞邊界資料
                if (type.isBlocking) {
                    data.blockingBounds.add(new Rectangle(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight()));
                }
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

    // 取得所有阻擋型物件的 Rectangle 陣列，提供給角色做碰撞檢查用
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
