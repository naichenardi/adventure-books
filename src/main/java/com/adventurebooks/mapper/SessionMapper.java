package com.adventurebooks.mapper;

import com.adventurebooks.generated.model.GameSessionDto;
import com.adventurebooks.model.entity.GameSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "finished", expression = "java(!session.isActive())")
    GameSessionDto toDto(GameSession session);
}
