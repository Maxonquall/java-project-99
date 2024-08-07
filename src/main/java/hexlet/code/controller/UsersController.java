package hexlet.code.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;




import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;


@RestController
@RequestMapping("/api")
public class UsersController {
    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> index() {
        var users = repository.findAll();
        var result = users.stream()
                .map(userMapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(result);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        var user = userMapper.map(userData);
        repository.save(user);
        return userMapper.map(user);
    }

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(value = "@userService.findById(#id).getEmail() == authentication.name")
    public UserDTO update(@RequestBody UserUpdateDTO userData, @PathVariable Long id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        userMapper.update(userData, user);
        repository.save(user);
        var userDTO = userMapper.map(user);
        return userDTO;
    }

    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        return userMapper.map(user);
    }

    @DeleteMapping(path = "/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(value = "@userService.findById(#id).getEmail() == authentication.name")
    public void delete(@PathVariable Long id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        repository.delete(user);
    }
}


