package Listeners;

import Resources.Constants;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;

public class JoinListener extends ListenerAdapter {
    Guild guild;
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        super.onGuildJoin(event);
        guild = event.getGuild();
        createChannel();
        createRole();
        createInitialMessage();
    }

    private void createRole() {
        if(guild.getRolesByName(Constants.DISCORD_ROLE_NAME, true).size() == 0){
            guild.createRole().setName(Constants.DISCORD_ROLE_NAME).queue();
        }
    }

    private void createChannel() {
        String channelName = Constants.DISCORD_CHANNEL_NAME;
        if(guild.getTextChannelsByName(channelName, true).size() == 0){
            guild.createTextChannel(channelName).queue();
        }
    }

    private void createInitialMessage() {
        TextChannel channel = guild.getTextChannelsByName(Constants.DISCORD_CHANNEL_NAME, true).get(0);
        MessageCreateAction messageCreateAction = channel.sendMessage("""
                Hello! I am SkinPriceChecker, a bot that checks the prices of CS:GO skins on CSFloat and Buff163.
                I will post the best deals I find every minute.
                To get notified when I post a deal, react to this message with a thumbs up.
                If you would like to add me to your server, click here: https://discord.com/oauth2/authorize?client_id=1141585102619545690&permissions=268437584&scope=bot
                """
        );
        Message message = messageCreateAction.complete();
        message.pin().queue();
        message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
        EmoteListener emoteListener = EmoteListener.getInstance();
        emoteListener.setInitialMessageId(guild.getId(), message.getId());
    }
}
