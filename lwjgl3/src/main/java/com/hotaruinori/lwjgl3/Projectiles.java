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
    private Texture projectileTexture;
    private Sound hitSound;
    private Array<Sprite> projectiles;
    private float spawnTimer;
    private Rectangle projectileRectangle;
    private float spawnInterval;
    private float projectileSpeed;

    public Projectiles(String texturePath, String soundPath, float spawnInterval, float projectileSpeed) {
        this.projectileTexture = new Texture(texturePath);
        this.hitSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        this.projectiles = new Array<>();
        this.projectileRectangle = new Rectangle();
        this.spawnInterval = spawnInterval;
        this.projectileSpeed = projectileSpeed;
        this.spawnTimer = 0;
    }

    public void update(float delta, Rectangle characterRect, Viewport viewport, Character character) {
        // 更新每一個投射物的位置與判斷是否離開畫面
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Sprite projectile = projectiles.get(i);

            // 以 scale 代表速度方向向量，乘以 delta 更新位置
            projectile.translate(projectile.getScaleX() * delta, projectile.getScaleY() * delta);

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
        for (Sprite projectile : projectiles) {
            projectile.draw(batch);
        }
    }

    // 負責生成新的投射物
    public void spawnProjectile(Vector2 characterCenter, Vector2 targetWorldPos) {
        float width = 1f;
        float height = 1f;

        Sprite projectile = new Sprite(projectileTexture);
        projectile.setSize(width, height);
        projectile.setOriginCenter();

        // 從角色中心發射
        projectile.setPosition(characterCenter.x - width / 2, characterCenter.y - height / 2);

        // 計算投射方向向量
        Vector2 direction = new Vector2(targetWorldPos).sub(characterCenter).nor();

        // 設定投射物旋轉角度（使其尖端朝角色）
        float angleDeg = direction.angleDeg() + 180;
        projectile.setRotation(angleDeg);

        // 偷吃步：用 scale 儲存方向速度（之後移動會乘 delta）
        projectile.setScale(direction.x * projectileSpeed, direction.y * projectileSpeed);

        // 加入到投射物陣列中
        projectiles.add(projectile);
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

    public Array<Sprite> getProjectiles() {
        return projectiles;
    }
}
