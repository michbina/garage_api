package com.garage.storage;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorage {

	String uploadFile(MultipartFile file, DocumentCategory category) throws Exception;

	InputStream downloadFile(DocumentCategory category, String storageName) throws Exception;

	void delete(DocumentCategory category, String storageName) throws Exception;

}
