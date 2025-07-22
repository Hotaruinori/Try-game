package com.hotaruinori.main.other;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.*;

import com.hotaruinori.main.other.InfiniteBackground.ChunkData;

public class TownTemplate {

    public static void tryGenerateTown(int chunkX, int chunkY, InfiniteBackground bg, ChunkData data) {
        InfiniteBackground.BackgroundObjectType houseType = null;
        InfiniteBackground.BackgroundObjectType fenceType = null;

        for (InfiniteBackground.BackgroundObjectType type : bg.getObjectTypes()) {
            if (type.name.equals("house")) houseType = type;
            else if (type.name.equals("fence")) fenceType = type;
        }
        if (houseType == null || fenceType == null) return;

        float chunkOriginX = chunkX * InfiniteBackground.CHUNK_SIZE;
        float chunkOriginY = chunkY * InfiniteBackground.CHUNK_SIZE;
        float chunkSize = InfiniteBackground.CHUNK_SIZE;
        float houseWidth = chunkSize * 0.25f;
        float houseHeight = chunkSize * 0.25f;
        float fenceThickness = chunkSize * 0.03f;
        float centerX = chunkOriginX + chunkSize / 2;
        float centerY = chunkOriginY + chunkSize / 2;

        float[][] offsets = {
            {centerX - houseWidth, centerY},
            {centerX, centerY},
            {centerX - houseWidth, centerY - houseHeight},
            {centerX, centerY - houseHeight}
        };

        for (float[] offset : offsets) {
            float houseX = offset[0];
            float houseY = offset[1];

            Sprite house = new Sprite(houseType.getRandomTexture());
            house.setSize(houseWidth, houseHeight);
            house.setOriginCenter();
            house.setPosition(houseX, houseY);
            data.objects.add(house);
            if (houseType.isBlocking) {
                data.blockingBounds.add(new Rectangle(houseX, houseY, houseWidth, houseHeight));
            }

            float fx = houseX - fenceThickness;
            float fy = houseY - fenceThickness;
            float fw = houseWidth + 2 * fenceThickness;
            float fh = houseHeight + 2 * fenceThickness;

            for (int i = 0; i < Math.ceil(fw / 0.5f); i++) {
                float x = fx + i * 0.5f;
                for (float y : new float[]{fy, fy + fh - fenceThickness}) {
                    Sprite fence = new Sprite(fenceType.getRandomTexture());
                    fence.setSize(0.4f, 0.3f);
                    fence.setOriginCenter();
                    fence.setPosition(x, y);
                    data.objects.add(fence);
                    if (fenceType.isBlocking) {
                        data.blockingBounds.add(new Rectangle(x, y, 0.4f, 0.3f));
                    }
                }
            }

            for (int i = 0; i < Math.ceil(fh / 0.5f); i++) {
                float y = fy + i * 0.5f;
                for (float x : new float[]{fx, fx + fw - fenceThickness}) {
                    Sprite fence = new Sprite(fenceType.getRandomTexture());
                    fence.setSize(0.3f, 0.4f);
                    fence.setOriginCenter();
                    fence.setPosition(x, y);
                    data.objects.add(fence);
                    if (fenceType.isBlocking) {
                        data.blockingBounds.add(new Rectangle(x, y, 0.3f, 0.4f));
                    }
                }
            }
        }
    }
}
