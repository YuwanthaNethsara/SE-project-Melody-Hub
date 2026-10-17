package com.example.project1.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The ONLY class that reads or writes uploaded files.
 *
 * <ul>
 *   <li>Every stored file gets a fresh random name (UUID + safe extension), so a user-supplied
 *       name can never overwrite another file or escape the upload folder.</li>
 *   <li>Type checks use the extension, the declared content type AND the first bytes of the file
 *       (a renamed .exe is rejected).</li>
 *   <li>Every path is normalised and must stay inside the upload root.</li>
 * </ul>
 *
 * Public images are served from {@code /uploads/<folder>/...} (see UploadResourceConfig).
 * Music files live in {@code <root>/music} and are NEVER served publicly - only through the
 * authenticated download endpoint.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    /** Folders that hold public images. Anything else under the root is private. */
    public static final String PRODUCTS = "products";
    public static final String ADVERTISEMENTS = "advertisements";
    public static final String ARTISTS = "artists";
    public static final String ALBUMS = "albums";
    public static final Set<String> IMAGE_FOLDERS = Set.of(PRODUCTS, ADVERTISEMENTS, ARTISTS, ALBUMS);

    public static final String MUSIC = "music";

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "jpg", "jpg", "jpeg", "jpg", "png", "png", "webp", "webp");
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/pjpeg", "image/png", "image/webp");
    private static final Set<String> AUDIO_MIME_TYPES = Set.of(
            "audio/mpeg", "audio/mp3", "audio/mpeg3", "audio/x-mpeg", "audio/x-mpeg-3",
            "application/octet-stream");

    private final Path root;
    private final long maxImageBytes;
    private final long maxArtistImageBytes;
    private final long maxAudioBytes;

    public FileStorageService(
            @Value("${melodyhub.upload.dir:uploads}") String uploadDir,
            @Value("${melodyhub.upload.max-image-mb:5}") long maxImageMb,
            @Value("${melodyhub.upload.max-artist-image-mb:2}") long maxArtistImageMb,
            @Value("${melodyhub.upload.max-audio-mb:30}") long maxAudioMb) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxImageBytes = maxImageMb * 1024L * 1024L;
        this.maxArtistImageBytes = maxArtistImageMb * 1024L * 1024L;
        this.maxAudioBytes = maxAudioMb * 1024L * 1024L;
    }

    public Path getRoot() {
        return root;
    }

    public long getMaxImageMb() {
        return maxImageBytes / (1024L * 1024L);
    }

    public long getMaxAudioMb() {
        return maxAudioBytes / (1024L * 1024L);
    }

    /** Size limit for one image folder: artist photos have their own (smaller) limit. */
    public long maxImageBytesFor(String folder) {
        return ARTISTS.equals(folder) ? maxArtistImageBytes : maxImageBytes;
    }

    // =====================================================================
    // images
    // =====================================================================

    /**
     * Validates and stores an image. Returns the public URL, e.g. /uploads/products/3f2c....png
     */
    public String storeImage(MultipartFile file, String folder) {
        if (!IMAGE_FOLDERS.contains(folder)) {
            throw new IllegalArgumentException("Unknown image folder: " + folder);
        }
        if (file == null || file.isEmpty()) {
            throw badRequest("Please choose an image to upload.");
        }
        long limit = maxImageBytesFor(folder);
        if (file.getSize() > limit) {
            throw badRequest((ARTISTS.equals(folder) ? "The artist photo" : "The image")
                    + " must be " + (limit / (1024L * 1024L)) + " MB or smaller.");
        }
        String extension = IMAGE_EXTENSIONS.get(extensionOf(file.getOriginalFilename()));
        if (extension == null) {
            throw badRequest("Only JPG, JPEG, PNG or WEBP images are allowed.");
        }
        String contentType = normalisedContentType(file);
        if (!contentType.isEmpty() && !IMAGE_MIME_TYPES.contains(contentType)) {
            throw badRequest("The selected file is not a supported image.");
        }
        byte[] head = readHead(file, 16);
        String detected = detectImageType(head);
        if (detected == null) {
            throw badRequest("The selected file is not a valid JPG, JPEG, PNG or WEBP image.");
        }
        // trust the real content over the extension
        String fileName = UUID.randomUUID() + "." + detected;
        write(file, folder, fileName);
        return "/uploads/" + folder + "/" + fileName;
    }

    /**
     * Replaces the picture of a record without leaving orphan files.
     * <ol>
     *   <li>a new upload is stored first (invalid files are rejected before anything changes);</li>
     *   <li>{@code saver} receives the URL to keep (new, old, or null when removed) and saves the record;</li>
     *   <li>only after a successful save is the previous file deleted. If the save fails, the new
     *       file is deleted and the old one is kept.</li>
     * </ol>
     */
    public <T> T replaceImage(String oldUrl, MultipartFile image, boolean remove, String folder,
                              java.util.function.Function<String, T> saver) {
        boolean uploaded = image != null && !image.isEmpty();
        String newUrl = uploaded ? storeImage(image, folder) : (remove ? null : oldUrl);
        T result;
        try {
            result = saver.apply(newUrl);
        } catch (RuntimeException e) {
            if (uploaded) {
                deleteImageQuietly(newUrl);
            }
            throw e;
        }
        if (!java.util.Objects.equals(oldUrl, newUrl)) {
            deleteImageQuietly(oldUrl);
        }
        return result;
    }

    /** Validates an image URL sent through the JSON endpoints: an uploaded image or an http(s) link. */
    public String validateImageUrl(String imageUrl, int maxLength) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String url = imageUrl.trim();
        boolean web = url.startsWith("http://") || url.startsWith("https://");
        if (url.length() > maxLength || !(web || isManagedImageUrl(url))) {
            throw badRequest("Image URL must be an uploaded image or a valid web address (http or https).");
        }
        return url;
    }

    /**
     * Decides which image a JSON (non-multipart) save keeps, so JSON clients cannot lose or steal pictures:
     * <ul>
     *   <li>{@code remove = true} or a blank value removes the image;</li>
     *   <li>no value at all ({@code null}) keeps the current image;</li>
     *   <li>the current URL is kept as it is;</li>
     *   <li>another stored upload is refused - files are attached by uploading them, never by pointing at
     *       a file that belongs to a different record (it would be deleted when that record changes);</li>
     *   <li>an http(s) link is accepted.</li>
     * </ul>
     */
    public String resolveJsonImageUrl(String currentUrl, String requestedUrl, boolean remove, int maxLength) {
        if (remove) {
            return null;
        }
        if (requestedUrl == null) {
            return currentUrl;
        }
        String url = requestedUrl.trim();
        if (url.isEmpty()) {
            return null;
        }
        if (url.equals(currentUrl)) {
            return currentUrl;
        }
        if (url.startsWith("/uploads/")) {
            throw badRequest("Please upload the image with the image field instead of typing its address.");
        }
        return validateImageUrl(url, maxLength);
    }

    /** True for URLs that point at a file this service stored (as opposed to an external https:// link). */
    public boolean isManagedImageUrl(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return false;
        }
        String[] parts = url.substring("/uploads/".length()).split("/");
        return parts.length == 2 && IMAGE_FOLDERS.contains(parts[0]) && isSafeFileName(parts[1]);
    }

    /** Deletes a stored image by its public URL. External URLs and unknown files are ignored. */
    public void deleteImageQuietly(String url) {
        if (!isManagedImageUrl(url)) {
            return;
        }
        String[] parts = url.substring("/uploads/".length()).split("/");
        deleteQuietly(root.resolve(parts[0]).resolve(parts[1]));
    }

    // =====================================================================
    // audio (MP3)
    // =====================================================================

    /** Result of storing an MP3: where it lives on disk (relative to the working directory or absolute). */
    public record StoredAudio(String storedPath, String storedName, long size) {
    }

    public StoredAudio storeMp3(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Please choose an MP3 file to upload.");
        }
        if (file.getSize() > maxAudioBytes) {
            throw badRequest("The MP3 file must be " + getMaxAudioMb() + " MB or smaller.");
        }
        if (!"mp3".equals(extensionOf(file.getOriginalFilename()))) {
            throw badRequest("Only MP3 files are allowed.");
        }
        String contentType = normalisedContentType(file);
        if (!contentType.isEmpty() && !AUDIO_MIME_TYPES.contains(contentType)) {
            throw badRequest("The selected file is not an MP3 audio file.");
        }
        if (!looksLikeMp3(readHead(file, 4))) {
            throw badRequest("The selected file is not a valid MP3 audio file.");
        }
        String fileName = UUID.randomUUID() + ".mp3";
        Path target = write(file, MUSIC, fileName);
        // keep the same format as existing rows ("uploads/music/x.mp3", relative to the working
        // directory) so the project folder can be moved without breaking stored paths
        Path workingDir = Paths.get("").toAbsolutePath().normalize();
        String stored = target.startsWith(workingDir) ? workingDir.relativize(target).toString() : target.toString();
        return new StoredAudio(stored, fileName, file.getSize());
    }

    /**
     * Resolves a stored music path (as saved in digital_music.file_path) and checks that it is a
     * regular file inside the upload root. Returns null when the file is missing.
     */
    public Path resolveStoredFile(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        Path path;
        try {
            path = Paths.get(storedPath).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return null;
        }
        if (!path.startsWith(root)) {
            log.warn("Refusing to access a file outside the upload folder: {}", storedPath);
            return null;
        }
        return Files.isRegularFile(path) ? path : null;
    }

    public void deleteStoredFileQuietly(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(storedPath).toAbsolutePath().normalize();
            if (path.startsWith(root)) {
                deleteQuietly(path);
            }
        } catch (InvalidPathException ignored) {
            // nothing we could delete
        }
    }

    // =====================================================================
    // helpers
    // =====================================================================

    private Path write(MultipartFile file, String folder, String fileName) {
        Path directory = root.resolve(folder).normalize();
        Path target = directory.resolve(fileName).normalize();
        if (!target.getParent().equals(directory) || !directory.startsWith(root)) {
            throw badRequest("Invalid file name.");
        }
        try {
            Files.createDirectories(directory);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException e) {
            deleteQuietly(target);
            log.error("Could not store uploaded file in {}: {}", directory, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "The file could not be saved on the server. Please try again.");
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", path, e.getMessage());
        }
    }

    private static boolean isSafeFileName(String name) {
        return name.matches("[A-Za-z0-9._-]{1,120}") && !name.contains("..");
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        String name = originalName.trim().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }

    private static String normalisedContentType(MultipartFile file) {
        String type = file.getContentType();
        if (type == null) {
            return "";
        }
        int semicolon = type.indexOf(';');
        return (semicolon >= 0 ? type.substring(0, semicolon) : type).trim().toLowerCase(Locale.ROOT);
    }

    private static byte[] readHead(MultipartFile file, int length) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(length);
        } catch (IOException e) {
            throw badRequest("The uploaded file could not be read.");
        }
    }

    /** jpg / png / webp from the file signature, or null. */
    static String detectImageType(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return "png";
        }
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /** "ID3" tag or an MPEG audio frame sync (11 set bits) at the start of the file. */
    static boolean looksLikeMp3(byte[] h) {
        if (h.length >= 3 && h[0] == 'I' && h[1] == 'D' && h[2] == '3') {
            return true;
        }
        return h.length >= 2 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xE0) == 0xE0;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
