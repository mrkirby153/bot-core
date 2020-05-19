package com.mrkirby153.botcore.spring;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

@Configuration
public class JDAAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JDAAutoConfiguration.class);

    private final String token;
    private final boolean eventRelay;
    private final ApplicationEventPublisher eventPublisher;

    public JDAAutoConfiguration(@Value("${bot.token}") String token,
        @Value("${bot.event.relay:true}") boolean eventRelay,
        ApplicationEventPublisher eventPublisher) {
        this.token = token;
        this.eventRelay = eventRelay;
        this.eventPublisher = eventPublisher;
    }

    @Bean
    @ConditionalOnMissingBean
    public JDABuilder jdaBuilder() {
        return JDABuilder.createDefault(token);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultShardManagerBuilder defaultShardManagerBuilder() {
        return DefaultShardManagerBuilder.createDefault(token);
    }

    @Bean(name = "jda")
    @ConditionalOnProperty(value = "bot.shard", havingValue = "false")
    public JDA jda(JDABuilder builder, @Qualifier("jdaEventHandler") EventHandler eventHandler)
        throws LoginException {
        log.info("Starting bot");
        JDA jda = builder.build();
        if (eventRelay) {
            jda.addEventListener(eventHandler);
        }
        jda.addEventListener(new JDAReadyListener());
        return jda;
    }

    @Bean(name = "shardManager")
    @ConditionalOnProperty(value = "bot.shard", havingValue = "true")
    public ShardManager shardManager(DefaultShardManagerBuilder defaultShardManagerBuilder,
        @Qualifier("jdaEventHandler") EventHandler eventHandler)
        throws LoginException {
        ShardManager manager = defaultShardManagerBuilder.build();
        if (eventRelay) {
            manager.addEventListener(eventHandler);
        }
        manager.addEventListener(new ShardReadyListener());
        return manager;
    }

    @Bean(name = "jdaEventHandler")
    @ConditionalOnMissingBean
    public EventHandler jdaEventHandler() {
        return new EventHandler(eventPublisher);
    }

    private class JDAReadyListener extends ListenerAdapter {

        @Override
        public void onReady(@Nonnull ReadyEvent event) {
            log.info("Logged in as {}#{}", event.getJDA().getSelfUser().getName(),
                event.getJDA().getSelfUser().getDiscriminator());
            event.getJDA().removeEventListener(this);
        }
    }

    private class ShardReadyListener extends ListenerAdapter {

        @Override
        public void onReady(@Nonnull ReadyEvent event) {
            log.info("Shard {} has logged in as {}#{}", event.getJDA().getShardInfo().getShardId(),
                event.getJDA().getSelfUser().getName(),
                event.getJDA().getSelfUser().getDiscriminator());
            ShardManager sm = event.getJDA().getShardManager();
            if (sm != null) {
                sm.removeEventListener(this);
            }
        }
    }
}
