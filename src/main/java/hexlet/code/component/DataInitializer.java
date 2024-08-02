package hexlet.code.component;



import hexlet.code.model.TaskStatus;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import hexlet.code.model.User;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;

import java.util.List;


@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final LabelRepository labelRepository;

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

        var draft = new TaskStatus();
        draft.setName("draft");
        draft.setSlug("draft");

        var toReview = new TaskStatus();
        toReview.setName("to review");
        toReview.setSlug("to_review");

        var toBeFixed = new TaskStatus();
        toBeFixed.setName("to be fixed");
        toBeFixed.setSlug("to_be_fixed");

        var toPublish = new TaskStatus();
        toPublish.setName("to publish");
        toPublish.setSlug("to_publish");

        var published = new TaskStatus();
        published.setName("published");
        published.setSlug("published");

        var defaultTaskStatuses = List.of(
                draft,
                toReview,
                toBeFixed,
                toPublish,
                published
        );

        var feature = new Label();
        feature.setName("feature");

        var bug = new Label();
        bug.setName("bug");

        var defaultLabels = List.of(
                feature,
                bug
        );

        for (var taskStatus : defaultTaskStatuses) {
            taskStatusRepository.save(taskStatus);
        }

        for (var label : defaultLabels) {
            labelRepository.save(label);
        }
    }
}
