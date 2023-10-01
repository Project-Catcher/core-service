package com.catcher.resource;


import com.catcher.app.AppApplication;
import com.catcher.core.UserCommandExecutor;
import com.catcher.core.datasource.UserRepository;
import com.catcher.core.domain.command.UserByUserIdCommand;
import com.catcher.core.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppApplication.class)
@AutoConfigureMockMvc
public class UserApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserCommandExecutor userCommandExecutor;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userCommandExecutor)).build();
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void testGetUserByUserId() throws Exception {
        // given
        String userId = generateRandomString();
        String password = generateRandomString();
        String name = generateRandomString();
        User user = new User(userId, password, name);

        //when
        when(userCommandExecutor.run(new UserByUserIdCommand(userId)))
                .thenReturn(user);

        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/user/" + user.getUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.password").value(password))
                .andExpect(jsonPath("$.name").value(name));
    }
}