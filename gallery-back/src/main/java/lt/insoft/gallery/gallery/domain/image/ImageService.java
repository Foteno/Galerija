package lt.insoft.gallery.gallery.domain.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lt.insoft.gallery.Image;
import lt.insoft.gallery.ImageFullDto;
import lt.insoft.gallery.ImagePreviewDto;
import lt.insoft.gallery.Image_;
import lt.insoft.gallery.Tag;
import lt.insoft.gallery.TagDto;
import lt.insoft.gallery.Tag_;
import lt.insoft.gallery.gallery.domain.constants.Constants;
import lt.insoft.gallery.gallery.domain.exceptions.ImageNotDeletedRuntimeException;
import lt.insoft.gallery.gallery.domain.tag.TagRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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

    @PersistenceContext
    private EntityManager entityManager;

    private Specification<Image> tagsLike(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.upper(root.join(Image_.TAGS).get(Tag_.NAME)),
                        "%"+ name.toUpperCase(Locale.ROOT) + "%");
    }

    /*private List<Image> getImagesByTag(String name) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
        Root<Image> root = criteriaQuery.from(Image.class);

        criteriaQuery.multiselect(root);
        criteriaQuery.where(builder.like(builder.upper(root.join(Image_.TAGS).get(Tag_.NAME)),
                "%"+ name.toUpperCase(Locale.ROOT) + "%"));
        TypedQuery<Tuple> typedQuery =  entityManager.createQuery(criteriaQuery);
        //typedQuery.setFirstResult()
        List<Tuple> tuples = typedQuery.getResultList();
        List<Image> images = new ArrayList<>();
        for (Tuple tuple: tuples) {
            Image image = tuple.get(root);
            System.out.println(image.getName());
            System.out.println();
            images.add(image);
        }
        return images;
    }*/

    private Page<ImagePreviewDto> getImageByNameOrDescriptionCriteria(int page, int size, String name) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
        Root<Image> root = criteriaQuery.from(Image.class);

        Predicate descriptionPredicate = builder.like(builder.upper(root.get(Image_.DESCRIPTION)),
                "%"+ name.toUpperCase(Locale.ROOT) + "%");
        Predicate namePredicate = builder.like(builder.upper(root.get(Image_.NAME)),
                "%"+ name.toUpperCase(Locale.ROOT) + "%");
        Predicate orNameDescription = builder.or(descriptionPredicate, namePredicate);

        criteriaQuery.multiselect(root);
        criteriaQuery.where(orNameDescription);

        TypedQuery<Tuple> typedQuery =  entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<Tuple> tuples = typedQuery.getResultList();
        List<Image> images = new ArrayList<>();
        for (Tuple tuple: tuples) {
            images.add(tuple.get(root));
        }

        Long totalCount;
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Image> root1 =  countQuery.from(Image.class);
        countQuery.select(builder.count(root1));
        countQuery.where(orNameDescription);
        totalCount = entityManager.createQuery(countQuery).getSingleResult();

        PageImpl<ImagePreviewDto> page1 = new PageImpl<>(images.stream().map(this::convertToImageDto).toList(),
                PageRequest.of(page, size), totalCount);

        return page1;
    }

    @Override
    public Page<ImagePreviewDto> findImageByTagUsingSpecification(int page, int size, String tag) {
        Page<Image> image = imageRepository.findAll(tagsLike(tag), PageRequest.of(page, size));
        return image.map(this::convertToImageDto);
    }

    @Override
    public Page<ImagePreviewDto> findPaginatedByNameOrDescription(int page, int size, String name) {
        Page<ImagePreviewDto> images = getImageByNameOrDescriptionCriteria(page, size, name);
        return images;
    }

    @Override
    @Transactional
    public ImageFullDto findByUuid(String uuid) {
        Image image = imageRepository.findByUuid(uuid);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Set<TagDto> set = getTagDtos(image);
        ImageFullDto imageFullDto = new ImageFullDto(image.getName(), image.getDate(),
                image.getDescription(), uuid, set);
        imageFullDto.setId(image.getId());
        return imageFullDto;
    }

    @Override
    public ImagePreviewDto convertToImageDto(Image image) {
        return new ImagePreviewDto(image.getName(), image.getDescription(), image.getUuid());
    }

    @Override
    @Transactional
    public int updateImage(ImageFullDto imageFullDto) {
        Set<Tag> tags = convertToTagFromTagDto(imageFullDto);

        Image image = imageRepository.getById(imageFullDto.getId());
        image.setName(imageFullDto.getName());
        image.setDate(imageFullDto.getDate());
        image.setDescription(imageFullDto.getDescription());
        image.setTags(tags);

        imageRepository.save(image);
        return 1;
    }

    @Override
    @Transactional
    public void saveImage(ImageFullDto imageFullDto) {
        Set<Tag> tags = convertToTagFromTagDto(imageFullDto);

        Image image = new Image(imageFullDto.getName(), imageFullDto.getDate(), imageFullDto.getDescription(),
                imageFullDto.getUuid(), tags);
        imageRepository.save(image);
    }

    private Set<Tag> convertToTagFromTagDto(ImageFullDto imageFullDto) {
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

    private void deleteImageFile(File imageFile, String s) {
        if (imageFile.delete()) {
            log.info(s + " deleted");
        } else {
            throw new ImageNotDeletedRuntimeException(s + " not deleted");
        }
    }

    @Override
    public ImageFullDto getImageById(int id) {
        Image image = imageRepository.findById(id);
        Set<TagDto> set = getTagDtos(image);
        ImageFullDto imageFullDto = new ImageFullDto(image.getName(), image.getDate(), image.getDescription(),
                image.getUuid(), set);
        imageFullDto.setId(image.getId());
        return imageFullDto;
    }

    private Set<TagDto> getTagDtos(Image image) {
        Set<TagDto> set = new HashSet<>();
        for (Tag tag : image.getTags()) {
            set.add(new TagDto(tag.getName()));
        }
        return set;
    }

}
