package com.example.bankcards.service;


import com.example.bankcards.dto.api.users.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private final Role testUserRole = Role.builder().id(1L).name("USER").permissions(List.of()).build();
    private final Role testAdminRole = Role.builder().id(1L).name("ADMIN").permissions(List.of()).build();
    private final User testUser = User.builder().id(1L).login("user123").password("encoded").role(testUserRole).build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_success() throws Exception {
        UserRequest request = new UserRequest("user123", "pass", "USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testUserRole));
        when(userRepository.existsByLogin("user123")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.createUser(request);

        assertEquals("user123", response.getLogin());
        assertEquals("USER", response.getRole());
    }

    @Test
    void createUser_alreadyExists() {
        UserRequest request = new UserRequest("user123", "pass", "USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testUserRole));
        when(userRepository.existsByLogin("user123")).thenReturn(true);

        assertThrows(UpdateEntityException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_roleNotFound_shouldThrowRoleException() {
        UserRequest request = new UserRequest("user123", "pass", "ADMIN");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(RoleException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_databaseException_shouldThrowDatabaseException() {
        UserRequest request = new UserRequest("user123", "pass", "USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testUserRole));
        when(userRepository.existsByLogin("user123")).thenThrow(new DataAccessResourceFailureException("Simulated DB error"));

        assertThrows(DatabaseException.class, () -> userService.createUser(request));
    }

    @Test
    void getUsers_success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("user123", result.getContent().get(0).getLogin());
    }

    @Test
    void getUsers_databaseException_shouldThrowDatabaseException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenThrow(PersistenceException.class);

        assertThrows(DatabaseException.class, () -> userService.getUsers(pageable));
    }

    @Test
    void updateUser_success() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("newLogin", "newPass", "ADMIN");
        User updatedUser = User.builder().id(1L).login("newLogin").password("encodedNew").role(testAdminRole).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testAdminRole));
        when(userRepository.existsByLogin("newLogin")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse response = userService.updateUser(request, 1L);

        assertEquals("newLogin", response.getLogin());
        assertEquals("ADMIN", response.getRole());
        assertEquals(1L, response.getId());

    }

    @Test
    void updateUser_userNotFound() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userService.updateUser(request, 1L));
    }

    @Test
    void updateUser_roleNotFound_shouldThrowRoleException() {
        UpdateUserRequest request = new UpdateUserRequest(null, null, "NEW_ROLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());

        assertThrows(RoleException.class, () -> userService.updateUser(request, 1L));
    }

    @Test
    void updateUser_loginAlreadyExists_shouldThrowUpdateEntityException() {
        UpdateUserRequest request = new UpdateUserRequest("newLogin", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByLogin("newLogin")).thenReturn(true);

        assertThrows(UpdateEntityException.class, () -> userService.updateUser(request, 1L));
    }

    @Test
    void updateUser_databaseException_shouldThrowDatabaseException() {
        UpdateUserRequest request = new UpdateUserRequest(null, "newPass", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(userRepository.save(any())).thenThrow(new DataAccessResourceFailureException("Simulated DB error"));

        assertThrows(DatabaseException.class, () -> userService.updateUser(request, 1L));
    }

    @Test
    void deleteUser_success() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        DeleteUserResponse response = userService.deleteUser(1L);

        assertEquals(1L, response.getUserID());
        assertEquals("Successfully deleted", response.getMessage());
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_databaseException_shouldThrowDatabaseException() {
        when(userRepository.existsById(1L)).thenThrow(PersistenceException.class);

        assertThrows(DatabaseException.class, () -> userService.deleteUser(1L));
    }
}
