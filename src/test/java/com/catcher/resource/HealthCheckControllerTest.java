package com.catcher.resource;

import com.catcher.app.AppApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppApplication.class)
@AutoConfigureMockMvc
class HealthCheckControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new HealthCheckController()).build();
    }

    @DisplayName("Health check 검사가 성공적으로 반환된다")
    @Test
    void health_check_and_receive_ok() throws Exception {
        //given

        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/health")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //Then
        resultActions.andExpect(status().isOk());
    }
}