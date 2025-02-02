package net.quantumfusion.dashloader.mixin;

import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultipartModelComponent.class)
public interface MultipartModelComponentAccessor {

    @Accessor()
    MultipartModelSelector getSelector();
}
