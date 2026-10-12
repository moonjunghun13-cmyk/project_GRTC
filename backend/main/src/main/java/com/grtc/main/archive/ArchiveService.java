package com.grtc.main.archive;

import com.grtc.main.global.common.HtmlSanitizer;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.file.FileStorage;
import com.grtc.main.global.security.AdminMemberService;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 자료실 로직. 조회·다운로드는 로그인한 회원 누구나, 등록·수정·삭제는 관리자만 (관리자 API 에서만 호출)
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    public static final Set<String> SORTABLE = Set.of("id", "title", "viewCount", "createdAt");

    public static final String MEMBER_URL = "/api/v1/archives";
    public static final String ADMIN_URL = "/api/v1/admin/archives";

    // 업로드 폴더 안에서 자료실 파일이 들어가는 하위 폴더
    private static final String FILE_CATEGORY = "archive";

    private final ArchiveRepository archiveRepository;
    private final ArchiveFileRepository archiveFileRepository;
    private final FileStorage fileStorage;
    private final LoginService loginService;
    private final AdminMemberService adminMemberService;

    // ── 조회 (회원·관리자 공통) ──

    public PageResponse<ArchiveDto.ListItem> listForMember(Long memberId, String keyword, Pageable pageable) {
        loginService.getActiveMember(memberId);
        return list(keyword, pageable);
    }

    public PageResponse<ArchiveDto.ListItem> list(String keyword, Pageable pageable) {
        Specification<ArchiveEntity> spec = (root, query, cb) -> {
            if (!SearchUtil.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = SearchUtil.contains(keyword);
            return cb.or(
                    cb.like(cb.lower(root.<String>get("title")), like, SearchUtil.ESCAPE),
                    cb.like(cb.lower(root.<String>get("content")), like, SearchUtil.ESCAPE)
            );
        };
        Page<ArchiveEntity> result = archiveRepository.findAll(spec, pageable);

        Map<Long, Long> fileCounts = new HashMap<>();
        List<Long> ids = result.getContent().stream().map(ArchiveEntity::getId).toList();
        if (!ids.isEmpty()) {
            for (Object[] row : archiveFileRepository.countByArchiveIds(ids)) {
                fileCounts.put((Long) row[0], (Long) row[1]);
            }
        }
        return PageResponse.from(result, a -> ArchiveDto.ListItem.of(a, fileCounts.getOrDefault(a.getId(), 0L)));
    }

    // 회원용 상세: 볼 때마다 조회수 +1
    @Transactional
    public ArchiveDto.Detail getForMember(Long memberId, Long id) {
        loginService.getActiveMember(memberId);
        if (archiveRepository.increaseViewCount(id) == 0) {
            throw new BusinessException(ErrorCode.ARCHIVE_NOT_FOUND);
        }
        return toDetail(find(id), MEMBER_URL);
    }

    public ArchiveDto.Detail getForAdmin(Long id) {
        return toDetail(find(id), ADMIN_URL);
    }

    // 다운로드할 첨부파일 (다른 글의 파일 번호로는 받을 수 없다)
    public ArchiveFileEntity getFileForMember(Long memberId, Long archiveId, Long fileId) {
        loginService.getActiveMember(memberId);
        return getFile(archiveId, fileId);
    }

    public ArchiveFileEntity getFile(Long archiveId, Long fileId) {
        find(archiveId);
        return archiveFileRepository.findByIdAndArchiveId(fileId, archiveId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    // ── 등록·수정·삭제 (관리자) ──

    @Transactional
    public ArchiveDto.Detail create(Long adminId, ArchiveDto.SaveRequest request, List<MultipartFile> files) {
        LoginEntity writer = adminMemberService.getActiveAdmin(adminId);
        List<MultipartFile> uploads = fileStorage.nonEmpty(files);
        fileStorage.validate(uploads, 0);

        ArchiveEntity archive = archiveRepository.save(ArchiveEntity.builder()
                .title(request.title().trim())
                .content(trimToNull(request.content()))
                .contentHtml(HtmlSanitizer.clean(request.contentHtml()))
                .writer(writer)
                .build());
        storeFiles(archive, uploads);

        log.info("[archive] 등록 id={} writerId={} files={}", archive.getId(), writer.getId(), uploads.size());
        return toDetail(archive, ADMIN_URL);
    }

    @Transactional
    public ArchiveDto.Detail update(Long id, ArchiveDto.SaveRequest request, List<MultipartFile> files) {
        ArchiveEntity archive = find(id);
        List<ArchiveFileEntity> existing = archiveFileRepository.findAllByArchiveIdOrderByIdAsc(id);

        Set<Long> deleteIds = request.deleteFileIds() == null ? Set.of() : new HashSet<>(request.deleteFileIds());
        List<ArchiveFileEntity> toDelete = existing.stream().filter(f -> deleteIds.contains(f.getId())).toList();
        if (toDelete.size() != deleteIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        List<MultipartFile> uploads = fileStorage.nonEmpty(files);
        fileStorage.validate(uploads, existing.size() - toDelete.size());

        for (ArchiveFileEntity file : toDelete) {
            archiveFileRepository.delete(file);
            fileStorage.deleteAfterCommit(file.getFilePath(), file.getSavedFileName());
        }
        storeFiles(archive, uploads);

        archive.update(request.title().trim(), trimToNull(request.content()),
                HtmlSanitizer.clean(request.contentHtml()));
        archive.touch();
        archiveRepository.flush();

        log.info("[archive] 수정 id={} deleted={} added={}", id, toDelete.size(), uploads.size());
        return toDetail(archive, ADMIN_URL);
    }

    // 글과 첨부파일을 함께 지운다. 디스크의 파일은 커밋이 끝난 뒤 지운다
    @Transactional
    public void delete(Long id) {
        ArchiveEntity archive = find(id);
        List<ArchiveFileEntity> files = archiveFileRepository.findAllByArchiveIdOrderByIdAsc(id);
        archiveFileRepository.deleteAll(files);
        files.forEach(f -> fileStorage.deleteAfterCommit(f.getFilePath(), f.getSavedFileName()));
        archiveRepository.delete(archive);
        log.info("[archive] 삭제 id={} files={}", id, files.size());
    }

    private void storeFiles(ArchiveEntity archive, List<MultipartFile> uploads) {
        for (MultipartFile upload : uploads) {
            FileStorage.StoredFile stored = fileStorage.store(FILE_CATEGORY, upload);
            archiveFileRepository.save(ArchiveFileEntity.builder()
                    .archive(archive)
                    .originalFileName(stored.originalName())
                    .savedFileName(stored.savedName())
                    .filePath(stored.relativeDir())
                    .fileSize(stored.size())
                    .contentType(stored.contentType())
                    .build());
        }
    }

    private ArchiveDto.Detail toDetail(ArchiveEntity archive, String urlPrefix) {
        return ArchiveDto.Detail.of(archive,
                archiveFileRepository.findAllByArchiveIdOrderByIdAsc(archive.getId()), urlPrefix);
    }

    private ArchiveEntity find(Long id) {
        return archiveRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARCHIVE_NOT_FOUND));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
