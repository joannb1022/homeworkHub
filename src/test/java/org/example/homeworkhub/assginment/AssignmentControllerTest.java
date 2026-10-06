package org.example.homeworkhub.assginment;

import org.example.homeworkhub.assignment.controller.AssignmentController;
import org.example.homeworkhub.assignment.dto.AssignmentResponse;
import org.example.homeworkhub.assignment.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
class AssignmentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AssignmentService assignmentService;

    @Test
    void createsAssignmentAndReturnsLocation() throws Exception {
        //given
        when(assignmentService.create(any()))
                .thenReturn(new AssignmentResponse(7L, "Algebra 1", "Exercises 1-10"));

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json",
                """
                {"title": "Algebra 1", "description": "Exercises 1-10"}
                """.getBytes());
        MockMultipartFile file = new MockMultipartFile(
                "file", "algebra.pdf", "application/pdf", "%PDF-1.4 test".getBytes());

        //when and then
        mockMvc.perform(multipart("/api/assignments")
                        .file(file)                              // the MockMultipartFile named "file"
                        .param("title", "Algebra 1")
                        .param("description", "Exercises 1-10"))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json", """
                {"title": "  ", "description": "x"}
                """.getBytes());
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});

        mockMvc.perform(multipart("/api/assignments").file(data).file(file))
                .andExpect(status().isBadRequest());
    }
}