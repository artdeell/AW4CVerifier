package org.example.botimpl;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.example.Utils;
import org.example.db.DbCreatorInterface;
import org.example.db.DbQueryInterface;

import java.sql.SQLException;
import java.util.Objects;

public class Discord extends ListenerAdapter {
    public static JDA jda;
    public static void start() {
        JDABuilder builder = JDABuilder.createLight(BotConstants.DISCORD_TOKEN);
        builder.disableCache(CacheFlag.ACTIVITY);
        builder.setMemberCachePolicy(MemberCachePolicy.NONE);
        builder.setChunkingFilter(ChunkingFilter.NONE);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);
        builder.setLargeThreshold(50);
        jda = builder.build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Objects.requireNonNull(jda.getGuildById(1105210625803698367L)).updateCommands().addCommands(
                Commands.slash("generate_key", "Generates an AW4C key"),
                Commands.slash("get_remaining_regisrations", "Tells you how much registrations you have remaining")
        ).queue();
        jda.addEventListener(new Discord());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        DbCreatorInterface creatorInterface = null;
        DbQueryInterface queryInterface = null;
        long userId = event.getUser().getIdLong();
        SlashCommandInteraction interaction = event.getInteraction();
        InteractionHook hook = interaction.getHook();
        switch (event.getName()) {
            case "generate_key":
                interaction.deferReply(true).queue();
                try {
                    queryInterface = DbQueryInterface.take();
                    String key = queryInterface.getUserKey(userId, BotConstants.BOT_TYPE_DISCORD);
                    if(key == null) {
                        creatorInterface = DbCreatorInterface.take();
                        key = Utils.enrollRandomKey(queryInterface, creatorInterface);
                        creatorInterface.createMessengerRecord(userId, BotConstants.BOT_TYPE_DISCORD, key);
                        hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.THANK_YOU, key)).queue();
                    }else {
                        hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.WELCOME_BACK, key)).queue();
                    }
                }catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                    hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.SQL_ERROR))
                            .queue();
                }
                break;
            case "get_remaining_regisrations":
                interaction.deferReply(true).queue();
                try {
                    queryInterface = DbQueryInterface.take();
                    String key = queryInterface.getUserKey(userId, BotConstants.BOT_TYPE_DISCORD);
                    if(key != null) {
                        long remainingRegistrations = queryInterface.getRemainingRegistrations(key);
                        if(remainingRegistrations < 0) {
                            hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.SQL_ERROR))
                                    .queue();
                        }else {
                            hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.REGISTRATIONS_REMAINING, remainingRegistrations))
                                    .queue();
                        }
                    }else {
                        hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.NO_KEY_YET))
                                .queue();
                    }
                }catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                    hook.editOriginal(LocalizableMessages.getString("en", LocalizableMessages.SQL_ERROR))
                            .queue();
                }
                break;
        }
        if(creatorInterface != null) DbCreatorInterface.giveBack(creatorInterface);
        if(queryInterface != null) DbQueryInterface.giveBack(queryInterface);
    }
}


