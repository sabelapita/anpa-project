package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.Message;
import es.udc.anpaproject.backend.model.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class MessageConverter {

    private MessageConverter() {}

    public static MessageDto toMessageDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getUser().getId(),
                message.getSubject(),
                message.getContent(),
                message.getSentAt(),
                message.getResponse(),
                message.getRespondedAt()
        );
    }

    public static List<MessageDto> toMessageDtos(List<Message> messages) {
        return messages.stream()
                .map(MessageConverter::toMessageDto)
                .collect(Collectors.toList());
    }

    public static Message toMessageEntity(MessageDto dto, User user) {

        Message message = new Message();
        message.setUser(user);
        message.setSubject(dto.getSubject());
        message.setContent(dto.getContent());

        return message;
    }
}