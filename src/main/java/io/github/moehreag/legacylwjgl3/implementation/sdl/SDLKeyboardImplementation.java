package io.github.moehreag.legacylwjgl3.implementation.sdl;

import java.nio.ByteBuffer;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.implementation.input.KeyboardImplementation;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;
import org.lwjgl.sdl.*;

import static org.lwjgl.sdl.SDLEvents.*;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 2:14 PM
 */
public class SDLKeyboardImplementation implements KeyboardImplementation {

	private long windowHandle;

	private final byte[] key_down_buffer = new byte[Keyboard.KEYBOARD_SIZE];
	private final EventQueue event_queue = new EventQueue(Keyboard.EVENT_SIZE);

	private final ByteBuffer tmp_event = ByteBuffer.allocate(Keyboard.EVENT_SIZE);
	private final SDL_KeyboardEvent keyboardEvent = Display.getEvent().key();
	private final SDL_TextEditingEvent textEditingEvent = Display.getEvent().edit();
	private final SDL_TextInputEvent textInputEvent = Display.getEvent().text();

	@Override
	public void createKeyboard() {
		this.windowHandle = Display.getHandle();

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

	@Override
	public void processKeyboardEvent(SDL_Event event) {
		switch (event.type()) {
			case SDL_EVENT_KEY_DOWN, SDL_EVENT_KEY_UP -> {
				int key = translateKeyFromSDL(keyboardEvent.key());
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

	public static int translateKeyFromSDL(int key) {
		if (key < 0) key = SDLKeycode.SDLK_UNKNOWN;
		if (!SDL2LWJGL.containsKey(key)) {
			LegacyLWJGL3.LOGGER.warn("Untranslated key: "+key+" ("+SDLKeyboard.SDL_GetKeyName(key)+")");
			return Keyboard.KEY_NONE;
		}
		return SDL2LWJGL.get(key);
	}

	private static final Int2IntMap SDL2LWJGL = new Int2IntOpenHashMap();

	static {
		SDL2LWJGL.put(SDLKeycode.SDLK_UNKNOWN, Keyboard.KEY_NONE);
		SDL2LWJGL.put(SDLKeycode.SDLK_SPACE, Keyboard.KEY_SPACE);
		SDL2LWJGL.put(SDLKeycode.SDLK_APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		SDL2LWJGL.put(SDLKeycode.SDLK_COMMA, Keyboard.KEY_COMMA);
		SDL2LWJGL.put(SDLKeycode.SDLK_MINUS, Keyboard.KEY_MINUS);
		SDL2LWJGL.put(SDLKeycode.SDLK_PERIOD, Keyboard.KEY_PERIOD);
		SDL2LWJGL.put(SDLKeycode.SDLK_SLASH, Keyboard.KEY_SLASH);
		SDL2LWJGL.put(SDLKeycode.SDLK_0, Keyboard.KEY_0);
		SDL2LWJGL.put(SDLKeycode.SDLK_1, Keyboard.KEY_1);
		SDL2LWJGL.put(SDLKeycode.SDLK_2, Keyboard.KEY_2);
		SDL2LWJGL.put(SDLKeycode.SDLK_3, Keyboard.KEY_3);
		SDL2LWJGL.put(SDLKeycode.SDLK_4, Keyboard.KEY_4);
		SDL2LWJGL.put(SDLKeycode.SDLK_5, Keyboard.KEY_5);
		SDL2LWJGL.put(SDLKeycode.SDLK_6, Keyboard.KEY_6);
		SDL2LWJGL.put(SDLKeycode.SDLK_7, Keyboard.KEY_7);
		SDL2LWJGL.put(SDLKeycode.SDLK_8, Keyboard.KEY_8);
		SDL2LWJGL.put(SDLKeycode.SDLK_9, Keyboard.KEY_9);
		SDL2LWJGL.put(SDLKeycode.SDLK_SEMICOLON, Keyboard.KEY_SEMICOLON);
		SDL2LWJGL.put(SDLKeycode.SDLK_EQUALS, Keyboard.KEY_EQUALS);
		SDL2LWJGL.put(SDLKeycode.SDLK_A, Keyboard.KEY_A);
		SDL2LWJGL.put(SDLKeycode.SDLK_B, Keyboard.KEY_B);
		SDL2LWJGL.put(SDLKeycode.SDLK_C, Keyboard.KEY_C);
		SDL2LWJGL.put(SDLKeycode.SDLK_D, Keyboard.KEY_D);
		SDL2LWJGL.put(SDLKeycode.SDLK_E, Keyboard.KEY_E);
		SDL2LWJGL.put(SDLKeycode.SDLK_F, Keyboard.KEY_F);
		SDL2LWJGL.put(SDLKeycode.SDLK_G, Keyboard.KEY_G);
		SDL2LWJGL.put(SDLKeycode.SDLK_H, Keyboard.KEY_H);
		SDL2LWJGL.put(SDLKeycode.SDLK_I, Keyboard.KEY_I);
		SDL2LWJGL.put(SDLKeycode.SDLK_J, Keyboard.KEY_J);
		SDL2LWJGL.put(SDLKeycode.SDLK_K, Keyboard.KEY_K);
		SDL2LWJGL.put(SDLKeycode.SDLK_L, Keyboard.KEY_L);
		SDL2LWJGL.put(SDLKeycode.SDLK_M, Keyboard.KEY_M);
		SDL2LWJGL.put(SDLKeycode.SDLK_N, Keyboard.KEY_N);
		SDL2LWJGL.put(SDLKeycode.SDLK_O, Keyboard.KEY_O);
		SDL2LWJGL.put(SDLKeycode.SDLK_P, Keyboard.KEY_P);
		SDL2LWJGL.put(SDLKeycode.SDLK_Q, Keyboard.KEY_Q);
		SDL2LWJGL.put(SDLKeycode.SDLK_R, Keyboard.KEY_R);
		SDL2LWJGL.put(SDLKeycode.SDLK_S, Keyboard.KEY_S);
		SDL2LWJGL.put(SDLKeycode.SDLK_T, Keyboard.KEY_T);
		SDL2LWJGL.put(SDLKeycode.SDLK_U, Keyboard.KEY_U);
		SDL2LWJGL.put(SDLKeycode.SDLK_V, Keyboard.KEY_V);
		SDL2LWJGL.put(SDLKeycode.SDLK_W, Keyboard.KEY_W);
		SDL2LWJGL.put(SDLKeycode.SDLK_X, Keyboard.KEY_X);
		SDL2LWJGL.put(SDLKeycode.SDLK_Y, Keyboard.KEY_Y);
		SDL2LWJGL.put(SDLKeycode.SDLK_Z, Keyboard.KEY_Z);
		SDL2LWJGL.put(SDLKeycode.SDLK_LEFTBRACKET, Keyboard.KEY_LBRACKET);
		SDL2LWJGL.put(SDLKeycode.SDLK_BACKSLASH, Keyboard.KEY_BACKSLASH);
		SDL2LWJGL.put(SDLKeycode.SDLK_RIGHTBRACKET, Keyboard.KEY_RBRACKET);
		SDL2LWJGL.put(SDLKeycode.SDLK_GRAVE, Keyboard.KEY_GRAVE);
		//GLFW2LWJGL.put(SDLKeycode.SDLK_, Keyboard.KEY_WORLD_1);
		//GLFW2LWJGL.put(SDLKeycode.SDLK_WORLD_2, Keyboard.KEY_WORLD_2);
		SDL2LWJGL.put(SDLKeycode.SDLK_ESCAPE, Keyboard.KEY_ESCAPE);
		SDL2LWJGL.put(SDLKeycode.SDLK_RETURN, Keyboard.KEY_RETURN);
		SDL2LWJGL.put(SDLKeycode.SDLK_TAB, Keyboard.KEY_TAB);
		SDL2LWJGL.put(SDLKeycode.SDLK_BACKSPACE, Keyboard.KEY_BACK);
		SDL2LWJGL.put(SDLKeycode.SDLK_INSERT, Keyboard.KEY_INSERT);
		SDL2LWJGL.put(SDLKeycode.SDLK_DELETE, Keyboard.KEY_DELETE);
		SDL2LWJGL.put(SDLKeycode.SDLK_RIGHT, Keyboard.KEY_RIGHT);
		SDL2LWJGL.put(SDLKeycode.SDLK_LEFT, Keyboard.KEY_LEFT);
		SDL2LWJGL.put(SDLKeycode.SDLK_DOWN, Keyboard.KEY_DOWN);
		SDL2LWJGL.put(SDLKeycode.SDLK_UP, Keyboard.KEY_UP);
		SDL2LWJGL.put(SDLKeycode.SDLK_PAGEUP, Keyboard.KEY_PRIOR);
		SDL2LWJGL.put(SDLKeycode.SDLK_PAGEDOWN, Keyboard.KEY_NEXT);
		SDL2LWJGL.put(SDLKeycode.SDLK_HOME, Keyboard.KEY_HOME);
		SDL2LWJGL.put(SDLKeycode.SDLK_END, Keyboard.KEY_END);
		SDL2LWJGL.put(SDLKeycode.SDLK_CAPSLOCK, Keyboard.KEY_CAPITAL);
		SDL2LWJGL.put(SDLKeycode.SDLK_SCROLLLOCK, Keyboard.KEY_SCROLL);
		SDL2LWJGL.put(SDLKeycode.SDLK_NUMLOCKCLEAR, Keyboard.KEY_NUMLOCK);
		SDL2LWJGL.put(SDLKeycode.SDLK_PRINTSCREEN, Keyboard.KEY_PRINT_SCREEN);
		SDL2LWJGL.put(SDLKeycode.SDLK_PAUSE, Keyboard.KEY_PAUSE);
		SDL2LWJGL.put(SDLKeycode.SDLK_F1, Keyboard.KEY_F1);
		SDL2LWJGL.put(SDLKeycode.SDLK_F2, Keyboard.KEY_F2);
		SDL2LWJGL.put(SDLKeycode.SDLK_F3, Keyboard.KEY_F3);
		SDL2LWJGL.put(SDLKeycode.SDLK_F4, Keyboard.KEY_F4);
		SDL2LWJGL.put(SDLKeycode.SDLK_F5, Keyboard.KEY_F5);
		SDL2LWJGL.put(SDLKeycode.SDLK_F6, Keyboard.KEY_F6);
		SDL2LWJGL.put(SDLKeycode.SDLK_F7, Keyboard.KEY_F7);
		SDL2LWJGL.put(SDLKeycode.SDLK_F8, Keyboard.KEY_F8);
		SDL2LWJGL.put(SDLKeycode.SDLK_F9, Keyboard.KEY_F9);
		SDL2LWJGL.put(SDLKeycode.SDLK_F10, Keyboard.KEY_F10);
		SDL2LWJGL.put(SDLKeycode.SDLK_F11, Keyboard.KEY_F11);
		SDL2LWJGL.put(SDLKeycode.SDLK_F12, Keyboard.KEY_F12);
		SDL2LWJGL.put(SDLKeycode.SDLK_F13, Keyboard.KEY_F13);
		SDL2LWJGL.put(SDLKeycode.SDLK_F14, Keyboard.KEY_F14);
		SDL2LWJGL.put(SDLKeycode.SDLK_F15, Keyboard.KEY_F15);
		SDL2LWJGL.put(SDLKeycode.SDLK_F16, Keyboard.KEY_F16);
		SDL2LWJGL.put(SDLKeycode.SDLK_F17, Keyboard.KEY_F17);
		SDL2LWJGL.put(SDLKeycode.SDLK_F18, Keyboard.KEY_F18);
		SDL2LWJGL.put(SDLKeycode.SDLK_F19, Keyboard.KEY_F19);
		SDL2LWJGL.put(SDLKeycode.SDLK_F20, Keyboard.KEY_F20);
		SDL2LWJGL.put(SDLKeycode.SDLK_F21, Keyboard.KEY_F21);
		SDL2LWJGL.put(SDLKeycode.SDLK_F22, Keyboard.KEY_F22);
		SDL2LWJGL.put(SDLKeycode.SDLK_F23, Keyboard.KEY_F23);
		SDL2LWJGL.put(SDLKeycode.SDLK_F24, Keyboard.KEY_F24);
		//GLFW2LWJGL.put(SDLKeycode.SDLK_F25, Keyboard.KEY_F25);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_0, Keyboard.KEY_NUMPAD0);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_1, Keyboard.KEY_NUMPAD1);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_2, Keyboard.KEY_NUMPAD2);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_3, Keyboard.KEY_NUMPAD3);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_4, Keyboard.KEY_NUMPAD4);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_5, Keyboard.KEY_NUMPAD5);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_6, Keyboard.KEY_NUMPAD6);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_7, Keyboard.KEY_NUMPAD7);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_8, Keyboard.KEY_NUMPAD8);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_9, Keyboard.KEY_NUMPAD9);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_DECIMAL, Keyboard.KEY_DECIMAL);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_DIVIDE, Keyboard.KEY_DIVIDE);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_MULTIPLY, Keyboard.KEY_MULTIPLY);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_MINUS, Keyboard.KEY_SUBTRACT);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_PLUS, Keyboard.KEY_ADD);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_ENTER, Keyboard.KEY_NUMPADENTER);
		SDL2LWJGL.put(SDLKeycode.SDLK_KP_EQUALS, Keyboard.KEY_NUMPADEQUALS);
		SDL2LWJGL.put(SDLKeycode.SDLK_LSHIFT, Keyboard.KEY_LSHIFT);
		SDL2LWJGL.put(SDLKeycode.SDLK_LCTRL, Keyboard.KEY_LCONTROL);
		SDL2LWJGL.put(SDLKeycode.SDLK_LALT, Keyboard.KEY_LMENU);
		SDL2LWJGL.put(SDLKeycode.SDLK_LMETA, Keyboard.KEY_LMETA);
		SDL2LWJGL.put(SDLKeycode.SDLK_LGUI, Keyboard.KEY_LMETA);
		SDL2LWJGL.put(SDLKeycode.SDLK_RSHIFT, Keyboard.KEY_RSHIFT);
		SDL2LWJGL.put(SDLKeycode.SDLK_RCTRL, Keyboard.KEY_RCONTROL);
		SDL2LWJGL.put(SDLKeycode.SDLK_RALT, Keyboard.KEY_RMENU);
		SDL2LWJGL.put(SDLKeycode.SDLK_RMETA, Keyboard.KEY_RMETA);
		SDL2LWJGL.put(SDLKeycode.SDLK_MODE, Keyboard.KEY_RMENU);
		SDL2LWJGL.put(SDLKeycode.SDLK_RGUI, Keyboard.KEY_RMETA);
		SDL2LWJGL.put(SDLKeycode.SDLK_MENU, Keyboard.KEY_MENU);
	}
}
