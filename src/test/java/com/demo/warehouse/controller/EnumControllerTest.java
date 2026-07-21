package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnumController.class)
class EnumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = TestFactory.createDefaultTenant();
        user = TestFactory.createDefaultUser(tenant);
        when(userRepository.findByAuth0Sub(anyString())).thenReturn(java.util.Optional.of(user));
        TestFactory.setUserContextHolder(user);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @WithMockUser
    void getAssignableRoles_SuperAdmin_ShouldReturnAllRoles() throws Exception {
        user.setRole(UserRole.SUPERADMIN);
        TestFactory.setUserContextHolder(user);

        mockMvc.perform(get("/enums/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("SUPERADMIN"))
                .andExpect(jsonPath("$[1]").value("RESELLER"))
                .andExpect(jsonPath("$[2]").value("ADMIN"))
                .andExpect(jsonPath("$[3]").value("USER"));
    }

    @Test
    @WithMockUser
    void getAssignableRoles_Reseller_ShouldReturnAdminAndUser() throws Exception {
        user.setRole(UserRole.RESELLER);
        TestFactory.setUserContextHolder(user);

        mockMvc.perform(get("/enums/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("USER"));
    }

    @Test
    @WithMockUser
    void getAssignableRoles_Admin_ShouldReturnAdminAndUser() throws Exception {
        user.setRole(UserRole.ADMIN);
        TestFactory.setUserContextHolder(user);

        mockMvc.perform(get("/enums/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("USER"));
    }

    @Test
    @WithMockUser
    void getAssignableRoles_User_ShouldReturnEmpty() throws Exception {
        user.setRole(UserRole.USER);
        TestFactory.setUserContextHolder(user);

        mockMvc.perform(get("/enums/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void getAssignableModules_ShouldReturnTenantModules() throws Exception {
        tenant.setModules(Set.of(ModuleType.DRESS, ModuleType.DRESS_MOVEMENT));
        TestFactory.setUserContextHolder(user);

        mockMvc.perform(get("/enums/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
