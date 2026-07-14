package sk.ainet.kotlinconf.models.transformer

/**
 * Builds a ready-to-train [TinyTransformerTrainer] from raw corpus lines: tokenizes,
 * builds the capped vocabulary, and slices the sliding next-token windows. This is the
 * one-call entry point the CLI, Android, and web demos share.
 */
fun buildTinyTransformerTrainer(
    corpus: List<String> = DEFAULT_CORPUS,
    maxVocab: Int = DEFAULT_MAX_VOCAB,
    contextLen: Int = DEFAULT_CONTEXT_LEN,
    seed: Int = 42,
): TinyTransformerTrainer {
    val vocab = WordTokenizer.buildVocab(corpus, maxVocab)
    val windows = WordTokenizer.windows(corpus, vocab, contextLen)
    return TinyTransformerTrainer(vocab, windows, contextLen, seed = seed)
}
