package com.campus.eventhub.repository;

import com.campus.eventhub.domain.Participant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ParticipantRepositoryDataJpaTest {

    @Autowired private ParticipantRepository participantRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    @DisplayName("Kiểm thử tính duy nhất (UNIQUE constraint) của Email Participant ở tầng DB")
    void testEmailUniquenessConstraint() {
        Participant p1 = new Participant("Nguyen Van A", "duplicate@campus.edu.vn");
        participantRepository.save(p1);
        entityManager.flush();

        Participant p2 = new Participant("Tran Van B", "duplicate@campus.edu.vn");

        assertThatThrownBy(() -> {
            participantRepository.save(p2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Kiểm thử truy vấn tìm kiếm Participant có áp dụng Phân trang (Pagination)")
    void testFindAllWithPagination() {
        participantRepository.save(new Participant("A", "a@campus.edu.vn"));
        participantRepository.save(new Participant("B", "b@campus.edu.vn"));
        participantRepository.save(new Participant("C", "c@campus.edu.vn"));
        entityManager.flush();

        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Participant> page = participantRepository.findAll(pageRequest);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}