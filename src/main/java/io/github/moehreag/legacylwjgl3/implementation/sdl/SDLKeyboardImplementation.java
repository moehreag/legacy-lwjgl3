package io.github.moehreag.legacylwjgl3.implementation.sdl;

import java.nio.ByteBuffer;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.implementation.input.KeyboardImplementation;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.EventQueue;
import org.lwjgl.opengl.SDLDisplay;
import org.lwjgl.sdl.*;

import static org.lwjgl.sdl.SDLEvents.*;

public class SDLKeyboardImplementation implements KeyboardImplementation {

	private final byte[] key_down_buffer = new byte[Keyboard.KEYBOARD_SIZE];
	private final EventQueue event_queue = new EventQueue(Keyboard.EVENT_SIZE);

	private final ByteBuffer tmp_event = ByteBuffer.allocate(Keyboard.EVENT_SIZE);
	private final SDL_KeyboardEvent keyboardEvent = SDLDisplay.getInstance().getEvent().key();
	private final SDL_TextEditingEvent textEditingEvent = SDLDisplay.getInstance().getEvent().edit();
	private final SDL_TextInputEvent textInputEvent = SDLDisplay.getInstance().getEvent().text();

	@Override
	public void createKeyboard() {

	}

	private void putKeyboardEvent(int keycode, byte state, int ch, long nanos, boolean repeat) {
		if (keycode == -1) {
			ByteBuffer lastEvent = event_queue.getLastEvent();

			if (lastEvent.getInt(0) > 0 && lastEvent.getInt(5) == 0) {
				lastEvent.putInt(5, ch);
				return;
			}
		}

		this.tmp_event.clear();
		this.tmp_event.putInt(keycode).put(state).putInt(ch).putLong(nanos).put(repeat ? (byte) 1 : (byte) 0);
		this.tmp_event.flip();
		this.event_queue.putEvent(this.tmp_event);
	}

	@Override
	public void destroyKeyboard() {

	}

	@Override
	public void pollKeyboard(ByteBuffer keyDownBuffer) {
		int old_position = keyDownBuffer.position();
		keyDownBuffer.put(this.key_down_buffer);
		keyDownBuffer.position(old_position);
	}

	@Override
	public void readKeyboard(ByteBuffer readBuffer) {
		event_queue.copyEvents(readBuffer);
	}

	public void processKeyboardEvent(SDL_Event event) {
		switch (event.type()) {
			case SDL_EVENT_KEY_DOWN, SDL_EVENT_KEY_UP -> {
				int key = translateKeyFromSDL(keyboardEvent.scancode());
				if (keyboardEvent.down()) {
					this.key_down_buffer[key] = 1;
				} else {
					this.key_down_buffer[key] = 0;
				}
				putKeyboardEvent(key, this.key_down_buffer[key], 0, System.nanoTime(), keyboardEvent.repeat());
			}
			case SDL_EVENT_TEXT_EDITING -> textEditingEvent.textString().codePoints().forEach(codepoint ->
					putKeyboardEvent(-1, (byte) 1, codepoint, textEditingEvent.timestamp(), false));
			case SDL_EVENT_TEXT_INPUT -> textInputEvent.textString().codePoints().forEach(codepoint ->
					putKeyboardEvent(-1, (byte) 1, codepoint, textEditingEvent.timestamp(), false));
		}
	}

	private int translateKeyFromSDL(int key) {
		if (key < 0) key = SDLKeycode.SDLK_UNKNOWN;
		if (!SDL_SCANCODE2LWJGL.containsKey(key)) {
			if (key < key_down_buffer.length) {
				return key;
			}
			LegacyLWJGL3.LOGGER.warn("Untranslated key: {} ({})", key, SDLKeyboard.SDL_GetScancodeName(key));
			return Keyboard.KEY_NONE;
		}
		return SDL_SCANCODE2LWJGL.get(key);
	}

	private static final Int2IntMap SDL_SCANCODE2LWJGL = new Int2IntOpenHashMap();

	static {
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_UNKNOWN, Keyboard.KEY_NONE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_SPACE, Keyboard.KEY_SPACE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_COMMA, Keyboard.KEY_COMMA);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_MINUS, Keyboard.KEY_MINUS);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_PERIOD, Keyboard.KEY_PERIOD);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_SLASH, Keyboard.KEY_SLASH);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_0, Keyboard.KEY_0);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_1, Keyboard.KEY_1);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_2, Keyboard.KEY_2);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_3, Keyboard.KEY_3);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_4, Keyboard.KEY_4);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_5, Keyboard.KEY_5);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_6, Keyboard.KEY_6);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_7, Keyboard.KEY_7);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_8, Keyboard.KEY_8);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_9, Keyboard.KEY_9);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_SEMICOLON, Keyboard.KEY_SEMICOLON);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_EQUALS, Keyboard.KEY_EQUALS);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_A, Keyboard.KEY_A);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_B, Keyboard.KEY_B);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_C, Keyboard.KEY_C);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_D, Keyboard.KEY_D);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_E, Keyboard.KEY_E);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F, Keyboard.KEY_F);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_G, Keyboard.KEY_G);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_H, Keyboard.KEY_H);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_I, Keyboard.KEY_I);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_J, Keyboard.KEY_J);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_K, Keyboard.KEY_K);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_L, Keyboard.KEY_L);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_M, Keyboard.KEY_M);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_N, Keyboard.KEY_N);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_O, Keyboard.KEY_O);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_P, Keyboard.KEY_P);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_Q, Keyboard.KEY_Q);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_R, Keyboard.KEY_R);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_S, Keyboard.KEY_S);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_T, Keyboard.KEY_T);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_U, Keyboard.KEY_U);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_V, Keyboard.KEY_V);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_W, Keyboard.KEY_W);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_X, Keyboard.KEY_X);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_Y, Keyboard.KEY_Y);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_Z, Keyboard.KEY_Z);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LEFTBRACKET, Keyboard.KEY_LBRACKET);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_BACKSLASH, Keyboard.KEY_BACKSLASH);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RIGHTBRACKET, Keyboard.KEY_RBRACKET);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_GRAVE, Keyboard.KEY_GRAVE);
		//GLFW2LWJGL.put(SDLScancode.SDL_SCANCODE_, Keyboard.KEY_WORLD_1);
		//GLFW2LWJGL.put(SDLScancode.SDL_SCANCODE_WORLD_2, Keyboard.KEY_WORLD_2);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_ESCAPE, Keyboard.KEY_ESCAPE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RETURN, Keyboard.KEY_RETURN);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_TAB, Keyboard.KEY_TAB);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_BACKSPACE, Keyboard.KEY_BACK);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_INSERT, Keyboard.KEY_INSERT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_DELETE, Keyboard.KEY_DELETE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RIGHT, Keyboard.KEY_RIGHT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LEFT, Keyboard.KEY_LEFT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_DOWN, Keyboard.KEY_DOWN);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_UP, Keyboard.KEY_UP);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_PAGEUP, Keyboard.KEY_PRIOR);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_PAGEDOWN, Keyboard.KEY_NEXT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_HOME, Keyboard.KEY_HOME);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_END, Keyboard.KEY_END);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_CAPSLOCK, Keyboard.KEY_CAPITAL);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_SCROLLLOCK, Keyboard.KEY_SCROLL);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_NUMLOCKCLEAR, Keyboard.KEY_NUMLOCK);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_PRINTSCREEN, Keyboard.KEY_PRINT_SCREEN);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_PAUSE, Keyboard.KEY_PAUSE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F1, Keyboard.KEY_F1);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F2, Keyboard.KEY_F2);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F3, Keyboard.KEY_F3);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F4, Keyboard.KEY_F4);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F5, Keyboard.KEY_F5);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F6, Keyboard.KEY_F6);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F7, Keyboard.KEY_F7);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F8, Keyboard.KEY_F8);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F9, Keyboard.KEY_F9);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F10, Keyboard.KEY_F10);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F11, Keyboard.KEY_F11);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F12, Keyboard.KEY_F12);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F13, Keyboard.KEY_F13);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F14, Keyboard.KEY_F14);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F15, Keyboard.KEY_F15);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F16, Keyboard.KEY_F16);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F17, Keyboard.KEY_F17);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F18, Keyboard.KEY_F18);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F19, Keyboard.KEY_F19);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F20, Keyboard.KEY_F20);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F21, Keyboard.KEY_F21);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F22, Keyboard.KEY_F22);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F23, Keyboard.KEY_F23);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_F24, Keyboard.KEY_F24);
		//GLFW2LWJGL.put(SDLScancode.SDL_SCANCODE_F25, Keyboard.KEY_F25);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_0, Keyboard.KEY_NUMPAD0);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_1, Keyboard.KEY_NUMPAD1);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_2, Keyboard.KEY_NUMPAD2);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_3, Keyboard.KEY_NUMPAD3);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_4, Keyboard.KEY_NUMPAD4);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_5, Keyboard.KEY_NUMPAD5);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_6, Keyboard.KEY_NUMPAD6);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_7, Keyboard.KEY_NUMPAD7);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_8, Keyboard.KEY_NUMPAD8);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_9, Keyboard.KEY_NUMPAD9);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_DECIMAL, Keyboard.KEY_DECIMAL);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_DIVIDE, Keyboard.KEY_DIVIDE);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_MULTIPLY, Keyboard.KEY_MULTIPLY);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_MINUS, Keyboard.KEY_SUBTRACT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_PLUS, Keyboard.KEY_ADD);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_ENTER, Keyboard.KEY_NUMPADENTER);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_KP_EQUALS, Keyboard.KEY_NUMPADEQUALS);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LSHIFT, Keyboard.KEY_LSHIFT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LCTRL, Keyboard.KEY_LCONTROL);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LALT, Keyboard.KEY_LMENU);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_LGUI, Keyboard.KEY_LMETA);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RSHIFT, Keyboard.KEY_RSHIFT);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RCTRL, Keyboard.KEY_RCONTROL);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RALT, Keyboard.KEY_RMENU);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_MODE, Keyboard.KEY_RMENU);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_RGUI, Keyboard.KEY_RMETA);
		SDL_SCANCODE2LWJGL.put(SDLScancode.SDL_SCANCODE_MENU, Keyboard.KEY_APPS);
	}
}
