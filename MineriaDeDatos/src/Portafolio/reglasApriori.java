package Portafolio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class reglasApriori {
    private static final double MIN_SUPPORT = 0;
    public static void main(String[] args) {
    	
    	SeleccionarArchivo select = new SeleccionarArchivo();
		String csvPath = select.selectFile();

        List<List<String>> transactions = readTransactionsFromCSV(csvPath);
        List<Set<String>> frequentItemsets = generateFrequentItemsets(transactions);
        List<AssociationRule> associationRules = generateAssociationRules(frequentItemsets, transactions);

        // Mostrar las reglas de asociación generadas
        for (AssociationRule rule : associationRules) {
            int nx = countFrequency(rule.getAntecedent(), transactions);
            int ny = countFrequency(rule.getConsequent(), transactions);
            int nx_y = countFrequency(concatenateLists(rule.getAntecedent(), rule.getConsequent()), transactions);
            int totalTransactions = transactions.size();

            double support = (double) nx_y / totalTransactions;
            double confidence = (double) nx_y / nx;
            double lift = ((double) nx_y * totalTransactions) / (nx * ny);

            System.out.println("Regla: " + rule.getAntecedent() + " -> " + rule.getConsequent());
            System.out.println("Nx: " + nx);
            System.out.println("Ny: " + ny);
            System.out.println("Nx^y: " + nx_y);
            System.out.println("Support: " + support);
            System.out.println("Confidence: " + confidence);
            System.out.println("Lift: " + lift);
            System.out.println("-------------------------");
        }
    }

    private static List<List<String>> readTransactionsFromCSV(String csvPath) {
        List<List<String>> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            // Leer y descartar la primera línea (encabezados)
            String headerLine = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");

                // Convertir los datos numéricos a categóricos
                if (Double.parseDouble(columns[0]) >= 150) {
                    columns[0] = "A";
                } else {
                    columns[0] = "B";
                }

                double windValue = Double.parseDouble(columns[1]);
                if (windValue <= 5) {
                    columns[1] = "B";
                } else if (windValue <= 12) {
                    columns[1] = "M";
                } else {
                    columns[1] = "A";
                }

                if (Double.parseDouble(columns[2]) <= 63) {
                    columns[2] = "F";
                } else {
                    columns[2] = "C";
                }

                // Agregar la transacción a la lista de transacciones
                List<String> transaction = new ArrayList<>();
                for (String column : columns) {
                    transaction.add(column.trim());
                }
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private static class AssociationRule {
        private List<String> antecedent;
        private List<String> consequent;
        private double confidence;
    
        public AssociationRule(List<String> antecedent, List<String> consequent, double confidence) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.confidence = confidence;
        }
    
        public List<String> getAntecedent() {
            return antecedent;
        }
    
        public List<String> getConsequent() {
            return consequent;
        }
    
        public double getConfidence() {
            return confidence;
        }
    }

    private static List<Set<String>> generateFrequentItemsets(List<List<String>> transactions) {
        List<Set<String>> frequentItemsets = new ArrayList<>();
    
        Set<String> uniqueItems = new HashSet<>();
        for (List<String> transaction : transactions) {
            uniqueItems.addAll(transaction);
        }
    
        List<Set<String>> currentLevelItemsets = new ArrayList<>();
        for (String item : uniqueItems) {
            Set<String> itemset = new HashSet<>();
            itemset.add(item);
            currentLevelItemsets.add(itemset);
        }
    
        int totalTransactions = transactions.size();
    
        while (!currentLevelItemsets.isEmpty()) {
            List<Set<String>> nextLevelItemsets = new ArrayList<>();
    
            for (Set<String> itemset : currentLevelItemsets) {
                int supportCount = countSupport(itemset, transactions);
                double support = (double) supportCount / totalTransactions;
    
                if (support >= MIN_SUPPORT) {
                    frequentItemsets.add(itemset);
    
                    Set<String> itemsetCopy = new HashSet<>(itemset);
                    for (String item : uniqueItems) {
                        if (!itemset.contains(item)) {
                            itemsetCopy.add(item);
                            nextLevelItemsets.add(new HashSet<>(itemsetCopy));
                            itemsetCopy.remove(item);
                        }
                    }
                }
            }
    
            currentLevelItemsets = nextLevelItemsets;
        }
    
        return frequentItemsets;
    }
    
    private static List<AssociationRule> generateAssociationRules(List<Set<String>> frequentItemsets, List<List<String>> transactions) {
        List<AssociationRule> associationRules = new ArrayList<>();
    
        for (Set<String> itemset : frequentItemsets) {
            if (itemset.size() > 1) {
                List<AssociationRule> rules = generateRules(itemset, transactions);
                associationRules.addAll(rules);
            }
        }
    
        return associationRules;
    }

    private static List<String> concatenateLists(List<String> list1, List<String> list2) {
        List<String> concatenatedList = new ArrayList<>(list1);
        concatenatedList.addAll(list2);
        return concatenatedList;
    }
    private static int countSupport(Set<String> itemset, List<List<String>> transactions) {
        int count = 0;
        for (List<String> transaction : transactions) {
            if (transaction.containsAll(itemset)) {
                count++;
            }
        }
        return count;
    }


    private static List<AssociationRule> generateRules(Set<String> itemset, List<List<String>> transactions) {
        List<AssociationRule> rules = new ArrayList<>();
    
        List<String> itemList = new ArrayList<>(itemset);
        List<Set<String>> subsets = generateSubsets(itemList);
    
        for (Set<String> antecedent : subsets) {
            if (antecedent.size() == 1) {
                List<String> consequent = new ArrayList<>(itemList);
                consequent.removeAll(antecedent);
    
                if (consequent.size() == 1) {
                    double confidence = calculateConfidence(antecedent, consequent, transactions);
    
                    AssociationRule rule = new AssociationRule(new ArrayList<>(antecedent), new ArrayList<>(consequent), confidence);
                    rules.add(rule);
                }
            }
        }
    
        return rules;
    }

    private static List<Set<String>> generateSubsets(List<String> itemList) {
        List<Set<String>> subsets = new ArrayList<>();
    
        int n = itemList.size();
        for (int i = 1; i < (1 << n) - 1; i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(itemList.get(j));
                }
            }
            subsets.add(subset);
        }
    
        return subsets;
    }
    

    private static int countFrequency(List<String> itemset, List<List<String>> transactions) {
        int count = 0;
        for (List<String> transaction : transactions) {
            if (transaction.containsAll(itemset)) {
                count++;
            }
        }
        return count;
    }
    private static int countFrequencyInSameRow(List<String> antecedent, List<String> consequent, List<List<String>> transactions) {
        int count = 0;
        for (List<String> transaction : transactions) {
            if (transaction.containsAll(antecedent) && transaction.containsAll(consequent)) {
                count++;
            }
        }
        return count;
    }

    private static double calculateConfidence(Set<String> antecedent, List<String> consequent,
            List<List<String>> transactions) {
        int antecedentCount = 0;
        int consequentCount = 0;

        for (List<String> transaction : transactions) {
            if (Collections.indexOfSubList(transaction, new ArrayList<>(antecedent)) != -1) {
                antecedentCount++;
                if (Collections.indexOfSubList(transaction, consequent) != -1) {
                    consequentCount++;
                }
            }
        }

        return (double) consequentCount / antecedentCount;
    }
}