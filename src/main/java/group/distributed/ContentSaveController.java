package group.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/save-content")
public class ContentSaveController {

	private final ObjectMapper mapper = new ObjectMapper();

	@PostMapping
	public void saveContent(@RequestParam String session, @RequestBody String jsonContent) {
		try {
			// Parse the JSON content sent by the client
			JsonNode jsonNode = mapper.readTree(jsonContent);
			String   content  = jsonNode.get("content").asText();

			// Save the content to a file
			saveToFile(session, content);

		} catch (IOException e) {
			System.out.println("Error parsing content: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Save content to a file with a given session ID
	protected static void saveToFile(String sessionId, String content) {
		Path sessionPath = Paths.get(System.getProperty("java.io.tmpdir"), "sessions", sessionId + ".txt");

		try (FileWriter writer = new FileWriter(sessionPath.toFile())) {
			writer.write(content);
		} catch (IOException exception) {
			System.out.println("A problem has occurred: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

}
