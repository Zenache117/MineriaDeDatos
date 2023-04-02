package Portafolio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class TratamientoSanFrancisco {

	public static void main(String[] args) throws IOException {
		String filePath = "C:\\Users\\mauri\\OneDrive\\Documentos\\Archivos Excel\\SanFranciscoHomicides1849-2003-10-2010 (1).csv";
		FileReader reader = new FileReader(filePath);

		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
		CSVParser csvParser = new CSVParser(reader, csvFormat);

		List<SanFrancisco> sanFranciscoList = new ArrayList<>();

		for (CSVRecord record : csvParser) {
			SanFrancisco sanFrancisco = new SanFrancisco();
			sanFrancisco.setMonth(record.get("month"));
			sanFrancisco.setDay(record.get("day"));
			sanFrancisco.setYear(record.get("year"));
			sanFrancisco.setCity(record.get("city"));
			sanFrancisco.setLocation(record.get("location"));
			sanFrancisco.setVictim(record.get("victim"));
			sanFrancisco.setSuspect(record.get("suspect"));
			sanFrancisco.setCircumst(record.get("circumst"));
			sanFrancisco.setComment(record.get("comment"));
			sanFrancisco.setSrace(record.get("srace"));
			sanFrancisco.setSethnici(record.get("sethnici"));
			sanFrancisco.setSgender(record.get("sgender"));
			sanFrancisco.setSage(record.get("sage"));
			sanFrancisco.setVrace(record.get("vrace"));
			sanFrancisco.setVethnici(record.get("vethnici"));
			sanFrancisco.setVgender(record.get("vgender"));
			sanFrancisco.setVage(record.get("vage"));
			sanFrancisco.setWeapon(record.get("weapon"));
			sanFrancisco.setSource1(record.get("source1"));
			sanFrancisco.setSource2(record.get("source2"));

			sanFranciscoList.add(sanFrancisco);
		}

		csvParser.close();

		List<SanFrancisco> crimenesDeOdio = new ArrayList<>();
		for (SanFrancisco s : sanFranciscoList) {
			if (!s.getSrace().equals(s.getVrace()) || !s.getSgender().equals(s.getVgender())
					|| !s.getSethnici().equals(s.getVethnici())) {
				crimenesDeOdio.add(s);
			}
		}

		String[] header = { "month", "day", "year", "city", "location", "victim", "suspect", "circumst", "comment",
				"srace", "sethnici", "sgender", "sage", "vrace", "vethnici", "vgender", "vage", "weapon", "source1",
		"source2" };

		FileWriter out = new FileWriter("C:\\Users\\mauri\\OneDrive\\Documentos\\Archivos Excel\\crimenesDeOdio.csv");
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(header))) {
			for (SanFrancisco s : crimenesDeOdio) {
				printer.printRecord(s.getMonth(), s.getDay(), s.getYear(), s.getCity(), s.getLocation(), s.getVictim(),
						s.getSuspect(), s.getCircumst(), s.getComment(), s.getSrace(), s.getSethnici(), s.getSgender(),
						s.getSage(), s.getVrace(), s.getVethnici(), s.getVgender(), s.getVage(), s.getWeapon(),
						s.getSource1(), s.getSource2());
			}
		}
	}
}
