package net.coderbot.iris.shaderpack;

import java.util.Optional;

/**
 * Parses comment-based directives found in shader source files of the form:
 *
 * <pre>/* KEY:VALUE *<i></i>/</pre>
 * <p>
 * A common example is draw buffer directives:
 *
 * <pre>/* DRAWBUFFERS:157 *<i></i>/</pre>
 * <p>
 * A given directive should only occur once in a shader file. If there are multiple occurrences of a directive with a
 * given key, the last occurrence is used.
 */
public class CommentDirectiveParser {
	// Directives take the following form:
	// /* KEY:VALUE */

	private CommentDirectiveParser() {
		// cannot be constructed
	}

	public static Optional<CommentDirective> findDirective(String haystack, CommentDirective.Type type) {
		String needle = type.name();
		String prefix = needle + ":";
		String suffix = "*/";

		// Search for the last occurrence of the directive within the text, since those take precedence.
		int indexOfPrefix = haystack.lastIndexOf(prefix);

		if (indexOfPrefix == -1) {
			return Optional.empty();
		}

		String before = haystack.substring(0, indexOfPrefix).trim();

		if (!before.endsWith("/*")) {
			// Reject a match if it doesn't actually start with a comment marker
			// TODO: If a line has two directives, one valid, and the other invalid, then this might not work properly
			return Optional.empty();
		}

		// Remove everything up to and including the prefix
		haystack = haystack.substring(indexOfPrefix + prefix.length());

		int indexOfSuffix = haystack.indexOf(suffix);

		// If there isn't a proper suffix, this directive is malformed and should be discarded.
		if (indexOfSuffix == -1) {
			return Optional.empty();
		}

		// Remove the suffix and everything afterwards, also trim any whitespace
		haystack = haystack.substring(0, indexOfSuffix).trim();

		return Optional.of(new CommentDirective(CommentDirective.Type.valueOf(needle), haystack, indexOfPrefix));
	}
}
