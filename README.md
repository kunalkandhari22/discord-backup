Discord Backup Application

This project is a Discord backup tool that uses Discord as storage to upload and download files. It supports both single-threaded and multi-threaded operations, making it easier to handle large backups efficiently.

For setup help, DM on Instagram: @kunal.kandhari


🚀 Features

Upload and download files using Discord channels as storage.

Two modes available:

Single-threaded: Uses one bot client (slow due to Discord rate limits).

Multi-threaded: Uses up to 4 bots for parallel file operations.


PostgreSQL database integration for tracking backups.

REST API endpoints through a Discord controller.


⚙️ Prerequisites

Before setting up the project, ensure you have the following:

PostgreSQL Database

Use the provided script discord_backup.sql to create the required tables.

Update your DB configuration in application.properties.


Discord Bot(s)

Create at least 1 Discord bot from the Discord Developer Portal.

Add your bot token(s) to application.properties.


🔑 Bot Configuration

In application.properties, set your bot tokens:

# Required for single-threaded operations & APIs
discord.bot.token=YOUR_BOT_TOKEN_HERE  

# Required only for multi-threaded operations
discord.bot.token2=YOUR_BOT_TOKEN_HERE  
discord.bot.token3=YOUR_BOT_TOKEN_HERE  
discord.bot.token4=YOUR_BOT_TOKEN_HERE  



Single-threaded methods (default) use only discord.bot.token.

Multi-threaded methods use all four tokens to upload/download files in parallel.


📂 File Operations

Core logic is inside ProcessSplitFilesService.java.

Upload Methods

uploadFilesToChannel → Single-threaded (slow, one bot).

uploadFilesToChannelMulThread → Multi-threaded (fast, uses 4 bots).

Download Methods

downloadFilesFromChannel → Single-threaded (slow, one bot).

downloadFilesFromChannelMulThread → Multi-threaded (fast, uses 4 bots).


▶️ Running the Project

Configure Postgres DB and run the discord_backup.sql script.

Add your Discord bot token(s) in application.properties.

Run the application (e.g., via Maven/Gradle or your IDE).

Use the frontend application(ReactJS) to First Create channels and then submit the Upload and Download Requests.

API Config is in discord-backup-frontend/src/api/axiosConfig.ts (change the port accordingly if you change it in application.properties)

Whitelist the URL of your react app in discord-backup-backend\src\main\java\com\discord\backup\discord_backup_backend\config\CorsConfig.java

📌 Notes

At least 1 bot token is required to run the project.
For faster performance, configure 4 bots and use multi-threaded methods.


👨‍💻 Author

For setup issues or questions, DM on Instagram: @kunal.kandhari
