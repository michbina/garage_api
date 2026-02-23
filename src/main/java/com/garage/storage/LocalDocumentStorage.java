package com.garage.storage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.garage.service.S3Service;

@Service
@Primary
public class LocalDocumentStorage implements DocumentStorage {

	@Value("${app.storage.path}")
	private String rootPath;

	@Value("${app.storage.devis}")
	private String rootPathDevis;

	@Value("${app.storage.factures}")
	private String rootPathFactures;

	private Path root() {
		return Path.of(rootPath).toAbsolutePath().normalize();
	}
	
	private S3Service s3Service;
	
	private LocalDocumentStorage(S3Service s3Service) {
        this.s3Service = s3Service;
    }

	@Override
	public String save(MultipartFile file, String folder) throws Exception {

		String original = StringUtils.cleanPath(file.getOriginalFilename());
		String ext = original.substring(original.lastIndexOf("."));

		String storageName = UUID.randomUUID().toString() + ext;

		Path dir = root().resolve(folder);
		Files.createDirectories(dir);

		Path target = dir.resolve(storageName);
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		return storageName;
	}
	
	
	public String saveToS3(MultipartFile file, String folder,String bucketName) throws Exception {
	    String original = StringUtils.cleanPath(file.getOriginalFilename());
	    String ext = original.substring(original.lastIndexOf("."));
	    String storageName = UUID.randomUUID().toString() + ext;

	    // Clé S3&nbsp;: dossier/nom
	    String key = folder + "/" + storageName;

	    // Upload direct sur S3
	    s3Service.uploadFile(bucketName, key, file.getInputStream(), file.getSize());

	    return storageName;
	}
	
	public ResponseEntity<Resource> downloadFileFromS3(String bucketName,String fileName) {
	    InputStream inputStream = s3Service.downloadFileAsStream(bucketName, fileName);
	    InputStreamResource resource = new InputStreamResource(inputStream);

	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	        .contentType(MediaType.APPLICATION_OCTET_STREAM)
	        .body(resource);
	}

	@Override
	public Resource load(String folder, String filename) throws Exception {

		if (filename.contains(".."))
			throw new RuntimeException("Nom de fichier invalide");

		Path file = root().resolve(folder).resolve(filename).normalize();

		if (!file.startsWith(root()))
			throw new RuntimeException("Accès interdit");

		Resource resource = new UrlResource(file.toUri());

		if (!resource.exists() || !resource.isReadable())
			throw new RuntimeException("Fichier introuvable");

		return resource;
	}

	@Override
	public void delete(String path) throws Exception {
		Path file = Path.of(rootPath).resolve(path);
		Files.deleteIfExists(file);
	}

}
