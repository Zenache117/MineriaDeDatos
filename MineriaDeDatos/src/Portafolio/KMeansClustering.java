package Portafolio;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KMeansClustering {
	// Esta será la lista que contendrá todas las distancias minnimas
	List<Double> sigmaDisMin = new ArrayList<>();

	// La lista de distancias tiene que ser una lista de listas porque la lista que
	// se contiene represnta las distancias de todas las instancias hacia un centro
	// y la lissta general contiene la de todos los centros
	List<List<Double>> distancias = new ArrayList<>();

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

		KMeansClustering kmeans = new KMeansClustering();
		List<List<Double>> registroNormalizado = new ArrayList<>();
		registroNormalizado = kmeans.Normalizar(registro);
		int cantCentros = 2;
		boolean mejoraCentros = true;
		boolean mejoraLocal = true;
		List<List<Double>> centros = new ArrayList<>();
		centros = kmeans.primerosCentros(registroNormalizado);
		double distanciaTotal = kmeans.distanciaCentros(centros, registroNormalizado);
		while (mejoraCentros == true) {
			while (mejoraLocal == true) {

			}
			cantCentros++;
		}
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

		// Inicializa los valores mínimos y máximos con el primer valor de cada columna
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

	public List<List<Double>> primerosCentros(List<List<Double>> registroNormalizado) {

		// Se asignan los primeros valores como centros y luego se comparan
		List<List<Double>> centros = new ArrayList<>();
		List<Double> primerosValores = new ArrayList<>();
		for (Double valFila : registroNormalizado.get(0)) {
			primerosValores.add(valFila);
		}

		List<Double> segundosValores = new ArrayList<>(primerosValores);

		centros.add(primerosValores);
		centros.add(segundosValores);

		for (List<Double> fila : registroNormalizado) {
			int i = 0;
			for (Double valor : fila) {
				if (valor < centros.get(0).get(i)) {
					centros.get(0).set(i, valor);
				}
				if (valor > centros.get(1).get(i)) {
					centros.get(1).set(i, valor);
				}
				i++;
			}

		}
		return centros;
	}

	public double distanciaCentros(List<List<Double>> centros, List<List<Double>> registroNormalizado) {
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
		// Aqui obtenemos la suma de los valores minimos
		double suma = 0;
		for (double valor : sigmaDisMin) {
		    suma += valor;
		}
		return suma;
	}
}
