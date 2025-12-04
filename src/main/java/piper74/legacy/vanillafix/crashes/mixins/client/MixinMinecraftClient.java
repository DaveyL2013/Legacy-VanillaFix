/*
 *This file is modified based on
 *https://github.com/vfyjxf/BetterCrashes/blob/da95d1f801d83b11479511b01d9d83bc822dfa2b/src/main/java/vfyjxf/bettercrashes/mixins/client/MixinMinecraft.java
 *The source file uses the MIT License.
 */

package piper74.legacy.vanillafix.crashes.mixins.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.TickTimer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.resource.pack.ResourcePacks;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.system.SoundManager;
import net.minecraft.client.render.texture.TextureManager;
import piper74.legacy.vanillafix.crashes.compatibility.CWindow;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.resource.pack.BuiltInResourcePack;
import net.minecraft.client.resource.manager.ReloadableResourceManager;
import net.minecraft.client.resource.manager.SimpleReloadableResourceManager;
import net.minecraft.client.resource.pack.ResourcePack;
import net.minecraft.text.LiteralText;
import net.minecraft.resource.Identifier;
import net.minecraft.client.resource.metadata.ResourceMetadataSerializerRegistry;
import net.minecraft.util.BlockableEventLoop;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.snooper.SnooperPopulator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.*;
import piper74.legacy.vanillafix.LegacyVanillaFix;
import piper74.legacy.vanillafix.config.LegacyVanillaFixConfig;
import piper74.legacy.vanillafix.util.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Runemoro
 */
@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient implements BlockableEventLoop, SnooperPopulator {
	
	LegacyVanillaFixConfig config = LegacyVanillaFix.getConfig();

	@Final
	@Mutable
	@Shadow
    private static Logger LOGGER = LogManager.getLogger();
	@Shadow
    volatile boolean running;
	@Shadow
	private CrashReport crashReport;
	@Shadow
	private void init() throws LWJGLException, IOException {}
	@Shadow
	public static byte[] MEMORY_RESERVED_FOR_CRASH;
	
	@Shadow
	public abstract CrashReport populateCrashReport(CrashReport crashReport);
	
	@Shadow
	public abstract void stop();
	
	@Shadow
	private void runGame() {}
	
	@Shadow
	private boolean crashed;
	
	@Shadow
	public GameOptions options;
	
	@Shadow
	public abstract void openScreen(Screen screen);
	
	@Shadow
	public ClientWorld world;
	
	//@Shadow
	private CrashReport crashReport2;
	
	@Shadow
	public GameGui gui;
	
	@Shadow
	private long f3CTime;
	
	@Shadow
	public abstract ClientPlayNetworkHandler getNetworkHandler();

	@Shadow
	public void setWorld(ClientWorld world) {}

	@Shadow public boolean focused;
	@Shadow private boolean /*glErrors*/ f_2765940;
	private static int clientCrashCount = 0;
    private static int serverCrashCount = 0;

	@Shadow
	private ReloadableResourceManager resourceManager;

	@Shadow
	private ResourcePacks resourcePacks;

	@Shadow
	private LanguageManager languageManager;

	@Final
	@Mutable
	@Shadow
	private ResourceMetadataSerializerRegistry resourceMetadataSerializerRegistry = new ResourceMetadataSerializerRegistry();

	@Shadow
	public void reloadResources() {}

	@Shadow
	public TextRenderer textRenderer;
	@Shadow
	public TextRenderer shadowTextRenderer;
	@Shadow
	public Screen screen;
	@Shadow
	private TextureManager textureManager;
	@Shadow
	private SoundManager soundManager;

	@Shadow
	public int width;

	@Shadow
	public int height;

	@Shadow
	private void initIcon() {}

	@Shadow
	private void initDisplayMode() throws LWJGLException {}

	@Shadow
	private void initDisplay() throws LWJGLException {}

	@Mutable
	@Final
	@Shadow
	private BuiltInResourcePack defaultResourcePack;

	@Shadow @Final private List<ResourcePack> defaultResourcePacks;

	@Shadow
	private RenderTarget renderTarget;
	@Shadow public MouseInput mouse;

	@Shadow public abstract void updateDisplay();

	@Shadow protected abstract void logGlError(String message);

	@Shadow private int attackCooldown;

	@Mutable
	@Final
	@Shadow
	private File resourcePacksDir;

	@Mutable
	@Final
	@Shadow
	public File runDir;

	@Shadow
	private void initTimerHackThread() {}

	@Shadow
	public GameRenderer gameRenderer;

	@Shadow
	private void initResourceMetadataSerializers() {}

	@Shadow
	public WorldRenderer worldRenderer;

	@Shadow
	private TickTimer timer = new TickTimer(20.0f);

	@Shadow public boolean skipGameRender;

	/**
     * @author Runemoro
     * @reason Overwrite Minecraft.run()
     */
	@Overwrite
	public void run(){
	
	      running = true;

      //CrashReport crashReport2;
      try {
         this.init();
      } catch (Throwable var11) {
         crashReport2 = CrashReport.of(var11, "Initializing game");
         crashReport2.addCategory("Initialization");
         //this.printCrashReport(addSystemDetailsToCrashReport(crashReport2));
         //return;
		 //this.stop();
		  if(config.catchInitCrashes) {
			  displayInitErrorScreen(crashReport2);
		  } else
		  {
			  this.printCrashReport(populateCrashReport(crashReport2));
			  this.stop();
		  }
		  return;
      }



	  
	  while(running) {
			  if (!crashed || crashReport == null) {
				  try {
					  runGame();
				  } catch (CrashException e) {
                     clientCrashCount++;
					 populateCrashReport(e.getReport());
					 addInfoToCrash(e.getReport());
					 //if (config.betterCrashes)
					 resetGameState();
						//else
						//cleanHeap();
					 LOGGER.fatal("Reported exception thrown!", e);
					 if (config.betterCrashes) {
					 displayCrashScreen(e.getReport(), clientCrashCount);
					 } else {
					 printCrashReport(e.getReport());
					 this.stop();
					 }
				  } catch (Throwable e) {
                     clientCrashCount++;
					 CrashReport report = new CrashReport("Unexpected error", e);
					 populateCrashReport(report);
					 addInfoToCrash(report);
					 //if (config.betterCrashes)
					 resetGameState();
						//else
						//cleanHeap();
					 LOGGER.fatal("Unreported exception thrown!", e);
					 if (config.betterCrashes) {
					 displayCrashScreen(report, clientCrashCount);
					 } else {
					 printCrashReport(report);
					 this.stop();
					 }
				  }
			  } else {
			  serverCrashCount++;
			  addInfoToCrash(crashReport2);
			  // FREE MEMORY HERE!
			  //Runtime.getRuntime().freeMemory();
			  cleanHeap();
			  displayCrashScreen(crashReport2, serverCrashCount);
			  crashed = false;
			  crashReport2 = null;
			  }
		  }
		  
		  this.stop();
	  }
			 
    private static void addInfoToCrash(CrashReport report) {
        report.getSystemDetails().add("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemDetails().add("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }			 
	
	 /**
     * @author Runemoro
     */
    public void resetGameState() {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalMemoryReserveSize = -1;
            try { // In case another mod actually deletes the memoryReservedForCrash field
                if (MEMORY_RESERVED_FOR_CRASH != null) {
                    originalMemoryReserveSize = MEMORY_RESERVED_FOR_CRASH.length;
                    MEMORY_RESERVED_FOR_CRASH = new byte[0];
                }
            } catch (Throwable ignored) {}

            StateManager.resetStates();

			if (getNetworkHandler() != null) {
			getNetworkHandler().getConnection().disconnect(new LiteralText(String.format("[%s] Client crashed", "Legacy VanillaFix")));
			}

         this.world.disconnect();
         this.setWorld((ClientWorld)null);
			
            //field_152351_aB.clear(); // TODO: Figure out why this isn't necessary for vanilla disconnect
			
		GlUtil.resetState();

            if (originalMemoryReserveSize != -1) {
                try {
                    MEMORY_RESERVED_FOR_CRASH = new byte[originalMemoryReserveSize];
                } catch (Throwable ignored) {}
            }
            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
            try {
                StateManager.resetStates();
				GlUtil.resetState();
            } catch (Throwable ignored) {}
        }
    }

	/**
	 * @author Runemoro
	 * @reason Disconnect from the current world and free memory, using a memory reserve
	 * to make sure that an OutOfMemory doesn't happen while doing this.
	 * <p>
	 * Bugs Fixed:
	 * - https://bugs.mojang.com/browse/MC-128953
	 * - Memory reserve not recreated after out-of memory
	 **/
	@Overwrite
	public void cleanHeap() {
		resetGameState();
	}
	
	 /**
     * @author Runemoro
     * @param report, crashCount
     */
    public void displayCrashScreen(CrashReport report, int crashCount) {
        try {
            CrashUtils.outputReport(report);
            // Reset crashed, f3CTime
            crashed = false;
			this.f3CTime = -1L;

            if (crashCount > 19) {
                throw new IllegalStateException("The game has crashed an excessive amount of times!");
            }
            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            options.debugEnabled = false;
			gui.getChat().clear();


            // Display the crash screen
            runGuiLoop(new GuiCrashScreen(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occurred while displaying the crash screen, making normal report instead", t);
            //displayCrashReport(report);
			printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

	private void runGuiLoop(Screen screen) throws IOException
	{
		openScreen(screen);
		this.focused = true;
		while (running && screen != null && !(screen instanceof TitleScreen)) {

			// Restore compatibility with Minecraft 1.8
			// No function here needs the Window class as an argument,
			// so we can work around the crash in 1.8 by
			// implementing our own custom Window class
			CWindow window = new CWindow(Minecraft.getInstance(), width, height);

			if(Display.isCreated() && Display.isCloseRequested()) System.exit(0);

			textureManager.tick();

			attackCooldown = 10000;
			screen.handleInputs();
			//currentScreen.getClass().getCanonicalName();
			screen.tick();
			//currentScreen.getClass().getCanonicalName();


			soundManager.tick();

			mouse.tick();

			//currentScreen.

			GlStateManager.pushMatrix();
			GlStateManager.clear(16640);
			renderTarget.bindWrite(true);
			GlStateManager.enableTexture(); //OG ONE


			GlStateManager.viewport(0, 0, width, height);

			GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();

			GlStateManager.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0, 1000, 3000);

			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translatef(0, 0, -2000);
			GlStateManager.clear(256);

			int windowWidth = window.getWidth();
			int windowHeight = window.getHeight();

			//GlStateManager.enableBlend();

			screen.render(
					(int) (Mouse.getX() * windowWidth / width),
					(int) (windowHeight - Mouse.getY() * windowHeight / height - 1),
					timer.tickDelta
			);

			//GlStateManager.disableBlend();

			renderTarget.unbindWrite();
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			//fbo.draw(window.getWidth() * i, window.getHeight() * i);
			renderTarget.draw(width, height);
			GlStateManager.popMatrix();

			//LegacyVanillaFix.LOGGER.info("RunGUILOOP FINISHED!");


			this.updateDisplay();
			Thread.yield();
			Display.sync(60);
			this.logGlError("Legacy VanillaFix GUI Loop");
		}
	}

	public void displayInitErrorScreen(CrashReport crashReport) {
		//Minecraft.getInstance().
		//crashReport = CrashReport.of(var11, "Initializing game");
		//crashReport.addElement("Initialization");

		CrashUtils.outputReport(crashReport2);
		try {
			options = new GameOptions(Minecraft.getInstance(), runDir);
			defaultResourcePacks.add(defaultResourcePack);
			initTimerHackThread();

			initIcon();
			initDisplayMode();
			initDisplay();
			GLX.init();

			this.renderTarget = new RenderTarget(width, height, true);
			this.renderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

			initResourceMetadataSerializers();
			this.resourcePacks = new ResourcePacks(this.resourcePacksDir, new File(runDir, "server-resource-packs"), defaultResourcePack, resourceMetadataSerializerRegistry, options);
			this.resourceManager = new SimpleReloadableResourceManager(this.resourceMetadataSerializerRegistry);

			this.languageManager = new LanguageManager(this.resourceMetadataSerializerRegistry, options.language);
			this.resourceManager.addListener(languageManager);

			this.textureManager = new TextureManager(this.resourceManager);
			this.resourceManager.addListener(this.textureManager);

			reloadResources();

			this.textRenderer = new TextRenderer(this.options, new Identifier("textures/font/ascii.png"), this.textureManager, false);
			this.shadowTextRenderer = new TextRenderer(options, new Identifier("textures/font/ascii_sga.png"), this.textureManager, false);
			this.resourceManager.addListener(this.textRenderer);
			this.resourceManager.addListener(this.shadowTextRenderer);

			soundManager = new SoundManager(resourceManager, options);
			resourceManager.addListener(soundManager);

			// DO NOT INITIALISE THIS, CAUSES FURTHER CRASHING PROBLEMS
			/*
			gameRenderer = new GameRenderer(Minecraft.getInstance(), resourceManager);
			resourceManager.addListener(gameRenderer);

			this.worldRenderer = new WorldRenderer(Minecraft.getInstance());
			this.resourceManager.addListener(this.worldRenderer);
			*/

			mouse = new MouseInput();

			running = true;

			//LegacyVanillaFix.LOGGER.info("DisplayInitErrorScreen ran!");

			runGuiLoop(new GuiInitErrorScreen(crashReport2));
		} catch (Throwable t) {
			CrashReport additionalReport = CrashReport.of(t, "Displaying init error screen");
			LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
			printCrashReport(additionalReport);
			System.exit(additionalReport.getFile() != null ? -1 : -2);
		}
	}

	 /**
     * @author Runemoro
     * @reason
     * @param report
     */
    @Overwrite
    public void printCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }

	/****
	 * @author ZombieHDGaming
	 * @reason removes a call to system.gc() to make world loading faster
	****/

	// breaks loading of singleplayer worlds
	/*
	@Inject(method="startGame", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"), cancellable = true)
	public void startGame(String worldName, String string, LevelInfo levelInfo, CallbackInfo ci) {
		ci.cancel();
	}
	*/

}


