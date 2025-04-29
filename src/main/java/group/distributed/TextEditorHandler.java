package group.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

public class TextEditorHandler extends TextWebSocketHandler {

	private final ObjectMapper mapper = new ObjectMapper();

	// Sessions by WebSocketSession
	private final Map<WebSocketSession, ClientInfo> clients   = new ConcurrentHashMap<>();
	private final Map<String, StringBuilder>        documents = new ConcurrentHashMap<>();

	// Messages Queue
	private final Map<String, BlockingQueue<DifferenceMessage>> messageQueues = new ConcurrentHashMap<>();
	private final ExecutorService                               executor      = Executors.newCachedThreadPool();

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		clients.remove(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String   json = message.getPayload();
		JsonNode node = mapper.readTree(json);

		// for debugging
		//System.out.println("Action: " + json);

		String type = node.get("type").asText();

		if (initMessage(session, type, node)) return;

		if (updateMessage(session, type, json, node)) return;
	}

	private boolean initMessage(WebSocketSession session, String type, JsonNode node) {
		if (!type.equals("init")) return false;

		String sessionId = node.get("sessionId").asText();
		String username  = node.get("username").asText();
		String userColor = node.get("userColor").asText();

		clients.put(session, new ClientInfo(sessionId, username, userColor));

		// Load the session document if needed
		documents.computeIfAbsent(sessionId, id -> {
			try {
				Path filePath = SessionController.SESSIONS_DIR.resolve(sessionId + ".html");
				if (Files.exists(filePath)) {
					String content = Files.readString(filePath, StandardCharsets.UTF_8);
					return new StringBuilder(content);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return new StringBuilder();
		});

		// Start the queue processor for this session if not running
		messageQueues.computeIfAbsent(sessionId, id -> {
			BlockingQueue<DifferenceMessage> queue = new LinkedBlockingQueue<>();
			startQueueProcessor(id, queue);
			return queue;
		});

		return true;
	}

	private boolean updateMessage(WebSocketSession session, String type, String json, JsonNode node) throws
			IOException {
		if (!type.equals("update")) return false;

		ClientInfo info = clients.get(session);
		if (info == null) return false;

		String            sessionId  = info.sessionId;
		DifferenceMessage difference = mapper.treeToValue(node, DifferenceMessage.class);

		// Queue the difference for processing
		BlockingQueue<DifferenceMessage> queue = messageQueues.computeIfAbsent(sessionId, id -> {
			BlockingQueue<DifferenceMessage> newQueue = new LinkedBlockingQueue<>();
			startQueueProcessor(id, newQueue);
			return newQueue;
		});

		queue.offer(difference);

		// Broadcast to users in the same session
		for (WebSocketSession sess : clients.keySet()) {
			ClientInfo other = clients.get(sess);
			if (sess.isOpen() && !sess.equals(session) && other.sessionId.equals(info.sessionId)) {
				sess.sendMessage(new TextMessage(json));
			}
		}

		return true;
	}

	private void startQueueProcessor(String sessionId, BlockingQueue<DifferenceMessage> queue) {
		executor.submit(() -> {
			try {
				while (true) {
					DifferenceMessage difference = queue.poll(60, TimeUnit.SECONDS);

					if (difference == null) break;

					StringBuilder document = documents.computeIfAbsent(sessionId, k -> new StringBuilder());

					synchronized (document) {
						applyDifference(document, difference);
						ContentSaveController.saveContent(sessionId, document.toString());
					}
				}
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
		});
	}

	private void applyDifference(StringBuilder text, DifferenceMessage difference) {
		int start = difference.start;
		int end   = difference.end;

		if (start < 0) start = 0;
		if (end < start) end = start;
		if (end > text.length()) end = text.length();

		text.replace(start, end, difference.inserted);
	}

	public static class DifferenceMessage {
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
