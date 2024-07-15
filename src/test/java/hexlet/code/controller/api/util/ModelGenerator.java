package hexlet.code.controller.api.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.HashSet;


@Getter
@Component
public class ModelGenerator {

    private Model<User> userModel;

    private Model<TaskStatus> taskStatusModel;

    private Model<Task> taskModel;

    private Model<Label> labelModel;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private  TaskStatusRepository taskStatusRepository;

    @Autowired
    private Faker faker;

    @PostConstruct
    private void init() {
        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password())
                .toModel();

        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug() + faker.internet().slug())
                .supply(Select.field(TaskStatus::getName), () -> faker.lorem().word() + faker.lorem().word())
                .toModel();

        taskModel = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getTaskStatus))
                .supply(Select.field(Task::getName), () -> faker.lorem().word() + faker.lorem().sentence())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomNumber())
                .supply(Select.field(Task::getLabels), () -> new HashSet<Label>())
                .toModel();

        labelModel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> faker.lorem().word() + faker.lorem().sentence())
                .supply(Select.field(Label::getTasks), () -> new ArrayList<Task>())
                .toModel();

    }

    @Bean
    public Task getTestTask() {
        var testTask = Instancio.of(getTaskModel())
                .create();

        var testUser = Instancio.of(getUserModel())
                .create();
        userRepository.save(testUser);
        testTask.setAssignee(testUser);

        var testTaskStatus = Instancio.of(getTaskStatusModel())
                .create();
        taskStatusRepository.save(testTaskStatus);
        testTask.setTaskStatus(testTaskStatus);

        var testLabel = Instancio.of(getLabelModel())
                .create();
        labelRepository.save(testLabel);
        testTask.getLabels().add(testLabel);
        testLabel.getTasks().add(testTask);

        return testTask;
    }

}
