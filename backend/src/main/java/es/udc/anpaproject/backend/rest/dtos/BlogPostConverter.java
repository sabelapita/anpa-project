package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.BlogPost;
import es.udc.anpaproject.backend.model.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class BlogPostConverter {

    private BlogPostConverter() {}

    public static BlogPostDto toPostDto(BlogPost post) {
        return new BlogPostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getAuthor().getId(),
                post.isPublished(),
                post.getCategory()

        );
    }

    public static List<BlogPostDto> toPostDtos(List<BlogPost> posts) {
        return posts.stream()
                .map(BlogPostConverter::toPostDto)
                .collect(Collectors.toList());
    }

    public static BlogPost toPostEntity(BlogPostDto dto, User author) {
        BlogPost post = new BlogPost();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setAuthor(author);
        post.setPublished(dto.isPublished());
        post.setCategory(dto.getCategory());
        return post;
    }

    public static void merge(BlogPost target, BlogPostDto dto) {
        target.setTitle(dto.getTitle());
        target.setContent(dto.getContent());
        target.setPublished(dto.isPublished());
        target.setCategory(dto.getCategory());
    }
}
