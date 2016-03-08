/*

SEMANTIXS (System for Extraction of doMAin-specific iNformation
from unstructured Text Including compleX Structures)

Copyright (c) 2010 Sushain Pandit.

Info: SEMANTIXS is a system for ontology-guided extraction and semantic
representation of structured information from unstructured text.
For further information, please visit - http://www.sushain.com/semantixs/home

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, see <http://www.gnu.org/licenses/>.

*/

package edu.iastate.airl.semtus.parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.BasicDocument;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.web.HTMLParser;

/**
 * Parser entity
 *
 * @author Bernard Bou
 *
 */
public class Parser
{
    /**
     * Stanford Lexicalized Parser
     */
    protected LexicalizedParser theParser=null;

    /**
     * Constructor
     *
     * @param thisInputStream
     *            object stream for serialized grammar file
     */
    public Parser(ObjectInputStream thisInputStream)
    {
	doLoad(thisInputStream);
    }

    // S Y N C H R O N I Z E D   J O B S

    /**
     * Load a grammar
     *
     * @param thisInputStream
     *            serialized grammar file input stream
     */
    synchronized private void doLoad(ObjectInputStream thisInputStream)
    {
	theParser = load(thisInputStream);
    }

    /**
     * Parse document
     *
     * @param thisDocumentPath
     *            document path
     */
    synchronized private List<Tree> doParse(String thisDocumentPath)
    {
	return parseDocument(thisDocumentPath);
    }

    // A C T I O N S

    /**
     * Load the grammar
     *
     * @param thisInputStream
     *            serialized grammar file input stream
     * @return LexicalizedParser
     */
    protected LexicalizedParser load(ObjectInputStream thisInputStream)
    {
	try
	{
	    LexicalizedParser thisParser = new LexicalizedParser(thisInputStream);

		thisParser.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

	    return thisParser;
	}
	catch (Exception e)
	{
	    System.out.println(e.toString());
	    return null;
	}
    }

    /**
     * Parse
     *
     * @param thisDocumentPath
     *            document path
     */
    public List<Tree> parse(final String thisDocumentPath)
    {
	return doParse(thisDocumentPath);
    }

    /**
     * Get document from text
     *
     * @param thisText
     *            text
     * @return document
     */
    static public BasicDocument<HasWord> getDocument(String thisText)
    {
	BasicDocument<HasWord> thisDoc = BasicDocument.init(thisText);
	return thisDoc;
    }

    // S E N T E N C E S

    /**
     * Get sentences from text
     *
     * @param thisText
     *            text
     * @return list of sentences
     */
    static public List<Sentence<Word>> getSentences(String thisText)
    {
	return getSentences(getDocument(thisText));
    }

    /**
     * Get sentences from words
     *
     * @param theseWords
     *            words
     * @return list of sentences
     */
    static public List<Sentence<Word>> getSentences(List<Word> theseWords)
    {
	WordToSentenceProcessor<Word, String, Word> thisSentenceProcessor = new WordToSentenceProcessor<Word, String, Word>();
	List<List<Word>> theseProtoSentences = thisSentenceProcessor.process(theseWords);
	List<Sentence<Word>> theseSentences = new ArrayList<Sentence<Word>>();
	for (List<Word> thisProtoSentence : theseProtoSentences)
	    theseSentences.add(new Sentence<Word>(thisProtoSentence));
	return theseSentences;
    }

    // P A R S E

    /**
     * Parse sentence
     *
     * @param thisSentence
     *            sentence to parse
     * @return parsed tree
     */
    public Tree parse(Sentence<Word> thisSentence)
    {
	try
	{
	    if (theParser.parse(thisSentence))
	    {
		Tree thisTree = theParser.getBestParse();
		return thisTree;
	    }
	}
	catch (Throwable t)
	{
	    System.err.println(t.toString());
	}
	return null;
    }

    /**
     * Parse sentences
     *
     * @param theseSentences
     *            sentences to parse
     * @return list of parse trees
     */
    public List<Tree> parse(List<Sentence<Word>> theseSentences)
    {
	List<Tree> theseTrees = new ArrayList<Tree>();
	for (Sentence<Word> thisSentence : theseSentences)
	{
	    Tree thisTree = parse(thisSentence);
	    theseTrees.add(thisTree);
	}
	return theseTrees;
    }

    /**
     * Parse document
     *
     * @param thisDocumentPath
     *            document path
     * @return list of parse trees
     */
    public List<Tree> parseDocument(String thisDocumentPath)
    {
	List<Sentence<Word>> theseSentences = getSentences(thisDocumentPath);
	return parse(theseSentences);
    }

    // H E L P E R

    /**
     * Whether this parser is valid
     *
     * @return true if this parser is valid
     */
    public boolean isValid()
    {
	return theParser != null;
    }

    /**
     * Get morphology base
     *
     * @param thisWord
     *            word
     * @return morphology base
     */
    static public String morphology(Word thisWord)
    {
	try
	{
	    Morphology thisMorphology = new Morphology();
	    Word thisBase = thisMorphology.stem(thisWord);
	    return thisBase.word();
	}
	catch (Throwable e)
	{
	    return null;
	}
    }

    /**
     * Get morphology base
     *
     * @param thisString
     *            string
     * @return morphology base
     */
    static public String morphology(String thisString)
    {
	Word thisWord = new Word(thisString);
	return morphology(thisWord);
    }
}
