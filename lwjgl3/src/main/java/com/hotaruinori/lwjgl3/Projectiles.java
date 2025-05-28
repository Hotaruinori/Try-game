package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Projectiles {
    // 可調整參數，圖片與聲音要到 main 去調整
    float PROJECTILE_WIDTH = 0.5f;  // 投射物寬度
    float PROJECTILE_HEIGHT = 0.5f; // 投射物高度
    float SPAWN_INTERVAL = 0.5f;    // 發射間隔
    float PROJECTILE_SPEED = 4.0f;  // 投射物飛行速度

    //宣告物件
    private Texture projectileTexture;  //儲存投射物使用的圖片材質（Texture 是圖片素材的基本單位）
    private Sound hitSound;             //播放的音效
    private Array<ProjectileInstance> projectiles; // 用來儲存目前場上所有的投射物實體，每個包含 Sprite 與速度向量
    private float spawnTimer;           //記錄時間累加，用來決定何時產生下一個投射物。
    private Rectangle projectileRectangle; //暫存矩形，用於未來可能進行的碰撞偵測
    private float spawnInterval;           // 發射間隔
    private float projectileSpeed;        // 投射物飛行速度

    // 用來記錄每個投射物的 Sprite 和移動速度向量
    private static class ProjectileInstance {
        Sprite sprite;       // 投射物本體(Sprite)
        Vector2 velocity;    // 速度向量（單位：距離/秒）（Vector2）

        ProjectileInstance(Sprite sprite, Vector2 velocity) {
            this.sprite = sprite;
            this.velocity = velocity;
        }
    }
    // 提供給main.java呼叫的投射物create的方法，呼叫要有圖片與聲音路徑，功能請參照上方宣告物件。
    public Projectiles(String texturePath, String soundPath) {
        this.projectileTexture = new Texture(texturePath);
        this.hitSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        this.projectiles = new Array<>();
        this.projectileRectangle = new Rectangle();
        this.spawnInterval = SPAWN_INTERVAL;
        this.projectileSpeed = PROJECTILE_SPEED;
        this.spawnTimer = 0;
    }

    public void update(float delta, Rectangle characterRect, Viewport viewport, Character character) {
        // 更新每一個投射物的位置與判斷是否離開畫面
        for (int i = projectiles.size - 1; i >= 0; i--) {
            ProjectileInstance instance = projectiles.get(i);
            Sprite projectile = instance.sprite;
            Vector2 velocity = instance.velocity;

            // 以 velocity 代表速度方向向量，乘以 delta 更新位置（速度是距離/秒，所以要乘上 delta 才是每幀位移）
            // delta：每幀的時間差（秒），用來確保移動速度不受 FPS 影響
            projectile.translate(velocity.x * delta, velocity.y * delta);

            // 更新暫存矩形用於碰撞判定或其他用途（這邊雖然沒用到）
            projectileRectangle.set(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());

            // 若投射物超出視野範圍，則移除
            if (isOutOfView(projectile, viewport)) {
                projectiles.removeIndex(i);
            }
        }

        // 自動產生投射物，每隔一定時間發射
        spawnTimer += delta;
        if (spawnTimer > spawnInterval) {
            spawnTimer = 0;

            // 取得角色中心座標
            Vector2 characterCenter = character.getCenterPosition();

            // 取得滑鼠位置並轉為世界座標
            Vector2 screenMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 worldMousePos = viewport.unproject(screenMousePos);

            // 發射新的投射物
            spawnProjectile(characterCenter, worldMousePos);
        }
    }

    public void render(SpriteBatch batch) {
        // 渲染所有投射物
        for (ProjectileInstance instance : projectiles) {
            instance.sprite.draw(batch);
        }
    }

    // 負責生成新的投射物
    public void spawnProjectile(Vector2 characterCenter, Vector2 targetWorldPos) {
        float width = PROJECTILE_WIDTH;
        float height = PROJECTILE_HEIGHT;

        Sprite projectile = new Sprite(projectileTexture);
        projectile.setSize(width, height);
        projectile.setOriginCenter();

        // 從角色中心發射
        projectile.setPosition(characterCenter.x - width / 2, characterCenter.y - height / 2);

        // 計算投射方向向量，sub()：用滑鼠位置 - 角色中心位置，得到方向向量。nor()：將向量標準化（長度變成 1），這樣可以方便設定速度。
        Vector2 direction = new Vector2(targetWorldPos).sub(characterCenter).nor();

        // 設定投射物旋轉角度（使其尖端朝角色）
        float angleDeg = direction.angleDeg() + 90; // 加 90 是因為圖片尖端朝上（視素材而定）
        projectile.setRotation(angleDeg);

        // 計算實際速度向量（單位：距離/秒）。scl()：縮放向量，使它變成具備實際速度的向量>>例如方向是 (0.6, 0.8)，乘上速度 4 就會是 (2.4, 3.2)。
        Vector2 velocity = direction.scl(projectileSpeed);

        // 加入到投射物陣列中
        projectiles.add(new ProjectileInstance(projectile, velocity));
        hitSound.play();
    }

    // 檢查投射物是否超出攝影機視野邊界
    private boolean isOutOfView(Sprite sprite, Viewport viewport) {
        float camX = viewport.getCamera().position.x;
        float camY = viewport.getCamera().position.y;
        float camWidth = viewport.getWorldWidth();
        float camHeight = viewport.getWorldHeight();

        float left = camX - camWidth / 2;
        float right = camX + camWidth / 2;
        float bottom = camY - camHeight / 2;
        float top = camY + camHeight / 2;

        return (sprite.getX() + sprite.getWidth() < left ||
            sprite.getX() > right ||
            sprite.getY() + sprite.getHeight() < bottom ||
            sprite.getY() > top);
    }

    public void dispose() {
        projectileTexture.dispose();
        hitSound.dispose();
    }

    public Array<ProjectileInstance> getProjectiles() {
        return projectiles;
    }
}
