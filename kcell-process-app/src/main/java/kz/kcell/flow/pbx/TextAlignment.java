package kz.kcell.flow.pbx;

public enum TextAlignment {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT;

    TextAlignment() {
    }

    public boolean isBottom() {
        return this == BOTTOM || this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }

    public boolean isCenter() {
        return this == TOP || this == MIDDLE || this == BOTTOM;
    }

    public boolean isLeft() {
        return this == TOP_LEFT || this == MIDDLE_LEFT || this == BOTTOM_LEFT;
    }

    public boolean isMiddle() {
        return this == MIDDLE || this == MIDDLE_LEFT || this == MIDDLE_RIGHT;
    }

    public boolean isRight() {
        return this == TOP_RIGHT || this == MIDDLE_RIGHT || this == BOTTOM_RIGHT;
    }

    public boolean isTop() {
        return this == TOP || this == TOP_LEFT || this == TOP_RIGHT;
    }
}
