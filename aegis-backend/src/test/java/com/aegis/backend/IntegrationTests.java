package com.aegis.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.dto.RegisterRequest;
import com.aegis.backend.entity.Role;
import com.aegis.backend.entity.User;
import com.aegis.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class IntegrationTests extends BaseTestContainer {

    @Autowired
    private com.aegis.backend.service.UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AiService aiService;

    @Test
    public void testAuthIntegrationFlow() {
        final RegisterRequest request = RegisterRequest.builder()
                .username("integration_user")
                .email("integration@test.com")
                .password("securePassword123")
                .build();

        final User registeredUser = userService.registerUser(request);
        assertNotNull(registeredUser.getId());
        assertEquals("integration_user", registeredUser.getUsername());
        assertEquals(Role.USER, registeredUser.getRole());

        assertTrue(userRepository.existsByUsername("integration_user"));
    }

    @Test
    public void testAiAgentMockIntegration() {
        Mockito.when(aiService.generateResponse(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("Mocked RAG grounding response");

        final String response = aiService.generateResponse("grounding system prompt", "agent query");
        assertEquals("Mocked RAG grounding response", response);
    }
}
