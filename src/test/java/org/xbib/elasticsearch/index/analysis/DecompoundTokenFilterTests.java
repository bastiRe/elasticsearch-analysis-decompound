package org.xbib.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

public class DecompoundTokenFilterTests extends Assert {

    @Test
    public void testExampleFromReadme() throws IOException {
        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";
        String[] expected = {
            "Die",
            "Die",
            "Jahresfeier",
            "Jahr",
            "feier",
            "der",
            "der",
            "Rechtsanwaltskanzleien",
            "Recht",
            "anwalt",
            "kanzlei",
            "auf",
            "auf",
            "dem",
            "dem",
            "Donaudampfschiff",
            "Donau",
            "dampf",
            "schiff",
            "hat",
            "hat",
            "viel",
            "viel",
            "Ökosteuer",
            "Ökosteuer",
            "gekostet",
            "gekosten"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("decompound_analysis.json");
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("decomp");
        Tokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void ignoreKeywordsByDefault() throws IOException {
        String source = "Schlüsselwort";
        String[] expected = {
            "Schlüsselwort",
            "Schlüssel",
            "wort"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("keywords_analysis.json");
        Analyzer analyzer = analysisService.analyzer("decompounding_default");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    @Test
    public void testRespectKeywords() throws IOException {
        String source = "Schlüsselwort";
        String[] expected = {
                "Schlüsselwort"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("keywords_analysis.json");
        Analyzer analyzer = analysisService.analyzer("with_keywords");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    @Test
    public void testDisablingRespectKeywords() throws IOException {
        String source = "Schlüsselwort";
        String[] expected = {
                "Schlüsselwort",
                "Schlüssel",
                "wort"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("keywords_analysis.json");
        Analyzer analyzer = analysisService.analyzer("with_keywords_disabled");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    @Test
    public void testWithSubwordsOnly() throws IOException {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Da",
                "ist",
                "ein",
                "Schlüssel",
                "wort",
                "ein",
                "Bindestrich",
                "wort"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("keywords_analysis.json");
        Analyzer analyzer = analysisService.analyzer("with_subwords_only");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    private static void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i++], termAttr.toString());
        }
        assertEquals(i, expected.length);
    }
}
