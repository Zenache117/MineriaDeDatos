package Portafolio;

/*
Este codigo es una implementación de un algoritmo para generar arboles de decisión a partir de un archivo CSV.
A continuaciÃ³n, se presenta una descripciÃ³n de las partes principales del cÃ³digo:

El paquete Decision contiene una clase llamada Arbol, que tiene un mÃ©todo main que se encarga de leer un archivo CSV, 
generar todas las permutaciones de las columnas del archivo, crear un Ã¡rbol para cada permutaciÃ³n y escribir el Ã¡rbol 
resultante en un archivo de texto. La clase Arbol tambiÃ©n define un mÃ©todo auxiliar llamado printTreeToFile que se utiliza para 
imprimir el Ã¡rbol en un archivo de texto.

La clase Node representa un nodo en un Ã¡rbol. Cada nodo tiene un valor (data), un padre (parent) y una lista de hijos (children).

En el mÃ©todo main, el archivo CSV se lee utilizando la biblioteca commons-io de Apache. La primera lÃ­nea del archivo se trata como encabezados 
y se almacena en una lista llamada headers. Se eliminan los encabezados de la lista lines, que contiene el resto de las lÃ­neas del archivo CSV.

Se genera una lista de todas las permutaciones posibles de los encabezados utilizando la clase PermutationIterator de la biblioteca 
commons-collections4 de Apache. Cada permutaciÃ³n se almacena en una lista de listas llamada perms.

Para cada permutaciÃ³n en perms, se crea un nuevo Ã¡rbol con la permutaciÃ³n como su nombre de raÃ­z. Luego, para cada lÃ­nea en lines, 
se recorre la permutaciÃ³n de columnas y se crean nodos con los valores posibles.

Finalmente, se imprime el Ã¡rbol resultante en un archivo de texto utilizando el mÃ©todo printTreeToFile. Se crea un archivo de texto para 
cada permutaciÃ³n en la ruta C:\\Users\\mauri\\OneDrive\\Documentos\\Arboles\\permutacion_ seguida del nombre de la permutaciÃ³n.

Este programa en Java genera un Ã¡rbol a partir de un archivo CSV de datos y produce un archivo de texto que contiene la representaciÃ³n del 
Ã¡rbol para cada permutaciÃ³n de las columnas del CSV.

El programa comienza leyendo un archivo CSV especificado en la ruta 
C:\Users\mauri\OneDrive\Documentos\Archivos Excel\Titanic_5Categoricas.csv utilizando la biblioteca Apache Commons IO. 
El archivo debe tener una fila de encabezado que se utiliza para identificar las columnas y sus nombres. 
La primera fila despuÃ©s del encabezado contiene los primeros valores para cada columna. Las siguientes filas contienen 
el resto de los valores para cada columna.

El programa utiliza la clase PermutationIterator de la biblioteca Apache Commons Collections para generar todas las 
permutaciones de las columnas del CSV. Para cada permutaciÃ³n, el programa crea un Ã¡rbol en memoria utilizando la clase 
Node definida en el cÃ³digo. Cada nodo del Ã¡rbol representa un valor Ãºnico para una columna en el CSV. El nodo raÃ­z del Ã¡rbol 
se nombra utilizando la permutaciÃ³n actual de las columnas. El programa recorre cada fila del archivo CSV y agrega cada valor en la 
fila al Ã¡rbol correspondiente en la posiciÃ³n de la columna.

DespuÃ©s de crear el Ã¡rbol para una permutaciÃ³n, el programa escribe la representaciÃ³n del Ã¡rbol en un archivo de texto en la ruta 
C:\Users\mauri\OneDrive\Documentos\Arboles\permutacion_[nombres de columnas].txt, donde [nombres de columnas] es la permutaciÃ³n actual 
de las columnas. La representaciÃ³n del Ã¡rbol se escribe utilizando la funciÃ³n printTreeToFile que recorre el Ã¡rbol recursivamente e 
imprime cada nodo y sus hijos en formato de texto. El archivo de texto contiene una lÃ­nea con el nombre de la permutaciÃ³n 
y la representaciÃ³n del Ã¡rbol debajo de ella.

La clase Node utilizada en el programa representa un nodo en el Ã¡rbol. Cada nodo tiene un valor de datos, un nodo padre 
y una lista de nodos hijos. El valor de datos es el valor de una columna en una fila del archivo CSV. 
El nodo padre es el nodo que es el padre del nodo actual en el Ã¡rbol. La lista de nodos hijos es una lista de nodos que son hijos 
del nodo actual en el Ã¡rbol. La clase tiene un constructor que toma el valor de datos y el nodo padre, asÃ­ como un constructor que 
toma solo el valor de datos. TambiÃ©n tiene mÃ©todos para obtener el valor de datos, el nodo padre y la lista de nodos hijos, 
asÃ­ como un mÃ©todo para agregar un nodo hijo a la lista de nodos hijos.
 * */
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.io.FileUtils;

public class MejorArbolDeDesicion {

	public static void main(String[] args) throws IOException {

		// Leer CSV
		// Crear un nuevo objeto FileDialog
		FileDialog fileChooser = new FileDialog((Frame) null, "Seleccionar archivo CSV", FileDialog.LOAD);

		// Mostrar el diálogo para que el usuario seleccione un archivo
		fileChooser.setVisible(true);

		// Crear un nuevo objeto FileDialog
		CarpetaDestino carpetaDestino = new CarpetaDestino();

		String rutaCarpetaDestino = carpetaDestino.selectCarpet();

		// Verificar si el usuario seleccionó un archivo
		if (fileChooser.getFile() != null) {
			// Obtener el archivo seleccionado
			File file = new File(fileChooser.getDirectory() + fileChooser.getFile());

			// Esta lista definirá las instacias a como vienen en el registro del csv
			List<String> lines = FileUtils.readLines(file, "UTF-8");

			// Asigna la primera linea del registro a los encabezados
			List<String> headers = Arrays.asList(lines.get(0).split(","));

			// Removemos los encabezados para que no formen parte de los valores del
			// registro
			lines.remove(0);

			// Generar todas las permutaciones de las columnas
			PermutationIterator<String> permIter = new PermutationIterator<String>(headers);
			List<List<String>> perms = new ArrayList<List<String>>();
			permIter.forEachRemaining(perm -> perms.add(perm));

			// Esta variable definirá el valor de entropia minimo encontrado en los arboles
			float minEnt = -1;
			// Esta lista representará al arbol de minima entropia encontrado
			List<String> arbolMinEnt = null;

			// Para cada permutación
			for (List<String> perm : perms) {
				// Crear raÃ­z del Ã¡rbol con el nombre de la permutaciÃ³n
				Node<String> root = new Node<String>(perm.toString());

				// Para cada fila del CSV
				for (String line : lines) {
					String[] values = line.split(",");
					// Recorrer la permutación de columnas y crear nodos con los valores posibles
					Node<String> currentNode = root;
					for (String col : perm) {
						String value = values[headers.indexOf(col)];
						List<Node<String>> children = IteratorUtils.toList(currentNode.getChildren().iterator());
						if (children.stream().noneMatch(node -> node.getData().equals(value))) {
							currentNode = new Node<String>(value, currentNode);
						} else {
							currentNode = children.stream().filter(node -> node.getData().equals(value)).findFirst()
									.get();
						}
					}
				}

				

				// Formamos una lista de las instancias en el orden de la permutación
				List<String> sortedLines = new ArrayList<>();
				for (String line : lines) {// Se recorre la lista del registro original
					List<String> values = new ArrayList<>(Arrays.asList(line.split(",")));
					List<String> sortedValues = new ArrayList<>();
					for (String col : perm) {// Se recorre las columnas segun la permutación
						sortedValues.add(values.get(headers.indexOf(col)));
					}
					String sortedLine = String.join(",", sortedValues);
					sortedLines.add(sortedLine);
				}

				float ent = calculateEntrophy(sortedLines);

				// si la entropia inicial es -1 entonces la primera entopia calculada es igual a
				// la menor entropia obtenida
				if (minEnt == -1) {
					minEnt = ent;
					arbolMinEnt = perm;
				} else {// Aqui entra cuando ya se calculo la primera entopia minima
					// Solo se actualiza la entopia cuando se obtiene una menor (Solo se toma en
					// cuenta un arbol de minima entropia, si hay otro con una entropia igual a la
					// menor, no se toma en cuenta)
					if (ent < minEnt) {
						minEnt = ent;
						arbolMinEnt = perm;
					}
				}
				
				
				// Imprimir árbol de la permutación actual en un archivo de texto
				File outputFile = new File(rutaCarpetaDestino + "permutacion_" + perm + ".txt");
				FileWriter writer = new FileWriter(outputFile);
				writer.write("Permutación: " + perm + " Entropia del arbol: " + ent + "\n");
				printTreeToFile(root, "", true, writer);
				
				
				
				 writer.close();
			}
			// Imprimir en consola el mejor resultado
			System.out.println("Mejor Permutación: " + arbolMinEnt + "Minima entropia: " + minEnt);

			// Imprimir el mejor arbol y su entropia
			Node<String> root = new Node<String>(arbolMinEnt.toString());
			for (String line : lines) {
				String[] values = line.split(",");
				Node<String> currentNode = root;
				for (String col : arbolMinEnt) {
					String value = values[headers.indexOf(col)];
					List<Node<String>> children = IteratorUtils.toList(currentNode.getChildren().iterator());
					if (children.stream().noneMatch(node -> node.getData().equals(value))) {
						currentNode = new Node<String>(value, currentNode);
					} else {
						currentNode = children.stream().filter(node -> node.getData().equals(value)).findFirst().get();
					}
				}
			}

			// Imprimir el arbol de desición de mejor entropia
			File outputFile = new File(rutaCarpetaDestino + "Mejor permutacion_" + arbolMinEnt + ".txt");
			FileWriter writer = new FileWriter(outputFile);
			writer.write("Permutación: " + arbolMinEnt + " Entropia del arbol: " + minEnt + "\n");
			printTreeToFile(root, "", true, writer);
			writer.close();
		}
		fileChooser.dispose();
	}

	// Este metodo calcula la entropia de cada arbol y la devueve como un valor tipo
	// float
	private static float calculateEntrophy(List<String> registro) {

		// Se llama al metodo para generar las tablas en función de la cantidad de
		// columnas del registro
		List<List<String>> tablas = splitRegistro(registro);

		// Esta variable esta encargada de aculumar la entropía de las tablas
		float totalEntropy = 0;

		// Se recorren todas las tablas y se calculan las entropías
		for (List<String> tabla : tablas) {

			// Se generan listas de las variantes que ya se han revisado
			List<String> revisado = new ArrayList<>();
			List<String> revisado2 = new ArrayList<>();

			// Se recorre cada fila de cada tabla (instancias)
			for (String fila : tabla) {

				int numInstances = 0; // Contador de instancias a las que pertenece la
				// instancia a revisar
				int instances = 0; // Contador de instancias a revisar

				int cant = 0;
				// Se buscan coincidencias en la tabla

				// Se itera dentro de la misma tabla para buscar elementos iguales a la
				// instancia a revisar
				for (String fila2 : tabla) {
					if (!revisado.contains(fila)) {
						if (fila.equals(fila2)) {
							cant++;
						}
					}
				}
				if (!revisado.contains(fila)) {
					revisado.add(fila);
				}
				instances = cant;

				// Se genera un string con la pos anterior por ejemeplo si tenemos (4,male,6 se
				// tendra un string con 4,6 para contar los elementos que tienen esa estructura
				// omitiendo el ultimo elemento del string completo)
				int posAnt = fila.lastIndexOf(',');
				String tipoPert;
				if (posAnt >= 0) {
					tipoPert = fila.substring(0, posAnt);
				} else {
					tipoPert = fila;
				}

				cant = 0;
				Conteo: for (String fila2 : tabla) {
					// Se asignan los valores de la fila a un array de strings para acceder a los
					// valores
					String[] valores = fila2.split(",");
					// Si la tabla que se revisa solo tiene una columna, la cantidad de instancias
					// para la proporcion debe se la cantidad de insatancias totales
					if (valores.length == 1) {
						cant = tabla.size();
						break Conteo;
					} else {
						if (!revisado2.contains(tipoPert)) {
							// Si la tabla tiene mas de una columna, las instancias deberían dividirse
							// entre
							// la cantidad de insatncias a las que pertenecen.
							// Obtener el tipo de instancia (último valor de la fila con respecto al valor
							// que se revisa)
							int ultimaComa = fila2.lastIndexOf(',');
							String instancia = fila2.substring(0, ultimaComa);// omite el ultimo caracter

							if (tipoPert.equals(instancia)) {
								cant++;
							}

						}
					}
				}
				if (posAnt >= 0) {
					if (revisado2.contains(tipoPert)) {
						revisado2.add(tipoPert);
					}
				}

				numInstances = cant;

				if (numInstances > 0 && instances > 0) {

					float entropy = 0;
					float p;
					p = (float) instances / (float) numInstances;
					entropy -= p * Math.log10(p); // La entropia de cada tipo de instancia se calcula aqui
					totalEntropy += entropy; // Aqui se acumula la entropía de todas las subtablas de la permutación

				}

			}
		}

		// Devolver la entropía total
		return totalEntropy;
	}

	// Este metodo ayuda a separar el registo enviado a calculateEntrohpy en las
	// tablas para calcular la entropía
	private static List<List<String>> splitRegistro(List<String> registro) {
		List<List<String>> tablas = new ArrayList<>();
		int numCols = registro.get(0).split(",").length;
		for (int i = 0; i < numCols; i++) {
			List<String> tabla = new ArrayList<>();
			for (String linea : registro) {
				String[] valores = linea.split(",", numCols);
				String fila = valores[i];
				if (i > 0) {
					fila = String.join(",", Arrays.copyOfRange(valores, 0, i)) + "," + fila;
				}
				tabla.add(fila);
			}
			tablas.add(tabla);
		}
		return tablas;
	}

	// Aqui se imprime el arbol que le sea enviado
	private static void printTreeToFile(Node<String> node, String prefix, boolean isTail, FileWriter writer)
			throws IOException {
		writer.write(prefix + (isTail ? "└── " : "├── ") + node.getData() + "\n");
		List<Node<String>> children = IteratorUtils.toList(node.getChildren().iterator());
		for (int i = 0; i < children.size() - 1; i++) {
			printTreeToFile(children.get(i), prefix + (isTail ? "    " : "│   "), false, writer);
		}
		if (children.size() > 0) {
			printTreeToFile(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true, writer);
		}
	}
}

class Node<T> {

	private T data;
	private Node<T> parent;
	private List<Node<T>> children;

	public Node(T data, Node<T> parent) {
		this.data = data;
		this.parent = parent;
		this.children = new ArrayList<Node<T>>();
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public Node(T data) {
		this(data, null);
	}

	public T getData() {
		return data;
	}

	public Node<T> getParent() {
		return parent;
	}

	public List<Node<T>> getChildren() {
		return children;
	}

	public void addChild(Node<T> child) {
		this.children.add(child);
	}
}