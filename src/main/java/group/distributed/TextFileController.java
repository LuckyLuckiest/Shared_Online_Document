package group.distributed;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class TextFileController {

	@GetMapping("/content")
	public String getFileContent() throws Exception {
		return Files.readString(Paths.get("saved_document.txt"));
	}

}
