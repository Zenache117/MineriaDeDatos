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

	}

}
