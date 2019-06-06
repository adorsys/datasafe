package de.adorsys.datasafe.rest.impl.controller;

import com.amazonaws.services.s3.AmazonS3;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseDatasafeEndpointTest extends BaseMockitoTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected AmazonS3 s3;
}
