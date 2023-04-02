package PIA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.naming.InitialContext;

public class Principal {
	private ArrayList<double[]> dataPoints;
	private int k;
	private ArrayList<double[]> initialCentroids;

	public Principal(ArrayList<double[]> dataPoints, int k) {
		this.dataPoints = dataPoints;
		this.k = k;
	}

	public ArrayList<ArrayList<double[]>> partition() {
		generateInitialCentroids();
		Map<double[], Integer> clusterMap = assignDataPointsToClusters();
		ArrayList<ArrayList<double[]>> clusters = updateClusters(clusterMap);
		while (true) {
			Map<double[], Integer> newClusterMap = assignDataPointsToClusters();
			if (clusterMap.equals(newClusterMap)) {
				break;
			}
			clusters = updateClusters(newClusterMap);
			clusterMap = newClusterMap;
		}
		return clusters;
	}

	private void generateInitialCentroids() {
	    initialCentroids = new ArrayList<>();
	    int n = dataPoints.size();
	    double[][] distances = new double[n][n];
	    for (int i = 0; i < n; i++) {
	        for (int j = 0; j < n; j++) {
	            if (i == j) {
	                distances[i][j] = Double.MAX_VALUE;
	            } else {
	                distances[i][j] = euclideanDistance(dataPoints.get(i), dataPoints.get(j));
	            }
	        }
	    }
	    int idx1 = 0, idx2 = 0;
	    double minDistance = Double.MAX_VALUE;
	    for (int i = 0; i < n; i++) {
	        for (int j = i+1; j < n; j++) {
	            if (distances[i][j] < minDistance) {
	                minDistance = distances[i][j];
	                idx1 = i;
	                idx2 = j;
	            }
	        }
	    }
	    initialCentroids.add(dataPoints.get(idx1));
	    initialCentroids.add(dataPoints.get(idx2));
	    for (int i = 2; i < k; i++) {
	        double[] centroid = new double[dataPoints.get(0).length];
	        double minDistanceSum = Double.MAX_VALUE;
	        for (int j = 0; j < n; j++) {
	            if (initialCentroids.contains(dataPoints.get(j))) {
	                continue;
	            }
	            double distanceSum = 0;
	            for (int l = 0; l < initialCentroids.size(); l++) {
	                distanceSum += euclideanDistance(dataPoints.get(j), initialCentroids.get(l));
	            }
	            if (distanceSum < minDistanceSum) {
	                minDistanceSum = distanceSum;
	                centroid = dataPoints.get(j);
	            }
	        }
	        initialCentroids.add(centroid);
	    }
	}

	private Map<double[], Integer> assignDataPointsToClusters() {
		Map<double[], Integer> clusterMap = new HashMap<>();
		for (double[] dataPoint : dataPoints) {
			double closestDistance = Double.MAX_VALUE;
			int closestClusterIndex = -1;
			for (int i = 0; i < initialCentroids.size(); i++) {
				double distance = euclideanDistance(dataPoint, initialCentroids.get(i));
				if (distance < closestDistance) {
					closestDistance = distance;
					closestClusterIndex = i;
				}
			}
			clusterMap.put(dataPoint, closestClusterIndex);
		}
		return clusterMap;
	}

	private ArrayList<ArrayList<double[]>> updateClusters(Map<double[], Integer> clusterMap) {
		ArrayList<ArrayList<double[]>> clusters = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			clusters.add(new ArrayList<>());
		}
		for (Map.Entry<double[], Integer> entry : clusterMap.entrySet()) {
			clusters.get(entry.getValue()).add(entry.getKey());
		}
		for (int i = 0; i < k; i++) {
			double[] centroid = calculateCentroid(clusters.get(i));
			initialCentroids.set(i, centroid);
		}
		return clusters;
	}

	private double euclideanDistance(double[] point1, double[] point2) {
		double sum = 0;
		for (int i = 0; i < point1.length; i++) {
			sum += Math.pow(point1[i] - point2[i], 2);
		}
		return Math.sqrt(sum);
	}

	private double[] calculateCentroid(ArrayList<double[]> cluster) {
	    double[] centroid = new double[cluster.get(0).length];
	    for (int i = 0; i < centroid.length; i++) {
	        double sum = 0;
	        for (int j = 0; j < cluster.size(); j++) {
	            sum += cluster.get(j)[i];
	        }
	        centroid[i] = sum / cluster.size();
	    }
	    return centroid;
	}

	//Ejemplo de uso:
		public static void main(String[] args) {
		ArrayList<double[]> dataPoints = new ArrayList<>();
		dataPoints.add(new double[]{1, 2});
		dataPoints.add(new double[]{2, 1});
		dataPoints.add(new double[]{2, 3});
		dataPoints.add(new double[]{3, 2});
		dataPoints.add(new double[]{6, 5});
		dataPoints.add(new double[]{7, 5});
		dataPoints.add(new double[]{7, 6});
		dataPoints.add(new double[]{8, 6});

		Principal clustering = new Principal(dataPoints, 2);
		ArrayList<ArrayList<double[]>> clusters = clustering.partition();

		System.out.println("Resultado de nueva propuesta K-means");
		for (ArrayList<double[]> cluster : clusters) {
			System.out.println("Cluster:");
			for (double[] dataPoint : cluster) {
				System.out.println("(" + dataPoint[0] + ", " + dataPoint[1] + ")");
			}
		}
		
		KMedoids kmedoids = new KMedoids(dataPoints, clustering.k, clustering.initialCentroids);
		kmedoids.run();
		List<List<double[]>> MedoidClusters = kmedoids.getClusters();
		
		System.out.println("Resultado de nueva propuesta K-medoidsS");
		for (List<double[]> cluster : MedoidClusters) {
			System.out.println("Cluster:");
			for (double[] dataPoint : cluster) {
				System.out.println("(" + dataPoint[0] + ", " + dataPoint[1] + ")");
			}
		}
		
		}
}