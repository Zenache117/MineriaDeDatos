/*El prograba debe imprimir los valores de Solar.R, Wind, y Temp, máximos y mínimos de cada columna. 
 Convertir cada valor numérico de las columnas a Categórico
 */
package Portafolio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class reglas {

	public static void main(String[] args) {
		String csvFile = "MineriaDeDatos/src/Portafolio/Reglas.csv";
		String line;
		String csvSplitBy = ",";
		String columnSeparator = "\t";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			// Arreglos para almacenar los valores de cada columna
			double[] maxValues = null;
			double[] minValues = null;
			boolean isFirstRow = true;
			String header = "";
			int totalInstances = 0;

			// Imprime la tabla original
			while ((line = br.readLine()) != null) {
				// Dividir la línea en columnas utilizando la coma como separador
				String[] columns = line.split(csvSplitBy);

				if (isFirstRow) {
					isFirstRow = false;
					header = line; // Almacenar la fila de encabezado
					continue; // Omitir la primera fila
				}

				// Verificar si es la primera línea para inicializar los arreglos de máximos y
				// mínimos
				if (maxValues == null) {
					maxValues = new double[columns.length];
					minValues = new double[columns.length];
					// Inicializar los arreglos con el primer valor de cada columna
					for (int i = 0; i < columns.length; i++) {
						maxValues[i] = Double.parseDouble(columns[i]);
						minValues[i] = Double.parseDouble(columns[i]);
					}
					// Imprimir los encabezados
					System.out.println(header);
				} else {
					// Actualizar los valores máximos y mínimos de cada columna
					for (int i = 0; i < columns.length; i++) {
						double value = Double.parseDouble(columns[i]);
						if (value > maxValues[i]) {
							maxValues[i] = value;
						}
						if (value < minValues[i]) {
							minValues[i] = value;
						}
					}
				}

				// Construir una cadena con las columnas separadas
				StringBuilder row = new StringBuilder();
				for (int i = 0; i < columns.length; i++) {
					row.append(columns[i]);
					if (i < columns.length - 1) {
						row.append(columnSeparator);
					}
				}

				// Imprimir la fila completa
				System.out.println(row.toString());

				// Incrementar el contador de instancias
				totalInstances++;
			}

			// Número de Instancias
			System.out.println("Número de instancias: " + (totalInstances));

			// Imprimir los valores máximos y mínimos de cada columna junto con la
			// información de la columna
			System.out.println("Valores máximos:");
			for (int i = 0; i < maxValues.length; i++) {
				System.out.println("Columna " + (i + 1) + ": " + maxValues[i]);
			}

			System.out.println("Valores mínimos:");
			for (int i = 0; i < minValues.length; i++) {
				System.out.println("Columna " + (i + 1) + ": " + minValues[i]);
			}
			System.out.println("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------------------------------");
		System.out.println("Convertidos a Categóricos: ");
		// Imprimir los valores convertidos a categóricos
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			// Arreglos para almacenar los valores de cada columna
			double[] maxValues = null;
			double[] minValues = null;
			boolean isFirstRow = true;
			String header = "";

			// Imprime la tabla original
			while ((line = br.readLine()) != null) {
				// Dividir la línea en columnas utilizando la coma como separador
				String[] columns = line.split(csvSplitBy);

				if (isFirstRow) {
					isFirstRow = false;
					header = line; // Almacenar la fila de encabezado
					continue; // Omitir la primera fila
				}

				// Asignar el valor "A" o "B" a la columna Solar.R
				if (Double.parseDouble(columns[0]) >= 150) {
					columns[0] = "A";
				} else {
					columns[0] = "B";
				}
				// Asignar el valor "B", "M" o "A" a la columna Wind
				if (Double.parseDouble(columns[1]) <= 5) {
					columns[1] = "B";
				} else if (Double.parseDouble(columns[1]) <= 12) {
					columns[1] = "M";
				} else {
					columns[1] = "A";
				}
				// Asignar el valor "F" o "C" a la columna Temp
				if (Double.parseDouble(columns[2]) <= 63) {
					columns[2] = "F";
				} else {
					columns[2] = "C";
				}
				// Construir una cadena con las columnas separadas
				StringBuilder row = new StringBuilder();
				for (int i = 0; i < columns.length; i++) {
					row.append(columns[i]);
					if (i < columns.length - 1) {
						row.append(columnSeparator);
					}
				}

				// Imprimir la tabla completa con valores categóricos
				System.out.println(row.toString());

			}

			System.out.println("------------------------------------------");
			System.out.println("Condiciones:\t\t Nx \t Ny \t Nx^y \t Support   Confidence   Lift");
			// Imprimir la primera ronda de condiciones (buscar como permutaciones)
			// Columna 1: Solar.R
			List<String> column1Values = new ArrayList<>();
			column1Values.add("A");
			column1Values.add("B");

			// Columna 2: Wind
			List<String> column2Values = new ArrayList<>();
			column2Values.add("B");
			column2Values.add("M");
			column2Values.add("A");

			// Columna 3: Temp
			List<String> column3Values = new ArrayList<>();
			column3Values.add("F");
			column3Values.add("C");

			// Generar todas las combinaciones posibles
			for (String value1 : column1Values) {
				for (String value2 : column2Values) {
					for (String value3 : column3Values) {

						//Valores de Nx y Ny
						int nx = calculateNx(value1, value2, value3);
						int ny = calculateNy(value1, value2, value3);

						// Determinar el valor que coincide entre los dos valores de entrada
						String matchingValue = getMatchingValue(value1, value2, value3);

						// Calcular los valores de support, confidence y lift
						double support = calculateSupport();
						double confidence = calculateConfidence();
						double lift = calculateLift();
						// Construir las combinaciones en el formato correcto
						String combination1 = "SI(SR=" + value1 + " Entonces T=" + value3 + ")";
						String combination2 = "SI(T=" + value3 + " Entonces SR=" + value1 + ")";
						String combination3 = "SI(W=" + value2 + " Entonces T=" + value3 + ")";
						//String combination4 = "SI(T=" + value3 + " Entonces W=" + value2 + ")";

						// Imprimir las combinaciones actuales
						System.out.println(combination1 + "\t" +nx+ "\t" +ny+ "" + matchingValue + "\t"  + support + "\t" + confidence + "\t" + lift + "\t");
						System.out.println(combination2 + "\t" +nx+ "\t" +ny+ "" + matchingValue + "\t"  + support + "\t" + confidence + "\t" + lift + "\t");
						System.out.println(combination3 + "\t" +nx+ "\t" +ny+ "" + matchingValue + "\t"  + support + "\t" + confidence + "\t" + lift + "\t");
						//System.out.println(combination4 + "\t" +nx+ "\t" +ny+ "" + matchingValue + "\t"  + support + "\t" + confidence + "\t" + lift + "\t");
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Método para determinar el valor de Nx
	private static int calculateNx(String column1Values, String column2Values, String column3Values){
		return 0;
	}

	//Método para determinar el valor de Ny
	private static int calculateNy(String column1Values, String column2Values, String column3Values){
		return 0;
	}

	// Método para determinar el valor que coincide entre los dos valores de entrada
	private static String getMatchingValue(String value1, String value2, String value3) {
		// Implementa la lógica para determinar el valor que coincide
		// entre value1 y value2 según tus requerimientos
		return ""; // Devuelve el valor que coincide
	}

	// Métodos para calcular el support, confidence y lift
	private static double calculateSupport() {
		// Implementa la lógica para calcular el support según tus requerimientos
		return 0.0; // Devuelve el valor de support calculado
	}

	private static double calculateConfidence() {
		// Implementa la lógica para calcular el confidence según tus requerimientos
		return 0.0; // Devuelve el valor de confidence calculado
	}

	private static double calculateLift() {
		// Implementa la lógica para calcular el lift según tus requerimientos
		return 0.0; // Devuelve el valor de lift calculado
	}
}