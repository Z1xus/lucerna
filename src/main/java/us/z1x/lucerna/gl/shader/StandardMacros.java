package us.z1x.lucerna.gl.shader;

import com.google.common.collect.ImmutableList;
import us.z1x.lucerna.gui.option.IrisVideoSettings;
import us.z1x.lucerna.pipeline.HandRenderer;
import us.z1x.lucerna.pipeline.WorldRenderingPhase;
import us.z1x.lucerna.shaderpack.StringPair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StandardMacros {
    private static final Pattern SEMVER_PATTERN = Pattern.compile("(?<major>\\d+)\\.(?<minor>\\d+)\\.*(?<bugfix>\\d*)(.*)");

    private static void define(List<StringPair> defines, String key) {
        defines.add(new StringPair(key, ""));
    }

    private static void define(List<StringPair> defines, String key, String value) {
        defines.add(new StringPair(key, value));
    }

    public static Iterable<StringPair> createStandardEnvironmentDefines() {
        ArrayList<StringPair> standardDefines = new ArrayList<>();

        define(standardDefines, "MC_VERSION", getMcVersion());
        define(standardDefines, "IS_IRIS");
        define(standardDefines, "MC_GL_VERSION", getGlVersion(GL20C.GL_VERSION));
        define(standardDefines, "MC_GLSL_VERSION", getGlVersion(GL20C.GL_SHADING_LANGUAGE_VERSION));
        define(standardDefines, getOsString());
        define(standardDefines, getVendor());
        define(standardDefines, getRenderer());

        for (String glExtension : getGlExtensions()) {
            define(standardDefines, glExtension);
        }

        define(standardDefines, "MC_NORMAL_MAP");
        define(standardDefines, "MC_SPECULAR_MAP");
        define(standardDefines, "MC_RENDER_QUALITY", Float.toString(IrisVideoSettings.renderQuality));
        define(standardDefines, "MC_SHADOW_QUALITY", Float.toString(IrisVideoSettings.shadowQuality));
        define(standardDefines, "MC_HAND_DEPTH", Float.toString(HandRenderer.DEPTH));

        getRenderStages().forEach((stage, index) -> define(standardDefines, stage, index));

        for (String irisDefine : getIrisDefines()) {
            define(standardDefines, irisDefine);
        }

        return ImmutableList.copyOf(standardDefines);
    }

    public static String getMcVersion() {
        String version = "1.8.9";

        if (version == null) {
            throw new IllegalStateException("Could not get the current minecraft version!");
        }

        String[] splitVersion = version.split("\\.");

        if (splitVersion.length < 2) {
            throw new IllegalStateException("Could not parse game version \"" + version +  "\"");
        }

        String major = splitVersion[0];
        String minor = splitVersion[1];
        String bugfix;

        if (splitVersion.length < 3) {
            bugfix = "00";
        } else {
            bugfix = splitVersion[2];
        }

        if (minor.length() == 1) {
            minor = 0 + minor;
        }
        if (bugfix.length() == 1) {
            bugfix = 0 + bugfix;
        }

        return major + minor + bugfix;
    }

    public static String getGlVersion(int name) {
        String info = GL11.glGetString(name);

        Matcher matcher = SEMVER_PATTERN.matcher(Objects.requireNonNull(info));

        if (!matcher.matches()) {
            throw new IllegalStateException("Could not parse GL version from \"" + info + "\"");
        }

        String major = group(matcher, "major");
        String minor = group(matcher, "minor");
        String bugfix = group(matcher, "bugfix");

        if (bugfix == null) {
            bugfix = "0";
        }

        if (major == null || minor == null) {
            throw new IllegalStateException("Could not parse GL version from \"" + info + "\"");
        }

        return major + minor + bugfix;
    }

    public static String group(Matcher matcher, String name) {
        try {
            return matcher.group(name);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return null;
        }
    }

    public static String getOsString() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            return "MC_OS_MAC";
        } else if (os.contains("linux") || os.contains("unix")) {
            return "MC_OS_LINUX";
        } else if (os.contains("win")) {
            return "MC_OS_WINDOWS";
        }
        return "MC_OS_UNKNOWN";
    }

    public static String getVendor() {
        String vendor = Objects.requireNonNull(GL11.glGetString(GL11.GL_VENDOR)).toLowerCase(Locale.ROOT);
        if (vendor.startsWith("ati")) {
            return "MC_GL_VENDOR_ATI";
        } else if (vendor.startsWith("intel")) {
            return "MC_GL_VENDOR_INTEL";
        } else if (vendor.startsWith("nvidia")) {
            return "MC_GL_VENDOR_NVIDIA";
        } else if (vendor.startsWith("amd")) {
            return "MC_GL_VENDOR_AMD";
        } else if (vendor.startsWith("x.org")) {
            return "MC_GL_VENDOR_XORG";
        }
        return "MC_GL_VENDOR_OTHER";
    }

    public static String getRenderer() {
        String renderer = Objects.requireNonNull(GL11.glGetString(GL11.GL_RENDERER)).toLowerCase(Locale.ROOT);
        if (renderer.startsWith("amd")) {
            return "MC_GL_RENDERER_RADEON";
        } else if (renderer.startsWith("ati")) {
            return "MC_GL_RENDERER_RADEON";
        } else if (renderer.startsWith("radeon")) {
            return "MC_GL_RENDERER_RADEON";
        } else if (renderer.startsWith("gallium")) {
            return "MC_GL_RENDERER_GALLIUM";
        } else if (renderer.startsWith("intel")) {
            return "MC_GL_RENDERER_INTEL";
        } else if (renderer.startsWith("geforce")) {
            return "MC_GL_RENDERER_GEFORCE";
        } else if (renderer.startsWith("nvidia")) {
            return "MC_GL_RENDERER_GEFORCE";
        } else if (renderer.startsWith("quadro")) {
            return "MC_GL_RENDERER_QUADRO";
        } else if (renderer.startsWith("nvs")) {
            return "MC_GL_RENDERER_QUADRO";
        } else if (renderer.startsWith("mesa")) {
            return "MC_GL_RENDERER_MESA";
        }
        return "MC_GL_RENDERER_OTHER";
    }

    public static Set<String> getGlExtensions() {
        String[] extensions = Objects.requireNonNull(GL11.glGetString(GL11.GL_EXTENSIONS)).split("\\s+");

        // a set because renderdoc and others list some extensions twice, https://github.com/IrisShaders/Iris/issues/971
        return Arrays.stream(extensions).map(s -> "MC_" + s).collect(Collectors.toSet());
    }

    public static Map<String, String> getRenderStages() {
        Map<String, String> stages = new HashMap<>();
        for (WorldRenderingPhase phase : WorldRenderingPhase.values()) {
            stages.put("MC_RENDER_STAGE_" + phase.name(), String.valueOf(phase.ordinal()));
        }
        return stages;
    }

    public static List<String> getIrisDefines() {
        List<String> defines = new ArrayList<>();

        return defines;
    }
}
