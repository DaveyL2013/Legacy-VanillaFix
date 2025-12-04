package piper74.legacy.vanillafix.particlecull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.particle.Particle;
import net.minecraft.client.render.Culler;
import piper74.legacy.vanillafix.LegacyVanillaFix;
import piper74.legacy.vanillafix.config.LegacyVanillaFixConfig;

// Used

public class CullParticle {
    static LegacyVanillaFixConfig config = LegacyVanillaFix.getConfig();

    //public static boolean shouldRenderParticle(Particle particle) {
        public static boolean shouldRenderParticle(Particle instance)
        {
        if(!config.cullParticles) {
            return true;
        }

        Culler camera = ((ICuller) Minecraft.getInstance().worldRenderer).getCamera();

        if(camera != null)
        {
            return camera.isVisible(instance.getShape());
        }

        return false;

    }
}