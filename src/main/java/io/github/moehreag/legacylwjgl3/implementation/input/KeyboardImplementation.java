package io.github.moehreag.legacylwjgl3.implementation.input;

import java.nio.ByteBuffer;

import org.lwjgl.sdl.SDL_Event;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 3:24 PM
 */
public interface KeyboardImplementation {
    void createKeyboard();

    void destroyKeyboard();

    void pollKeyboard(ByteBuffer keyDownBuffer);

    void readKeyboard(ByteBuffer readBuffer);

    void processKeyboardEvent(SDL_Event event);

}
