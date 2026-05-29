package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.Message;
import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import es.udc.anpaproject.backend.model.entities.MessageReply;
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
                message.getRespondedAt(),
                message.getUser().getUserName(),
                message.getUser().getFirstName() + " " + message.getUser().getLastName(),
                toAttachmentDtos(message.getAttachments()),
                toReplyDtos(message.getReplies())
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

    private static MessageAttachmentDto toAttachmentDto(MessageAttachment attachment) {
        return new MessageAttachmentDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType()
        );
    }

    private static List<MessageAttachmentDto> toAttachmentDtos(List<MessageAttachment> attachments) {
        return attachments.stream()
                .map(MessageConverter::toAttachmentDto)
                .collect(Collectors.toList());
    }

    private static MessageReplyDto toReplyDto(MessageReply reply) {
        return new MessageReplyDto(
                reply.getId(),
                reply.isAdminReply(),
                reply.getContent(),
                reply.getSentAt()
        );
    }

    private static List<MessageReplyDto> toReplyDtos(List<MessageReply> replies) {
        return replies.stream()
                .map(MessageConverter::toReplyDto)
                .collect(Collectors.toList());
    }
}
