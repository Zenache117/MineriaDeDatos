package PIA;


import java.util.ArrayList;
import java.util.List;

public class KMedoids {
	// Lista de puntos (vectores) que se quieren clusterizar
	private List<double[]> dataPoints;
	//Número de clusters (k)
	private int k;

	//Lista de centroides iniciales
	private List<double[]> initialCentroids;

	//Lista de medoids
	private List<double[]> medoids;

	//Lista de clusters
	private List<List<double[]>> clusters;

	//Constructor de la clase KMedoids
	public KMedoids(List<double[]> dataPoints, int k, List<double[]> initialCentroids) {
		this.dataPoints = dataPoints;
		this.k = k;
		this.initialCentroids = initialCentroids;
		this.medoids = new ArrayList<double[]>();
		this.clusters = new ArrayList<List<double[]>>();
	}

	//Método para ejecutar el algoritmo de k-medoids
	public void run() {
		// Paso 1: Inicializar los medoids con los centroides iniciales
		for (int i = 0; i < k; i++) {
			medoids.add(initialCentroids.get(i));
		}

		boolean changed = true;
		while (changed) {
			// Paso 2: Asociar cada punto al medoid más cercano
			clusters.clear();
			for (int i = 0; i < k; i++) {
				clusters.add(new ArrayList<double[]>());
			}
			for (double[] point : dataPoints) {
				int closestMedoid = -1;
				double minDistance = Double.MAX_VALUE;
				for (int i = 0; i < k; i++) {
					double distance = euclideanDistance(point, medoids.get(i));
					if (distance < minDistance) {
						closestMedoid = i;
						minDistance = distance;
					}
				}
				clusters.get(closestMedoid).add(point);
			}

			// Paso 3: Para cada medoid, encontrar el punto no medoid que minimiza la
			// suma de las distancias a todos los puntos del cluster
			changed = false;
			for (int i = 0; i < k; i++) {
				double[] currentMedoid = medoids.get(i);
				List<double[]> currentCluster = clusters.get(i);
				double currentCost = calculateCost(currentCluster, currentMedoid);
				for (double[] point : currentCluster) {
					int index = dataPoints.indexOf(point);
					double[] tempMedoid = dataPoints.get(index);
					double tempCost = calculateCost(currentCluster, tempMedoid);
					if (tempCost < currentCost) {
						medoids.set(i, tempMedoid);
						changed = true;
						break;
					}
				}
			}
		}
	}

	//Método que devuelve los clusters encontrados
	public List<List<double[]>> getClusters() {
		return clusters;
	}

	//Método para calcular la distancia euclidiana entre dos puntos
	private double euclideanDistance(double[] point1, double[] point2) {
		double distance = 0;
		for (int i = 0; i < point1.length; i++) {
			distance += Math.pow(point1[i] - point2[i], 2);
		}
		return Math.sqrt(distance);
	}

	//Método para calcular el costo de un cluster y un medoid
	private double calculateCost(List<double[]> cluster, double[] medoid){
		double cost = 0;
		for (double[] point : cluster) {
			cost += euclideanDistance(point, medoid);
		}
		return cost;
	}
}
