package Listeners;

import Resources.Constants;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmoteListener extends ListenerAdapter {
    private static EmoteListener instance;
    // Map to store guild IDs and their corresponding initial message IDs
    private final Map<String, String> initialMessageMap;

    private EmoteListener() {
        initialMessageMap = new HashMap<>();
    }

    public static EmoteListener getInstance() {
        if (instance == null) {
            instance = new EmoteListener();
        }
        return instance;
    }

    // Method to set the initial message ID for a guild
    public void setInitialMessageId(String guildId, String messageId) {
        initialMessageMap.put(guildId, messageId);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUserId().equals(event.getJDA().getSelfUser().getId())) {
            return; // Ignore reactions from the bot itself
        }

        String guildId = event.getGuild().getId();
        String initialMessageId = initialMessageMap.get(guildId);

        if (initialMessageId != null && event.getMessageId().equals(initialMessageId)) {
            Emoji thumbsUpEmoji = Emoji.fromUnicode("U+1F44D");
            Message message = event.getChannel().retrieveMessageById(initialMessageId).complete();
            List<MessageReaction> thumbsUpReactions = message.getReactions();
            for (MessageReaction thumbsUpReaction : thumbsUpReactions) {
                if (thumbsUpReaction.getEmoji().getAsReactionCode().equals(thumbsUpEmoji.getAsReactionCode())) {
                    Member member = event.getMember();
                    if (member != null) {
                        // Replace "SkinPriceChecker" with your actual role name
                        event.getGuild().addRoleToMember(member, event.getGuild().getRolesByName(Constants.DISCORD_ROLE_NAME, true).get(0)).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUserId().equals(event.getJDA().getSelfUser().getId())) {
            return; // Ignore reactions removed by the bot itself
        }

        String guildId = event.getGuild().getId();
        String initialMessageId = initialMessageMap.get(guildId);

        if (initialMessageId != null && event.getMessageId().equals(initialMessageId)) {
            Emoji thumbsUpEmoji = Emoji.fromUnicode("U+1F44D");
            Message message = event.getChannel().retrieveMessageById(initialMessageId).complete();
            List<MessageReaction> thumbsUpReactions = message.getReactions();
            for (MessageReaction thumbsUpReaction : thumbsUpReactions) {
                if (thumbsUpReaction.getEmoji().getAsReactionCode().equals(thumbsUpEmoji.getAsReactionCode())) {
                    Member member = event.getMember();
                    if (member != null) {
                        // Replace "SkinPriceChecker" with your actual role name
                        event.getGuild().removeRoleFromMember(member, event.getGuild().getRolesByName("SkinPriceChecker", true).get(0)).queue();
                    }
                }
            }
        }
    }
}
