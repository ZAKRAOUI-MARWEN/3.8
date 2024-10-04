/**
 * Copyright © 2024 The Sobeam Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sobeam.server.controller;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.sobeam.common.util.JacksonUtil;
import org.sobeam.server.common.data.Customer;
import org.sobeam.server.common.data.Dashboard;
import org.sobeam.server.common.data.DashboardInfo;
import org.sobeam.server.common.data.DeviceProfile;
import org.sobeam.server.common.data.ShortCustomerInfo;
import org.sobeam.server.common.data.StringUtils;
import org.sobeam.server.common.data.Tenant;
import org.sobeam.server.common.data.User;
import org.sobeam.server.common.data.asset.AssetProfile;
import org.sobeam.server.common.data.audit.ActionType;
import org.sobeam.server.common.data.edge.Edge;
import org.sobeam.server.common.data.id.CustomerId;
import org.sobeam.server.common.data.id.DashboardId;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.common.data.security.Authority;
import org.sobeam.server.dao.dashboard.DashboardDao;
import org.sobeam.server.dao.exception.DataValidationException;
import org.sobeam.server.dao.service.DaoSqlTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {DashboardControllerTest.Config.class})
@DaoSqlTest
public class DashboardControllerTest extends AbstractControllerTest {

    private IdComparator<DashboardInfo> idComparator = new IdComparator<>();

    private Tenant savedTenant;
    private User tenantAdmin;

    @Autowired
    private DashboardDao dashboardDao;

    static class Config {
        @Bean
        @Primary
        public DashboardDao dashboardDao(DashboardDao dashboardDao) {
            return Mockito.mock(DashboardDao.class, AdditionalAnswers.delegatesTo(dashboardDao));
        }
    }

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        savedTenant = saveTenant(tenant);
        Assert.assertNotNull(savedTenant);

        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@sobeam.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();

        deleteTenant(savedTenant.getId());
    }

    @Test
    public void testSaveDashboardInfoWithViolationOfValidation() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle(StringUtils.randomAlphabetic(300));
        String msgError = msgErrorFieldLength("title");

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/dashboard", dashboard)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        dashboard.setTenantId(savedTenant.getId());
        testNotifyEntityEqualsOneTimeServiceNeverError(dashboard, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
        Mockito.reset(tbClusterService, auditLogService);
    }

    @Test
    public void testUpdateDashboardFromDifferentTenant() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        loginDifferentTenant();

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/dashboard", savedDashboard, Dashboard.class, status().isForbidden());

        testNotifyEntityNever(savedDashboard.getId(), savedDashboard);

        deleteDifferentTenant();
    }

    @Test
    public void testFindDashboardById() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertNotNull(foundDashboard);
        Assert.assertEquals(savedDashboard, foundDashboard);
    }

    @Test
    public void testDeleteDashboard() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Mockito.reset(tbClusterService, auditLogService);

        doDelete("/api/dashboard/" + savedDashboard.getId().getId().toString()).andExpect(status().isOk());

        testNotifyEntityAllOneTime(savedDashboard, savedDashboard.getId(), savedDashboard.getId(),
                savedDashboard.getTenantId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.DELETED,
                savedDashboard.getId().getId().toString());

        String dashboardIdStr = savedDashboard.getId().getId().toString();
        doGet("/api/dashboard/" + savedDashboard.getId().getId().toString())
                .andExpect(status().isNotFound())
                .andExpect(statusReason(containsString(msgErrorNoFound("Dashboard", dashboardIdStr))));
    }

    @Test
    public void testSaveDashboardWithEmptyTitle() throws Exception {
        Dashboard dashboard = new Dashboard();
        String msgError = "Dashboard title " + msgErrorShouldBeSpecified;

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/dashboard", dashboard)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString(msgError)));

        testNotifyEntityEqualsOneTimeServiceNeverError(dashboard, savedTenant.getId(),
                tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ADDED, new DataValidationException(msgError));
    }

    @Test
    public void testAssignUnassignDashboardToCustomer() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        Mockito.reset(tbClusterService, auditLogService);

        Dashboard assignedDashboard = doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(assignedDashboard.getAssignedCustomers().contains(savedCustomer.toShortCustomerInfo()));

        testNotifyEntityAllOneTimeLogEntityActionEntityEqClass(assignedDashboard, assignedDashboard.getId(), assignedDashboard.getId(),
                savedTenant.getId(), savedCustomer.getId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ASSIGNED_TO_CUSTOMER,
                ActionType.UPDATED, assignedDashboard.getId().getId().toString(), savedCustomer.getId().getId().toString(), savedCustomer.getTitle());

        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertTrue(foundDashboard.getAssignedCustomers().contains(savedCustomer.toShortCustomerInfo()));

        Mockito.reset(tbClusterService, auditLogService);

        Dashboard unassignedDashboard =
                doDelete("/api/customer/" + savedCustomer.getId().getId().toString() + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        testNotifyEntityAllOneTimeLogEntityActionEntityEqClass(assignedDashboard, assignedDashboard.getId(), assignedDashboard.getId(),
                savedTenant.getId(), savedCustomer.getId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.UNASSIGNED_FROM_CUSTOMER,
                ActionType.UPDATED, unassignedDashboard.getId().getId().toString(), savedCustomer.getId().getId().toString(), savedCustomer.getTitle());

        Assert.assertTrue(unassignedDashboard.getAssignedCustomers() == null || unassignedDashboard.getAssignedCustomers().isEmpty());

        foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(foundDashboard.getAssignedCustomers() == null || foundDashboard.getAssignedCustomers().isEmpty());
    }

    @Test
    public void testAssignUnassignDashboardToPublicCustomer() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Mockito.reset(tbClusterService, auditLogService);

        Dashboard assignedDashboard = doPost("/api/customer/public/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        CustomerId publicCustomerId = null;
        for (ShortCustomerInfo assignedCustomer : assignedDashboard.getAssignedCustomers()) {
            if (assignedCustomer.isPublic()) {
                publicCustomerId = assignedCustomer.getCustomerId();
            }
        }
        Assert.assertNotNull(publicCustomerId);
        Customer publicCustomer = doGet("/api/customer/" + publicCustomerId, Customer.class);
        Assert.assertTrue(publicCustomer.isPublic());

        testNotifyEntityAllOneTimeLogEntityActionEntityEqClass(assignedDashboard, assignedDashboard.getId(), assignedDashboard.getId(),
                savedTenant.getId(), publicCustomer.getId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ASSIGNED_TO_CUSTOMER,
                ActionType.UPDATED, assignedDashboard.getId().getId().toString(), publicCustomer.getId().getId().toString(), publicCustomer.getTitle());

        Dashboard foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);
        Assert.assertTrue(foundDashboard.getAssignedCustomers().contains(publicCustomer.toShortCustomerInfo()));

        Mockito.reset(tbClusterService, auditLogService);

        Dashboard unassignedDashboard =
                doDelete("/api/customer/public/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        testNotifyEntityAllOneTimeLogEntityActionEntityEqClass(assignedDashboard, assignedDashboard.getId(), assignedDashboard.getId(),
                savedTenant.getId(), publicCustomer.getId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.UNASSIGNED_FROM_CUSTOMER,
                ActionType.UPDATED, unassignedDashboard.getId().getId().toString(), publicCustomer.getId().getId().toString(), publicCustomer.getTitle());

        Assert.assertTrue(unassignedDashboard.getAssignedCustomers() == null || unassignedDashboard.getAssignedCustomers().isEmpty());

        foundDashboard = doGet("/api/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        Assert.assertTrue(foundDashboard.getAssignedCustomers() == null || foundDashboard.getAssignedCustomers().isEmpty());
    }

    @Test
    public void testAssignDashboardToNonExistentCustomer() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        String customerIdStr = Uuids.timeBased().toString();
        doPost("/api/customer/" + customerIdStr
                + "/dashboard/" + savedDashboard.getId().getId().toString())
                .andExpect(status().isNotFound())
                .andExpect(statusReason(containsString(msgErrorNoFound("Customer", customerIdStr))));

        Mockito.reset(tbClusterService, auditLogService);
        testNotifyEntityNever(savedDashboard.getId(), savedDashboard);
    }

    @Test
    public void testAssignDashboardToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();

        Tenant tenant2 = new Tenant();
        tenant2.setTitle("Different tenant");
        Tenant savedTenant2 = saveTenant(tenant2);
        Assert.assertNotNull(savedTenant2);

        User tenantAdmin2 = new User();
        tenantAdmin2.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin2.setTenantId(savedTenant2.getId());
        tenantAdmin2.setEmail("tenant3@sobeam.org");
        tenantAdmin2.setFirstName("Joe");
        tenantAdmin2.setLastName("Downs");

        createUserAndLogin(tenantAdmin2, "testPassword1");

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        login(tenantAdmin.getEmail(), "testPassword1");

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString())
                .andExpect(status().isForbidden())
                .andExpect(statusReason(containsString(msgErrorPermission)));

        Mockito.reset(tbClusterService, auditLogService);
        testNotifyEntityNever(savedDashboard.getId(), savedDashboard);

        doDelete("/api/tenant/" + savedTenant2.getId().getId().toString())
                .andExpect(status().isForbidden())
                .andExpect(statusReason(containsString(msgErrorPermission)));

        testNotifyEntityNever(savedDashboard.getId(), savedDashboard);

        loginSysAdmin();

        deleteTenant(savedTenant2.getId());
    }

    @Test
    public void testFindTenantDashboards() throws Exception {
        List<DashboardInfo> expectedDashboards = new ArrayList<>();
        PageLink pageLink = new PageLink(24);
        PageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                    new TypeReference<PageData<DashboardInfo>>() {
                    }, pageLink);
            expectedDashboards.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        Mockito.reset(tbClusterService, auditLogService);

        int cntEntity = 173;
        for (int i = 0; i < cntEntity; i++) {
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle("Dashboard" + i);
            expectedDashboards.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }

        testNotifyManyEntityManyTimeMsgToEdgeServiceEntityEqAny(new Dashboard(), new Dashboard(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.ADDED, cntEntity, cntEntity, cntEntity);

        List<DashboardInfo> loadedDashboards = new ArrayList<>();
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                    new TypeReference<PageData<DashboardInfo>>() {
                    }, pageLink);
            loadedDashboards.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        expectedDashboards.sort(idComparator);
        loadedDashboards.sort(idComparator);

        Assert.assertEquals(expectedDashboards, loadedDashboards);
    }

    @Test
    public void testFindTenantDashboardsByTitle() throws Exception {
        String title1 = "Dashboard title 1";
        List<DashboardInfo> dashboardsTitle1 = new ArrayList<>();
        int cntEntity = 134;
        for (int i = 0; i < cntEntity; i++) {
            Dashboard dashboard = new Dashboard();
            String suffix = StringUtils.randomAlphanumeric((int) (Math.random() * 15));
            String title = title1 + suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            dashboard.setTitle(title);
            dashboardsTitle1.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }
        String title2 = "Dashboard title 2";
        List<DashboardInfo> dashboardsTitle2 = new ArrayList<>();

        for (int i = 0; i < 112; i++) {
            Dashboard dashboard = new Dashboard();
            String suffix = StringUtils.randomAlphanumeric((int) (Math.random() * 15));
            String title = title2 + suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            dashboard.setTitle(title);
            dashboardsTitle2.add(new DashboardInfo(doPost("/api/dashboard", dashboard, Dashboard.class)));
        }

        List<DashboardInfo> loadedDashboardsTitle1 = new ArrayList<>();
        PageLink pageLink = new PageLink(15, 0, title1);
        PageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                    new TypeReference<PageData<DashboardInfo>>() {
                    }, pageLink);
            loadedDashboardsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        dashboardsTitle1.sort(idComparator);
        loadedDashboardsTitle1.sort(idComparator);

        Assert.assertEquals(dashboardsTitle1, loadedDashboardsTitle1);

        List<DashboardInfo> loadedDashboardsTitle2 = new ArrayList<>();
        pageLink = new PageLink(4, 0, title2);
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                    new TypeReference<PageData<DashboardInfo>>() {
                    }, pageLink);
            loadedDashboardsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        dashboardsTitle2.sort(idComparator);
        loadedDashboardsTitle2.sort(idComparator);

        Assert.assertEquals(dashboardsTitle2, loadedDashboardsTitle2);

        Mockito.reset(tbClusterService, auditLogService);

        for (DashboardInfo dashboard : loadedDashboardsTitle1) {
            doDelete("/api/dashboard/" + dashboard.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        testNotifyManyEntityManyTimeMsgToEdgeServiceEntityEqAnyAdditionalInfoAny(new Dashboard(), new Dashboard(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.DELETED, ActionType.DELETED, cntEntity, cntEntity, 1);

        pageLink = new PageLink(4, 0, title1);
        pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                new TypeReference<PageData<DashboardInfo>>() {
                }, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        for (DashboardInfo dashboard : loadedDashboardsTitle2) {
            doDelete("/api/dashboard/" + dashboard.getId().getId().toString())
                    .andExpect(status().isOk());
        }

        pageLink = new PageLink(4, 0, title2);
        pageData = doGetTypedWithPageLink("/api/tenant/dashboards?",
                new TypeReference<PageData<DashboardInfo>>() {
                }, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindCustomerDashboards() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId customerId = customer.getId();

        Mockito.reset(tbClusterService, auditLogService);

        int cntEntity = 173;
        List<DashboardInfo> dashboards = new ArrayList<>();
        for (int i = 0; i < cntEntity; i++) {
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle("Dashboard" + i);
            dashboard = doPost("/api/dashboard", dashboard, Dashboard.class);
            dashboards.add(new DashboardInfo(doPost("/api/customer/" + customerId.getId().toString()
                    + "/dashboard/" + dashboard.getId().getId().toString(), Dashboard.class)));
        }

        testNotifyManyEntityManyTimeMsgToEdgeServiceEntityEqAny(new Dashboard(), new Dashboard(),
                savedTenant.getId(), tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(),
                ActionType.ADDED, cntEntity, cntEntity, cntEntity * 2);

        List<DashboardInfo> loadedDashboards = new ArrayList<>();
        PageLink pageLink = new PageLink(21);
        PageData<DashboardInfo> pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId.getId().toString() + "/dashboards?",
                    new TypeReference<PageData<DashboardInfo>>() {
                    }, pageLink);
            loadedDashboards.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        dashboards.sort(idComparator);
        loadedDashboards.sort(idComparator);

        Assert.assertEquals(dashboards, loadedDashboards);
    }

    @Test
    public void testAssignDashboardToEdge() throws Exception {
        Edge edge = constructEdge("My edge", "default");
        Edge savedEdge = doPost("/api/edge", edge, Edge.class);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Mockito.reset(tbClusterService, auditLogService);

        doPost("/api/edge/" + savedEdge.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        testNotifyEntityAllOneTime(savedDashboard, savedDashboard.getId(), savedDashboard.getId(), savedTenant.getId(),
                tenantAdmin.getCustomerId(), tenantAdmin.getId(), tenantAdmin.getEmail(), ActionType.ASSIGNED_TO_EDGE,
                savedDashboard.getId().getId().toString(), savedEdge.getId().getId().toString(), savedEdge.getName());

        PageData<Dashboard> pageData = doGetTypedWithPageLink("/api/edge/" + savedEdge.getId().getId().toString() + "/dashboards?",
                new TypeReference<PageData<Dashboard>>() {
                }, new PageLink(100));

        Assert.assertEquals(1, pageData.getData().size());

        doDelete("/api/edge/" + savedEdge.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        pageData = doGetTypedWithPageLink("/api/edge/" + savedEdge.getId().getId().toString() + "/dashboards?",
                new TypeReference<PageData<Dashboard>>() {
                }, new PageLink(100));

        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testDeleteDashboardWithDeleteRelationsOk() throws Exception {
        DashboardId dashboardId = createDashboard("Dashboard for Test WithRelationsOk").getId();
        testEntityDaoWithRelationsOk(savedTenant.getId(), dashboardId, "/api/dashboard/" + dashboardId);
    }

    @Test
    public void testDeleteDashboardExceptionWithRelationsTransactional() throws Exception {
        DashboardId dashboardId = createDashboard("Dashboard for Test WithRelations Transactional Exception").getId();
        testEntityDaoWithRelationsTransactionalException(dashboardDao, savedTenant.getId(), dashboardId, "/api/dashboard/" + dashboardId);
    }

    @Test
    public void whenDeletingDashboard_ifReferencedByDeviceProfile_thenReturnError() throws Exception {
        Dashboard dashboard = createDashboard("test");
        DeviceProfile deviceProfile = createDeviceProfile("test");
        deviceProfile.setDefaultDashboardId(dashboard.getId());
        doPost("/api/deviceProfile", deviceProfile, DeviceProfile.class);

        String response = doDelete("/api/dashboard/" + dashboard.getUuidId()).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        String errorMessage = JacksonUtil.toJsonNode(response).get("message").asText();
        assertThat(errorMessage).containsIgnoringCase("referenced by a device profile");
    }

    @Test
    public void whenDeletingDashboard_ifReferencedByAssetProfile_thenReturnError() throws Exception {
        Dashboard dashboard = createDashboard("test");
        AssetProfile assetProfile = createAssetProfile("test");
        assetProfile.setDefaultDashboardId(dashboard.getId());
        doPost("/api/assetProfile", assetProfile, AssetProfile.class);

        String response = doDelete("/api/dashboard/" + dashboard.getUuidId()).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        String errorMessage = JacksonUtil.toJsonNode(response).get("message").asText();
        assertThat(errorMessage).containsIgnoringCase("referenced by an asset profile");

    }

    private Dashboard createDashboard(String title) {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle(title);
        return doPost("/api/dashboard", dashboard, Dashboard.class);
    }

}
