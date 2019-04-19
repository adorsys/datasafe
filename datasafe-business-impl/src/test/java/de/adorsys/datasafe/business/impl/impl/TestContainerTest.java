package de.adorsys.datasafe.business.impl.impl;

import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Testcontainers
public class TestContainerTest {

    @Test
    @SneakyThrows
    public void uploadFile() {

        String dockerComposePath = "src/test/resources/docker-compose.yml";
        DockerComposeContainer compose = new DockerComposeContainer(new File(dockerComposePath))
                .withExposedService("minio", 9000);
        compose.start();

        String endpoint = "http://127.0.0.1:9000";
        MinioClient minioClient = new MinioClient(endpoint, "admin", "password");

        String bucketName = "home";

        boolean isExist = minioClient.bucketExists(bucketName);
        if (isExist) {
            System.out.println("Bucket already exists");
        } else {
            minioClient.makeBucket(bucketName);
        }

        // Put file
        File file = new File(dockerComposePath);
        System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        InputStream os = FileUtils.openInputStream(file);
        minioClient.putObject(bucketName, "docker-compose.yml", os, "");

        System.out.println("successfully uploaded");

        // Get file
        InputStream is = minioClient.getObject(bucketName, "docker-compose.yml");

        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8);
        System.out.println(writer.toString());

        Assert.assertTrue(IOUtils.contentEquals(os, is));
    }

}
