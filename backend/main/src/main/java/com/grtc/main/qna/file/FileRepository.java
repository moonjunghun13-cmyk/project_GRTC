package com.grtc.main.qna.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByComplainId(Long complainId);

    long countByComplainId(Long complainId);
}
