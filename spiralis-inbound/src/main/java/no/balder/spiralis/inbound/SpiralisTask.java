package no.balder.spiralis.inbound;

import java.nio.file.Path;

/**
 * @author steinar
 *         Date: 04.02.2017
 *         Time: 10.42
 */
class SpiralisTask {
    private final Path path;

    public SpiralisTask(Path path) {

        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpiralisTask{");
        sb.append("path=").append(path);
        sb.append('}');
        return sb.toString();
    }
}
