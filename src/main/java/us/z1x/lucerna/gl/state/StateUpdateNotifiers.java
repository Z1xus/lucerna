package us.z1x.lucerna.gl.state;

public class StateUpdateNotifiers {
    public static ValueUpdateNotifier fogToggleNotifier;
    public static ValueUpdateNotifier fogModeNotifier;
    public static ValueUpdateNotifier fogStartNotifier;
    public static ValueUpdateNotifier fogEndNotifier;
    public static ValueUpdateNotifier fogDensityNotifier;
    public static ValueUpdateNotifier blendFuncNotifier;
    public static ValueUpdateNotifier bindTextureNotifier;
    public static ValueUpdateNotifier normalTextureChangeNotifier = listener -> {};
    public static ValueUpdateNotifier specularTextureChangeNotifier = listener -> {};
    public static ValueUpdateNotifier phaseChangeNotifier;
}
