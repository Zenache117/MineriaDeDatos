package PIA;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.formdev.flatlaf.ui.FlatMenuItemBorder;

import Portafolio.CarpetaDestino;

public class KMedoides {
	// Esta será la lista que contendrá todas las distancias minnimas
	List<Double> sigmaDisMin = new ArrayList<>();

	// La lista de distancias tiene que ser una lista de listas porque la lista que
	// se contiene representa las distancias de todas las instancias hacia un centro
	// y la lissta general contiene la de todos los centros
	List<List<Double>> distancias = new ArrayList<>();

	// Una lista para guardar los valores de pureza de cada instancia
	List<Double> pureza = new ArrayList<>();

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
		KMedoides kmeans = new KMedoides();
		List<List<Double>> registroNormalizado = new ArrayList<>();

		// Se elige si se normaliza o no el registro esto originalmente con la intenci�n
		// de comparar con el dataset utilizado en el documento
		int opcion = JOptionPane.showOptionDialog(null, "Elije si normalizar o no el dataset", "Normalizacion?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "Si normalizar", "No normalizar" }, "Opci�n 1");

		if (opcion == JOptionPane.YES_OPTION) {
			registroNormalizado = kmeans.Normalizar(registro);
		} else if (opcion == JOptionPane.NO_OPTION) {
			for (CSVRecord record : registro) {
				List<Double> fila = new ArrayList<>();
				for (String valor : record) {
					fila.add(Double.parseDouble(valor));
				}
				registroNormalizado.add(fila);
			}
		}

		// Seleccionar la carpeta destino para guardar el txt
		CarpetaDestino carpetaDestino = new CarpetaDestino();
		kmeans.rutaCarpetaDestino = carpetaDestino.selectCarpet();

		kmeans.Pureza(registroNormalizado, kmeans);

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

		int cantLimite = 2;

		// Listas para mostar al final la canntidad de iteraciones que se tuvo en cada
		// nivel de k
		List<Integer> cantIteraciones = new ArrayList<>();

		int numIteracionesGeneral = 0;

		MejoraLocal: while (mejoraCentros == true && cantCentros < 10) {
			numIteracionesGeneral++;
			int i = 0;

			int numIteracionesLocal = 0;
			while (mejoraLocal == true) {
				numIteracionesLocal++;
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

			System.out.println("\n Numero de iteraciones local: " + numIteracionesLocal + "\n");
			cantIteraciones.add(numIteracionesLocal);

			if (mejorValor == -1 || distanciaTotal < mejorValor) {
				mejorValor = distanciaTotal;
				mejorCantCentros = cantCentros;
				mejoraLocal = true;
			} else {

				mejoraCentros = false;
			}

			cantCentros++;
			cantLimite++;

			// En caso que nunca empeore la distancia minima acumulada, se detiene antes de
			// que cada instancia sea un medoide
			if (cantLimite == registroNormalizado.size()) {
				break MejoraLocal;
			}

		}

		System.out.println("La cantidad de medoides encontrada fue de: " + mejorCantCentros + " con una distancia de: "
				+ mejorValor + "\nNumero de iteraciones general: " + numIteracionesGeneral + "\n");

		int k = 2;
		int sumaIteraciones = 0;
		for (Integer valor : cantIteraciones) {
			sumaIteraciones += valor;
			System.out.println("k: " + k + " Cantidad de iteraciones: " + valor);
			k++;
		}
		double mediaIteraciones = sumaIteraciones / cantIteraciones.size();
		System.out.println("Cantidad total de iteraciones: " + sumaIteraciones + "\nPromedio de iteraciones por k: "
				+ mediaIteraciones);
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

	// Calulo de disstancias a centros
	public double distanciaCentros(List<List<Double>> centros, List<List<Double>> registroNormalizado,
			KMedoides kmeans) {

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

		// Se remueven valores null de las inicializaciones previa
		kmeans.distClusters.get(0).remove(0);

		for (List<List<Double>> interno : kmeans.clusters) {
			interno.remove(0);
		}

		// Finalizando este punto ya see tiene una lista de clusters correpondiente a la
		// cantidad de medoides actual

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
			writer.write("Clusters\n");
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

	// Calculo de centroides mediante promedio
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

		KMedoides obtener = new KMedoides();
		List<List<Double>> medoides = new ArrayList<>();

		// Se obtiene el medoide real
		for (List<Double> center : sumProdCentros) {
			List<Double> med = new ArrayList<>();
			med = obtener.ajustar(center, registroNormalizado);
			medoides.add(med);
		}

		System.out.println("Medoide: " + cantCentros);
		for (List<Double> fila : medoides) {
			for (Double valor : fila) {
				System.out.print(" /" + valor + "/ ");
			}
			System.out.println("\n");
		}

		return medoides;

	}

	// Centros iniciales definidos por el documento, si el valor de k es par se
	// toman los valores menores de pureza y si es impar se toman los de mayor
	// pureza para la primer iterracion de cada k
	public List<List<Double>> centrosIniciales(int cantCentros, List<List<Double>> registrosNormalizados) {

		if (registrosNormalizados.size() < 100) {
			System.out.println("Cantidad de intancias no valida.");
			System.exit(0);
		}
		if (registrosNormalizados.get(0).size() < 2) {
			System.out.println("Cantidad de variables no valida.");
			System.exit(0);
		}

		// Crear una lista vac�a para almacenar los centros resultantes
		List<List<Double>> centros = new ArrayList<>();

		/*
		 * Crea una lista de �ndices y la ordena en funci�n de los valores en la lista
		 * pureza. Luego, utiliza la lista de �ndices ordenada para crear nuevas listas
		 * que contengan los elementos de las listas originales en el orden deseado.
		 */

		// Crea una lista de �ndices
		List<Integer> indices = IntStream.range(0, pureza.size()).boxed().collect(Collectors.toList());

		// Ordena la lista de �ndices en funci�n de los valores en la lista pureza
		indices.sort((i1, i2) -> pureza.get(i2).compareTo(pureza.get(i1)));

		// Crea nuevas listas ordenadas
		List<Double> purezaOrdenada = new ArrayList<>();
		List<List<Double>> registroNormalizadoOrdenado = new ArrayList<>();
		for (int i : indices) {
			purezaOrdenada.add(pureza.get(i));
			registroNormalizadoOrdenado.add(registrosNormalizados.get(i));
		}

		// Imprime las listas ordenadas
		System.out.println("Instancias ordenadas por pureza de forma descendente:");
		System.out.println(purezaOrdenada);
		System.out.println(registroNormalizadoOrdenado + "\n");

		float residuo = cantCentros % 2;

		// Si el residuo de la division de k entre 2 es 0 es un valor par en caso
		// contrario impar
		if (residuo == 0) {
			switch (cantCentros) {
			case 2:
				int n1 = cantCentros; // N�mero de elementos que deseas obtener
				int inicio1 = Math.max(0, registroNormalizadoOrdenado.size() - n1);
				centros = registroNormalizadoOrdenado.subList(inicio1, registroNormalizadoOrdenado.size());
				break;
			case 4:
				int n2 = cantCentros; // N�mero de elementos que deseas obtener
				int inicio2 = Math.max(0, registroNormalizadoOrdenado.size() - n2);
				centros = registroNormalizadoOrdenado.subList(inicio2, registroNormalizadoOrdenado.size());
				break;
			case 6:
				int n3 = cantCentros; // N�mero de elementos que deseas obtener
				int inicio3 = Math.max(0, registroNormalizadoOrdenado.size() - n3);
				centros = registroNormalizadoOrdenado.subList(inicio3, registroNormalizadoOrdenado.size());
				break;
			case 8:
				int n4 = cantCentros; // N�mero de elementos que deseas obtener
				int inicio4 = Math.max(0, registroNormalizadoOrdenado.size() - n4);
				centros = registroNormalizadoOrdenado.subList(inicio4, registroNormalizadoOrdenado.size());
				break;
			case 10:
				int n5 = cantCentros; // N�mero de elementos que deseas obtener
				int inicio5 = Math.max(0, registroNormalizadoOrdenado.size() - n5);
				centros = registroNormalizadoOrdenado.subList(inicio5, registroNormalizadoOrdenado.size());
				break;
			default:
				System.out.println("Cantidad de Medoides no valida.");
				System.exit(0);
				break;
			}
		} else {
			switch (cantCentros) {
			case 3:
				int n1 = cantCentros; // N�mero de elementos que deseas obtener
				int fin1 = Math.min(n1, registroNormalizadoOrdenado.size());
				centros = registroNormalizadoOrdenado.subList(0, fin1);
				break;
			case 5:
				int n2 = cantCentros; // N�mero de elementos que deseas obtener
				int fin2 = Math.min(n2, registroNormalizadoOrdenado.size());
				centros = registroNormalizadoOrdenado.subList(0, fin2);
				break;
			case 7:
				int n3 = cantCentros; // N�mero de elementos que deseas obtener
				int fin3 = Math.min(n3, registroNormalizadoOrdenado.size());
				centros = registroNormalizadoOrdenado.subList(0, fin3);
				break;
			case 9:
				int n4 = cantCentros; // N�mero de elementos que deseas obtener
				int fin4 = Math.min(n4, registroNormalizadoOrdenado.size());
				centros = registroNormalizadoOrdenado.subList(0, fin4);
				break;
			default:
				System.out.println("Cantidad de Medoides no valida.");
				System.exit(0);
				break;
			}
		}

		System.out.println("Medoide: " + cantCentros);
		for (List<Double> fila : centros) {
			for (Double valor : fila) {
				System.out.print(" /" + valor + "/ ");
			}
			System.out.println("\n");
		}

		return centros;
	}

	// Metodo para ajustar el promedio a una instancia existente del registro
	public List<Double> ajustar(List<Double> centro, List<List<Double>> registroNormalizado) {
		List<Double> instanciaMasCercana = null;
		double distanciaMinima = Double.MAX_VALUE;

		// Se calcula la disstancia euclidiana del centro obtenido a todas lass
		// instancias del registro y luego se obtiene la insstancia con menor
		// alejamiento
		for (List<Double> instancia : registroNormalizado) {
			double distancia = 0.0;
			for (int i = 0; i < instancia.size(); i++) {
				distancia += Math.pow(instancia.get(i) - centro.get(i), 2);
			}
			distancia = distancia * (1.0 / centro.size());
			if (distancia < distanciaMinima) {
				distanciaMinima = distancia;
				instanciaMasCercana = instancia;
			}
		}
		return instanciaMasCercana;
	}

	// Generar la lista de valores de pureza para cada instancia del datasset
	// utilizado
	public void Pureza(List<List<Double>> registroNormalizado, KMedoides kmeans) {

		for (List<Double> instancia : registroNormalizado) {
			double valorPureza;
			double maxValor = 0;
			double sumaFila = 0;

			for (Double valor : instancia) {
				sumaFila += valor;
				if (valor > maxValor) {
					maxValor = valor;
				}
			}

			valorPureza = (1 / sumaFila) * maxValor;

			kmeans.pureza.add(valorPureza);
		}

		System.out.println("");

	}

}