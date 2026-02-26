package com.garage.strorage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.garage.storage.DocumentCategory;
import com.garage.storage.S3DocumentStorage;

import software.amazon.awssdk.services.s3.S3Client;

public class S3DocumentStorageTest {

	private S3DocumentStorage storage;
	private S3Client mockS3;

	@BeforeEach
	void setup() {
		mockS3 = mock(S3Client.class);
		storage = new S3DocumentStorage(mockS3);
		// Injection possible des valeurs de bucket via reflection si nécessaire
	}

	@Test
	void testUploadReturnsName() throws Exception {
		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getOriginalFilename()).thenReturn("file.txt");
		when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("data".getBytes()));
		when(mockFile.getSize()).thenReturn((long) "data".length());

		// Pas d'exception attendu
		String name = storage.uploadFile(mockFile, DocumentCategory.DEVIS);
		assertNotNull(name);
	}

	// Ajoute d'autres tests rapidement (downloadFile, delete) en simulant le
	// comportement du S3Client

}
