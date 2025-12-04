package piper74.legacy.vanillafix.particlecull.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.render.Culler;
import net.minecraft.entity.Entity;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import piper74.legacy.vanillafix.particlecull.ICuller;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements ICuller {
    @Shadow @Final private static Logger LOGGER;
    @Unique
    Culler cameraView;

    @Inject(method = "setupRender", at = @At("HEAD"))
    public void setupRender(Entity entity, double d, Culler cameraView, int i, boolean bl, CallbackInfo ci)
    {
        this.cameraView = cameraView;
    }

    @Override
    public Culler getCamera()
    {
        return this.cameraView;
    }

}
