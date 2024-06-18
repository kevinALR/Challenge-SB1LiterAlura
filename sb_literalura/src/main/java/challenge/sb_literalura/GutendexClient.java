package challenge.sb_literalura;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GutendexClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<Book> searchedBooks;

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public GutendexClient(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.searchedBooks = new ArrayList<>();
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Book searchBookByTitle(String searchQuery) throws Exception {
        String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        URI uri = new URI("https://gutendex.com/books/?search=" + encodedQuery);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            if (jsonResponse.has("results") && jsonResponse.get("results").isArray()) {
                JsonNode results = jsonResponse.get("results").get(0); // Tomar el primer resultado

                // Mapear el autor desde el JSON
                String authorName = results.get("authors").get(0).get("name").asText();
                int birthYear = results.get("authors").get(0).get("birth_year").asInt();
                int deathYear = results.get("authors").get(0).get("death_year").asInt();

                // Guardar el autor en la base de datos o recuperarlo si ya existe
                Author author = saveOrGetAuthor(authorName, birthYear, deathYear);

                // Mapear el libro desde el JSON
                String title = results.get("title").asText();
                String language = results.get("languages").get(0).asText();
                int downloadCount = results.get("download_count").asInt();

                // Crear el objeto Book
                Book book = new Book(title, author, language, downloadCount);

                // Guardar el libro en la base de datos
                bookRepository.save(book);

                // Agregar el libro buscado a la lista local
                searchedBooks.add(book);

                return book;
            } else {
                throw new RuntimeException("No se encontró ningún libro con la búsqueda proporcionada: " + searchQuery);
            }
        } else {
            throw new RuntimeException("Error al buscar el libro: " + response.statusCode());
        }
    }

    private Author saveOrGetAuthor(String authorName, int birthYear, int deathYear) {
        Optional<Author> existingAuthor = authorRepository.findByName(authorName);
        if (existingAuthor.isPresent()) {
            return existingAuthor.get();
        } else {
            Author newAuthor = new Author(authorName, birthYear, deathYear);
            return authorRepository.save(newAuthor);
        }
    }

    public List<Book> getSearchedBooks() {
        return bookRepository.findAll();
    }
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }
    public List<Book> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
    }

}
