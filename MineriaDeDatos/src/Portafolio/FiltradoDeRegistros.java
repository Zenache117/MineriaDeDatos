package Portafolio;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class FiltradoDeRegistros {

	public static void main(String[] args) {

		List<Instancias> listaInstancias = new ArrayList<>();
		List<Instancias> listaAtendidos = new ArrayList<>();
		List<Instancias> listaNoAtendidos = new ArrayList<>();

		try {
			FileReader reader = new FileReader(
					new File("C:/Users/mauri/OneDrive/Documentos/Archivos Excel/ExampleLog.csv"));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
			for (CSVRecord csvRecord : csvParser) {
				Instancias instancia = new Instancias();
				instancia.setCaseID(csvRecord.get("Case ID"));
				instancia.setActivity(csvRecord.get("Activity"));
				instancia.setStartDate(csvRecord.get("Start Date"));
				instancia.setEndDate(csvRecord.get("End Date"));
				instancia.setAgentPosition(csvRecord.get("Agent Position"));
				instancia.setCustID(csvRecord.get("Customer ID"));
				instancia.setProduct(csvRecord.get("Product"));
				instancia.setServiceType(csvRecord.get("Service Type"));
				instancia.setResource(csvRecord.get("Resource"));

				listaInstancias.add(instancia);
			}
			csvParser.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		String currentCaseID = listaInstancias.get(0).getCaseID();
		boolean Cambio = false;
		int CantCases = 0;

		List<Instancias> listaProcesados = new ArrayList<>();
		for (int i = 0; i < listaInstancias.size(); i++) {
			if (CantCases == 0) {
				listaProcesados.add(listaInstancias.get(i));
				CantCases++;
				Cambio = false;
			} else {
				if (Cambio == false) {
					if (currentCaseID.equals(listaInstancias.get(i).getCaseID())) {
						if (!listaInstancias.get(i).getActivity().equals(listaInstancias.get(i - 1).getActivity())) {
							Cambio = true;
							listaProcesados.add(listaInstancias.get(i));
							CantCases++;
						} else {
							CantCases++;
							listaProcesados.add(listaInstancias.get(i));
							Cambio = false;
						}
					} else {
						if (Cambio == true) {
							for (int j = 0; j < listaProcesados.size(); j++) {
								listaAtendidos.add(listaProcesados.get(j));
							}
							Cambio = false;
						} else {
							for (int j = 0; j < listaProcesados.size(); j++) {
								listaNoAtendidos.add(listaProcesados.get(j));
							}
						}
						CantCases = 0;
						currentCaseID = listaInstancias.get(i).getCaseID();
						listaProcesados.clear();
						listaProcesados.add(listaInstancias.get(i));
						CantCases++;
					}
				} else {
					if (currentCaseID.equals(listaInstancias.get(i).getCaseID())) {
						CantCases++;
						listaProcesados.add(listaInstancias.get(i));
					} else {
						if (Cambio == true) {
							for (int j = 0; j < listaProcesados.size(); j++) {
								listaAtendidos.add(listaProcesados.get(j));
							}
							Cambio = false;
						} else {
							for (int j = 0; j < listaProcesados.size(); j++) {
								listaNoAtendidos.add(listaProcesados.get(j));
							}
						}
						CantCases = 0;
						currentCaseID = listaInstancias.get(i).getCaseID();
						listaProcesados.clear();
						listaProcesados.add(listaInstancias.get(i));
						CantCases++;
					}
				}

			}

		}

		writeCSV("C:/Users/mauri/OneDrive/Documentos/Archivos Excel/atendidos.csv", listaAtendidos);
		writeCSV("C:/Users/mauri/OneDrive/Documentos/Archivos Excel/noAtendidos.csv", listaNoAtendidos);
	}

	public static void writeCSV(String filename, List<Instancias> instances) {
		try (FileWriter writer = new FileWriter(filename);
				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("Case ID", "Activity", "Start Date", "End Date", "Agent Position",
								"Customer ID", "Product", "Service Type", "Resource"));) {

			for (Instancias instance : instances) {
				csvPrinter.printRecord(instance.getCaseID(), instance.getActivity(), instance.getStartDate(),
						instance.getEndDate(), instance.getAgentPosition(), instance.getCustID(), instance.getProduct(),
						instance.getServiceType(), instance.getResource());
			}

			csvPrinter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
