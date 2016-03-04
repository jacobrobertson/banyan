package com.robestone.species.parse;

import static com.robestone.species.parse.WikipediaTaxoboxParser.W;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * Uses some well-known patterns to find the common name from a page.
 * @author jacob
 *
 */
public class WikipediaCommonNamePatternParser {

	private static final int MAX_LEN_TO_CHECK = 800;
	
	private static final String Q1 = "(?:'{2,}|'*\")";
	private static final String Q2 = "(?:'{2,}|\"'*)";
	private static final String WORDS = W + "\\[\\]\\(\\)";
	private static final String W2 = W + "[0-9],\\*\\[\\]\\|\\\"\\(\\)\\':<>\\=\\{\\};/\\&–"; //that's not a normal dash at the end
	private static final String THE = "(?: the)?";
	private static final String COMMON_CAPTURE = "([WORDS]+'?[WORDS]+)"; // allow one ' - for "brown's"
	private static final String PUNCTUATION = "\\s*[,\\.\\);\\:'\"]";
 
	private static final String PHRASES = 
		"(?:[Kk]nown by the common names?" + THE +
		"|[Kk]nown and classified as" + THE +
		"|[Cc]ommonly known as" + THE +
		"|[Mm]embers are known as" + THE +
		"|[Cc]ommonly referred to as" + THE +
		"|[Cc]ollectively referred to as" + THE +
		"|the scientific name for" + THE +
		"|[cC]ommon names [\\s\\p{L}]{0,10}includ(?:e|ing)" + THE +
		"|[cC]ommon names? for it(?: are)?(?: is)?" + THE +
		"|[Oo]ccasionally known as" + THE +
		"|[Kk]nown collectively as" + THE +
		"|[Cc]ommonly called" + THE +
		"|[Kk]nown as" + THE +
		"|\\[\\[[cC]ommon name\\]\\]" + THE +
		"|[Kk]nown generally as" + THE +
		"|common names?\\s*(?:are|is)?" + THE +
		"|[Oo]ften called" + THE +
		")";
	
	private String[] patterns = {
			"Q1LATIN_NAMEQ2[WORDS2]*?\\(?" + PHRASES  + "\\s+Q1COMMON_CAPTUREQ2",
			"Q1LATIN_NAMEQ2[WORDS2]*?\\(?" + PHRASES  + "\\s+Q1?COMMON_CAPTUREQ2?" + PUNCTUATION,
			
			// '''Oregon cherry''' or '''Bitter cherry''' ('''''Prunus emarginata''''') is a species of ''[[Prunus]]''
			// The '''fossa''' (''Cryptoprocta ferox'')
			"Q1COMMON_CAPTUREQ2\\s+\\(Q1LATIN_NAMEQ2\\)",
			"Q1LATIN_NAMEQ2\\s+\\(Q1COMMON_CAPTUREQ2",
			
			// '''''Formica sanguinea''''', the slavemaker ant,
			"Q1LATIN_NAMEQ2,(?: or)? the Q1?COMMON_CAPTUREQ2?,",
			
			"Q1LATIN_NAMEQ2 or Q1COMMON_CAPTUREQ2",
			
			//The '''pygmy-falcons''', '''''Polihierax''''',
			"The\\s+Q1COMMON_CAPTUREQ2\\s*,\\s+Q1LATIN_NAMEQ2",
			
			// The '''Apioceridae''', or '''flower-loving flies''',
			"The\\s+Q1LATIN_NAMEQ2\\s*, or\\s+Q1COMMON_CAPTUREQ2",
			
			// '''Caenidae''', or the Small Squaregill Mayflies,
			"Q1LATIN_NAMEQ2\\s*, or the COMMON_CAPTURE,",
			
			// '''''Acontias''''', the '''lance skinks''', is a [[genus]] of limbless [[skink]]s
			"Q1LATIN_NAMEQ2\\s*,(?: or)? the Q1COMMON_CAPTUREQ2,",
			
			// The '''Clouded Buff''' ''(Diacrisia sannio)'' is a [[moth]] of the family [[Arctiidae]].
			"[Tt]he Q1COMMON_CAPTUREQ2 Q1\\(LATIN_NAME\\)Q2",
			
			// '''''Vibrio cholerae''''' (also ''Kommabacillus'')
			"Q1LATIN_NAMEQ2\\s+\\(also Q1COMMON_CAPTUREQ2",
			
			// :'''''Common names:''' Mohave rattlesnake,
			"Q1Common names?\\s*:\\s*Q2\\s+COMMON_CAPTURE" + PUNCTUATION,
			
			// '''''Ulex gallii''''', '''Western Gorse'''
			"Q1LATIN_NAMEQ2,\\s+Q1COMMON_CAPTUREQ2",
			
			// Their [[common name]] '''velvet ant'''
			"[Tt]he(?:ir)?\\s+\\[\\[common name\\]\\]\\s+Q1COMMON_CAPTUREQ2",
	};
	
	public WikipediaCommonNamePatternParser() {
	}
	public String parse(String latinName, String page) {
		if (page.length() > MAX_LEN_TO_CHECK) {
			// we do this so that the common name isn't buried deep in the article
			// which often means it's the common name of some subspecies or something
			page = page.substring(0, MAX_LEN_TO_CHECK);
		}
		String bestFound = null;
		int bestFoundPos = Integer.MAX_VALUE;
		for (String spattern: patterns) {
			spattern = spattern.replace("LATIN_NAME", latinName);
			spattern = spattern.replace("COMMON_CAPTURE", COMMON_CAPTURE);
			spattern = spattern.replace("WORDS2", W2);
			spattern = spattern.replace("WORDS", WORDS);
			spattern = spattern.replace("Q1", Q1);
			spattern = spattern.replace("Q2", Q2);
			Pattern pattern = Pattern.compile(spattern);
			String found = parse(pattern, page);
			if (found != null) {
				int pos = page.indexOf(found);
				if (pos < bestFoundPos) {
					found = clean(found);
					if (found != null) {
						bestFound = found;
						bestFoundPos = pos;
					}
				}
			}
		}
		return bestFound;
	}
	private String clean(String s) {
		s = StringUtils.remove(s, '[');
		s = StringUtils.remove(s, ']');
		int spaces = StringUtils.countMatches(s, " ");
		if (spaces > 3) {
			s = null;
		}
		return s;
	}
	public String parse(Pattern pattern, String page) {
		return WikiSpeciesParser.getGroup(pattern, page);
	}
	
}
