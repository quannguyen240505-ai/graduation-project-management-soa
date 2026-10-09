package com.group.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import com.group.registration.client.StudentClient;
import com.group.registration.client.TopicClient;
import com.group.registration.dto.StudentDTO;
import com.group.registration.dto.TopicDTO;
import com.group.registration.entity.Registration;
import com.group.registration.repository.RegistrationRepository;

class RegistrationOwnershipTest {
    private RegistrationRepository registrations;
    private StudentClient students;
    private TopicClient topics;
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        registrations = mock(RegistrationRepository.class);
        students = mock(StudentClient.class);
        topics = mock(TopicClient.class);
        service = new RegistrationService(registrations, students, topics);
    }

    private Jwt token(long userId, String role) {
        return Jwt.withTokenValue("fake-jwt-only-for-unit-test")
                .header("alg", "HS256")
                .subject("SV001")
                .claim("userId", userId)
                .claim("role", role)
                .build();
    }

    private Registration request(Long studentId) {
        Registration r = new Registration();
        r.setStudentId(studentId);
        r.setTopicId(10L);
        return r;
    }

    @Test
    void studentAIsForbiddenToRegisterForStudentB() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(request(22L), token(11L, "STUDENT"), "Bearer A"));
        assertEquals(403, ex.getStatusCode().value());
        verifyNoInteractions(students, topics, registrations);
    }

    @Test
    void omittingStudentIdAutomaticallyUsesAuthenticatedId() {
        StudentDTO currentUser = new StudentDTO();
        currentUser.setId(11L);
        currentUser.setRole("STUDENT");
        currentUser.setStatus("ACTIVE");
        when(students.getCurrentStudent("Bearer A")).thenReturn(currentUser);

        TopicDTO topic = new TopicDTO();
        topic.setStatus("OPEN");
        topic.setCurrentStudents(0);
        topic.setMaxStudents(3);
        when(topics.getTopicById(10L, "Bearer A")).thenReturn(topic);
        when(registrations.save(any(Registration.class))).thenAnswer(x -> x.getArgument(0));

        Registration created = service.create(request(null), token(11L, "STUDENT"), "Bearer A");
        assertEquals(11L, created.getStudentId());
        assertEquals("PENDING", created.getStatus());
    }

    @Test
    void lecturerCannotSubmitRegistration() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(request(null), token(11L, "LECTURER"), "Bearer A"));
        assertEquals(403, ex.getStatusCode().value());
        verifyNoInteractions(students, topics, registrations);
    }

    @Test
    void studentOnlySeesOwnRegistrations() {
        Registration mine = request(11L);
        when(registrations.findByStudentId(11L)).thenReturn(List.of(mine));
        List<Registration> visible = service.getAll(token(11L, "STUDENT"));
        assertEquals(1, visible.size());
        assertEquals(11L, visible.get(0).getStudentId());
        verify(registrations, never()).findAll();
    }

    @Test
    void studentCannotReadAnotherStudentsRegistration() {
        Registration others = request(22L);
        when(registrations.findById(99L)).thenReturn(Optional.of(others));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getById(99L, token(11L, "STUDENT")));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void lecturerCanReadAllRegistrations() {
        when(registrations.findAll()).thenReturn(List.of(request(11L), request(22L)));
        assertEquals(2, service.getAll(token(77L, "LECTURER")).size());
    }

}
