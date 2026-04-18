package io.github.moehreag.legacylwjgl3.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

public record PreeditEvent(@NotNull String fullText, int caretPosition, List<String> blocks, int focusedBlock,
                           int focusStart, int focusLength) {
	public static PreeditEvent createFromCompositionEvent(String s, int start, int length) {
		if (s.isEmpty()) return null;
		return new PreeditEvent(s, s.length(), null, 0, start, length);
	}

	public static PreeditEvent createFromCallback(
			final int preeditSize, final long preeditPtr, final int blockCount, final long blockSizesPtr, final int focusedBlock, final int caret
	) {
		if (preeditSize == 0) {
			return null;
		} else {
			int[] codepoints = readIntBuffer(preeditSize, preeditPtr);
			int[] blockSizes = readIntBuffer(blockCount, blockSizesPtr);
			StringBuilder fullText = new StringBuilder();
			List<String> blocks = new ArrayList<>();
			int offset = 0;
			int convertedCaret = 0;

			for (int blockSize : blockSizes) {
				StringBuilder blockBuilder = new StringBuilder();

				for (int i = 0; i < blockSize; i++) {
					int codepoint = codepoints[offset];
					if (offset == caret) {
						convertedCaret = fullText.length() + blockBuilder.length();
					}

					blockBuilder.appendCodePoint(codepoint);
					offset++;
				}

				String block = blockBuilder.toString();
				blocks.add(block);
				fullText.append(block);
			}

			if (offset == caret) {
				convertedCaret = fullText.length();
			}

			return new PreeditEvent(fullText.toString(), convertedCaret, blocks, focusedBlock, 0, 0);
		}
	}

	private static int[] readIntBuffer(final int size, final long ptr) {
		IntBuffer buffer = MemoryUtil.memIntBuffer(ptr, size);
		int[] result = new int[size];
		buffer.get(result);
		return result;
	}

	public String toFormattedText(final String focusedStyle) {
		var reset = focusedStyle.isEmpty() ? "" : "§r";
		if (blocks == null) {
			if (focusStart == 0 && focusLength == fullText.length()) {
				return focusedStyle + fullText + reset;
			} else if (focusStart == 0) {
				return focusedStyle + fullText.substring(0, focusLength) + reset + fullText.substring(focusLength);
			}
			var start = fullText.substring(0, focusStart);
			var focused = fullText.substring(focusStart, focusStart + focusLength);
			var end = fullText.substring(focusStart + focusLength);
			return start + focusedStyle + focused + reset + end;
		}
		int blockCount = this.blocks.size();
		if (blockCount == 1) {
			return focusedStyle + this.blocks.get(0) + reset;
		} else {
			StringBuilder result = new StringBuilder();

			for (int i = 0; i < blockCount; i++) {
				var part = this.blocks.get(i);
				if (i == this.focusedBlock) {
					result.append(focusedStyle).append(part).append(reset);
				} else {
					result.append(part);
				}
			}

			return result.toString();
		}
	}
}