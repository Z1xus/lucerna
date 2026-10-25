package us.z1x.lucerna.shaderpack.option;

import us.z1x.lucerna.shaderpack.include.AbsolutePackPath;

public class OptionLocation {
    private final AbsolutePackPath filePath;
    private final int lineIndex;

    public OptionLocation(AbsolutePackPath filePath, int lineIndex) {
        this.filePath = filePath;
        this.lineIndex = lineIndex;
    }

    public AbsolutePackPath getFilePath() {
        return filePath;
    }

    // zero-based
    public int getLineIndex() {
        return lineIndex;
    }
}
