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
		//Seleccionar CSV
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
	}
	
	public List<List<Double>> Normalizar(List<CSVRecord> registro) {
		// Calcula el número de columnas en el registro CSV
		int numColumnas = registro.get(0).size();

		// Crea arreglos para almacenar los valores mínimos, máximos, m y b para cada columna
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
		    bValues[i] = (-1)*minValues[i] * mValues[i];
		}

		// Crea una lista para almacenar el registro CSV normalizado
		List<List<Double>> registroNormalizado = new ArrayList<>();

		// Normaliza los datos en cada columna y almacena los valores normalizados en la lista
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

}