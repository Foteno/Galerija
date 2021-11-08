package lt.insoft.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.Image;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.Image_;
import lt.insoft.gallery.Tag;
import lt.insoft.gallery.TagDto;
import lt.insoft.gallery.Tag_;
import lt.insoft.gallery.User;
import lt.insoft.gallery.User_;
import lt.insoft.gallery.domain.constants.Constants;
import lt.insoft.gallery.domain.exceptions.ImageNotDeletedRuntimeException;
import lt.insoft.gallery.domain.tag.TagRepository;
import lt.insoft.gallery.domain.user.UserDetailsImpl;
import lt.insoft.gallery.domain.user.UserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CommonsLog
public class ImageService implements IImageService {
    private static final String IMAGE_PATH = Constants.IMAGE_STORAGE_PATH;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Specification<Image> tagsLike(String name, String username) {
        if (username.equals("admin")) {
            return (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.like(criteriaBuilder.upper(root.join(Image_.TAGS).get(Tag_.NAME)),
                        "%" + name.toUpperCase(Locale.ROOT) + "%");
            };
        } else {
            return (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.and(criteriaBuilder.like(criteriaBuilder.upper(root.join(Image_.TAGS).get(Tag_.NAME)),
                                "%" + name.toUpperCase(Locale.ROOT) + "%"),
                        criteriaBuilder.equal(root.get(Image_.USER).get(User_.USERNAME), username));
            };

        }

    }

    private Page<ImagePreviewDto> getImageByNameOrDescriptionCriteria(int page, int size, String name, String username) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
        Root<Image> root = criteriaQuery.from(Image.class);

        Predicate descriptionPredicate = builder.like(builder.upper(root.get(Image_.DESCRIPTION)),
                "%" + name.toUpperCase(Locale.ROOT) + "%");
        Predicate namePredicate = builder.like(builder.upper(root.get(Image_.NAME)),
                "%" + name.toUpperCase(Locale.ROOT) + "%");
        Predicate orNameDescription = builder.or(descriptionPredicate, namePredicate);
        if (username != null) {
            Predicate byUserPredicate = builder.equal(root.get(Image_.USER).get(User_.USERNAME), username);
            orNameDescription = builder.and(builder.or(descriptionPredicate, namePredicate), byUserPredicate);
        }

        criteriaQuery.multiselect(root);
        criteriaQuery.where(orNameDescription);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<Tuple> tuples = typedQuery.getResultList();
        List<Image> images = new ArrayList<>();
        for (Tuple tuple : tuples) {
            images.add(tuple.get(root));
        }

        Long totalCount;
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Image> root1 = countQuery.from(Image.class);
        countQuery.select(builder.count(root1));
        countQuery.where(orNameDescription);
        totalCount = entityManager.createQuery(countQuery).getSingleResult();


        return new PageImpl<>(images.stream().map(this::convertToImageDto).toList(),
                PageRequest.of(page, size), totalCount);
    }

    @Override
    public Page<ImagePreviewDto> findImageByTagUsingSpecification(int page, int size, String tag) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Page<Image> image;
        try {
            image = imageRepository.findAll(tagsLike(tag, userDetails.getUsername()), PageRequest.of(page, size));
        } catch (IllegalArgumentException e) {
            log.error("Wrong parameters " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        return image.map(this::convertToImageDto);
    }

    @Override
    public Page<ImagePreviewDto> findPaginatedByNameOrDescription(int page, int size, String name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String username = userDetails.getUsername();
        if (roles.contains("ROLE_admin")) {
            username = null;
        }
        Page<ImagePreviewDto> images;
        try {
            images = getImageByNameOrDescriptionCriteria(page, size, name, username);
        } catch (IllegalArgumentException e) {
            log.error("Wrong parameters " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blogi parametrai");
        }
        return images;
    }

    @Override
    @Transactional
    public ImageFullDto findByUuid(String uuid) {
        Image image = imageRepository.findByUuid(uuid);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Set<TagDto> tagDtos = getTagDtos(image);
        ImageFullDto imageFullDto = new ImageFullDto(image.getName(), image.getDate(),
                image.getDescription(), uuid, tagDtos);
        imageFullDto.setId(image.getId());
        return imageFullDto;
    }

    private ImagePreviewDto convertToImageDto(Image image) {
        return new ImagePreviewDto(image.getName(), image.getDescription(), image.getUuid());
    }

    @Override
    @Transactional
    public int updateImage(ImageFullDto imageFullDto) {
        Set<Tag> tags = getTagsFromImageFullDto(imageFullDto);

        Image image = imageRepository.getById(imageFullDto.getId());
        image.setName(imageFullDto.getName());
        image.setDate(imageFullDto.getDate());
        image.setDescription(imageFullDto.getDescription());
        image.setTags(tags);

        return  imageRepository.save(image).getId();
    }

    @Override
    @Transactional
    public int saveImage(ImageFullDto imageFullDto) {
        Set<Tag> tags = getTagsFromImageFullDto(imageFullDto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername());

        Image image = new Image(user, imageFullDto.getName(), imageFullDto.getDate(), imageFullDto.getDescription(),
                imageFullDto.getUuid(), tags);
        return imageRepository.save(image).getId();
    }

    private Set<Tag> getTagsFromImageFullDto(ImageFullDto imageFullDto) {
        List<String> tagNames = imageFullDto.getTags()
                .stream()
                .map(TagDto::getName)
                .collect(Collectors.toList());

        Set<Tag> tagsFromDb = tagRepository.getByNameIn(tagNames);

        List<String> tagNamesFromDb = tagsFromDb
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toList());


        for (String tagName : tagNames) {
            if (!tagNamesFromDb.contains(tagName)) {
                tagsFromDb.add(new Tag(tagName));
            }
        }
        return tagsFromDb;
    }


    @Override
    @Transactional
    public void deleteImage(int id) {
        try {
            Image imageToDelete = imageRepository.findById(id);
            imageRepository.deleteById(id);
            File imageFile = new File(IMAGE_PATH + imageToDelete.getUuid());
            File imageThumbnailFile = new File(IMAGE_PATH + imageToDelete.getUuid() + "small");
            if (imageFile.canWrite() && imageThumbnailFile.canWrite()) {
                deleteImageFile(imageFile, "imageFile");
                deleteImageFile(imageThumbnailFile, "imageThumbnailFile");
            } else {
                throw new ImageNotDeletedRuntimeException("Can't write to imageFile or imageThumbnailFile");
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("There's no such entry in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nera duombazej");
        }
    }

    private void deleteImageFile(File imageFile, String fileTypeName) {
        if (imageFile.delete()) {
            log.info(fileTypeName + " deleted");
        } else {
            throw new ImageNotDeletedRuntimeException(fileTypeName + " not deleted");
        }
    }

    private Set<TagDto> getTagDtos(Image image) {
        Set<TagDto> tagDtos = new HashSet<>();
        for (Tag tag : image.getTags()) {
            tagDtos.add(new TagDto(tag.getName()));
        }
        return tagDtos;
    }

}
