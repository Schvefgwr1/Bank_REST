package com.example.bankcards.service;


import com.example.bankcards.dto.api.users.DeleteUserResponse;
import com.example.bankcards.dto.api.users.UpdateUserRequest;
import com.example.bankcards.dto.api.users.UserRequest;
import com.example.bankcards.dto.api.users.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.custom_exceptions.*;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserResponse createUser(UserRequest request) throws RoleException, DatabaseException, UpdateEntityException {
        Role roleUser = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RoleException(request.getRoleName()));
        try {
            if(userRepository.existsByLogin(request.getLogin())) {
                throw new UpdateEntityException("User", "login", request.getLogin());
            } else {
                User savedUser = userRepository.save(User.builder()
                        .login(request.getLogin())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(roleUser)
                        .build()
                );
                return UserResponse.builder()
                        .id(savedUser.getId())
                        .role(savedUser.getRole().getName())
                        .login(savedUser.getLogin())
                        .build();
            }
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public Page<UserResponse> getUsers(Pageable pageable) throws DatabaseException {
        try {
            return userRepository.findAll(pageable).map(user -> UserResponse.builder()
                    .id(user.getId())
                    .login(user.getLogin())
                    .role(user.getRole().getName())
                    .build()
            );
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    public UserResponse updateUser(UpdateUserRequest request, Long userId)
    throws RoleException, DatabaseException, UserException, UpdateEntityException {
        try {
            User ourUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException(userId));

            if (request.getRoleName() != null) {
                Role roleNewUser = roleRepository.findByName(request.getRoleName())
                        .orElseThrow(() -> new RoleException(request.getRoleName()));

                ourUser.setRole(roleNewUser);
            }

            if(request.getLogin() != null) {
                if(userRepository.existsByLogin(request.getLogin())) {
                    throw new UpdateEntityException("User", "login", request.getLogin());
                } else {
                    ourUser.setLogin(request.getLogin());
                }
            }

            if (request.getPassword() != null) {
                ourUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            User savedUser = userRepository.save(ourUser);
            return UserResponse.builder()
                    .id(savedUser.getId())
                    .login(savedUser.getLogin())
                    .role(savedUser.getRole().getName())
                    .build();
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public DeleteUserResponse deleteUser(Long userId) throws UserException, DatabaseException {
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                return DeleteUserResponse.builder()
                        .userID(userId)
                        .message("Successfully deleted")
                        .build();
            } else {
                throw new UserException(userId);
            }
        } catch (PersistenceException | DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
