package group.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TextEditorHandler extends TextWebSocketHandler {

	private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
	private final ObjectMapper          mapper   = new ObjectMapper();
	private       StringBuilder         fullText = new StringBuilder();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String   json = message.getPayload();
		JsonNode node = mapper.readTree(json);

		String type = node.get("type").asText();

		if (type.equals("diff")) {
			DiffMessage diff = mapper.treeToValue(node, DiffMessage.class);

			applyDiff(diff);
			saveToFile(fullText.toString());
		}

		// broadcast everything
		for (WebSocketSession ses : sessions) {
			if (!(ses.isOpen() && !ses.equals(session))) continue;

			ses.sendMessage(new TextMessage(json));
		}

	}

	private void applyDiff(DiffMessage diff) {
		fullText.replace(diff.start, diff.end, diff.inserted);
	}

	private void saveToFile(String content) {
		try {
			File file = new File("saved_document.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (FileWriter writer = new FileWriter(file)) {
				writer.write(content);
			}
		} catch (IOException exception) {
			System.out.println("A problem has occurred: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public static class DiffMessage {
		public String userId;
		public int    start;
		public int    end;
		public String inserted;
	}
}
