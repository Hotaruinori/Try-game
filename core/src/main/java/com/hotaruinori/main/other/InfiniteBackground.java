package com.hotaruinori.main.other;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.*;

public class InfiniteBackground {
    private Texture texture; // 背景地磚材質
    private float tileWidth = 10f;
    private float tileHeight = 10f;

    private List<Sprite> backgroundObjects = new ArrayList<>(); // 所有物件實體
    private List<Rectangle> blockingObjectsBounds = new ArrayList<>(); // 所有阻擋碰撞的邊界框

    // 定義可用的物件類型（含圖片與是否阻擋）
    static class BackgroundObjectType {
        public final String name;
        Array<Texture> textures;
        boolean isBlocking;
        boolean allowRandomSize;  // ➕ 是否允許隨機大小
        boolean allowRotation;    // ➕ 是否允許旋轉
        float density;       // ➕ 每個單位面積的物件密度（例如 0.2 表示每 1 單位面積期望 0.2 個）
        float probability;   // ➕ 每次嘗試時的物件生成機率（例如 0.6 表示 60% 機率會放）

        BackgroundObjectType(String name, String[] texturePaths, boolean isBlocking,
                             boolean allowRandomSize, boolean allowRotation, float density, float probability) {
            this.name = name;
            this.textures = new Array<>();
            for (String path : texturePaths) {
                this.textures.add(new Texture(path));
            }
            this.isBlocking = isBlocking;
            this.allowRandomSize = allowRandomSize;
            this.allowRotation = allowRotation;
            this.density = density;
            this.probability = probability;
        }
        // 隨機取得一張圖片
        public Texture getRandomTexture() {
            return textures.random();
        }
    }

    private List<BackgroundObjectType> objectTypes = new ArrayList<>(); // 所有可生成的物件種類

    public InfiniteBackground(String tileTexturePath) {
        texture = new Texture(tileTexturePath); // 載入地磚圖片

        // ➕ 將各種背景物件加入objectTypes，可指定圖片、是否阻擋人物，是否隨機大小，是否旋轉、物件密度、生成機率
        // new String[]{"box.png", "house2.png"} 第二個參數用這樣去放，目的是可以使用多圖片做隨機功能
        objectTypes.add(new BackgroundObjectType("house",new String[]{"box.png","Piercing_Ring.png","Air_Cannon.png","Small_Light.png","Big_Light.png","Time_Scarf.png"}, true, true, true,0.1f, 0.7f));       // 樹，阻擋
        objectTypes.add(new BackgroundObjectType("fence",new String[]{"rock_hat.png"}, true,false, false,0.5f, 0.7f));  // 石頭，阻擋
        objectTypes.add(new BackgroundObjectType("box",new String[]{"Dog1.png","Dog2.png"}, true, true, true,0.1f, 0.7f));       // 樹，阻擋
        objectTypes.add(new BackgroundObjectType("rock_hat",new String[]{"Emotional_Band.png"}, true,false, false,0.5f, 0.7f));  // 石頭，阻擋
        objectTypes.add(new BackgroundObjectType("bucket",new String[]{"Welcome_Cat.png"}, false, true, true,0.05f, 0.5f));   // 草叢，不阻擋
        objectTypes.add(new BackgroundObjectType("bucket2",new String[]{"Mini_Dora.png"}, false, true, true,0.05f, 0.5f));   // 招牌，不阻擋
    }

    // ✅（新增）用來標記每個區塊的座標 key(ChunkKey)，給後面的generateChunksAround方法使用。
    // <不是 LibGDX 內建的東西> Java 的 HashMap 和 HashSet 這種集合，是用 雜湊值 (hashCode) + 比較 (equals) 來判斷兩個物件是否一樣的。
    //當你把一個物件當作 Map 的 key（像 Map<ChunkKey, ChunkData>）時：
    //hashCode()：決定這個 key 落在哪個 「桶子」。
    //equals()：當兩個物件的 hash 相同，會再用 .equals() 去確認是不是「真的一樣」。
    //如果沒有實作這兩個方法，Java 只會檢查「是不是同一個物件」，而不是「值是否一樣」
    //在 Java 中，所有類別都（直接或間接）繼承自 java.lang.Object。
    // 預設 .equals()：只會比是不是「同一個記憶體位置」; 預設 .hashCode()：回傳記憶體位置的雜湊值（差不多是地址）
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
            return this.chunkX == other.chunkX && this.chunkY == other.chunkY;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkX, chunkY);
        }
    }

    // ✅（新增）記錄每個區塊已生成的物件（Sprite 與 blocking 判定）
    static class ChunkData {
        List<Sprite> objects = new ArrayList<>();
        List<Rectangle> blockingBounds = new ArrayList<>();
    }

    // ✅（新增）記憶系統，用來記住哪些區塊生成過
    private Map<ChunkKey, ChunkData> generatedChunks = new HashMap<>();

    // ✅（新增）區塊大小設定為 10x10 單位
    public static final int CHUNK_SIZE = 10;

    // ✅（新增）generateChunksAround() 方法是「無限地圖背景生成系統」的核心之一，
    // 它會根據角色的位置來決定要載入哪些區塊（chunk），並且自動產生或取出記憶中的區塊資料，將對應的物件加入渲染與碰撞清單中。
    // Vector2 center: 表示角色目前的位置（世界座標）;int rangeInChunks: 表示以角色為中心，要載入幾個區塊的範圍，例如 1 就是 3x3（中心 + 上下左右各 1）。
    public void generateChunksAround(Vector2 center, int rangeInChunks) {
        //根據角色目前的位置，計算出它目前所在的「區塊座標」例如角色在 (25, 37)，若 CHUNK_SIZE = 10，則會對應到區塊 (2, 3)
        int centerChunkX = (int) Math.floor(center.x / CHUNK_SIZE);
        int centerChunkY = (int) Math.floor(center.y / CHUNK_SIZE);
        //在重新產生區塊前，先清除畫面上原本的物件與碰撞資料
        backgroundObjects.clear();        // ⚠️ 注意這裡是合併所有 chunk 的物件進背景
        blockingObjectsBounds.clear();
        //遍歷周圍的所有區塊（以角色為中心）
        for (int dx = -rangeInChunks; dx <= rangeInChunks; dx++) {
            for (int dy = -rangeInChunks; dy <= rangeInChunks; dy++) {
                //計算目前區塊的座標與對應 key，chunkX 與 chunkY 是這次要處理的實際區塊座標
                int chunkX = centerChunkX + dx;
                int chunkY = centerChunkY + dy;
                //ChunkKey 是自定的 key 類別，用來作為 Map 裡 generatedChunks 的 key（要實作 .equals() 和 .hashCode()）。
                ChunkKey key = new ChunkKey(chunkX, chunkY);
                //嘗試從記憶體取出區塊資料，若無則新生成
                ChunkData data = generatedChunks.get(key);
                if (data == null) {
                    data = generateChunk(chunkX, chunkY);
                    generatedChunks.put(key, data);
                }
                // 將此 chunk 區塊中的物件加入目前渲染與碰撞清單
                backgroundObjects.addAll(data.objects);
                blockingObjectsBounds.addAll(data.blockingBounds);
            }
        }
    }

    // ✅（新增）實際生成一個區塊的地圖物件，關鍵
    private ChunkData generateChunk(int chunkX, int chunkY) {
        //ChunkData 是一個自訂類別，用來儲存這個區塊的所有物件（Sprite）和碰撞阻擋邊界（Rectangle）
        ChunkData data = new ChunkData();
        //載入特殊生成規則，方法目前有效，但還需要調整
        TownTemplate.tryGenerateTown(chunkX, chunkY, this, data);
        //chunkOriginX 和 chunkOriginY 計算出這個區塊在世界地圖中的左下角絕對座標。
        //例如：第 (2, 3) 區塊的左下角就是 (20, 30)（如果 CHUNK_SIZE = 10）
        float chunkOriginX = chunkX * CHUNK_SIZE;
        float chunkOriginY = chunkY * CHUNK_SIZE;

        //針對每一種物件類型開始嘗試生成，每種物件都可以有自己的密度、機率、尺寸、旋轉等設定
        for (BackgroundObjectType type : objectTypes) {
            // 跳過房子和圍欄，因為已經由 TownTemplate 生成，不用重複生成
            if (type.name.equals("house") || type.name.equals("fence")) continue;
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
                Sprite obj = new Sprite(type.getRandomTexture());

                // 依照是否允許隨機大小設定尺寸，並設定預設值
                float width = type.allowRandomSize ? MathUtils.random(0.4f, 0.8f) : 0.6f;
                float height = type.allowRandomSize ? MathUtils.random(0.4f, 0.8f) : 0.6f;

                // 計算物件矩形範圍，用於碰撞檢查
                Rectangle newObjRect = new Rectangle(x - width / 2, y - height / 2, width, height);
                // **檢查新物件是否和已存在的阻擋區域（尤其是房子與圍欄）重疊，若重疊則跳過本次生成**
                boolean overlaps = false;
                // 新增：避免生成在起始點附近（假設起始點在 0,0）
                float distance = Vector2.dst(newObjRect.x, newObjRect.y, 0, 0);
                for (Rectangle blockRect : data.blockingBounds) { //對 data.blockingBounds 裡的每個 Rectangle（矩形）都執行一次
                    if (blockRect.overlaps(newObjRect)) {
                        overlaps = true;
                        break;
                    }
                }
                if (overlaps || distance < 2f) {
                    // 跳過這次生成，避免物件生成於房子或圍欄區域
                    continue;
                }
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

    //處理人物是否會被地圖生成物件給阻擋
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

    public List<BackgroundObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void dispose() {
        texture.dispose();
        for (BackgroundObjectType type : objectTypes) {
            type.getRandomTexture().dispose();
        }
    }
}
