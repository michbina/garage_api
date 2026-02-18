package com.garage.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorage {
	
	String save(MultipartFile file, String folder) throws Exception;

    Resource load(String folder, String filename) throws Exception;

    void delete(String path) throws Exception;

}
