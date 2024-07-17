package hexlet.code.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.controller.api.util.ModelGenerator;
import hexlet.code.repository.UserRepository;
import net.javacrumbs.jsonunit.core.Option;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;

import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

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
        testTask = Instancio.of(modelGenerator.getTaskModel())
                .create();
        taskRepository.save(testTask);

    }

    @AfterEach
    public void clear() {
    //    taskRepository.deleteAll();
     //   taskStatusRepository.deleteAll();
        //    userRepository.deleteAll();
     //   taskRepository.delete(testTask);
    }


    @Test
    public void testIndex() throws Exception {
        var request = get("/api/tasks")
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var tasks = om.readValue(body, new TypeReference<List<Task>>() { });
        var expected = taskRepository.findAll();

        assertThat(tasks).containsAll(expected);
    }


    @Test
    public void testCreate() throws Exception {
        var data = new HashMap<>();
        data.put("title", testTaskCreate.getName());
        data.put("index", testTaskCreate.getIndex());
        data.put("content", testTaskCreate.getDescription());

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
        assertThat(task.getIndex()).isEqualTo(testTaskCreate.getIndex());
    }

    @Test
    public void testUpdate() throws Exception {
        var oldDescription = testTask.getDescription();
        var newDescription = faker.lorem().sentence();

        var data = new HashMap<>();
        data.put("content", newDescription);

        var tasksCount = taskRepository.count();

        token = jwt().jwt(builder -> builder.subject(oldDescription));

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskRepository.count()).isEqualTo(tasksCount);

        var task = taskRepository.findByName(testTask.getName()).get();

        assertThat(task.getDescription()).isEqualTo(newDescription);
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(taskRepository.findByDescription(oldDescription)).isEmpty();
        assertThat(taskRepository.findByDescription(newDescription)).get().isEqualTo(task);
    }

    @Test
    public void testDestroy() throws Exception {
        var tasksCount = taskRepository.count();

        token = jwt().jwt(builder -> builder.subject(testTask.getName()));

        mockMvc.perform(delete("/api/tasks/" + testTask.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.count()).isEqualTo(tasksCount - 1);
        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    public void testFilter() throws Exception {
        var titleCont = testTask.getName().substring(1).toLowerCase();
        var assigneeId = testTask.getAssignee().getId();
        var status = testTask.getTaskStatus().getSlug();
        var labelId = testTask.getLabels().iterator().next().getId();

        var wrongTask = modelGenerator.getTestTask();
        taskRepository.save(wrongTask);

        var request = get("/api/tasks"
                + "?"
                + "name=" + titleCont
                + "&assigneeId=" + assigneeId
                + "&status=" + status
                + "&labelId=" + labelId)
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var data = new HashMap<>();
        data.put("assignee_id", testTask.getAssignee().getId());
        data.put("content", testTask.getDescription());
        data.put("createdAt", testTask.getCreatedAt());
        data.put("id", testTask.getId());
        data.put("index", testTask.getIndex());
        data.put("status", testTask.getTaskStatus().getSlug());
        data.put("title", testTask.getName());
        data.put("taskLabelIds", List.of(labelId));

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).when(Option.IGNORING_ARRAY_ORDER)
                .isArray()
                .contains(om.writeValueAsString(data));
    }
}
