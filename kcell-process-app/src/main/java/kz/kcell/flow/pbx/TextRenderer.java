package kz.kcell.flow.pbx;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public final class TextRenderer {

    private TextRenderer() {
    }

    public static Rectangle drawString(Graphics g, String text, Font font, Color color, Rectangle bounds) {
        return drawString(g, text, font, color, bounds, TextAlignment.TOP_LEFT, TextFormat.NONE);
    }

    public static Rectangle drawString(Graphics g, String text, Font font, Color color, Rectangle bounds, TextAlignment align) {
        return drawString(g, text, font, color, bounds, align, TextFormat.NONE);
    }

    public static Rectangle drawString(Graphics g, String text, Font font, Color color, Rectangle bounds, TextAlignment align, int format) {
        if (g == null)
            throw new NullPointerException("The graphics handle cannot be null.");
        if (text == null)
            throw new NullPointerException("The text cannot be null.");
        if (font == null)
            throw new NullPointerException("The font cannot be null.");
        if (color == null)
            throw new NullPointerException("The text color cannot be null.");
        if (bounds == null)
            throw new NullPointerException("The text bounds cannot be null.");
        if (align == null)
            throw new NullPointerException("The text alignment cannot be null.");
        if (text.length() == 0)
            return new Rectangle(bounds.x, bounds.y, 0, 0);

        Graphics2D g2D = (Graphics2D) g;

        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FOREGROUND, color);
        attributedString.addAttribute(TextAttribute.FONT, font);

        AttributedCharacterIterator attributedCharIterator = attributedString.getIterator();

        FontRenderContext fontContext = new FontRenderContext(null, !TextFormat.isEnabled(format, TextFormat.NO_ANTI_ALIASING), false);
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(attributedCharIterator, fontContext);

        Point targetLocation = new Point(bounds.x, bounds.y);
        int nextOffset;

        if (align.isMiddle() || align.isBottom()) {
            if (align.isMiddle())
                targetLocation.y = bounds.y + (bounds.height / 2);
            if (align.isBottom())
                targetLocation.y = bounds.y + bounds.height;

            while (lineMeasurer.getPosition() < text.length()) {
                nextOffset = lineMeasurer.nextOffset(bounds.width);
                nextOffset = nextTextIndex(nextOffset, lineMeasurer.getPosition(), text);

                TextLayout textLayout = lineMeasurer.nextLayout(bounds.width, nextOffset, false);

                if (align.isMiddle())
                    targetLocation.y -= (textLayout.getAscent() + textLayout.getLeading() + textLayout.getDescent()) / 2;
                if (align.isBottom())
                    targetLocation.y -= (textLayout.getAscent() + textLayout.getLeading() + textLayout.getDescent());
            }

            if (TextFormat.isEnabled(format, TextFormat.FIRST_LINE_VISIBLE))
                targetLocation.y = Math.max(0, targetLocation.y);

            lineMeasurer.setPosition(0);
        }

        if (align.isRight() || align.isCenter())
            targetLocation.x = bounds.x + bounds.width;

        Rectangle consumedBounds = new Rectangle(targetLocation.x, targetLocation.y, 0, 0);

        while (lineMeasurer.getPosition() < text.length()) {
            nextOffset = lineMeasurer.nextOffset(bounds.width);
            nextOffset = nextTextIndex(nextOffset, lineMeasurer.getPosition(), text);

            TextLayout textLayout = lineMeasurer.nextLayout(bounds.width, nextOffset, false);
            Rectangle2D textBounds = textLayout.getBounds();

            targetLocation.y += textLayout.getAscent();
            consumedBounds.width = Math.max(consumedBounds.width, (int) textBounds.getWidth());

            switch (align) {
                case TOP_LEFT:
                case MIDDLE_LEFT:
                case BOTTOM_LEFT:
                    textLayout.draw(g2D, targetLocation.x, targetLocation.y);
                    break;

                case TOP:
                case MIDDLE:
                case BOTTOM:
                    targetLocation.x = bounds.x + (bounds.width / 2) - (int) (textBounds.getWidth() / 2);
                    consumedBounds.x = Math.min(consumedBounds.x, targetLocation.x);
                    textLayout.draw(g2D, targetLocation.x, targetLocation.y);
                    break;

                case TOP_RIGHT:
                case MIDDLE_RIGHT:
                case BOTTOM_RIGHT:
                    targetLocation.x = bounds.x + bounds.width - (int) textBounds.getWidth();
                    textLayout.draw(g2D, targetLocation.x, targetLocation.y);
                    consumedBounds.x = Math.min(consumedBounds.x, targetLocation.x);
                    break;
            }

            targetLocation.y += textLayout.getLeading() + textLayout.getDescent();
        }

        consumedBounds.height = targetLocation.y - consumedBounds.y;

        return consumedBounds;
    }

    private static int nextTextIndex(int nextOffset, int measurerPosition, String text) {
        for (int i = measurerPosition + 1; i < nextOffset; ++i) {
            if (text.charAt(i) == '\n') {
                return i;
            }
        }
        return nextOffset;
    }
}
