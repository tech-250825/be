package com.ll.demo03.mock;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.global.domain.Status;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeImageTaskRepository implements ImageTaskRepository {

    private final Map<Long, ImageTask> storage = new HashMap<>(); // ImageTask를 저장할 떄 id를 Key로, ImageTask 객체를 value로 넣는다.
    private final AtomicLong idGenerator = new AtomicLong(1L); //고유한 ID를 자동으로 생성하기 위해. 하나의 JVM 프로세스 안에서 여러 스레드가 동시에 접근해도 Atomic하게 처리됨 .CAS(Compare and swap)으로!@

    @Override
    public void deleteByMemberId(Long memberId) {
        storage.values().removeIf(task -> task.getCreator().getId().equals(memberId));
    }

    @Override
    public Slice<ImageTask> findByMember(Member creator, PageRequest pageRequest) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(creator.getId()))
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageRequest);
    }


    @Override
    public Slice<ImageTask> findByMemberAndImageUrlIsNull(Member creator, PageRequest pageRequest) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(creator.getId()) && task.getImageUrl() == null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<ImageTask> findCreatedAfterAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(member.getId()) 
                        && task.getCreatedAt().isAfter(createdAt) 
                        && task.getImageUrl() == null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageable);
    }

    @Override
    public Slice<ImageTask> findCreatedBeforeAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(member.getId()) 
                        && task.getCreatedAt().isBefore(createdAt) 
                        && task.getImageUrl() == null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageable);
    }

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt) {
        return storage.values().stream()
                .anyMatch(task -> task.getCreator().getId().equals(creator.getId()) 
                        && task.getCreatedAt().isAfter(createdAt) 
                        && task.getImageUrl() == null);
    }

    @Override
    public boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt) {
        return storage.values().stream()
                .anyMatch(task -> task.getCreator().getId().equals(creator.getId()) 
                        && task.getCreatedAt().isBefore(createdAt) 
                        && task.getImageUrl() == null);
    }

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt) {
        return storage.values().stream()
                .anyMatch(task -> task.getCreator().getId().equals(creator.getId()) 
                        && task.getCreatedAt().isAfter(createdAt) 
                        && task.getImageUrl() != null);
    }

    @Override
    public boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt) {
        return storage.values().stream()
                .anyMatch(task -> task.getCreator().getId().equals(creator.getId()) 
                        && task.getCreatedAt().isBefore(createdAt) 
                        && task.getImageUrl() != null);
    }

    @Override
    public ImageTask save(ImageTask imageTask) {
        Long id = imageTask.getId() != null ? imageTask.getId() : idGenerator.getAndIncrement();

        ImageTask saved = ImageTask.builder()
                .id(id)
                .prompt(imageTask.getPrompt())
                .checkpoint(imageTask.getCheckpoint())
                .lora(imageTask.getLora())
                .runpodId(imageTask.getRunpodId())
                .status(imageTask.getStatus())
                .createdAt(imageTask.getCreatedAt())
                .modifiedAt(imageTask.getModifiedAt())
                .creator(imageTask.getCreator())
                .resolutionProfile(imageTask.getResolutionProfile())
                .imageUrl(imageTask.getImageUrl())
                .build();

        storage.put(id, saved);
        return saved;
    }


    @Override
    public Optional<ImageTask> findById(Long taskId) {
        return Optional.ofNullable(storage.get(taskId));
    }

    @Override
    public void delete(ImageTask task) {
        storage.remove(task.getId());
    }

    @Override
    public Slice<ImageTask> findByMemberAndImageUrlIsNotNull(Member member, PageRequest pageRequest) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(member.getId()) && task.getImageUrl() != null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageRequest);
    }

    @Override
    public Slice<ImageTask> findCreatedAfterAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(member.getId()) 
                        && task.getCreatedAt().isAfter(createdAt) 
                        && task.getImageUrl() != null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageable);
    }

    @Override
    public Slice<ImageTask> findCreatedBeforeAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        List<ImageTask> filtered = storage.values().stream()
                .filter(task -> task.getCreator().getId().equals(member.getId()) 
                        && task.getCreatedAt().isBefore(createdAt) 
                        && task.getImageUrl() != null)
                .sorted(Comparator.comparing(ImageTask::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getSlice(filtered, pageable);
    }

    private Slice<ImageTask> getSlice(List<ImageTask> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        boolean hasNext = list.size() > end;
        List<ImageTask> content = list.subList(start, end);
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
