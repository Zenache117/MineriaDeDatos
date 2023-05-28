package Portafolio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class reglasApriori {
    private static final double MIN_SUPPORT = 0.1;

    public static void main(String[] args) {
    	SeleccionarArchivo select = new SeleccionarArchivo();
		String csvFile = select.selectFile();
        String line = "";
        String cvsSplitBy = ",";
        List<List<String>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String[] headers = br.readLine().split(cvsSplitBy);
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                List<String> rowData = new ArrayList<>();
                for (int i = 0; i < row.length; i++) {
                    if (isNumeric(row[i])) {
                        rowData.add(headers[i] + "=" + convertToCategorical(Double.parseDouble(row[i])));
                    } else {
                        rowData.add(headers[i] + "=" + row[i]);
                    }
                }
                data.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Set<String>, Integer> frequentItemsets = generateFrequentItemsets(data);
        generateAssociationRules(frequentItemsets, data.size());
    }

    private static Map<Set<String>, Integer> generateFrequentItemsets(List<List<String>> data) {
        Map<Set<String>, Integer> frequentItemsets = new HashMap<>();
        Map<Set<String>, Integer> itemsets = generateInitialItemsets(data);

        while (!itemsets.isEmpty()) {
            Map<Set<String>, Integer> newItemsets = new HashMap<>();
            for (Map.Entry<Set<String>, Integer> entry : itemsets.entrySet()) {
                if ((double) entry.getValue() / data.size() >= MIN_SUPPORT) {
                    frequentItemsets.put(entry.getKey(), entry.getValue());
                    newItemsets.putAll(generateCandidates(entry.getKey(), itemsets.keySet()));
                }
            }
            itemsets = countCandidates(newItemsets, data);
        }

        return frequentItemsets;
    }

    private static Map<Set<String>, Integer> generateInitialItemsets(List<List<String>> data) {
        Map<Set<String>, Integer> itemsets = new HashMap<>();

        for (List<String> transaction : data) {
            for (String item : transaction) {
                Set<String> itemset = new HashSet<>();
                itemset.add(item);
                itemsets.put(itemset, itemsets.getOrDefault(itemset, 0) + 1);
            }
        }

        return itemsets;
    }

    private static Map<Set<String>, Integer> generateCandidates(Set<String> itemset, Set<Set<String>> keys) {
        Map<Set<String>, Integer> candidates = new HashMap<>();

        for (Set<String> key : keys) {
            Set<String> union = new HashSet<>(itemset);
            union.addAll(key);
            if (union.size() == itemset.size() + 1) {
                Set<Set<String>> subsets = generateSubsets(union);
                if (keys.containsAll(subsets)) {
                    candidates.put(union, 0);
                }
            }
        }

        return candidates;
    }

    private static Set<Set<String>> generateSubsets(Set<String> set) {
        Set<Set<String>> subsets = new HashSet<>();

        for (String element : set) {
            Set<String> subset = new HashSet<>(set);
            subset.remove(element);
            subsets.add(subset);
        }

        return subsets;
    }

    private static Map<Set<String>, Integer> countCandidates(Map<Set<String>, Integer> candidates, List<List<String>> data) {
        Map<Set<String>, Integer> countedCandidates = new HashMap<>();

        for (Map.Entry<Set<String>, Integer> candidate : candidates.entrySet()) {
            int count = 0;
            for (List<String> transaction : data) {
                if (transaction.containsAll(candidate.getKey())) {
                    count++;
                }
            }
            countedCandidates.put(candidate.getKey(), count);
        }

        return countedCandidates;
    }

    private static void generateAssociationRules(Map<Set<String>, Integer> frequentItemsets, int numTransactions) {
        for (Map.Entry<Set<String>, Integer> entry : frequentItemsets.entrySet()) {
            if (entry.getKey().size() > 1) {
                Set<Set<String>> subsets = generateSubsets(entry.getKey());
                for (Set<String> subset : subsets) {
                    double support = (double) entry.getValue() / numTransactions;
                    double confidence = (double) entry.getValue() / frequentItemsets.get(subset);
                    double lift = confidence / ((double) frequentItemsets.get(entry.getKey()) / numTransactions);

                    System.out.println(subset + " => " + getComplement(entry.getKey(), subset));
                    System.out.println("Support: " + support);
                    System.out.println("Confidence: " + confidence);
                    System.out.println("Lift: " + lift);
                    System.out.println();
                }
            }
        }
    }

    private static Set<String> getComplement(Set<String> set, Set<String> subset) {
        Set<String> complement = new HashSet<>(set);
        complement.removeAll(subset);
        return complement;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String convertToCategorical(double value) {
        if (value >= 0 && value <= 60) {
            return "Low";
        } else if (value > 60 && value <= 120) {
            return "Medium";
        } else if (value > 120 && value <= 180) {
            return "High";
        } else {
            return "Very High";
        }
    }
}
