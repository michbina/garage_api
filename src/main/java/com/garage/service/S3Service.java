package com.garage.service;

import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	private final S3Client s3Client;

	@Value("s3://[S3_BUCKET_NAME]/[FILE_NAME]")
	private Resource s3Resource;

	public S3Service(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	public void uploadFile(String bucketName, String key, InputStream inputStream, long contentLength) throws Exception {
	    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
	            .bucket(bucketName)
	            .key(key)
	            .build();
	    s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, contentLength));
	}
	
	public InputStream downloadFileAsStream(String bucketName, String key) {
	    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
	        .bucket(bucketName)
	        .key(key)
	        .build();
	    return s3Client.getObject(getObjectRequest);
	}

}
