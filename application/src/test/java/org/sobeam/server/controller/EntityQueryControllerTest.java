/**
 * Copyright © 2016-2024 The Sobeam Authors
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.sobeam.common.util.JacksonUtil;
import org.sobeam.server.common.data.Customer;
import org.sobeam.server.common.data.Dashboard;
import org.sobeam.server.common.data.DataConstants;
import org.sobeam.server.common.data.Device;
import org.sobeam.server.common.data.EntityType;
import org.sobeam.server.common.data.StringUtils;
import org.sobeam.server.common.data.Tenant;
import org.sobeam.server.common.data.User;
import org.sobeam.server.common.data.alarm.Alarm;
import org.sobeam.server.common.data.alarm.AlarmSeverity;
import org.sobeam.server.common.data.id.DeviceId;
import org.sobeam.server.common.data.id.EntityId;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.query.AlarmCountQuery;
import org.sobeam.server.common.data.query.DeviceTypeFilter;
import org.sobeam.server.common.data.query.DynamicValue;
import org.sobeam.server.common.data.query.DynamicValueSourceType;
import org.sobeam.server.common.data.query.EntityCountQuery;
import org.sobeam.server.common.data.query.EntityData;
import org.sobeam.server.common.data.query.EntityDataPageLink;
import org.sobeam.server.common.data.query.EntityDataQuery;
import org.sobeam.server.common.data.query.EntityDataSortOrder;
import org.sobeam.server.common.data.query.EntityKey;
import org.sobeam.server.common.data.query.EntityKeyType;
import org.sobeam.server.common.data.query.EntityListFilter;
import org.sobeam.server.common.data.query.EntityTypeFilter;
import org.sobeam.server.common.data.query.FilterPredicateValue;
import org.sobeam.server.common.data.query.KeyFilter;
import org.sobeam.server.common.data.query.NumericFilterPredicate;
import org.sobeam.server.common.data.query.RelationsQueryFilter;
import org.sobeam.server.common.data.query.StringFilterPredicate;
import org.sobeam.server.common.data.query.TsValue;
import org.sobeam.server.common.data.queue.QueueStats;
import org.sobeam.server.common.data.relation.EntityRelation;
import org.sobeam.server.common.data.relation.EntitySearchDirection;
import org.sobeam.server.common.data.relation.RelationEntityTypeFilter;
import org.sobeam.server.common.data.security.Authority;
import org.sobeam.server.dao.queue.QueueStatsService;
import org.sobeam.server.dao.service.DaoSqlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DaoSqlTest
public class EntityQueryControllerTest extends AbstractControllerTest {

    private static final String CUSTOMER_USER_EMAIL = "entityQueryCustomer@sobeam.org";
    private static final String TENANT_PASSWORD = "testPassword1";
    private static final String CUSTOMER_USER_PASSWORD = "customer";
    private static final String TENANT_EMAIL = "entityQueryTenant@sobeam.org";

    private Tenant savedTenant;
    private User tenantAdmin;

    @Autowired
    private QueueStatsService queueStatsService;

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
        tenantAdmin.setEmail(TENANT_EMAIL);
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        tenantAdmin = createUserAndLogin(tenantAdmin, TENANT_PASSWORD);
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();
        deleteTenant(savedTenant.getId());
    }

    @Test
    public void testTenantCountEntitiesByQuery() throws Exception {
        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }
        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        EntityCountQuery countQuery = new EntityCountQuery(filter);

        Long count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        filter.setDeviceTypes(List.of("unknown"));
        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(0, count.longValue());

        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("Device1");

        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(11, count.longValue());

        EntityListFilter entityListFilter = new EntityListFilter();
        entityListFilter.setEntityType(EntityType.DEVICE);
        entityListFilter.setEntityList(devices.stream().map(Device::getId).map(DeviceId::toString).collect(Collectors.toList()));

        countQuery = new EntityCountQuery(entityListFilter);

        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        EntityTypeFilter filter2 = new EntityTypeFilter();
        filter2.setEntityType(EntityType.DEVICE);

        EntityCountQuery countQuery2 = new EntityCountQuery(filter2);

        Long count2 = doPostWithResponse("/api/entitiesQuery/count", countQuery2, Long.class);
        Assert.assertEquals(97, count2.longValue());
    }

    @Test
    public void testSysAdminCountEntitiesByQuery() throws Exception {
        loginSysAdmin();

        EntityTypeFilter allDeviceFilter = new EntityTypeFilter();
        allDeviceFilter.setEntityType(EntityType.DEVICE);
        EntityCountQuery query = new EntityCountQuery(allDeviceFilter);
        Long initialCount = doPostWithResponse("/api/entitiesQuery/count", query, Long.class);

        loginTenantAdmin();

        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }
        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceType("default");
        filter.setDeviceNameFilter("");

        loginSysAdmin();

        EntityCountQuery countQuery = new EntityCountQuery(filter);

        Long count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        filter.setDeviceType("unknown");
        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(0, count.longValue());

        filter.setDeviceType("default");
        filter.setDeviceNameFilter("Device1");

        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(11, count.longValue());

        EntityListFilter entityListFilter = new EntityListFilter();
        entityListFilter.setEntityType(EntityType.DEVICE);
        entityListFilter.setEntityList(devices.stream().map(Device::getId).map(DeviceId::toString).collect(Collectors.toList()));

        countQuery = new EntityCountQuery(entityListFilter);

        count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        Long count2 = doPostWithResponse("/api/entitiesQuery/count", query, Long.class);
        Assert.assertEquals(initialCount + 97, count2.longValue());
    }

    @Test
    public void testTenantCountAlarmsByQuery() throws Exception {
        loginTenantAdmin();
        List<Device> devices = new ArrayList<>();
        List<Alarm> alarms = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }

        for (int i = 0; i < devices.size(); i++) {
            Alarm alarm = new Alarm();
            alarm.setOriginator(devices.get(i).getId());
            alarm.setType("alarm" + i);
            alarm.setSeverity(AlarmSeverity.WARNING);
            alarms.add(doPost("/api/alarm", alarm, Alarm.class));
            Thread.sleep(1);
        }
        testCountAlarmsByQuery(alarms);
    }

    @Test
    public void testCustomerCountAlarmsByQuery() throws Exception {
        loginTenantAdmin();
        List<Device> devices = new ArrayList<>();
        List<Alarm> alarms = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setCustomerId(customerId);
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }

        loginCustomerUser();

        for (int i = 0; i < devices.size(); i++) {
            Alarm alarm = new Alarm();
            alarm.setCustomerId(customerId);
            alarm.setOriginator(devices.get(i).getId());
            alarm.setType("alarm" + i);
            alarm.setSeverity(AlarmSeverity.WARNING);
            alarms.add(doPost("/api/alarm", alarm, Alarm.class));
            Thread.sleep(1);
        }
        testCountAlarmsByQuery(alarms);
    }

    private void testCountAlarmsByQuery(List<Alarm> alarms) throws Exception {
        AlarmCountQuery countQuery = new AlarmCountQuery();

        Long count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .typeList(List.of("unknown"))
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(0, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .typeList(List.of("alarm1", "alarm2", "alarm3"))
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(3, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .typeList(alarms.stream().map(Alarm::getType).collect(Collectors.toList()))
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .severityList(List.of(AlarmSeverity.CRITICAL))
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(0, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .severityList(List.of(AlarmSeverity.WARNING))
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        long startTs = alarms.stream().map(Alarm::getCreatedTime).min(Long::compareTo).get();
        long endTs = alarms.stream().map(Alarm::getCreatedTime).max(Long::compareTo).get();

        countQuery = AlarmCountQuery.builder()
                .startTs(startTs - 1)
                .endTs(endTs + 1)
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .startTs(0)
                .endTs(endTs + 1)
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .startTs(0)
                .endTs(System.currentTimeMillis())
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());

        countQuery = AlarmCountQuery.builder()
                .startTs(endTs + 1)
                .endTs(System.currentTimeMillis())
                .build();

        count = doPostWithResponse("/api/alarmsQuery/count", countQuery, Long.class);
        Assert.assertEquals(0, count.longValue());
    }

    @Test
    public void testSimpleFindEntityDataByQuery() throws Exception {
        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }

        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));

        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, null, null);

        PageData<EntityData> data =
                doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
                });

        Assert.assertEquals(97, data.getTotalElements());
        Assert.assertEquals(10, data.getTotalPages());
        Assert.assertTrue(data.hasNext());
        Assert.assertEquals(10, data.getData().size());

        List<EntityData> loadedEntities = new ArrayList<>(data.getData());
        while (data.hasNext()) {
            query = query.next();
            data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
            });
            loadedEntities.addAll(data.getData());
        }
        Assert.assertEquals(97, loadedEntities.size());

        List<EntityId> loadedIds = loadedEntities.stream().map(EntityData::getEntityId).collect(Collectors.toList());
        List<EntityId> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());

        Assert.assertEquals(deviceIds, loadedIds);

        List<String> loadedNames = loadedEntities.stream().map(entityData ->
                entityData.getLatest().get(EntityKeyType.ENTITY_FIELD).get("name").getValue()).collect(Collectors.toList());
        List<String> deviceNames = devices.stream().map(Device::getName).collect(Collectors.toList());

        Assert.assertEquals(deviceNames, loadedNames);

        sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "name"), EntityDataSortOrder.Direction.DESC
        );

        pageLink = new EntityDataPageLink(10, 0, "device1", sortOrder);
        query = new EntityDataQuery(filter, pageLink, entityFields, null, null);
        data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
        });
        Assert.assertEquals(11, data.getTotalElements());
        Assert.assertEquals("Device19", data.getData().get(0).getLatest().get(EntityKeyType.ENTITY_FIELD).get("name").getValue());


        EntityTypeFilter filter2 = new EntityTypeFilter();
        filter2.setEntityType(EntityType.DEVICE);

        EntityDataSortOrder sortOrder2 = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink2 = new EntityDataPageLink(10, 0, null, sortOrder2);
        List<EntityKey> entityFields2 = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));

        EntityDataQuery query2 = new EntityDataQuery(filter2, pageLink2, entityFields2, null, null);

        PageData<EntityData> data2 =
                doPostWithTypedResponse("/api/entitiesQuery/find", query2, new TypeReference<PageData<EntityData>>() {
                });

        Assert.assertEquals(97, data2.getTotalElements());
        Assert.assertEquals(10, data2.getTotalPages());
        Assert.assertTrue(data2.hasNext());
        Assert.assertEquals(10, data2.getData().size());

    }

    @Test
    public void testFindEntityDataByQueryWithNegateParam() throws Exception {
        List<Device> devices = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            Device device = new Device();
            device.setName("Device" + i);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device", device, Device.class));
            Thread.sleep(1);
        }

        Device mainDevice = new Device();
        mainDevice.setName("Main device");
        mainDevice = doPost("/api/device", mainDevice, Device.class);

        for (int i = 0; i < 10; i++) {
            EntityRelation relation = createFromRelation(mainDevice, devices.get(i), "CONTAINS");
            doPost("/api/relation", relation).andExpect(status().isOk());
        }

        for (int i = 10; i < 97; i++) {
            EntityRelation relation = createFromRelation(mainDevice, devices.get(i), "NOT_CONTAINS");
            doPost("/api/relation", relation).andExpect(status().isOk());
        }

        RelationsQueryFilter filter = new RelationsQueryFilter();
        filter.setRootEntity(mainDevice.getId());
        filter.setDirection(EntitySearchDirection.FROM);
        filter.setNegate(true);
        filter.setFilters(List.of(new RelationEntityTypeFilter("CONTAINS", List.of(EntityType.DEVICE), false)));

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));

        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, null, null);

        PageData<EntityData> data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<>() {});

        Assert.assertEquals(87, data.getTotalElements());

        filter.setFilters(List.of(new RelationEntityTypeFilter("NOT_CONTAINS", List.of(EntityType.DEVICE), false)));
        query = new EntityDataQuery(filter, pageLink, entityFields, null, null);
        data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<>() {});
        Assert.assertEquals(10, data.getTotalElements());

        filter.setFilters(List.of(new RelationEntityTypeFilter("NOT_CONTAINS", List.of(EntityType.DEVICE), true)));
        query = new EntityDataQuery(filter, pageLink, entityFields, null, null);
        data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<>() {});
        Assert.assertEquals(87, data.getTotalElements());
    }

    private EntityRelation createFromRelation(Device mainDevice, Device device, String relationType) {
        return new EntityRelation(mainDevice.getId(), device.getId(), relationType);
    }

    @Test
    public void testFindEntityDataByQueryWithAttributes() throws Exception {
        List<Device> devices = new ArrayList<>();
        List<Long> temperatures = new ArrayList<>();
        List<Long> highTemperatures = new ArrayList<>();
        for (int i = 0; i < 67; i++) {
            Device device = new Device();
            String name = "Device" + i;
            device.setName(name);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));
            devices.add(doPost("/api/device?accessToken=" + name, device, Device.class));
            Thread.sleep(1);
            long temperature = (long) (Math.random() * 100);
            temperatures.add(temperature);
            if (temperature > 45) {
                highTemperatures.add(temperature);
            }
        }
        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get(i);
            String payload = "{\"temperature\":" + temperatures.get(i) + "}";
            doPost("/api/plugins/telemetry/" + device.getId() + "/" + DataConstants.SHARED_SCOPE, payload, String.class, status().isOk());
        }
        Thread.sleep(1000);

        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));
        List<EntityKey> latestValues = Collections.singletonList(new EntityKey(EntityKeyType.ATTRIBUTE, "temperature"));

        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, latestValues, null);
        PageData<EntityData> data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
        });

        List<EntityData> loadedEntities = new ArrayList<>(data.getData());
        while (data.hasNext()) {
            query = query.next();
            data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
            });
            loadedEntities.addAll(data.getData());
        }
        Assert.assertEquals(67, loadedEntities.size());

        List<String> loadedTemperatures = loadedEntities.stream().map(entityData ->
                entityData.getLatest().get(EntityKeyType.ATTRIBUTE).get("temperature").getValue()).collect(Collectors.toList());
        List<String> deviceTemperatures = temperatures.stream().map(aLong -> Long.toString(aLong)).collect(Collectors.toList());
        Assert.assertEquals(deviceTemperatures, loadedTemperatures);

        pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        KeyFilter highTemperatureFilter = new KeyFilter();
        highTemperatureFilter.setKey(new EntityKey(EntityKeyType.ATTRIBUTE, "temperature"));
        NumericFilterPredicate predicate = new NumericFilterPredicate();
        predicate.setValue(FilterPredicateValue.fromDouble(45));
        predicate.setOperation(NumericFilterPredicate.NumericOperation.GREATER);
        highTemperatureFilter.setPredicate(predicate);
        List<KeyFilter> keyFilters = Collections.singletonList(highTemperatureFilter);

        query = new EntityDataQuery(filter, pageLink, entityFields, latestValues, keyFilters);

        data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
        });
        loadedEntities = new ArrayList<>(data.getData());
        while (data.hasNext()) {
            query = query.next();
            data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
            });
            loadedEntities.addAll(data.getData());
        }
        Assert.assertEquals(highTemperatures.size(), loadedEntities.size());

        List<String> loadedHighTemperatures = loadedEntities.stream().map(entityData ->
                entityData.getLatest().get(EntityKeyType.ATTRIBUTE).get("temperature").getValue()).collect(Collectors.toList());
        List<String> deviceHighTemperatures = highTemperatures.stream().map(aLong -> Long.toString(aLong)).collect(Collectors.toList());

        Assert.assertEquals(deviceHighTemperatures, loadedHighTemperatures);
    }

    @Test
    public void testFindEntityDataByQueryWithDynamicValue() throws Exception {
        int numOfDevices = 2;

        for (int i = 0; i < numOfDevices; i++) {
            Device device = new Device();
            String name = "Device" + i;
            device.setName(name);
            device.setType("default");
            device.setLabel("testLabel" + (int) (Math.random() * 1000));

            Device savedDevice1 = doPost("/api/device?accessToken=" + name, device, Device.class);
            JsonNode content = JacksonUtil.toJsonNode("{\"alarmActiveTime\": 1" + i + "}");
            doPost("/api/plugins/telemetry/" + EntityType.DEVICE.name() + "/" + savedDevice1.getUuidId() + "/SERVER_SCOPE", content)
                    .andExpect(status().isOk());
        }
        JsonNode content = JacksonUtil.toJsonNode("{\"dynamicValue\": 0}");
        doPost("/api/plugins/telemetry/" + EntityType.TENANT.name() + "/" + tenantId.getId() + "/SERVER_SCOPE", content)
                .andExpect(status().isOk());


        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        KeyFilter highTemperatureFilter = new KeyFilter();
        highTemperatureFilter.setKey(new EntityKey(EntityKeyType.SERVER_ATTRIBUTE, "alarmActiveTime"));
        NumericFilterPredicate predicate = new NumericFilterPredicate();

        DynamicValue<Double> dynamicValue =
                new DynamicValue<>(DynamicValueSourceType.CURRENT_TENANT, "dynamicValue");
        FilterPredicateValue<Double> predicateValue = new FilterPredicateValue<>(0.0, null, dynamicValue);

        predicate.setValue(predicateValue);
        predicate.setOperation(NumericFilterPredicate.NumericOperation.GREATER);
        highTemperatureFilter.setPredicate(predicate);

        List<KeyFilter> keyFilters = Collections.singletonList(highTemperatureFilter);


        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);

        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));
        List<EntityKey> latestValues = Collections.singletonList(new EntityKey(EntityKeyType.ATTRIBUTE, "alarmActiveTime"));

        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, latestValues, keyFilters);

        Awaitility.await()
                .alias("data by query")
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> {
                    var data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {});
                    var loadedEntities = new ArrayList<>(data.getData());
                    return loadedEntities.size() == numOfDevices;
                });

        var data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {});
        var loadedEntities = new ArrayList<>(data.getData());

        Assert.assertEquals(numOfDevices, loadedEntities.size());

        for (int i = 0; i < numOfDevices; i++) {
            var entity = loadedEntities.get(i);
            String name = entity.getLatest().get(EntityKeyType.ENTITY_FIELD).getOrDefault("name", new TsValue(0, "Invalid")).getValue();
            String alarmActiveTime = entity.getLatest().get(EntityKeyType.ATTRIBUTE).getOrDefault("alarmActiveTime", new TsValue(0, "-1")).getValue();

            Assert.assertEquals("Device" + i, name);
            Assert.assertEquals("1" + i, alarmActiveTime);
        }
    }

    @Test
    public void givenInvalidEntityDataPageLink_thenReturnError() throws Exception {
        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        String invalidSortProperty = "created(Time)";
        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, invalidSortProperty), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));
        List<EntityKey> latestValues = Collections.singletonList(new EntityKey(EntityKeyType.ATTRIBUTE, "temperature"));
        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, latestValues, null);

        ResultActions result = doPost("/api/entitiesQuery/find", query).andExpect(status().isBadRequest());
        assertThat(getErrorMessage(result)).contains("Invalid").contains("sort property");
    }

    @Test
    public void testFindQueueStatsEntitiesByQuery() throws Exception {
        List<QueueStats> queueStatsList = new ArrayList<>();
        for (int i = 0; i < 97; i++) {
            QueueStats queueStats = new QueueStats();
            queueStats.setQueueName(StringUtils.randomAlphabetic(5));
            queueStats.setServiceId(StringUtils.randomAlphabetic(5));
            queueStats.setTenantId(savedTenant.getTenantId());
            queueStatsList.add(queueStatsService.save(savedTenant.getId(), queueStats));
            Thread.sleep(1);
        }

        EntityTypeFilter entityTypeFilter = new EntityTypeFilter();
        entityTypeFilter.setEntityType(EntityType.QUEUE_STATS);

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "queueName"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Arrays.asList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"), new EntityKey(EntityKeyType.ENTITY_FIELD, "queueName"),
                new EntityKey(EntityKeyType.ENTITY_FIELD, "serviceId"));

        EntityDataQuery query = new EntityDataQuery(entityTypeFilter, pageLink, entityFields, null, null);

        PageData<EntityData> data =
                doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
                });

        Assert.assertEquals(97, data.getTotalElements());
        Assert.assertEquals(10, data.getTotalPages());
        Assert.assertTrue(data.hasNext());
        Assert.assertEquals(10, data.getData().size());
        data.getData().forEach(entityData -> {
            String queueName = entityData.getLatest().get(EntityKeyType.ENTITY_FIELD).get("queueName").getValue();
            String serviceId = entityData.getLatest().get(EntityKeyType.ENTITY_FIELD).get("serviceId").getValue();
            assertThat(queueName).isNotBlank();
            assertThat(serviceId).isNotBlank();
            assertThat(entityData.getLatest().get(EntityKeyType.ENTITY_FIELD).get("name").getValue()).isEqualTo(queueName + "_" + serviceId);

        });

        EntityCountQuery countQuery = new EntityCountQuery(entityTypeFilter);

        Long count = doPostWithResponse("/api/entitiesQuery/count", countQuery, Long.class);
        Assert.assertEquals(97, count.longValue());
    }

    @Test
    public void testFindDevicesCountByOwnerNameAndOwnerType() throws Exception {
        loginTenantAdmin();
        int numOfDevices = 8;

        for (int i = 0; i < numOfDevices; i++) {
            Device device = new Device();
            String name = "Device" + i;
            device.setName(name);
            device.setType("default");

            Device savedDevice = doPost("/api/device?accessToken=" + name, device, Device.class);
            JsonNode content = JacksonUtil.toJsonNode("{\"alarmActiveTime\": 1" + i + "}");
            doPost("/api/plugins/telemetry/" + EntityType.DEVICE.name() + "/" + savedDevice.getUuidId() + "/SERVER_SCOPE", content)
                    .andExpect(status().isOk());
        }

        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        KeyFilter activeAlarmTimeFilter = getServerAttributeNumericGreaterThanKeyFilter("alarmActiveTime", 5);
        KeyFilter activeAlarmTimeToLongFilter = getServerAttributeNumericGreaterThanKeyFilter("alarmActiveTime", 30);
        KeyFilter tenantOwnerNameFilter = getEntityFieldStringEqualToKeyFilter("ownerName", TEST_TENANT_NAME);
        KeyFilter wrongOwnerNameFilter = getEntityFieldStringEqualToKeyFilter("ownerName", "wrongName");
        KeyFilter tenantOwnerTypeFilter =  getEntityFieldStringEqualToKeyFilter("ownerType", "TENANT");
        KeyFilter customerOwnerTypeFilter = getEntityFieldStringEqualToKeyFilter("ownerType", "CUSTOMER");

        // all devices with ownerName = TEST TENANT
        EntityCountQuery query = new EntityCountQuery(filter,  List.of(activeAlarmTimeFilter, tenantOwnerNameFilter));
        checkEntitiesCount(query, numOfDevices);

        // all devices with ownerName = TEST TENANT
        EntityCountQuery activeAlarmTimeToLongQuery = new EntityCountQuery(filter,  List.of(activeAlarmTimeToLongFilter, tenantOwnerNameFilter));
        checkEntitiesCount(activeAlarmTimeToLongQuery, 0);

        // all devices with wrong ownerName
        EntityCountQuery wrongTenantNameQuery = new EntityCountQuery(filter, List.of(activeAlarmTimeFilter, wrongOwnerNameFilter));
        checkEntitiesCount(wrongTenantNameQuery, 0);

        // all devices with owner type = TENANT
        EntityCountQuery tenantEntitiesQuery = new EntityCountQuery(filter, List.of(activeAlarmTimeFilter, tenantOwnerTypeFilter));
        checkEntitiesCount(tenantEntitiesQuery, numOfDevices);

        // all devices with owner type = CUSTOMER
        EntityCountQuery customerEntitiesQuery = new EntityCountQuery(filter, List.of(activeAlarmTimeFilter, customerOwnerTypeFilter));
        checkEntitiesCount(customerEntitiesQuery, 0);
    }

    @Test
    public void testFindDevicesByOwnerNameAndOwnerType() throws Exception {
        loginTenantAdmin();
        int numOfDevices = 3;

        for (int i = 0; i < numOfDevices; i++) {
            Device device = new Device();
            String name = "Device" + i;
            device.setName(name);
            device.setType("default");

            Device savedDevice = doPost("/api/device?accessToken=" + name, device, Device.class);
            JsonNode content = JacksonUtil.toJsonNode("{\"alarmActiveTime\": 1" + i + "}");
            doPost("/api/plugins/telemetry/" + EntityType.DEVICE.name() + "/" + savedDevice.getUuidId() + "/SERVER_SCOPE", content)
                    .andExpect(status().isOk());
        }

        DeviceTypeFilter filter = new DeviceTypeFilter();
        filter.setDeviceTypes(List.of("default"));
        filter.setDeviceNameFilter("");

        KeyFilter activeAlarmTimeFilter = getServerAttributeNumericGreaterThanKeyFilter("alarmActiveTime", 5);
        KeyFilter tenantOwnerNameFilter = getEntityFieldStringEqualToKeyFilter("ownerName", TEST_TENANT_NAME);
        KeyFilter wrongOwnerNameFilter = getEntityFieldStringEqualToKeyFilter("ownerName", "wrongName");
        KeyFilter tenantOwnerTypeFilter =  getEntityFieldStringEqualToKeyFilter("ownerType", "TENANT");
        KeyFilter customerOwnerTypeFilter = getEntityFieldStringEqualToKeyFilter("ownerType", "CUSTOMER");

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC
        );
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = List.of(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"), new EntityKey(EntityKeyType.ENTITY_FIELD, "ownerName"),
                new EntityKey(EntityKeyType.ENTITY_FIELD, "ownerType"));
        List<EntityKey> latestValues = Collections.singletonList(new EntityKey(EntityKeyType.ATTRIBUTE, "alarmActiveTime"));

        // all devices with ownerName = TEST TENANT
        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, latestValues, List.of(activeAlarmTimeFilter, tenantOwnerNameFilter));
        checkEntitiesByQuery(query, numOfDevices, TEST_TENANT_NAME, "TENANT");

        // all devices with wrong ownerName
        EntityDataQuery wrongTenantNameQuery = new EntityDataQuery(filter, pageLink, entityFields, latestValues, List.of(activeAlarmTimeFilter, wrongOwnerNameFilter));
        checkEntitiesByQuery(wrongTenantNameQuery, 0, null, null);

        // all devices with owner type = TENANT
        EntityDataQuery tenantEntitiesQuery = new EntityDataQuery(filter, pageLink, entityFields, latestValues, List.of(activeAlarmTimeFilter, tenantOwnerTypeFilter));
        checkEntitiesByQuery(tenantEntitiesQuery, numOfDevices, TEST_TENANT_NAME, "TENANT");

        // all devices with owner type = CUSTOMER
        EntityDataQuery customerEntitiesQuery = new EntityDataQuery(filter, pageLink, entityFields, latestValues, List.of(activeAlarmTimeFilter, customerOwnerTypeFilter));
        checkEntitiesByQuery(customerEntitiesQuery, 0, null, null);
    }

    @Test
    public void testFindCustomerDashboards() throws Exception {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("My dashboard");
        Dashboard savedDashboard = doPost("/api/dashboard", dashboard, Dashboard.class);

        Customer customer = new Customer();
        customer.setTitle("My customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        //assign dashboard
        doPost("/api/customer/" + savedCustomer.getId().getId().toString()
                + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        // check entity data query by customer
        User customerUser = new User();
        customerUser.setAuthority(Authority.CUSTOMER_USER);
        customerUser.setTenantId(savedTenant.getId());
        customerUser.setCustomerId(savedCustomer.getId());
        customerUser.setEmail(CUSTOMER_USER_EMAIL);

        createUserAndLogin(customerUser, CUSTOMER_USER_PASSWORD);

        EntityTypeFilter filter = new EntityTypeFilter();
        filter.setEntityType(EntityType.DASHBOARD);

        EntityDataSortOrder sortOrder = new EntityDataSortOrder(
                new EntityKey(EntityKeyType.ENTITY_FIELD, "createdTime"), EntityDataSortOrder.Direction.ASC);
        EntityDataPageLink pageLink = new EntityDataPageLink(10, 0, null, sortOrder);
        List<EntityKey> entityFields = Collections.singletonList(new EntityKey(EntityKeyType.ENTITY_FIELD, "name"));

        EntityDataQuery query = new EntityDataQuery(filter, pageLink, entityFields, null, null);

        PageData<EntityData> data =
                doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
                });

        Assert.assertEquals(1, data.getTotalElements());
        Assert.assertEquals(1, data.getTotalPages());
        Assert.assertEquals(1, data.getData().size());

        // unnassign dashboard
        login(TENANT_EMAIL, TENANT_PASSWORD);
        doDelete("/api/customer/" + savedCustomer.getId().getId().toString() + "/dashboard/" + savedDashboard.getId().getId().toString(), Dashboard.class);

        login(CUSTOMER_USER_EMAIL, CUSTOMER_USER_PASSWORD);
        PageData<EntityData> dataAfterUnassign =
                doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {
                });

        Assert.assertEquals(0, dataAfterUnassign.getTotalElements());
        Assert.assertEquals(0, dataAfterUnassign.getTotalPages());
        Assert.assertEquals(0, dataAfterUnassign.getData().size());
    }

    private void checkEntitiesByQuery(EntityDataQuery query, int expectedNumOfDevices, String expectedOwnerName, String expectedOwnerType) throws Exception {
        Awaitility.await()
                .alias("data by query")
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    var data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {});
                    var loadedEntities = new ArrayList<>(data.getData());
                    return loadedEntities.size() == expectedNumOfDevices;
                });
         if (expectedNumOfDevices == 0) {
             return;
         }
        var data = doPostWithTypedResponse("/api/entitiesQuery/find", query, new TypeReference<PageData<EntityData>>() {});
        var loadedEntities = new ArrayList<>(data.getData());

        Assert.assertEquals(expectedNumOfDevices, loadedEntities.size());

        for (int i = 0; i < expectedNumOfDevices; i++) {
            var entity = loadedEntities.get(i);
            String name = entity.getLatest().get(EntityKeyType.ENTITY_FIELD).getOrDefault("name", new TsValue(0, "Invalid")).getValue();
            String ownerName = entity.getLatest().get(EntityKeyType.ENTITY_FIELD).getOrDefault("ownerName", new TsValue(0, "Invalid")).getValue();
            String ownerType = entity.getLatest().get(EntityKeyType.ENTITY_FIELD).getOrDefault("ownerType", new TsValue(0, "Invalid")).getValue();
            String alarmActiveTime = entity.getLatest().get(EntityKeyType.ATTRIBUTE).getOrDefault("alarmActiveTime", new TsValue(0, "-1")).getValue();

            Assert.assertEquals("Device" + i, name);
            Assert.assertEquals( expectedOwnerName, ownerName);
            Assert.assertEquals( expectedOwnerType, ownerType);
            Assert.assertEquals("1" + i, alarmActiveTime);
        }
    }

    private void checkEntitiesCount(EntityCountQuery query, int expectedNumOfDevices) {
        Awaitility.await()
                .alias("count by query")
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    var count = doPost("/api/entitiesQuery/count", query, Integer.class);
                    return count == expectedNumOfDevices;
                });
   }

    private KeyFilter getEntityFieldStringEqualToKeyFilter(String keyName, String value) {
        KeyFilter tenantOwnerNameFilter = new KeyFilter();
        tenantOwnerNameFilter.setKey(new EntityKey(EntityKeyType.ENTITY_FIELD, keyName));
        StringFilterPredicate ownerNamePredicate = new StringFilterPredicate();
        ownerNamePredicate.setValue(FilterPredicateValue.fromString(value));
        ownerNamePredicate.setOperation(StringFilterPredicate.StringOperation.EQUAL);
        tenantOwnerNameFilter.setPredicate(ownerNamePredicate);
        return tenantOwnerNameFilter;
    }

    private KeyFilter getServerAttributeNumericGreaterThanKeyFilter(String attribute, int value) {
        KeyFilter numericFilter = new KeyFilter();
        numericFilter.setKey(new EntityKey(EntityKeyType.SERVER_ATTRIBUTE, attribute));
        NumericFilterPredicate predicate = new NumericFilterPredicate();
        predicate.setValue(FilterPredicateValue.fromDouble(value));
        predicate.setOperation(NumericFilterPredicate.NumericOperation.GREATER);
        numericFilter.setPredicate(predicate);
        return numericFilter;
    }

}
