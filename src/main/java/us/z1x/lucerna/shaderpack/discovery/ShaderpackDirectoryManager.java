package us.z1x.lucerna.shaderpack.discovery;

import us.z1x.lucerna.Iris;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShaderpackDirectoryManager {
    private final Path root;

    public ShaderpackDirectoryManager(Path root) {
        this.root = root;
    }

    public void copyPackIntoDirectory(String name, Path source) throws IOException {
        Path target = Iris.getShaderpacksDirectory().resolve(name);

        Files.copy(source, target);

        if (Files.isDirectory(source)) {
            // plain loops since createDirectory throws IOException
            // collect to a list to keep walk order, parents before children
            try (Stream<Path> stream = Files.walk(source)) {
                for (Path p : stream.filter(Files::isDirectory).collect(Collectors.toList())) {
                    Path folder = source.relativize(p);

                    if (Files.exists(folder)) {
                        continue;
                    }

                    Files.createDirectory(target.resolve(folder));
                }
            }

            try (Stream<Path> stream = Files.walk(source)) {
                for (Path p : stream.filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet())) {
                    Path file = source.relativize(p);

                    Files.copy(p, target.resolve(file));
                }
            }
        }
    }

    public Collection<String> enumerate() throws IOException {
        // not all systems sort directory listings, sort case-insensitively then naturally
        // chat formatting is ignored since some packs use it in their file names
        Comparator<String> baseComparator = String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder());
        Comparator<String> comparator = (a, b) -> {
            a = removeFormatting(a);
            b = removeFormatting(b);

            return baseComparator.compare(a, b);
        };

        try (Stream<Path> list = Files.list(root)) {
            return list.filter(Iris::isValidShaderpack)
                .map(path -> path.getFileName().toString())
                .sorted(comparator).collect(Collectors.toList());
        }
    }

    private static String removeFormatting(String formatted) {
        char[] original = formatted.toCharArray();
        char[] cleaned = new char[original.length];
        int c = 0;

        for (int i = 0; i < original.length; i++) {
            if (original[i] == '\u00a7') {
                i++;
            } else {
                cleaned[c++] = original[i];
            }
        }

        return new String(cleaned, 0, c);
    }

    public URI getDirectoryUri() {
        return root.toUri();
    }
}
