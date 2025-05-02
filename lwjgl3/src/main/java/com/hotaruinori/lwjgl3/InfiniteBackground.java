package com.hotaruinori.lwjgl3;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class InfiniteBackground {
    private Texture texture;
    private float tileWidth = 4f;
    private float tileHeight = 4f;

    public InfiniteBackground(String texturePath) {
        texture = new Texture(texturePath);
    }

    public void render(SpriteBatch batch, Vector2 characterCenter, float worldWidth, float worldHeight) {
        int tilesX = (int) Math.ceil(worldWidth / tileWidth) + 2;
        int tilesY = (int) Math.ceil(worldHeight / tileHeight) + 2;

        float offsetX = characterCenter.x % tileWidth;
        float offsetY = characterCenter.y % tileHeight;

        float startX = characterCenter.x - worldWidth / 2 - offsetX - tileWidth;
        float startY = characterCenter.y - worldHeight / 2 - offsetY - tileHeight;

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                batch.draw(
                    texture,
                    startX + x * tileWidth,
                    startY + y * tileHeight,
                    tileWidth,
                    tileHeight
                );
            }
        }
    }

    public void dispose() {
        texture.dispose();
    }
}
