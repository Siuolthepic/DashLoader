package net.quantumfusion.dashloader.cache.models.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.quantumfusion.dashloader.cache.DashDirection;

public class DashBakedQuad {
    @Serialize(order = 0)
    public int[] vertexData;
    @Serialize(order = 1)
    public int colorIndex;
    @Serialize(order = 2)
    public DashDirection face;
    @Serialize(order = 3)
    public boolean shade;

    //post


    public DashBakedQuad(@Deserialize("vertexData") int[] vertexData,
                         @Deserialize("colorIndex") int colorIndex,
                         @Deserialize("face") DashDirection face,
                         @Deserialize("shade") boolean shade) {
        this.vertexData = vertexData;
        this.colorIndex = colorIndex;
        this.face = face;
        this.shade = shade;
    }

    public DashBakedQuad(BakedQuad bakedQuad) {
        vertexData = bakedQuad.getVertexData();
        colorIndex = bakedQuad.getColorIndex();
        face = new DashDirection(bakedQuad.getFace());
        shade = bakedQuad.hasShade();
    }

    public BakedQuad toUndash(Sprite sprite) {
        return new BakedQuad(vertexData, colorIndex, face.toUndash(), sprite, shade);
    }
}
