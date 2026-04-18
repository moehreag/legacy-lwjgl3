package io.github.moehreag.legacylwjgl3.util;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.client.render.TextRenderer;

public class IMEPreeditOverlay {
	private static final String FOCUSED_STYLE;


	static {
		boolean hasFormatting;
		try {
			hasFormatting = VersionPredicate.parse(">=1.2.4").test(FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion());
		} catch (VersionParsingException e) {
			hasFormatting = false;
		}
		FOCUSED_STYLE = hasFormatting ? "§n" : "";
	}

	private static final int SEPARATION_FROM_INPUT = 4;
	private static final int BORDER_MARGIN = 4;
	private static final int BORDER_WIDTH = 1;
	private static final int BORDER_OFFSET = 5;
	private static final int TEXT_COLOR = 0xff000000;
	private static final int HOT_AREA_MARGIN = 2;
	private int inputLeft;
	private int inputTop;
	private final int inputHeight;
	private final long initTimeMs;
	private final String preEditText;
	private final int preEditTextWidth;
	private final int caretPos;

	public IMEPreeditOverlay(final PreeditEvent contents, final TextRenderer font, final int inputHeight) {
		this.inputHeight = inputHeight;
		this.initTimeMs = System.currentTimeMillis();
		this.preEditText = contents.toFormattedText(FOCUSED_STYLE);
		this.preEditTextWidth = font.getWidth(this.preEditText);
		String textBeforeCaret = contents.fullText().substring(0, contents.caretPosition());
		this.caretPos = font.getWidth(textBeforeCaret);
	}

	public void updateInputPosition(final int inputLeft, final int inputTop) {
		this.inputLeft = inputLeft;
		this.inputTop = inputTop;
	}

	public void render(int guiScale, int windowWidth, int windowHeight) {
		int preeditLeft = this.inputLeft;
		int preeditRight = preeditLeft + this.preEditTextWidth;
		if (preeditRight > windowWidth) {
			preeditLeft = windowWidth - this.preEditTextWidth;
			preeditRight = preeditLeft + this.preEditTextWidth;
		}

		int inputBottom = this.inputTop + this.inputHeight;
		int preeditBottom = inputBottom + SEPARATION_FROM_INPUT + 9;
		if (preeditBottom > windowHeight) {
			preeditBottom = this.inputTop - BORDER_MARGIN - 9;
		}

		int preeditTop = preeditBottom - 9;
		IMEManager.getInstance().setTextInputArea(
				Math.min(preeditLeft, this.inputLeft) - HOT_AREA_MARGIN,
				Math.min(preeditTop, this.inputTop) - HOT_AREA_MARGIN,
				preeditRight + HOT_AREA_MARGIN,
				Math.max(preeditBottom, inputBottom) + HOT_AREA_MARGIN,
				guiScale);
		int backgroundWidth = preeditRight - preeditLeft + BORDER_OFFSET * 2;
		int backgroundHeight = preeditBottom - preeditTop + BORDER_OFFSET * 2;
		LegacyLWJGL3RenderHelper.pushMatrix();
		LegacyLWJGL3RenderHelper.translate(0, 0, 800);
		blitTexture(preeditLeft - BORDER_OFFSET, preeditTop - BORDER_OFFSET, backgroundWidth, backgroundHeight);
		LegacyLWJGL3RenderHelper.drawString(this.preEditText, preeditLeft, preeditTop, TEXT_COLOR);
		if ((System.currentTimeMillis() - this.initTimeMs) / 300L % 2L == 0L) {
			LegacyLWJGL3RenderHelper.fill(preeditLeft + this.caretPos, preeditTop - 1, preeditLeft + this.caretPos + 1, preeditTop + 9 + BORDER_WIDTH, TEXT_COLOR);
		}
		LegacyLWJGL3RenderHelper.popMatrix();
	}

	private void blitTexture(int x, int y, int width, int height) {
		int borderLeft = Math.min(1, width / 2);
		int borderRight = Math.min(1, width / 2);
		int borderTop = Math.min(1, height / 2);
		int borderBottom = Math.min(1, height / 2);
		int ninesliceWidth = 200;
		int ninesliceHeight = 20;
		if (width == ninesliceWidth && height == ninesliceHeight) {
			blitSprite(ninesliceWidth, ninesliceHeight, 0, 0, x, y, width, height);
		} else if (height == ninesliceHeight) {
			blitSprite(ninesliceWidth, ninesliceHeight, 0, 0, x, y, borderLeft, height);
			blitNineSliceInnerSegment(
					x + borderLeft,
					y,
					width - borderRight - borderLeft,
					height,
					borderLeft,
					0,
					ninesliceWidth - borderRight - borderLeft,
					ninesliceHeight,
					ninesliceWidth,
					ninesliceHeight
			);
			blitSprite(ninesliceWidth, ninesliceHeight, ninesliceWidth - borderRight, 0, x + width - borderRight, y, borderRight, height);
		} else if (width == ninesliceWidth) {
			blitSprite(ninesliceWidth, ninesliceHeight, 0, 0, x, y, width, borderTop);
			blitNineSliceInnerSegment(
					x,
					y + borderTop,
					width,
					height - borderBottom - borderTop,
					0,
					borderTop,
					ninesliceWidth,
					ninesliceHeight - borderBottom - borderTop,
					ninesliceWidth,
					ninesliceHeight
			);
			blitSprite(ninesliceWidth, ninesliceHeight, 0, ninesliceHeight - borderBottom, x, y + height - borderBottom, width, borderBottom);
		} else {
			blitSprite(ninesliceWidth, ninesliceHeight, 0, 0, x, y, borderLeft, borderTop);
			blitNineSliceInnerSegment(
					x + borderLeft, y, width - borderRight - borderLeft, borderTop, borderLeft, 0, ninesliceWidth - borderRight - borderLeft, borderTop, ninesliceWidth, ninesliceHeight
			);
			blitSprite(ninesliceWidth, ninesliceHeight, ninesliceWidth - borderRight, 0, x + width - borderRight, y, borderRight, borderTop);
			blitSprite(ninesliceWidth, ninesliceHeight, 0, ninesliceHeight - borderBottom, x, y + height - borderBottom, borderLeft, borderBottom);
			blitNineSliceInnerSegment(
					x + borderLeft,
					y + height - borderBottom,
					width - borderRight - borderLeft,
					borderBottom,
					borderLeft,
					ninesliceHeight - borderBottom,
					ninesliceWidth - borderRight - borderLeft,
					borderBottom,
					ninesliceWidth,
					ninesliceHeight
			);
			blitSprite(
					ninesliceWidth, ninesliceHeight, ninesliceWidth - borderRight, ninesliceHeight - borderBottom, x + width - borderRight, y + height - borderBottom, borderRight, borderBottom
			);
			blitNineSliceInnerSegment(
					x, y + borderTop, borderLeft, height - borderBottom - borderTop, 0, borderTop, borderLeft, ninesliceHeight - borderBottom - borderTop, ninesliceWidth, ninesliceHeight
			);
			blitNineSliceInnerSegment(
					x + borderLeft,
					y + borderTop,
					width - borderRight - borderLeft,
					height - borderBottom - borderTop,
					borderLeft,
					borderTop,
					ninesliceWidth - borderRight - borderLeft,
					ninesliceHeight - borderBottom - borderTop,
					ninesliceWidth,
					ninesliceHeight
			);
			blitNineSliceInnerSegment(
					x + width - borderRight,
					y + borderTop,
					borderRight,
					height - borderBottom - borderTop,
					ninesliceWidth - borderRight,
					borderTop,
					borderRight,
					ninesliceHeight - borderBottom - borderTop,
					ninesliceWidth,
					ninesliceHeight
			);
		}
	}

	private void blitSprite(
			int texWidth, int texHeight, int u, int v, int x, int y, int width, int height
	) {
		if (width != 0 && height != 0) {
			LegacyLWJGL3RenderHelper.blitPreeditBackground(texWidth, texHeight, u, v, x, y, width, height);
		}
	}

	private void blitNineSliceInnerSegment(
			int x,
			int y,
			int width,
			int height,
			int u,
			int v,
			int regionWidth,
			int regionHeight,
			int texWidth,
			int texHeight
	) {
		if (width > 0 && height > 0) {
			if (regionWidth > 0 && regionHeight > 0) {
				for (int xStep = 0; xStep < width; xStep += regionWidth) {
					int i = Math.min(regionWidth, width - xStep);

					for (int yStep = 0; yStep < height; yStep += regionHeight) {
						int w = Math.min(regionHeight, height - yStep);
						blitSprite(texWidth, texHeight, u, v, x + xStep, y + yStep, i, w);
					}
				}
			} else {
				throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + regionWidth + "x" + regionHeight);
			}
		}
	}

}