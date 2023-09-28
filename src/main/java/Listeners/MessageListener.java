package Listeners;

import Models.Item;
import Resources.Constants;
import Services.ListingService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ListingService listingService;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        listingService = initializeListingService();
        startScheduler(event);
    }

    private ListingService initializeListingService() {
        try {
            return ListingService.getInstance();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private void startScheduler(ReadyEvent event) {
        Runnable task = () -> {
            List<Item> items = getBestDeals(listingService);
            if (items != null && items.size() > 0) {
                sendItemsToChannels(event, items);
            }
        };
        // Initial delay before starting the task
        long SCHEDULE_DELAY = 0;
        // Time period in minutes
        long SCHEDULE_PERIOD = 5;
        scheduler.scheduleAtFixedRate(task, SCHEDULE_DELAY, SCHEDULE_PERIOD, TimeUnit.MINUTES);
    }

    private List<Item> getBestDeals(ListingService listingService) {
        return listingService.getDeals();
    }

    private void sendItemsToChannels(ReadyEvent event, List<Item> items) {
        for (Guild guild : event.getJDA().getGuilds()) {
            String TARGET_CHANNEL_NAME = Constants.DISCORD_CHANNEL_NAME;
            guild.getTextChannelsByName(TARGET_CHANNEL_NAME, true).forEach(channel -> {
                for (Item item : items) {
                    Role role = guild.getRolesByName(Constants.DISCORD_ROLE_NAME, true).get(0);
                    channel.sendMessage(role.getAsMention() + "\n" + item.toString()).queue();
                }
            });
        }
    }
}
