package search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.DocumentData;

public class TFIDF {
  /**
   *
   * @param words, the list of words to search to target term
   * @param term, the target term for search
   * @return the term frequency of the target term all words
   */
  public static double calculateTermFrequency(List<String> words, String term) {
    long count = 0;
    for (String word : words) {
      if (term.equalsIgnoreCase(word)) {
        count ++;
      }
    }
    double termFrequency = (double) count/words.size();
    return termFrequency;
  }

  /**
   * Calculate all the term frequencies for the given document
   * @param words, list of words to search
   * @param terms, list of target terms for search
   * @return the DocumentData object with the term frequency data
   */
  public static DocumentData createDocumentData(List<String> words, List<String> terms) {
    DocumentData documentData = new DocumentData();
    for (String term: terms) {
      double termFreq = calculateTermFrequency(words, term);
      documentData.putTermFrequency(term, termFreq);
    }
    return documentData;
  }

  /**
   * Calculate the inverse frequency of the term in the document
   * @param term, the target term to search for
   * @param documentResults, hashMap of {document name : {terms: freq1, term2: freq2 ...}} pair
   * @return the inverse frequency of the term
   */
  private static double getInverseDocumentFrequency(String term, Map<String, DocumentData> documentResults) {
    double nt = 0;
    for (String document:documentResults.keySet()) {
      DocumentData documentData = documentResults.get(document);
      double termFrequency = documentData.getFrequency(term);
      if (termFrequency > 0.0){
        nt ++;
      }

    }
    return nt==0? 0: Math.log10(documentResults.size()/nt);
  }

  /**
   * Calculate idf for all the terms and store them in hashmap
   * @param terms, the list of terms to search for
   * @param documentResults, hashMap of {document name: {terms: freq1, term2: freq2 ...}} pair
   * @return the hashMap with {term: idf} pair
   */
  private static Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms,
                                                                          Map<String, DocumentData> documentResults) {
    Map<String, Double> termToIDF = new HashMap<>();
    for (String term : terms) {
      double idf = getInverseDocumentFrequency(term, documentResults);
      termToIDF.put(term, idf);
    }
    return termToIDF;
  }

  /**
   * Calculate the total score of the list of terms in the single document
   * @param terms, the list of term to search for
   * @param documentData, hashMap of {string: frequency}
   * @param termToInverseDocumentFrequency, hashMap of {string: idf}
   * @return the total score of all terms in list calculated by multiplication of tf*idf
   */
  private static double calculateDocumentScore(List<String> terms, DocumentData documentData,
                                               Map<String, Double> termToInverseDocumentFrequency) {
    double score = 0;
    for (String term: terms) {
      double termFrequency = documentData.getFrequency(term);
      double inverseTermFrequency = termToInverseDocumentFrequency.get(term);
      score += termFrequency*inverseTermFrequency;
    }
    return score;
  }

  /**
   * Calculate the score for all the documents and sort them in descending order
   * @param terms, the list of terms for search
   * @param documentResults, hashMap of {document name: {terms: freq1, term2: freq2 ...}} pair
   * @return the tree map of {score1: [doc1, doc2...], score2: [doc3, doc4...]} in descending order of score
   */
  public static Map<Double, List<String>> getDocumentsSortedByScore(List<String> terms,
                                                                   Map<String, DocumentData> documentResults) {
    TreeMap<Double, List<String>> scoreToDocuments = new TreeMap<>();
    Map<String, Double> termToInverseDocumentFrequency = getTermToInverseDocumentFrequencyMap(terms, documentResults);
    for (String document : documentResults.keySet()) {
      DocumentData documentData = documentResults.get(document);
      double score = calculateDocumentScore(terms, documentData, termToInverseDocumentFrequency);
      addDocumentScoreToTreeMap(scoreToDocuments, score, document);
    }
    return scoreToDocuments.descendingMap();
  }

  /**
   * Add the score with list of documents to treemap
   * @param scoreToDocuments, treemap of {string: [docs]}
   * @param score, calculated score of current document
   * @param document, name of the document to be added to the treemap
   */
  private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDocuments, double score, String document) {
    List<String> documentsWithCurrentScore = scoreToDocuments.get(score);
    if (documentsWithCurrentScore == null) {
      documentsWithCurrentScore = new ArrayList<>();
    }
    documentsWithCurrentScore.add(document);
    scoreToDocuments.put(score, documentsWithCurrentScore);
  }

  /**
   * Break the long string list into array of string with general regex
   * @param line, the input string that can be super long
   * @return the list that contains the string after breaking down the input line
   */
  public static List<String> getWordsFromLine(String line) {
    return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
  }

  /**
   * Get all the words in all the lines of document
   * @param lines, the list of containing multiple lines
   * @return the list of all the words in list of lines
   */
  public static List<String> getWordsFromLines(List<String> lines) {
    List<String> words = new ArrayList<>();
    for (String line : lines) {
      words.addAll(getWordsFromLine(line));
    }
    return words;
  }

}
