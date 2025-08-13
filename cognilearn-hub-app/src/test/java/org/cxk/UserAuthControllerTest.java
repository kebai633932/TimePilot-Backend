//package org.cxk;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.cxk.infrastructure.adapter.dao.po.User;
//import org.cxk.infrastructure.adapter.repository.UserRepository;
//import org.cxk.service.impl.UserAuthServiceImpl;
//import org.cxk.trigger.dto.UserDeleteDTO;
//import org.cxk.trigger.dto.UserLoginDTO;
//import org.cxk.trigger.dto.UserRegisterDTO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
////@WebMvcTest(UserAuthController.class)
////只加载和 Web 层相关的 Bean，如：
////你的 UserAuthController
////@ControllerAdvice（异常处理）
////MVC 配置（如拦截器、参数绑定器）
////MockMvc（提供模拟 HTTP 请求的能力）
////
////💡不会加载 Service、Repository、数据库连接等 Bean，所以你需要用 @MockBean 来模拟它们。
////单独测试Controller	@WebMvcTest	     仅 Web 层
////集成测试完整应用	    @SpringBootTest	 全部 Bean
//@SpringBootTest(classes = Application.class)
//@AutoConfigureMockMvc
////@Transactional// 测试一结束，自动 rollback，不影响其他测试
//public class UserAuthControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
////    @MockBean
//    @Autowired
//    private UserAuthServiceImpl userAuthService; // 必须手动 mock，否则注入失败
//    @Autowired
//    private UserRepository userRepository; // ✅ 你自己的 Mapper/Repository，用于查询数据库
//    @Test
//    @WithMockUser // 模拟一个已认证用户，避免 401
//    public void testRegisterSuccess() throws Exception {
//        //1.构造请求参数
//        UserRegisterDTO registerDTO = new UserRegisterDTO();
//        registerDTO.setUsername("CrazyXiaoKe");
//        registerDTO.setPassword("CrazyXiaoKe@13326932249");
//
//        // 模拟 userAuthService.register() 正常执行（不抛异常），@MockBean
////        Mockito.doNothing().when(userAuthService).register(Mockito.any(UserRegisterDTO.class));
//
//        //2.使用 Jackson 的 ObjectMapper 将 registerDTO 对象序列化成 JSON 字符串
//        String json = new ObjectMapper().writeValueAsString(registerDTO);
//
//        //3.使用 MockMvc 模拟发起一个 POST 请求到 "/api/auth/register" 接口
//        // 设置请求类型为 application/json，并携带前面构造的 JSON 请求体
//        mockMvc.perform(post("/api/user/auth/register")
//                        .with(csrf()) // ✅ 加上这句,在测试中添加 CSRF token
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                // 期望返回状态码为 200（即 HTTP OK）
//                .andExpect(status().isOk())
//                // 期望响应体中 JSON 的 message 字段为 "注册成功"
//                .andExpect(jsonPath("$.info").value("调用成功"))
//                // 期望响应体中 JSON 的 code 字段为 200（自定义业务成功码）
//                .andExpect(jsonPath("$.code").value("0000"));
//        //4.查询数据库是否真的插入
//        User user = userRepository.findByUsername(registerDTO.getUsername());
//        assertEquals(registerDTO.getUsername(), user.getUsername());
//        assertNotNull(user.getId()); // TinyId 分配成功
//        assertNotNull(user.getPassword()); // 密码加密成功
//    }
//
//    @Test
//    @WithMockUser // 模拟一个已认证用户，避免 401
//    //注册失败，用户名不规范
//    public void testRegisterFail() throws Exception {
//        //1.构造请求参数
//        UserRegisterDTO registerDTO = new UserRegisterDTO();
//        registerDTO.setUsername("CrazyXiaoKe");
//        registerDTO.setPassword("CrazyXiaoKe");
//
//        // 模拟 userAuthService.register() 正常执行（不抛异常），@MockBean
////        Mockito.doNothing().when(userAuthService).register(Mockito.any(UserRegisterDTO.class));
//
//        //2.使用 Jackson 的 ObjectMapper 将 registerDTO 对象序列化成 JSON 字符串
//        String json = new ObjectMapper().writeValueAsString(registerDTO);
//
//        //3.使用 MockMvc 模拟发起一个 POST 请求到 "/api/auth/register" 接口
//        // 设置请求类型为 application/json，并携带前面构造的 JSON 请求体
//        mockMvc.perform(post("/api/user/auth/register")
//                        .with(csrf()) // ✅ 加上这句,在测试中添加 CSRF token
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                // 期望返回状态码为 200（即 HTTP OK）
//                .andExpect(status().isOk())
//                // 期望响应体中 JSON 的 message 字段为 "注册成功"
//                .andExpect(jsonPath("$.info").value("调用失败"))
//                // 期望响应体中 JSON 的 code 字段为 200（自定义业务成功码）
//                .andExpect(jsonPath("$.code").value("0001"));
//    }
//
//    @Test
//    @WithMockUser // 模拟一个已认证用户，避免 401
//    public void testLoginSuccess() throws Exception {
//        //1.构造请求参数
//        UserLoginDTO userLoginDTO = new UserLoginDTO();
//        userLoginDTO.setUsername("test123");
//        userLoginDTO.setPassword("test123");
////        registerDTO.setUsername("test");
////        registerDTO.setPassword("test123456");
//
//        // 模拟 userAuthService.register() 正常执行（不抛异常），@MockBean
////        Mockito.doNothing().when(userAuthService).register(Mockito.any(UserRegisterDTO.class));
//
//        //2.使用 Jackson 的 ObjectMapper 将 registerDTO 对象序列化成 JSON 字符串
//        String json = new ObjectMapper().writeValueAsString(userLoginDTO);
//
//        //3.使用 MockMvc 模拟发起一个 POST 请求到 "/api/auth/register" 接口
//        // 设置请求类型为 application/json，并携带前面构造的 JSON 请求体
//        mockMvc.perform(post("/api/user/auth/login")
//                        .with(csrf()) // ✅ 加上这句,在测试中添加 CSRF token
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                // 期望返回状态码为 200（即 HTTP OK）
//                .andExpect(status().isOk())
//                // 期望响应体中 JSON 的 message 字段为 "注册成功"
//                .andExpect(jsonPath("$.info").value("调用成功"))
//                // 期望响应体中 JSON 的 code 字段为 200（自定义业务成功码）
//                .andExpect(jsonPath("$.code").value("0000"));
//    }
//
//    @Test
//    @WithMockUser
//    public void testDeleteUserSuccess() throws Exception {
//        // 准备阶段：先注册一个用户
//        String username = "delete_test_user";
//
//        UserRegisterDTO registerDTO = new UserRegisterDTO();
//        registerDTO.setUsername(username);
//        registerDTO.setPassword("password123");
//
//        // 注册用户
//        String json = new ObjectMapper().writeValueAsString(registerDTO);
//        mockMvc.perform(post("/api/user/auth/register")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value("0000"));
//
//        // 删除用户
//        UserDeleteDTO deleteDTO=new UserDeleteDTO();
//        deleteDTO.setUsername(username);
//        String jsonDelete = new ObjectMapper().writeValueAsString(deleteDTO);
//        mockMvc.perform(post("/api/user/auth/delete")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonDelete))
//                // 期望返回状态码为 200（即 HTTP OK）
//                .andExpect(status().isOk())
//                // 期望响应体中 JSON 的 message 字段为 "注册成功"
//                .andExpect(jsonPath("$.info").value("调用成功"))
//                // 期望响应体中 JSON 的 code 字段为 200（自定义业务成功码）
//                .andExpect(jsonPath("$.code").value("0000"));
//
//    }
//
//}
//
