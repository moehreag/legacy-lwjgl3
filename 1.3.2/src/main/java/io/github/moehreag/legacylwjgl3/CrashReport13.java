package io.github.moehreag.legacylwjgl3;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class CrashReport13 {
	public static void report(net.minecraft.util.crash.CrashReport report) {
		StringWriter stringWriter = new StringWriter();
		report.getException().printStackTrace(new PrintWriter(stringWriter));
		String trace = stringWriter.toString();
		String cardManufacturer = "";
		StringBuilder errorText = new StringBuilder();

		try {
			errorText.append("Generated ").append(new SimpleDateFormat().format(new Date())).append("\n");
			errorText.append("\n");
			errorText.append(report.buildString());
			cardManufacturer = GL11.glGetString(7936);
		} catch (Throwable var8) {
			errorText.append(errorText).append("[failed to get system properties (").append(var8).append(")]\n");
		}

		errorText.append("\n\n");
		errorText.append(trace);
		StringBuilder reportText = new StringBuilder();
		reportText.append("\n");
		reportText.append("\n");
		if (trace.contains("Pixel format not accelerated")) {
			reportText.append("      Bad video card drivers!      \n");
			reportText.append("      -----------------------      \n");
			reportText.append("\n");
			reportText.append("Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n");
			reportText.append("This can usually be fixed by updating the video card drivers.\n");
			if (cardManufacturer.toLowerCase().contains("nvidia")) {
				reportText.append("\n");
				reportText.append("You might be able to find drivers for your video card here:\n");
				reportText.append("  http://www.nvidia.com/\n");
			} else if (cardManufacturer.toLowerCase().contains("ati")) {
				reportText.append("\n");
				reportText.append("You might be able to find drivers for your video card here:\n");
				reportText.append("  http://www.amd.com/\n");
			}
		} else {
			reportText.append("      Minecraft has crashed!      \n");
			reportText.append("      ----------------------      \n");
			reportText.append("\n");
			reportText.append("Minecraft has stopped running because it encountered a problem; ").append(report.getDescription()).append("\n");
			File file = report.getFile();
			if (file == null) {
				report.save(
						new File(
								new File(Minecraft.getWorkingDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt"
						)
				);
				file = report.getFile();
			}

			if (file != null) {
				reportText.append("This error has been saved to ").append(file.getAbsolutePath()).append(" for your convenience. Please include a copy of this file if you report this crash to anyone.");
			} else {
				reportText.append("We were unable to save this report to a file.");
			}

			reportText.append("\n");
		}

		reportText.append("\n");
		reportText.append("\n");
		reportText.append("\n");
		reportText.append("--- BEGIN ERROR REPORT ").append(Integer.toHexString(reportText.hashCode())).append(" --------\n");
		reportText.append(errorText);
		reportText.append("--- END ERROR REPORT ").append(Integer.toHexString(reportText.hashCode())).append(" ----------\n");
		reportText.append("\n");
		reportText.append("\n");

		Display.destroy();
		System.out.println(reportText);
	}
}