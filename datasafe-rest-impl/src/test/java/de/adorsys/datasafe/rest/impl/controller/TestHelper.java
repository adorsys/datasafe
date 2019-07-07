package de.adorsys.datasafe.rest.impl.controller;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@UtilityClass
public class TestHelper {

    public static MockHttpServletRequestBuilder putFileBuilder(String path, Object vars) {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "file.txt",
            "text/plain",
            "DATA".getBytes()
        );

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(path, vars).file(file);
        return builder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });
    }
}
