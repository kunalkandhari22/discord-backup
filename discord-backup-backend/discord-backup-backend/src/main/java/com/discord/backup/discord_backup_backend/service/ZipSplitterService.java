package com.discord.backup.discord_backup_backend.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipSplitterService {
    private static final Pattern PART_FILE_PATTERN = Pattern.compile("(.+)\\.part(\\d{3})$");

    public Path convertFolderToZip(Path sourcePath) throws Exception {

        if(!Files.isDirectory(sourcePath))
            throw new Exception("Invalid Input Directory!!!");

        Path zipPath = Path.of(sourcePath.toFile().toString().replace(" ", "_") + ".zip");

        if(Files.exists((zipPath))) {
            Files.deleteIfExists(zipPath);
        }

        Files.createFile(zipPath);

        try(FileOutputStream os = new FileOutputStream(zipPath.toFile());
            ZipOutputStream zos = new ZipOutputStream(os)) {

            String baseEntryPath = sourcePath.getFileName().toString();

            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String entryName = baseEntryPath + File.separator + sourcePath.relativize(path).toString();
                        entryName = entryName.replace("\\", "/");

                        try {
                            System.out.println("Adding entry: " + entryName);
                            zos.putNextEntry(new ZipEntry(entryName));

                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
                                byte[] bytes = new byte[8192];
                                int length;
                                while ((length = bis.read(bytes)) != -1) {
                                    zos.write(bytes, 0, length);
                                }
                            }
                            zos.closeEntry();
                        } catch (IOException e) {
                            // Log error for this specific file but try to continue zipping others
                            System.err.println("Error adding file to zip: " + path.toAbsolutePath() + " - " + e.getMessage());
                            throw new RuntimeException("Failed to add file to zip: " + path.toAbsolutePath(), e);
                        }
                    });

            return zipPath;
        }
    }

    public int splitZipFileIntoChunks(Path inputFile, Path outputDir, int chunkSize) throws Exception {
        if(!Files.exists(inputFile) || !Files.isRegularFile(inputFile)) {
            throw new Exception("Invalid File.");
        }

        if(chunkSize <= 0)
            throw new Exception("Invalid Chunk Size.");

        if(Files.isDirectory(outputDir)) {
            try (Stream<Path> walk = Files.walk(outputDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        Files.createDirectories(outputDir);

        byte[] buffer = new byte[8192];
        int bytesRead, partNumber = 1;
        int currentChunkSize = 0;

        OutputStream currentOutputStream = null;

        String originalFileName = inputFile.getFileName().toString();

        try(InputStream inputStream = Files.newInputStream(inputFile)) {
            while((bytesRead = inputStream.read(buffer)) != -1) {
                if(currentChunkSize >= chunkSize || currentOutputStream == null) {
                    if(currentOutputStream != null)
                        currentOutputStream.close();

                    Path partFile = outputDir.resolve(originalFileName + ".part" + String.format("%03d", partNumber));
                    currentOutputStream = Files.newOutputStream(partFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    currentChunkSize = 0;
                    partNumber++;
                }
                currentOutputStream.write(buffer, 0, bytesRead);
                currentChunkSize += bytesRead;
                System.out.println(partNumber);
            }
        }
        finally {
            if(currentOutputStream != null)
                currentOutputStream.close();
        }

        return partNumber - 1;
    }

    public void combineChunksIntoFile(Path inputDir, String fileName, Path outputDir) throws Exception {
        if(!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
            throw new Exception("Invalid Input File.");
        }
        List<Path> partFiles;

        try(Stream<Path> files = Files.list(inputDir)) {
            partFiles = files
                    .filter(p -> p.getFileName().toString().startsWith(fileName + ".part"))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingInt(this::partNumber))
                    .toList();
        }

        if(partFiles.isEmpty())
            throw new Exception("Invalid input directory(No file found).");

        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve(fileName);

        byte[] buffer = new byte[8192];
        int bytesRead;

        try(OutputStream outputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for(Path partFile: partFiles) {
                try(InputStream inputStream = Files.newInputStream(partFile)) {
                    while((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    private int partNumber(Path partFile) {
        Matcher matcher = PART_FILE_PATTERN.matcher(partFile.getFileName().toString());
        if(matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }
        return Integer.MAX_VALUE;
    }

}
