package com.garage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageProperties {

	@Value("${storage.type}")
	private String storageType;

	@Value("${app.storage.devis}")
	private String localDevis;

	@Value("${app.storage.factures}")
	private String localFactures;

	@Value("${s3.bucket.devis}")
	private String s3Devis;

	@Value("${s3.bucket.factures}")
	private String s3Factures;

	public String getStorageName(String type) {
		if ("s3".equalsIgnoreCase(storageType)) {
			return "devis".equals(type) ? s3Devis : s3Factures;
		} else {
			return "devis".equals(type) ? localDevis : localFactures;
		}
	}

}
