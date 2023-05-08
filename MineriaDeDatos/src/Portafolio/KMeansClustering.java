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
	// se contiene representa las distancias de todas las instancias hacia un centro
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

		// Generamos esta instancia para desplazarnos a travez de los metodos de la
		// clase
		KMeansClustering kmeans = new KMeansClustering();

		List<List<Double>> registroNormalizado = new ArrayList<>();
		registroNormalizado = kmeans.Normalizar(registro);

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

		while (mejoraCentros == true) {
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

				// Esto ayuda a decirle al ciclo que no es la primera iteración con ese calor de
				// centros
				i++;
			}
			
			if (mejorValor == -1 || distanciaTotal < mejorValor) {
				mejorValor = distanciaTotal;
				mejorCantCentros = cantCentros;
				mejoraLocal = true;
			} else {
				System.out.println("La cantidad de centros encontrada fue de: " + mejorCantCentros
						+ " con una distancia de: " + mejorValor);
				mejoraCentros = false;
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

	public double distanciaCentros(List<List<Double>> centros, List<List<Double>> registroNormalizado,
			KMeansClustering kmeans) {

		// Se limpian las listas para asegurarse de poder trabajar con ellas
		kmeans.distancias.clear();
		kmeans.sigmaDisMin.clear();

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

		System.out.println("Distancia minima acumulada: " + suma + "\n");
		System.out.println("-----------------------------------------");

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
				 * devuelve el valor absoluto de un número. El valor absoluto de un número es su
				 * distancia desde cero en la recta numérica, sin tener en cuenta su signo. Por
				 * ejemplo, el valor absoluto de -5 es 5 y el valor absoluto de 5 es 5.
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

		// Obtener el número de variables en el registro (El numero de variables es
		// igual en cualquier pisición de la lista registroNormalizado por eso no
		// importa de donde tome el size())
		int numVariables = registrosNormalizados.get(0).size();

		// Iterar sobre cada variable del registro
		for (int i = 0; i < numVariables; i++) {
			// Inicializar las variables min y max para encontrar el valor mínimo y máximo
			// para esta variable
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;

			// Iterar sobre cada registro en registrosNormalizados
			for (List<Double> registro : registrosNormalizados) {
				// Obtener el valor de la variable actual para este registro
				double valor = registro.get(i);

				// Actualizar las variables min y max si es necesario
				if (valor < min) {
					min = valor;
				}
				if (valor > max) {
					max = valor;
				}
			}

			// Calcular el intervalo entre los centros
			double intervalo = (max - min) / (cantCentros - 1);

			// Agregar cada centro a la lista centros
			for (int j = 0; j < cantCentros; j++) {
				if (centros.size() <= j) {
					centros.add(new ArrayList<>());
				}
				centros.get(j).add(min + intervalo * j);
			}
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
