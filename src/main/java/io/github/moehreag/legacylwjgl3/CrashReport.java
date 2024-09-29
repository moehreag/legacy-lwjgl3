package io.github.moehreag.legacylwjgl3;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.class_447;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class CrashReport {
	public static void report(class_447 summary){
		Display.destroy();
		StringWriter var2 = new StringWriter();
		summary.field_1705.printStackTrace(new PrintWriter(var2));
		String stackTrace = var2.toString();
		String cardManufacturer = "";
		StringBuilder report = new StringBuilder();

		try {
			report.append("Generated ").append(new SimpleDateFormat().format(new Date())).append("\n");
			report.append("\n");
			report.append("Minecraft: Minecraft b1.7.3\n");
			report.append("OS: ")
				.append(System.getProperty("os.name"))
				.append(" (")
				.append(System.getProperty("os.arch"))
				.append(") version ")
				.append(System.getProperty("os.version"))
				.append("\n");
			report.append("Java: ").append(System.getProperty("java.version")).append(", ").append(System.getProperty("java.vendor")).append("\n");
			report.append("VM: ")
				.append(System.getProperty("java.vm.name"))
				.append(" (").append(System.getProperty("java.vm.info")).append("), ")
				.append(System.getProperty("java.vm.vendor")).append("\n");
			report.append("LWJGL: ").append(Sys.getVersion()).append("\n");
			cardManufacturer = GL11.glGetString(7936);
			report.append("OpenGL: ").append(GL11.glGetString(7937)).append(" version ").append(GL11.glGetString(7938)).append(", ").append(GL11.glGetString(7936)).append("\n");
		} catch (Throwable var8) {
			report.append("[failed to get system properties (").append(var8).append(")]\n");
		}

		report.append("\n");
		report.append(var2);
		StringBuilder var6 = new StringBuilder();
		var6.append("\n");
		var6.append("\n");
		if (stackTrace.contains("Pixel format not accelerated")) {
			var6.append("      Bad video card drivers!      \n");
			var6.append("      -----------------------      \n");
			var6.append("\n");
			var6.append("Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n");
			var6.append("This can usually be fixed by updating the video card drivers.\n");
			if (cardManufacturer.toLowerCase().contains("nvidia")) {
				var6.append("\n");
				var6.append("You might be able to find drivers for your video card here:\n");
				var6.append("  http://www.nvidia.com/\n");
			} else if (cardManufacturer.toLowerCase().contains("ati")) {
				var6.append("\n");
				var6.append("You might be able to find drivers for your video card here:\n");
				var6.append("  http://www.amd.com/\n");
			}
		} else {
			var6.append("      Minecraft has crashed!      \n");
			var6.append("      ----------------------      \n");
			var6.append("\n");
			var6.append("Minecraft has stopped running because it encountered a problem.\n");
			var6.append("\n");
		}

		var6.append("\n");
		var6.append("\n");
		var6.append("\n");
		var6.append("--- BEGIN ERROR REPORT ").append(Integer.toHexString(var6.hashCode())).append(" --------\n");
		var6.append(report);
		var6.append("--- END ERROR REPORT ").append(Integer.toHexString(var6.hashCode())).append(" ----------\n");
		var6.append("\n");
		var6.append("\n");

		System.out.println(var6);
		//JOptionPane.showMessageDialog(null, var6, "Minecraft has crashed!", JOptionPane.ERROR_MESSAGE);
	}
}
