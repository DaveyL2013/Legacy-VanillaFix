package piper74.legacy.vanillafix.stacktrace.mixin;

import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import piper74.legacy.vanillafix.util.PatchedCrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import piper74.legacy.vanillafix.stacktrace.ModIdentifier;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Mixin(value = CrashReport.class, priority = 500)
public abstract class MixinCrashReport implements PatchedCrashReport {
	@Shadow private StackTraceElement[] stackTrace;
    @Shadow
    @Final
    private Throwable exception;
	
    @Shadow
    @Final
    private CrashReportCategory systemDetails;
	
    @Shadow
    @Final
    private List<CrashReportCategory> details;
	
	@Shadow
    private static String getWittyComment() {
        return null;
    }
	
    @Shadow
    @Final
    private String description;
	
	private Set<ModMetadata> suspectedMods = null;
	
    private static String stacktraceToString(Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
	
	@Override
    public Set<ModMetadata> getSuspectedMods() {
        return suspectedMods;
    }
	
     /**
     * @reason Adds a list of mods which may have caused the crash to the report.
	 * @author Runemoro
     */
    @Inject(method = "fillSystemDetails", at = @At("TAIL"))
    private void afterFillSystemDetails(CallbackInfo ci) {
        systemDetails.add("Suspected Mods", () -> {
            try {
                suspectedMods = ModIdentifier.identifyFromStacktrace(exception);

                String modListString = "Unknown";
                List<String> modNames = new ArrayList<>();
                for (ModMetadata mod : suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getId() + ")");
                }

                if (!modNames.isEmpty()) {
                    modListString = StringUtils.join(modNames, ", ");
                }
                return modListString;
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }
	
    /**
     * @reason Improve report formatting
	 * @author Runemoro
     */
    @Overwrite
    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append("---- Minecraft Crash Report ----\n")
                        .append("// ").append(getWittyComment())
                        .append("\n\n")
                        .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append("\n")
                        .append("Description: ").append(description)
                        .append("\n\n")
                        .append(stacktraceToString(exception)
                                        .replace("\t", "    ")) // Vanilla's getCauseStackTraceOrString doesn't print causes and suppressed exceptions
                        .append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; i++) {
            builder.append("-");
        }

        builder.append("\n\n");
        addDetails(builder);
        return builder.toString().replace("\t", "    ");
    }
	
    /**
     * @reason Improve report formatting
	 * @author Runemoro
     */
    @Overwrite
    public void addDetails(StringBuilder builder) {
        for (CrashReportCategory section : details) {
            section.addDetails(builder);
            builder.append("\n");
        }

        systemDetails.addDetails(builder);
    }

	
}
