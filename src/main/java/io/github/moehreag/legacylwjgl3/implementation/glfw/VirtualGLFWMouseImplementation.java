package io.github.moehreag.legacylwjgl3.implementation.glfw;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.implementation.input.MouseImplementation;
import io.github.moehreag.legacylwjgl3.util.GlStateManager;
import io.github.moehreag.legacylwjgl3.util.TextureUtil;
import io.github.moehreag.legacylwjgl3.util.XDGPathResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.class_564;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.client.render.Tessellator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;

/**
 * @author moehreag
 * <p>2023/12/18</p>
 */
public class VirtualGLFWMouseImplementation implements MouseImplementation {
	private static final Logger LOGGER = LogManager.getLogger("Virtual Mouse");
	private static final VirtualGLFWMouseImplementation INSTANCE = new VirtualGLFWMouseImplementation();
	private final EventQueue event_queue = new EventQueue(Mouse.EVENT_SIZE);
	private final ByteBuffer tmp_event = ByteBuffer.allocate(Mouse.EVENT_SIZE);
	private final List<XCursor.ImageChunk> chunks = new ArrayList<>();
	protected GLFWMouseButtonCallback buttonCallback;
	protected byte[] button_states = new byte[this.getButtonCount()];
	private GLFWCursorPosCallback posCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWCursorEnterCallback cursorEnterCallback;
	private long windowHandle;
	private boolean grabbed;
	private boolean isInsideWindow;
	private double last_x;
	private double last_y;
	private double accum_dx, accum_dy, accum_dz;
	private double virt_offset_x, virt_offset_y;
	private int current;
	private int[] images = new int[]{-1};
	private long animationTime;
	private boolean virtual;
	private boolean created;

	public static VirtualGLFWMouseImplementation getInstance() {
		return INSTANCE;
	}

	public static void render() {
		getInstance().draw();
	}

	@Override
	public void createMouse() {
		this.windowHandle = Display.getHandle();

		if (GLFW.glfwRawMouseMotionSupported() && !Mouse.getPrivilegedBoolean("org.lwjgl.input.Mouse.disableRawInput"))
			GLFW.glfwSetInputMode(this.windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);

		this.buttonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
			byte state = action == GLFW.GLFW_PRESS ? (byte) 1 : (byte) 0;
			putMouseEvent((byte) button, state, 0, System.nanoTime());
			if (button < button_states.length)
				button_states[button] = state;
		});
		this.posCallback = GLFWCursorPosCallback.create((window, xpos, ypos) -> {
			synchronized (VirtualGLFWMouseImplementation.this) {
				if (!created) {
					created = true;
					setup();
				}
			}
			int x = (int) xpos;
			int y = Display.getHeight() - (int) ypos; // I don't know why but this un-inverts the y motion of mouse inputs
			double dx = x - last_x;
			double dy = y - last_y;
			if (dx != 0 || dy != 0) {
				accum_dx += dx;
				accum_dy += dy;

				last_x = x;
				last_y = y;

				if (virtual) {

					/*
					 * Stop the virtual cursor from leaving the screen entirely
					 */
					while (getX() <= 0) { // TODO get rid if the loops. loops are bad here.
						virt_offset_x++;
					}
					while (getX() > Display.getWidth()) {
						virt_offset_x--;
					}

					while (getY() < 0) {
						virt_offset_y++;
					}
					while (getY() > Display.getHeight() - 1) {
						virt_offset_y--;
					}
				}

				long nanos = System.nanoTime();
				if (grabbed) {
					putMouseEventWithCoords((byte) -1, (byte) 0, dx, dy, 0, nanos);
				} else {
					putMouseEventWithCoords((byte) -1, (byte) 0, x, y, 0, nanos);
				}
			}


		});
		this.scrollCallback = GLFWScrollCallback.create((window, xoffset, yoffset) -> {
			accum_dz += yoffset;
			putMouseEvent((byte) -1, (byte) 0, (int) yoffset, System.nanoTime());
		});
		this.cursorEnterCallback = GLFWCursorEnterCallback.create((window, entered) -> {
			this.isInsideWindow = entered;
		});

		GLFW.glfwSetMouseButtonCallback(this.windowHandle, this.buttonCallback);
		GLFW.glfwSetCursorPosCallback(this.windowHandle, this.posCallback);
		GLFW.glfwSetScrollCallback(this.windowHandle, this.scrollCallback);
		GLFW.glfwSetCursorEnterCallback(this.windowHandle, this.cursorEnterCallback);


		created = false;
	}

	private boolean mayVirtualize() {
		return LegacyLWJGL3.getMinecraft().world != null;
	}

	/*
	 * whether we are on a screen where the virtual cursor is allowed
	 */
	private boolean isValidScreen() {
		Screen s = LegacyLWJGL3.getMinecraft().currentScreen;
		return s instanceof ContainerScreen || s instanceof ChatScreen;
	}

	private void setup() {
		virt_offset_x = 0;
		virt_offset_y = 0;
		loadCursor();
	}

	protected void putMouseEvent(byte button, byte state, int dz, long nanos) {
		if (grabbed)
			putMouseEventWithCoords(button, state, 0, 0, dz, nanos);
		else
			putMouseEventWithCoords(button, state, getX(), getY(), dz, nanos);
	}

	protected void putMouseEventWithCoords(byte button, byte state, double coord1, double coord2, int dz, long nanos) {
		tmp_event.clear();
		tmp_event.put(button).put(state).putDouble(coord1).putDouble(coord2).putDouble(dz).putLong(nanos);
		tmp_event.flip();
		event_queue.putEvent(tmp_event);
	}

	/*
	 * deconstruct everything and free native memory as well as cursor textures
	 */
	@Override
	public void destroyMouse() {
		this.buttonCallback.free();
		this.posCallback.free();
		this.scrollCallback.free();
		this.cursorEnterCallback.free();
		if (images[0] != 0) {
			for (int i : images) {
				GlStateManager.deleteTexture(i);
			}
			images = new int[]{-1};
		}
	}

	private void reset() {
		this.event_queue.clearEvents();
		accum_dx = accum_dy = 0;
	}

	@Override
	public void pollMouse(DoubleBuffer coord_buffer, ByteBuffer buttons_buffer) {
		if (grabbed) {
			coord_buffer.put(0, accum_dx);
			coord_buffer.put(1, accum_dy);
		} else {
			coord_buffer.put(0, getX());
			coord_buffer.put(1, getY());
		}
		coord_buffer.put(2, accum_dz);
		accum_dx = accum_dy = accum_dz = 0;
		for (int i = 0; i < button_states.length; i++)
			buttons_buffer.put(i, button_states[i]);
	}

	@Override
	public void readMouse(ByteBuffer readBuffer) {
		event_queue.copyEvents(readBuffer);
	}

	/*
	 * Set the cursor position (of the virtual cursor)
	 */
	@Override
	public void setCursorPosition(double x, double y) {
		virt_offset_y = y - last_y;
		virt_offset_x = x - last_x;
	}

	/*
	 * (un)grabs the mouse and activates the virtual cursor if necessary
	 */
	@Override
	public void grabMouse(boolean grab) {
		if (grab) {
			GLFW.glfwSetInputMode(this.windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

			virtual = false;
		} else {
			if (isValidScreen() && mayVirtualize()) {
				//virt_offset_x = virt_offset_y = 0;
				virtual = true;
			} else {
				GLFW.glfwSetInputMode(this.windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
			}
		}
		this.grabbed = grab;
		this.reset();
	}

	@Override
	public boolean hasWheel() {
		return true;
	}

	@Override
	public int getButtonCount() {
		return GLFW.GLFW_MOUSE_BUTTON_LAST + 1;
	}

	@Override
	public boolean isInsideWindow() {
		return this.isInsideWindow;
	}

	/*
	 * Draw the virtual cursor
	 */
	private void draw() {
		if (virtual && images[0] != -1) {
			GlStateManager.enableTexture();
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			GlStateManager.disableLighting();
			GlStateManager.color3f(1, 1, 1);
			GlStateManager.bindTexture(images[current]);

			float scale = new class_564(LegacyLWJGL3.getMinecraft().options, LegacyLWJGL3.getMinecraft().displayWidth, LegacyLWJGL3.getMinecraft().displayHeight).field_2391;
			double x = getX();
			double y = getY();
			drawTexture((x - getCurrent().xhot) / scale, (Display.getHeight() - y - getCurrent().yhot) / scale, getCurrent().width / scale, getCurrent().height / scale, getCurrent().width / scale, getCurrent().height / scale);

			advanceAnimation();
		}
	}

	private void drawTexture(double x, double y, double width, double height, double textureWidth, double textureHeight) {
		double n = 1.0F / textureWidth;
		double o = 1.0F / textureHeight;
		double z = 1000;
		Tessellator bufferBuilder = Tessellator.INSTANCE;
		bufferBuilder.start(7);
		bufferBuilder.vertex(x, y + height, z, 0, height * o);
		bufferBuilder.vertex(x + width, y + height, z, width * n, height * o);
		bufferBuilder.vertex(x + width, y, z, width * n, 0);
		bufferBuilder.vertex(x, y, z, 0, 0);
		bufferBuilder.draw();
	}

	private void advanceAnimation() {
		if (images.length > 1) {
			if (animationTime == 0 || System.currentTimeMillis() - animationTime > getCurrent().delay) {
				animationTime = System.currentTimeMillis();
				current++;
				if (current >= images.length) {
					current = 0;
				}
			}
		}
	}

	private XCursor.ImageChunk getCurrent() {
		return chunks.get(current);
	}

	/*
	 * Load the virtual cursor
	 */
	private void loadCursor() {
		XCursor cursor = SystemCursor.load();

		if (Boolean.getBoolean("virtual_mouse.export")) {
			cursor.export();
		}

		chunks.clear();
		for (XCursor.Chunk chunk : cursor.chunks) {
			if (chunk instanceof XCursor.ImageChunk) {
				XCursor.ImageChunk c = (XCursor.ImageChunk) chunk;
				if (c.getSubtype() == XDGPathResolver.getCursorSize()) {
					chunks.add(c);
				}
			}
		}

		current = 0;
		images = new int[chunks.size()];
		for (int i = 0; i < images.length; i++) {
			XCursor.ImageChunk c = chunks.get(i);
			int id = TextureUtil.genTextures();
			images[i] = id;
			TextureUtil.prepare(id, (int) c.width, (int) c.height);
			TextureUtil.uploadTexture(id, c.getImage(), (int) c.width, (int) c.height);
		}
	}

	private double getX() {
		return virtual ? last_x + virt_offset_x : last_x;
	}

	private double getY() {
		return virtual ? last_y + virt_offset_y : last_y;
	}

	private static class SystemCursor {

		private static final SystemCursor INSTANCE = new SystemCursor();

		/*
		 * Loads the cursor file and parse it
		 */
		public static XCursor load() {

			try {
				byte[] c = LegacyLWJGL3.toByteArray(INSTANCE.getArrowCursor());


				ByteBuffer buf = ByteBuffer.wrap(c);

				return XCursor.parse(buf);

			} catch (IOException e) {
				throw new IllegalStateException("Unable to load cursor texture!", e);
			}

		}

		private InputStream getArrowCursor() throws IOException {
			Path theme = XDGPathResolver.getIconTheme("cursors/left_ptr"); // load the arrow pointer cursor from the selected theme
			if (theme != null) {
				LOGGER.info("Loading system cursor: " + theme);
				return Files.newInputStream(theme);
			}

			LOGGER.info("Falling back to packaged cursor");
			return this.getClass().getResourceAsStream("/assets/virtual_cursor/default");
		}
	}

	@Data
	private static class XCursor {

		private final String magic;
		private final long headerLength;
		private final long fileVersion;
		private final long toCEntryCount;
		private final TableOfContents[] toC;
		private final Chunk[] chunks;

		/**
		 * Parse a cursor icon file
		 *
		 * @param buf the data of the file
		 * @return a parsed cursor object
		 */
		public static XCursor parse(ByteBuffer buf) {

			String magic = getString(buf, 4);
			if (!"Xcur".equals(magic)) {
				throw new IllegalArgumentException("Not an Xcursor file! Magic: " + magic);
			}

			long headerLength = getInt(buf);
			long version = getInt(buf);
			long ntoc = getInt(buf);

			TableOfContents[] toc = new TableOfContents[(int) ntoc];
			Chunk[] chunks = new Chunk[(int) ntoc];

			for (int i = 0; i < ntoc; i++) {
				/*
				 * Procedure:
				 *  - read a table of contents
				 *  - read the corresponding chunk from the file without modifying the buffer's indices
				 *  - repeat until all tables are read
				 */
				TableOfContents table = new TableOfContents(getInt(buf), getInt(buf), getInt(buf));
				toc[i] = table;
				chunks[i] = parseChunk(buf, table);
			}

			return new XCursor(magic, headerLength, version, ntoc, toc, chunks);
		}

		/*
		 * read a chunk from a corresponding table
		 */
		private static Chunk parseChunk(ByteBuffer buf, TableOfContents table) {
			int pos = buf.position();
			buf.position((int) table.position);
			Chunk c;
			switch ((int) table.type) {
				case 0xfffe0001: // Comment
					c = parseComment(buf, table); // I have yet to find a single cursor file that uses these, not even `xcursorgen` supports them.
					break;
				case 0xfffd0002: // Image
					c = parseImage(buf, table);
					break;
				default:
					throw new IllegalArgumentException("Unrecognized type: " + table.type);
			}
			buf.position(pos);
			return c;
		}

		/*
		 * parse an image chunk
		 */
		private static Chunk parseImage(ByteBuffer buf, TableOfContents table) {
			long size = getInt(buf);
			if (size != 36) {
				throw new IllegalArgumentException("not an image chunk! size != 36: " + size);
			}

			long type = getInt(buf);
			if (type != 0xfffd0002L || type != table.type) {
				throw new IllegalArgumentException("not an image chunk! type != image: " + type);
			}

			long subtype = getInt(buf);
			if (subtype != table.subtype) {
				throw new IllegalArgumentException("not an image chunk! subtype != table.subtype: " + subtype);
			}
			long version = getInt(buf);

			long width = getInt(buf);

			if (width > 0x7ff) {
				throw new IllegalArgumentException("image too large! width > 0x7ff: " + width);
			}

			long height = getInt(buf);
			if (height > 0x7ff) {
				throw new IllegalArgumentException("image too large! height > 0x7ff: " + height);
			}
			long xhot = getInt(buf);
			if (xhot > width) {
				throw new IllegalArgumentException("xhot outside image!: " + xhot);
			}
			long yhot = getInt(buf);
			if (yhot > height) {
				throw new IllegalArgumentException("yhot outside image!: " + yhot);
			}
			long delay = getInt(buf);

			long[] pixels = new long[(int) (width * height)];

			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = getInt(buf);
			}

			return new ImageChunk(size, type, subtype, version, width, height, xhot, yhot, delay, pixels);
		}

		/*
		 * parse a comment chunk
		 */
		private static Chunk parseComment(ByteBuffer buf, TableOfContents table) {
			long size = getInt(buf);
			if (size != 20) {
				throw new IllegalArgumentException("not a comment chunk! size != 20: " + size);
			}

			long type = getInt(buf);
			if (type != 0xfffe0001L || type != table.type) {
				throw new IllegalArgumentException("not a comment chunk! type != comment: " + type);
			}

			long subtype = getInt(buf);
			if (subtype != table.subtype) {
				throw new IllegalArgumentException("not a comment chunk! subtype != table.subtype: " + subtype);
			}
			long version = getInt(buf);
			long commentLength = getInt(buf);
			String comment = getString(buf, (int) commentLength);
			return new CommentChunk(size, type, subtype, version, commentLength, comment);
		}

		private static long getInt(ByteBuffer buf) {
			return readUnsignedInteger(buf).longValue();
		}

		private static BigInteger readUnsignedInteger(ByteBuffer buffer) {
			return new BigInteger(1, readBytes(buffer));
		}

		private static byte[] readBytes(ByteBuffer buffer) {
			byte[] bytes = new byte[4];
			for (int i = 0; i < 4; i++) {
				bytes[4 - 1 - i] = buffer.get();
			}
			return bytes;
		}

		private static String getString(ByteBuffer buf, int length) {
			byte[] data = new byte[length];
			for (int i = 0; i < length; i++) {
				data[i] = buf.get();
			}
			return new String(data, StandardCharsets.UTF_8);
		}

		/*
		 * export the cursor files
		 */
		public void export() {

			Path dir = Paths.get("cursors");
			try {
				Files.walkFileTree(dir,
						new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult postVisitDirectory(
									Path dir, IOException exc) throws IOException {
								Files.delete(dir);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFile(
									Path file, BasicFileAttributes attrs)
									throws IOException {
								Files.delete(file);
								return FileVisitResult.CONTINUE;
							}
						});
				Files.createDirectory(dir);
			} catch (IOException e) {
				LOGGER.warn("Failed to create clean export directory, export will likely fail!", e);
			}

			LOGGER.info("Exporting chunks..");
			for (Chunk c : chunks) {
				c.export();
			}
		}

		@Data
		private static class TableOfContents {
			private final long type;
			private final long subtype; // type-specific label - image size
			private final long position; // byte position in the file;
		}

		@Data
		private abstract static class Chunk {
			private final long length; // header length
			private final long type, subtype; // must match Table of Contents
			private final long version; // Chunk type version

			public Chunk(long length, long type, long subtype, long version) {
				this.length = length;
				this.type = type;
				this.subtype = subtype;
				this.version = version;
			}

			public abstract void export();
		}

		/*
		 * Comment header size: 20 bytes
		 *
		 * Type: 0xfffe0001
		 * subtype: 1 (COPYRIGHT), 2 (LICENSE), 3 (OTHER)
		 * version: 1
		 */
		@Getter
		@ToString
		@EqualsAndHashCode(callSuper = true)
		private static class CommentChunk extends Chunk {

			private final long length;
			private final String comment;

			public CommentChunk(long length, long type, long subtype, long version, long commentLength, String comment) {
				super(length, type, subtype, version);
				this.length = commentLength;
				this.comment = comment;
			}

			public void export() {

				String name;
				if (getSubtype() == 1) {
					name = "COPYRIGHT";
				} else if (getSubtype() == 2) {
					name = "LICENSE";
				} else {
					name = "COMMENT";
				}

				try {
					Files.write(Paths.get("cursors", name), comment.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
				} catch (IOException e) {
					LOGGER.warn("Image export failed!", e);
				}

			}
		}

		/*
		 * Image header size: 36 bytes
		 *
		 * Type: 0xfffd002
		 * subtype: image size
		 * version: 1
		 *
		 */
		@Getter
		@ToString
		@EqualsAndHashCode(callSuper = true)
		private static class ImageChunk extends Chunk {

			private final long width, height, xhot, yhot;
			private final long delay;
			private final long[] pixels;

			public ImageChunk(long length, long type, long subtype, long version, long width, long height, long xhot, long yhot, long delay, long[] pixels) {
				super(length, type, subtype, version);
				this.width = width;
				this.height = height;
				this.xhot = xhot;
				this.yhot = yhot;
				this.delay = delay;
				this.pixels = pixels;
			}

			public int[] getImage() {
				int[] data = new int[pixels.length];

				for (int i = 0; i < pixels.length; i++) {
					data[i] = (int) pixels[i];
				}

				return data;
			}

			public void export() {

				BufferedImage im = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

				int[] data = getImage();

				for (int i = 0; i < data.length; i++) {
					im.setRGB((int) (i % width), (int) (i / height), data[i]);
				}


				List<String> lines = new ArrayList<>();
				lines.add("[Sizes]");
				lines.add("Cursor: " + getSubtype() + "x" + getSubtype());
				lines.add("Image: " + width + "x" + height);
				lines.add("[Hotspots]");
				lines.add("X: " + xhot);
				lines.add("Y: " + yhot);
				lines.add("[Delay]");
				lines.add("" + delay);


				String imageName = getSubtype() + "x" + getSubtype();
				String name = imageName;
				if (delay != 0) {

					int i = 0;
					name = imageName + "_" + i;
					while (new File("cursors", name + ".png").exists()) {
						i++;
						name = imageName + "_" + i;
					}


				}

				String cursor = (getSubtype() + " " + xhot + " " + yhot + " " + name + ".png");
				Path cursorFile = Paths.get("cursors", "cursor.cursor");
				if (delay != 0) {
					cursor += " " + delay;
					if (Files.exists(cursorFile)) {
						cursor = "\n" + cursor;
					}
				}

				try {
					ImageIO.write(im, "png", new File("cursors", name + ".png"));
					Files.write(Paths.get("cursors", name + ".txt"), lines, StandardOpenOption.CREATE);
					Files.write(cursorFile, cursor.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				} catch (IOException e) {
					LOGGER.warn("Image export failed!", e);
				}
			}
		}
	}
}
