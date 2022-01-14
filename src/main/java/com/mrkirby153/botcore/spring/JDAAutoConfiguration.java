package com.mrkirby153.botcore.spring;

import com.mrkirby153.botcore.spring.event.BotReadyEvent;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

@ConditionalOnProperty("bot.token")
@Configuration
public class JDAAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JDAAutoConfiguration.class);

    private final String token;
    private final boolean eventRelay;
    private final ApplicationEventPublisher eventPublisher;

    private boolean applicationReady = false;
    private boolean botReady = false;

    public JDAAutoConfiguration(@Value("${bot.token}") String token,
        @Value("${bot.event.relay:true}") boolean eventRelay,
        ApplicationEventPublisher eventPublisher) {
        this.token = token;
        this.eventRelay = eventRelay;
        this.eventPublisher = eventPublisher;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultShardManagerBuilder defaultShardManagerBuilder() {
        return DefaultShardManagerBuilder.createDefault(token);
    }

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
        manager.addEventListener(new ShardReadyListener(manager));
        return manager;
    }

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

    private void handleShardReady(ShardManager shardManager) {
        log.debug("Handling shard ready event");
        long totalShards = shardManager.getShardsTotal();
        long readyShards = shardManager.getShards().stream()
            .filter(jda -> jda.getStatus() == Status.CONNECTED).count() + 1;
        if (totalShards == readyShards) {
            log.info("All shards ready!");
            log.info("Is application ready? {}", this.applicationReady);
            shardManager.setStatus(OnlineStatus.ONLINE);
            shardManager.setActivity(null);
            botReady = true;
            dispatchReadyEvent();
        } else {
            log.info("{}/{} shards ready", readyShards, totalShards);
        }
    }

    private class ShardReadyListener extends ListenerAdapter {

        private final ShardManager shardManager;

        private ShardReadyListener(ShardManager shardManager) {
            this.shardManager = shardManager;
        }

        @Override
        public void onReady(@Nonnull ReadyEvent event) {
            log.info("Shard {} has logged in as {}#{}", event.getJDA().getShardInfo().getShardId(),
                event.getJDA().getSelfUser().getName(),
                event.getJDA().getSelfUser().getDiscriminator());
            ShardManager sm = event.getJDA().getShardManager();
            if (sm != null) {
                sm.removeEventListener(this);
            }
            handleShardReady(this.shardManager);
        }
    }
}
