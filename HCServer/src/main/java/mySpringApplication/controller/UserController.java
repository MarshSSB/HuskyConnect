package mySpringApplication.controller;


import mySpringApplication.model.User;
import mySpringApplication.security.Authenticator;
import mySpringApplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;


    /**
     * gets the data of a user in json form
     * @param username username of user that is being gotten
     * @param tokenId tokenId that is required to authenticated being logged in
     * @return user data in json form
     */
    @GetMapping("/{username}")
    public ResponseEntity<User> get(@PathVariable String username, @RequestParam String tokenId) {
        //Authentication
        if(Authenticator.getAccess( Long.parseLong(tokenId) ) == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            User user = userService.getUser(username);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * attempts to start a session with the user
     * @param username name of the user passed as a RequestParam
     * @param password password of the user passed as a RequestParam
     * @return the tokenId of the session or null if login failed
     */
    @PostMapping("/login")
    public ResponseEntity<Long> login(@RequestParam String username, @RequestParam String password){
        try{
            long tokenId = userService.login(username, password);
            return new ResponseEntity<>(tokenId,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * adds a new user to the database
     * @param user the user being added
     */
    @PostMapping("/")
    public ResponseEntity<?> add(@RequestBody User user) {
        System.out.println(user.getUsername());
        if(userService.getUser(user.getUsername()) == null){
            userService.saveUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

    }

    /**
     *
     * @param user updates user in database
     * @param tokenId token for user
     * @return http status
     */
    @PutMapping("")
    public ResponseEntity<?> update(@RequestBody User user, @RequestParam String tokenId) {
        //Authentication
        if(Authenticator.getAccess( Long.parseLong(tokenId) ) == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            user.setUsername(Objects.requireNonNull(Authenticator.getAccess(Long.parseLong(tokenId))).getUsername());
            userService.saveUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes user from database
     * @param tokenId token for user
     * @return http status
     */
    @DeleteMapping("")
    public ResponseEntity<?> delete(@RequestParam String tokenId) {
        //Authentication
        if(Authenticator.getAccess( Long.parseLong(tokenId) ) == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userService.deleteUser(Authenticator.getAccess( Long.parseLong(tokenId) ));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}