// This code was based on
// github.com/FreakingChicken/TransparentSkins/blob/master/src/main/java/me/FreakingChicken/TransparentSkins/client/renderer/entity/RenderPlayerOverride.java

package piper74.legacy.vanillafix.bugs.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.Model;
import net.minecraft.client.render.model.entity.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import piper74.legacy.vanillafix.LegacyVanillaFix;
import piper74.legacy.vanillafix.config.LegacyVanillaFixConfig;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<ClientPlayerEntity> {

    LegacyVanillaFixConfig config = LegacyVanillaFix.getConfig();
    public PlayerRendererMixin(EntityRenderDispatcher dispatcher, Model model, float shadowSize) {
        super(dispatcher, model, shadowSize);
    }

    @Shadow
    private void setModelStatus(ClientPlayerEntity abstractClientPlayerEntity) {}


    @Shadow public abstract PlayerModel getModel();


    /**
     * @author piper74
     * @reason Enables semi-transparency on skins
     */

    @Inject(method = "render(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;DDDFF)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerRenderer;setModelStatus(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;)V"))
    public void enableblend(ClientPlayerEntity abstractClientPlayerEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        if(config.enableSkinSemiTransparency) {
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;DDDFF)V", at=@At("TAIL"))
    public void disableblend(ClientPlayerEntity abstractClientPlayerEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        if(config.enableSkinSemiTransparency) {
            GlStateManager.disableBlend();
        }
    }


    /**
     * @author piper74
     * @reason Enables semi-transparency on arms
     */
    @Overwrite
    public void renderPlayerRightHandModel(ClientPlayerEntity abstractClientPlayerEntity) {
        float f = 1.0f;
        GlStateManager.color3f(f, f, f);
        PlayerModel playerEntityModel = this.getModel();
        this.setModelStatus(abstractClientPlayerEntity);
        if(config.enableSkinSemiTransparency) {
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        }
        playerEntityModel.handSwingProgress = 0.0f;

        if(config.handBugFix) {
        // WEIRD HAND FIX
        // Fix by zlainsama
        // https://github.com/zlainsama/SkinPort/commit/d86b2d7670c0ab0fca98d474882583f286ec7a9f

        // hasVehicle = isRiding
            playerEntityModel.hasVehicle = false;
        }
        // sneaking = isSneaking
            playerEntityModel.sneaking = false;


        playerEntityModel.setAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, abstractClientPlayerEntity);
        playerEntityModel.renderRightArm();

        if(config.enableSkinSemiTransparency) {
            GlStateManager.disableBlend();
        }
    }

    /**
     * @author piper74
     * @reason Enables semi-transparency on arms
     */
    @Overwrite
    public void renderPlayerLeftHandModel(ClientPlayerEntity abstractClientPlayerEntity) {
        float f = 1.0f;
        GlStateManager.color3f(f, f, f);
        PlayerModel playerEntityModel = this.getModel();
        this.setModelStatus(abstractClientPlayerEntity);
        if(config.enableSkinSemiTransparency) {
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        }
        playerEntityModel.sneaking = false;
        playerEntityModel.handSwingProgress = 0.0f;
        playerEntityModel.setAngles(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, abstractClientPlayerEntity);
        playerEntityModel.renderLeftArm();
        if(config.enableSkinSemiTransparency) {
            GlStateManager.disableBlend();
        }
    }

}
