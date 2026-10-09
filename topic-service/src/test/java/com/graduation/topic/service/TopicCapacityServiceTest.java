package com.graduation.topic.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.graduation.topic.dto.TopicCapacityRequest;
import com.graduation.topic.entity.Topic;
import com.graduation.topic.entity.TopicRegistrationSlot;
import com.graduation.topic.repository.TopicRegistrationSlotRepository;
import com.graduation.topic.repository.TopicRepository;

class TopicCapacityServiceTest {

    private TopicRepository topics;
    private TopicRegistrationSlotRepository slots;
    private TopicCapacityService service;
    private Topic topic;

    @BeforeEach
    void prepare() {
        topics = mock(TopicRepository.class);
        slots = mock(TopicRegistrationSlotRepository.class);
        service = new TopicCapacityService(topics, slots);
        topic = new Topic();
        topic.setId(10L);
        topic.setLecturerId(2L);
        topic.setMaxStudents(1);
        topic.setCurrentStudents(0);
        topic.setStatus("OPEN");
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(slots.findById(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void approvalIsCountedOnceAndReleaseIsCountedOnce() {
        TopicRegistrationSlot slot = new TopicRegistrationSlot(20L, 10L);
        when(slots.saveAndFlush(any())).thenAnswer(invocation -> {
            TopicRegistrationSlot saved = invocation.getArgument(0);
            slot.setApproved(saved.isApproved());
            slot.setCapacityVersion(saved.getCapacityVersion());
            when(slots.findById(20L)).thenReturn(Optional.of(slot));
            return saved;
        });

        call(true, 1);
        assertEquals(1, topic.getCurrentStudents());
        call(true, 1);
        assertEquals(1, topic.getCurrentStudents());
        call(false, 2);
        assertEquals(0, topic.getCurrentStudents());
        call(false, 2);
        assertEquals(0, topic.getCurrentStudents());
    }

    @Test
    void fullTopicDoesNotSaveAnything() {
        topic.setCurrentStudents(1);
        conflict(() -> call(true, 1));
        assertEquals(1, topic.getCurrentStudents());
        verify(slots, never()).saveAndFlush(any());
        verify(topics, never()).saveAndFlush(any());
    }

    @Test
    void oldApprovalCannotUndoANewerRejection() {
        existing(false, 2);
        conflict(() -> call(true, 1));
        assertEquals(0, topic.getCurrentStudents());
    }

    @Test
    void equalVersionCannotChangeTheTarget() {
        topic.setCurrentStudents(1);
        existing(true, 1);
        conflict(() -> call(false, 1));
        assertEquals(1, topic.getCurrentStudents());
    }

    @Test
    void anotherLecturerCannotChangeCapacity() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.synchronize(10L, 20L,
                        new TopicCapacityRequest(true, 1L), 99L, "LECTURER"));
        assertEquals(403, ex.getStatusCode().value());
        verify(slots, never()).saveAndFlush(any());
    }

    @Test
    void studentCannotChangeCapacityEvenIfIdsMatch() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.synchronize(10L, 20L,
                        new TopicCapacityRequest(true, 1L), 2L, "STUDENT"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void adminCanChangeCapacity() {
        service.synchronize(10L, 20L, new TopicCapacityRequest(true, 1L), null, "ADMIN");
        assertEquals(1, topic.getCurrentStudents());
    }

    @Test
    void unknownTopicReturns404() {
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> call(true, 1));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void registrationCannotMoveToAnotherTopic() {
        TopicRegistrationSlot wrong = new TopicRegistrationSlot(20L, 99L);
        when(slots.findById(20L)).thenReturn(Optional.of(wrong));
        conflict(() -> call(true, 1));
    }

    @Test
    void invalidBodyReturns400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.synchronize(10L, 20L,
                        new TopicCapacityRequest(null, 1L), 2L, "LECTURER"));
        assertEquals(400, ex.getStatusCode().value());
        verify(topics, never()).findByIdForUpdate(anyLong());
    }

    private void existing(boolean approved, long version) {
        TopicRegistrationSlot slot = new TopicRegistrationSlot(20L, 10L);
        slot.setApproved(approved);
        slot.setCapacityVersion(version);
        when(slots.findById(20L)).thenReturn(Optional.of(slot));
    }

    private void call(boolean approved, long version) {
        service.synchronize(10L, 20L,
                new TopicCapacityRequest(approved, version), 2L, "LECTURER");
    }

    private void conflict(Runnable action) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(409, ex.getStatusCode().value());
    }
}
