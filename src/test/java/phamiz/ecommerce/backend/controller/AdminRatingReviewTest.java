package phamiz.ecommerce.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import phamiz.ecommerce.backend.service.PayOSService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "PAYOS_CLIENT_ID=test_client_id",
        "PAYOS_API_KEY=test_api_key",
        "PAYOS_CHECKSUM_KEY=test_checksum_key"
})
@AutoConfigureMockMvc
public class AdminRatingReviewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayOSService payOSService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testGetAllRatings() throws Exception {
        var result = mockMvc.perform(get("/api/admin/ratings")
                .param("page", "0")
                .param("size", "10"))
                .andReturn();
        System.out.println("STATUS: " + result.getResponse().getStatus());
        System.out.println("BODY: " + result.getResponse().getContentAsString());
        if (result.getResponse().getStatus() != 200) {
            throw new RuntimeException("Failed with status: " + result.getResponse().getStatus());
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testGetAllReviews() throws Exception {
        var result = mockMvc.perform(get("/api/admin/reviews")
                .param("page", "0")
                .param("size", "10"))
                .andReturn();
        System.out.println("STATUS: " + result.getResponse().getStatus());
        System.out.println("BODY: " + result.getResponse().getContentAsString());
        if (result.getResponse().getStatus() != 200) {
            throw new RuntimeException("Failed with status: " + result.getResponse().getStatus());
        }
    }
}
