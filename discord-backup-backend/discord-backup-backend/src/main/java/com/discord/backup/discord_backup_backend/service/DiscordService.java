package com.discord.backup.discord_backup_backend.service;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
public class DiscordService {

    private final DiscordClient _discordClient;
    private final GatewayDiscordClient _gateway;

    public DiscordService(@Value("${discord.bot.token}") String token) {
        this._discordClient = DiscordClient.create(token);
        this._gateway = this._discordClient.login().block();

        assert this._gateway != null;
        this._gateway.on(ReadyEvent.class)
                .subscribe(readyEvent -> {
                    System.out.println("Logged in as: " + readyEvent.getSelf().getUsername());
                });
    }

    public Mono<String> createNewChannel(String guildId, String channelName) throws Exception {

        if (channelName == null || channelName.trim().isEmpty()) {
            throw new Exception("Channel name cannot be empty.");
        }

        return this._gateway.getGuildById(Snowflake.of(guildId))
                .switchIfEmpty(Mono.error(new IllegalStateException("Guild with ID " + guildId + " not found.")))
                .flatMap(guild -> guild.createTextChannel(channelName))
                .map(channel -> channel.getId().asString());
    }


    public void deleteChannel(String channelId) throws Exception {
        this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(Channel::delete).block();
    }

    public Mono<Message> sendMessage(String channelId, String content) {
        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(content));
    }

    public Mono<Message> sendMessage(String channelId, String content, String fileName, InputStream stream) {
        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(MessageCreateSpec
                        .builder()
                        .content(content)
                        .addFile(fileName, stream).build())
                );
    }

    public Mono<Void> deleteMessage(String channelId, Long messageId) {
        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(channel -> channel.getMessageById(Snowflake.of(messageId)))
                .flatMap(Message::delete);
    }

    public Mono<Message> editMessage(Long channelId, Long messageId, String newContent) {
        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(channel -> channel.getMessageById(Snowflake.of(messageId)))
                .flatMap(message -> message.edit(MessageEditSpec.builder().contentOrNull(newContent).build()));
    }

    public Flux<Message> getMessagesByChannelId(String channelId, Integer limit) {

        limit = Math.min(limit, 100);

        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMapMany(messageChannel -> messageChannel.getMessagesBefore(Snowflake.of(System.currentTimeMillis() << 22))).take(limit);
    }

    public Mono<Message> getMessageByMessageId(String channelId, String messageId) {
        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMap(messageChannel -> messageChannel.getMessageById(Snowflake.of(messageId)));
    }

    public Flux<Message> getMessagesBeforeMessageId(Long channelId, Long beforeMessageId, Integer limit) {

        limit = Math.min(limit, 100);

        return this._gateway.getChannelById(Snowflake.of(channelId))
                .cast(MessageChannel.class)
                .flatMapMany(messageChannel -> messageChannel.getMessagesBefore(Snowflake.of(beforeMessageId)))
                .take(limit);
    }

    @PreDestroy
    public void shutdown() {
        if (this._gateway != null) {
            this._gateway.logout().block();
        }
    }

}
