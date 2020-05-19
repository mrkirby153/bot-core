package com.mrkirby153.botcore.spring;

import com.mrkirby153.botcore.command.ClearanceResolver;
import com.mrkirby153.botcore.command.CommandExecutor;
import com.mrkirby153.botcore.command.CommandExecutor.MentionMode;
import com.mrkirby153.botcore.command.args.CommandContextResolver;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConditionalOnClass(CommandExecutor.class)
public class CommandAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CommandAutoConfiguration.class);

    private final String prefix;
    private final String owner;
    private final boolean alertNoClearance;
    private final boolean alertUnknownCommand;
    private final MentionMode mentionMode;

    public CommandAutoConfiguration(@Value("${bot.prefix:!}") String prefix,
        @Value("${bot.owner:}") String owner,
        @Value("${bot.command.alert-no-clearance:true}") boolean alertNoClearance,
        @Value("${bot.command.alert-unknown-command:true}") boolean alertUnknownCommand,
        @Value(
            "${bot.command.mention-mode:OPTIONAL}") MentionMode mentionMode) {
        this.prefix = prefix;
        this.owner = owner;
        this.alertNoClearance = alertNoClearance;
        this.alertUnknownCommand = alertUnknownCommand;
        this.mentionMode = mentionMode;
    }

    @Bean(name = "commandExecutor")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "bot.shard", havingValue = "false")
    public CommandExecutor jdaCommandExecutor(JDA jda,
        ClearanceResolver clearanceResolver,
        @Qualifier("contextResolvers") Map<String, CommandContextResolver> resolvers) {
        CommandExecutor ex = new CommandExecutor(prefix, mentionMode, jda, null);
        return getCommandExecutor(clearanceResolver, resolvers, ex);
    }

    @NotNull
    private CommandExecutor getCommandExecutor(
        ClearanceResolver clearanceResolver,
        @Qualifier("contextResolvers") Map<String, CommandContextResolver> resolvers,
        CommandExecutor ex) {
        ex.setAlertNoClearance(alertNoClearance);
        ex.setAlertUnknownCommand(alertUnknownCommand);
        ex.setClearanceResolver(clearanceResolver);
        if (resolvers != null) {
            log.debug("Registering {} context resolvers", resolvers.size());
            resolvers.forEach(ex::addContextResolver);
        }
        return ex;
    }

    @Bean(name = "commandExecutor")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "bot.shard", havingValue = "true")
    public CommandExecutor shardManagerCommandExecutor(ShardManager shardManager,
        @Qualifier("clearanceResolver") ClearanceResolver clearanceResolver,
        @Qualifier("contextResolvers") Map<String, CommandContextResolver> resolvers) {
        CommandExecutor ex = new CommandExecutor(prefix, mentionMode, null, shardManager);
        return getCommandExecutor(clearanceResolver, resolvers, ex);
    }

    @Bean(name = "clearanceResolver")
    @ConditionalOnMissingBean
    public ClearanceResolver defaultClearanceResolver() {
        return member -> {
            if (member.getUser().getId().equals(owner)) {
                return 100;
            } else {
                return 0;
            }
        };
    }

    @Bean(name = "contextResolvers")
    @ConditionalOnMissingBean
    public Map<String, CommandContextResolver> contextResolvers() {
        return null;
    }
}
