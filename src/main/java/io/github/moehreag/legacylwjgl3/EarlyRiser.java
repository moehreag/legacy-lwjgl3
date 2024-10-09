package io.github.moehreag.legacylwjgl3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.github.moehreag.legacylwjgl3.util.Triple;
import javassist.*;
import javassist.util.proxy.DefineClassHelper;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;

/**
 * Uses gross hacks to "redefine" classes that I couldn't find a way to edit with mixins.
 * <p>
 * Uses javassist to edit the classes e.x adding fields and methods for compatibility with legacy LWJGL2 code.
 *
 * @author Zarzelcow
 */
public class EarlyRiser implements Runnable {

	// list of legacy methods that we need to add to GL11. moved out of method for readability
	private final List<Triple<String, String, String>> gl11Translations = Arrays.asList(
			Triple.of("glGetFloat", "glGetFloatv", "(ILjava/nio/FloatBuffer;)V"),
			Triple.of("glGetInteger", "glGetIntegerv", "(ILjava/nio/IntBuffer;)V"),
			Triple.of("glFog", "glFogfv", "(ILjava/nio/FloatBuffer;)V"),
			Triple.of("glLight", "glLightfv", "(IILjava/nio/FloatBuffer;)V"),
			Triple.of("glLightModel", "glLightModelfv", "(ILjava/nio/FloatBuffer;)V"),
			Triple.of("glMultMatrix", "glMultMatrixf", "(Ljava/nio/FloatBuffer;)V"),
			Triple.of("glTexEnv", "glTexEnvfv", "(IILjava/nio/FloatBuffer;)V"),
			Triple.of("glTexGen", "glTexGenfv", "(IILjava/nio/FloatBuffer;)V")
	);
	private final List<Triple<String, String, String>> al10Translations = Arrays.asList(
			Triple.of("alListener", "alListenerfv", "(ILjava/nio/FloatBuffer;)V"),
			Triple.of("alSource", "alSourcefv", "(IILjava/nio/FloatBuffer;)V"),
			Triple.of("alSourceStop", "alSourceStopv", "(Ljava/nio/IntBuffer;)V")
	);
	private final List<Triple<String, String, String>> gl20Translations = Arrays.asList(
			Triple.of("glUniformMatrix4", "glUniformMatrix4fv", "(IZLjava/nio/FloatBuffer;)V"),
			Triple.of("glUniform3", "glUniform3fv", "(ILjava/nio/FloatBuffer;)V"),
			Triple.of("glUniform1", "glUniform1iv", "(ILjava/nio/IntBuffer;)V")
	);
	private final List<Triple<String, String, String>> arbShaderObjectsTranslations = Arrays.asList(
			Triple.of("glGetObjectParameterARB", "glGetObjectParameterivARB", "(IILjava/nio/IntBuffer;)V"),
			Triple.of("glUniformMatrix4ARB", "glUniformMatrix4fvARB", "(IZLjava/nio/FloatBuffer;)V")
	);
	private final List<Triple<String, String, String>> arbOcclusionQueryTranslations = Arrays.asList(
			Triple.of("glGetQueryObjectuARB", "glGetQueryObjectuivARB", "(IILjava/nio/IntBuffer;)V")
	);

	@Override
	public void run() {
		LegacyLWJGL3.LOGGER.debug("EarlyRiser running");
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(FabricLauncherBase.getLauncher().getTargetClassLoader())); // knot class loader contains the fat jar with lwjgl 3 bundled

		macroRedefineWithErrorHandling(pool, this::addMissingGLCapabilities);
		macroRedefineWithErrorHandling(pool, this::addLegacyCompatibilityMethodsToGL11);
		macroRedefineWithErrorHandling(pool, this::addLegacyCompatibilityMethodsToGL20);
		macroTranslateMethodNames(pool, "org.lwjgl.opengl.ARBShaderObjects", GL.class, arbShaderObjectsTranslations);
		macroTranslateMethodNames(pool, "org.lwjgl.opengl.ARBOcclusionQuery", GL.class, arbOcclusionQueryTranslations);
		macroRedefineWithErrorHandling(pool, this::copyAlExtensions);
		macroTranslateMethodNames(pool, "org.lwjgl.openal.AL10", ALCapabilities.class, al10Translations);
	}

	private void macroTranslateMethodNames(ClassPool pool, String clazz, Class<?> neighbor, List<Triple<String, String, String>> methodNames) {
		try {
			addMethodTranslations(pool, clazz, neighbor, methodNames);
		} catch (Exception e) {
			LegacyLWJGL3.LOGGER.error("Failed in early riser while attempting to do hacky things", e);
		}
	}

	// rather than handling errors is every method manually, this will do it for you
	private void macroRedefineWithErrorHandling(ClassPool pool, Redefiner method) {
		try {
			method.redefine(pool);
		} catch (Exception e) {
			LegacyLWJGL3.LOGGER.error("Failed in early riser while attempting to do hacky things", e);
		}
	}

	// Adds missing extension checks from LWJGL2 for use in ContextCapabilities
	private void addMissingGLCapabilities(ClassPool classPool) throws NotFoundException, CannotCompileException, IOException {
		CtClass ctClass = classPool.get("org.lwjgl.opengl.GLCapabilities");

		ctClass.addField(CtField.make("public final boolean GL_EXT_multi_draw_arrays;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_paletted_texture;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_rescale_normal;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_texture_3d;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_texture_lod_bias;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_vertex_shader;", ctClass));
		ctClass.addField(CtField.make("public final boolean GL_EXT_vertex_weighting;", ctClass));
		CtConstructor constructor =
				ctClass.getConstructor("(Lorg/lwjgl/system/FunctionProvider;Ljava/util/Set;ZLjava/util/function/IntFunction;)V");
		constructor.insertAfter(
				"GL_EXT_multi_draw_arrays = ext.contains(\"GL_EXT_multi_draw_arrays\");" +
				"GL_EXT_paletted_texture = ext.contains(\"GL_EXT_paletted_texture\");" +
				"GL_EXT_rescale_normal = ext.contains(\"GL_EXT_rescale_normal\"); " +
				"GL_EXT_texture_3d = ext.contains(\"GL_EXT_texture_3d\");\n" +
				"GL_EXT_texture_lod_bias = ext.contains(\"GL_EXT_texture_lod_bias\");\n" +
				"GL_EXT_vertex_shader = ext.contains(\"GL_EXT_vertex_shader\");\n" +
				"GL_EXT_vertex_weighting = ext.contains(\"GL_EXT_vertex_weighting\");".trim());
		defineCtClass(ctClass, GL.class, classPool.getClassLoader());
	}


	// new GL20 doesn't have a way to supply a shader source using ByteBuffer so this adds a method to do it
	private void addLegacyCompatibilityMethodsToGL20(ClassPool classPool) throws NotFoundException, CannotCompileException, IOException {
		CtClass cc = classPool.get("org.lwjgl.opengl.GL20");
		String code = "public static void glShaderSource(int shader, java.nio.ByteBuffer string) {" +
					  "byte[] data = new byte[string.limit()];" +
					  "string.position(0);" +
					  "string.get(data);" +
					  "string.position(0);" +
					  "org.lwjgl.opengl.GL20.glShaderSource(shader, new String(data));" +
					  "}";

		cc.addMethod(CtNewMethod.make(code, cc));
		macroTranslateMethodNames(classPool, "org.lwjgl.opengl.GL20", GL.class, gl20Translations);
	}

	private void addLegacyCompatibilityMethodsToGL11(ClassPool pool) throws Exception {
		CtClass cc = pool.get("org.lwjgl.opengl.GL11");
		String[] methods = new String[]{
				"public static void glTexCoordPointer(int i, int stride, java.nio.FloatBuffer pointer){" +
				"glTexCoordPointer(i, 0x1406, stride, pointer);" + // 0x1404 = INT
				"}",
				"public static void glTexCoordPointer(int i, int stride, java.nio.ShortBuffer pointer){" +
				"glTexCoordPointer(i, 0x1402, stride, pointer);" + // 0x1402 = SHORT
				"}",
				"public static void glColorPointer(int i, boolean bl, int i2, java.nio.ByteBuffer pointer){" +
				"glColorPointer(i, 0x1401, i2, pointer);" +
				"}",
				"public static void glVertexPointer(int i, int i2, java.nio.FloatBuffer pointer){" +
				"glVertexPointer(i, 0x1406, i2, pointer);" +
				"}",
				"public static void glNormalPointer(int stride, java.nio.ByteBuffer pointer){" +
				"glNormalPointer(0x1400, stride, pointer);" +
				"}"
		};

		for (String method : methods) {
			cc.addMethod(CtNewMethod.make(method, cc));
		}

		macroTranslateMethodNames(pool, "org.lwjgl.opengl.GL11", GL.class, gl11Translations);
	}

	/**
	 * It copies all the methods and fields from the extension class to the real class
	 */
	private void copyAlExtensions(ClassPool classPool) throws NotFoundException, CannotCompileException, IOException {
		CtClass extension = classPool.get("io.github.moehreag.legacylwjgl3.ALExtensions");
		CtClass target = classPool.get("org.lwjgl.openal.AL");
		// this code is hacky, but it replaces the stub method with the real one
		target.getMethod("destroy", "()V")
				.setName("al_destroy"); // rename destroy to al_destroy so it doesnt conflict
		extension.removeMethod(
				extension.getMethod(
						"al_destroy",
						"()V"
				)
		); // remove stub method from extension so it calls the real one

		// copy methods and felds from cc to target
		for (CtMethod method : extension.getDeclaredMethods()) {
			CtMethod copied = CtNewMethod.copy(method, target, null);
			// dont add method if it already exists
			target.addMethod(copied);
			debug("Added AL extension method ${method.name}");
		}

		for (CtField field : extension.getDeclaredFields()) {
			CtField copied = new CtField(field, target);
			target.addField(copied);
			debug("Added AL extension field ${field.name}");
		}
		defineCtClass(target, ALCapabilities.class, classPool.getClassLoader());
	}

	private void addMethodTranslations(ClassPool classPool, String clazz, Class<?> neighbor, List<Triple<String, String, String>> methodNames) throws NotFoundException, CannotCompileException, IOException {
		CtClass cc = classPool.get(clazz);
		for (Triple<String, String, String> t : methodNames) {
			CtMethod original = cc.getMethod(t.getMiddle(), t.getRight());
			// copy original method and rename it
			CtMethod copied = CtNewMethod.copy(original, cc, null);
			copied.setName(t.getLeft());
			cc.addMethod(copied);

			debug("Added legacy compat method " + t.getLeft());
		}
		defineCtClass(cc, neighbor, classPool.getClassLoader());
	}

	private void debug(String message) {
		LegacyLWJGL3.LOGGER.debug(message);
	}

	// all the Javassist [CtClass.toClass] methods use new java features,
	// DefineClassHelper.toClass has support for much older versions of java so use that instead
	private void defineCtClass(CtClass cc, Class<?> neighbor, ClassLoader classLoader) throws IOException, CannotCompileException {
		DefineClassHelper.toClass(
				cc.getName(),
				neighbor,
				classLoader,
				null,
				cc.toBytecode()
		);
		cc.detach();
	}

	private interface Redefiner {
		void redefine(ClassPool pool) throws Exception;
	}
}
