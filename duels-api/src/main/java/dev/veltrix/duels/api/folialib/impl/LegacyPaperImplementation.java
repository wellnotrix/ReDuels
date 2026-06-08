package dev.veltrix.duels.api.folialib.impl;

import dev.veltrix.duels.api.folialib.FoliaLib;

@SuppressWarnings("unused")
public class LegacyPaperImplementation extends LegacySpigotImplementation {

    public LegacyPaperImplementation(FoliaLib foliaLib) {
        super(foliaLib);
    }

    // Don't need to override anything, since we're extending BukkitImplementation
}
