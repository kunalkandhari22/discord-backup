package com.discord.backup.discord_backup_backend.helper;

import com.discord.backup.discord_backup_backend.dto.DiscordMessageDto;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Helper {
    public static DiscordMessageDto convertToDiscordMessageDto(Message message) {
        String guildId = message.getGuildId().map(Snowflake::asString).orElse(null);

        // Author
        DiscordMessageDto.AuthorDto authorDto = message.getAuthor()
                .map(author -> new DiscordMessageDto.AuthorDto(
                        author.getId().asString(),
                        author.getUsername(),
                        author.getDiscriminator(),
                        Optional.ofNullable(author.getAvatarUrl()), // getAvatarUrl can return null
                        author.isBot()))
                .orElse(null); // Or provide a default "Unknown" author DTO

        // User Mentions
        List<DiscordMessageDto.UserMentionDto> userMentions = message.getUserMentions()
                .stream().map(user -> new DiscordMessageDto.UserMentionDto(
                        user.getId().asString(),
                        user.getUsername(),
                        user.getDiscriminator(),
                        Optional.ofNullable(user.getAvatarUrl())))
                .collect(Collectors.toList());

        // Role Mentions
        List<DiscordMessageDto.RoleMentionDto> roleMentions = message.getRoleMentions().toStream()
                .map(role -> new DiscordMessageDto.RoleMentionDto(
                        role.getId().asString(),
                        role.getName()))
                .collect(Collectors.toList());

        // Attachments
        List<DiscordMessageDto.AttachmentDto> attachments = message.getAttachments().stream()
                .map(attachment -> new DiscordMessageDto.AttachmentDto(
                        attachment.getId().asString(),
                        attachment.getFilename(),
                        Optional.empty(), // Description is not available in Discord4J Attachment
                        attachment.getContentType().orElse("application/octet-stream"),
                        attachment.getSize(),
                        attachment.getUrl(),
                        attachment.getProxyUrl(),
                        attachment.getHeight().isPresent() ? Optional.of(attachment.getHeight().getAsInt()) : Optional.empty(),
                        attachment.getWidth().isPresent() ? Optional.of(attachment.getWidth().getAsInt()) : Optional.empty()))
                .collect(Collectors.toList());

        // Embeds
        List<DiscordMessageDto.EmbedDto> embeds = message.getEmbeds().stream()
                .map(embed -> new DiscordMessageDto.EmbedDto(
                        embed.getTitle(),
                        Optional.ofNullable(embed.getType()).map(Embed.Type::name),
                        embed.getDescription(),
                        embed.getUrl(),
                        embed.getTimestamp(),
                        embed.getColor().map(discord4j.rest.util.Color::getRGB),
                        embed.getFooter().map(f -> new DiscordMessageDto.EmbedDto.FooterDto(
                                f.getText(),
                                f.getIconUrl(),        // Remove Optional.ofNullable() if this returns Optional
                                f.getProxyIconUrl())), // Remove Optional.ofNullable() if this returns Optional
                        embed.getImage().map(i -> new DiscordMessageDto.EmbedDto.ImageDto(
                                Optional.ofNullable(i.getUrl()),      // Remove Optional.ofNullable() if this returns Optional
                                Optional.ofNullable(i.getProxyUrl()), // Remove Optional.ofNullable() if this returns Optional
                                Optional.ofNullable(i.getHeight()),
                                Optional.ofNullable(i.getWidth()))),
                        embed.getThumbnail().map(t -> new DiscordMessageDto.EmbedDto.ThumbnailDto(
                                Optional.ofNullable(t.getUrl()),      // Remove Optional.ofNullable() if this returns Optional
                                Optional.ofNullable(t.getProxyUrl()), // Remove Optional.ofNullable() if this returns Optional
                                Optional.ofNullable(t.getHeight()),
                                Optional.ofNullable(t.getWidth()))),
                        embed.getVideo().map(v -> new DiscordMessageDto.EmbedDto.VideoDto(
                                Optional.ofNullable(v.getUrl()),      // Remove Optional.ofNullable() if this returns Optional
                                Optional.ofNullable(v.getHeight()),
                                Optional.ofNullable(v.getWidth()))),
                        embed.getProvider().map(p -> new DiscordMessageDto.EmbedDto.ProviderDto(
                                p.getName(),     // Remove Optional.ofNullable() if this returns Optional
                                p.getUrl())),    // Remove Optional.ofNullable() if this returns Optional
                        embed.getAuthor().map(a -> new DiscordMessageDto.EmbedDto.AuthorEmbedDto(
                                a.getName(),     // Remove Optional.ofNullable() if this returns Optional
                                a.getUrl(),      // Remove Optional.ofNullable() if this returns Optional
                                a.getIconUrl(),  // Remove Optional.ofNullable() if this returns Optional
                                a.getProxyIconUrl())), // Remove Optional.ofNullable() if this returns Optional
                        embed.getFields().stream()
                                .map(f -> new DiscordMessageDto.EmbedDto.FieldDto(
                                        f.getName(),
                                        f.getValue(),
                                        Optional.of(f.isInline()))) // Use Optional.of() for boolean
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        // Reactions
        List<DiscordMessageDto.ReactionDto> reactions = message.getReactions().stream()
                .map(reaction -> new DiscordMessageDto.ReactionDto(
                        reaction.getCount(),
                        reaction.selfReacted(),
                        new DiscordMessageDto.ReactionDto.EmojiDto(
                                reaction.getEmoji().asCustomEmoji().map(e -> e.getId().asString()),
                                reaction.getEmoji().asUnicodeEmoji().map(u -> u.getRaw()), // Unicode emoji name
                                reaction.getEmoji().asCustomEmoji().map(e -> e.isAnimated()) // Custom emoji animated status
                        )))
                .collect(Collectors.toList());

        // Message Reference (for replies)
        Optional<DiscordMessageDto.MessageReferenceDto> messageReference = message.getMessageReference()
                .map(ref -> new DiscordMessageDto.MessageReferenceDto(
                        ref.getMessageId().map(Snowflake::asString),
                        Optional.of(ref.getChannelId()).map(Snowflake::asString),
                        ref.getGuildId().map(Snowflake::asString),
                        ref.getData().failIfNotExists().toOptional()
                ));

        // Components (e.g., buttons, select menus)
        List<DiscordMessageDto.ComponentDto> components = message.getComponents().stream()
                .map(Helper::convertComponentToDto)
                .collect(Collectors.toList());


        return new DiscordMessageDto(
                message.getId().asString(),
                message.getChannelId().asString(),
                guildId,
                message.getContent(), // getContent returns Optional<String>
                authorDto,
                message.getTimestamp(),
                message.getEditedTimestamp(),
                message.isTts(),
                message.isPinned(),
                message.mentionsEveryone(),
                userMentions,
                roleMentions,
                attachments,
                embeds,
                reactions,
                message.getWebhookId().map(Snowflake::asString),
                messageReference,
                message.getType().name(), // Convert Message.Type enum to string
                components
        );
    }

    // Helper to convert Discord4J Component to ComponentDto
    private static DiscordMessageDto.ComponentDto convertComponentToDto(LayoutComponent component) {
        Optional<String> customId = Optional.empty();
        Optional<String> label = Optional.empty();
        Optional<String> url = Optional.empty();
        Optional<String> emoji = Optional.empty();
        List<DiscordMessageDto.ComponentDto> nestedComponents = List.of();

        if (component instanceof ActionRow) {
            ActionRow actionRow = (ActionRow) component;
            nestedComponents = actionRow.getChildren().stream()
                    .filter(ActionComponent.class::isInstance) // Ensure it's an ActionComponent
                    .map(ActionComponent.class::cast)           // Cast to ActionComponent
                    .map(Helper::convertActionComponentToDto)
                    .collect(Collectors.toList());
        }

        return new DiscordMessageDto.ComponentDto(
                component.getType().getValue(),
                customId,
                label,
                url,
                emoji,
                nestedComponents
        );
    }

    private static DiscordMessageDto.ComponentDto convertActionComponentToDto(ActionComponent component) {
        Optional<String> customId = Optional.empty();
        Optional<String> label = Optional.empty();
        Optional<String> url = Optional.empty();
        Optional<String> emoji = Optional.empty();
        List<DiscordMessageDto.ComponentDto> nestedComponents = List.of();

        if (component instanceof Button) {
            Button button = (Button) component;
            if (button.getCustomId().isPresent()) {
                customId = button.getCustomId();
            } else if (button.getUrl().isPresent()) {
                url = button.getUrl();
            }
            label = button.getLabel();
            emoji = button.getEmoji().map(Object::toString);
        } else if (component instanceof SelectMenu) {
            SelectMenu selectMenu = (SelectMenu) component;
            customId = Optional.of(selectMenu.getCustomId());
        }

        return new DiscordMessageDto.ComponentDto(
                component.getType().getValue(),
                customId,
                label,
                url,
                emoji,
                nestedComponents
        );
    }
}
