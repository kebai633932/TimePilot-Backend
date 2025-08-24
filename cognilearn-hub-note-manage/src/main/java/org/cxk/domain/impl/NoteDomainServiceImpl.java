package org.cxk.domain.impl;

import org.cxk.api.INoteService;
import org.cxk.api.dto.NoteVectorDTO;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.cxk.api.dto.*;
import org.cxk.domain.INoteDomainService;
import org.cxk.domain.model.entity.NoteEntity;
import org.cxk.domain.repository.IFolderRepository;
import org.cxk.domain.repository.INoteRepository;
import org.cxk.util.MarkdownUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.cxk.types.exception.BizException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 笔记管理服务接口实现（DDD 风格）
 * @create 2025/4/25 0:46
 */
@Slf4j
@Service
@DubboService(interfaceClass = INoteService.class, version = "1.0")
// learn: INoteDomainService 继承了 INoteService，
// Dubbo 也不会自动认为它提供了 INoteService，因为 Dubbo 注册的时候只看接口全名，不看继承关系，不设定取第一个
public class NoteDomainServiceImpl implements INoteDomainService, INoteService {

    @Resource
    private INoteRepository noteRepository;

    @Resource
    private IFolderRepository folderRepository;
    @Resource
    private AmazonS3 amazonS3;

    @Value("${jdoss.bucket-name}")
    private String bucketName;

    @Override
    public Long createNote(Long userId, NoteCreateDTO dto) {
        // 1. 检查父文件夹是否存在
        if(dto.getFolderId()!=0){
            folderRepository.findByFolderIdAndUserId(dto.getFolderId(), userId)
                    .orElseThrow(() -> new BizException("父文件夹不存在或无权限"));
        }


        // 2. 生成新笔记 ID
        Long noteId = TinyId.nextId("note_create");

        // 3. 构建 NoteEntity（由领域对象维护 Markdown → PlainText 一致性）
        NoteEntity noteEntity = NoteEntity.builder()
                .noteId(noteId)
                .userId(userId)
                .folderId(dto.getFolderId())
                .title(dto.getTitle())
                .contentMd(dto.getContentMd())
                .contentPlain(MarkdownUtils.mdToPlainText(dto.getContentMd()))
                .status((short) 0)
                .isPublic(Boolean.FALSE)
                .version(0L)
                .build();

        // 4. 保存
        noteRepository.save(noteEntity);

        return noteId;
    }
    @Override
    public void moveNote(Long userId, NoteMoveDTO dto) {

        // 1. 检查笔记是否存在 & 属于用户
        NoteEntity noteEntity = noteRepository.findByNoteIdAndUserId(dto.getNoteId(), userId)
                .orElseThrow(() -> new BizException("笔记不存在或无权限"));

        // 2. 如果更新了文件夹，则校验文件夹归属
        if (dto.getFolderId() != null) {
            folderRepository.findByFolderIdAndUserId(dto.getFolderId(), userId)
                    .orElseThrow(() -> new BizException("目标文件夹不存在或无权限"));
            noteEntity.setFolderId(dto.getFolderId());
        }

        // 3. 保存
        noteRepository.move(noteEntity);
    }
    @Override
    public void updateNote(Long userId, NoteUpdateDTO dto) {
        // 1. 检查笔记是否存在 & 属于用户
        NoteEntity noteEntity = noteRepository.findByNoteIdAndUserId(dto.getNoteId(), userId)
                .orElseThrow(() -> new BizException("笔记不存在或无权限"));

        // 2. 更新笔记内容、标题
        noteEntity.setFolderId(dto.getFolderId());
        noteEntity.setTitle(dto.getTitle());
        noteEntity.setContentMd(dto.getContentMd());
        noteEntity.setContentPlain(MarkdownUtils.mdToPlainText(dto.getContentMd())); // Markdown -> PlainText

        // 3. 保存
        noteRepository.update(noteEntity);
    }

    @Override
    public void deleteNote(Long userId, Long noteId) {
        NoteEntity noteEntity = noteRepository.findByNoteIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BizException("笔记不存在或无权限"));

        // 逻辑删除（由实体封装）
        noteEntity.markDeleted();
        noteRepository.update(noteEntity);
    }

    //todo 公开,公开状态机


    //todo 这个应该放types模块的其他service
    //todo 未测试
    @Override
    public String uploadNoteImage(Long userId, MultipartFile file) {
        try {
            // 生成唯一文件名（可按用户ID分目录）
            String fileName = "notes/" + userId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 上传到京东云 OSS
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

            // 拼接访问 URL（公有读可以直接访问，私有需要签名）
            String fileUrl = String.format("https://%s.%s/%s",
                    bucketName, "oss.cn-north-1.jdcloud-oss.com", fileName);

            return fileUrl;

        } catch (Exception e) {
            throw new RuntimeException("上传图片失败", e);
        }
    }

    @Override
    public List<NoteInfoDTO> attachNotesToFolders(Long userId, Map<Long, FolderNoteDTO> folderNoteDTOMap) {
        List<NoteInfoDTO> rootNotes=new ArrayList<>();
        // 1. 查询用户所有笔记
        List<NoteEntity> noteEntityList = noteRepository.findByUserId(userId);
        if (noteEntityList == null || noteEntityList.isEmpty()) {
            return rootNotes; // 没有笔记，直接返回
        }

        // 2. 遍历笔记，挂到对应的文件夹 DTO 上
        for (NoteEntity noteEntity : noteEntityList) {
            NoteInfoDTO noteInfoDTO = new NoteInfoDTO();
            noteInfoDTO.setNoteId(noteEntity.getNoteId());
            noteInfoDTO.setFolderId(noteEntity.getFolderId());
            noteInfoDTO.setTitle(noteEntity.getTitle());

            Long folderId = noteEntity.getFolderId();

            // 3. 找到对应的文件夹
            if (folderId == null || folderId == 0) {
                // 没有父节点 → 说明它是一个根笔记
                rootNotes.add(noteInfoDTO);
            } else {
                // 找到父节点 DTO
                FolderNoteDTO parentDto = folderNoteDTOMap.get(folderId);

                if (parentDto != null) {
                    // 如果父节点存在，就把当前节点挂到父节点的 children 列表里
                    parentDto.getNotes().add(noteInfoDTO);
                } else {
                    // 如果找不到父节点（可能是数据异常），就退化成根节点
                    log.error("笔记找不到父节点（可能是数据异常），就退化成根节点，folderId={}", folderId);
                    rootNotes.add(noteInfoDTO);
                }
            }
        }
        return rootNotes;

    }


    @Override
    public List<NoteVectorDTO> findNotesByIds(List<Long> batchIds) {
        // 1. 批量查询近期修改但未向量化的笔记
        List<NoteEntity> noteEntities = noteRepository.findByNoteIds(batchIds);
        if (noteEntities == null || noteEntities.isEmpty()) {
            log.warn("没有批量查询近期修改但未向量化的笔记");
        }

        // 2. 转换为 NoteVectorDTO
        return noteEntities.stream()
                .map(note -> {
                    NoteVectorDTO dto = new NoteVectorDTO();
                    dto.setNoteId(note.getNoteId());
                    dto.setUserId(note.getUserId());
                    dto.setFolderId(note.getFolderId());
                    dto.setTitle(note.getTitle());
                    dto.setContentPlain(note.getContentPlain());
                    dto.setIsDeleted(note.getIsDeleted());
                    dto.setDeleteTime(note.getDeleteTime());
                    dto.setVersion(note.getVersion());
                    dto.setContentMd(note.getContentMd());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
