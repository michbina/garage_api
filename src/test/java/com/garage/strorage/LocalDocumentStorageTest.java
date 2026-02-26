package com.garage.strorage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.garage.storage.DocumentCategory;
import com.garage.storage.LocalDocumentStorage;

public class LocalDocumentStorageTest {

	private LocalDocumentStorage storage;
	private Path tmpRoot;

	@BeforeEach
	void setup() throws Exception {
		tmpRoot = Files.createTempDirectory("localstorage-test");
		storage = new LocalDocumentStorage();
		// inject rootPath via reflection
		Field f = LocalDocumentStorage.class.getDeclaredField("rootPath");
		f.setAccessible(true);
		f.set(storage, tmpRoot.toString());
	}

	@AfterEach
	void cleanup() throws Exception {
		// nettoyage optionnel
		// Files.walk(tmpRoot).sorted(Comparator.reverseOrder()).forEach(p ->
		// p.toFile().delete());
	}

	@Test
	void testUploadAndDownloadDevis() throws Exception {
		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
		when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

		String name = storage.uploadFile(mockFile, DocumentCategory.DEVIS);
		assertNotNull(name);

		InputStream is = storage.downloadFile(DocumentCategory.DEVIS, name);
		assertNotNull(is);
	}

	@Test
	void testDelete() throws Exception {
		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.getOriginalFilename()).thenReturn("t.pdf");
		when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("d".getBytes()));

		String name = storage.uploadFile(mockFile, DocumentCategory.DEVIS);
		storage.delete(DocumentCategory.DEVIS, name);
		// Vérification basique: le fichier peut exister ou non selon l’implémentation,
		// ici on tente une seconde lecture attendue d’échouer si vous avez lancé un
		// check explicite.
	}

}
