package com.fitconnect.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject; // Not needed if using constructor injection for config
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

@ApplicationScoped
public class FirebaseStorageService {

    private static final Logger LOG = Logger.getLogger(FirebaseStorageService.class);

    private final String bucketName;
    private Storage storage;

    // Using constructor injection for ConfigProperty
    public FirebaseStorageService(@ConfigProperty(name = "firebase.storage.bucket-name") String bucketName) {
        this.bucketName = bucketName;
        if ("your-default-bucket".equals(this.bucketName) || this.bucketName == null || this.bucketName.trim().isEmpty()) {
            LOG.warn("Firebase Storage bucket name ('firebase.storage.bucket-name') is not properly configured or is using the default placeholder. File uploads will FAIL.");
            this.storage = null; // Ensure storage client is null if not configured
        } else {
            try {
                // Ensure FirebaseApp is initialized before this service is constructed,
                // typically via FirebaseInitializer, which should run on startup.
                this.storage = StorageClient.getInstance().bucket(this.bucketName).getStorage();
                LOG.infof("FirebaseStorageService initialized for bucket: %s", this.bucketName);
            } catch (Exception e) {
                // This catch block is important. If FirebaseApp is not initialized (e.g. missing service account file in prod),
                // StorageClient.getInstance() will throw an IllegalStateException.
                LOG.errorf(e, "Failed to initialize Firebase Storage client for bucket: %s. FirebaseApp might not be ready (service account missing or invalid?) or bucket name is invalid.", this.bucketName);
                this.storage = null; // Ensure storage client is null on error
            }
        }
    }

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param fileUpload The file to upload.
     * @param destinationDirectory The directory within the bucket (e.g., "professional-documents").
     * @return The gs:// path of the uploaded file.
     * @throws IOException If an error occurs during upload or if storage is not configured.
     */
    public String uploadFile(FileUpload fileUpload, String destinationDirectory) throws IOException {
        if (this.storage == null) {
            LOG.error("Firebase Storage client is not initialized. Cannot upload file. Check configuration and Firebase initialization.");
            throw new IOException("Firebase Storage not available. Ensure 'firebase.storage.bucket-name' is configured and FirebaseApp is initialized correctly.");
        }
        // Redundant check if constructor logic is robust, but good for safety:
        if ("your-default-bucket".equals(this.bucketName) || this.bucketName == null || this.bucketName.trim().isEmpty()){
             LOG.error("Firebase Storage bucket name is not configured. Cannot upload file.");
            throw new IOException("Firebase Storage bucket name not configured.");
        }

        String originalFileName = fileUpload.fileName();
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        // Generate a unique file name to prevent collisions
        String uniqueFileName = destinationDirectory + "/" + UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, uniqueFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                                .setContentType(fileUpload.contentType())
                                .build();

        LOG.infof("Attempting to upload file %s to gs://%s/%s", originalFileName, bucketName, uniqueFileName);

        try (InputStream inputStream = Files.newInputStream(fileUpload.uploadedFile())) {
            storage.create(blobInfo, inputStream);
            LOG.infof("Successfully uploaded %s to gs://%s/%s", originalFileName, bucketName, uniqueFileName);
            return String.format("gs://%s/%s", bucketName, uniqueFileName);
        } catch (IOException e) {
            LOG.errorf(e, "Failed to upload file %s to Firebase Storage at gs://%s/%s", originalFileName, bucketName, uniqueFileName);
            throw e;
        }
    }
}
