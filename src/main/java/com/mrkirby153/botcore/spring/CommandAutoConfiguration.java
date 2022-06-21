package com.mrkirby153.botcore.spring;

import com.mrkirby153.botcore.command.ClearanceResolver;
import com.mrkirby153.botcore.command.CommandExecutor;
import com.mrkirby153.botcore.command.CommandExecutor.MentionMode;
import com.mrkirby153.botcore.command.args.CommandContextResolver;
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

/**
 * Autoconfiguration for chat based commands. This will automatically be loaded when
 * the {@code bot.command.chat.autoconfigure} property is set to {@code true}.
 * <br/>
 * All beans declared by this class can be manually overridden by declaring them locally
 */
@Configuration
@ConditionalOnClass(CommandExecutor.class)
@ConditionalOnProperty(value = "bot.command.chat.autoconfigure", havingValue = "true")
public class CommandAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CommandAutoConfiguration.class);

    private final String prefix;
    private final String owner;
    private final boolean alertNoClearance;
    private final boolean alertUnknownCommand;
    private final MentionMode mentionMode;

    /**
     * Constructs a new instance of the autoconfiguration.
     *
     * @param prefix              The command prefix. Injected from {@code bot.prefix}, defaults to {@code !}
     * @param owner               The owner of the bot. Injected from {@code bot.owner}
     * @param alertNoClearance    If the bot should alert when the user has no clearance. Injected from {@code bot.command.alert-no-clearance}, defaults to {@code true}
     * @param alertUnknownCommand If the bot should alert when the command was not found. Injected from {@code bot.command.alert-unknown-command}, defaults to {@code true}
     * @param mentionMode         The mention mode for the bot. Injected from {@code bot.command.mention-mode}, defaults to {@link MentionMode#OPTIONAL}
     */
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

    /**
     * Constructs a new command executor
     *
     * @param shardManager      The shard manager to use
     * @param clearanceResolver The clearance resolver to use
     * @param resolvers         A list of additional context resolvers to register
     *
     * @return A {@link CommandExecutor}
     */
    @Bean(name = "commandExecutor")
    @ConditionalOnMissingBean
    public CommandExecutor shardManagerCommandExecutor(ShardManager shardManager,
        @Qualifier("clearanceResolver") ClearanceResolver clearanceResolver,
        @Qualifier("contextResolvers") Map<String, CommandContextResolver> resolvers) {
        CommandExecutor ex = new CommandExecutor(prefix, mentionMode, null, shardManager);
        return getCommandExecutor(clearanceResolver, resolvers, ex);
    }

    /**
     * Bean constructing the default clearance resolver, giving the owner of the bot (as configured with
     * {@code bot.owner}) clearance level 100, and everyone else clearance level 0
     *
     * @return A {@link ClearanceResolver}
     */
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

    /**
     * Bean constructing a list of additional clearance resolvers to add to the {@link CommandAutoConfiguration#shardManagerCommandExecutor}
     *
     * @return A map of the context resolvers and the keys they are registered under
     */
    @Bean(name = "contextResolvers")
    @ConditionalOnMissingBean
    public Map<String, CommandContextResolver> contextResolvers() {
        return null;
    }
}
