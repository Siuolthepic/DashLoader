package net.quantumfusion.dashloader.mixin;

import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ParticleTextureData.class)
public interface ParticleTextureDataAccessor {

    @Invoker("<init>")
    static ParticleTextureData newParticleTextureData(@Nullable List<Identifier> textureList) {
        throw new AssertionError();
    }
}
