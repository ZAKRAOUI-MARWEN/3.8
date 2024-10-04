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

import com.google.common.util.concurrent.ListenableFuture;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.sobeam.server.common.data.Customer;
import org.sobeam.server.common.data.EntitySubtype;
import org.sobeam.server.common.data.asset.Asset;
import org.sobeam.server.common.data.asset.AssetInfo;
import org.sobeam.server.common.data.asset.AssetSearchQuery;
import org.sobeam.server.common.data.edge.Edge;
import org.sobeam.server.common.data.exception.SobeamException;
import org.sobeam.server.common.data.id.AssetId;
import org.sobeam.server.common.data.id.AssetProfileId;
import org.sobeam.server.common.data.id.CustomerId;
import org.sobeam.server.common.data.id.EdgeId;
import org.sobeam.server.common.data.id.TenantId;
import org.sobeam.server.common.data.page.PageData;
import org.sobeam.server.common.data.page.PageLink;
import org.sobeam.server.common.data.page.TimePageLink;
import org.sobeam.server.common.data.sync.ie.importing.csv.BulkImportRequest;
import org.sobeam.server.common.data.sync.ie.importing.csv.BulkImportResult;
import org.sobeam.server.config.annotations.ApiOperation;
import org.sobeam.server.dao.exception.IncorrectParameterException;
import org.sobeam.server.dao.model.ModelConstants;
import org.sobeam.server.queue.util.TbCoreComponent;
import org.sobeam.server.service.asset.AssetBulkImportService;
import org.sobeam.server.service.entitiy.asset.TbAssetService;
import org.sobeam.server.service.security.model.SecurityUser;
import org.sobeam.server.service.security.permission.Operation;
import org.sobeam.server.service.security.permission.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.sobeam.server.controller.ControllerConstants.ASSET_ID_PARAM_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.ASSET_INFO_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.ASSET_NAME_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.ASSET_PROFILE_ID_PARAM_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.ASSET_TEXT_SEARCH_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.ASSET_TYPE_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.CUSTOMER_ID_PARAM_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.EDGE_ASSIGN_ASYNC_FIRST_STEP_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.EDGE_ASSIGN_RECEIVE_STEP_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.EDGE_ID_PARAM_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.EDGE_UNASSIGN_ASYNC_FIRST_STEP_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.EDGE_UNASSIGN_RECEIVE_STEP_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.PAGE_DATA_PARAMETERS;
import static org.sobeam.server.controller.ControllerConstants.PAGE_NUMBER_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.PAGE_SIZE_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.SORT_ORDER_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.SORT_PROPERTY_DESCRIPTION;
import static org.sobeam.server.controller.ControllerConstants.TENANT_AUTHORITY_PARAGRAPH;
import static org.sobeam.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;
import static org.sobeam.server.controller.ControllerConstants.UUID_WIKI_LINK;
import static org.sobeam.server.controller.EdgeController.EDGE_ID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AssetController extends BaseController {
    private final AssetBulkImportService assetBulkImportService;
    private final TbAssetService tbAssetService;

    public static final String ASSET_ID = "assetId";

    @ApiOperation(value = "Get Asset (getAssetById)",
            notes = "Fetch the Asset object based on the provided Asset Id. " +
                    "If the user has the authority of 'Tenant Administrator', the server checks that the asset is owned by the same tenant. " +
                    "If the user has the authority of 'Customer User', the server checks that the asset is assigned to the same customer." + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH
            )
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/asset/{assetId}", method = RequestMethod.GET)
    @ResponseBody
    public Asset getAssetById(@Parameter(description = ASSET_ID_PARAM_DESCRIPTION)
                              @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(ASSET_ID, strAssetId);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        return checkAssetId(assetId, Operation.READ);
    }

    @ApiOperation(value = "Get Asset Info (getAssetInfoById)",
            notes = "Fetch the Asset Info object based on the provided Asset Id. " +
                    "If the user has the authority of 'Tenant Administrator', the server checks that the asset is owned by the same tenant. " +
                    "If the user has the authority of 'Customer User', the server checks that the asset is assigned to the same customer. "
                    + ASSET_INFO_DESCRIPTION + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/asset/info/{assetId}", method = RequestMethod.GET)
    @ResponseBody
    public AssetInfo getAssetInfoById(@Parameter(description = ASSET_ID_PARAM_DESCRIPTION)
                                      @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(ASSET_ID, strAssetId);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        return checkAssetInfoId(assetId, Operation.READ);
    }

    @ApiOperation(value = "Create Or Update Asset (saveAsset)",
            notes = "Creates or Updates the Asset. When creating asset, platform generates Asset Id as " + UUID_WIKI_LINK +
                    "The newly created Asset id will be present in the response. " +
                    "Specify existing Asset id to update the asset. " +
                    "Referencing non-existing Asset Id will cause 'Not Found' error. " +
                    "Remove 'id', 'tenantId' and optionally 'customerId' from the request body example (below) to create new Asset entity. "
                    + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/asset", method = RequestMethod.POST)
    @ResponseBody
    public Asset saveAsset(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A JSON value representing the asset.") @RequestBody Asset asset) throws Exception {
        asset.setTenantId(getTenantId());
        checkEntity(asset.getId(), asset, Resource.ASSET);
        return tbAssetService.save(asset, getCurrentUser());
    }

    @ApiOperation(value = "Delete asset (deleteAsset)",
            notes = "Deletes the asset and all the relations (from and to the asset). Referencing non-existing asset Id will cause an error." + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/asset/{assetId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteAsset(@Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws Exception {
        checkParameter(ASSET_ID, strAssetId);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        Asset asset = checkAssetId(assetId, Operation.DELETE);
        tbAssetService.delete(asset, getCurrentUser());
    }

    @ApiOperation(value = "Assign asset to customer (assignAssetToCustomer)",
            notes = "Creates assignment of the asset to customer. Customer will be able to query asset afterwards." + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/asset/{assetId}", method = RequestMethod.POST)
    @ResponseBody
    public Asset assignAssetToCustomer(@Parameter(description = CUSTOMER_ID_PARAM_DESCRIPTION) @PathVariable("customerId") String strCustomerId,
                                       @Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter("customerId", strCustomerId);
        checkParameter(ASSET_ID, strAssetId);
        CustomerId customerId = new CustomerId(toUUID(strCustomerId));
        Customer customer = checkCustomerId(customerId, Operation.READ);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        checkAssetId(assetId, Operation.ASSIGN_TO_CUSTOMER);
        return tbAssetService.assignAssetToCustomer(getTenantId(), assetId, customer, getCurrentUser());
    }

    @ApiOperation(value = "Unassign asset from customer (unassignAssetFromCustomer)",
            notes = "Clears assignment of the asset to customer. Customer will not be able to query asset afterwards." + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/asset/{assetId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Asset unassignAssetFromCustomer(@Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(ASSET_ID, strAssetId);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        Asset asset = checkAssetId(assetId, Operation.UNASSIGN_FROM_CUSTOMER);
        if (asset.getCustomerId() == null || asset.getCustomerId().getId().equals(ModelConstants.NULL_UUID)) {
            throw new IncorrectParameterException("Asset isn't assigned to any customer!");
        }
        Customer customer = checkCustomerId(asset.getCustomerId(), Operation.READ);
        return tbAssetService.unassignAssetToCustomer(getTenantId(), assetId, customer, getCurrentUser());
    }

    @ApiOperation(value = "Make asset publicly available (assignAssetToPublicCustomer)",
            notes = "Asset will be available for non-authorized (not logged-in) users. " +
                    "This is useful to create dashboards that you plan to share/embed on a publicly available website. " +
                    "However, users that are logged-in and belong to different tenant will not be able to access the asset." + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/asset/{assetId}", method = RequestMethod.POST)
    @ResponseBody
    public Asset assignAssetToPublicCustomer(@Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(ASSET_ID, strAssetId);
        AssetId assetId = new AssetId(toUUID(strAssetId));
        checkAssetId(assetId, Operation.ASSIGN_TO_CUSTOMER);
        return tbAssetService.assignAssetToPublicCustomer(getTenantId(), assetId, getCurrentUser());
    }

    @ApiOperation(value = "Get Tenant Assets (getTenantAssets)",
            notes = "Returns a page of assets owned by tenant. " +
                    PAGE_DATA_PARAMETERS + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/assets", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<Asset> getTenantAssets(
            @Parameter(description = PAGE_SIZE_DESCRIPTION)
            @RequestParam int pageSize,
            @Parameter(description = PAGE_NUMBER_DESCRIPTION)
            @RequestParam int page,
            @Parameter(description = ASSET_TYPE_DESCRIPTION)
            @RequestParam(required = false) String type,
            @Parameter(description = ASSET_TEXT_SEARCH_DESCRIPTION)
            @RequestParam(required = false) String textSearch,
            @Parameter(description = SORT_PROPERTY_DESCRIPTION, schema = @Schema(allowableValues = {"createdTime", "name", "type", "label", "customerTitle"}))
            @RequestParam(required = false) String sortProperty,
            @Parameter(description = SORT_ORDER_DESCRIPTION, schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String sortOrder) throws SobeamException {
        TenantId tenantId = getCurrentUser().getTenantId();
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        if (type != null && type.trim().length() > 0) {
            return checkNotNull(assetService.findAssetsByTenantIdAndType(tenantId, type, pageLink));
        } else {
            return checkNotNull(assetService.findAssetsByTenantId(tenantId, pageLink));
        }
    }

    @ApiOperation(value = "Get Tenant Asset Infos (getTenantAssetInfos)",
            notes = "Returns a page of assets info objects owned by tenant. " +
                    PAGE_DATA_PARAMETERS + ASSET_INFO_DESCRIPTION + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/assetInfos", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<AssetInfo> getTenantAssetInfos(
            @Parameter(description = PAGE_SIZE_DESCRIPTION)
            @RequestParam int pageSize,
            @Parameter(description = PAGE_NUMBER_DESCRIPTION)
            @RequestParam int page,
            @Parameter(description = ASSET_TYPE_DESCRIPTION)
            @RequestParam(required = false) String type,
            @Parameter(description = ASSET_PROFILE_ID_PARAM_DESCRIPTION)
            @RequestParam(required = false) String assetProfileId,
            @Parameter(description = ASSET_TEXT_SEARCH_DESCRIPTION)
            @RequestParam(required = false) String textSearch,
            @Parameter(description = SORT_PROPERTY_DESCRIPTION, schema = @Schema(allowableValues = {"createdTime", "name", "type", "label", "customerTitle"}))
            @RequestParam(required = false) String sortProperty,
            @Parameter(description = SORT_ORDER_DESCRIPTION, schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String sortOrder) throws SobeamException {
        TenantId tenantId = getCurrentUser().getTenantId();
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        if (type != null && type.trim().length() > 0) {
            return checkNotNull(assetService.findAssetInfosByTenantIdAndType(tenantId, type, pageLink));
        } else if (assetProfileId != null && assetProfileId.length() > 0) {
            AssetProfileId profileId = new AssetProfileId(toUUID(assetProfileId));
            return checkNotNull(assetService.findAssetInfosByTenantIdAndAssetProfileId(tenantId, profileId, pageLink));
        } else {
            return checkNotNull(assetService.findAssetInfosByTenantId(tenantId, pageLink));
        }
    }

    @ApiOperation(value = "Get Tenant Asset (getTenantAsset)",
            notes = "Requested asset must be owned by tenant that the user belongs to. " +
                    "Asset name is an unique property of asset. So it can be used to identify the asset." + TENANT_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/assets", params = {"assetName"}, method = RequestMethod.GET)
    @ResponseBody
    public Asset getTenantAsset(
            @Parameter(description = ASSET_NAME_DESCRIPTION)
            @RequestParam String assetName) throws SobeamException {
        TenantId tenantId = getCurrentUser().getTenantId();
        return checkNotNull(assetService.findAssetByTenantIdAndName(tenantId, assetName));
    }

    @ApiOperation(value = "Get Customer Assets (getCustomerAssets)",
            notes = "Returns a page of assets objects assigned to customer. " +
                    PAGE_DATA_PARAMETERS)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/assets", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<Asset> getCustomerAssets(
            @Parameter(description = CUSTOMER_ID_PARAM_DESCRIPTION)
            @PathVariable("customerId") String strCustomerId,
            @Parameter(description = PAGE_SIZE_DESCRIPTION)
            @RequestParam int pageSize,
            @Parameter(description = PAGE_NUMBER_DESCRIPTION)
            @RequestParam int page,
            @Parameter(description = ASSET_TYPE_DESCRIPTION)
            @RequestParam(required = false) String type,
            @Parameter(description = ASSET_TEXT_SEARCH_DESCRIPTION)
            @RequestParam(required = false) String textSearch,
            @Parameter(description = SORT_PROPERTY_DESCRIPTION, schema = @Schema(allowableValues = {"createdTime", "name", "type", "label", "customerTitle"}))
            @RequestParam(required = false) String sortProperty,
            @Parameter(description = SORT_ORDER_DESCRIPTION, schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String sortOrder) throws SobeamException {
        checkParameter("customerId", strCustomerId);
        TenantId tenantId = getCurrentUser().getTenantId();
        CustomerId customerId = new CustomerId(toUUID(strCustomerId));
        checkCustomerId(customerId, Operation.READ);
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        if (type != null && type.trim().length() > 0) {
            return checkNotNull(assetService.findAssetsByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
        } else {
            return checkNotNull(assetService.findAssetsByTenantIdAndCustomerId(tenantId, customerId, pageLink));
        }
    }

    @ApiOperation(value = "Get Customer Asset Infos (getCustomerAssetInfos)",
            notes = "Returns a page of assets info objects assigned to customer. " +
                    PAGE_DATA_PARAMETERS + ASSET_INFO_DESCRIPTION)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/assetInfos", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<AssetInfo> getCustomerAssetInfos(
            @Parameter(description = CUSTOMER_ID_PARAM_DESCRIPTION)
            @PathVariable("customerId") String strCustomerId,
            @Parameter(description = PAGE_SIZE_DESCRIPTION)
            @RequestParam int pageSize,
            @Parameter(description = PAGE_NUMBER_DESCRIPTION)
            @RequestParam int page,
            @Parameter(description = ASSET_TYPE_DESCRIPTION)
            @RequestParam(required = false) String type,
            @Parameter(description = ASSET_PROFILE_ID_PARAM_DESCRIPTION)
            @RequestParam(required = false) String assetProfileId,
            @Parameter(description = ASSET_TEXT_SEARCH_DESCRIPTION)
            @RequestParam(required = false) String textSearch,
            @Parameter(description = SORT_PROPERTY_DESCRIPTION, schema = @Schema(allowableValues = {"createdTime", "name", "type", "label", "customerTitle"}))
            @RequestParam(required = false) String sortProperty,
            @Parameter(description = SORT_ORDER_DESCRIPTION, schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String sortOrder) throws SobeamException {
        checkParameter("customerId", strCustomerId);
        TenantId tenantId = getCurrentUser().getTenantId();
        CustomerId customerId = new CustomerId(toUUID(strCustomerId));
        checkCustomerId(customerId, Operation.READ);
        PageLink pageLink = createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        if (type != null && type.trim().length() > 0) {
            return checkNotNull(assetService.findAssetInfosByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
        } else if (assetProfileId != null && assetProfileId.length() > 0) {
            AssetProfileId profileId = new AssetProfileId(toUUID(assetProfileId));
            return checkNotNull(assetService.findAssetInfosByTenantIdAndCustomerIdAndAssetProfileId(tenantId, customerId, profileId, pageLink));
        } else {
            return checkNotNull(assetService.findAssetInfosByTenantIdAndCustomerId(tenantId, customerId, pageLink));
        }
    }

    @ApiOperation(value = "Get Assets By Ids (getAssetsByIds)",
            notes = "Requested assets must be owned by tenant or assigned to customer which user is performing the request. ")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/assets", params = {"assetIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Asset> getAssetsByIds(
            @Parameter(description = "A list of assets ids, separated by comma ','", array = @ArraySchema(schema = @Schema(type = "string")))
            @RequestParam("assetIds") String[] strAssetIds) throws SobeamException, ExecutionException, InterruptedException {
        checkArrayParameter("assetIds", strAssetIds);
        SecurityUser user = getCurrentUser();
        TenantId tenantId = user.getTenantId();
        CustomerId customerId = user.getCustomerId();
        List<AssetId> assetIds = new ArrayList<>();
        for (String strAssetId : strAssetIds) {
            assetIds.add(new AssetId(toUUID(strAssetId)));
        }
        ListenableFuture<List<Asset>> assets;
        if (customerId == null || customerId.isNullUid()) {
            assets = assetService.findAssetsByTenantIdAndIdsAsync(tenantId, assetIds);
        } else {
            assets = assetService.findAssetsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, assetIds);
        }
        return checkNotNull(assets.get());
    }

    @ApiOperation(value = "Find related assets (findByQuery)",
            notes = "Returns all assets that are related to the specific entity. " +
                    "The entity id, relation type, asset types, depth of the search, and other query parameters defined using complex 'AssetSearchQuery' object. " +
                    "See 'Model' tab of the Parameters for more info.")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/assets", method = RequestMethod.POST)
    @ResponseBody
    public List<Asset> findByQuery(@RequestBody AssetSearchQuery query) throws SobeamException, ExecutionException, InterruptedException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getAssetTypes());
        checkEntityId(query.getParameters().getEntityId(), Operation.READ);
        List<Asset> assets = checkNotNull(assetService.findAssetsByQuery(getTenantId(), query).get());
        assets = assets.stream().filter(asset -> {
            try {
                accessControlService.checkPermission(getCurrentUser(), Resource.ASSET, Operation.READ, asset.getId(), asset);
                return true;
            } catch (SobeamException e) {
                return false;
            }
        }).collect(Collectors.toList());
        return assets;
    }

    @ApiOperation(value = "Get Asset Types (getAssetTypes)",
            notes = "Deprecated. See 'getAssetProfileNames' API from Asset Profile Controller instead." + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/asset/types", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated(since = "3.6.2")
    public List<EntitySubtype> getAssetTypes() throws SobeamException, ExecutionException, InterruptedException {
        SecurityUser user = getCurrentUser();
        TenantId tenantId = user.getTenantId();
        ListenableFuture<List<EntitySubtype>> assetTypes = assetService.findAssetTypesByTenantId(tenantId);
        return checkNotNull(assetTypes.get());
    }

    @ApiOperation(value = "Assign asset to edge (assignAssetToEdge)",
            notes = "Creates assignment of an existing asset to an instance of The Edge. " +
                    EDGE_ASSIGN_ASYNC_FIRST_STEP_DESCRIPTION +
                    "Second, remote edge service will receive a copy of assignment asset " +
                    EDGE_ASSIGN_RECEIVE_STEP_DESCRIPTION +
                    "Third, once asset will be delivered to edge service, it's going to be available for usage on remote edge instance.")
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/edge/{edgeId}/asset/{assetId}", method = RequestMethod.POST)
    @ResponseBody
    public Asset assignAssetToEdge(@Parameter(description = EDGE_ID_PARAM_DESCRIPTION) @PathVariable(EDGE_ID) String strEdgeId,
                                   @Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(EDGE_ID, strEdgeId);
        checkParameter(ASSET_ID, strAssetId);

        EdgeId edgeId = new EdgeId(toUUID(strEdgeId));
        Edge edge = checkEdgeId(edgeId, Operation.READ);

        AssetId assetId = new AssetId(toUUID(strAssetId));
        checkAssetId(assetId, Operation.READ);

        return tbAssetService.assignAssetToEdge(getTenantId(), assetId, edge, getCurrentUser());
    }

    @ApiOperation(value = "Unassign asset from edge (unassignAssetFromEdge)",
            notes = "Clears assignment of the asset to the edge. " +
                    EDGE_UNASSIGN_ASYNC_FIRST_STEP_DESCRIPTION +
                    "Second, remote edge service will receive an 'unassign' command to remove asset " +
                    EDGE_UNASSIGN_RECEIVE_STEP_DESCRIPTION +
                    "Third, once 'unassign' command will be delivered to edge service, it's going to remove asset locally.")
    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/edge/{edgeId}/asset/{assetId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Asset unassignAssetFromEdge(@Parameter(description = EDGE_ID_PARAM_DESCRIPTION) @PathVariable(EDGE_ID) String strEdgeId,
                                       @Parameter(description = ASSET_ID_PARAM_DESCRIPTION) @PathVariable(ASSET_ID) String strAssetId) throws SobeamException {
        checkParameter(EDGE_ID, strEdgeId);
        checkParameter(ASSET_ID, strAssetId);
        EdgeId edgeId = new EdgeId(toUUID(strEdgeId));
        Edge edge = checkEdgeId(edgeId, Operation.READ);

        AssetId assetId = new AssetId(toUUID(strAssetId));
        Asset asset = checkAssetId(assetId, Operation.READ);

        return tbAssetService.unassignAssetFromEdge(getTenantId(), asset, edge, getCurrentUser());
    }

    @ApiOperation(value = "Get assets assigned to edge (getEdgeAssets)",
            notes = "Returns a page of assets assigned to edge. " +
                    PAGE_DATA_PARAMETERS)
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/edge/{edgeId}/assets", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<Asset> getEdgeAssets(
            @Parameter(description = EDGE_ID_PARAM_DESCRIPTION)
            @PathVariable(EDGE_ID) String strEdgeId,
            @Parameter(description = PAGE_SIZE_DESCRIPTION)
            @RequestParam int pageSize,
            @Parameter(description = PAGE_NUMBER_DESCRIPTION)
            @RequestParam int page,
            @Parameter(description = ASSET_TYPE_DESCRIPTION)
            @RequestParam(required = false) String type,
            @Parameter(description = ASSET_TEXT_SEARCH_DESCRIPTION)
            @RequestParam(required = false) String textSearch,
            @Parameter(description = SORT_PROPERTY_DESCRIPTION, schema = @Schema(allowableValues = {"createdTime", "name", "type", "label", "customerTitle"}))
            @RequestParam(required = false) String sortProperty,
            @Parameter(description = SORT_ORDER_DESCRIPTION, schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String sortOrder,
            @Parameter(description = "Timestamp. Assets with creation time before it won't be queried")
            @RequestParam(required = false) Long startTime,
            @Parameter(description = "Timestamp. Assets with creation time after it won't be queried")
            @RequestParam(required = false) Long endTime) throws SobeamException {
        checkParameter(EDGE_ID, strEdgeId);
        TenantId tenantId = getCurrentUser().getTenantId();
        EdgeId edgeId = new EdgeId(toUUID(strEdgeId));
        checkEdgeId(edgeId, Operation.READ);
        TimePageLink pageLink = createTimePageLink(pageSize, page, textSearch, sortProperty, sortOrder, startTime, endTime);
        PageData<Asset> nonFilteredResult;
        if (type != null && type.trim().length() > 0) {
            nonFilteredResult = assetService.findAssetsByTenantIdAndEdgeIdAndType(tenantId, edgeId, type, pageLink);
        } else {
            nonFilteredResult = assetService.findAssetsByTenantIdAndEdgeId(tenantId, edgeId, pageLink);
        }
        List<Asset> filteredAssets = nonFilteredResult.getData().stream().filter(asset -> {
            try {
                accessControlService.checkPermission(getCurrentUser(), Resource.ASSET, Operation.READ, asset.getId(), asset);
                return true;
            } catch (SobeamException e) {
                return false;
            }
        }).collect(Collectors.toList());
        PageData<Asset> filteredResult = new PageData<>(filteredAssets,
                nonFilteredResult.getTotalPages(),
                nonFilteredResult.getTotalElements(),
                nonFilteredResult.hasNext());
        return checkNotNull(filteredResult);
    }

    @ApiOperation(value = "Import the bulk of assets (processAssetsBulkImport)",
            notes = "There's an ability to import the bulk of assets using the only .csv file.")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @PostMapping("/asset/bulk_import")
    public BulkImportResult<Asset> processAssetsBulkImport(@RequestBody BulkImportRequest request) throws Exception {
        SecurityUser user = getCurrentUser();
        return assetBulkImportService.processBulkImport(request, user);
    }

}
