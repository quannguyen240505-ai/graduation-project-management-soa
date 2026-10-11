package com.group.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.group.registration.client.StudentClient;
import com.group.registration.client.TopicClient;
import com.group.registration.dto.TopicCapacityRequest;
import com.group.registration.dto.TopicDTO;
import com.group.registration.entity.Registration;
import com.group.registration.repository.RegistrationRepository;
import com.group.registration.producer.RegistrationEventProducer;

import feign.FeignException;

class RegistrationCapacityTest {

    private static final String TOKEN = "Bearer unit-test-placeholder";
    private RegistrationRepository repository;
    private TopicClient topics;
    private RegistrationService service;
    private Registration registration;
    private RegistrationEventProducer registrationEventProducer;

    @BeforeEach
    void prepare() {
        repository = mock(RegistrationRepository.class);
        topics = mock(TopicClient.class);
        registrationEventProducer = mock(RegistrationEventProducer.class);

        service = new RegistrationService(
                repository,
                mock(StudentClient.class),
                topics,
                registrationEventProducer
        );
        registration = new Registration();
        registration.setId(20L);
        registration.setTopicId(10L);
        registration.setStudentId(30L);
        registration.setStatus("PENDING");
        when(repository.findByIdForUpdate(20L)).thenReturn(Optional.of(registration));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(topics.synchronizeCapacity(anyLong(), anyLong(), any(), anyString()))
                .thenReturn(new TopicDTO());
    }

    @Test
    void approvalForwardsTheLecturerRequestToken() {
        // TOKEN là placeholder; request thực tế phải mang JWT của LECTURER/ADMIN.
        service.updateStatus(20L, " approved ", TOKEN);
        assertEquals("APPROVED", registration.getStatus());
        assertEquals(1, registration.getCapacityVersion());
        verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(true, 1), TOKEN);
    }

    @Test
    void repeatedApprovalUsesTheSameVersion() {
        service.updateStatus(20L, "APPROVED", TOKEN);
        service.updateStatus(20L, "APPROVED", TOKEN);
        assertEquals(1, registration.getCapacityVersion());
        verify(topics, times(2)).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(true, 1), TOKEN);
    }

    @Test
    void rejectionReleasesAnApprovedSlotWithANewerVersion() {
        registration.setStatus("APPROVED");
        registration.setCapacityVersion(1);
        service.updateStatus(20L, "REJECTED", TOKEN);
        assertEquals("REJECTED", registration.getStatus());
        assertEquals(2, registration.getCapacityVersion());
        verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(false, 2), TOKEN);
    }

    @Test
    void returningToPendingAlsoReleasesTheSlot() {
        registration.setStatus("APPROVED");
        registration.setCapacityVersion(1);
        service.updateStatus(20L, "PENDING", TOKEN);
        verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(false, 2), TOKEN);
    }

    @Test
    void pendingAndRejectedDoNotAllocateCapacity() {
        service.updateStatus(20L, "REJECTED", TOKEN);
        verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(false, 1), TOKEN);
    }

    @Test
    void approvingLegacyDataStartsAtVersionOne() {
        registration.setStatus("APPROVED");
        service.updateStatus(20L, "APPROVED", TOKEN);
        verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(true, 1), TOKEN);
        assertEquals(1, registration.getCapacityVersion());
    }

    @Test
    void conflictFromTopicDoesNotChangeTheLocalStatus() {
        failTopic(409);
        expectStatus(409, () -> service.updateStatus(20L, "APPROVED", TOKEN));
        assertEquals("PENDING", registration.getStatus());
        assertEquals(0, registration.getCapacityVersion());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void topicUnavailableReturns503AndKeepsPending() {
        failTopic(-1);
        expectStatus(503, () -> service.updateStatus(20L, "APPROVED", TOKEN));
        assertEquals("PENDING", registration.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void studentOrWrongLecturerRejectionFromTopicIsPreserved() {
        failTopic(403);
        expectStatus(403, () -> service.updateStatus(20L, "APPROVED", TOKEN));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deletionReleasesBeforeDeletingTheLocalRecord() {
        registration.setStatus("APPROVED");
        registration.setCapacityVersion(3);
        service.delete(20L, TOKEN);
        var order = inOrder(topics, repository);
        order.verify(topics).synchronizeCapacity(10L, 20L,
                new TopicCapacityRequest(false, 4), TOKEN);
        order.verify(repository).delete(registration);
        order.verify(repository).flush();
    }

    @Test
    void failedReleaseDoesNotDeleteRegistration() {
        registration.setStatus("APPROVED");
        failTopic(409);
        expectStatus(409, () -> service.delete(20L, TOKEN));
        verify(repository, never()).delete(any());
    }

    @Test
    void pendingRegistrationCanBeRemovedAfterItsTopicWasDeleted() {
        failTopic(404);
        service.delete(20L, TOKEN);
        verify(repository).delete(registration);
    }

    @Test
    void approvedRegistrationRequiresItsTopicToReleaseTheSlot() {
        registration.setStatus("APPROVED");
        failTopic(404);
        expectStatus(404, () -> service.delete(20L, TOKEN));
        verify(repository, never()).delete(any());
    }

    @Test
    void invalidStatusDoesNotCallTopic() {
        expectStatus(400, () -> service.updateStatus(20L, "DELETED", TOKEN));
        verifyNoInteractions(topics);
    }

    @Test
    void missingTokenDoesNotChangeStatus() {
        expectStatus(401, () -> service.updateStatus(20L, "APPROVED", null));
        assertEquals("PENDING", registration.getStatus());
        verifyNoInteractions(topics);
    }

    private void failTopic(int status) {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(status);
        when(topics.synchronizeCapacity(anyLong(), anyLong(), any(), anyString()))
                .thenThrow(ex);
    }

    private void expectStatus(int expected, Runnable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(expected, ex.getStatusCode().value());
    }
}
