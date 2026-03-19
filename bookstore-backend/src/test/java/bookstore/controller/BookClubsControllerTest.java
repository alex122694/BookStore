package bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import bookstore.bean.BookClubsBean;
import bookstore.bean.ClubConstants;
import bookstore.dto.BookClubRequestDTO;
import bookstore.service.BookClubService;
import bookstore.util.JwtUtil;

import io.jsonwebtoken.Claims;
import static org.mockito.Mockito.mock;

@WebMvcTest(BookClubsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookClubsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private BookClubService bookClubService;

        @MockitoBean
        private JwtUtil jwtUtil;

        @Autowired
        private ObjectMapper objectMapper;

        private BookClubsBean mockClub;

        @BeforeEach
        public void setup() {
                mockClub = new BookClubsBean();
                mockClub.setClubId(1);
                mockClub.setClubName("Test Reading Group");
                mockClub.setLocation("Online");
                mockClub.setMaxParticipants(50);
        }

        @Test
        @DisplayName("Case 1: 未登入拒絕 (401 Unauthorized)")
        public void testUpdateClubUnauthenticated() throws Exception {
                BookClubRequestDTO dto = new BookClubRequestDTO();
                dto.setClubName("Test Club");

                MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json",
                                objectMapper.writeValueAsBytes(dto));

                mockMvc.perform(multipart("/api/clubs/update/{clubId}", 1)
                                .file(dataPart)
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andDo(print())
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Case 2: 一般會員 (Member) 更新成功")
        public void testUpdateClubAsMemberSuccess() throws Exception {
                Claims mockClaims = mock(Claims.class);
                when(mockClaims.get("role")).thenReturn("MEMBER");
                when(jwtUtil.getClaims(anyString())).thenReturn(mockClaims);
                when(jwtUtil.getRole(anyString())).thenReturn("MEMBER");
                when(jwtUtil.getMemberId(anyString())).thenReturn("10");

                BookClubRequestDTO dto = new BookClubRequestDTO();
                dto.setClubName("Updated Name");

                when(bookClubService.updateBookclub(
                                eq(1), any(BookClubRequestDTO.class), eq(10), eq(ClubConstants.ROLE_MEMBER)))
                                .thenReturn(mockClub);

                MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json",
                                objectMapper.writeValueAsBytes(dto));
                MockMultipartFile proposalPart = new MockMultipartFile("proposal", "prop.pdf", "application/pdf",
                                "proposal".getBytes());

                mockMvc.perform(multipart("/api/clubs/update/{clubId}", 1)
                                .file(dataPart)
                                .file(proposalPart)
                                .header("Authorization", "Bearer mockVideoToken")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setAttribute("userId", 10);
                                        return request;
                                }))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Case 3: 管理員 (Admin) 更新成功")
        public void testUpdateClubAsAdminSuccess() throws Exception {
                Claims mockClaims = mock(Claims.class);
                when(mockClaims.get("role")).thenReturn("ADMIN");
                when(jwtUtil.getClaims(anyString())).thenReturn(mockClaims);
                when(jwtUtil.getRole(anyString())).thenReturn("ADMIN");
                when(jwtUtil.getMemberId(anyString())).thenReturn("99");

                BookClubRequestDTO dto = new BookClubRequestDTO();
                dto.setClubName("Admin Update");

                when(bookClubService.updateBookclub(
                                eq(1), any(BookClubRequestDTO.class), eq(99), eq(ClubConstants.ROLE_ADMIN)))
                                .thenReturn(mockClub);

                MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json",
                                objectMapper.writeValueAsBytes(dto));

                mockMvc.perform(multipart("/api/clubs/update/{clubId}", 1)
                                .file(dataPart)
                                .header("Authorization", "Bearer mockAdminToken")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setAttribute("userId", 99);
                                        return request;
                                }))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Case 4: Service 層拋出例外 (業務邏輯失敗)")
        public void testUpdateClubServiceException() throws Exception {
                Claims mockClaims = mock(Claims.class);
                when(mockClaims.get("role")).thenReturn("MEMBER");
                when(jwtUtil.getClaims(anyString())).thenReturn(mockClaims);
                when(jwtUtil.getRole(anyString())).thenReturn("MEMBER");
                when(jwtUtil.getMemberId(anyString())).thenReturn("10");

                BookClubRequestDTO dto = new BookClubRequestDTO();

                when(bookClubService.updateBookclub(
                                anyInt(), any(BookClubRequestDTO.class), anyInt(), anyInt()))
                                .thenThrow(new RuntimeException("Business Logic Error: Not Owner"));

                MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json",
                                objectMapper.writeValueAsBytes(mockClub));

                mockMvc.perform(multipart("/api/clubs/update/{clubId}", 1)
                                .file(dataPart)
                                .header("Authorization", "Bearer mockToken")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setAttribute("userId", 10);
                                        return request;
                                }))
                                .andDo(print())
                                .andExpect(status().isInternalServerError());
        }
}
