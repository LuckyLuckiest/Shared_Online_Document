package group.distributed;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class SessionController {

	protected static final Path   SESSIONS_DIR      = Paths.get(System.getProperty("java.io.tmpdir"), "sessions");
	// need to work on the regex
	private static final String SESSION_KEY_REGEX = "^session-[a-zA-Z0-9_-]{6,12}$";

	static {
		try {
			Files.createDirectories(SESSIONS_DIR);
		} catch (IOException e) {
			throw new RuntimeException("Could not create sessions directory", e);
		}
	}

	@GetMapping("/content")
	public String getContent(@RequestParam String session) throws IOException {
		// Validate the session key
		if (!isValidSessionKey(session)) {
			throw new IllegalArgumentException("Invalid session key.");
		}

		Path filePath = getSessionFilePath(session);

		if (!Files.exists(filePath)) {
			Files.writeString(filePath, "");
		}

		return Files.readString(filePath);
	}

	private boolean isValidSessionKey(String sessionKey) {
		Pattern pattern = Pattern.compile(SESSION_KEY_REGEX);
		Matcher matcher = pattern.matcher(sessionKey);
		return matcher.matches();
	}

	private Path getSessionFilePath(String sessionId) {
		return SESSIONS_DIR.resolve(sessionId + ".html");
	}
}
