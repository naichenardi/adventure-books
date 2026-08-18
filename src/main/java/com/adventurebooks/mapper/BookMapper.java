package com.adventurebooks.mapper;

import com.adventurebooks.generated.model.*;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Consequence;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", expression = "java(book.getId() != null ? book.getId().intValue() : null)")
    @Mapping(target = "difficulty", source = "difficulty", qualifiedByName = "difficultyToDto")
    BookDto toDto(Book book);

    @Mapping(target = "type", source = "type", qualifiedByName = "sectionTypeToDto")
    SectionDto toDto(Section section);

    @Mapping(target = "consequence", source = "consequence")
    OptionDto toDto(Option option);

    @Mapping(target = "type", source = "type", qualifiedByName = "consequenceTypeToDto")
    @Mapping(target = "value", expression = "java(consequence.getValue() != null ? String.valueOf(consequence.getValue()) : null)")
    ConsequenceDto toDto(Consequence consequence);

    @Named("difficultyToDto")
    default DifficultyDto difficultyToDto(Difficulty difficulty) {
        return difficulty != null ? DifficultyDto.fromValue(difficulty.name()) : null;
    }

    @Named("sectionTypeToDto")
    default SectionTypeDto sectionTypeToDto(SectionType type) {
        return type != null ? SectionTypeDto.fromValue(type.name()) : null;
    }

    @Named("consequenceTypeToDto")
    default ConsequenceTypeDto consequenceTypeToDto(ConsequenceType type) {
        return type != null ? ConsequenceTypeDto.fromValue(type.name()) : null;
    }
}
