package piper74.legacy.vanillafix.particlecull.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.particle.Particle;
import net.minecraft.client.entity.particle.ParticleManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import piper74.legacy.vanillafix.particlecull.CullParticle;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

@Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/particle/Particle;render(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/entity/Entity;FFFFFF)V"))
public void cullParticles(Particle instance, BufferBuilder builder, Entity entity, float f, float g, float h, float i, float j, float k) {
    if(CullParticle.shouldRenderParticle(instance))
        instance.render(builder, entity, f, g, h, i, j, k);
    }

    @Redirect(method = "renderLitParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/particle/Particle;render(Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/entity/Entity;FFFFFF)V"))
    public void cullLitParticles(Particle instance, BufferBuilder builder, Entity entity, float f, float g, float h, float i, float j, float k) {
        if(CullParticle.shouldRenderParticle(instance))
           instance.render(builder, entity, f, g, h, i, j, k);
    }

}
