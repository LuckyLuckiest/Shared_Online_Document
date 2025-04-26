package group.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TextEditorHandler extends TextWebSocketHandler {

	private final ObjectMapper mapper = new ObjectMapper();

	// Sessions by WebSocketSession
	private final Map<WebSocketSession, ClientInfo> clients = new ConcurrentHashMap<>();

	// One document per sessionId
	private final Map<String, StringBuilder> documents = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// Extract query params
		URI uri = session.getUri();
		if (uri == null) return;

		Map<String, String> query     = parseQueryParams(uri.getQuery());
		String              sessionId = query.getOrDefault("session", "default");
		String              username  = query.getOrDefault("username", "Anonymous");
		String              userColor = query.getOrDefault("userColor", "#000000");

		// Add client info
		clients.put(session, new ClientInfo(sessionId, username, userColor));

		// Initialize document if it doesn't exist
		documents.putIfAbsent(sessionId, new StringBuilder());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		clients.remove(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		ClientInfo info = clients.get(session);
		if (info == null) return;

		String   json      = message.getPayload();
		JsonNode node      = mapper.readTree(json);
		String   type      = node.get("type").asText();
		String   sessionId = getSessionIdFromUri(Objects.requireNonNull(session.getUri()));

		if ("diff".equals(type)) {
			DiffMessage   diff = mapper.treeToValue(node, DiffMessage.class);
			StringBuilder doc  = documents.computeIfAbsent(sessionId, k -> new StringBuilder());

			// Apply and save
			applyDiff(doc, diff);
			ContentSaveController.saveToFile(sessionId, doc.toString());
		}

		// Broadcast to users in the same session
		for (WebSocketSession sess : clients.keySet()) {
			ClientInfo other = clients.get(sess);
			if (sess.isOpen() && !sess.equals(session) && other.sessionId.equals(info.sessionId)) {
				sess.sendMessage(new TextMessage(json));
			}
		}
	}

	private String getSessionFilePath(String sessionId) {
		return Paths.get(System.getProperty("java.io.tmpdir"), sessionId + ".txt").toString();
	}

	private String getSessionIdFromUri(URI uri) {
		String query = uri.getQuery();

		if (query != null) for (String part : query.split("&")) {
			String[] pair = part.split("=");

			if (pair.length == 2 && pair[0].equalsIgnoreCase("session")) {
				return pair[1];
			}
		}

		return "default";
	}

	private void applyDiff(StringBuilder text, DiffMessage diff) {
		text.replace(diff.start, diff.end, diff.inserted);
	}

	private Map<String, String> parseQueryParams(String query) {
		Map<String, String> map = new HashMap<>();
		if (query == null || query.isEmpty()) return map;

		for (String pair : query.split("&")) {
			String[] parts = pair.split("=", 2);
			if (parts.length == 2) {
				map.put(parts[0], parts[1]);
			}
		}
		return map;
	}

	// Data classes

	public static class DiffMessage {
		public String type;
		public String userId;
		public String username;
		public String userColor;
		public String sessionId;
		public int    start;
		public int    end;
		public String inserted;
		public int    cursor;
	}

	private static class ClientInfo {
		String sessionId;
		String username;
		String userColor;

		public ClientInfo(String sessionId, String username, String userColor) {
			this.sessionId = sessionId;
			this.username  = username;
			this.userColor = userColor;
		}
	}

}
