import Listeners.EmoteListener;
import Listeners.JoinListener;
import Listeners.MessageListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String token = loadDiscordApiToken();
        if (token == null) {
            System.out.println("Discord API token not found");
            return;
        }

        JDABuilder builder = JDABuilder.createDefault(token);

        // Set the activity for the session
        builder.setActivity(Activity.watching("skin prices"));

        // Add event listeners
        builder.addEventListeners(new JoinListener());
        builder.addEventListeners(new MessageListener());
        builder.addEventListeners(EmoteListener.getInstance());

        // Set enabled intents
        builder.setEnabledIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.SCHEDULED_EVENTS);

        // Set member cache and chunking
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);

        // Build that bitch
        builder.build();
    }

    private static String loadDiscordApiToken() throws ParserConfigurationException, IOException, SAXException {
        InputStream in = Main.class.getClassLoader().getResourceAsStream("apikeys.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);
        document.getDocumentElement().normalize();

        NodeList apiKeyNodes = document.getElementsByTagName("string");

        for (int i = 0; i < apiKeyNodes.getLength(); i++) {
            Node apiKeyNode = apiKeyNodes.item(i);
            if (apiKeyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element apiKeyElement = (Element) apiKeyNode;
                String apiKeyName = apiKeyElement.getAttribute("name");
                if ("discordapi".equals(apiKeyName)) {
                    return apiKeyElement.getTextContent().trim();
                }
            }
        }

        return null;
    }
}
