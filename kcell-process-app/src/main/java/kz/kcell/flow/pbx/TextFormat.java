package kz.kcell.flow.pbx;

public final class TextFormat {

    public final static int NONE = 0;

    public final static int NO_ANTI_ALIASING = 1;

    public final static int FIRST_LINE_VISIBLE = 2;

    private TextFormat() {
    }

    public static boolean isEnabled(int format, int flag) {
        return (format & flag) == flag;
    }
}
