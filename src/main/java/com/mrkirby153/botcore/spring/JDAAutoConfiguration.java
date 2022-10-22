package com.mrkirby153.botcore.spring;

import com.mrkirby153.botcore.command.slashcommand.dsl.DslCommandExecutor;
import com.mrkirby153.botcore.spring.event.BotReadyEvent;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;

/**
 * Automatically sets up a {@link ShardManager} configured with a bot token. The bot will automatically
 * fire a {@link BotReadyEvent} when the bot has logged in completely
 */
@ConditionalOnProperty("bot.token")
@Configuration
public class JDAAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JDAAutoConfiguration.class);

    private static final Pattern rangePattern = Pattern.compile("(\\d+)\\.\\.(\\d+)");

    private final String token;
    private final boolean eventRelay;
    private final ApplicationEventPublisher eventPublisher;

    private final int totalShards;
    private final String[] shards;
    private final String[] extraIntents;

    private boolean applicationReady = false;
    private boolean botReady = false;

    /**
     * Constructs a new configuration bean
     *
     * @param token          The bot token. Injected from {@code bot.token}
     * @param eventRelay     If events from JDA should automatically be relayed to Spring's event bus. Injected from {@code bot.event.relay}, defaults to true
     * @param shards         The shard ids to start. Injected from {@code bot.shards.shards}, defaults to an empty string
     * @param totalShards    The total number of shards to start. Injected from {@code bot.shards.total}, defaults to -1 (Discord recommended)
     * @param intents        Additional intents to enable. Injected from {@code bot.extra-intents}, defaults to an empty string (No additional intents)
     * @param eventPublisher The event bus publisher to relay events to
     */
    public JDAAutoConfiguration(@Value("${bot.token}") String token,
        @Value("${bot.event.relay:true}") boolean eventRelay,
        @Value("${bot.shards.shards:}") String[] shards,
        @Value("${bot.shards.total:-1}") int totalShards,
        @Value("${bot.extra-intents:}") String[] intents,
        ApplicationEventPublisher eventPublisher) {
        this.token = token;
        this.eventRelay = eventRelay;
        this.eventPublisher = eventPublisher;
        this.shards = shards;
        this.extraIntents = intents;
        this.totalShards = totalShards;
    }

    /**
     * Bean for a shard manager builder
     *
     * @return The shard manager
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultShardManagerBuilder defaultShardManagerBuilder() {
        List<Integer> shards = getShardIds();
        List<GatewayIntent> extraIntents = getExtraIntents();
        log.info(
            "Initializing a DefaultShardManagerBuilder with the shards [{}] of {} and enabling additional intents: [{}]",
            totalShards == -1 ? "auto" : shards.stream().map(Object::toString).collect(
                Collectors.joining(",")), totalShards == -1 ? "auto" : totalShards,
            extraIntents.stream().map(Objects::toString).collect(
                Collectors.joining(",")));
        return DefaultShardManagerBuilder.createDefault(token).setShardsTotal(totalShards)
            .setShards(shards).enableIntents(getExtraIntents());
    }

    /**
     * Bean creating a {@link ShardManager}. While shards start up, the bot will automatically
     * set its status to "idle" and a playing status of "Starting up...". Once the bot has loaded
     * it will switch itself to "online" and remove its playing status
     *
     * @param defaultShardManagerBuilder The builder to use when creating the shard manager
     * @param eventHandler               The event handler to relay events on, if enabled
     *
     * @return The shard manager
     *
     * @throws LoginException If there was an error logging in
     */
    @Bean(name = "shardManager")
    @ConditionalOnMissingBean
    public ShardManager shardManager(DefaultShardManagerBuilder defaultShardManagerBuilder,
        @Qualifier("jdaEventHandler") EventHandler eventHandler)
        throws LoginException {
        ShardManager manager = defaultShardManagerBuilder.build();
        // Set shards to idle while everything boots up
        manager.setStatus(OnlineStatus.IDLE);
        manager.setActivity(Activity.playing("Starting up..."));
        if (eventRelay) {
            manager.addEventListener(eventHandler);
        }
        manager.addEventListener(dslCommandExecutor());
        manager.addEventListener(new ShardReadyListener(manager));
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean
    public DslCommandExecutor dslCommandExecutor() {
        return new DslCommandExecutor();
    }

    /**
     * Bean for the JDA to Spring event bus
     *
     * @return The Event Handler
     */
    @Bean(name = "jdaEventHandler")
    @ConditionalOnMissingBean
    public EventHandler jdaEventHandler() {
        return new EventHandler(eventPublisher);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.debug("Application is ready. Is Bot? {}", this.botReady);
        applicationReady = true;
        dispatchReadyEvent();
    }

    private void dispatchReadyEvent() {
        if (botReady && applicationReady) {
            log.debug("Bot and application are ready. Bot is online and good to go");
            eventPublisher.publishEvent(new BotReadyEvent());
        } else {
            log.debug(
                "Deferring ready event. Bot or application not ready. Bot: {}, Application: {}",
                botReady, applicationReady);
        }
    }

    private void handleShardReady(ShardManager shardManager, ShardReadyListener listener) {
        log.debug("Handling shard ready event");
        long totalShards = shardManager.getShards().size();
        long readyShards = shardManager.getShards().stream()
            .filter(jda -> jda.getStatus() == Status.CONNECTED).count() + 1;
        if (totalShards == readyShards) {
            log.info("All shards ready!");
            log.debug("Is application ready? {}", this.applicationReady);
            shardManager.setStatus(OnlineStatus.ONLINE);
            shardManager.setActivity(null);
            botReady = true;
            dispatchReadyEvent();
            shardManager.removeEventListener(listener);
        } else {
            log.info("{}/{} shards ready", readyShards, totalShards);
        }
    }

    private List<Integer> getShardIds() {
        if (shards.length == 1 && shards[0].equals("auto")) {
            return Collections.emptyList();
        }
        List<Integer> shardIds = new ArrayList<>();
        for (String shard : shards) {
            Matcher m = rangePattern.matcher(shard);
            if (m.find()) {
                try {
                    int start = Integer.parseInt(m.group(1));
                    int end = Integer.parseInt(m.group(2));
                    if (start > end) {
                        throw new IllegalArgumentException("Provided range " + shard
                            + " is not valid. Range must be strictly ascending");
                    }
                    for (int i = start; i <= end; i++) {
                        shardIds.add(i);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Provided range " + shard + " is not valid");
                }
            } else {
                try {
                    shardIds.add(Integer.parseInt(shard));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Provided shard id " + shard + " is not a number or range");
                }
            }
        }
        return shardIds;
    }

    private List<GatewayIntent> getExtraIntents() {
        List<GatewayIntent> intents = new ArrayList<>();
        for (String intent : extraIntents) {
            try {
                intents.add(GatewayIntent.valueOf(intent));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Provided intent " + intent + " is not a valid intent");
            }
        }
        return intents;
    }

    private class ShardReadyListener extends ListenerAdapter {

        private final ShardManager shardManager;

        private ShardReadyListener(ShardManager shardManager) {
            this.shardManager = shardManager;
        }

        @Override
        public void onReady(ReadyEvent event) {
            log.info("Shard {} has logged in as {}#{}", event.getJDA().getShardInfo().getShardId(),
                event.getJDA().getSelfUser().getName(),
                event.getJDA().getSelfUser().getDiscriminator());
            handleShardReady(this.shardManager, this);
        }
    }
}
