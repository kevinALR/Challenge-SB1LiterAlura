package challenge.sb_literalura;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

@SpringBootApplication
public class SbLiteraluraApplication {

	private final GutendexClient gutendexClient;

	public SbLiteraluraApplication(GutendexClient gutendexClient) {
		this.gutendexClient = gutendexClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(SbLiteraluraApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			displayMenu();
		};
	}

	private void displayMenu() {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("\n--- Menú ---");
			System.out.println("1. Buscar libro por título");
			System.out.println("2. Listar todos los libros buscados");
			System.out.println("3. Listar todos los autores de los libros buscados");
			System.out.println("4. Listar autores vivos en un determinado año");
			System.out.println("5. Listar libros por idioma");
			System.out.println("0. Salir");
			System.out.print("Selecciona una opción: ");

			int option = scanner.nextInt();
			scanner.nextLine(); // Consumir el salto de línea después de nextInt()

			switch (option) {
				case 1:
					searchBookByTitle(scanner);
					break;
				case 2:
					listSearchedBooks();
					break;
				case 3:
					listSearchedAuthors();
					break;
				case 4:
					listLivingAuthorsByYear(scanner);
					break;
				case 5:
					listBooksByLanguage(scanner);
					break;
				case 0:
					System.out.println("Saliendo del programa...");
					return;
				default:
					System.out.println("Opción no válida. Por favor, selecciona una opción válida.");
			}
		}
	}

	private void searchBookByTitle(Scanner scanner) {
		System.out.print("Introduce el título del libro a buscar: ");
		String title = scanner.nextLine();

		try {
			Book book = gutendexClient.searchBookByTitle(title);
			System.out.println("\nLibro encontrado:");
			System.out.println("Título: " + book.getTitle());
			System.out.println("Autor: " + book.getAuthor());
			System.out.println("Idioma: " + book.getLanguage());
			System.out.println("Número de Descargas: " + book.getDownloadCount());
		} catch (Exception e) {
			System.err.println("Error al buscar el libro: " + e.getMessage());
		}
	}

	private void listSearchedBooks() {
		System.out.println("\nListado de todos los libros buscados:");
		List<Book> books = gutendexClient.getSearchedBooks();
		if (books.isEmpty()) {
			System.out.println("No hay libros buscados aún.");
		} else {
			for (Book book : books) {
				System.out.println("\nTítulo: " + book.getTitle());
				System.out.println("Autor: " + book.getAuthor().getName());
				System.out.println("Idioma: " + book.getLanguage());
				System.out.println("Número de Descargas: " + book.getDownloadCount());
			}
		}
	}


	private void listSearchedAuthors() {
		List<Author> authors = gutendexClient.getAllAuthors();
		if (!authors.isEmpty()) {
			System.out.println("Lista de autores de los libros buscados:");
			for (Author author : authors) {
				System.out.println("Nombre: " + author.getName());
				System.out.println("Año de Nacimiento: " + author.getBirthYear());
				System.out.println("Año de Fallecimiento: " + (author.getDeathYear() == 0 ? "N/A" : author.getDeathYear()));
				System.out.println("----------------------");
			}
		} else {
			System.out.println("No se han buscado libros aún.");
		}
	}


	private void listLivingAuthorsByYear(Scanner scanner) {
		System.out.print("Introduce el año para consultar autores vivos: ");
		int year = scanner.nextInt();
		scanner.nextLine(); // Consumir el salto de línea después de nextInt()

		List<Author> authors = gutendexClient.getAllAuthors();
		List<Author> livingAuthors = new ArrayList<>();
		for (Author author : authors) {
			if (author.getBirthYear() <= year && (author.getDeathYear() == 0 || author.getDeathYear() >= year)) {
				livingAuthors.add(author);
			}
		}

		if (!livingAuthors.isEmpty()) {
			System.out.println("Autores vivos en el año " + year + ":");
			for (Author author : livingAuthors) {
				System.out.println("Nombre: " + author.getName());
				System.out.println("Año de Nacimiento: " + author.getBirthYear());
				System.out.println("Año de Fallecimiento: " + (author.getDeathYear() == 0 ? "N/A" : author.getDeathYear()));
				System.out.println("----------------------");
			}
		} else {
			System.out.println("No se encontraron autores vivos en el año " + year);
		}
	}
	private void listBooksByLanguage(Scanner scanner) {
		System.out.print("Selecciona el idioma (en/tl): ");
		String language = scanner.nextLine().toLowerCase();

		if (!language.equals("en") && !language.equals("tl")) {
			System.out.println("Idioma no válido. Por favor, selecciona 'en' o 'tl'.");
			return;
		}

		List<Book> books = gutendexClient.getBooksByLanguage(language);
		if (books.isEmpty()) {
			System.out.println("No se encontraron libros en el idioma seleccionado.");
		} else {
			System.out.println("Libros en idioma " + (language.equals("en") ? "Inglés" : "Tagalo") + ":");
			for (Book book : books) {
				System.out.println("- Título: " + book.getTitle());
				System.out.println("  Autor: " + book.getAuthor().getName());
				System.out.println("  Idioma: " + book.getLanguage());
				System.out.println("  Número de Descargas: " + book.getDownloadCount());
				System.out.println("----------------------");
			}
		}
	}

}
