package hexlet.code.component;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CustomUserDetailsService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var admin = new User();
        var faker = new Faker();

        admin.setFirstName(faker.name().firstName());
        admin.setLastName(faker.name().lastName());
        admin.setEmail("hexlet@example.com");
        admin.setPasswordDigest("qwerty");
        userService.createUser(admin);


    }
//        var email = "hexlet@example.com";
//        var userData = new User();
//        userData.setEmail(email);
//        userData.setPasswordDigest("qwerty");
//        userService.createUser(userData);
//
//        var user = userRepository.findByEmail(email).get();
//
//        var faker = new Faker();
//        IntStream.range(1, 10).forEach(i -> {
//           // var user = new User();
//            user.setFirstName(faker.name().firstName());
//            user.setLastName(faker.name().lastName());
//            userRepository.save(user);
//        });
//    }
}
