import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;

class SessionManager {
    private final Set<Session> sessions = new HashSet<>();
    private final Map<String, Session> addresses = new HashMap<>();

    public Set<Session> getSessions() {
        return sessions;
    }

    public Session getSessionByPacket(DatagramPacket p) throws IOException {
        String key = p.getAddress().getHostAddress() + ":" + p.getPort();
        if (addresses.containsKey(key)) {
            return addresses.get(key);
        }
        else {
            Session session = new Session(p.getAddress(), p.getPort(), this);
            sessions.add(session);
            addresses.put(key, session);
            return session;
        }
    }

    public void close(Session session) {
        String key = session.getAddress().getHostAddress() + ":" + session.getPort();
        addresses.remove(key);
        sessions.remove(session);
    }

    public List<Session> getSessionList() {
        return new ArrayList<>(sessions);
    }
}