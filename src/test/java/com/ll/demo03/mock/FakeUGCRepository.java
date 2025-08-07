package com.ll.demo03.mock;

import com.ll.demo03.UGC.infrastructure.UGCEntity;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeUGCRepository implements UGCRepository {

    private final Map<Long, UGC> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public UGC save(UGC ugc) {
        if (ugc.getId() == null) {
            Long id = idGenerator.getAndIncrement();
            ugc = UGC.builder()
                    .id(id)
                    .url(ugc.getUrl())
                    .index(ugc.getIndex())
                    .imageTask(ugc.getImageTask())
                    .videoTask(ugc.getVideoTask())
                    .createdAt(ugc.getCreatedAt())
                    .creator(ugc.getCreator())
                    .build();

        }
        storage.put(ugc.getId(), ugc);
        return ugc;
    }

    @Override
    public void delete(UGC ugc) {
        storage.remove(ugc.getId());
    }

    @Override
    public Optional<UGC> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsByIdLessThanAndMemberId(Long id, Long memberId) {
        return storage.values().stream()
                .anyMatch(ugc -> ugc.getId() < id && ugc.getCreator().getId().equals(memberId));
    }

    @Override
    public boolean existsByIdGreaterThanAndMemberId(Long id, Long memberId) {
        return storage.values().stream()
                .anyMatch(ugc -> ugc.getId() > id && ugc.getCreator().getId().equals(memberId));
    }

    @Override
    public Slice<UGC> findByMemberIdOrderByIdDesc(Long memberId, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId))
                .sorted(Comparator.comparingLong(UGC::getId).reversed())
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndIdLessThanOrderByIdDesc(Long memberId, Long cursorId, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId) && ugc.getId() < cursorId)
                .sorted(Comparator.comparingLong(UGC::getId).reversed())
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndIdGreaterThanOrderByIdAsc(Long memberId, Long cursorId, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId) && ugc.getId() > cursorId)
                .sorted(Comparator.comparingLong(UGC::getId))
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        storage.values().removeIf(ugc -> ugc.getCreator().getId().equals(memberId));
    }

    @Override
    public List<UGC> findByImageTaskIn(List<ImageTask> tasks) {
        Set<Long> taskIds = tasks.stream().map(ImageTask::getId).collect(Collectors.toSet());
        return storage.values().stream()
                .filter(ugc -> ugc.getImageTask() != null && taskIds.contains(ugc.getImageTask().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UGC> findByVideoTaskIn(List<VideoTask> tasks) {
        Set<Long> taskIds = tasks.stream().map(VideoTask::getId).collect(Collectors.toSet());
        return storage.values().stream()
                .filter(ugc -> ugc.getVideoTask() != null && taskIds.contains(ugc.getVideoTask().getId()))
                .collect(Collectors.toList());
    }

    private Slice<UGC> getSlice(List<UGC> fullList, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), fullList.size());
        List<UGC> content = start > fullList.size() ? List.of() : fullList.subList(start, end);
        boolean hasNext = end < fullList.size();
        return new PageImpl<>(content, pageable, hasNext ? end + 1 : fullList.size());
    }

    @Override
    public List<UGC> findAllByVideoTaskId(Long id) {
        return storage.values().stream()
                .filter(ugc -> ugc.getVideoTask() != null && ugc.getVideoTask().getId().equals(id))
                .collect(Collectors.toList());
    }


    @Override
    public void deleteAll(List<UGC> ugcs) {
        for (UGC ugc : ugcs) {
            storage.remove(ugc.getId());
        }
    }

    @Override
    public List<UGC> findAllByImageTaskId(Long id) {
        return storage.values().stream()
                .filter(ugc -> ugc.getImageTask() != null && ugc.getImageTask().getId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeOrderByIdDesc(Long memberId, String type, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId) && getUGCType(ugc).equals(type))
                .sorted(Comparator.comparingLong(UGC::getId).reversed())
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeAndIdLessThanOrderByIdDesc(Long memberId, String type, Long cursorId, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId) 
                             && getUGCType(ugc).equals(type) 
                             && ugc.getId() < cursorId)
                .sorted(Comparator.comparingLong(UGC::getId).reversed())
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeAndIdGreaterThanOrderByIdAsc(Long memberId, String type, Long cursorId, Pageable pageRequest) {
        List<UGC> filtered = storage.values().stream()
                .filter(ugc -> ugc.getCreator().getId().equals(memberId) 
                             && getUGCType(ugc).equals(type) 
                             && ugc.getId() > cursorId)
                .sorted(Comparator.comparingLong(UGC::getId))
                .collect(Collectors.toList());
        return getSlice(filtered, pageRequest);
    }

    @Override
    public boolean existsByIdGreaterThanAndMemberIdAndType(Long id, Long memberId, String type) {
        return storage.values().stream()
                .anyMatch(ugc -> ugc.getId() > id 
                              && ugc.getCreator().getId().equals(memberId) 
                              && getUGCType(ugc).equals(type));
    }

    @Override
    public boolean existsByIdLessThanAndMemberIdAndType(Long id, Long memberId, String type) {
        return storage.values().stream()
                .anyMatch(ugc -> ugc.getId() < id 
                              && ugc.getCreator().getId().equals(memberId) 
                              && getUGCType(ugc).equals(type));
    }

    private String getUGCType(UGC ugc) {
        if (ugc.getImageTask() != null) {
            return "image";
        } else if (ugc.getVideoTask() != null) {
            return "video";
        }
        return "unknown";
    }

}
