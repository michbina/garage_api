package com.garage.storage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalDocumentStorage implements DocumentStorage {

	@Value("${app.storage.path}")
	private String rootPath;

	private Path root() {
		return Path.of(rootPath).toAbsolutePath().normalize();
	}
	
	/**
	 * Retourne le chemin racine absolu utilisé par ce service de stockage.
	 * Nécessaire pour la configuration MVC qui doit servir les fichiers.
	 * @return Le chemin racine du stockage.
	 */
	public Path getRootLocation() {
		return root();
	}

	@Override
	public String uploadFile(MultipartFile file, DocumentCategory category) throws Exception {
		try {
			String original = StringUtils.cleanPath(file.getOriginalFilename());
			String ext = original.substring(original.lastIndexOf("."));

			String storageName = UUID.randomUUID().toString() + ext;

			Path dir = root().resolve(category.getFolder());
			Files.createDirectories(dir);

			Path target = dir.resolve(storageName);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

			return storageName;
		} catch (Exception e) {
			throw new StorageException("Échec du stockage local lors de l'upload", e);
		}
	}

	@Override
	public InputStream downloadFile(DocumentCategory category, String storageName) throws Exception {
		try {
			if (storageName.contains(".."))
				throw new RuntimeException("Nom de fichier invalide");

			Path file = root().resolve(category.getFolder()).resolve(storageName).normalize();

			if (!file.startsWith(root()))
				throw new RuntimeException("Accès interdit");

			Resource resource = new UrlResource(file.toUri());

			if (!resource.exists() || !resource.isReadable())
				throw new RuntimeException("Fichier introuvable");

			return resource.getInputStream();
		} catch (Exception e) {
			throw new StorageException("Erreur inattendue lors du téléchargement local", e);
		}
	}

	@Override
	public void delete(DocumentCategory category, String filename) throws Exception {
		try {
			if (filename.contains(".."))
				throw new RuntimeException("Nom de fichier invalide");

			Path file = root().resolve(category.getFolder()).resolve(filename).normalize();

			if (!file.startsWith(root()))
				throw new RuntimeException("Accès interdit");

			Files.deleteIfExists(file);
		} catch (Exception e) {
			throw new StorageException("Erreur inattendue lors de la suppression local", e);
		}
	}

}
