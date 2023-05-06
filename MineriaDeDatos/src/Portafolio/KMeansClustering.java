package Portafolio;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMeansClustering {

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

		// Se añade 2 veces para que una lista sea los valores minimos y al otra los
		// valores maximos y estos a su vez sean los primeros centros
		centros.add(primerosValores);
		centros.add(primerosValores);

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

}
