package es.udc.anpaproject.backend.test.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.ActivityType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.entities.User.RoleType;
import es.udc.anpaproject.backend.model.services.ActivityService;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @MockBean
    private ActivityRepository activityRepository;

    @MockBean
    private UserRepository userRepository;


    /* =========================== HELPERS =========================== */

    private ActivityDto createDto() {
        ActivityDto dto = new ActivityDto();
        dto.setTitle("Fútbol");
        dto.setDescription("Entrenamiento semanal");
        dto.setStartDate(LocalDate.of(2025, 1, 10));
        dto.setEndDate(LocalDate.of(2025, 5, 10));
        dto.setPrice(20.0);
        dto.setType(ActivityType.ACTIVITY);
        dto.setImageUrl(null);
        return dto;
    }

    private User admin() {
        User u = new User();
        u.setId(1L);
        u.setRole(RoleType.ADMIN);
        return u;
    }

    private User normalUser() {
        User u = new User();
        u.setId(2L);
        u.setRole(RoleType.USER);
        return u;
    }


    /* =========================== CREATE =========================== */

    @Test
    public void testCreateActivityWithoutImage() throws Exception {

        ActivityDto dto = createDto();

        // mock usuario creador
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));

        // mock save() generando ID automáticamente
        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(invocation -> {
                    Activity a = invocation.getArgument(0);
                    ReflectionTestUtils.setField(a, "id", 10L); // simular ID de BD
                    return a;
                });

        ActivityDto result = activityService.createActivity(dto, 1L, null);

        assertEquals(10L, result.getId());
        assertEquals("Fútbol", result.getTitle());
        assertEquals(20.0, result.getPrice());
    }


    @Test
    public void testCreateActivityWithImage() throws Exception {

        ActivityDto dto = createDto();

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "foto.jpg",
                "image/jpeg",
                "contenido".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));

        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(invocation -> {
                    Activity a = invocation.getArgument(0);
                    ReflectionTestUtils.setField(a, "id", 20L);
                    return a;
                });

        ActivityDto result = activityService.createActivity(dto, 1L, file);

        assertEquals(20L, result.getId());
        assertEquals("Fútbol", result.getTitle());
    }


    /* =========================== UPDATE =========================== */

    @Test
    public void testUpdateActivityAsAdmin() {

        Activity existing = new Activity();
        ReflectionTestUtils.setField(existing, "id", 30L);

        when(activityRepository.findById(30L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        ActivityDto dto = createDto();
        dto.setTitle("Actualizado");

        ActivityDto result = activityService.updateActivity(30L, dto, 1L);

        assertEquals("Actualizado", result.getTitle());
    }


    @Test
    public void testUpdateActivityAsUserForbidden() {

        Activity existing = new Activity();
        ReflectionTestUtils.setField(existing, "id", 30L);

        when(activityRepository.findById(30L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser()));

        ActivityDto dto = createDto();

        assertThrows(RuntimeException.class, () ->
                activityService.updateActivity(30L, dto, 2L)
        );
    }


    /* =========================== DELETE =========================== */

    @Test
    public void testDeleteActivityAsAdmin() {

        Activity existing = new Activity();
        ReflectionTestUtils.setField(existing, "id", 40L);

        when(activityRepository.findById(40L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin()));

        assertDoesNotThrow(() ->
                activityService.deleteActivity(40L, 1L)
        );
    }


    @Test
    public void testDeleteActivityAsUserForbidden() {

        Activity existing = new Activity();
        ReflectionTestUtils.setField(existing, "id", 40L);

        when(activityRepository.findById(40L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser()));

        assertThrows(RuntimeException.class, () ->
                activityService.deleteActivity(40L, 2L)
        );
    }
}
