package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.AppUserDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AppUserService {
    AppUserDTO createUser(AppUserDTO userDTO);
    List<AppUserDTO> getAllUsers();
    AppUserDTO getUserById(Long id);
    AppUserDTO updateUser(Long id, AppUserDTO userDTO);
    void deleteUser(Long id);
    // Ajoutez cette méthode à votre interface
    Page<AppUserDTO> getPaginatedAndSearchedUsers(String keyword, int page, int size, String sortBy, String direction);

}
