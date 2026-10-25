package net.coderbot.iris.shaderpack.preprocessor;

import net.coderbot.iris.shaderpack.StringPair;
import org.anarres.cpp.Feature;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;
import org.anarres.cpp.Token;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JcppProcessor {
	private static final Pattern EMPTY_DEFINE = Pattern.compile("(?m)^([ \\t]*#[ \\t]*define[ \\t]+(\\w+))[ \\t]*$");
	private static final Pattern CONDITION = Pattern.compile("(?m)^[ \\t]*#[ \\t]*(?:el)?if\\b(.*)$");
	private static final Pattern CONDITION_NAME = Pattern.compile("(defined\\s*\\(?\\s*)?\\b([A-Za-z_]\\w*)");

	// Derived from GlShader from Canvas, licenced under LGPL
	public static String glslPreprocessSource(String source, Iterable<StringPair> environmentDefines) {
		if (source.contains(GlslCollectingListener.VERSION_MARKER)
				|| source.contains(GlslCollectingListener.EXTENSION_MARKER)) {
			throw new RuntimeException("Some shader author is trying to exploit internal Iris implementation details, stop!");
		}

		// Note: This is an absolutely awful hack. But JCPP's lack of extensibility leaves me with no choice...
		//       We should write our own preprocessor at some point to avoid this.
		//
		// Why are we doing this awful hack instead of just using the preprocessor like a normal person? Because it lets
		// us only hoist #extension directives if they're actually used. This is needed for shader packs written on
		// lenient drivers that allow #extension directives to be placed anywhere to work on strict drivers like Mesa
		// that require #extension directives to occur at the top.
		//
		// TODO: This allows #version to not appear as the first non-comment non-whitespace thing in the file.
		//       That's not the behavior we want. If you're reading this, don't rely on this behavior.
		source = source.replace("#version", GlslCollectingListener.VERSION_MARKER);
		source = source.replace("#extension", GlslCollectingListener.EXTENSION_MARKER);

		Set<String> conditionNames = conditionNames(source);
		source = EMPTY_DEFINE.matcher(source).replaceAll(match -> conditionNames.contains(match.group(2)) ? match.group(1) + " 1" : match.group(0));

		GlslCollectingListener listener = new GlslCollectingListener();

		@SuppressWarnings("resource")
		final Preprocessor pp = new Preprocessor();

		// Add the values of the environment defines without actually modifying the source code
		// of the shader program, one step down the road of having accurate line number reporting
		// in errors...
		try {
			for (StringPair envDefine : environmentDefines) {
				pp.addMacro(envDefine.getKey(), envDefine.getValue().isEmpty() ? "1" : envDefine.getValue());
			}
		} catch (LexerException e) {
			throw new RuntimeException("Unexpected LexerException processing macros", e);
		}

		pp.setListener(listener);
		pp.addInput(new StringLexerSource(source, true));
		pp.addFeature(Feature.KEEPCOMMENTS);

		final StringBuilder builder = new StringBuilder();

		try {
			for (;;) {
				final Token tok = pp.token();
				if (tok == null) break;
				if (tok.getType() == Token.EOF) break;
				builder.append(tok.getText());
			}
		} catch (final Exception e) {
			throw new RuntimeException("GLSL source pre-processing failed", e);
		}

		builder.append("\n");

		source = listener.collectLines() + builder;

		return source;
	}

	// names used as values in #if and #elif, an empty macro there is a syntax error that optifine and lenient drivers accept as true
	private static Set<String> conditionNames(String source) {
		Set<String> names = new HashSet<>();
		Matcher condition = CONDITION.matcher(source);

		while (condition.find()) {
			Matcher name = CONDITION_NAME.matcher(condition.group(1));

			while (name.find()) {
				if (name.group(1) == null) {
					names.add(name.group(2));
				}
			}
		}

		return names;
	}
}
