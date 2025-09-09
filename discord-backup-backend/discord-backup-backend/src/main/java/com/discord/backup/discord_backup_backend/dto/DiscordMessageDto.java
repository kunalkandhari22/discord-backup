package com.discord.backup.discord_backup_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Setter
public class DiscordMessageDto {
    private String id;
    private String channelId;
    private String guildId; // Optional: Message might be in a DM, so guildId might not be present
    private String content;
    private AuthorDto author;
    private Instant timestamp;
    private Optional<Instant> editedTimestamp; // Optional
    private boolean tts;
    private boolean pinned;
    private boolean mentionsEveryone;
    private List<UserMentionDto> userMentions;
    private List<RoleMentionDto> roleMentions;
    private List<AttachmentDto> attachments;
    private List<EmbedDto> embeds;
    private List<ReactionDto> reactions;
    private Optional<String> webhookId; // Optional
    private Optional<MessageReferenceDto> messageReference; // For replies/crossposts
    private String type; // Message.Type enum as string
    private List<ComponentDto> components; // For message components (buttons, select menus etc.)

    @AllArgsConstructor
    @Getter
    @Setter
    public static class AuthorDto {
        private String id;
        private String username;
        private String discriminator;
        private Optional<String> avatarUrl;
        private boolean isBot;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserMentionDto {
        private String id;
        private String username;
        private String discriminator;
        private Optional<String> avatarUrl;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class RoleMentionDto {
        private String id;
        private String name;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class AttachmentDto {
        private String id;
        private String filename;
        private Optional<String> description; // Optional
        private String contentType; // MIME type
        private long size; // in bytes
        private String url;
        private String proxyUrl;
        private Optional<Integer> height; // Optional, for images/videos
        private Optional<Integer> width;  // Optional, for images/videos
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class EmbedDto {
        private Optional<String> title;
        private Optional<String> type;
        private Optional<String> description;
        private Optional<String> url;
        private Optional<Instant> timestamp;
        private Optional<Integer> color;
        private Optional<FooterDto> footer;
        private Optional<ImageDto> image;
        private Optional<ThumbnailDto> thumbnail;
        private Optional<VideoDto> video;
        private Optional<ProviderDto> provider;
        private Optional<AuthorEmbedDto> author;
        private List<FieldDto> fields;

        @AllArgsConstructor
        @Getter
        @Setter
        public static class FooterDto {
            private String text;
            private Optional<String> iconUrl;
            private Optional<String> proxyIconUrl;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class ImageDto {
            private Optional<String> url;
            private Optional<String> proxyUrl;
            private Optional<Integer> height;
            private Optional<Integer> width;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class ThumbnailDto {
            private Optional<String> url;
            private Optional<String> proxyUrl;
            private Optional<Integer> height;
            private Optional<Integer> width;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class VideoDto {
            private Optional<String> url;
            private Optional<Integer> height;
            private Optional<Integer> width;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class ProviderDto {
            private Optional<String> name;
            private Optional<String> url;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class AuthorEmbedDto { // Renamed to avoid clash with main AuthorDto
            private Optional<String> name;
            private Optional<String> url;
            private Optional<String> iconUrl;
            private Optional<String> proxyIconUrl;
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class FieldDto {
            private String name;
            private String value;
            private Optional<Boolean> inline; // Optional
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReactionDto {
        private int count;
        private boolean me;
        private EmojiDto emoji;

        @AllArgsConstructor
        @Getter
        @Setter
        public static class EmojiDto {
            private Optional<String> id; // Custom emoji ID
            private Optional<String> name; // Emoji name (e.g., "👍" or "custom_emoji_name")
            private Optional<Boolean> animated; // For custom emojis
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class MessageReferenceDto {
        private Optional<String> messageId;
        private Optional<String> channelId;
        private Optional<String> guildId;
        private Optional<Boolean> failIfNotExists;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class ComponentDto {
        private int type; // Component.Type value (e.g., 1 for ActionRow, 2 for Button)
        private Optional<String> customId; // For interactive components
        private Optional<String> label; // For buttons
        private Optional<String> url; // For URL buttons
        private Optional<String> emoji; // For buttons with emoji
        private List<ComponentDto> components; // For ActionRows containing nested components
    }
}