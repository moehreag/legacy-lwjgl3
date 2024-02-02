package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftFixEarlyCrashNoReports {
	/*
    // @formatter:off
    @Shadow @Final private String gameVersion;
    @Shadow public GameOptions options;
    @Shadow private LanguageManager languageManager;
    @Shadow @Final public Profiler profiler;
    @Shadow public ClientWorld world;
    // @formatter:on

    /**
     *
     * @reason Fix early crashes (before an opengl context is created) causing crash reports not being generated
     * @author mojang
     *
    @Overwrite
    public CrashReport populateCrashReport(CrashReport crashReport) {
        crashReport.getSystemDetails().add("Launched Version", () -> MixinMinecraftFixEarlyCrashNoReports.this.gameVersion);
        crashReport.getSystemDetails().add("LWJGL", Sys::getVersion);
        // check if gl is available in the current thread
        try {
            GL.getCapabilities();
            crashReport.getSystemDetails().add("OpenGL", () -> GL11.glGetString(7937) + " GL version " + GL11.glGetString(7938) + ", " + GL11.glGetString(7936));
        } catch (IllegalStateException throwable) {
            // no-op if gl is not available
        }
        crashReport.getSystemDetails().add("GL Caps", GLX::getGlCapsInfo);
        crashReport.getSystemDetails().add("Using VBOs", () -> MixinMinecraftFixEarlyCrashNoReports.this.options.useVbo ? "Yes" : "No");
        crashReport.getSystemDetails().add("Is Modded", () -> {
            String string = ClientBrandRetriever.getClientModName();
            if (!string.equals("vanilla")) {
                return "Definitely; Client brand changed to '" + string + "'";
            } else {
                return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.";
            }
        });
        crashReport.getSystemDetails().add("Type", () -> "Client (map_client.txt)");
        crashReport.getSystemDetails().add("Resource Packs", () -> {
            StringBuilder stringBuilder = new StringBuilder();

            for (String string : MixinMinecraftFixEarlyCrashNoReports.this.options.resourcePacks) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }

                stringBuilder.append(string);
                if (MixinMinecraftFixEarlyCrashNoReports.this.options.incompatibleResourcePacks.contains(string)) {
                    stringBuilder.append(" (incompatible)");
                }
            }

            return stringBuilder.toString();
        });
        crashReport.getSystemDetails().add("Current Language", () -> MixinMinecraftFixEarlyCrashNoReports.this.languageManager.getLanguage().toString());
        crashReport.getSystemDetails().add("Profiler Position", () -> MixinMinecraftFixEarlyCrashNoReports.this.profiler.isProfiling ? MixinMinecraftFixEarlyCrashNoReports.this.profiler.getCurrentLocation() : "N/A (disabled)");
        crashReport.getSystemDetails().add("CPU", GLX::getCpuInfo);
        if (this.world != null) {
            this.world.populateCrashReport(crashReport);
        }

        return crashReport;
    }*/
}
