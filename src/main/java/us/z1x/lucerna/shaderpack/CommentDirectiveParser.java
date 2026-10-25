package us.z1x.lucerna.shaderpack;

import java.util.Optional;

// parses /* KEY:VALUE */ directives such as DRAWBUFFERS, the last occurrence wins
public class CommentDirectiveParser {
    private CommentDirectiveParser() {
    }

    public static Optional<CommentDirective> findDirective(String haystack, CommentDirective.Type type) {
        String needle = type.name();
        String prefix = needle + ":";
        String suffix = "*/";

        int indexOfPrefix = haystack.lastIndexOf(prefix);

        if (indexOfPrefix == -1) {
            return Optional.empty();
        }

        String before = haystack.substring(0, indexOfPrefix).trim();

        if (!before.endsWith("/*")) {
            return Optional.empty();
        }

        haystack = haystack.substring(indexOfPrefix + prefix.length());

        int indexOfSuffix = haystack.indexOf(suffix);

        if (indexOfSuffix == -1) {
            return Optional.empty();
        }

        haystack = haystack.substring(0, indexOfSuffix).trim();

        return Optional.of(new CommentDirective(CommentDirective.Type.valueOf(needle), haystack, indexOfPrefix));
    }
}
