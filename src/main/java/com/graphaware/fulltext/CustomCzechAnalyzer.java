package com.graphaware.fulltext;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.cz.CzechStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * {@link Analyzer} for Czech language.
 * <p>
 * Supports an external list of stopwords (words that will not be indexed at
 * all). A default set of stopwords is used unless an alternative list is
 * specified.
 * </p>
 */
public final class CustomCzechAnalyzer extends StopwordAnalyzerBase {
    /** File containing default Czech stopwords. */
    public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";

    /**
     * Returns a set of default Czech-stopwords
     *
     * @return a set of default Czech-stopwords
     */
    public static final CharArraySet getDefaultStopSet(){
        return CustomCzechAnalyzer.DefaultSetHolder.DEFAULT_SET;
    }

    private static class DefaultSetHolder {
        private static final CharArraySet DEFAULT_SET;

        static {
            try {
                DEFAULT_SET = WordlistLoader.getWordSet(IOUtils.getDecodingReader(org.apache.lucene.analysis.cz.CzechAnalyzer.class,
                        DEFAULT_STOPWORD_FILE, StandardCharsets.UTF_8), "#");
            } catch (IOException ex) {
                // default set should always be present as it is part of the
                // distribution (JAR)
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }


    private final CharArraySet stemExclusionTable;

    /**
     * Builds an analyzer with the default stop words ({@link #getDefaultStopSet()}).
     */
    public CustomCzechAnalyzer() {
        this(DefaultSetHolder.DEFAULT_SET);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords a stopword set
     */
    public CustomCzechAnalyzer(CharArraySet stopwords) {
        this(stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words and a set of work to be
     * excluded from the {@link CzechStemFilter}.
     *
     * @param stopwords a stopword set
     * @param stemExclusionTable a stemming exclusion set
     */
    public CustomCzechAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionTable) {
        super(stopwords);
        this.stemExclusionTable = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionTable));
    }

    /**
     * Creates
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
     * used to tokenize all the text in the provided {@link Reader}.
     *
     * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
     *         built from a {@link StandardTokenizer} filtered with
     *         {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter}
     *         , and {@link CzechStemFilter} (only if version is &gt;= LUCENE_31). If
     *         a stem exclusion set is provided via
     *         {@link #CzechAnalyzer(CharArraySet, CharArraySet)} a
     *         {@link SetKeywordMarkerFilter} is added before
     *         {@link CzechStemFilter}.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source;
        if (getVersion().onOrAfter(Version.LUCENE_4_7_0)) {
            source = new StandardTokenizer();
        } else {
            source = new StandardTokenizer40();
        }
        TokenStream result = new StandardFilter(source);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);
        if(!this.stemExclusionTable.isEmpty())
            result = new SetKeywordMarkerFilter(result, stemExclusionTable);
        result = new CzechStemFilter(result);
        result = new ASCIIFoldingFilter(result);
        return new TokenStreamComponents(source, result);
    }
}
