package com.kizzo.langchain4j_spingboot_demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    List<String> selectMessagesByMemoryId(@Param("memoryId") String memoryId);

    int deleteMessagesByMemoryId(@Param("memoryId") String memoryId);

    int insertMessages(@Param("memoryId") String memoryId, @Param("messageJson") String messageJson);
}