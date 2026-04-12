/*package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.entities.User.RoleType;
import es.udc.anpaproject.backend.rest.dtos.ActivityConversor;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path uploadDir = Paths.get("uploads");

    public ActivityDto createActivity(ActivityDto dto, Long userId, MultipartFile image) throws IOException {

        // 1. Usuario creador
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. DTO -> entidad
        Activity activity = ActivityConversor.toActivityEntity(dto);
        activity.setCreatedBy(creator);

        // 3. Si viene imagen, la guardamos y fijamos imageUrl
        if (image != null && !image.isEmpty()) {

            // Crear carpeta /uploads si no existe
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Nombre de archivo único
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            // Ruta final
            Path filePath = uploadDir.resolve(fileName);

            // Guardar fichero
            Files.copy(image.getInputStream(), filePath);

            // URL que guardamos en BD
            activity.setImageUrl("/uploads/" + fileName);
        }

        // 4. Guardar actividad
        Activity saved = activityRepository.save(activity);

        // 5. Devolver DTO
        return ActivityConversor.toActivityDto(saved);
    }


    public List<ActivityDto> findAll() {
        return ActivityConversor.toActivityDtos(activityRepository.findAll());
    }

    public ActivityDto findById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        return ActivityConversor.toActivityDto(activity);
    }

    
    public ActivityDto updateActivity(Long id, ActivityDto dto, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("No tienes permiso para editar actividades");
        }

        ActivityConversor.merge(activity, dto);
        Activity updated = activityRepository.save(activity);
        return ActivityConversor.toActivityDto(updated);
    }

    public void deleteActivity(Long id, Long userId) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != RoleType.ADMIN) {
            throw new RuntimeException("No tienes permiso para eliminar actividades");
        }

        activityRepository.delete(activity);
    }

    public static List<ActivityDto> toActivityDtos(List<Activity> activities) {
        return activities.stream()
                .map(ActivityConversor::toActivityDto)
                .toList();
    }
}
*/