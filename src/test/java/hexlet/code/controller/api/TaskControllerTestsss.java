package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.controller.api.util.ModelGenerator;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import net.datafaker.Faker;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTestsss {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper om;

    private JwtRequestPostProcessor token;

    private Task testTask;

    private Task testTaskCreate;

    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        // Создаём TaskStatus
        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("New Status");
        testTaskStatus.setSlug("new-status"); // Установка значения для slug
        taskStatusRepository.save(testTaskStatus);

        testTask = new Task();
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskStatus(testTaskStatus);
        taskRepository.save(testTask);

        testTaskCreate = new Task();
        testTaskCreate.setName("New Task");
        testTaskCreate.setDescription("New Description");
        testTaskCreate.setTaskStatus(testTaskStatus);
    }

    @AfterEach
    public void clean() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }



    @Test
    public void testIndex() throws Exception {
        var request = get("/api/tasks").with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var tasks = om.readValue(body, new TypeReference<List<Task>>() {});
        var expected = taskRepository.findAll();

        assertThat(tasks).containsAll(expected);
    }



    @Test
    public void testCreate() throws Exception {
        var data = new HashMap<>();
        data.put("name", testTaskCreate.getName());
        data.put("description", testTaskCreate.getDescription());
        data.put("taskStatus", testTaskStatus.getId()); // Устанавливаем taskStatus

        var tasksCount = taskRepository.count();

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(tasksCount + 1);

        var task = taskRepository.findByName(testTaskCreate.getName()).get();

        assertNotNull(task);
        assertThat(taskRepository.findByName(testTaskCreate.getName())).isPresent();

        assertThat(task.getName()).isEqualTo(testTaskCreate.getName());
        assertThat(task.getDescription()).isEqualTo(testTaskCreate.getDescription());
        assertThat(task.getTaskStatus()).isEqualTo(testTaskStatus);
    }


    @Test
    public void testUpdate() throws Exception {
        var oldDescription = testTask.getDescription();
        var newDescription = faker.lorem().sentence();

        var data = new HashMap<>();
        data.put("description", newDescription);
        data.put("taskStatus", testTaskStatus.getId()); // Устанавливаем taskStatus

        var tasksCount = taskRepository.count();

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskRepository.count()).isEqualTo(tasksCount);

        var task = taskRepository.findById(testTask.getId()).get();

        assertThat(task.getDescription()).isEqualTo(newDescription);
        assertThat(task.getTaskStatus()).isEqualTo(testTaskStatus);
    }


    @Test
    public void testDestroy() throws Exception {
        var tasksCount = taskRepository.count();

        var request = delete("/api/tasks/" + testTask.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.count()).isEqualTo(tasksCount - 1);
        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    public void testFilter() throws Exception {
        var titleCont = testTask.getName().substring(1).toLowerCase();
        var assigneeId = testTask.getAssignee() != null ? testTask.getAssignee().getId() : null;
        var status = testTask.getTaskStatus().getId();
        var labelId = testTask.getLabels().isEmpty() ? null : testTask.getLabels().iterator().next().getId();

        var wrongTask = new Task();
        wrongTask.setName("Wrong Task");
        wrongTask.setDescription("Wrong Description");
        wrongTask.setTaskStatus(testTaskStatus); // Устанавливаем taskStatus для wrongTask
        taskRepository.save(wrongTask);

        var request = get("/api/tasks"
                + "?"
                + "name=" + titleCont
                + (assigneeId != null ? "&assigneeId=" + assigneeId : "")
                + "&status=" + status
                + (labelId != null ? "&labelId=" + labelId : ""))
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).when(Option.IGNORING_ARRAY_ORDER)
                .isArray()
                .contains(om.writeValueAsString(testTask));
    }
}
