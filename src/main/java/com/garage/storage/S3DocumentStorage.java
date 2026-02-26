package com.garage.storage;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3DocumentStorage implements DocumentStorage {

	private final S3Client s3Client;

	@Value("${s3.bucket.devis}")
	private String bucketDevis;

	@Value("${s3.bucket.factures}")
	private String bucketFactures;

	public S3DocumentStorage(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	@Override
	public String uploadFile(MultipartFile file, DocumentCategory category) throws Exception {
		try {
			String original = StringUtils.cleanPath(file.getOriginalFilename());
			String ext = original.substring(original.lastIndexOf("."));
			String storageName = UUID.randomUUID().toString() + ext;
			String key = category.getFolder() + "/" + storageName;
			PutObjectRequest req = PutObjectRequest.builder()
					.bucket(category == DocumentCategory.DEVIS ? bucketDevis : bucketFactures).key(key).build();
			s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
			return storageName;
		} catch (Exception e) {
			throw new StorageException("Échec du stockage S3 lors de l'upload", e);
		}
	}

	@Override
	public InputStream downloadFile(DocumentCategory category, String storageName) {
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(category == DocumentCategory.DEVIS ? bucketDevis : bucketFactures).key(storageName).build();
			return s3Client.getObject(getObjectRequest);
		} catch (S3Exception e) {
			throw new StorageException("Fichier non trouvé sur S3: " + category + "/" + storageName, e);
		} catch (SdkException e) {
			throw new StorageException("Erreur réseau/S3 lors du téléchargement", e);
		} catch (Exception e) {
			throw new StorageException("Erreur inattendue lors du téléchargement S3", e);
		}
	}

	@Override
	public void delete(DocumentCategory category, String storageName) throws Exception {
		try {
			String key = category.getFolder() + "/" + storageName;
			String bucketName = (category == DocumentCategory.DEVIS) ? bucketDevis : bucketFactures;
			DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucketName).key(storageName).build();
			s3Client.deleteObject(req);
		} catch (NoSuchKeyException e) {
			// fichier déjà absent
			throw new StorageException("Échec de la suppression S3", e);
		} catch (SdkException e) {
			throw new StorageException("Échec de la suppression S3", e);
		} catch (Exception e) {
			throw new StorageException("Erreur inattendue lors de la suppression S3", e);
		}

	}

}
