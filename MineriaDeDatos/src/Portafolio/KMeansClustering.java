package Portafolio;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KMeansClustering {
	// Esta será la lista que contendrá todas las distancias minnimas
	List<Double> sigmaDisMin = new ArrayList<>();

	// La lista de distancias tiene que ser una lista de listas porque la lista que
	// se contiene representa las distancias de todas las instancias hacia un centro
	// y la lissta general contiene la de todos los centros
	List<List<Double>> distancias = new ArrayList<>();

	// Una lista para guardar los clusters
	List<List<List<Double>>> clusters = new ArrayList<>();

	// Una lista para guardar las distancias de clusters
	List<List<Double>> distClusters = new ArrayList<>();

	String rutaCarpetaDestino;

	public static void main(String[] args) {
		// Seleccionar CSV
		SeleccionarArchivo select = new SeleccionarArchivo();
		String rutaArchivo = select.selectFile();
		List<CSVRecord> registro = new ArrayList<>();
		try (CSVParser parser = new CSVParser(new FileReader(rutaArchivo), CSVFormat.DEFAULT)) {
			registro = parser.getRecords();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Generamos esta instancia para desplazarnos a travez de los metodos de la
		// clase
		KMeansClustering kmeans = new KMeansClustering();

		List<List<Double>> registroNormalizado = new ArrayList<>();
		registroNormalizado = kmeans.Normalizar(registro);

		// Seleccionar la carpeta destino para guardar el txt
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		kmeans.rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Iniciamos la cantidad de centros en 2
		int cantCentros = 2;
		boolean mejoraCentros = true;
		boolean mejoraLocal = true;
		double distanciaTotal = 0.0;
		double nuevaDistancia = 0.0;
		double mejorValor = -1;
		int mejorCantCentros = 2;

		// Esta lista contendrá los valores de los centros como tal en función de las
		// variables del registro
		List<List<Double>> centros = new ArrayList<>();
		List<List<Double>> nuevosCentros = new ArrayList<>();

		while (mejoraCentros == true && cantCentros < 6) {
			int i = 0;
			while (mejoraLocal == true) {

				// En caso que sea la primera vez que se calculan los centros entra aqui para
				// tener valores con loss cauless comparar desspuess
				if (i == 0) {
					centros = kmeans.centrosIniciales(cantCentros, registroNormalizado);
					distanciaTotal = kmeans.distanciaCentros(centros, registroNormalizado, kmeans);
				} else {

					// Se calculan loss nuevos centros y su distancia
					nuevosCentros = kmeans.nuevosCentros(cantCentros, registroNormalizado, centros);
					nuevaDistancia = kmeans.distanciaCentros(nuevosCentros, registroNormalizado, kmeans);

					// Si hay una mejora se actualizan los valores en caso contrario se cambia la
					// variable de mejora para salir del ciclo y registrar la mejor distancia con la
					// cantidad de centros actual.
					if (nuevaDistancia < distanciaTotal) {
						distanciaTotal = nuevaDistancia;
						centros = nuevosCentros;
					} else {
						mejoraLocal = false;
					}
				}

				// Esto ayuda a decirle al ciclo que no es la primera iteración con ese calor
				// de
				// centros
				i++;
			}

			if (mejorValor == -1 || distanciaTotal < mejorValor) {
				mejorValor = distanciaTotal;
				mejorCantCentros = cantCentros;
				mejoraLocal = true;
			} else {
				mejoraCentros = false;
			}

			cantCentros++;

		}
		System.out.println("La cantidad de centros encontrada fue de: " + mejorCantCentros + " con una distancia de: "
				+ mejorValor);
	}

	public List<List<Double>> Normalizar(List<CSVRecord> registro) {
		// Calcula el número de columnas en el registro CSV
		int numColumnas = registro.get(0).size();

		// Crea arreglos para almacenar los valores mínimos, máximos, m y b para cada
		// columna
		double[] minValues = new double[numColumnas];
		double[] maxValues = new double[numColumnas];
		double[] mValues = new double[numColumnas];
		double[] bValues = new double[numColumnas];

		// Inicializa los valores mínimos y máximos con el primer valor de cada
		// columna
		for (int i = 0; i < numColumnas; i++) {
			minValues[i] = -1;
			maxValues[i] = -1;
		}

		// Encuentra los valores mínimos y máximos para cada columna
		for (CSVRecord fila : registro) {
			for (int i = 0; i < numColumnas; i++) {
				double valor = Double.parseDouble(fila.get(i).replace(",", ""));
				if (valor < minValues[i] || minValues[i] == -1) {
					minValues[i] = valor;
				}
				if (valor > maxValues[i] || maxValues[i] == -1) {
					maxValues[i] = valor;
				}
			}
		}

		// Calcula los valores de m y b para cada columna
		for (int i = 0; i < numColumnas; i++) {
			mValues[i] = 1 / (maxValues[i] - minValues[i]);
			bValues[i] = (-1) * minValues[i] * mValues[i];
		}

		// Crea una lista para almacenar el registro CSV normalizado
		List<List<Double>> registroNormalizado = new ArrayList<>();

		// Normaliza los datos en cada columna y almacena los valores normalizados en la
		// lista
		for (CSVRecord fila : registro) {
			List<Double> filaNormalizada = new ArrayList<>();
			for (int i = 0; i < numColumnas; i++) {
				double valor = Double.parseDouble(fila.get(i).replace(",", ""));
				double valorNormalizado = valor * mValues[i] + bValues[i];
				filaNormalizada.add(valorNormalizado);
			}
			registroNormalizado.add(filaNormalizada);
		}

		return registroNormalizado;

	}

	public double distanciaCentros(List<List<Double>> centros, List<List<Double>> registroNormalizado,
			KMeansClustering kmeans) {

		// Se limpian las listas para asegurarse de poder trabajar con ellas
		kmeans.distancias.clear();
		kmeans.sigmaDisMin.clear();
		kmeans.clusters.clear();
		kmeans.distClusters.clear();
		// Inicializamos la lista de distClusters
		for (int i = 0; i < centros.size(); i++) {
			List<Double> valor = new ArrayList<>();
			valor.add(null);
			kmeans.distClusters.add(valor);
		}

		// Inicializamos la lista de clusters
		for (int i = 0; i < centros.size(); i++) {
			List<Double> valor = new ArrayList<>();
			valor.add(null);
			List<List<Double>> valores = new ArrayList<>();
			valores.add(valor);
			kmeans.clusters.add(valores);
		}

		// Se recorren los centros
		for (List<Double> filaCentro : centros) {

			// Se declara una lista local para las distancias del centro actual
			List<Double> distCent = new ArrayList<>();

			// Se recorre el registro
			for (List<Double> fila : registroNormalizado) {

				// Esta variable apoya a realizar primero la sumatoria de los cuadrados de la
				// diferencia entre el valor de los centros de la variable del registro y la
				// instancia que se esta revisando
				double sigmaCuadrados = 0;
				for (int i = 0; i < fila.size(); i++) {
					sigmaCuadrados = sigmaCuadrados + Math.pow((fila.get(i) - filaCentro.get(i)), 2);
				}
				// Se eleva al inverso de la cantidad de variables del registro para la
				// distancia euclidiana
				// (declarar el .0 en el 1.0 ayuda a que la operación de divisiónn se declare
				// como un valor double y no como uno entero)
				double distancia = Math.pow(sigmaCuadrados, (1.0 / registroNormalizado.get(0).size()));
				distCent.add(distancia);
			}

			// Se agregan las distancias el centro recorrido a la lista de distancias
			// generales
			distancias.add(distCent);
		}
		// Esta lista representa distancias de cada instancia a los diferenbtes centros
		List<List<Double>> distanciaACentros = new ArrayList<>();

		// Segun la cantidad de variables del registro recorremos las distancias de cada
		// instancia a cada centro y se elige la minima entre ellas para agregarse a la
		// lista de las distancias minimas
		int numElementos = distancias.get(0).size();
		for (int i = 0; i < numElementos; i++) {
			// Esta lista apoya a ser la que contenga los elementos en la posición indicada
			// en el indice i (el centro al que se esta tomando como referencia para la
			// distancia)
			List<Double> valores = new ArrayList<>();
			for (List<Double> lista : distancias) {
				valores.add(lista.get(i));
			}
			distanciaACentros.add(valores);
			/*
			 * El método Collections.min(valores) toma como argumento una colección de
			 * valores y devuelve el valor mínimo de esa colección. Para hacer esto, el
			 * método itera sobre todos los elementos de la colección y compara cada
			 * elemento con el valor mínimo actual. Si encuentra un elemento que es menor
			 * que el valor mínimo actual, actualiza el valor mínimo con ese elemento. Al
			 * final del proceso, el método devuelve el valor mínimo encontrado en la
			 * colección. En otras palabras, el método Collections.min(valores) busca el
			 * valor más pequeño en la colección de valores que le proporcionas como
			 * argumento y te devuelve ese valor.
			 */
			double minimo = Collections.min(valores);
			sigmaDisMin.add(minimo);
		}

		// Para rellenar la lista de clusters se recorren las filas del registro y se
		// asignan las insatncias correspondientes a cada medoide
		for (int w = 0; w < registroNormalizado.size(); w++) {

			// Se obtiene el la posici�n del valor minimo de las disatncias a cada centro en
			// la instancia actual
			double minValue = distanciaACentros.get(w).get(0);
			int minIndex = 0;
			for (int j = 1; j < distanciaACentros.get(w).size(); j++) {
				if (distanciaACentros.get(w).get(j) < minValue) {
					minValue = distanciaACentros.get(w).get(j);
					minIndex = j;
				}
			}

			// Se a�ade la instancia y distancia a su medoide correspondiente
			kmeans.clusters.get(minIndex).add(registroNormalizado.get(w));
			kmeans.distClusters.get(minIndex).add(minValue);

		}

		// Finalizando este punto ya see tiene una lista de clusters correpondiente a la
		// cantidad de medoides actual

		// Se remueven valores null de las inicializaciones previa
		kmeans.distClusters.get(0).remove(0);
		for (List<Double> interno : kmeans.distClusters) {
			interno.remove(0);
		}
		for (List<List<Double>> interno : kmeans.clusters) {
			interno.remove(0);
		}

		// Aqui obtenemos la suma de los valores minimos
		double suma = 0;
		for (double valor : sigmaDisMin) {
			suma += valor;
		}

		System.out.println("Distancia minima acumulada: " + suma + "\n");
		System.out.println("-----------------------------------------");

		int contadorCluster = 0;
		try {
			File file = new File(kmeans.rutaCarpetaDestino + "/clusters_k" + centros.size() + ".txt");
			FileWriter writer = new FileWriter(file);
			writer.write("  Clusters:\n");
			writer.write("********************************************\n");
			contadorCluster = 0;
			for (List<List<Double>> cluster : kmeans.clusters) {
				contadorCluster++;
				writer.write("Cluster: " + contadorCluster + "\n");
				for (List<Double> fila : cluster) {
					for (Double valor : fila) {
						writer.write(" /" + valor + "/ ");
					}
					writer.write("\n");
				}
				writer.write("\n");
			}
			writer.write("********************************************\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return suma;
	}

	public List<List<Double>> nuevosCentros(int cantCentros, List<List<Double>> registroNormalizado,
			List<List<Double>> centros) {
		// A esta lista se le sumarán los valores de las variables de las instancias a
		// sus respectivos centros
		List<List<Double>> sumProdCentros = new ArrayList<>();

		// Esta lista contará la cantidad de insatncias que pertenecen a un centro
		List<Integer> cantInstancias = new ArrayList<>();

		// Se inicializa la lista para luego trabajar cpn posiciones especificas de ella
		for (int i = 0; i < cantCentros; i++) {
			cantInstancias.add(0);
			List<Double> filasumProd = new ArrayList<>();
			for (int j = 0; j < registroNormalizado.get(0).size(); j++) {
				filasumProd.add(0.0);
			}
			sumProdCentros.add(filasumProd);
		}

		// Se recorren las filas de las listas de las distancias minimas, las distancias
		// generales y los valores normalizados del registro
		// Indices:
		// La i indica el nivel de la instancia, La j indica el centro, La y indica la
		// variable
		for (int i = 0; i < sigmaDisMin.size(); i++) {
			// Se recorre las distancias de cada centro en la fila i
			for (int j = 0; j < cantCentros; j++) {

				// Debido a que el comparador == no tiene mucha presición al comparar valores
				// double se tomara en consideración la diferencia de una resta entre los
				// valores y si la diferencia es 0 se consideraran iguales
				double a = distancias.get(j).get(i);
				double b = sigmaDisMin.get(i);
				double epsilon = 0.00000000000001;

				// Se utiliza el la función de Math.abs para obtener el valor absoluto y tener
				// la diferencia
				/*
				 * El método Math.abs() es un método estático de la clase Math en Java que
				 * devuelve el valor absoluto de un número. El valor absoluto de un número es
				 * su distancia desde cero en la recta numérica, sin tener en cuenta su signo.
				 * Por ejemplo, el valor absoluto de -5 es 5 y el valor absoluto de 5 es 5.
				 * 
				 * El método Math.abs() acepta un argumento de tipo int, long, float o double y
				 * devuelve un valor del mismo tipo. Internamente, el método Math.abs() calcula
				 * el valor absoluto de un número utilizando operaciones aritméticas básicas.
				 * Por ejemplo, para calcular el valor absoluto de un número x, se puede
				 * utilizar la siguiente fórmula: x >= 0 ? x : -x.
				 */

				if (Math.abs(a - b) < epsilon) {

					int aumento = cantInstancias.get(j);
					cantInstancias.set(j, aumento + 1);

					// Se itera dentro de las variables de la instancia i
					for (int y = 0; y < registroNormalizado.get(0).size(); y++) {
						double valor = sumProdCentros.get(j).get(y);
						sumProdCentros.get(j).set(y, valor + registroNormalizado.get(i).get(y));
					}
				}
			}
		}

		// Una vez se tiene la suma de los valores de las instancias que pertenecen a
		// cada centro, se les suma el valor del centro al que pertenecen
		// Una vez se tiene cada suma se divide entre la cantidad de instancias
		// pertenecientes a ese centro mas 1
		for (int i = 0; i < cantCentros; i++) {
			for (int j = 0; j < registroNormalizado.get(0).size(); j++) {
				double valor = sumProdCentros.get(i).get(j);
				int instanciasPlus = cantInstancias.get(i) + 1;

				// se usa el cast (double) para asegurarse que el valor resultante sea tipo
				// double debido a que la división se hace sobre un entero
				sumProdCentros.get(i).set(j, (double) (valor + centros.get(i).get(j)) / instanciasPlus);
			}
		}

		System.out.println("Centros: " + cantCentros);
		for (List<Double> fila : sumProdCentros) {
			for (Double valor : fila) {
				System.out.print(" /" + valor + "/ ");
			}
			System.out.println("\n");
		}

		return sumProdCentros;

	}

	public List<List<Double>> centrosIniciales(int cantCentros, List<List<Double>> registrosNormalizados) {
		// Crear una lista vacía para almacenar los centros resultantes
		List<List<Double>> centros = new ArrayList<>();

		// Las listas utilizadas usan diferentes nombres y se generan disstintas en cada
		// case para mejor manejo y que no se entorpezca la ejecuci�n dentro del switch
		switch (cantCentros) {
		case 2:
			List<Double> valores0 = new ArrayList<>();
			List<Double> valores1 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores0.add(0.0);
				valores0.add(0.0);

				valores1.add(1.0);
				valores1.add(1.0);

				centros.add(valores0);
				centros.add(valores1);
				break;
			case 3:
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);

				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);

				centros.add(valores0);
				centros.add(valores1);
				break;
			case 4:
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);

				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);

				centros.add(valores0);
				centros.add(valores1);
				break;
			case 5:
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);
				valores0.add(0.0);

				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);
				valores1.add(1.0);

				centros.add(valores0);
				centros.add(valores1);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 3:
			List<Double> valores2 = new ArrayList<>();
			List<Double> valores3 = new ArrayList<>();
			List<Double> valores4 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores2.add(0.0);
				valores2.add(0.0);

				valores3.add(0.5);
				valores3.add(0.5);

				valores4.add(1.0);
				valores4.add(1.0);

				centros.add(valores2);
				centros.add(valores3);
				centros.add(valores4);
				break;
			case 3:
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);

				valores3.add(0.5);
				valores3.add(0.5);
				valores3.add(0.5);

				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);

				centros.add(valores2);
				centros.add(valores3);
				centros.add(valores4);
				break;
			case 4:
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);

				valores3.add(1.0);
				valores3.add(0.0);
				valores3.add(0.0);
				valores3.add(0.0);

				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);

				centros.add(valores2);
				centros.add(valores3);
				centros.add(valores4);
				break;
			case 5:
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);
				valores2.add(0.0);

				valores3.add(1.0);
				valores3.add(0.0);
				valores3.add(0.0);
				valores3.add(0.0);
				valores3.add(0.0);

				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);
				valores4.add(1.0);

				centros.add(valores2);
				centros.add(valores3);
				centros.add(valores4);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 4:
			List<Double> valores5 = new ArrayList<>();
			List<Double> valores6 = new ArrayList<>();
			List<Double> valores7 = new ArrayList<>();
			List<Double> valores8 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores5.add(0.0);
				valores5.add(0.0);

				valores6.add(1.0);
				valores6.add(1.0);

				valores7.add(0.0);
				valores7.add(1.0);

				valores8.add(1.0);
				valores8.add(0.0);

				centros.add(valores5);
				centros.add(valores6);
				centros.add(valores7);
				centros.add(valores8);
				break;
			case 3:
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);

				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);

				valores7.add(1.0);
				valores7.add(1.0);
				valores7.add(0.0);
				valores7.add(0.0);

				valores8.add(0.0);
				valores8.add(0.0);
				valores8.add(1.0);
				valores8.add(1.0);

				centros.add(valores5);
				centros.add(valores6);
				centros.add(valores7);
				centros.add(valores8);
				break;
			case 4:
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);

				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);

				valores7.add(0.0);
				valores7.add(0.0);
				valores7.add(0.0);
				valores7.add(1.0);

				valores8.add(1.0);
				valores8.add(0.0);
				valores8.add(0.0);
				valores8.add(0.0);

				centros.add(valores5);
				centros.add(valores6);
				centros.add(valores7);
				centros.add(valores8);
				break;
			case 5:
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);
				valores5.add(0.0);

				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);
				valores6.add(1.0);

				valores7.add(1.0);
				valores7.add(1.0);
				valores7.add(1.0);
				valores7.add(1.0);
				valores7.add(0.0);

				valores8.add(0.0);
				valores8.add(0.0);
				valores8.add(0.0);
				valores8.add(0.0);
				valores8.add(1.0);

				centros.add(valores5);
				centros.add(valores6);
				centros.add(valores7);
				centros.add(valores8);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 5:
			List<Double> valores9 = new ArrayList<>();
			List<Double> valores10 = new ArrayList<>();
			List<Double> valores11 = new ArrayList<>();
			List<Double> valores12 = new ArrayList<>();
			List<Double> valores13 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores9.add(0.0);
				valores9.add(0.0);

				valores10.add(1.0);
				valores10.add(1.0);

				valores11.add(1.0);
				valores11.add(0.0);

				valores12.add(0.0);
				valores12.add(1.0);

				valores13.add(0.5);
				valores13.add(0.5);

				centros.add(valores9);
				centros.add(valores10);
				centros.add(valores11);
				centros.add(valores12);
				centros.add(valores13);
				break;
			case 3:
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);

				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);

				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(0.0);

				valores12.add(0.0);
				valores12.add(0.0);
				valores12.add(1.0);

				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);

				centros.add(valores9);
				centros.add(valores10);
				centros.add(valores11);
				centros.add(valores12);
				centros.add(valores13);
				break;
			case 4:
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);

				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);

				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(0.0);

				valores12.add(0.0);
				valores12.add(1.0);
				valores12.add(1.0);
				valores12.add(1.0);

				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);

				centros.add(valores9);
				centros.add(valores10);
				centros.add(valores11);
				centros.add(valores12);
				centros.add(valores13);
				break;
			case 5:
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);
				valores9.add(0.0);

				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);
				valores10.add(1.0);

				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(1.0);
				valores11.add(0.0);

				valores12.add(0.0);
				valores12.add(0.0);
				valores12.add(0.0);
				valores12.add(0.0);
				valores12.add(1.0);

				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);
				valores13.add(0.5);

				centros.add(valores9);
				centros.add(valores10);
				centros.add(valores11);
				centros.add(valores12);
				centros.add(valores13);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 6:
			List<Double> valores14 = new ArrayList<>();
			List<Double> valores15 = new ArrayList<>();
			List<Double> valores16 = new ArrayList<>();
			List<Double> valores17 = new ArrayList<>();
			List<Double> valores18 = new ArrayList<>();
			List<Double> valores19 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores14.add(0.0);
				valores14.add(0.0);

				valores15.add(1.0);
				valores15.add(1.0);

				valores16.add(1.0);
				valores16.add(0.0);

				valores17.add(0.0);
				valores17.add(1.0);

				valores18.add(0.33);
				valores18.add(0.33);

				valores19.add(0.66);
				valores19.add(0.66);

				centros.add(valores14);
				centros.add(valores15);
				centros.add(valores16);
				centros.add(valores17);
				centros.add(valores18);
				centros.add(valores19);
				break;
			case 3:
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);

				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);

				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(0.0);

				valores16.add(0.0);
				valores16.add(0.0);
				valores16.add(1.0);

				valores17.add(1.0);
				valores17.add(0.0);
				valores17.add(0.0);

				valores18.add(0.0);
				valores18.add(1.0);
				valores18.add(1.0);

				valores19.add(0.0);
				valores19.add(1.0);
				valores19.add(0.0);

				centros.add(valores14);
				centros.add(valores15);
				centros.add(valores16);
				centros.add(valores17);
				centros.add(valores18);
				centros.add(valores19);
				break;
			case 4:
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);

				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);

				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(0.0);

				valores17.add(0.0);
				valores17.add(0.0);
				valores17.add(0.0);
				valores17.add(1.0);

				valores18.add(1.0);
				valores18.add(1.0);
				valores18.add(0.0);
				valores18.add(0.0);

				valores19.add(0.0);
				valores19.add(0.0);
				valores19.add(1.0);
				valores19.add(1.0);

				centros.add(valores14);
				centros.add(valores15);
				centros.add(valores16);
				centros.add(valores17);
				centros.add(valores18);
				centros.add(valores19);
				break;
			case 5:
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);
				valores14.add(0.0);

				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);
				valores15.add(1.0);

				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(1.0);
				valores16.add(0.0);

				valores17.add(0.0);
				valores17.add(0.0);
				valores17.add(0.0);
				valores17.add(0.0);
				valores17.add(1.0);

				valores18.add(0.0);
				valores18.add(0.0);
				valores18.add(0.0);
				valores18.add(1.0);
				valores18.add(1.0);

				valores19.add(1.0);
				valores19.add(1.0);
				valores19.add(1.0);
				valores19.add(0.0);
				valores19.add(0.0);

				centros.add(valores14);
				centros.add(valores15);
				centros.add(valores16);
				centros.add(valores17);
				centros.add(valores18);
				centros.add(valores19);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 7:
			List<Double> valores20 = new ArrayList<>();
			List<Double> valores21 = new ArrayList<>();
			List<Double> valores22 = new ArrayList<>();
			List<Double> valores23 = new ArrayList<>();
			List<Double> valores24 = new ArrayList<>();
			List<Double> valores25 = new ArrayList<>();
			List<Double> valores26 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores20.add(0.0);
				valores20.add(0.0);

				valores21.add(1.0);
				valores21.add(1.0);

				valores22.add(1.0);
				valores22.add(0.0);

				valores23.add(0.0);
				valores23.add(1.0);

				valores24.add(0.33);
				valores24.add(0.33);

				valores25.add(0.66);
				valores25.add(0.66);

				valores26.add(0.5);
				valores26.add(0.5);

				centros.add(valores20);
				centros.add(valores21);
				centros.add(valores22);
				centros.add(valores23);
				centros.add(valores24);
				centros.add(valores25);
				centros.add(valores26);
				break;
			case 3:
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);

				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);

				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(0.0);

				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(1.0);

				valores24.add(1.0);
				valores24.add(0.0);
				valores24.add(0.0);

				valores25.add(0.0);
				valores25.add(1.0);
				valores25.add(1.0);

				valores26.add(0.5);
				valores26.add(0.5);
				valores26.add(0.5);

				centros.add(valores20);
				centros.add(valores21);
				centros.add(valores22);
				centros.add(valores23);
				centros.add(valores24);
				centros.add(valores25);
				centros.add(valores26);
				break;
			case 4:
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);

				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);

				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(0.0);

				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(1.0);

				valores24.add(0.0);
				valores24.add(0.0);
				valores24.add(1.0);
				valores24.add(1.0);

				valores25.add(1.0);
				valores25.add(1.0);
				valores25.add(0.0);
				valores25.add(0.0);

				valores26.add(1.0);
				valores26.add(0.0);
				valores26.add(0.0);
				valores26.add(0.0);

				centros.add(valores20);
				centros.add(valores21);
				centros.add(valores22);
				centros.add(valores23);
				centros.add(valores24);
				centros.add(valores25);
				centros.add(valores26);
				break;
			case 5:
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);
				valores20.add(0.0);

				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);
				valores21.add(1.0);

				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(1.0);
				valores22.add(0.0);

				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(0.0);
				valores23.add(1.0);

				valores24.add(1.0);
				valores24.add(1.0);
				valores24.add(1.0);
				valores24.add(0.0);
				valores24.add(0.0);

				valores25.add(0.0);
				valores25.add(0.0);
				valores25.add(0.0);
				valores25.add(1.0);
				valores25.add(1.0);

				valores26.add(1.0);
				valores26.add(0.0);
				valores26.add(0.0);
				valores26.add(0.0);
				valores26.add(0.0);

				centros.add(valores20);
				centros.add(valores21);
				centros.add(valores22);
				centros.add(valores23);
				centros.add(valores24);
				centros.add(valores25);
				centros.add(valores26);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 8:
			List<Double> valores27 = new ArrayList<>();
			List<Double> valores28 = new ArrayList<>();
			List<Double> valores29 = new ArrayList<>();
			List<Double> valores30 = new ArrayList<>();
			List<Double> valores31 = new ArrayList<>();
			List<Double> valores32 = new ArrayList<>();
			List<Double> valores33 = new ArrayList<>();
			List<Double> valores34 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores27.add(0.0);
				valores27.add(0.0);

				valores28.add(1.0);
				valores28.add(1.0);

				valores29.add(1.0);
				valores29.add(0.0);

				valores30.add(0.0);
				valores30.add(1.0);

				valores31.add(0.33);
				valores31.add(0.33);

				valores32.add(0.66);
				valores32.add(0.66);

				valores33.add(0.33);
				valores33.add(0.66);

				valores34.add(0.66);
				valores34.add(0.33);

				centros.add(valores27);
				centros.add(valores28);
				centros.add(valores29);
				centros.add(valores30);
				centros.add(valores31);
				centros.add(valores32);
				centros.add(valores33);
				centros.add(valores34);
				break;
			case 3:
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);

				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);

				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(0.0);

				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(1.0);

				valores31.add(1.0);
				valores31.add(0.0);
				valores31.add(0.0);

				valores32.add(0.0);
				valores32.add(1.0);
				valores32.add(1.0);

				valores33.add(1.0);
				valores33.add(0.0);
				valores33.add(1.0);

				valores34.add(0.0);
				valores34.add(1.0);
				valores34.add(0.0);

				centros.add(valores27);
				centros.add(valores28);
				centros.add(valores29);
				centros.add(valores30);
				centros.add(valores31);
				centros.add(valores32);
				centros.add(valores33);
				centros.add(valores34);
				break;
			case 4:
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);

				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);

				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(0.0);

				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(1.0);

				valores31.add(1.0);
				valores31.add(0.0);
				valores31.add(0.0);
				valores31.add(0.0);

				valores32.add(0.0);
				valores32.add(1.0);
				valores32.add(1.0);
				valores32.add(1.0);

				valores33.add(1.0);
				valores33.add(1.0);
				valores33.add(0.0);
				valores33.add(0.0);

				valores34.add(0.0);
				valores34.add(0.0);
				valores34.add(1.0);
				valores34.add(1.0);

				centros.add(valores27);
				centros.add(valores28);
				centros.add(valores29);
				centros.add(valores30);
				centros.add(valores31);
				centros.add(valores32);
				centros.add(valores33);
				centros.add(valores34);
				break;
			case 5:
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);
				valores27.add(0.0);

				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);
				valores28.add(1.0);

				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(1.0);
				valores29.add(0.0);

				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(0.0);
				valores30.add(1.0);

				valores31.add(1.0);
				valores31.add(0.0);
				valores31.add(0.0);
				valores31.add(0.0);
				valores31.add(0.0);

				valores32.add(0.0);
				valores32.add(1.0);
				valores32.add(1.0);
				valores32.add(1.0);
				valores32.add(1.0);

				valores33.add(1.0);
				valores33.add(1.0);
				valores33.add(1.0);
				valores33.add(0.0);
				valores33.add(0.0);

				valores34.add(0.0);
				valores34.add(0.0);
				valores34.add(0.0);
				valores34.add(1.0);
				valores34.add(1.0);

				centros.add(valores27);
				centros.add(valores28);
				centros.add(valores29);
				centros.add(valores30);
				centros.add(valores31);
				centros.add(valores32);
				centros.add(valores33);
				centros.add(valores34);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 9:
			List<Double> valores35 = new ArrayList<>();
			List<Double> valores36 = new ArrayList<>();
			List<Double> valores37 = new ArrayList<>();
			List<Double> valores38 = new ArrayList<>();
			List<Double> valores39 = new ArrayList<>();
			List<Double> valores40 = new ArrayList<>();
			List<Double> valores41 = new ArrayList<>();
			List<Double> valores42 = new ArrayList<>();
			List<Double> valores43 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores35.add(0.0);
				valores35.add(0.0);

				valores36.add(1.0);
				valores36.add(1.0);

				valores37.add(1.0);
				valores37.add(0.0);

				valores38.add(0.0);
				valores38.add(1.0);

				valores39.add(0.33);
				valores39.add(0.33);

				valores40.add(0.66);
				valores40.add(0.66);

				valores41.add(0.33);
				valores41.add(0.66);

				valores42.add(0.66);
				valores42.add(0.33);

				valores43.add(0.5);
				valores43.add(0.5);

				centros.add(valores35);
				centros.add(valores36);
				centros.add(valores37);
				centros.add(valores38);
				centros.add(valores39);
				centros.add(valores40);
				centros.add(valores41);
				centros.add(valores42);
				centros.add(valores43);
				break;
			case 3:
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);

				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);

				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(0.0);

				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(1.0);

				valores39.add(1.0);
				valores39.add(0.0);
				valores39.add(0.0);

				valores40.add(0.0);
				valores40.add(1.0);
				valores40.add(1.0);

				valores41.add(1.0);
				valores41.add(0.0);
				valores41.add(1.0);

				valores42.add(0.0);
				valores42.add(1.0);
				valores42.add(0.0);

				valores43.add(0.5);
				valores43.add(0.5);
				valores43.add(0.5);

				centros.add(valores35);
				centros.add(valores36);
				centros.add(valores37);
				centros.add(valores38);
				centros.add(valores39);
				centros.add(valores40);
				centros.add(valores41);
				centros.add(valores42);
				centros.add(valores43);
				break;
			case 4:
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);

				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);

				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(0.0);

				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(1.0);

				valores39.add(1.0);
				valores39.add(0.0);
				valores39.add(0.0);
				valores39.add(0.0);

				valores40.add(0.0);
				valores40.add(1.0);
				valores40.add(1.0);
				valores40.add(1.0);

				valores41.add(1.0);
				valores41.add(1.0);
				valores41.add(0.0);
				valores41.add(0.0);

				valores42.add(0.0);
				valores42.add(0.0);
				valores42.add(1.0);
				valores42.add(1.0);

				valores43.add(1.0);
				valores43.add(0.0);
				valores43.add(0.0);
				valores43.add(1.0);

				centros.add(valores35);
				centros.add(valores36);
				centros.add(valores37);
				centros.add(valores38);
				centros.add(valores39);
				centros.add(valores40);
				centros.add(valores41);
				centros.add(valores42);
				centros.add(valores43);
				break;
			case 5:
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);
				valores35.add(0.0);

				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);
				valores36.add(1.0);

				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(1.0);
				valores37.add(0.0);

				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(0.0);
				valores38.add(1.0);

				valores39.add(1.0);
				valores39.add(0.0);
				valores39.add(0.0);
				valores39.add(0.0);
				valores39.add(0.0);

				valores40.add(0.0);
				valores40.add(1.0);
				valores40.add(1.0);
				valores40.add(1.0);
				valores40.add(1.0);

				valores41.add(1.0);
				valores41.add(1.0);
				valores41.add(1.0);
				valores41.add(0.0);
				valores41.add(0.0);

				valores42.add(0.0);
				valores42.add(0.0);
				valores42.add(0.0);
				valores42.add(1.0);
				valores42.add(1.0);

				valores43.add(0.0);
				valores43.add(0.0);
				valores43.add(1.0);
				valores43.add(1.0);
				valores43.add(1.0);

				centros.add(valores35);
				centros.add(valores36);
				centros.add(valores37);
				centros.add(valores38);
				centros.add(valores39);
				centros.add(valores40);
				centros.add(valores41);
				centros.add(valores42);
				centros.add(valores43);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		case 10:
			List<Double> valores44 = new ArrayList<>();
			List<Double> valores45 = new ArrayList<>();
			List<Double> valores46 = new ArrayList<>();
			List<Double> valores47 = new ArrayList<>();
			List<Double> valores48 = new ArrayList<>();
			List<Double> valores49 = new ArrayList<>();
			List<Double> valores50 = new ArrayList<>();
			List<Double> valores51 = new ArrayList<>();
			List<Double> valores52 = new ArrayList<>();
			List<Double> valores53 = new ArrayList<>();
			switch (registrosNormalizados.get(0).size()) {
			case 2:
				valores44.add(0.0);
				valores44.add(0.0);

				valores45.add(1.0);
				valores45.add(1.0);

				valores46.add(1.0);
				valores46.add(0.0);

				valores47.add(0.0);
				valores47.add(1.0);

				valores48.add(0.2);
				valores48.add(0.2);

				valores49.add(0.8);
				valores49.add(0.8);

				valores50.add(0.2);
				valores50.add(0.8);

				valores51.add(0.8);
				valores51.add(0.2);

				valores52.add(0.6);
				valores52.add(0.6);

				valores53.add(0.4);
				valores53.add(0.4);

				centros.add(valores44);
				centros.add(valores45);
				centros.add(valores46);
				centros.add(valores47);
				centros.add(valores48);
				centros.add(valores49);
				centros.add(valores50);
				centros.add(valores51);
				centros.add(valores52);
				centros.add(valores53);
				break;
			case 3:
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);

				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);

				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(0.0);

				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(1.0);

				valores48.add(1.0);
				valores48.add(0.0);
				valores48.add(0.0);

				valores49.add(0.0);
				valores49.add(1.0);
				valores49.add(1.0);

				valores50.add(1.0);
				valores50.add(0.0);
				valores50.add(1.0);

				valores51.add(0.0);
				valores51.add(1.0);
				valores51.add(0.0);

				valores52.add(0.33);
				valores52.add(0.33);
				valores52.add(0.33);

				valores53.add(0.66);
				valores53.add(0.66);
				valores53.add(0.66);

				centros.add(valores44);
				centros.add(valores45);
				centros.add(valores46);
				centros.add(valores47);
				centros.add(valores48);
				centros.add(valores49);
				centros.add(valores50);
				centros.add(valores51);
				centros.add(valores52);
				centros.add(valores53);
				break;
			case 4:
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);

				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);

				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(0.0);

				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(1.0);

				valores48.add(1.0);
				valores48.add(0.0);
				valores48.add(0.0);
				valores48.add(0.0);

				valores49.add(0.0);
				valores49.add(1.0);
				valores49.add(1.0);
				valores49.add(1.0);

				valores50.add(1.0);
				valores50.add(1.0);
				valores50.add(0.0);
				valores50.add(0.0);

				valores51.add(0.0);
				valores51.add(0.0);
				valores51.add(1.0);
				valores51.add(1.0);

				valores52.add(1.0);
				valores52.add(0.0);
				valores52.add(0.0);
				valores52.add(1.0);

				valores52.add(0.0);
				valores52.add(1.0);
				valores52.add(1.0);
				valores52.add(0.0);

				centros.add(valores44);
				centros.add(valores45);
				centros.add(valores46);
				centros.add(valores47);
				centros.add(valores48);
				centros.add(valores49);
				centros.add(valores50);
				centros.add(valores51);
				centros.add(valores52);
				centros.add(valores53);
				break;
			case 5:
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);
				valores44.add(0.0);

				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);
				valores45.add(1.0);

				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(1.0);
				valores46.add(0.0);

				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(0.0);
				valores47.add(1.0);

				valores48.add(1.0);
				valores48.add(0.0);
				valores48.add(0.0);
				valores48.add(0.0);
				valores48.add(0.0);

				valores49.add(0.0);
				valores49.add(1.0);
				valores49.add(1.0);
				valores49.add(1.0);
				valores49.add(1.0);

				valores50.add(1.0);
				valores50.add(1.0);
				valores50.add(1.0);
				valores50.add(0.0);
				valores50.add(0.0);

				valores51.add(0.0);
				valores51.add(0.0);
				valores51.add(0.0);
				valores51.add(1.0);
				valores51.add(1.0);

				valores52.add(0.0);
				valores52.add(0.0);
				valores52.add(1.0);
				valores52.add(1.0);
				valores52.add(1.0);

				valores53.add(1.0);
				valores53.add(1.0);
				valores53.add(0.0);
				valores53.add(0.0);
				valores53.add(0.0);

				centros.add(valores44);
				centros.add(valores45);
				centros.add(valores46);
				centros.add(valores47);
				centros.add(valores48);
				centros.add(valores49);
				centros.add(valores50);
				centros.add(valores51);
				centros.add(valores52);
				centros.add(valores53);
				break;
			default:
				System.out.println("Cantidad de variables de registro no valida.");
				System.exit(0);
				break;
			}
			break;
		default:
			System.out.println("Cantidad de centros exedida.");
			break;
		}

		System.out.println("Centros:" + cantCentros);
		for (List<Double> fila : centros) {
			for (Double valor : fila) {
				System.out.print(" /" + valor + "/ ");
			}
			System.out.println("\n");
		}

		return centros;
	}

}
