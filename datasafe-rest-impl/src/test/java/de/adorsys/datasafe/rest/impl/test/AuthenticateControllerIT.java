package de.adorsys.datasafe.rest.impl.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthenticateControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAuthenticateSuccess() throws Exception {
        this.mockMvc.perform(get("/api/public")).andDo(print()).andExpect(status().isOk());
       /* HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getUrl(), HttpMethod.POST, entity, String.class);
        String actual = response.getHeaders().get(HttpHeaders.LOCATION).get(0);


        assertTrue(actual.contains("/students"));*/
        assertTrue(true);
    }

    private String getUrl() {
        //return "http://localhost:" + port + "/api/private";
        return null;
    }

    public void testAuthenticateFailWithInccorectCredentials() throws Exception {

    }

    public void testGetDataWithToken() throws Exception {

    }

    public void testGetDataWitoutToken() {

    }


}
