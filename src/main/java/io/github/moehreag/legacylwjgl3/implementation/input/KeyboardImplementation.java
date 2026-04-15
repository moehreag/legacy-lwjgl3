package io.github.moehreag.legacylwjgl3.implementation.input;

import java.nio.ByteBuffer;

/**
 * @author Zarzelcow
 *
 * <p>28/09/2022 - 3:24 PM</p>
 */
public interface KeyboardImplementation {
    void createKeyboard();

    void destroyKeyboard();

    void pollKeyboard(ByteBuffer keyDownBuffer);

    void readKeyboard(ByteBuffer readBuffer);
}
