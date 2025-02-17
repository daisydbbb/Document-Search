import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.DocumentData;
import search.TFIDF;

public class SequencialSearch {
  public static final String BOOKS_DIRECTORY = "./resources/books";
  public static final String SEARCH_QUERY_1 = "The best detective";

  public static void main(String [] args) throws FileNotFoundException {
    File documentsDirectory = new File(BOOKS_DIRECTORY);
    List<String> documents = Arrays.asList(documentsDirectory.list())
            .stream()
            .map(documentName -> BOOKS_DIRECTORY +"/"+documentName)
            .collect(Collectors.toList());
    List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_1);
    findMostRelevantDocument(documents, terms);
  }
  private static void findMostRelevantDocument(List<String> documents, List<String> terms) throws FileNotFoundException {
    Map<String, DocumentData> documentResults = new HashMap<>();
    for (String document : documents) {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(document));
      List<String> lines = bufferedReader.lines().collect(Collectors.toList());
      List<String> words = TFIDF.getWordsFromLines(lines);
      DocumentData documentData = TFIDF.createDocumentData(words, terms);
      documentResults.put(document, documentData);
    }
    Map<Double, List<String>> documentByScore = TFIDF.getDocumentsSortedByScore(terms,documentResults);
    printResults(documentByScore);
  }

  private static void printResults(Map<Double, List<String>> documentByScore) {
    for (Map.Entry<Double, List<String>> docScorePair: documentByScore.entrySet()) {
      double score = docScorePair.getKey();
      for (String document: docScorePair.getValue()) {
        System.out.println(String.format("Book: %s - score %f", document.split("/")[3], score));
      }
    }
  }
}
