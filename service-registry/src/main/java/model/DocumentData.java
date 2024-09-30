package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocumentData implements Serializable {
  private Map<String, Double> termToFrequency = new HashMap<>(); // {term: frequency}

  /**
   * add the frequency of the term termToFrequency hashMap
   * @param term, the term to be added to the hashMap
   * @param frequency, the frequency of the term
   */
  public void putTermFrequency(String term, double frequency) {
    termToFrequency.put(term, frequency);
  }

  /**
   * Get the frequency of the term
   * @param term, the target term to search for frequency
   * @return the frequency of the searched term
   */
  public double getFrequency(String term) {
    return termToFrequency.get(term);
  }
}
