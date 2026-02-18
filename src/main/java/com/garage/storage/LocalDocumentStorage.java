package com.garage.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

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
	        return Paths.get(rootPath).toAbsolutePath().normalize();
	    }
	 
	 private Path rootDevis() {
	        return Paths.get(rootPathDevis).toAbsolutePath().normalize();
	    }
	 
	 private Path rootFactures() {
	        return Paths.get(rootPathFactures).toAbsolutePath().normalize();
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
	
	@Override
	public Resource load(String folder, String filename) throws Exception {

		 if (filename.contains(".."))
	            throw new RuntimeException("Nom de fichier invalide");

	        Path file = root()
	                .resolve(folder)
	                .resolve(filename)
	                .normalize();

	        if (!file.startsWith(root()))
	            throw new RuntimeException("Accès interdit");

	        Resource resource = new UrlResource(file.toUri());

	        if (!resource.exists() || !resource.isReadable())
	            throw new RuntimeException("Fichier introuvable");

	        return resource;
    }


    @Override
    public void delete(String path) throws Exception {
        Path file = Paths.get(rootPath).resolve(path);
        Files.deleteIfExists(file);
    }
    


}
