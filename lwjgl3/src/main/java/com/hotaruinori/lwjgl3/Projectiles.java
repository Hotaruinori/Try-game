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
    private float projectile_Speed;

    public Projectiles(String texturePath, String soundPath, float spawnInterval, float projectile_Speed) {
        projectileTexture = new Texture(texturePath);
        hitSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
        projectiles = new Array<>();
        projectileRectangle = new Rectangle();
        this.spawnInterval = spawnInterval;
        this.projectile_Speed = projectile_Speed;
        spawnTimer = 0;
    }

    public void update(float delta, Rectangle characterRect, Viewport viewport, Character character) {
        // 更新現有投射物
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Sprite projectile = projectiles.get(i);
            //更新位置
            projectile.translate(projectile.getScaleX() * delta, projectile.getScaleY() * delta);
            projectileRectangle.set(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
            //觸碰邊界消失
            if (projectile.getY() < -projectile.getHeight() || projectile.getY() > 10 ||
                projectile.getX() < -projectile.getWidth() || projectile.getX() > 10) {
                projectiles.removeIndex(i);
            }
        }

        //更新角色中心點座標，即投射物發射位置基準；與滑鼠瞄準座標
        Vector2 startPos = character.getCenterPosition();
        Vector2 target = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 mouse_target = viewport.unproject(target);
        // 生成新投射物
        spawnTimer += delta;
        if (spawnTimer > spawnInterval) {
            spawnTimer = 0;
            spawnProjectile(startPos, mouse_target);
        }
    }

    public void render(SpriteBatch batch) {
        for (Sprite projectile : projectiles) {
            projectile.draw(batch);
        }
    }

    public void spawnProjectile(Vector2 characterCenter, Vector2 targetWorldPos) {
        float width = 1;
        float height = 1;

        Sprite projectile = new Sprite(projectileTexture);
        projectile.setSize(width, height);
        projectile.setOriginCenter();
        projectile.setPosition(characterCenter.x - width / 2, characterCenter.y - height / 2);

        // 計算方向與角度
        Vector2 direction = new Vector2(targetWorldPos).sub(characterCenter).nor();
        float angleDeg = direction.angleDeg() + 180; // 尖端朝向角色
        projectile.setRotation(angleDeg);

        // 將速度記錄到 scale（偷吃步）
        projectile.setScale(direction.x * projectile_Speed, direction.y * projectile_Speed);

        projectiles.add(projectile);
        hitSound.play();
    }

    public void dispose() {
        projectileTexture.dispose();
        hitSound.dispose();
    }

    public Array<Sprite> getProjectiles() {
        return projectiles;
    }
}
