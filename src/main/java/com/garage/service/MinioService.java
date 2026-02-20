package com.garage.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

@Service
public class MinioService {

	private final MinioClient minioClient;

	@Value("${minio.bucket.devis}")
	private String devisBucket;

	@Value("${minio.bucket.factures}")
	private String facturesBucket;

	public MinioService(@Value("${minio.url}") String url, @Value("${minio.access-key}") String accessKey,
			@Value("${minio.secret-key}") String secretKey) {
		this.minioClient = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
	}

	// upload d'un devis
	public void uploadDevis(String storageName, InputStream stream, long size, String contentType) throws Exception {
		minioClient.putObject(PutObjectArgs.builder().bucket(devisBucket).object(storageName).stream(stream, size, -1)
				.contentType(contentType).build());
	}

	// Téléchargement d’un devis
	public InputStream downloadDevis(String storageName) throws Exception {
		return minioClient.getObject(GetObjectArgs.builder().bucket(devisBucket).object(storageName).build());
	}

	// Suppression d’un devis
	public void deleteDevis(String storageName) throws Exception {
		minioClient.removeObject(RemoveObjectArgs.builder().bucket(devisBucket).object(storageName).build());
	}

	public void uploadFactures(String storageName, InputStream stream, long size, String contentType) throws Exception {
		minioClient.putObject(PutObjectArgs.builder().bucket(devisBucket).object(storageName).stream(stream, size, -1)
				.contentType(contentType).build());
	}

	// Téléchargement d’une facture
	public InputStream downloadFacture(String storageName) throws Exception {
		return minioClient.getObject(GetObjectArgs.builder().bucket(facturesBucket).object(storageName).build());
	}

	// Suppression d’une facture
	public void deleteFacture(String storageName) throws Exception {
		minioClient.removeObject(RemoveObjectArgs.builder().bucket(facturesBucket).object(storageName).build());
	}

}
