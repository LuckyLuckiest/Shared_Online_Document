package group.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/save-content")
public class ContentSaveController {

	private static final ObjectMapper mapper = new ObjectMapper();

	@PostMapping
	public static void saveContentFromJson(@RequestParam String session, @RequestBody String jsonContent) {
		try {
			JsonNode jsonNode    = mapper.readTree(jsonContent);
			String   htmlContent = jsonNode.get("content").asText();

			saveContent(session, htmlContent);
		} catch (IOException e) {
			System.out.println("Error parsing content: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Used by internal server code (like TextEditorHandler) when we already have HTML
	public static void saveContent(String sessionId, String htmlContent) {
		Path sessionDir = Paths.get(System.getProperty("java.io.tmpdir"), "sessions");

		try {
			if (!Files.exists(sessionDir)) {
				Files.createDirectories(sessionDir);
			}

			Path sessionPath = sessionDir.resolve(sessionId + ".html");

			try (FileWriter writer = new FileWriter(sessionPath.toFile(), StandardCharsets.UTF_8)) {
				writer.write(htmlContent);
			}
		} catch (IOException exception) {
			System.out.println("A problem has occurred: " + exception.getMessage());
			exception.printStackTrace();
		}
	}
}
